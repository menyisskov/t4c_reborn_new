package com.perso.T4C.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.CharacterProgress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Timed spell buffs survive the database exactly as committed: the rolled amounts, the absolute expiry
 * (including "never"), the settle anchor, and a record written before buffs carried amounts still loads
 * unchanged. Runs against PostgreSQL, including in CI.
 */
@EnabledIfEnvironmentVariable(named="T4C_DB_URL", matches=".+")
class OnlineBuffPersistenceTest {
  private OnlineStore store;

  @BeforeEach void setup() throws Exception { store = OnlineStore.fromEnvironment(); store.migrate(); }

  private static CharacterProgress withBuffs(CharacterProgress p, int version, List<CharacterProgress.Buff> buffs,
      Map<String,Long> cooldowns) {
    return new CharacterProgress(version, p.characterClass(), p.gender(), p.attributes(), p.vitals(), p.advancement(),
        p.inventory(), p.equipment(), p.skills(), p.spells(), p.questFlags(), p.storage(), p.storageGold(), p.respawn(),
        buffs, cooldowns, p.hiddenUntil(), p.companion());
  }

  @Test void buffAmountsExpiryAndAnchorRoundTripAcrossAStoreRestart() throws Exception {
    var key = store.provisionAccount();
    var created = store.create(key.accountId(), "Buffed", 0, 10, 20);
    var session = store.acquire(key.secret(), created.id());
    var buffs = List.of(
        new CharacterProgress.Buff("spell.bless", 1_900_000_000_000L, 0, 0,
            Map.of("maxHp", 70, "skill:attack", 29, "skill:archery", 29), 1_899_999_000_000L),
        new CharacterProgress.Buff("spell.light", Long.MAX_VALUE, 0, 0, Map.of("radiance", 100), 1_899_999_000_000L));
    var progress = withBuffs(CharacterProgressTest.sample(), CharacterProgress.CURRENT_VERSION, buffs,
        Map.of("effects_anchor", 1_899_999_500_000L, "attack", 1_900_000_000_000L));
    var saved = store.checkpointCharacter(session, created.revision(), 0, 10, 20, progress);
    var restarted = OnlineStore.fromEnvironment();
    restarted.migrate();
    var loaded = restarted.load(key.accountId(), created.id()).progress();
    assertEquals(progress, loaded);
    assertEquals(buffs, loaded.buffs());
    assertEquals(Long.MAX_VALUE, loaded.buffs().get(1).expiresAt(), "a buff that never expires keeps its sentinel");
    assertEquals(saved.revision(), restarted.load(key.accountId(), created.id()).revision());
    // A gameplay event on top of the buffed state keeps every amount, and a stale revision still conflicts.
    var next = restarted.commitGameplayEvent(session, saved.revision(), UUID.randomUUID(), 0, 0, loaded).character();
    assertEquals(buffs, next.progress().buffs());
    assertThrows(OnlineStore.Conflict.class,
        () -> restarted.commitGameplayEvent(session, saved.revision(), UUID.randomUUID(), 0, 0, loaded));
    store.release(session);
  }

  @Test void aVersionOneRecordWithoutAmountsLoadsUnchangedAndIsRewrittenAsWritten() throws Exception {
    var key = store.provisionAccount();
    var created = store.create(key.accountId(), "Old save", 0, 10, 20);
    var session = store.acquire(key.secret(), created.id());
    // The shape every character had before buffs carried amounts: four-field buffs, version 1.
    var old = withBuffs(CharacterProgressTest.sample(), 1,
        List.of(new CharacterProgress.Buff("spell.heal_light", 1_900_000_000_000L, 75, 75)), Map.of());
    var saved = store.checkpointCharacter(session, created.revision(), 0, 10, 20, old);
    var loaded = store.load(key.accountId(), created.id()).progress();
    assertEquals(old, loaded);
    assertEquals(1, loaded.version());
    assertTrue(loaded.buffs().get(0).modifiers().isEmpty());
    assertEquals(0, loaded.buffs().get(0).appliedAt());
    assertEquals(saved.revision(), store.load(key.accountId(), created.id()).revision());
    store.release(session);
  }
}
