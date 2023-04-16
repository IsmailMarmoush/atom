package io.memoria.atom.reactive.eventsourcing.nats;

import io.memoria.atom.core.stream.ESMsg;
import io.nats.client.api.StorageType;
import io.vavr.collection.List;

import java.time.Duration;

public class TestUtils {
  private TestUtils() {}

  static TopicConfig createTopicConfig(String topic, int partition) {
    return TopicConfig.appendOnly(topic, partition, StorageType.File, 1, 100, Duration.ofMillis(500));
  }

  static TopicConfig[] createConfigs(String topic, int nTotalPartitions) {
    return List.range(0, nTotalPartitions)
               .map(partition -> createTopicConfig(topic, partition))
               .toJavaArray(TopicConfig[]::new);
  }

  static ESMsg createEsMsg(String topic, int partition, int i) {
    return new ESMsg(topic, partition, String.valueOf(i), "hello" + i);
  }
}
