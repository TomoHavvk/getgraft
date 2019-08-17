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

  def stats: Option[Stats] = Supernode.nodeStats
}
