package io.memoria.atom.testsuite.eventsourcing.banking;

import io.memoria.atom.testsuite.eventsourcing.banking.command.Credit;
import io.memoria.atom.testsuite.eventsourcing.banking.event.Debited;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.memoria.atom.testsuite.eventsourcing.banking.TestUtils.*;

class AccountSagaTest {
  @Test
  void evolve() {
    // Given
    var debited = new Debited(aliceEventMeta, bobId, 300);

    // When
    var credit = (Credit) saga.apply(debited).get();

    // Then
    Assertions.assertThat(credit.accountId()).isEqualTo(bobId);
    Assertions.assertThat(credit.debitedAcc()).isEqualTo(aliceId);
  }
}