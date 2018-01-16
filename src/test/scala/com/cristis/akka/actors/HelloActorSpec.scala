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


}
