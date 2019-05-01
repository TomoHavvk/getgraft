package org.getgraft.service

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Cancellable, Scheduler}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import org.getgraft.http.client.SupernodeHttpClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration

object RefreshService extends LazyLogging {

  def startRefreshingNodes(implicit system: ActorSystem, materializer: ActorMaterializer): Cancellable = {
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    implicit val scheduler: Scheduler = system.scheduler

    val refreshNodesTask: Runnable = () => {
      try {
        logger.debug("Nodes refreshing start")
        SupernodeHttpClient.get().foreach(data => if (data.contains("jsonrpc")) Supernode.updateOnlineNodes(data) else logger.warn(data))
        SupernodeHttpClient.get(withOffline = true).foreach(data => if (data.contains("jsonrpc")) Supernode.updateOfflineNodes(data) else logger.warn(data))

        logger.debug("Nodes successfully refreshed")
      } catch {
        case e: Throwable => logger.error(e.getMessage)
      }
    }

    scheduler.schedule(
      initialDelay = Duration(5, TimeUnit.SECONDS),
      interval = Duration(50, TimeUnit.SECONDS),
      runnable = refreshNodesTask)
  }
}
