package com.cristis.akka.actors

import java.util.logging.Logger

import akka.actor.{Actor, ActorRef, Props, Timers}
import com.cristis.akka.actors.HeartbeatActor.NotifyAlive

import scala.concurrent.duration._

object DataStorageActor {
  def props(heartbeatActor: ActorRef, id: Int): Props = Props(new DataStorageActor(heartbeatActor, id))
  private case object TimerKey
  final case class BulkLoad(data: Map[String, String])
  case object BulkGet
}
class DataStorageActor(heartbeatActor: ActorRef, val id: Int) extends Actor with Timers {
  import DataStorageActor._
  import DataPartitionActor._

  private val logger = Logger.getLogger(classOf[DataStorageActor].getName)
  var dataStore: Map[String, String] = Map()

  timers.startPeriodicTimer(TimerKey, NotifyAlive, 1000.millis)

  override def receive = {
    case NotifyAlive => heartbeatActor ! NotifyAlive

    case Get(key: String) =>
      sender() ! dataStore.get(key)

    case Put(key: String, value: String) =>
      logger.info(s"Putting kv: [$key, $value]")
      dataStore += key -> value

    case Distribute(actors) =>
      logger.info(s"Distributing data to $actors")
      actors.foreach {
        a => a ! BulkLoad(dataStore)
      }

    case BulkLoad(data) =>
      logger.info(s"Loading data from ${sender()}")
      this.dataStore = data

    case BulkGet => sender() ! dataStore

    case Delete(key: String) =>
      if(dataStore.contains(key)) {
        logger.info(s"Deleting key: $key")
        dataStore -= key
      }
      sender() ! "ok"
  }

  override def postRestart(reason: Throwable): Unit = {
    println(s"$self -- Actor restarted!")
  }
}
