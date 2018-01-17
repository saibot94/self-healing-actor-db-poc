package com.cristis.akka.actors

import java.util.logging.Logger

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.cristis.akka.actors.DataStorageActor.BulkGet
import com.cristis.akka.actors.MasterActor.{ActorChange, GetChildrenData, SetDataActors}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object DataPartitionActor {
  def props(dataActors: Seq[ActorRef], partitions: Int, replication: Int): Props =
    Props(new DataPartitionActor(dataActors, partitions, replication))

  /**
    * Get a data value from a key
    *
    * @param key - the key to get from the datastore
    */
  final case class Get(key: String)

  final case class Put(key: String, value: String)

  final case class GetResult(value: Option[String])

  final case class Delete(key: String)

  final case class GetChildrenDataResponse(actors: Seq[Seq[(ActorRef, Map[String, String])]])

  /** Distribute this nodes' values to the other nodes */
  final case class Distribute(actors: Seq[ActorRef])

}

/**
  * An actor that coordinates all the data distribution throughout the system
  *
  * @param dataActors - the actors that actually hold the data
  */
class DataPartitionActor(var dataActors: Seq[ActorRef], partitions: Int, replication: Int) extends Actor {

  import DataPartitionActor._

  implicit val timeout: Timeout = Timeout(500 millis)

  private var dataPartitions: ListBuffer[Seq[ActorRef]] = _
  private var selectedPartition: Int = 0
  implicit val ec = context.system.dispatcher
  private val logger = Logger.getLogger(classOf[DataPartitionActor].getName)
  setupPartitions()

  override def receive = {
    case Get(key: String) =>
      val stringResults: ListBuffer[Option[String]] = getAllValuesForKey(key)
      sender() ! GetResult(stringResults.find(_.isDefined).flatten)

    case Put(key: String, value: String) =>
      deleteKeyForAll(key)
      for (actor <- dataPartitions(selectedPartition)) {
        actor ! Put(key, value)
      }
      logger.info(s"Put kv: [$key -> $value] in partition $selectedPartition")
      selectedPartition = (selectedPartition + 1) % partitions

    case Delete(key: String) =>
      deleteKeyForAll(key)

    case ActorChange(newActors, deadActors) =>
      val affectedPartitions = dataPartitions.zipWithIndex.filter(p => deadActors.exists(d => p._1.contains(d))).map(_._2)
      println("Dead actors: " + deadActors)
      println("Affected partitions: " + affectedPartitions)
      val newActorsPerPart = newActors.size / affectedPartitions.size
      var newActorSplits = affectedPartitions.indices.map { i =>
        newActors.slice(i, i + newActorsPerPart)
      }
      println("New actor splits: " + newActorSplits)

      affectedPartitions.zipWithIndex foreach { case (i, index) =>
        dataPartitions(i) = dataPartitions(i).filterNot(deadActors.toSet)

        // Pick an actor to distribute its data to the newly initiated nodes
        val actorToDistribute = dataPartitions(i).head
        actorToDistribute ! Distribute(newActorSplits(index))
        dataPartitions(i) ++= newActorSplits(index)
      }
      println("Partitions: " + dataPartitions.size)
      dataPartitions.foreach {
        p =>
          println(s"${p.size} --- $p")
      }


    case SetDataActors(actors: Seq[ActorRef]) =>
      dataActors = actors
      setupPartitions()

    case GetChildrenData =>
      val resp = dataPartitions.map { p =>
        p.map {
          a =>
            val res = Try(Await.result((a ? BulkGet).mapTo[Map[String, String]], timeout.duration))
            res match {
              case Success(dataMap) => (a, dataMap)
              case Failure(_) => (a, null)
            }

        }.filter(_._2 != null)
      }
      sender() ! GetChildrenDataResponse(resp)
  }

  private def deleteKeyForAll(key: String): Unit = {
    dataPartitions.flatMap {
      p =>
        p.map {
          pr =>
            pr ? Delete(key)
        }
    }.foreach { res =>
      Try(Await.result(res, timeout.duration))
    }
  }

  private def getAllValuesForKey(key: String) = {
    val results = dataPartitions.flatMap {
      p =>
        p.map {
          pr =>
            (pr ? Get(key)).mapTo[Option[String]]
        }
    }
    val resTry = for (res <- results) yield Try(Await.result(res, timeout.duration))
    val stringResults = resTry.map {
      case Success(r) => r
      case Failure(_) =>
        logger.warning("Failed to get data from an actor, some inconsistencies might be present")
        None
    }
    stringResults
  }

  private def setupPartitions(): Unit = {
    val lb = ListBuffer[Seq[ActorRef]]()
    lb ++= (0 until partitions * replication by replication).map {
      i =>
        dataActors.slice(i, i + replication)
    }
    dataPartitions = lb
  }

}
