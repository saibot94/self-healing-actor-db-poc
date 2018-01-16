package com.cristis.akka.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.cristis.akka.actors.MasterActor.{ActorChange, SetDataActors}

import scala.concurrent.Await
import scala.concurrent.duration._

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

  implicit val timeout: Timeout = Timeout(1 second)

  private var dataPartitions: List[List[ActorRef]] = _
  private var selectedPartition: Int = 0
  setupPartitions()

  override def receive = {
    case Get(key: String) =>
      val results = for (future <- dataPartitions.flatMap {
        p =>
          p.map {
            pr =>
              (pr ? Get(key)).mapTo[Option[String]]
          }
      }) yield {
        Await.result(future, 500.millis)
      }

      sender() ! GetResult(results.find(_.isDefined).flatten)

    case Put(key: String, value: String) =>
      for (actor <- dataPartitions(selectedPartition)) {
        actor ! Put(key, value)
      }
      selectedPartition = (selectedPartition + 1) % partitions

    case ActorChange(newActors, deadActors) =>
      dataActors = dataActors.filterNot(p => deadActors.contains(p)) ++ newActors
      setupPartitions()
      println("Partitions: " + dataPartitions.size)
      dataPartitions.foreach {
        p =>
          println(p)
          print(" ----  -- " + dataPartitions.head.size)
      }
      println("")
      val index = dataPartitions.zipWithIndex.filter { case (l, i) => l.contains(newActors.head) }.head._2

      val goodActorOption = dataPartitions(index).filterNot(a => newActors.contains(a)).headOption
      if (goodActorOption.isDefined) {
        goodActorOption.foreach {
          ac => ac ! Distribute(newActors)
        }
      }

    case SetDataActors(actors: Seq[ActorRef]) =>
      dataActors = actors
      setupPartitions()
  }

  private def setupPartitions(): Unit = {
    dataPartitions = (0 until partitions).map {
      i =>
        (0 until replication).map {
          j =>
            dataActors(i + j)
        }.toList
    }.toList
  }

}
