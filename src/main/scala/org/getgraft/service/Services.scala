package org.getgraft.service

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor

case class Services(implicit val system: ActorSystem, materializer: ActorMaterializer) extends LazyLogging {

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

  def stats: Option[Stats] = {
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
        Stats(totalNodes, List(t1, t2, t3, t4))
      })
    })
  }
}
