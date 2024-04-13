package io.memoria.atom.eventsourcing.aggregate.store;

import io.memoria.atom.eventsourcing.aggregate.Aggregate;
import io.memoria.atom.eventsourcing.state.StateId;

import javax.cache.Cache;
import javax.cache.Cache.Entry;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.StreamSupport;

class CacheAdapter implements AggregateStore {
  private final Cache<StateId, Aggregate> cache;

  public CacheAdapter(Cache<StateId, Aggregate> cache) {
    this.cache = cache;
  }

  @Override
  public void computeIfAbsent(StateId stateId, Function<StateId, Aggregate> actorFn) {
    cache.putIfAbsent(stateId, actorFn.apply(stateId));
  }

  @Override
  public Aggregate get(StateId actorId) {
    return cache.get(actorId);
  }

  @Override
  public void close() {
    cache.close();
  }

  @Override
  public Iterator<Aggregate> iterator() {
    return StreamSupport.stream(cache.spliterator(), false).map(Entry::getValue).iterator();
  }
}