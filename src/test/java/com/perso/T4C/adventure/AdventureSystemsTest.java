package com.perso.T4C.adventure;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.*;
import com.perso.T4C.item.*;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import java.util.*;
import org.junit.jupiter.api.Test;

class AdventureSystemsTest {
  @org.junit.jupiter.api.BeforeEach
  void loadItems() {
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister();
  }

  @org.junit.jupiter.api.AfterEach
  void resetItems() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  private static final class Host implements WorldEvents.Host {
    int rewards, spawns, threats;

    public boolean spawn(WorldEvents.Definition e, boolean boss, int wave) {
      spawns++;
      return true;
    }

    public int threats(WorldEvents.Definition e) {
      return threats;
    }

    public void clear(WorldEvents.Definition e) {}

    public void reward(int gold, int xp) {
      rewards++;
    }

    public void save() {}

    public void message(String text) {}
  }

  private Player eligible(WorldEvents.Definition e) {
    Player p = new Player();
    p.setStrength(500);
    p.setMaxHp(100);
    p.setCurrentHp(100);
    p.setQuestFlag(QuestService.zoneUnlockFlag(e.unlock()), 1);
    p.setWorldPosition(e.ground().x() * 32f, e.ground().y() * 16f, 0);
    return p;
  }

  private void restore(Player p, WorldEvents events, WorldEvents.Definition e) {
    for (var node : e.nodes()) {
      p.setWorldPosition(node.x() * 32f, node.y() * 16f, 0);
      assertTrue(events.interact());
    }
  }

  @Test
  void discoveriesSurviveSaleSaveAndRebirthButFailedGrantsDoNotCount() {
    Player p = new Player();
    String key = WorldEvents.rewardKey(WorldEvents.definitions().get(0), "warrior");
    p.setStrength(0);
    assertFalse(InventoryService.add(p, key).success());
    assertFalse(CollectionBook.has(p, key));
    p.setStrength(500);
    assertTrue(InventoryService.add(p, key).success());
    assertTrue(CollectionBook.has(p, key));
    assertTrue(InventoryService.remove(p, 0, ItemRegistry.findByKey(key).getKey()).success());
    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(p), restored);
    assertTrue(CollectionBook.has(restored, key));
    assertEquals(0, InventoryService.count(restored, key));
    assertTrue(com.perso.T4C.npc.behavior.RebirthBehavior.perform(restored));
    assertTrue(CollectionBook.has(restored, key));
  }

  @Test
  void eventStagesPayOnceAndPersistAnUnclaimedRewardWithoutLosingItToCapacity() {
    var e = WorldEvents.definitions().get(0);
    var p = eligible(e);
    var host = new Host();
    long[] now = {2000000000000L};
    var events = new WorldEvents(p, host, () -> now[0]);
    events.tick();
    assertEquals(WorldEvents.Stage.RESTORE, WorldEvents.stage(p, e));
    restore(p, events, e);
    assertEquals(WorldEvents.Stage.DEFEND, WorldEvents.stage(p, e));
    for (int second = 1; second <= 90; second++) {
      now[0] += 1000;
      events.tick();
    }
    assertEquals(WorldEvents.Stage.CHAMPION, WorldEvents.stage(p, e));
    events.championKilled(e);
    events.championKilled(e);
    assertEquals(1, WorldEvents.pending(p, e));
    assertEquals(3, host.rewards);
    assertTrue(WorldEvents.wardBonus(p, now[0]) > 0);
    Player saved = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(p), saved);
    saved.setStrength(0);
    assertFalse(WorldEvents.claim(saved, e, "warrior"));
    assertEquals(1, WorldEvents.pending(saved, e));
    saved.setStrength(500);
    assertTrue(WorldEvents.claim(saved, e, "warrior"));
    assertFalse(WorldEvents.claim(saved, e, "warrior"));
    assertEquals(1, InventoryService.count(saved, WorldEvents.rewardKey(e, "warrior")));
    assertEquals(0, WorldEvents.wardBonus(saved, now[0] + 600001));
  }

  @Test
  void unprotectedAndExpiredOfflineDefensesFailWithoutACompletionReward() {
    var e = WorldEvents.definitions().get(0);
    var p = eligible(e);
    var host = new Host();
    long[] now = {2000000000000L};
    var events = new WorldEvents(p, host, () -> now[0]);
    events.tick();
    restore(p, events, e);
    Player saved = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(p), saved);
    now[0] += 100000;
    new WorldEvents(saved, host, () -> now[0]).tick();
    assertEquals(WorldEvents.Stage.FAILURE, WorldEvents.stage(saved, e));
    assertEquals(0, WorldEvents.pending(saved, e));
    assertEquals(1, host.rewards, "Only the restoration checkpoint was earned");
    saved.setWorldPosition(e.ground().x() * 32f, e.ground().y() * 16f, 0);
    now[0] += 180001;
    events = new WorldEvents(saved, host, () -> now[0]);
    events.tick();
    now[0] += 1001;
    events.tick();
    restore(saved, events, e);
    host.threats = 100;
    now[0] += 1001;
    events.tick();
    now[0] += 1001;
    events.tick();
    now[0] += 1001;
    events.tick();
    now[0] += 1001;
    events.tick();
    assertEquals(WorldEvents.Stage.FAILURE, WorldEvents.stage(saved, e));
    assertEquals(0, WorldEvents.pending(saved, e));
  }

  @Test
  void collectionAndEventReferencesResolveAndTrackerRespectsUnlocks() {
    var p = new Player();
    var goals = ProgressionTracker.goals(p);
    assertTrue(
        goals.stream().noneMatch(g -> g.id().startsWith("event_") || g.id().startsWith("hunt_")));
    p.setQuestFlag(QuestService.zoneUnlockFlag("avalon_sanctuary"), 1);
    goals = ProgressionTracker.goals(p);
    assertTrue(goals.stream().anyMatch(g -> g.id().startsWith("event_")));
    ProgressionTracker.pin(p, goals.get(0));
    assertNotNull(ProgressionTracker.pinned(p, goals));
    Set<Integer> ids = new HashSet<>();
    Set<Integer> boosts = new HashSet<>();
    for (var item : ItemRegistry.load())
      if (item.getKey().startsWith("item.event_") || item.getKey().startsWith("event_")) {
        assertTrue(ids.add(item.getNumId()));
        for (var b : item.getBoosts()) assertTrue(boosts.add(b.getBoostId()));
      }
    for (var set : CollectionBook.sets())
      for (String item : set.items()) assertNotNull(ItemRegistry.findByKey(item));
    for (var e : WorldEvents.definitions())
      for (String style : com.perso.T4C.quest.definition.ConcordCampaign.STYLES)
        assertNotNull(ItemRegistry.findByKey(WorldEvents.rewardKey(e, style)));
  }

  @Test
  void clearingTransientActorsCannotFireDeathRewardsOrRemoveAuthoredMonsters() throws Exception {
    var manager = new com.perso.T4C.monster.core.MonsterManager(null);
    int[] deaths = {0};
    manager.setDeathCallback(m -> deaths[0]++);
    var def = com.perso.T4C.monster.core.MonsterRegistry.findByName("Training 1 Skeleton");
    var event = com.perso.T4C.monster.core.MonsterRegistry.create(def, 0, 0);
    var authored = com.perso.T4C.monster.core.MonsterRegistry.create(def, 32, 16);
    manager.addMonster(event);
    manager.addMonster(authored);
    manager.discardMonsters(List.of(event));
    assertEquals(0, deaths[0]);
    assertTrue(event.isDead());
    assertEquals(List.of(authored), manager.getMonsters());
  }
}
