package com.cristis.akka.actors

import akka.actor.{Actor, ActorRef, Props}


final case class Ask(name: String)
final case class Greet(message: String)

object HelloActor {
  def props(printerActor: ActorRef): Props = Props(new HelloActor(printerActor))
}

class HelloActor(printerActor: ActorRef) extends Actor {


  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("I'm in prerestart")
  }


  override def postRestart(reason: Throwable): Unit = {
    println("I'm in postrestart")
  }

  override def receive = {
    case msg: Ask => printerActor ! Greet("Hello " + msg.name)
  }
}
