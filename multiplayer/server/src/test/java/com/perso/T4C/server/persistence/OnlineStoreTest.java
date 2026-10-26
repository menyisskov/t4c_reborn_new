package com.perso.T4C.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Runs against PostgreSQL, including in CI; no in-memory substitute for transaction semantics. */
@EnabledIfEnvironmentVariable(named="T4C_DB_URL", matches=".+")
class OnlineStoreTest {
  private OnlineStore store;
  @BeforeEach void setup() throws Exception { store = OnlineStore.fromEnvironment(); store.migrate(); }

  @Test void reloadsAcrossStoreRestartAndRejectsWrongOwnerOrStaleCheckpoint() throws Exception {
    UUID account = UUID.randomUUID();
    var created = store.create(account, "Checkpoint test", 0, 10, 20);
    var saved = store.checkpoint(account, created.id(), created.revision(), 1, 30, 40);
    var restarted = OnlineStore.fromEnvironment();
    restarted.migrate();
    assertEquals(saved, restarted.load(account, created.id()));
    assertThrows(OnlineStore.Conflict.class, () -> restarted.load(UUID.randomUUID(), created.id()));
    assertThrows(OnlineStore.Conflict.class, () -> restarted.checkpoint(account, created.id(), created.revision(), 0, 1, 2));
    assertEquals(saved, restarted.load(account, created.id()));
  }

  @Test void concurrentRetriesGrantOneRewardAndRejectChangedPayload() throws Exception {
    UUID account = UUID.randomUUID(), event = UUID.randomUUID();
    var created = store.create(account, "Reward test", 0, 10, 20);
    try (var executor = Executors.newFixedThreadPool(8)) {
      var actions = new ArrayList<Callable<OnlineStore.RewardResult>>();
      for (int i=0; i<8; i++) actions.add(() -> store.reward(account, created.id(), event, 7, 11));
      int applied=0;
      for (var result : executor.invokeAll(actions)) if (result.get().applied()) applied++;
      assertEquals(1, applied);
    }
    var state=store.load(account, created.id());
    assertEquals(7, state.gold()); assertEquals(11, state.experience());
    assertThrows(OnlineStore.Conflict.class, () -> store.reward(account, created.id(), event, 8, 11));
    assertEquals(state, store.load(account, created.id()));
  }

  @Test void failedBalanceUpdateRollsBackRewardReceiptSoRetryCanSucceed() throws Exception {
    UUID account=UUID.randomUUID(), failedEvent=UUID.randomUUID();
    var created=store.create(account, "Rollback test", 0, 10, 20);
    store.reward(account, created.id(), UUID.randomUUID(), Long.MAX_VALUE, 0);
    assertThrows(SQLException.class, () -> store.reward(account, created.id(), failedEvent, 1, 0));
    // If the failed transaction left its receipt behind, this retry would conflict or be ignored.
    assertTrue(store.reward(account, created.id(), failedEvent, 0, 3).applied());
    var state=store.load(account, created.id());
    assertEquals(Long.MAX_VALUE, state.gold()); assertEquals(3, state.experience());
  }
}
