package com.tomohavvk.snwatcher.http.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import akka.util.ByteString
import com.tomohavvk.snwatcher.util.JsonUtil
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContextExecutor, Future}

object SupernodeHttpClient extends LazyLogging {

  def result(withOffline: Boolean = false)(implicit system: ActorSystem, materializer: Materializer): Future[Result] = {
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val url = {
      if (withOffline) system.settings.config.getString("grafttools.http.client.supernode-all-url")
      else system.settings.config.getString("grafttools.http.client.supernode-url")
    }

    Http().singleRequest(HttpRequest(uri = url))
      .flatMap(_.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(body => {

        val result = JsonUtil.fromJson[Data](body.utf8String.toString)
        logger.info(result.toString)
        result
      })).map(_.result)
  }

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

      val res = s"$day days, $hours hours, $minute minutes"

      Node(Address, PublicId, stakeAmount, StakeFirstValidBlock,StakeExpiringBlock, IsStakeValid, tier, AuthSampleBlockchainBasedListTier, IsAvailableForAuthSample, LastUpdateAge, res,  isOnline)
    }
  }

  case class Info(nodesOnline: Long, totalStake: Long, t1: Long, t2: Long, t3: Long, t4: Long)
  case class Result(items: List[Node], height: Long)
  case class Data(result: Result)
}
