package com.cristis.akka.actors

import akka.actor.{Actor, ActorRef, Props, Timers}
import com.cristis.akka.actors.HeartbeatActor.NotifyAlive

import scala.concurrent.duration._

object DataStorageActor {
  def props(heartbeatActor: ActorRef): Props = Props(new DataStorageActor(heartbeatActor))
  private case object TimerKey
}
class DataStorageActor(heartbeatActor: ActorRef) extends Actor with Timers {
  import DataStorageActor._

  timers.startPeriodicTimer(TimerKey, NotifyAlive, 1000.millis)

  override def receive = {
    case NotifyAlive => heartbeatActor ! NotifyAlive
  }

  override def postRestart(reason: Throwable): Unit = {
    println(s"$self -- Actor restarted!")
  }
}
