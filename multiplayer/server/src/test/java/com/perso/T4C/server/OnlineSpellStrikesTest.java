package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.spell.engine.SpellEngine;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** PostgreSQL contract: one cast commits mana, exhaustion, every creature's health, kills and drops atomically. */
@EnabledIfEnvironmentVariable(named = "T4C_DB_URL", matches = ".+")
class OnlineSpellStrikesTest {
  @Test void aNukeKillsOneCreatureSplashesAnotherAndCommitsOnceThenRefusesARepeat() throws Exception {
    var store = OnlineStore.fromEnvironment(); store.migrate();
    var gson = new Gson();
    var json = gson.toJsonTree(MonsterCatalog.loadBundled().monsters().get("Skeleton")).getAsJsonObject();
    json.addProperty("health", 1); json.addProperty("respawnTime", 300_000);
    json.add("resists", gson.toJsonTree(new int[12]));
    json.add("loot", gson.toJsonTree(List.of(new MonsterDef.LootDrop("Leather gloves", 1f))));
    var small = gson.fromJson(json, MonsterDef.class);
    json.addProperty("name", "Tough skeleton"); json.addProperty("health", 100_000);
    var tough = gson.fromJson(json, MonsterDef.class);
    var killed = new MonsterContent.Spawn("spell-" + UUID.randomUUID(), small.getName(), 6, 5, 0, false, false);
    var splashed = new MonsterContent.Spawn("spell-" + UUID.randomUUID(), tough.getName(), 7, 5, 0, false, false);
    var content = new MonsterContent(MonsterContent.VERSION, Map.of(small.getName(), small, tough.getName(), tough), List.of(killed, splashed));
    var map = new PreviewMap(new boolean[20][20], 0, 0, 3, 5);
    var world = new PreviewWorld(map, content);
    var sessions = new PersistentSessions(store, map, world);
    var key = store.provisionAccount();
    var prepared = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3));
    var base = prepared.progress();
    var caster = new CharacterProgress(base.version(), base.characterClass(), base.gender(), base.attributes(),
        new CharacterProgress.Vitals(base.vitals().hp(), base.vitals().maxHp(), 20, 20), base.advancement(), base.inventory(),
        base.equipment(), base.skills(), Set.of("spell.fire_dart"), base.questFlags(), base.storage(), base.storageGold(),
        base.respawn(), base.buffs(), base.cooldowns(), 0, base.companion());
    var created = store.create(key.accountId(), "Spell caster", 0, 3.5, 5.5, caster, prepared.gold());
    String token = sessions.join(store.acquire(key.secret(), created.id()), System.nanoTime()).token();
    long now = System.currentTimeMillis();
    try (var creatures = new PersistentCreatures(store, world)) {
      var spells = new OnlineSpellStrikes(map, world, sessions, creatures, ItemCatalog.loadBundled(), SpellCatalogV2.bundled(),
          new Random(4), () -> now);
      var unit = new SpellEngine.Target.Unit(1);
      assertEquals("revision", assertThrows(PreviewWorld.Rejected.class, () -> spells.cast(token, 99, "spell.fire_dart", unit)).getMessage());
      assertEquals("wrong_target", assertThrows(PreviewWorld.Rejected.class,
          () -> spells.cast(token, 0, "spell.fire_dart", new SpellEngine.Target.Self())).getMessage());
      assertEquals(created, store.load(key.accountId(), created.id()), "refusals mutate nothing");
      var pending = spells.cast(token, 0, "spell.fire_dart", unit).toCompletableFuture();
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
      while (!pending.isDone() && System.nanoTime() < deadline) { sessions.step(); creatures.step(); Thread.sleep(5); }
      var reply = pending.get();
      assertEquals(2, reply.hits().size());
      assertTrue(reply.hits().get(0).killed());
      assertFalse(reply.hits().get(1).killed());
      var saved = store.load(key.accountId(), created.id());
      assertEquals(19, saved.progress().vitals().mana());
      assertTrue(saved.progress().cooldowns().get(OnlineCasting.MENTAL_EXHAUSTION) > now);
      assertTrue(saved.experience() > 0);
      assertEquals(reply.revision(), saved.revision());
      assertTrue(creatures.life(1).diedAt() != null || world.snapshot(token).creatures().size() == 1);
      assertEquals("revision", assertThrows(PreviewWorld.Rejected.class, () -> spells.cast(token, 0, "spell.fire_dart", unit)).getMessage(),
          "a repeat of the same revision is fenced and never replayed");
    } finally { sessions.shutdown(); }
  }
}
