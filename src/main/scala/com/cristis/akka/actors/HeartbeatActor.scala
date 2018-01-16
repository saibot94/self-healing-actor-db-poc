package com.cristis.akka.actors

import java.util.logging.Logger

import akka.actor.{Actor, ActorRef, Props, Timers}
import com.cristis.akka.actors.MasterActor.SetDataActors

import scala.collection.parallel.mutable
import scala.concurrent.duration._

object HeartbeatActor {

  private case object HeartbeatCheckKey
  private case object HeartbeatCheck
  case object GetHealthChecks
  case object NotifyAlive
  final case class DeadActors(deadActors: Seq[ActorRef])

  def props(master:ActorRef): Props = {
    Props(new HeartbeatActor(master))
  }
}

class HeartbeatActor(master: ActorRef) extends Actor with Timers {
  import HeartbeatActor._
  private val logger = Logger.getLogger(classOf[HeartbeatActor].getName)
  var healthChecks: mutable.ParHashMap[ActorRef, Long] = mutable.ParHashMap()
  var dataActors: Seq[ActorRef] = _

  override def preStart(): Unit = {
    timers.startPeriodicTimer(HeartbeatCheckKey, HeartbeatCheck, 2000.millis)
  }

  def receive = {

    case NotifyAlive =>
      val lastHeartbeatOpt = healthChecks.get(sender())
      if (lastHeartbeatOpt.isDefined) {
        val diff = (System.currentTimeMillis() - lastHeartbeatOpt.get).millis
        //        println(s"${sender()} -> ${diff.toUnit(duration.SECONDS)}s since last contact")
        healthChecks += sender() -> System.currentTimeMillis()
      }

    case HeartbeatCheck =>
      val deadActors = healthChecks.filter {
        case (_, l) =>
          val diff = (System.currentTimeMillis() - l).millis
          diff >= 5.seconds
      }

      if(deadActors.nonEmpty) {
        logger.info("These actors seem to be dead: " + deadActors + ", telling the master!")
        val deadActorsMessage = DeadActors(deadActors.keys.seq.toSeq)
        master ! deadActorsMessage
      }

    case SetDataActors(dataActors: Seq[ActorRef]) =>
      this.dataActors = dataActors
      healthChecks = mutable.ParHashMap()
      this.dataActors.foreach(c => {
        healthChecks += c -> System.currentTimeMillis()
      })


    case GetHealthChecks => sender() ! healthChecks
  }
}
