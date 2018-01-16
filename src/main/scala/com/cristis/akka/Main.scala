package com.cristis.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask
import com.cristis.akka.actors.MasterActor
import com.cristis.akka.actors.MasterActor.Get

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.io.StdIn
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

case class Resp(resp: List[Int])


object Main {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(1 second)
  val masterActor = system.actorOf(MasterActor.props(4), "master")

  implicit val responseFormat = jsonFormat1(Resp)
  def main(args: Array[String]): Unit = {

    val route = path("auction") {
      get {
        val future = (masterActor ? Get).mapTo[List[Int]]
        complete {
          future.map {
            r => Resp(r)
          }
        }
      }
    }
    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    println("Server online att http://localhost:9000/. Press anykey to stop")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }



}
