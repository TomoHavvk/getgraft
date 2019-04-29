package org.getgraft

import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import org.getgraft.http.server.{HttpServer, IndexHttp, MetricsHttp, ShutdownHttp}
import org.getgraft.service.Services

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

object Boot extends App with LazyLogging {

  implicit val system = ActorSystem("getgraft-system")
  implicit val matr = ActorMaterializer()
  implicit val ec = system.dispatcher

  private val stopping: AtomicBoolean = new AtomicBoolean(false)
  private val _whenStopped = Promise[Done]
  def whenStopped: Future[Done] = _whenStopped.future

  val services = Services()
  val indexHttp = IndexHttp(services)
  val metricsHttp = new MetricsHttp()
  val shutdownHttp = new ShutdownHttp(() => shutdown())

  lazy val http = HttpServer(shutdownHttp, metricsHttp, indexHttp)
  def start(): Unit = {

    val version: String = Option(getClass.getPackage.getImplementationVersion) getOrElse "development"

    http.serverBinding.onComplete {
      case Success(_) =>
        logger.info(s"Server version: $version")
        logger.info(s"Started in ${Instant.now.toEpochMilli - http.startedAt.toEpochMilli}ms")
      case Failure(err) =>
        logger.error(s"Server error: $err")
        sys.exit(1)
    }
  }

  def shutdown(): Future[Done] = {
    if (stopping.compareAndSet(false, true)) {
      val stopped = for {
        _ <- http.shutdown(5.seconds)
      } yield {

        val u = FiniteDuration(Instant.now.toEpochMilli - http.startedAt.toEpochMilli, MILLISECONDS)
        logger.info(s"Uptime: ${u.toDays}d ${u.toHours % 24}h ${u.toMinutes % 60}m ${u.toSeconds % 60}s")
        logger.info(s"Shutdown done in ${Instant.now.toEpochMilli - http.startedAt.toEpochMilli}ms")
        Done
      }
      _whenStopped completeWith stopped
    }
    whenStopped
  }
  sys addShutdownHook shutdown()
  start()
}
