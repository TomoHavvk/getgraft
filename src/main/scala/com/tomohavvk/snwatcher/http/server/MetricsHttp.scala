package com.tomohavvk.snwatcher.http.server

import java.io.{ByteArrayOutputStream, OutputStreamWriter}

import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.{ContentType, HttpCharsets, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import com.tomohavvk.snwatcher.metrics.MetricsRegistry
import com.typesafe.scalalogging.LazyLogging
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.exporter.common.TextFormat.CONTENT_TYPE_004

class MetricsHttp() extends HasRoute with Directives with LazyLogging {

  import MetricsHttp._

  override def route: Route = get {
    path("metrics") {
      encodeResponseWith(Gzip) {
        complete {
          HttpEntity(MetricsContentType, metricsFormatted())
        }
      }
    }
  }

  private def metricsFormatted(): Array[Byte] = {
    val byteStream = new ByteArrayOutputStream(MetricsResponseBufferSize)
    val writer = new OutputStreamWriter(byteStream, MetricsResponseCharset.value)
    TextFormat.write004(writer, MetricsRegistry.registry.metricFamilySamples)
    writer.close()
    byteStream.toByteArray
  }
}

object MetricsHttp {
  final case class InvalidContentTypeException(contentType: String, description: String)
    extends RuntimeException(s"Invalid content type in Prometheus client: '$contentType'. $description")

  val MetricsContentType =
    ContentType.parse(CONTENT_TYPE_004).fold(
      err => throw InvalidContentTypeException(CONTENT_TYPE_004, err.map(_.formatPretty).mkString),
      identity
    )

  private val MetricsResponseCharset = MetricsContentType.charsetOption.getOrElse(HttpCharsets.`UTF-8`)
  private val MetricsResponseBufferSize = 16 * 1024
}