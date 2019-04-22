package com.tomohavvk.snwatcher.http.server

import java.time.Instant

import akka.Done
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.duration._

trait HasRoute {
  def route: Route
}

class HttpServer(routes: Seq[HasRoute])(
  implicit private val system: ActorSystem,
  implicit private val materializer: Materializer
) extends LazyLogging {

  import HttpServer._

  private val logging = Logging(system, this.getClass)

  val startedAt: Instant = Instant.now

  val route: Route = routes map (_.route) reduce { _ ~ _ }

  val serverBinding: Future[Http.ServerBinding] = {
    logger.info(s"Starting AkkaHttp server on port $port")

    Http().bindAndHandle(route, interface = interface, port = port)
  }

  def shutdown(hardDeadline: FiniteDuration): Future[Done] = {
    import system.dispatcher

    serverBinding flatMap (_ terminate hardDeadline) map (_ => Done)
  }
}

object HttpServer {
  private val config = ConfigFactory.load() getConfig "snwatcher.http.server"
  private val interface = config.getString("interface")
  private val port = config.getInt("port")

  def apply(routes: HasRoute*)(implicit system: ActorSystem, materializer: Materializer): HttpServer = {
    new HttpServer(Seq(routes: _*))
  }
}