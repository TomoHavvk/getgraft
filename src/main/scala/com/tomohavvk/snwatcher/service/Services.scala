package com.tomohavvk.snwatcher.service

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props, Scheduler}
import akka.stream.{ActorMaterializer, Materializer}
import com.tomohavvk.snwatcher.actors.SupernodeActor.Nodes
import com.tomohavvk.snwatcher.actors.SupernodeActor
import com.tomohavvk.snwatcher.http.client.SupernodeHttpClient
import com.tomohavvk.snwatcher.http.client.SupernodeHttpClient.{Node, Result}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

case class Services(implicit val system: ActorSystem, materializer: ActorMaterializer) extends LazyLogging {

  private val supernodeActor: ActorRef = system.actorOf(Props[SupernodeActor], name = "supernode-actor")
  private val scheduler: Scheduler = system.scheduler

  private implicit val ec: ExecutionContextExecutor = system.dispatcher

  private def nodes()(implicit system: ActorSystem, materializer: Materializer): Future[Result] = SupernodeHttpClient.result()

  private implicit val timeout: Timeout = Timeout(5 seconds)

  def updateNodes(): Unit = supernodeActor ? Nodes

  def nodes: Future[Nodes] = (supernodeActor ? Nodes).map(_.asInstanceOf[Nodes])

  private val task: Runnable = () => {
    val online = Await.result(SupernodeHttpClient.result(), 10.seconds)
    val onlineWithOffline = Await.result(SupernodeHttpClient.result(withOffline = true), 10.seconds)
    supernodeActor ! Nodes(online.items, onlineWithOffline.items, null, online.height)
    logger.info("Services after send `update info`")
  }

  scheduler.schedule(
    initialDelay = Duration(2, TimeUnit.SECONDS),
    interval = Duration(59, TimeUnit.SECONDS),
    runnable = task)
}
