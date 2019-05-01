package org.getgraft.http.server.api.v1

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import org.getgraft.http.server.{CorsHandler, HasRoute}
import org.getgraft.service.Services
import org.getgraft.util.JsonUtil

import scala.concurrent.ExecutionContextExecutor

case class StatsHttp(services: Services)(implicit val system: ActorSystem, materializer: ActorMaterializer) extends HasRoute with Directives with LazyLogging {
  implicit val ec: ExecutionContextExecutor = system.dispatcher


  override def route: Route =
    handleExceptions(unexpectedErrorHandler) {
      CorsHandler.handle(
        get {
          path("api" / "v1" / "supernode" / "stats") {
            respondWithHeaders(RawHeader("Cache-Control", "max-age=0")) {
              logger.info("stats-request")
              complete(HttpEntity(contentType = ContentTypes.`application/json`, services.stats.map(stats => JsonUtil.toJson(stats)).getOrElse("{}")))
            }
          }
        })
    }

  private def unexpectedErrorHandler = ExceptionHandler {
    case e: Exception =>
      logger.error(e.getMessage)
      complete(StatusCodes.InternalServerError -> e.getMessage)
  }
}
