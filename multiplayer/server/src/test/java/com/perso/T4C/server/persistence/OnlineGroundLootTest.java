package com.perso.T4C.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.LootDropDraft;
import com.perso.T4C.server.ItemCatalog;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Ground loot against real PostgreSQL. Each test uses its own region number, so runs never mix. */
@EnabledIfEnvironmentVariable(named = "T4C_DB_URL", matches = ".+")
class OnlineGroundLootTest {
  private static final String GLOVES = "item.leather_gloves";
  private static final OnlineStore.TileRect WHOLE_REGION = new OnlineStore.TileRect(0, 0, 1_000, 1_000);

  /** A character who has just killed a creature, leaving one drop in their region. */
  private record Fixture(OnlineStore.AccountKey key, int world, LootDropDraft drop,
      OnlineStore.Session ownerSession, OnlineStore.CharacterState owner) {}

  @Test void existingVersionNineRowsSurviveTheAdditiveMigrations() throws Exception {
    String schema = "loot_migration_" + UUID.randomUUID().toString().replace("-", "");
    try (var c = testConnection(); var s = c.createStatement()) { s.execute("CREATE SCHEMA " + schema); }
    var store = inSchema(schema);
    store.migrate();
    // Recreate the prior version-nine layout in this isolated test schema.
    try (var c = connectionIn(schema); var s = c.createStatement()) {
      s.execute("ALTER TABLE online_spawns DROP COLUMN current_x CASCADE, DROP COLUMN current_y CASCADE, DROP COLUMN mode CASCADE, DROP COLUMN target_epoch CASCADE, DROP COLUMN state_revision CASCADE");
      s.execute("DELETE FROM online_schema WHERE version = 11");
      s.execute("DROP TABLE online_loot_claims");
      s.execute("DROP TABLE online_ground_loot");
      s.execute("DELETE FROM online_schema WHERE version = 10");
    }
    var key = store.provisionAccount();
    var character = store.create(key.accountId(), "Version nine", 0, 10, 20, emptyProgress(), 7);
    var session = store.acquire(key.secret(), character.id());
    UUID event = UUID.randomUUID();
    var rewarded = store.reward(session, event, 3, 4).character();

    store.migrate();
    store.migrate();
    try (var c = connectionIn(schema); var s = c.createStatement();
        var rows = s.executeQuery("SELECT string_agg(version::text, ',' ORDER BY version) FROM online_schema")) {
      rows.next();
      assertEquals("1,2,3,4,5,6,7,8,9,10,11", rows.getString(1));
    }
    assertEquals(rewarded, store.load(key.accountId(), character.id()), "existing character state is untouched");
    assertFalse(store.reward(session, event, 3, 4).applied(), "existing reward receipts still replay");
  }

  @Test void lethalStrikeCommitsItsDropsWithTheRewardOrNotAtAll() throws Exception {
    var store = store();
    int world = freshWorld();
    var key = store.provisionAccount();
    var killer = store.create(key.accountId(), "Drop killer", world, 10, 20, emptyProgress(), 0);
    var session = store.acquire(key.secret(), killer.id());
    var life = store.loadOrCreateSpawn("loot:" + UUID.randomUUID());
    var motion = new OnlineStore.SpawnMotion(life.key(), life.eventId(), life.stateRevision(),
        10.5, 20.5, "IDLE", null, null);
    var drop = draft(world, motion.x(), motion.y());
    var strike = new OnlineStore.CreatureStrike(life.key(), life.eventId(), 10, 10, 0, motion);

    assertThrows(OnlineStore.Conflict.class, () -> store.commitCreatureStrike(session,
        killer.revision() + 5, life.eventId(), 3, 4, emptyProgress(), strike, null, List.of(drop)));
    assertTrue(store.listGroundLoot(world, WHOLE_REGION).isEmpty(), "a rolled-back kill leaves no loot");
    assertNull(store.loadOrCreateSpawn(life.key()).recipient(), "and no creature death");

    assertTrue(store.commitCreatureStrike(session, killer.revision(), life.eventId(), 3, 4,
        emptyProgress(), strike, null, List.of(drop)).applied());
    var listed = store.listGroundLoot(world, WHOLE_REGION);
    assertEquals(1, listed.size());
    assertEquals(drop.id(), listed.get(0).id());
    assertEquals(motion.x(), listed.get(0).x());
    assertEquals(motion.y(), listed.get(0).y());
    assertEquals(motion.x(), store.loadOrCreateSpawn(life.key()).currentX());
    assertEquals(GLOVES, listed.get(0).key());
    assertEquals(killer.id(), listed.get(0).ownerCharacter(), "the killer owns the first window");
    assertTrue(listed.get(0).ownerRemainingMillis() > 0);

    assertFalse(store.commitCreatureStrike(session, killer.revision(), life.eventId(), 3, 4,
        emptyProgress(), strike, null, List.of(drop)).applied(), "a retried kill is a replay");
    assertEquals(1, store.listGroundLoot(world, WHOLE_REGION).size(), "a replay never duplicates drops");
  }

  @Test void claimCommitsOnceAndAReplayReturnsTheSameReceipt() throws Exception {
    var store = store();
    var content = ItemCatalog.loadBundled();
    var fixture = killedDrop(store);
    var owner = fixture.owner();
    UUID claimEvent = UUID.randomUUID();
    var claimed = store.claimGroundLoot(fixture.ownerSession(), owner.revision(), claimEvent,
        fixture.drop().id(), fixture.world(), 10, 20, content);
    assertTrue(claimed.applied());
    var item = claimed.character().progress().inventory().stream()
        .filter(candidate -> candidate.id().equals(fixture.drop().id())).findFirst().orElseThrow();
    assertEquals(GLOVES, item.key());
    assertEquals(1, item.quantity());
    assertEquals(owner.revision() + 1, claimed.character().revision());
    assertTrue(store.listGroundLoot(fixture.world(), WHOLE_REGION).isEmpty());

    var replay = store.claimGroundLoot(fixture.ownerSession(), owner.revision(), claimEvent,
        fixture.drop().id(), fixture.world(), 10, 20, content);
    assertFalse(replay.applied(), "the receipt replays before the stale revision check");
    assertEquals(claimed.character(), replay.character());

    assertThrows(OnlineStore.Conflict.class, () -> store.claimGroundLoot(fixture.ownerSession(),
        claimed.character().revision(), claimEvent, UUID.randomUUID(), fixture.world(), 10, 20, content),
        "a reused event for different loot conflicts");
    assertEquals(claimed.character(), store.load(fixture.key().accountId(), owner.id()));
  }

  @Test void refusedClaimsLeaveTheLootAndEveryInventoryUntouched() throws Exception {
    var store = store();
    var content = ItemCatalog.loadBundled();
    var fixture = killedDrop(store);
    var owner = fixture.owner();
    var rival = store.create(fixture.key().accountId(), "Rival", fixture.world(), 10, 20, emptyProgress(), 0);
    var rivalSession = store.acquire(fixture.key().secret(), rival.id());

    var reserved = assertThrows(OnlineStore.Conflict.class, () -> store.claimGroundLoot(rivalSession, rival.revision(),
        UUID.randomUUID(), fixture.drop().id(), fixture.world(), 10, 20, content));
    assertEquals("loot_reserved", reserved.getMessage(), "the owner window excludes other characters");

    var far = assertThrows(OnlineStore.Conflict.class, () -> store.claimGroundLoot(fixture.ownerSession(),
        owner.revision(), UUID.randomUUID(), fixture.drop().id(), fixture.world(), 19, 20, content));
    assertEquals("out_of_range", far.getMessage(), "nine tiles is one tile too far");

    var listed = store.listGroundLoot(fixture.world(), WHOLE_REGION);
    assertEquals(1, listed.size());
    assertEquals(owner.id(), listed.get(0).ownerCharacter());
    assertEquals(owner, store.load(fixture.key().accountId(), owner.id()), "refused pickups change no state");
    assertEquals(rival, store.load(fixture.key().accountId(), rival.id()));
  }

  @Test void concurrentTwoCharacterClaimHasExactlyOneWinner() throws Exception {
    var store = store();
    var content = ItemCatalog.loadBundled();
    var fixture = killedDrop(store);
    // Close the owner window so both claimants are equally eligible.
    updateLoot("UPDATE online_ground_loot SET owner_until=clock_timestamp()-interval '1 second' WHERE id=?",
        fixture.drop().id());
    var key = fixture.key();
    var first = store.acquire(key.secret(),
        store.create(key.accountId(), "Race one", fixture.world(), 10, 20, emptyProgress(), 0).id());
    var second = store.acquire(key.secret(),
        store.create(key.accountId(), "Race two", fixture.world(), 10, 20, emptyProgress(), 0).id());
    var start = new CountDownLatch(1);
    var outcomes = new ArrayList<String>();
    try (var executor = Executors.newFixedThreadPool(2)) {
      var attempts = new ArrayList<java.util.concurrent.Future<String>>();
      for (var session : List.of(first, second)) attempts.add(executor.submit(() -> {
        start.await();
        try {
          store.claimGroundLoot(session, session.character().revision(), UUID.randomUUID(),
              fixture.drop().id(), fixture.world(), 10, 20, content);
          return "won";
        } catch (OnlineStore.Conflict refused) {
          return refused.getMessage();
        }
      }));
      start.countDown();
      for (var attempt : attempts) outcomes.add(attempt.get());
    }
    assertEquals(1, outcomes.stream().filter("won"::equals).count(), "exactly one character wins: " + outcomes);
    assertEquals(List.of("loot_unavailable"), outcomes.stream().filter(outcome -> !outcome.equals("won")).toList());
    assertTrue(store.listGroundLoot(fixture.world(), WHOLE_REGION).isEmpty());
    int holders = 0;
    for (var character : List.of(first.character(), second.character())) {
      var saved = store.load(key.accountId(), character.id());
      if (saved.progress().inventory().stream().anyMatch(item -> item.id().equals(fixture.drop().id()))) holders++;
    }
    assertEquals(1, holders, "the item lives in exactly one inventory");
  }

  @Test void expiredLootIsRefusedAndSweptExactlyOnce() throws Exception {
    var store = store();
    var content = ItemCatalog.loadBundled();
    var fixture = killedDrop(store);
    updateLoot("UPDATE online_ground_loot SET owner_until=clock_timestamp()-interval '2 seconds', "
        + "expires_at=clock_timestamp()-interval '1 second' WHERE id=?", fixture.drop().id());

    var expired = assertThrows(OnlineStore.Conflict.class, () -> store.claimGroundLoot(fixture.ownerSession(),
        fixture.owner().revision(), UUID.randomUUID(), fixture.drop().id(), fixture.world(), 10, 20, content));
    assertEquals("loot_unavailable", expired.getMessage(), "the database clock refuses expired loot before any sweep");
    assertTrue(store.listGroundLoot(fixture.world(), WHOLE_REGION).isEmpty());

    // Other runs may leave due rows in the shared schema, so sweep in bounded batches until ours appears.
    int sweptHere = 0;
    for (int batch = 0; batch < 100; batch++) {
      var swept = store.expireGroundLoot(256);
      if (swept.isEmpty()) break;
      sweptHere += (int) swept.stream().filter(loot -> loot.id().equals(fixture.drop().id())).count();
    }
    assertEquals(1, sweptHere, "the sweep reports the drop exactly once");
    assertTrue(store.expireGroundLoot(256).stream().noneMatch(loot -> loot.id().equals(fixture.drop().id())),
        "a swept drop is never reported again");
    assertEquals(fixture.owner(), store.load(fixture.key().accountId(), fixture.owner().id()), "expiry grants nothing");
  }

  /** A lethal kill by a new character, committed through the real strike path. */
  private static Fixture killedDrop(OnlineStore store) throws SQLException {
    int world = freshWorld();
    var key = store.provisionAccount();
    var owner = store.create(key.accountId(), "Looter", world, 10, 20, emptyProgress(), 0);
    var session = store.acquire(key.secret(), owner.id());
    var life = store.loadOrCreateSpawn("loot:" + UUID.randomUUID());
    var drop = draft(world, 10, 20);
    var strike = new OnlineStore.CreatureStrike(life.key(), life.eventId(), 10, 10, 0);
    store.commitCreatureStrike(session, owner.revision(), life.eventId(), 3, 4, emptyProgress(), strike, null, List.of(drop));
    return new Fixture(key, world, drop, session, store.load(key.accountId(), owner.id()));
  }

  private static CharacterProgress emptyProgress() {
    return new CharacterProgress(1, "warrior", "male", new CharacterProgress.Attributes(95, 85, 75, 75, 75),
        new CharacterProgress.Vitals(41, 60, 17, 25), new CharacterProgress.Advancement(2, 3, 5, 0, 100),
        List.of(), Map.of(), Map.of(), Set.of(), Map.of(), List.of(), 0,
        new CharacterProgress.Respawn(0, 20, 30), List.of(), Map.of(), 0, null);
  }

  private static LootDropDraft draft(int world, double x, double y) {
    return new LootDropDraft(UUID.randomUUID(), 0, GLOVES, 1, 0, world, x, y);
  }

  private static int freshWorld() {
    return ThreadLocalRandom.current().nextInt(1_000_000, Integer.MAX_VALUE);
  }

  private static OnlineStore store() throws SQLException {
    var store = OnlineStore.fromEnvironment();
    store.migrate();
    return store;
  }

  private static OnlineStore inSchema(String schema) {
    return new OnlineStore(() -> connectionIn(schema));
  }

  private static Connection connectionIn(String schema) throws SQLException {
    var c = testConnection();
    c.setSchema(schema);
    return c;
  }

  private static Connection testConnection() throws SQLException {
    return DriverManager.getConnection(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"));
  }

  private static void updateLoot(String sql, UUID id) throws SQLException {
    try (var c = testConnection(); var s = c.prepareStatement(sql)) {
      s.setObject(1, id);
      assertEquals(1, s.executeUpdate());
    }
  }
}
