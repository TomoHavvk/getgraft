package com.tomohavvk.snwatcher.redis

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory

object RClient {
  private val config = ConfigFactory.load()
  private val client = new RedisClient(config.getString("snwatcher.redis.host"), config.getInt("snwatcher.redis.port"))

  def incr(key: String): Option[Long] = client.incr(key)
  def get(key: String): Option[String] = client.get(key)
  def set(key: String, value: String): Boolean = client.set(key, value)
}
