package com.cristis.akka

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.cristis.akka.actors.{Ask, Greet, HelloActor, PrinterActor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main {

  implicit val timeout: Timeout = Timeout(5 seconds)

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("helloSystem")
    val printerActor = system.actorOf(Props[PrinterActor])
    val helloActor = system.actorOf(HelloActor.props(printerActor))


    helloActor ! Ask("Cristi")
    helloActor ! Ask("Andrei")
    helloActor ! Ask("Gigel")

    Thread.sleep(3000)
    system.terminate() map {
      _ => println("System shut down...")
    }
  }
}
