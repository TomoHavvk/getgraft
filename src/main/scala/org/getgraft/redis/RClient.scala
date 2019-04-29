package org.getgraft.redis

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

object RClient extends LazyLogging {
  private val config = ConfigFactory.load()

  val client: ThreadLocal[RedisClient] = new ThreadLocal[RedisClient]() {
    override def initialValue(): RedisClient = {
      logger.debug("Init Redis instance")
      new RedisClient(config.getString("getgraft.redis.host"), config.getInt("getgraft.redis.port"))
    }
  }

  def incr(key: String): Option[Long] = client.get().incr(key)
  def get(key: String): Option[String] = client.get().get(key)
  def set(key: String, value: String): Boolean = client.get().set(key, value)
}
