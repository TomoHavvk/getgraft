package com.tomohavvk.snwatcher.actors

import com.tomohavvk.snwatcher.redis.RClient
import com.tomohavvk.snwatcher.util.JsonUtil
import com.typesafe.scalalogging.LazyLogging


case object Supernode extends LazyLogging {

  def nodes: Option[List[Node]] = {
    // TODO set online when save
    concatNodes(List(onlineNodes.map(_.result.items).map(x => x.map(_.copy(isOnline = true))), offlineNodes.map(_.result.items)))
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
        .map(_.copy(isOnline = false))

      RClient.set("offlineNodes", JsonUtil.toJson(Data(Result(offline, online.result.height))))
    })
  }

  private def concatNodes(list: List[Option[List[Node]]]): Option[List[Node]] =
    list.foldLeft(Option(List.empty[Node])) { case (acc, el) =>
      el.flatMap(value => acc.map(ac => ac ++ value))
    }
}

case class Nodes(nodes: List[Node], info: Info, height: Long)
case class NodesOnline(nodes: String)
case class NodesOffline(nodes: String)
case class Data(result: Result)
case class Result(items: List[Node], height: Long)
case class Info(nodesOnline: Long, nodesOffline: Long, totalStake: Long, t1: Long, t2: Long, t3: Long, t4: Long)

case class Node(
  Address: String,
  PublicId: String,
  StakeAmount: Long,
  StakeFirstValidBlock: Long,
  StakeExpiringBlock: Long,
  IsStakeValid: Boolean,
  BlockchainBasedListTier: Long,
  AuthSampleBlockchainBasedListTier: Long,
  IsAvailableForAuthSample: String,
  LastUpdateAge: Long,
  ExpirationTime: String,
  isOnline: Boolean = false) {

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

    Node(Address, PublicId, stakeAmount, StakeFirstValidBlock, StakeExpiringBlock, IsStakeValid, tier, AuthSampleBlockchainBasedListTier, IsAvailableForAuthSample, LastUpdateAge, time, isOnline)
  }
}
