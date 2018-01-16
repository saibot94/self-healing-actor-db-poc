package com.cristis.akka

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.cristis.akka.actors.DataPartitionActor.{Get, GetResult, Put}
import com.cristis.akka.actors.MasterActor
import com.cristis.akka.actors.MasterActor.GetChildren
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.collection.parallel.mutable
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._
import scala.io.StdIn


case class ActorHealthcheck(actorPath: String, lastResponse: Double)

case class GetActorsResponse(resp: List[ActorHealthcheck])


object Main {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(1 second)
  val masterActor: ActorRef = system.actorOf(MasterActor.props(4, executionContext), "master")

//  implicit val getActorsResponseFormat = jsonFormat1(GetActorsResponse)
  implicit val actorHealthCheckFormat = jsonFormat2(ActorHealthcheck)

  def main(args: Array[String]): Unit = {

    val route = buildRoutes
    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    println("Server online att http://localhost:9000/. Press anykey to stop")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }


  def buildRoutes = path("data") {
    get {
      parameter("key".as[String]) { key =>
        val response = (masterActor ? Get(key)).mapTo[GetResult]
        complete {
           response.map {
             r => r.value
           }
        }
      }
    } ~
      put {
        parameter("key".as[String], "value".as[String]) {
          (key, value) =>
            masterActor ! Put(key, value)
            complete("Successfully put value!")
        }
      }
  } ~
    path("data" / "all") {
      get {
        complete("hola")
      }
    } ~
    path("actors") {
      get {
        val future = (masterActor ? GetChildren).mapTo[mutable.ParHashMap[ActorRef, Long]]
        val response = future.map { r =>
          val currentTime = System.currentTimeMillis()
          val actorHealthchecks = r.map {
            case (actor, lastTimestamp) =>
              val path = actor.path.toString
              ActorHealthcheck(path, (currentTime - lastTimestamp).millis.toUnit(SECONDS))
          }.toSeq.seq.toList
          val gar = GetActorsResponse(actorHealthchecks)
          println("Get actors: " + gar)
          actorHealthchecks
        }
        complete {
          response.map { r => r }
        }
      }
    }

}
