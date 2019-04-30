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

        Nodes(asView, height, totalStake)
      })
    })
  }

  // TODO Refactoring
  def stats: Option[Stats] = {
    val stimulus = 112000
    Supernode.height.flatMap(height => {
      Supernode.nodes.map(nodes => {
        val asView = nodes.map(_.asView(height))

        val totalNodes = asView.count(_.isOnline)

        val t1Count = asView.count(_.BlockchainBasedListTier == 1)
        val t2Count = asView.count(_.BlockchainBasedListTier == 2)
        val t3Count = asView.count(_.BlockchainBasedListTier == 3)
        val t4Count = asView.count(_.BlockchainBasedListTier == 4)

        val t1 = Tier(1, t1Count, BigDecimal(((stimulus.doubleValue() / 4 / (t1Count * 50000)) * 30) * 100).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
        val t2 = Tier(2, t2Count, BigDecimal(((stimulus.doubleValue() / 4 / (t2Count * 90000)) * 30) * 100).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
        val t3 = Tier(3, t3Count, BigDecimal(((stimulus.doubleValue() / 4 / (t3Count * 150000)) * 30) * 100).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
        val t4 = Tier(4, t4Count, BigDecimal(((stimulus.doubleValue() / 4 / (t4Count * 250000)) * 30) * 100).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)


        Stats(totalNodes, List(t1, t2, t3, t4))
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
