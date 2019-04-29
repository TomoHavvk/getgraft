package org.getgraft.http.server

import akka.Done
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future

class ShutdownHttp(run: () => Future[Done]) extends HasRoute with Directives with StrictLogging {
  override def route: Route = get {
      path("shutdown") {
        run()
        complete(StatusCodes.OK -> "OK")
      }
  }
}
