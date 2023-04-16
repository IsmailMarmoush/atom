package io.memoria.atom.eventsourcing.stream;

import io.memoria.atom.core.stream.ESMsg;
import io.memoria.atom.core.stream.ESMsgStream;
import io.memoria.atom.core.text.TextTransformer;
import io.memoria.atom.eventsourcing.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static io.memoria.atom.core.vavr.ReactorVavrUtils.toMono;

class EventStreamImpl<E extends Event> implements EventStream<E> {
  private final ESMsgStream esMsgStream;
  private final TextTransformer transformer;
  private final Class<E> cClass;
  private final String topic;
  private final int partition;

  EventStreamImpl(String topic, int partition, ESMsgStream esMsgStream, TextTransformer transformer, Class<E> cClass) {
    this.topic = topic;
    this.partition = partition;
    this.esMsgStream = esMsgStream;
    this.transformer = transformer;
    this.cClass = cClass;
  }

  public Mono<E> pub(E e) {
    return toMono(transformer.serialize(e)).flatMap(cStr -> pubMsg(topic, partition, e, cStr)).map(id -> e);
  }

  public Flux<E> sub() {
    return esMsgStream.sub(topic, partition).flatMap(msg -> toMono(transformer.deserialize(msg.value(), cClass)));

  }

  private Mono<ESMsg> pubMsg(String topic, int partition, E e, String cStr) {
    return esMsgStream.pub(new ESMsg(topic, partition, e.commandId().value(), cStr));
  }
}
