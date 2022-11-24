package io.memoria.atom.active.eventsourcing.kafka;

import io.memoria.atom.active.eventsourcing.repo.CmdMsg;
import io.vavr.collection.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.Duration;
import java.util.Random;

@TestMethodOrder(OrderAnnotation.class)
class KafkaCommandStreamTest {
  private static final Random r = new Random();
  private static final String topic = "some_topic_" + r.nextInt();
  private static final KafkaCommandStream client = createRepo();
  private static final int partition = 0;
  private static final int msgCount = 100;

  @Test
  @Order(1)
  void push() {
    // Given
    var msgs = createMessages(0, msgCount);
    // When
    var result = msgs.map(client::pub);
    // Then
    result.forEach(tr -> Assertions.assertTrue(tr.isSuccess()));
  }

  @Test
  @Order(2)
  void stream() {
    // Given
    new Thread(() -> createMessages(msgCount, msgCount + 10).forEach(KafkaCommandStreamTest::delayedSend)).start();
    // Then
    client.sub(topic, partition)
          .takeWhile(cmd -> !cmd.key().equals("109"))
          .forEach(msg -> Assertions.assertTrue(msg.value().contains("hello world")));
  }

  private static List<CmdMsg> createMessages(int start, int count) {
    return List.range(start, count).map(i -> CmdMsg.create(topic, partition, i + "", "hello world:" + i));
  }

  private static void delayedSend(CmdMsg msg) {
    try {
      Thread.sleep(100);
      client.pub(msg);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static KafkaCommandStream createRepo() {
    return new KafkaCommandStream(Duration.ofMillis(100), Dataset.producerConfigs(), Dataset.consumerConfigs());
  }
}