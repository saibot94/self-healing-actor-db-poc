package com.cristis.akka.actors

import java.util.logging.Logger

import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.{Actor, ActorRef, Kill, PoisonPill, Props, Timers}
import com.cristis.akka.actors.HeartbeatActor.{DeadActors, GetHealthChecks}
import akka.pattern.ask
import akka.util.Timeout
import com.cristis.akka.actors.DataPartitionActor.{Delete, Get, GetResult, Put}

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.mutable
import scala.concurrent.duration._
import scala.collection.parallel.mutable._
import scala.concurrent.{Await, ExecutionContext, duration}


object MasterActor {
  case object GetChildren
  case object GetChildrenData
  case class ActorChange(newActors: Seq[ActorRef], deadActors: Seq[ActorRef])
  final case class SetDataActors(dataActors: Seq[ActorRef])
  def props(replication: Int, ec: ExecutionContext): Props = Props(new MasterActor(replication, ec))

  val ReplicationMultiplier = 3
}

class MasterActor(replication: Int, implicit val ec: ExecutionContext) extends Actor with Timers {
  import MasterActor._

  implicit val timeout: Timeout = Timeout(1 second)

  private var heartbeatActor: ActorRef = _
  private var dataPartitionActor: ActorRef = _
  private var maxId = replication*ReplicationMultiplier
  private var childrenDataStorageActors: ListBuffer[ActorRef] = ListBuffer()
  private val logger: Logger = Logger.getLogger(classOf[MasterActor].getName)
  import MasterActor._


  override def preStart(): Unit = {
    heartbeatActor = context.system.actorOf(HeartbeatActor.props(self))
     childrenDataStorageActors ++= (0 until maxId).map(id => {
      context.actorOf(DataStorageActor.props(heartbeatActor, id), s"dataStorage_$id")
    })
    dataPartitionActor = context.system.actorOf(DataPartitionActor.props(childrenDataStorageActors, ReplicationMultiplier, replication))


    heartbeatActor ! SetDataActors(childrenDataStorageActors)
  }


  override def receive = {
    case DeadActors(deadActors: Seq[ActorRef]) =>
      val newActors = deadActors.map { a =>
        maxId += 1
        context.actorOf(DataStorageActor.props(heartbeatActor, maxId), s"dataStorage_$maxId")
      }
      childrenDataStorageActors = childrenDataStorageActors.filterNot(deadActors.toSet) ++ newActors
      dataPartitionActor ! ActorChange(newActors, deadActors)
      heartbeatActor ! SetDataActors(childrenDataStorageActors)

    case GetChildren =>
      sender() ! Await.result(heartbeatActor ? GetHealthChecks, timeout.duration)

    case put: Put =>
      logger.info(s"Putting data: $put")
      dataPartitionActor ! put

    case get: Get =>
      sender() ! Await.result(dataPartitionActor ? get, timeout.duration)

    case GetChildrenData =>
      sender() ! Await.result(dataPartitionActor ? GetChildrenData, timeout.duration)

    case delete: Delete => dataPartitionActor ! delete
  }
}
