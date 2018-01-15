package com.cristis.akka

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.cristis.akka.actors.{Ask, Greet, HelloActor}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._
object Main {

  implicit val timeout: Timeout = Timeout(5 seconds)

  def main(args: Array[String]): Unit = {
    println("Hello world!")

    val system = ActorSystem("helloSystem")
    val helloActor = system.actorOf(Props[HelloActor])


    val resp = helloActor ? Ask("Cristi")
    resp.map {
      case msg: Greet =>
        println(s"Got [${msg.message}]")
    }

    Thread.sleep(3000)
    system.terminate() map {
      _ => println("System shut down...")
    }
  }
}
