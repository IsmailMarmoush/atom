package io.memoria.atom.active.eventsourcing.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import io.memoria.atom.core.eventsourcing.StateId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static io.memoria.atom.active.eventsourcing.cassandra.TestUtils.KEYSPACE;

@TestMethodOrder(OrderAnnotation.class)
class QueryClientTest {
  private static final String TABLE = QueryClientTest.class.getSimpleName() + "_events";
  private static final String STATE_ID = StateId.randomUUID().value();
  private static final CqlSession session = TestUtils.CqlSession();
  private static final QueryClient client = new QueryClient(session);
  private static final int COUNT = 100;

  @Test
  @Order(1)
  void createKeyspace() {
    // When
    var isCreated = TestUtils.createKeyspace(session, KEYSPACE, 1);
    // Then
    assert isCreated;
  }

  @Test
  @Order(2)
  void createTable() {
    // When
    var isCreated = TestUtils.createEventsTable(session, KEYSPACE, TABLE);
    // Then
    assert isCreated;
  }

  @Test
  @Order(3)
  void push() {
    // When
    var rows = IntStream.range(0, COUNT).mapToObj(i -> client.push(KEYSPACE, TABLE, STATE_ID, i, "someEvent_" + i));
    AtomicInteger idx = new AtomicInteger(0);
    // Then
    rows.forEach(i -> Assertions.assertEquals(i.get(), idx.getAndIncrement()));
  }

  @Test
  @Order(4)
  void get() {
    // When
    var count = client.get(TestUtils.KEYSPACE, TABLE, STATE_ID).count();
    // Then
    assert count == COUNT;
  }

  @BeforeAll
  static void beforeAll() {
    System.out.println("QueryClientTest: StateId:" + STATE_ID);
  }
}
