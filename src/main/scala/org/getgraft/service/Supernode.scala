package org.getgraft.service

import com.typesafe.scalalogging.LazyLogging
import org.getgraft.redis.RClient
import org.getgraft.util.JsonUtil


case object Supernode extends LazyLogging {

  def nodes: Option[List[Node]] = {
    concatNodes(List(onlineNodes.map(_.result.items).map(x => x.map(_.copy(IsOnline = true))), offlineNodes.map(_.result.items)))
      .map(_.sortWith(_.StakeExpiringBlock < _.StakeExpiringBlock))
  }

  def height: Option[Long] = onlineNodes.map(_.result.height)

  def onlineNodes: Option[Data] = RClient.get("onlineNodes").map(JsonUtil.fromJson[Data](_))

  def offlineNodes: Option[Data] = RClient.get("offlineNodes").map(JsonUtil.fromJson[Data](_))

  def updateOnlineNodes(nodes: String): Option[Boolean] = Option(RClient.set("onlineNodes", nodes))

  def updateOfflineNodes(nodes: String): Option[Boolean] = {
    onlineNodes.map(online => {
      val offline = JsonUtil.fromJson[Data](nodes).result.items
        .filter(x => !online.result.items.exists(z => x.PublicId == z.PublicId) && x.StakeAmount > 0)
        .map(_.copy(IsOnline = false))

      RClient.set("offlineNodes", JsonUtil.toJson(Data(Result(offline, online.result.height))))
    })
  }

  private def concatNodes(list: List[Option[List[Node]]]): Option[List[Node]] =
    list.foldLeft(Option(List.empty[Node])) { case (acc, el) =>
      el.flatMap(value => acc.map(ac => ac ++ value))
    }
}

case class Nodes(nodes: List[Node], height: Long, totalStake: Long)
case class NodesOnline(nodes: String)
case class NodesOffline(nodes: String)
case class Data(result: Result)
case class Result(items: List[Node], height: Long)
case class Stats(nodes: Long, tiers: List[Tier])
case class Tier(tier: Long, nodes: Long, roi: Double)

case class Node(
  Address: String,
  PublicId: String,
  StakeAmount: Long,
  StakeFirstValidBlock: Long,
  StakeExpiringBlock: Long,
  IsStakeValid: Boolean,
  BlockchainBasedListTier: Long,
  AuthSampleBlockchainBasedListTier: Long,
  IsAvailableForAuthSample: Boolean,
  LastUpdateAge: Long,
  ExpirationTime: String,
  IsOnline: Boolean = false) {

  def asView(currentBlock: Long): Node = {
    val stakeAmount = StakeAmount.toString.dropRight(10).toLong

    val tier = {
      if (stakeAmount >= 250000) 4
      else if (stakeAmount >= 150000) 3
      else if (stakeAmount >= 90000) 2
      else if (stakeAmount >= 50000) 1
      else 0
    }

    val ExpirationTime = (StakeExpiringBlock - currentBlock) * 120

    import java.util.concurrent.TimeUnit
    val day = TimeUnit.SECONDS.toDays(ExpirationTime).toInt
    val hours = TimeUnit.SECONDS.toHours(ExpirationTime) - (day * 24)
    val minute = TimeUnit.SECONDS.toMinutes(ExpirationTime) - (TimeUnit.SECONDS.toHours(ExpirationTime) * 60)

    val time = s"$day days, $hours hours, $minute minutes"

    Node(Address, PublicId, stakeAmount, StakeFirstValidBlock, StakeExpiringBlock, IsStakeValid, tier, AuthSampleBlockchainBasedListTier, IsAvailableForAuthSample, LastUpdateAge, time, IsOnline)
  }
}
