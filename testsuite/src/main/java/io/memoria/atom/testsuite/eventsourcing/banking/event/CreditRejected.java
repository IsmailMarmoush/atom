package io.memoria.atom.testsuite.eventsourcing.banking.event;

import io.memoria.atom.eventsourcing.EventMeta;
import io.memoria.atom.eventsourcing.StateId;

public record CreditRejected(EventMeta meta, StateId debitedAcc, long amount) implements AccountEvent {}
