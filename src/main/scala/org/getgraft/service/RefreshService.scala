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

  def startRefreshingStats(implicit system: ActorSystem, materializer: ActorMaterializer): Cancellable = {
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    implicit val scheduler: Scheduler = system.scheduler

    val refreshStatsTask: Runnable = () => {
      try {
        Supernode.height.flatMap(height => {
          Supernode.nodes.map(nodes => {
            val asView = nodes.filter(_.IsOnline).map(_.asView(height))

            val t1Count = asView.count(_.BlockchainBasedListTier == 1)
            val t2Count = asView.count(_.BlockchainBasedListTier == 2)
            val t3Count = asView.count(_.BlockchainBasedListTier == 3)
            val t4Count = asView.count(_.BlockchainBasedListTier == 4)

            val t1 = Tier(1, t1Count, RoiService.monthlyRoi(t1Count, 50000))
            val t2 = Tier(2, t2Count, RoiService.monthlyRoi(t2Count, 90000))
            val t3 = Tier(3, t3Count, RoiService.monthlyRoi(t3Count, 150000))
            val t4 = Tier(4, t4Count, RoiService.monthlyRoi(t4Count, 250000))

            val totalNodes = asView.size
            Supernode.updateNodeStats(Stats(totalNodes, List(t1, t2, t3, t4)))
          })
        })
      } catch {
        case e: Throwable => logger.error(e.getMessage)
      }
    }

    scheduler.schedule(
      initialDelay = Duration(6, TimeUnit.SECONDS),
      interval = Duration(52, TimeUnit.SECONDS),
      runnable = refreshStatsTask)
  }
}
