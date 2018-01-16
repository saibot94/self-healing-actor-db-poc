package com.cristis.akka.actors

import akka.actor.{Actor, ActorRef, Props}


final case class Ask(name: String)
final case class Greet(message: String)

object HelloActor {
  def props(printerActor: ActorRef): Props = Props(new HelloActor(printerActor))
}

class HelloActor(printerActor: ActorRef) extends Actor {
  override def receive = {
    case msg: Ask => printerActor ! Greet("Hello " + msg.name)
    case _ => throw new Exception("Undefined message received")
  }
}
