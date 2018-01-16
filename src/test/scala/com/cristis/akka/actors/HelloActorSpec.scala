package com.cristis.akka.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class HelloActorSpec(_system: ActorSystem) extends TestKit(_system) with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {


  def this() = this(ActorSystem("AkkaQuickstartSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "A greeter actor" should "pass on a greeting" in {
    val testProbe = TestProbe()
    val person = "Cristi"
    val helloGreeter = system.actorOf(HelloActor.props(testProbe.ref))
    helloGreeter ! Ask(person)
    testProbe.expectMsg(500 millis, Greet(s"Hello $person"))
  }
}
