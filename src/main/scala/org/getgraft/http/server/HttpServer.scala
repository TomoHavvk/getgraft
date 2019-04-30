package org.getgraft.http.server

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}
import java.time.Instant

import akka.Done
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import scala.concurrent.Future
import scala.concurrent.duration._

trait HasRoute {
  def route: Route
}

class HttpServer(routes: Seq[HasRoute])(
  implicit private val system: ActorSystem,
  implicit private val materializer: Materializer
) extends LazyLogging {

  import HttpServer._

  private val logging = Logging(system, this.getClass)

  val startedAt: Instant = Instant.now

  val route: Route = routes map (_.route) reduce { _ ~ _ }

  val serverBinding: Future[Http.ServerBinding] = {
    logger.info(s"Starting AkkaHttp server on port $port")

    Http().bindAndHandle(route, interface, port = 80, connectionContext = https)
    Http().bindAndHandle(route, interface = interface, port = port)
  }

  def shutdown(hardDeadline: FiniteDuration): Future[Done] = {
    import system.dispatcher

    serverBinding flatMap (_ terminate hardDeadline) map (_ => Done)
  }


  def https: HttpsConnectionContext = {
    val password: Array[Char] = sslPassword.toCharArray

    val ks: KeyStore = KeyStore.getInstance("PKCS12")
    val keystore: InputStream = getClass.getClassLoader.getResourceAsStream("getgraft.p12")

    require(keystore != null, "Keystore required!")
    ks.load(keystore, password)

    val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)

    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    tmf.init(ks)

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
    ConnectionContext.https(sslContext)
  }
}

object HttpServer {
  private val config = ConfigFactory.load() getConfig "getgraft.http.server"
  private val interface = config.getString("interface")
  private val port = config.getInt("port")
  private val sslPassword = config.getString("ssl-password")

  def apply(routes: HasRoute*)(implicit system: ActorSystem, materializer: Materializer): HttpServer = {
    new HttpServer(Seq(routes: _*))
  }
}