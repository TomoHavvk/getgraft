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

  def get(withOffline: Boolean = false)(implicit system: ActorSystem, materializer: Materializer): Future[String] = {
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val url = {
      if (withOffline) system.settings.config.getString("snwatcher.http.client.supernodes-online-and-offline-url")
      else system.settings.config.getString("snwatcher.http.client.supernodes-online-url")
    }

    Http().singleRequest(HttpRequest(uri = url))
      .flatMap(_.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String.toString))
  }
}
