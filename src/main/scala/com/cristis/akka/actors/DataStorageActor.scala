package com.cristis.akka.actors

import akka.actor.{Actor, ActorRef, Props, Timers}
import com.cristis.akka.actors.HeartbeatActor.NotifyAlive

import scala.concurrent.duration._

object DataStorageActor {
  def props(heartbeatActor: ActorRef, id: Int): Props = Props(new DataStorageActor(heartbeatActor, id))
  private case object TimerKey
  final case class BulkLoad(data: Map[String, String])
}
class DataStorageActor(heartbeatActor: ActorRef, val id: Int) extends Actor with Timers {
  import DataStorageActor._
  import DataPartitionActor._

  var dataStore: Map[String, String] = Map()

  timers.startPeriodicTimer(TimerKey, NotifyAlive, 1000.millis)

  override def receive = {
    case NotifyAlive => heartbeatActor ! NotifyAlive

    case Get(key: String) =>
      sender() ! dataStore.get(key)

    case Put(key: String, value: String) =>
      dataStore += key -> value

    case Distribute(actors) =>
      actors.foreach {
        a => a ! BulkLoad(dataStore)
      }

    case BulkLoad(data) =>
      this.dataStore = data
  }

  override def postRestart(reason: Throwable): Unit = {
    println(s"$self -- Actor restarted!")
  }
}
