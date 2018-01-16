package com.cristis.akka.actors

import java.util.logging.Logger

import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.{Actor, ActorRef, Kill, PoisonPill, Props, Timers}
import com.cristis.akka.actors.HeartbeatActor.{DeadActors, GetHealthChecks}
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.parallel.mutable
import scala.concurrent.duration._
import scala.collection.parallel.mutable._
import scala.concurrent.{Await, ExecutionContext, duration}


object MasterActor {
  case object GetChildren
  final case class SetDataActors(dataActors: Seq[ActorRef])
  def props(replication: Int, ec: ExecutionContext): Props = Props(new MasterActor(replication, ec))

  val ReplicationMultiplier = 3
}

class MasterActor(replication: Int, implicit val ec: ExecutionContext) extends Actor with Timers {

  implicit val timeout: Timeout = Timeout(1 second)

  private var heartbeatActor: ActorRef = _
  private var dataPartitionActor: ActorRef = _
  private val logger: Logger = Logger.getLogger(classOf[MasterActor].getName)
  import MasterActor._


  override def preStart(): Unit = {
    heartbeatActor = context.system.actorOf(HeartbeatActor.props(self))
    val childrenDataStorageActors = (0 until replication*ReplicationMultiplier).map(id => {
      context.actorOf(DataStorageActor.props(heartbeatActor), s"dataStorage_$id")
    })
    dataPartitionActor = context.system.actorOf(DataPartitionActor.props(childrenDataStorageActors))


    childrenDataStorageActors.head ! PoisonPill
    childrenDataStorageActors.tail.head ! Kill
    heartbeatActor ! SetDataActors(childrenDataStorageActors)
  }


  override def receive = {
    case DeadActors(deadActors: Seq[ActorRef]) =>

      heartbeatActor ! SetDataActors(context.children.toSeq)

    case GetChildren =>
      sender() ! Await.result(heartbeatActor ? GetHealthChecks, timeout.duration)
  }
}
