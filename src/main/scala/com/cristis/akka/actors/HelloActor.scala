package com.cristis.akka.actors

import akka.actor.Actor


case class Ask(name: String)
case class Greet(message: String)

class HelloActor extends Actor {
  override def receive = {
    case msg: Ask => sender() ! Greet("Hello " + msg.name)
    case _ => throw new Exception("Undefined message received")
  }
}
