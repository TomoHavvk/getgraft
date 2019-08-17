package org.getgraft.http.server

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, ExceptionHandler, RejectionHandler, Route}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import org.getgraft.service.Services
import org.getgraft.util.JsonUtil

import scala.concurrent.ExecutionContextExecutor

case class IndexHttp(services: Services)(implicit val system: ActorSystem, materializer: ActorMaterializer) extends HasRoute with Directives with LazyLogging {
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  override def route: Route =
    CorsHandler.handle(
      handleExceptions(unexpectedErrorHandler) {
        handleRejections(rejectionHandler) {

          scheme("http") {
            extract(_.request.uri) { uri =>
              redirect(uri.withScheme("https"), StatusCodes.MovedPermanently)
            }
          } ~ scheme("https") {
            get {
              pathSingleSlash {
                getFromResource("webapp/dist/index.html")
              } ~ path("sn") {
                complete(services.nodes.map(nodes => JsonUtil.toJson(nodes)))
              } ~ {
                getFromResourceDirectory("webapp/dist/")
              }
            }
          }
        }
      })

  private def unexpectedErrorHandler = ExceptionHandler {
    case e: Exception =>
      logger.error(e.getMessage)
      complete(StatusCodes.InternalServerError -> e.getMessage)
  }

  private implicit def rejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handleNotFound {
        getFromResource("webapp/dist/index.html")
      }.result()
}
