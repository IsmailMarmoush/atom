package io.memoria.atom.reactive.eventsourcing.nats;

import io.memoria.atom.core.stream.ESMsg;
import io.nats.client.api.StorageType;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.time.Duration;
import java.util.Objects;

/**
 *
 */
public final class TopicConfig {

  public static final String SPLIT_TOKEN = "_";
  public static final String SUBJECT_EXT = ".subject";
  public final String topic;
  public final int partition;
  public final StorageType storageType;
  public final int replicationFactor;
  public final int fetchBatchSize;
  public final Duration fetchMaxWait;
  public final boolean denyDelete;
  public final boolean denyPurge;

  public TopicConfig(String topic,
                     int partition,
                     StorageType storageType,
                     int replicationFactor,
                     int fetchBatchSize,
                     Duration fetchMaxWait,
                     boolean denyDelete,
                     boolean denyPurge) {
    validateName(topic, partition);
    this.topic = topic;
    this.partition = partition;
    this.storageType = storageType;
    this.replicationFactor = replicationFactor;
    this.fetchBatchSize = fetchBatchSize;
    this.fetchMaxWait = fetchMaxWait;
    this.denyDelete = denyDelete;
    this.denyPurge = denyPurge;
  }

  public String streamName() {
    return streamName(topic, partition);
  }

  public String subjectName() {
    return subjectName(topic, partition);
  }

  public static String streamName(String topic, int partition) {
    return "%s%s%d".formatted(topic, SPLIT_TOKEN, partition);
  }

  public static String subjectName(String topic, int partition) {
    return streamName(topic, partition) + SUBJECT_EXT;
  }

  public static String toSubjectName(ESMsg msg) {
    return subjectName(msg.topic(), msg.partition());
  }

  public static Tuple2<String, Integer> topicPartition(String subject) {
    var idx = subject.indexOf(SUBJECT_EXT);
    var s = subject.substring(0, idx).split(SPLIT_TOKEN);
    var topic = s[0];
    var partition = Integer.parseInt(s[1]);
    return Tuple.of(topic, partition);
  }

  public static TopicConfig appendOnly(String topic,
                                       int partition,
                                       StorageType storageType,
                                       int replicationFactor,
                                       int fetchBatch,
                                       Duration fetchMaxWait) {
    return new TopicConfig(topic, partition, storageType, replicationFactor, fetchBatch, fetchMaxWait, true, true);
  }

  private static void validateName(String name, int partition) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Name can't be null or empty string");
    }
    if (partition < 0) {
      throw new IllegalArgumentException("Partition can't be less than 0");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (obj == null || obj.getClass() != this.getClass())
      return false;
    var that = (TopicConfig) obj;
    return Objects.equals(this.topic, that.topic) && this.partition == that.partition;
  }

  @Override
  public int hashCode() {
    return Objects.hash(topic, partition);
  }

  @Override
  public String toString() {
    return "TopicConfig["
           + "topic="
           + topic
           + ", "
           + "partition="
           + partition
           + ", "
           + "storageType="
           + storageType
           + ", "
           + "replicationFactor="
           + replicationFactor
           + ", "
           + "fetchBatchSize="
           + fetchBatchSize
           + ", "
           + "fetchMaxWait="
           + fetchMaxWait
           + ", "
           + "denyDelete="
           + denyDelete
           + ", "
           + "denyPurge="
           + denyPurge
           + ']';
  }
}