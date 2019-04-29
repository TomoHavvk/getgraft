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

  // TODO implement prometheus metrics
  val ips = scala.collection.mutable.SortedSet.empty[String]
  var indexCounter = 0
  var snCounter = 0

  override def route: Route =
    CorsHandler.handle(
      handleExceptions(unexpectedErrorHandler) {
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
              } ~ path("visitor-size") {
                complete(ips.size.toString)
              } ~ path("indexCounter") {
                complete(indexCounter.toString)
              } ~ path("snCounter") {
                complete(snCounter.toString)
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
