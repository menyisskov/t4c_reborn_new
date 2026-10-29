package com.perso.T4C.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.audio.SoundSpy;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.npc.core.NPCManager;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.NpcView;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * The sweep test of the client unification design (section 5): the server's snapshot is applied to
 * EVERY registered monster class and NPC class, a death (for NPCs the matching removal and every
 * combat and conversation entry point) is applied to each, and no loot drop, experience award,
 * quest credit, sound, save or other game callback may fire. A new monster or NPC class is covered
 * automatically.
 */
@ResourceLock("sound.manager")
@ResourceLock("npc.summon")
class PuppetSweepTest {
  private static final Pattern PUBLIC_NPC_ID = Pattern.compile("[A-Za-z][A-Za-z0-9_]{0,63}");

  @AfterEach
  void removeSoundListener() {
    SoundSpy.remove();
  }

  private OnlineConnection.View view;
  private int callbacks;

  private WorldEntities load(RemoteWorldDriver driver) {
    return driver.loadEntities(new WorldEntityInputs(null, null, null, () -> null, null));
  }

  private void tick(RemoteWorldDriver driver, WorldEntities entities, float delta) {
    driver.tickEntities(
        new EntityTick(
            entities.npcs(), entities.monsters(), new Player(), new Vector2(), delta, 0, 100000, 0, 100000, 5));
  }

  private static Player witness() throws Exception {
    Player player = new Player();
    player.getCoordinates().setX(4020);
    player.getCoordinates().setY(4000);
    player.setGold(500);
    return player;
  }

  private static String fingerprint(Player player) {
    return player.getCurrentXp()
        + "|"
        + player.getLevel()
        + "|"
        + player.getGold()
        + "|"
        + player.getInventory()
        + "|"
        + player.getQuestFlags();
  }

  @Test
  void aDeathSnapshotOnEveryRegisteredMonsterClassFiresNothing() throws Exception {
    // The data-driven (JSON) monsters are registered at game start; sweep them with the classes.
    com.perso.T4C.monster.json.MonsterJsonLoader.loadAndRegister();
    try {
      sweepMonsters();
    } finally {
      MonsterRegistry.resetToGeneratedDefinitions();
    }
  }

  private void sweepMonsters() throws Exception {
    var byName = new java.util.LinkedHashMap<String, com.perso.T4C.monster.core.MonsterDef>();
    for (var definition : MonsterRegistry.load()) byName.putIfAbsent(definition.getName(), definition);
    var definitions = new ArrayList<>(byName.values());
    assertTrue(definitions.size() > 400, "the registry is populated: " + definitions.size());
    System.out.println("SWEEP monster classes: " + definitions.size());
    var driver = new RemoteWorldDriver(() -> view, null);
    WorldEntities entities = load(driver);
    MonsterManager manager = entities.monsters();
    manager.setDeathCallback(m -> callbacks++);
    manager.setLootCallback(m -> callbacks++);
    manager.setPlayerKillCallback(m -> callbacks++);
    manager.setScriptEffectsCallback(e -> callbacks++);
    manager.setPlayerDamageCallback((a, d) -> callbacks++);
    manager.setDamageDealtCallback((d, p) -> callbacks++);
    manager.setCompanionDamageCallback(d -> callbacks++);

    var alive = new ArrayList<CreatureView>();
    var dead = new ArrayList<CreatureView>();
    long id = 1;
    for (var definition : definitions) {
      String walk =
          definition.getWalkPattern() == null || definition.getWalkPattern().isBlank()
              ? "walk"
              : definition.getWalkPattern();
      alive.add(new CreatureView(id, definition.getName(), 10 + id % 50, 10 + id / 50, 10, 10, walk, null, null));
      dead.add(new CreatureView(id, definition.getName(), 10 + id % 50, 10 + id / 50, 0, 10, walk, null, null));
      id++;
    }
    view = WorldFixtures.view(1, List.of(), alive, List.of(), List.of());
    tick(driver, entities, 0.016f);
    assertTrue(driver.unknownDefinitions().isEmpty(), "unknown: " + driver.unknownDefinitions());
    assertEquals(definitions.size(), manager.getMonsters().size(), "one puppet per class");

    SoundSpy sounds = SoundSpy.install();
    Player player = witness();
    String before = fingerprint(player);
    XpCurve curve = XpCurve.loadDefault();

    view = WorldFixtures.view(2, List.of(), dead, List.of(), List.of());
    tick(driver, entities, 0.016f);
    tick(driver, entities, 5f);

    for (BaseMonster monster : manager.getMonsters()) {
      String label = monster.getClass().getSimpleName() + " / " + monster.getCanonicalName();
      assertTrue(monster.isPuppet(), label);
      assertTrue(monster.isDead(), label + " dies from the snapshot");
      assertEquals(0, monster.getHealth(), label);
      // Every local entry point that would pay out, on a dead and then on a revived puppet.
      monster.takeDamage(1_000_000);
      monster.applyPlayerDamage(1_000_000, player, curve);
      monster.applyClanDamage(1_000_000, monster);
      monster.aggroOn(player.getPositionVector());
      monster.heal(1_000_000);
      monster.respawn();
      monster.update(10f, player.getPositionVector(), List.of(monster));
      assertFalse(monster.shouldRespawn(), label);
      assertFalse(monster.shouldRemoveAfterDeath(), label);
      manager.notifyKilledByPlayer(monster);
      manager.notifyPlayerDamagedMonster(monster, player);
      manager.notifyMonsterAttackHit(monster, player);
      assertFalse(manager.attackMonster(monster, player), label);
    }
    manager.update(10f, player.getPositionVector());
    manager.updateVisible(10f, player.getPositionVector(), 0, 100000, 0, 100000, 5);

    assertEquals(definitions.size(), manager.getMonsters().size(), "only the snapshot removes monsters");
    assertEquals(0, callbacks, "no loot, experience, quest, script, damage or death callback fired");
    assertTrue(sounds.asked().isEmpty(), "sounds asked for: " + sounds.asked());
    assertEquals(before, fingerprint(player), "the witness player gained nothing");

    view = WorldFixtures.view(3, List.of(), List.of(), List.of(), List.of());
    tick(driver, entities, 0.016f);
    assertTrue(manager.getMonsters().isEmpty());
    assertEquals(0, callbacks);
  }

  @Test
  void combatAndConversationEntryPointsOnEveryRegisteredNpcClassFireNothing() throws Exception {
    var registrations = NpcFactoryRegistry.registrations();
    assertTrue(registrations.size() > 400, "the registry is populated: " + registrations.size());
    System.out.println("SWEEP npc classes: " + registrations.size());
    var driver = new RemoteWorldDriver(() -> view, null);
    WorldEntities entities = load(driver);
    NPCManager manager = entities.npcs();
    manager.setPlayerDamageCallback((a, d) -> callbacks++);

    var views = new ArrayList<NpcView>();
    int skipped = 0;
    int index = 0;
    for (var registration : registrations) {
      if (!PUBLIC_NPC_ID.matcher(registration.id()).matches()) {
        skipped++;
        continue;
      }
      String sprite = registration.spriteBase();
      views.add(
          new NpcView(
              registration.id(),
              registration.displayName() == null || registration.displayName().isBlank()
                  ? registration.id()
                  : registration.displayName(),
              10 + index % 50,
              10 + index / 50,
              sprite == null || sprite.isBlank() ? "@invisible" : sprite,
              Map.of()));
      index++;
    }
    view = WorldFixtures.view(1, List.of(), List.of(), views, List.of());
    SoundSpy sounds = SoundSpy.install();
    tick(driver, entities, 0.016f);
    System.out.println("SWEEP npc puppets: " + views.size() + ", ids not publishable: " + skipped);
    assertEquals(views.size(), manager.getNPCs().size(), "one puppet per class (" + skipped + " ids not publishable)");

    Player player = witness();
    String before = fingerprint(player);
    for (BaseNPC npc : manager.getNPCs()) {
      String label = npc.getClass().getSimpleName() + " / " + npc.getTypeId();
      assertTrue(npc.isPuppet(), label);
      player.getCoordinates().setX(npc.getPosition().x + 8f);
      player.getCoordinates().setY(npc.getPosition().y);
      assertFalse(npc.onClick(player), label);
      npc.onInitialise(player);
      npc.onPopup(player);
      npc.onAttacked(player);
      npc.onHit(player);
      npc.onAttack(player);
      npc.onAttackHit(player);
      npc.onDeath(player);
      npc.onDestroy(player);
      npc.provoke();
      npc.fleeFrom(player);
      assertFalse(npc.talk("hello", player), label);
      npc.update(10f, player.getPositionVector());
      npc.checkPlayerRange(player.getPositionVector());
      assertFalse(npc.isInteracting(), label);
      assertFalse(npc.isHostile(), label);
      assertFalse(manager.damageNpc(npc, 1_000_000, player), label);
      manager.onNpcAttacked(npc, player);
      assertFalse(
          manager.attackNpc(npc.getPosition().x, npc.getPosition().y, player, null), label);
      assertFalse(manager.onClick(npc.getPosition().x, npc.getPosition().y, player), label);
    }
    manager.triggerPopupEvents(player);
    assertFalse(manager.talkToActiveNpc("hello", player));
    assertFalse(manager.spawnNPC(registrations.get(0).id(), 1f, 1f));
    manager.update(10f, player.getPositionVector());
    manager.updateVisible(10f, player.getPositionVector(), 0, 100000, 0, 100000, 5);

    assertEquals(views.size(), manager.getNPCs().size(), "only the snapshot removes NPCs");
    assertEquals(0, callbacks);
    assertTrue(sounds.asked().isEmpty(), "sounds asked for: " + sounds.asked());
    assertEquals(before, fingerprint(player), "the witness player gained nothing");

    view = WorldFixtures.view(2, List.of(), List.of(), List.of(), List.of());
    tick(driver, entities, 0.016f);
    assertTrue(manager.getNPCs().isEmpty(), "removal by the snapshot");
    assertEquals(0, callbacks);
  }
}
