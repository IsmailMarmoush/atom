package io.memoria.atom.eventsourcing.usecase.banking.event;

import io.memoria.atom.eventsourcing.CommandId;
import io.memoria.atom.eventsourcing.EventId;
import io.memoria.atom.eventsourcing.StateId;
import io.memoria.atom.eventsourcing.usecase.banking.command.ChangeName;
import io.memoria.atom.eventsourcing.usecase.banking.state.Account;

public record NameChanged(EventId eventId, int seqId, CommandId commandId, StateId accountId, String newName)
        implements AccountEvent {
  @Override
  public StateId stateId() {
    return accountId;
  }

  public static NameChanged from(Account account, ChangeName cmd) {
    return new NameChanged(EventId.randomUUID(), account.seqId() + 1, cmd.commandId(), cmd.stateId(), cmd.name());
  }
}