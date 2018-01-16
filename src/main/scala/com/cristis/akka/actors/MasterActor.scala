package com.cristis.akka.actors

import java.util.logging.Logger

import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.{Actor, ActorRef, Kill, PoisonPill, Props, Timers}

import scala.collection.parallel.mutable
import scala.concurrent.duration._
import scala.collection.parallel.mutable._
import scala.concurrent.duration


object MasterActor {

  private case object HeartbeatCheckKey
  private case object HeartbeatCheck
  case object Get

  case object NotifyAlive

  def props(replication: Int): Props = Props(new MasterActor(replication))

  val ReplicationMultiplier = 3
}

class MasterActor(replication: Int) extends Actor with Timers {



  private val logger: Logger = Logger.getLogger(classOf[MasterActor].getName)
  import MasterActor._

  val healthChecks: mutable.ParHashMap[ActorRef, Long] = mutable.ParHashMap()

  var childrenDataStorageActors: Seq[ActorRef] = _

  override def preStart(): Unit = {
    childrenDataStorageActors = (0 until replication*ReplicationMultiplier).map(id => {
      context.actorOf(DataStorageActor.props(self), s"dataStorage_$id")
    })
    childrenDataStorageActors.foreach(c => {
      healthChecks += c -> System.currentTimeMillis()
    })
    timers.startPeriodicTimer(HeartbeatCheckKey, HeartbeatCheck, 1000.millis)

    childrenDataStorageActors.head ! PoisonPill
    childrenDataStorageActors.tail.head ! Kill
  }


  override def receive = {
    case HeartbeatCheck =>
      val deadActors = healthChecks.filter {
        case (_, l) =>
          val diff = (System.currentTimeMillis() - l).millis
          diff >= 5.seconds
      }

      if(deadActors.nonEmpty) {
        logger.info("These actors seem to be dead: " + deadActors)
        deadActors.foreach { case (a, l) => a ! Resume }

        println(context.children)
      }
      println(" ")


    case NotifyAlive =>
      val lastHeartbeatOpt = healthChecks.get(sender())
      if (lastHeartbeatOpt.isDefined) {
        val diff = (System.currentTimeMillis() - lastHeartbeatOpt.get).millis
        println(s"${sender()} -> ${diff.toUnit(duration.SECONDS)}s since last contact")
        healthChecks += sender() -> System.currentTimeMillis()
      }

    case Get =>
      sender() ! List(1,2,3,4)
  }
}
