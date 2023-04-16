package io.memoria.atom.reactive.eventsourcing.nats;

import io.memoria.atom.core.stream.ESMsg;
import io.nats.client.*;
import io.nats.client.api.StreamInfo;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static io.memoria.atom.reactive.eventsourcing.nats.NatsUtils.createOrUpdateStream;
import static io.memoria.atom.reactive.eventsourcing.nats.NatsUtils.jetStreamSub;

class DefaultNatsESMsgStream implements NatsESMsgStream {
  private static final Logger log = LoggerFactory.getLogger(DefaultNatsESMsgStream.class.getName());
  private final NatsConfig natsConfig;
  private final Connection nc;
  private final JetStream js;

  DefaultNatsESMsgStream(NatsConfig natsConfig) throws IOException, InterruptedException {
    this.natsConfig = natsConfig;
    this.nc = Nats.connect(NatsUtils.toOptions(natsConfig));
    this.js = nc.jetStream();
    natsConfig.topics()
              .map(NatsUtils::toStreamConfiguration)
              .map(c -> createOrUpdateStream(nc, c))
              .map(Try::get)
              .map(StreamInfo::toString)
              .forEach(log::info);
  }

  @Override
  public Mono<ESMsg> pub(ESMsg msg) {
    return Mono.fromCallable(() -> NatsUtils.publishMsg(js, msg)).flatMap(Mono::fromFuture).thenReturn(msg);
  }

  @Override
  public Flux<ESMsg> sub(String topic, int partition) {
    var topicConfig = this.natsConfig.find(topic, partition).get();
    return Mono.fromCallable(() -> jetStreamSub(js, topicConfig))
               .flatMapMany(sub -> this.fetchBatch(sub, topicConfig).repeat())
               .map(NatsUtils::toMsg);
  }

  @Override
  public void close() {
    try {
      this.nc.close();
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
  }

  private Flux<Message> fetchBatch(JetStreamSubscription sub, TopicConfig config) {
    return Mono.fromCallable(() -> {
      nc.flushBuffer();
      var msgs = sub.fetch(config.fetchBatchSize, config.fetchMaxWait);
      System.out.println(msgs.size());
      return msgs;
    }).flatMapMany(Flux::fromIterable).doOnNext(Message::ack);
  }
}
