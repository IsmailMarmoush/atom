package io.memoria.atom.reactive.eventsourcing.nats;

import io.memoria.atom.core.stream.ESMsg;
import io.nats.client.*;
import io.nats.client.api.*;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import io.vavr.control.Try;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for directly using nats, this layer is for testing NATS java driver, and ideally it shouldn't have
 * Flux/Mono utilities, only pure java/nats APIs
 */
class NatsUtils {
  public static final String ID_HEADER = "ID_HEADER";

  private NatsUtils() {}

  static Try<StreamInfo> createOrUpdateStream(Connection nc, StreamConfiguration streamConfiguration) {
    return Try.of(() -> {
      var streamNames = nc.jetStreamManagement().getStreamNames();
      if (streamNames.contains(streamConfiguration.getName()))
        return nc.jetStreamManagement().updateStream(streamConfiguration);
      else
        return nc.jetStreamManagement().addStream(streamConfiguration);
    });
  }

  static ErrorListener errorListener() {
    return new ErrorListener() {
      @Override
      public void errorOccurred(Connection conn, String error) {
        ErrorListener.super.errorOccurred(conn, error);
      }
    };
  }

  static CompletableFuture<PublishAck> publishMsg(JetStream js, ESMsg msg) {
    var message = toMessage(msg);
    var opts = PublishOptions.builder().clearExpected().messageId(msg.key()).build();
    return js.publishAsync(message, opts);
  }

  static JetStreamSubscription jetStreamSub(JetStream js, TopicConfig topicConfig)
          throws IOException, JetStreamApiException {
    var config = ConsumerConfiguration.builder()
                                      .ackPolicy(AckPolicy.Explicit)
                                      .deliverPolicy(DeliverPolicy.All)
                                      .replayPolicy(ReplayPolicy.Instant)
                                      .build();
    var subscribeOptions = PullSubscribeOptions.builder()
                                               .stream(topicConfig.streamName())
                                               .configuration(config)
                                               .build();
    return js.subscribe(topicConfig.subjectName(), subscribeOptions);
  }

  static JetStreamSubscription jetStreamSubLatest(JetStream js, TopicConfig topicConfig)
          throws IOException, JetStreamApiException {
    var config = ConsumerConfiguration.builder()
                                      .ackPolicy(AckPolicy.Explicit)
                                      .deliverPolicy(DeliverPolicy.LastPerSubject)
                                      .build();
    var subscribeOptions = PullSubscribeOptions.builder()
                                               .stream(topicConfig.streamName())
                                               .configuration(config)
                                               .build();
    return js.subscribe(topicConfig.subjectName(), subscribeOptions);
  }

  static Message toMessage(ESMsg msg) {
    var subjectName = TopicConfig.toSubjectName(msg);
    var headers = new Headers();
    headers.add(ID_HEADER, msg.key());
    return NatsMessage.builder().subject(subjectName).headers(headers).data(msg.value()).build();
  }

  static ESMsg toMsg(Message message) {
    var value = new String(message.getData(), StandardCharsets.UTF_8);
    var tp = TopicConfig.topicPartition(message.getSubject());
    return new ESMsg(tp._1, tp._2, message.getHeaders().getFirst(ID_HEADER), value);
  }

  static Options toOptions(NatsConfig natsConfig) {
    return new Options.Builder().server(natsConfig.url()).errorListener(errorListener()).build();
  }

  static StreamConfiguration toStreamConfiguration(TopicConfig c) {
    return StreamConfiguration.builder()
                              .storageType(c.storageType)
                              .denyDelete(c.denyDelete)
                              .denyPurge(c.denyPurge)
                              .name(c.streamName())
                              .subjects(c.subjectName())
                              .build();
  }
}