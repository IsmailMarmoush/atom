package io.memoria.atom.testsuite.eventsourcing.event;

import io.memoria.atom.eventsourcing.event.EventMeta;

public record ClosureRejected(EventMeta meta) implements AccountEvent {}
