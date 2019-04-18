package com.tomohavvk.snwatcher.actors

import akka.actor.Actor
import com.tomohavvk.snwatcher.actors.SupernodeActor.Nodes
import com.tomohavvk.snwatcher.http.client.SupernodeHttpClient.{Info, Node}
import com.typesafe.scalalogging.LazyLogging


class SupernodeActor extends Actor with LazyLogging {

  private var onlineNodeList = List.empty[Node]
  private var offlineNodeList = List.empty[Node]
  private var allNodes = List.empty[Node]
  private var currentBlock: Long = 0

  override def receive: Receive = {
    case Nodes                            =>
      val online = onlineNodeList.map(_.asView(currentBlock)).sortWith(_.StakeExpiringBlock < _.StakeExpiringBlock)/*.take(200)*/
      val offline = offlineNodeList.map(_.asView(currentBlock)).sortWith(_.StakeExpiringBlock < _.StakeExpiringBlock)

      val totalStake = online.map(_.StakeAmount).sum + offline.map(_.StakeAmount).sum
      val t1 = online.count(_.BlockchainBasedListTier == 1)
      val t2 = online.count(_.BlockchainBasedListTier == 2)
      val t3 = online.count(_.BlockchainBasedListTier == 3)
      val t4 = online.count(_.BlockchainBasedListTier == 4)
      val info = Info(online.size, totalStake, t1, t2, t3, t4)

      sender() ! Nodes(online, offline, info, currentBlock)

    case Nodes(online, onlineWithOffline, _, height) =>
      onlineNodeList = online.map(_.copy(isOnline = true))
      allNodes = onlineWithOffline
      offlineNodeList = onlineWithOffline.filter(x => !online.exists(z => x.PublicId == z.PublicId) && x.StakeAmount > 0)
      currentBlock = height

      logger.info("onlineNodeList size: " + onlineNodeList.size)
      logger.info("offlineNodeList size: " + offlineNodeList.size)
      logger.info("allNodes size: " + allNodes.size)



    case _                                 =>
      logger.warn("WTF")
  }
}

object SupernodeActor {

  case object Nodes
  case class Nodes(onlineNodes: List[Node], offlineNodes:  List[Node], info: Info, height: Long)

}