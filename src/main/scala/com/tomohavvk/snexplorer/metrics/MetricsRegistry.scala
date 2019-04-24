package com.tomohavvk.snexplorer.metrics

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.hotspot._


object MetricsRegistry {
  val registry: CollectorRegistry = new CollectorRegistry(true)

  Seq(new StandardExports,
    new MemoryPoolsExports,
    new GarbageCollectorExports,
    new ThreadExports,
    new ClassLoadingExports,
    new VersionInfoExports
  ) foreach MetricsRegistry.registry.register
}
