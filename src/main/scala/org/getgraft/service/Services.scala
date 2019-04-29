package org.getgraft.service

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Scheduler}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import org.getgraft.http.client.SupernodeHttpClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration

case class Services(implicit val system: ActorSystem, materializer: ActorMaterializer) extends LazyLogging {

  private val scheduler: Scheduler = system.scheduler
  private implicit val ec: ExecutionContextExecutor = system.dispatcher

  def nodes: Option[Nodes] = {
    Supernode.height.flatMap(height => {
      Supernode.nodes.map(nodes => {
        val asView = nodes.map(_.asView(height))

        val totalStake = asView.map(_.StakeAmount).sum
        val t1 = asView.count(_.BlockchainBasedListTier == 1)
        val t2 = asView.count(_.BlockchainBasedListTier == 2)
        val t3 = asView.count(_.BlockchainBasedListTier == 3)
        val t4 = asView.count(_.BlockchainBasedListTier == 4)
        val info = Info(asView.count(_.isOnline), asView.count(!_.isOnline), totalStake, t1, t2, t3, t4)

        Nodes(asView, info, height)
      })
    })
  }

  private val refreshNodes: Runnable = () => {
    try {
      SupernodeHttpClient.get().foreach(data => Supernode.updateOnlineNodes(data))
      SupernodeHttpClient.get(withOffline = true).foreach(data => Supernode.updateOfflineNodes(data))
    } catch {
      case e: Throwable => logger.error(e.getMessage)
    }
  }

  scheduler.schedule(
    initialDelay = Duration(2, TimeUnit.SECONDS),
    interval = Duration(50, TimeUnit.SECONDS),
    runnable = refreshNodes)
}
