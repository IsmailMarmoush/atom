package io.memoria.atom.tests.eventsourcing.event;

import io.memoria.atom.eventsourcing.event.EventMeta;

public record DebitConfirmed(EventMeta meta) implements AccountEvent {}