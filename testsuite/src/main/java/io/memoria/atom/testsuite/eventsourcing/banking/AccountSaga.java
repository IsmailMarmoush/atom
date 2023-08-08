package io.memoria.atom.testsuite.eventsourcing.banking;

import io.memoria.atom.core.id.Id;
import io.memoria.atom.eventsourcing.CommandId;
import io.memoria.atom.eventsourcing.CommandMeta;
import io.memoria.atom.eventsourcing.StateId;
import io.memoria.atom.eventsourcing.rule.Saga;
import io.memoria.atom.testsuite.eventsourcing.banking.command.AccountCommand;
import io.memoria.atom.testsuite.eventsourcing.banking.command.ConfirmDebit;
import io.memoria.atom.testsuite.eventsourcing.banking.command.Credit;
import io.memoria.atom.testsuite.eventsourcing.banking.event.AccountEvent;
import io.memoria.atom.testsuite.eventsourcing.banking.event.CreditRejected;
import io.memoria.atom.testsuite.eventsourcing.banking.event.Credited;
import io.memoria.atom.testsuite.eventsourcing.banking.event.Debited;
import io.vavr.control.Option;

import java.util.function.Supplier;

public record AccountSaga(Supplier<Id> idSupplier, Supplier<Long> timeSupplier)
        implements Saga<AccountEvent, AccountCommand> {

  CommandMeta meta(StateId stateId) {
    return new CommandMeta(CommandId.of(idSupplier.get()), stateId, timeSupplier.get());
  }

  @Override
  public Option<AccountCommand> apply(AccountEvent event) {
    return switch (event) {
      case Debited e -> Option.some(new Credit(meta(e.creditedAcc()), e.accountId(), e.amount()));
      case Credited e -> Option.some(new ConfirmDebit(meta(e.debitedAcc()), e.accountId()));
      case CreditRejected e -> Option.some(new Credit(meta(e.debitedAcc()), e.accountId(), e.amount()));
      default -> Option.none();
    };
  }
}