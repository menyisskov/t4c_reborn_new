package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.spell.engine.SpellEngine;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** PostgreSQL contract: one cast commits mana, exhaustion, every creature's health, kills and drops atomically. */
@EnabledIfEnvironmentVariable(named = "T4C_DB_URL", matches = ".+")
class OnlineSpellStrikesTest {
  @Test void learnedWordOfRecallCommitsCostsAndReturnsToSavedRespawnRegion() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();var account=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(31));
    var base=prepared.progress();
    var progress=new CharacterProgress(base.version(),base.characterClass(),base.gender(),base.attributes(),
        new CharacterProgress.Vitals(base.vitals().hp(),base.vitals().maxHp(),200,200),base.advancement(),base.inventory(),
        base.equipment(),base.skills(),Set.of("spell.word_of_recall"),base.questFlags(),base.storage(),base.storageGold(),
        new CharacterProgress.Respawn(0,2944.5,1059.5),base.buffs(),base.cooldowns(),0,base.companion());
    var created=store.create(account.accountId(),"Recall caster",1,304.5,383.5,progress,0);
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    try(var hosted=new HostedWorlds(plan,store,ItemCatalog.loadBundled(),MonsterCatalog.loadBundled())) {
      var joined=hosted.sessions.join(store.acquire(account.secret(),created.id()),System.nanoTime());
      hosted.sessions.regionReady(joined.token(),0);
      var region=hosted.actor(joined.token());
      var pending=region.spells.cast(joined.token(),0,"spell.word_of_recall",new SpellEngine.Target.Self()).toCompletableFuture();
      long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
      while(!pending.isDone() && System.nanoTime()<deadline) { hosted.step();Thread.sleep(5); }
      var reply=pending.get();var saved=store.load(account.accountId(),created.id());
      assertEquals(reply.revision(),saved.revision());
      assertEquals(0,saved.world());assertEquals(1,saved.locationEpoch());
      assertEquals(2944.5,saved.x());assertEquals(1059.5,saved.y());
      assertEquals(100,saved.progress().vitals().mana());
      assertTrue(saved.progress().cooldowns().get(OnlineCasting.MENTAL_EXHAUSTION)>0);
      assertEquals(0,hosted.sessions.region(joined.token()).world());
      assertEquals(100,reply.manaSpent());
      var left=hosted.sessions.leave(joined.token());
      deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
      while(!left.isDone() && System.nanoTime()<deadline) { hosted.step();Thread.sleep(5); }
      left.get();
    }
  }

  @Test void learnedLightHealCommitsCasterCostAndNearbyPlayersHealthTogether() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var map=new PreviewMap(new boolean[20][20],0,0,3,5);
    var world=new PreviewWorld(map,new MonsterContent(MonsterContent.VERSION,MonsterCatalog.loadBundled().monsters(),List.of()));
    var sessions=new PersistentSessions(store,map,world);
    var key=store.provisionAccount();
    var healer=OnlineCharacterCreator.prepare(CharacterClass.HEALER,"male",false,new Random(8));
    var base=healer.progress();
    var casterProgress=new CharacterProgress(base.version(),base.characterClass(),base.gender(),base.attributes(),
        new CharacterProgress.Vitals(base.vitals().hp(),base.vitals().maxHp(),200,200),
        new CharacterProgress.Advancement(3,0,0,0,0),base.inventory(),base.equipment(),base.skills(),
        Set.of("spell.heal_light","spell.bless"),base.questFlags(),base.storage(),base.storageGold(),base.respawn(),base.buffs(),
        base.cooldowns(),0,base.companion());
    var recipientBase=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(9)).progress();
    var recipientProgress=new CharacterProgress(recipientBase.version(),recipientBase.characterClass(),recipientBase.gender(),
        recipientBase.attributes(),new CharacterProgress.Vitals(2,recipientBase.vitals().maxHp(),17,25),
        recipientBase.advancement(),recipientBase.inventory(),recipientBase.equipment(),recipientBase.skills(),
        recipientBase.spells(),recipientBase.questFlags(),recipientBase.storage(),recipientBase.storageGold(),
        recipientBase.respawn(),recipientBase.buffs(),recipientBase.cooldowns(),0,recipientBase.companion());
    var caster=store.create(key.accountId(),"Online healer",0,4.5,5.5,casterProgress,healer.gold());
    var recipient=store.create(key.accountId(),"Online ally",0,5.5,5.5,recipientProgress,0);
    var casterToken=sessions.join(store.acquire(key.secret(),caster.id()),System.nanoTime()).token();
    var allyJoined=sessions.join(store.acquire(key.secret(),recipient.id()),System.nanoTime());
    var clock=new AtomicLong(System.currentTimeMillis());
    long now=clock.get();
    try(var creatures=new PersistentCreatures(store,world)) {
      var spells=new OnlineSpellStrikes(map,world,sessions,creatures,ItemCatalog.loadBundled(),SpellCatalogV2.bundled(),
          new Random(10),clock::get);
      var pending=spells.cast(casterToken,0,"spell.heal_light",new SpellEngine.Target.Friend(allyJoined.id())).toCompletableFuture();
      long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
      while(!pending.isDone() && System.nanoTime()<deadline) { sessions.step();creatures.step();Thread.sleep(5); }
      var reply=pending.get();
      var savedCaster=store.load(key.accountId(),caster.id());
      var savedRecipient=store.load(key.accountId(),recipient.id());
      assertEquals(caster.revision()+1,savedCaster.revision());
      assertEquals(reply.revision(),savedCaster.revision());
      assertTrue(savedCaster.progress().vitals().mana()<200,"the spell cost commits with the heal");
      assertTrue(savedRecipient.progress().vitals().hp()>2);
      assertEquals(recipient.revision()+1,savedRecipient.revision());
      assertEquals(17,savedRecipient.progress().vitals().mana());
      clock.addAndGet(TimeUnit.MINUTES.toMillis(2)); // normal cast exhaustion elapses before the next cast
      var buffPending=spells.cast(casterToken,reply.revision(),"spell.bless",
          new SpellEngine.Target.Friend(allyJoined.id())).toCompletableFuture();
      deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
      while(!buffPending.isDone() && System.nanoTime()<deadline) { sessions.step();creatures.step();Thread.sleep(5); }
      var buffReply=buffPending.get();
      savedCaster=store.load(key.accountId(),caster.id());savedRecipient=store.load(key.accountId(),recipient.id());
      assertEquals(buffReply.revision(),savedCaster.revision());
      assertTrue(savedCaster.progress().vitals().mana()<198,"the learned buff charges the caster");
      assertTrue(savedRecipient.progress().buffs().stream().anyMatch(buff->buff.spell().equals("spell.bless")
          && buff.expiresAt()>now && !buff.modifiers().isEmpty()),"recipient buff and absolute expiry commit durably");
      assertEquals(recipient.revision()+2,savedRecipient.revision());
    } finally { sessions.shutdown(); }
  }

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
      assertNotNull(store.loadOrCreateSpawn(killed.persistenceKey()).diedAt(), "the kill is durable");
      assertNotNull(store.loadOrCreateSpawn(splashed.persistenceKey()).health(), "the splashed creature kept its reduced health");
      assertEquals("revision", assertThrows(PreviewWorld.Rejected.class, () -> spells.cast(token, 0, "spell.fire_dart", unit)).getMessage(),
          "a repeat of the same revision is fenced and never replayed");
    } finally { sessions.shutdown(); }
  }

  /** An area spell kills two creatures and wounds a third in one transaction: receipts, kills, drops and experience together. */
  @Test void aFireballKillsTwoCreaturesAndWoundsAThirdInOneAtomicCommit() throws Exception {
    var store = OnlineStore.fromEnvironment(); store.migrate();
    var gson = new Gson();
    var json = gson.toJsonTree(MonsterCatalog.loadBundled().monsters().get("Skeleton")).getAsJsonObject();
    json.addProperty("health", 1); json.addProperty("respawnTime", 300_000);
    json.add("resists", gson.toJsonTree(new int[12]));
    json.add("loot", gson.toJsonTree(List.of(new MonsterDef.LootDrop("Leather gloves", 1f))));
    var small = gson.fromJson(json, MonsterDef.class);
    json.addProperty("name", "Tough skeleton"); json.addProperty("health", 100_000);
    var tough = gson.fromJson(json, MonsterDef.class);
    var first = new MonsterContent.Spawn("area-" + UUID.randomUUID(), small.getName(), 6, 5, 0, false, false);
    var second = new MonsterContent.Spawn("area-" + UUID.randomUUID(), small.getName(), 6, 6, 0, false, false);
    var third = new MonsterContent.Spawn("area-" + UUID.randomUUID(), tough.getName(), 7, 5, 0, false, false);
    var far = new MonsterContent.Spawn("area-" + UUID.randomUUID(), tough.getName(), 17, 17, 0, false, false);
    var content = new MonsterContent(MonsterContent.VERSION, Map.of(small.getName(), small, tough.getName(), tough),
        List.of(first, second, third, far));
    var map = new PreviewMap(new boolean[20][20], 0, 0, 3, 5);
    var world = new PreviewWorld(map, content);
    var sessions = new PersistentSessions(store, map, world);
    var key = store.provisionAccount();
    var prepared = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3));
    var base = prepared.progress();
    var caster = new CharacterProgress(base.version(), base.characterClass(), base.gender(), base.attributes(),
        new CharacterProgress.Vitals(base.vitals().hp(), base.vitals().maxHp(), 200, 200), base.advancement(), base.inventory(),
        base.equipment(), base.skills(), Set.of("spell.fireball"), base.questFlags(), base.storage(), base.storageGold(),
        base.respawn(), base.buffs(), base.cooldowns(), 0, base.companion());
    var created = store.create(key.accountId(), "Area caster", 0, 3.5, 5.5, caster, prepared.gold());
    String token = sessions.join(store.acquire(key.secret(), created.id()), System.nanoTime()).token();
    long now = System.currentTimeMillis();
    try (var creatures = new PersistentCreatures(store, world)) {
      var spells = new OnlineSpellStrikes(map, world, sessions, creatures, ItemCatalog.loadBundled(), SpellCatalogV2.bundled(),
          new Random(4), () -> now);
      long aimed = world.creatureSpawns().stream().filter(s -> s.spawn().persistenceKey().equals(first.persistenceKey())).findFirst().orElseThrow().id();
      var pending = spells.cast(token, 0, "spell.fireball", new SpellEngine.Target.Unit(aimed)).toCompletableFuture();
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
      while (!pending.isDone() && System.nanoTime() < deadline) { sessions.step(); creatures.step(); Thread.sleep(5); }
      var reply = pending.get();
      assertEquals(3, reply.hits().size(), "the aimed creature and the two inside the radius; the far one is untouched");
      assertEquals(2, reply.hits().stream().filter(OnlineSpellStrikes.Hit::killed).count());
      var saved = store.load(key.accountId(), created.id());
      assertTrue(saved.progress().vitals().mana() < 200);
      assertTrue(saved.experience() > 0);
      assertEquals(reply.revision(), saved.revision());
      assertNotNull(store.loadOrCreateSpawn(first.persistenceKey()).diedAt(), "both kills are durable");
      assertNotNull(store.loadOrCreateSpawn(second.persistenceKey()).diedAt());
      assertNull(store.loadOrCreateSpawn(third.persistenceKey()).diedAt());
      assertNotNull(store.loadOrCreateSpawn(third.persistenceKey()).health(), "the wounded one kept its reduced health");
      assertNull(store.loadOrCreateSpawn(far.persistenceKey()).health(), "the distant creature was never touched");
      assertEquals("revision", assertThrows(PreviewWorld.Rejected.class,
          () -> spells.cast(token, 0, "spell.fireball", new SpellEngine.Target.Unit(aimed))).getMessage(),
          "a repeat of the same revision is fenced and never replayed");
    } finally { sessions.shutdown(); }
  }

  /** A self buff commits with its rolled amounts, survives logout and a server restart, and logged-out time never runs it down. */
  @Test void aBuffCommitsOnceSurvivesLogoutAndRestartAndLoggedOutTimeDoesNotRunItDown() throws Exception {
    var store = OnlineStore.fromEnvironment(); store.migrate();
    var map = new PreviewMap(new boolean[20][20], 0, 0, 3, 5);
    var gson = new Gson();
    var json = gson.toJsonTree(MonsterCatalog.loadBundled().monsters().get("Skeleton")).getAsJsonObject();
    json.addProperty("health", 100_000);
    json.add("resists", gson.toJsonTree(new int[12]));
    var distant = gson.fromJson(json, MonsterDef.class);
    // One far-away creature only so the region has content; the buff cast never touches it.
    var spawn = new MonsterContent.Spawn("buff-" + UUID.randomUUID(), distant.getName(), 17, 17, 0, false, false);
    var content = new MonsterContent(MonsterContent.VERSION, Map.of(distant.getName(), distant), List.of(spawn));
    var key = store.provisionAccount();
    var prepared = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3));
    var base = prepared.progress();
    var caster = new CharacterProgress(base.version(), base.characterClass(), base.gender(), base.attributes(),
        new CharacterProgress.Vitals(base.vitals().hp(), base.vitals().maxHp(), 500, 500), base.advancement(), base.inventory(),
        base.equipment(), base.skills(), Set.of("spell.stone_skin", "spell.bless", "spell.entangle"), base.questFlags(), base.storage(),
        base.storageGold(), base.respawn(), base.buffs(), base.cooldowns(), 0, base.companion());
    var created = store.create(key.accountId(), "Buff caster", 0, 3.5, 5.5, caster, prepared.gold());
    var clock = new java.util.concurrent.atomic.AtomicLong(1_900_000_000_000L);
    long start = clock.get();
    var items = ItemCatalog.loadBundled();
    var plain = OnlineCombatLoadout.resolve(created.progress(), items, start, false);
    var world = new PreviewWorld(map, content);
    var sessions = new PersistentSessions(store, map, world);
    sessions.wallClock = clock::get;
    String token = sessions.join(store.acquire(key.secret(), created.id()), System.nanoTime()).token();
    long expiresAt;
    try (var creatures = new PersistentCreatures(store, world)) {
      var spells = new OnlineSpellStrikes(map, world, sessions, creatures, items, SpellCatalogV2.bundled(), new Random(4), clock::get);
      assertEquals("unsupported_spell", assertThrows(PreviewWorld.Rejected.class,
          () -> spells.cast(token, 0, "spell.entangle", new SpellEngine.Target.Self())).getMessage());
      var pending = spells.cast(token, 0, "spell.stone_skin", new SpellEngine.Target.Self()).toCompletableFuture();
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
      while (!pending.isDone() && System.nanoTime() < deadline) { sessions.step(); creatures.step(); Thread.sleep(5); }
      var reply = pending.get();
      assertEquals(1, reply.buffs().size());
      expiresAt = reply.buffs().get(0).expiresAt();
      assertEquals(start + 1_200_000, expiresAt);
      var saved = store.load(key.accountId(), created.id());
      assertEquals(reply.revision(), saved.revision());
      assertEquals(455, saved.progress().vitals().mana(), "stone skin costs 45");
      var buff = saved.progress().buffs().get(0);
      assertEquals("spell.stone_skin", buff.spell());
      assertTrue(buff.modifiers().get("armorClass") > 0, "the rolled amount is stored");
      assertEquals(start, buff.appliedAt());
      var buffed = OnlineCombatLoadout.resolve(saved.progress(), items, start, false);
      assertEquals(plain.profile().armorClass() + buff.modifiers().get("armorClass"), buffed.profile().armorClass(), 1e-9,
          "melee and retaliation read the same armor class");
      assertEquals("revision", assertThrows(PreviewWorld.Rejected.class,
          () -> spells.cast(token, 0, "spell.stone_skin", new SpellEngine.Target.Self())).getMessage());
      // Online for a minute, then log out.
      clock.set(start + 60_000);
      var left = sessions.leave(token);
      long leaveBy = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
      while (!left.isDone() && System.nanoTime() < leaveBy) { sessions.step(); creatures.step(); Thread.sleep(5); }
      left.get();
    } finally { sessions.shutdown(); }
    var afterLogout = store.load(key.accountId(), created.id()).progress();
    assertEquals(start + 60_000, afterLogout.cooldowns().get(OnlineBuffs.ANCHOR), "the final checkpoint records when it left");
    assertEquals(expiresAt, afterLogout.buffs().get(0).expiresAt());
    // A different server process an hour later: the buff has exactly the time it had when the player left.
    long rejoin = start + 60_000 + 3_600_000;
    clock.set(rejoin);
    var restartedWorld = new PreviewWorld(map, content);
    var restarted = new PersistentSessions(store, map, restartedWorld);
    restarted.wallClock = clock::get;
    try {
      String again = restarted.join(store.acquire(key.secret(), created.id()), System.nanoTime()).token();
      var joined = restarted.character(again).progress();
      assertEquals(expiresAt + 3_600_000, joined.buffs().get(0).expiresAt());
      assertEquals(1_200_000 - 60_000, joined.buffs().get(0).expiresAt() - rejoin, "remaining time unchanged by the hour away");
      assertEquals(1, OnlineBuffs.active(joined, rejoin).size());
      assertDoesNotThrow(() -> OnlineCombatLoadout.resolve(joined, items, rejoin, false));
    } finally { restarted.shutdown(); }
  }
}
