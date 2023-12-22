package io.memoria.atom.testsuite.eventsourcing.event;

import io.memoria.atom.eventsourcing.EventMeta;

public record NameChanged(EventMeta meta, String newName) implements AccountEvent {}