package com.cristis.akka.actors

import akka.actor.{Actor, ActorRef, Props, Timers}
import scala.concurrent.duration._

object DataStorageActor {
  def props(masterActor: ActorRef): Props = Props(new DataStorageActor(masterActor))

  private case object TimerKey
}
class DataStorageActor(masterActor: ActorRef) extends Actor with Timers {
  import MasterActor._
  import DataStorageActor._

  timers.startPeriodicTimer(TimerKey, NotifyAlive, 1000.millis)

  def receive = {
    case NotifyAlive => masterActor ! NotifyAlive
  }

  override def postRestart(reason: Throwable): Unit = {
    println(s"$self -- Actor restarted!")
  }
}
