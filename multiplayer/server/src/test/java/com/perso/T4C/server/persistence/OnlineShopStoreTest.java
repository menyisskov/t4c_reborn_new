package com.perso.T4C.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.server.ItemCatalog;
import com.perso.T4C.server.OnlineShopTrades.Side;
import com.perso.T4C.server.OnlineShopTrades.Trade;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Vendor trades against real PostgreSQL: one atomic gold-and-inventory write per receipt. */
@EnabledIfEnvironmentVariable(named = "T4C_DB_URL", matches = ".+")
class OnlineShopStoreTest {
  private static final String POTION = "item.healing_potion"; // price 42, sells for 21, weight 2, one charge

  private record Fixture(OnlineStore store, OnlineStore.AccountKey key, OnlineStore.Session session,
      OnlineStore.CharacterState character) {}

  private static ItemContent items() throws Exception { return ItemCatalog.loadBundled(); }

  private static OnlineStore.CarryRules rules(ItemContent content, long maximum) {
    return new OnlineStore.CarryRules() {
      public long maximumWeight(CharacterProgress progress) { return maximum; }
      public long unitWeight(String key) { return content.items().get(key).weight(); }
    };
  }

  private static CharacterProgress progress(List<CharacterProgress.Item> inventory) {
    return new CharacterProgress(1, "warrior", "male", new CharacterProgress.Attributes(95, 85, 75, 75, 75),
        new CharacterProgress.Vitals(41, 60, 17, 25), new CharacterProgress.Advancement(2, 3, 5, 0, 100),
        inventory, Map.of(), Map.of(), Set.of(), Map.of(), List.of(), 0,
        new CharacterProgress.Respawn(0, 20, 30), List.of(), Map.of(), 0, null);
  }

  private static Fixture fixture(long gold, List<CharacterProgress.Item> inventory) throws SQLException {
    var store = OnlineStore.fromEnvironment();
    store.migrate();
    var key = store.provisionAccount();
    var created = store.create(key.accountId(), "Shopper", 0, 10, 20, progress(inventory), gold);
    var session = store.acquire(key.secret(), created.id());
    return new Fixture(store, key, session, store.load(key.accountId(), created.id()));
  }

  private static OnlineStore.CharacterState reload(Fixture f) throws SQLException {
    return f.store().load(f.key().accountId(), f.character().id());
  }

  private static String refusal(org.junit.jupiter.api.function.Executable action) {
    return assertThrows(OnlineStore.Conflict.class, action).getMessage();
  }

  @Test void buyDebitsGoldAndAddsTheStackInOneRevision() throws Exception {
    var f = fixture(500, List.of());
    var content = items();
    var result = f.store().commitShopTrade(f.session(), f.character().revision(), UUID.randomUUID(),
        new Trade(Side.BUY, POTION, 3, 42), content, rules(content, 1000));
    assertTrue(result.applied());
    var saved = reload(f);
    assertEquals(500 - 126, saved.gold());
    assertEquals(f.character().revision() + 1, saved.revision());
    assertEquals(1, saved.progress().inventory().size());
    assertEquals(POTION, saved.progress().inventory().getFirst().key());
    assertEquals(3, saved.progress().inventory().getFirst().quantity());
    assertEquals(1, saved.progress().inventory().getFirst().charges());
  }

  @Test void aSecondBuyMergesIntoTheExistingStack() throws Exception {
    var f = fixture(500, List.of());
    var content = items();
    var rules = rules(content, 1000);
    var first = f.store().commitShopTrade(f.session(), f.character().revision(), UUID.randomUUID(),
        new Trade(Side.BUY, POTION, 1, 42), content, rules);
    f.store().commitShopTrade(f.session(), first.character().revision(), UUID.randomUUID(),
        new Trade(Side.BUY, POTION, 2, 42), content, rules);
    var saved = reload(f);
    assertEquals(1, saved.progress().inventory().size(), "stacks merge instead of piling up instances");
    assertEquals(3, saved.progress().inventory().getFirst().quantity());
  }

  @Test void sellCreditsGoldAndRemovesUnitsInOneRevision() throws Exception {
    var stack = new CharacterProgress.Item(UUID.randomUUID(), POTION, 5, 1);
    var f = fixture(10, List.of(stack));
    var content = items();
    var result = f.store().commitShopTrade(f.session(), f.character().revision(), UUID.randomUUID(),
        new Trade(Side.SELL, POTION, 2, 21), content, rules(content, 1000));
    assertTrue(result.applied());
    var saved = reload(f);
    assertEquals(10 + 42, saved.gold());
    assertEquals(3, saved.progress().inventory().getFirst().quantity());
    assertEquals(stack.id(), saved.progress().inventory().getFirst().id(), "the same stack, fewer units");
  }

  @Test void aReplayOfTheSameReceiptChangesNothingAndReturnsTheCommittedState() throws Exception {
    var f = fixture(500, List.of());
    var content = items();
    var rules = rules(content, 1000);
    UUID event = UUID.randomUUID();
    var trade = new Trade(Side.BUY, POTION, 2, 42);
    var first = f.store().commitShopTrade(f.session(), f.character().revision(), event, trade, content, rules);
    var replay = f.store().commitShopTrade(f.session(), f.character().revision(), event, trade, content, rules);
    assertTrue(first.applied());
    assertFalse(replay.applied(), "a lost response that is retried is a replay, not a second purchase");
    assertEquals(first.character().revision(), replay.character().revision());
    assertEquals(first.character().gold(), replay.character().gold());
    assertEquals(500 - 84, reload(f).gold());
  }

  @Test void aReceiptCannotBeReusedForADifferentTrade() throws Exception {
    var f = fixture(500, List.of());
    var content = items();
    var rules = rules(content, 1000);
    UUID event = UUID.randomUUID();
    var first = f.store().commitShopTrade(f.session(), f.character().revision(), event,
        new Trade(Side.BUY, POTION, 1, 42), content, rules);
    assertEquals("Shop receipt payload mismatch", refusal(() -> f.store().commitShopTrade(f.session(),
        first.character().revision(), event, new Trade(Side.BUY, POTION, 9, 42), content, rules)));
    assertEquals(500 - 42, reload(f).gold());
  }

  @Test void insufficientGoldIsRefusedAndNothingIsWritten() throws Exception {
    var f = fixture(83, List.of());
    var content = items();
    assertEquals("insufficient_gold", refusal(() -> f.store().commitShopTrade(f.session(), f.character().revision(),
        UUID.randomUUID(), new Trade(Side.BUY, POTION, 2, 42), content, rules(content, 1000))));
    assertEquals(f.character(), reload(f), "a refusal leaves gold, inventory and revision exactly as they were");
  }

  @Test void exactlyEnoughGoldBuysAndLeavesZero() throws Exception {
    var f = fixture(84, List.of());
    var content = items();
    f.store().commitShopTrade(f.session(), f.character().revision(), UUID.randomUUID(),
        new Trade(Side.BUY, POTION, 2, 42), content, rules(content, 1000));
    assertEquals(0, reload(f).gold());
  }

  @Test void overweightIsRefusedAndTheLimitIsInclusive() throws Exception {
    var f = fixture(500, List.of());
    var content = items();
    assertEquals("too_heavy", refusal(() -> f.store().commitShopTrade(f.session(), f.character().revision(),
        UUID.randomUUID(), new Trade(Side.BUY, POTION, 3, 42), content, rules(content, 5))));
    assertEquals(f.character(), reload(f));
    assertTrue(f.store().commitShopTrade(f.session(), f.character().revision(), UUID.randomUUID(),
        new Trade(Side.BUY, POTION, 3, 42), content, rules(content, 6)).applied(), "6 of 6 is allowed");
  }

  @Test void aStaleRevisionIsRefusedBeforeAnyWrite() throws Exception {
    var f = fixture(500, List.of());
    var content = items();
    var rules = rules(content, 1000);
    f.store().commitShopTrade(f.session(), f.character().revision(), UUID.randomUUID(),
        new Trade(Side.BUY, POTION, 1, 42), content, rules);
    assertEquals("revision", refusal(() -> f.store().commitShopTrade(f.session(), f.character().revision(),
        UUID.randomUUID(), new Trade(Side.BUY, POTION, 1, 42), content, rules)));
    assertEquals(500 - 42, reload(f).gold());
  }

  @Test void sellingMoreThanYouCarryIsRefused() throws Exception {
    var f = fixture(0, List.of(new CharacterProgress.Item(UUID.randomUUID(), POTION, 1, 1)));
    var content = items();
    assertEquals("item_unavailable", refusal(() -> f.store().commitShopTrade(f.session(), f.character().revision(),
        UUID.randomUUID(), new Trade(Side.SELL, POTION, 2, 21), content, rules(content, 1000))));
    assertEquals(f.character(), reload(f));
  }

  @Test void aRefusedReceiptIsNotConsumedSoItsIdCanStillBeUsed() throws Exception {
    var f = fixture(40, List.of());
    var content = items();
    var rules = rules(content, 1000);
    UUID event = UUID.randomUUID();
    assertEquals("insufficient_gold", refusal(() -> f.store().commitShopTrade(f.session(), f.character().revision(),
        event, new Trade(Side.BUY, POTION, 1, 42), content, rules)));
    assertTrue(f.store().commitShopTrade(f.session(), f.character().revision(), event,
        new Trade(Side.BUY, "item.torch", 1, 5), content, rules).applied(),
        "the refusal rolled back, so no receipt was stored");
  }

  @Test void twoConcurrentBuysOfTheLastAffordableUnitCommitExactlyOnce() throws Exception {
    var f = fixture(42, List.of());
    var content = items();
    var rules = rules(content, 1000);
    var start = new CountDownLatch(1);
    var pool = Executors.newFixedThreadPool(2);
    try {
      var results = new ArrayList<java.util.concurrent.Future<Boolean>>();
      for (int i = 0; i < 2; i++)
        results.add(pool.submit(() -> {
          start.await();
          try {
            return f.store().commitShopTrade(f.session(), f.character().revision(), UUID.randomUUID(),
                new Trade(Side.BUY, POTION, 1, 42), content, rules).applied();
          } catch (OnlineStore.Conflict refused) { return false; }
        }));
      start.countDown();
      int applied = 0;
      for (var result : results) if (result.get()) applied++;
      assertEquals(1, applied);
    } finally { pool.shutdownNow(); }
    var saved = reload(f);
    assertEquals(0, saved.gold());
    assertEquals(1, saved.progress().inventory().getFirst().quantity());
  }
}
