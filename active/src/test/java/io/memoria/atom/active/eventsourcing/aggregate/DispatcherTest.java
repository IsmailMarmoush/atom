package io.memoria.atom.active.eventsourcing.aggregate;

import io.memoria.atom.active.eventsourcing.banking.AccountDecider;
import io.memoria.atom.active.eventsourcing.banking.AccountEvolver;
import io.memoria.atom.active.eventsourcing.banking.AccountSaga;
import io.memoria.atom.active.eventsourcing.banking.command.CreateAccount;
import io.memoria.atom.active.eventsourcing.banking.command.UserCommand;
import io.memoria.atom.active.eventsourcing.banking.event.UserEvent;
import io.memoria.atom.active.eventsourcing.banking.state.User;
import io.memoria.atom.active.eventsourcing.infra.repo.ESRepo;
import io.memoria.atom.active.eventsourcing.infra.repo.EventRepo;
import io.memoria.atom.active.eventsourcing.infra.stream.CommandStream;
import io.memoria.atom.active.eventsourcing.infra.stream.ESStream;
import io.memoria.atom.core.eventsourcing.Domain;
import io.memoria.atom.core.eventsourcing.Route;
import io.memoria.atom.core.eventsourcing.StateId;
import io.memoria.atom.core.text.SerializableTransformer;
import io.memoria.atom.core.text.TextTransformer;
import io.vavr.control.Try;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;

class DispatcherTest {
  private static final Logger log = LoggerFactory.getLogger(DispatcherTest.class.getSimpleName());

  private static final TextTransformer transformer = new SerializableTransformer();
  private static final Route route = new Route("commands_topic", 0, 1, "events_topic");
  private static final Domain<User, UserCommand, UserEvent> domain = createdomain();
  private final static ESStream esStream = ESStream.inMemory(route.cmdTopic(), route.totalCmdPartitions());
  private final static ESRepo esRepo = ESRepo.inMemory(route.eventTable());
  private final static Duration totalAwait = Duration.ofSeconds(5);

  private final CommandStream<UserCommand> cmdStream;
  private final EventRepo<UserEvent> eventRepo;

  DispatcherTest() {
    cmdStream = CommandStream.create(route, esStream, transformer, domain.cClass());
    eventRepo = EventRepo.create(route, esRepo, transformer, domain.eClass());
  }

  @Test
  void twoAccounts() {
    // Given
    var bobId = StateId.of("bob");
    var janId = StateId.of("jan");

    // When
    var commandsCount = new AtomicInteger(0);
    twoAccountsCommands(bobId, janId).map(cmdStream::pub).peek(c -> commandsCount.incrementAndGet()).forEach(Try::get);

    try (var dispatcher = createDispatcher()) {
      assert dispatcher.run().limit(commandsCount.get()).count() == commandsCount.get();
      await().atMost(totalAwait)
             .until(() -> eventRepo.getAll(bobId).count() == 4 && eventRepo.getAll(janId).count() == 1);
    }

    // Then
    var bobEvents = eventRepo.getAll(bobId).toList();
    assertEvents(bobId, bobEvents);
    // and
    var janEvents = eventRepo.getAll(janId).toList();
    assertEvents(janId, janEvents);
  }

  @Test
  void multipleAccounts() {
    // Given
    int nAccounts = 4;
    int balance = 100;
    var createAccounts = DataSet.createAccounts(nAccounts, balance);
    var createRandomTransactions = DataSet.createRandomTransactions(nAccounts, balance);
    var commands = createAccounts.appendAll(createRandomTransactions);

    int treasury = nAccounts * balance;
    var stateIds = createAccounts.map(UserCommand::stateId);

    // When
    var commandsCount = new AtomicInteger(0);
    commands.map(cmdStream::pub).peek(t -> commandsCount.incrementAndGet()).forEach(Try::get);

    try (var dispatcher = createDispatcher()) {
      assert dispatcher.run().limit(commandsCount.get()).count() == commandsCount.get();

    }
    //    var pipelines = Flux.merge(streamRepo.push(cmds), statePipeline.run(), blockingSagaPipeline.run());
    //    StepVerifier.create(pipelines).expectNextCount(20).verifyTimeout(timeout);
    //    // Then
    //    var accounts = stateIds.map(statePipeline::stateOrInit).map(u -> (Acc) u);
    //    Assertions.assertEquals(nAccounts, accounts.size());
    //    var total = accounts.foldLeft(0, (a, b) -> a + b.balance());
    //    Assertions.assertEquals(treasury, total);
  }

  public Stream<UserCommand> twoAccountsCommands(StateId bobId, StateId janId) {
    var createBob = CreateAccount.of(bobId, "bob", 100);
    var createJan = CreateAccount.of(janId, "jan", 100);
    var sendMoneyFromBobToJan = DataSet.createTransfer(bobId, janId, 50);
    var sendSecondMoney = DataSet.createTransfer(bobId, janId, 25);
    var sendThirdMoney = DataSet.createTransfer(bobId, janId, 25);
    return Stream.of(createBob, createJan, sendMoneyFromBobToJan, sendSecondMoney, sendThirdMoney);
  }

  private static Domain<User, UserCommand, UserEvent> createdomain() {
    return new Domain<>(User.class,
                        UserCommand.class,
                        UserEvent.class,
                        new AccountDecider(),
                        new AccountSaga(),
                        new AccountEvolver());
  }

  private static Dispatcher<User, UserCommand, UserEvent> createDispatcher() {
    return new Dispatcher<>(domain, route, esStream, esRepo, transformer, tr -> log.info(tr.toString()));
  }

  private static void assertEvents(StateId janId, List<Try<UserEvent>> janEvents) {
    janEvents.forEach(e -> Assertions.assertThat(e.isSuccess()).isTrue());
    janEvents.forEach(e -> Assertions.assertThat(e.get().stateId()).isEqualTo(janId));
  }
}
