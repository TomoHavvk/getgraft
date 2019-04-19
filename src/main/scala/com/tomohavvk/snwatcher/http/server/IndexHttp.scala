package com.tomohavvk.snwatcher.http.server

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directive0, Route, _}
import akka.stream.ActorMaterializer
import com.tomohavvk.snwatcher.service.Services
import com.tomohavvk.snwatcher.util.JsonUtil
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor

case class IndexHttp(services: Services)(implicit val system: ActorSystem, materializer: ActorMaterializer) extends HasRoute with Directives with LazyLogging {
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val ips = scala.collection.mutable.SortedSet.empty[String]
  var indexCounter = 0
  var snCounter = 0

  override def route: Route =
    corsHandler(
      handleRejections(rejectionHandler) {
        extractClientIP { IP =>
          val ip = IP.toOption.map(_.getHostAddress).getOrElse("unknown")

          get {
            path("") {
              indexCounter += 1
              ips += ip
              logger.info("index endpoint: " + ip)
              getFromResource("webapp/dist/index.html")
            } ~ path("sn") {
              logger.info("sn endpoint: " + ip)
              snCounter += 1
              complete(services.nodes.map(nodes => JsonUtil.toJson(nodes)))
            } ~ path("visitor") {
              complete(JsonUtil.toJson(ips))
            }~ path("visitor-size") {
              complete(ips.size.toString)
            }~ path("indexCounter") {
              complete(indexCounter.toString)
            }  ~ path("snCounter") {
              complete(snCounter.toString)
            } ~ {
              getFromResourceDirectory("webapp/dist/")
            }
          }
        }
      })

  implicit def rejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handleNotFound {
        getFromResource("webapp/dist/index.html")
      }.result()


  private val corsResponseHeaders = List(
    `Access-Control-Allow-Origin`.*,
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Allow-Headers`("Authorization",
      "Content-Type", "X-Requested-With")
  )

  private def addAccessControlHeaders: Directive0 =
    respondWithHeaders(corsResponseHeaders)

  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK).
      withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
  }

  def corsHandler(r: Route): Route =
    addAccessControlHeaders {
      preflightRequestHandler ~ r
    }

  def addCORSHeaders(response: HttpResponse): HttpResponse =
    response.withHeaders(corsResponseHeaders)
}

