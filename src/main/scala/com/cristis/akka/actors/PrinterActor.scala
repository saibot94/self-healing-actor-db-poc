package com.cristis.akka.actors

import akka.actor.Actor

class PrinterActor extends Actor {
  override def receive = {
    case Greet(msg) =>
      println(msg)
  }
}
