package io.memoria.atom.core.stream;

import io.vavr.collection.Stream;
import io.vavr.control.Try;

public interface BlockingChain<T> {
  Try<T> append(T t);

  Try<Stream<T>> fetch();

  static <T> BlockingChain<T> inMemory() {
    return new MemBlockingChain<>();
  }
}