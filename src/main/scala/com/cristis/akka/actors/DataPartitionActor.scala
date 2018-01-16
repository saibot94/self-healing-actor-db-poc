package com.cristis.akka.actors

import akka.actor.{Actor, ActorRef, Props}
import com.cristis.akka.actors.DataPartitionActor.Get
import com.cristis.akka.actors.MasterActor.SetDataActors

object DataPartitionActor {
  def props(dataActors: Seq[ActorRef]): Props = Props(new DataPartitionActor(dataActors))

  /**
    * Get a data value from a key
    *
    * @param key - the key to get from the datastore
    */
  final case class Get(key: String)

}

/**
  * An actor that coordinates all the data distribution throughout the system
  *
  * @param dataActors - the actors that actually hold the data
  */
class DataPartitionActor(var dataActors: Seq[ActorRef]) extends Actor {

  private var partitions: List[List[ActorRef]] = _

  override def receive = {
    case Get(key: String) =>
    // Do something here

    case SetDataActors(actors: Seq[ActorRef]) =>
      dataActors = actors

  }

}
