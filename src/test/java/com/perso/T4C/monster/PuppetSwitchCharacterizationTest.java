package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.npc.LighthavenSamaritan;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.npc.core.NPCManager;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Characterizes, in the default (non-puppet) mode, every single-player method that the online
 * puppet switch (T4C-0288) touches: a lethal hit fires the death callback and plays the death
 * sound, a kill pays loot and kill callbacks once, damage pays experience, a dead monster is
 * removed or respawned on schedule, an NPC click starts a conversation in range and a hit NPC
 * dies and is removed. Written before the switch exists; it must pass unchanged afterwards.
 */
@ResourceLock("sound.manager")
class PuppetSwitchCharacterizationTest {
  /** Counts every attempt to play a sound; the sound manager asks the asset manager first. */
  private static final class CountingAssets extends AssetManager {
    final List<String> asked = new ArrayList<>();

    @Override
    public synchronized boolean isLoaded(String fileName) {
      asked.add(fileName);
      return false;
    }
  }

  private static final class TestMonster extends BaseMonster {
    TestMonster(float x, float y) throws GameException {
      super("Puppet test", x, y, 10, 0, 3, 50, 1, 1, 1_000L, null, null, null, "attack.wav", "die.wav");
    }
  }

  private CountingAssets assets;

  private CountingAssets listenForSounds() {
    assets = new CountingAssets();
    SoundManager.init(assets);
    return assets;
  }

  @AfterEach
  void removeSoundListener() {
    SoundManager.init(null);
  }

  // --- monster ---

  @Test
  void aLethalHitFiresTheDeathCallbackOnceAndPlaysTheDeathSound() throws Exception {
    TestMonster monster = new TestMonster(0, 0);
    int[] deaths = {0};
    monster.setDeathCallback(m -> deaths[0]++);
    CountingAssets sounds = listenForSounds();

    monster.takeDamage(99);
    monster.takeDamage(99);

    assertTrue(monster.isDead());
    assertEquals(0, monster.getHealth());
    assertEquals(1, deaths[0]);
    assertFalse(sounds.asked.isEmpty(), "the death animation plays its sound");
  }

  @Test
  void aSurvivedHitOnlyLowersHealth() throws Exception {
    TestMonster monster = new TestMonster(0, 0);
    int[] deaths = {0};
    monster.setDeathCallback(m -> deaths[0]++);

    monster.takeDamage(4);

    assertFalse(monster.isDead());
    assertEquals(6, monster.getHealth());
    assertEquals(0, deaths[0]);
  }

  @Test
  void healRaisesLivingMonstersOnlyAndStopsAtTheMaximum() throws Exception {
    TestMonster monster = new TestMonster(0, 0);
    monster.takeDamage(4);
    monster.heal(100);
    assertEquals(10, monster.getHealth());
    monster.takeDamage(99);
    monster.heal(5);
    assertEquals(0, monster.getHealth());
  }

  @Test
  void playerDamagePaysExperienceAndAggroesTheMonster() throws Exception {
    TestMonster monster = new TestMonster(4000, 4000);
    Player player = new Player();
    player.getCoordinates().setX(4020);
    player.getCoordinates().setY(4000);
    long before = player.getCurrentXp();

    monster.applyPlayerDamage(3, player, XpCurve.loadDefault());

    assertTrue(player.getCurrentXp() > before, "damage experience is paid");
    assertTrue(monster.isAggro());
    assertEquals(7, monster.getHealth());
  }

  @Test
  void aKillingPlayerHitPaysTheDeathExperienceAndFiresTheDeathCallback() throws Exception {
    TestMonster monster = new TestMonster(4000, 4000);
    int[] deaths = {0};
    monster.setDeathCallback(m -> deaths[0]++);
    Player player = new Player();
    player.getCoordinates().setX(4020);
    player.getCoordinates().setY(4000);
    long before = player.getCurrentXp();

    monster.applyPlayerDamage(99, player, XpCurve.loadDefault());

    assertTrue(monster.isDead());
    assertEquals(1, deaths[0]);
    assertTrue(player.getCurrentXp() > before);
  }

  @Test
  void aDeadMonsterWithoutRespawnIsRemovedOnceItsDeathAnimationEnds() throws Exception {
    TestMonster monster = new TestMonster(0, 0);
    monster.disableRespawn();
    monster.takeDamage(99);
    assertFalse(monster.shouldRemoveAfterDeath(), "the death animation is still playing");

    monster.update(5f, new Vector2(0, 0));

    assertTrue(monster.shouldRemoveAfterDeath());
    assertFalse(monster.shouldRespawn(), "a monster without respawn never comes back");
  }

  @Test
  void aDeadMonsterWithRespawnComesBackAtItsSpawnWithFullHealth() throws Exception {
    TestMonster monster = new TestMonster(64, 32);
    monster.setRespawnDelayMillis(0L);
    monster.takeDamage(99);
    monster.update(5f, new Vector2(0, 0));
    assertTrue(monster.shouldRespawn());

    monster.respawn();

    assertFalse(monster.isDead());
    assertEquals(10, monster.getHealth());
    assertEquals(64f, monster.getPosition().x);
    assertEquals(32f, monster.getPosition().y);
  }

  @Test
  void aStationaryLivingMonsterStaysWhereItIs() throws Exception {
    TestMonster monster = new TestMonster(64, 32);
    monster.setStationary(true);
    monster.update(1f, new Vector2(65, 32));
    assertEquals(64f, monster.getPosition().x);
    assertFalse(monster.isAggro());
  }

  @Test
  void aggroOnMakesAMobileMonsterHostile() throws Exception {
    TestMonster monster = new TestMonster(4000, 4000);
    monster.aggroOn(new Vector2(4010, 4000));
    assertTrue(monster.isAggro());
  }

  // --- monster manager ---

  @Test
  void addedMonstersInheritTheManagersDeathCallback() throws Exception {
    MonsterManager manager = new MonsterManager(null);
    int[] deaths = {0};
    manager.setDeathCallback(m -> deaths[0]++);
    TestMonster monster = new TestMonster(0, 0);
    manager.addMonster(monster);

    monster.takeDamage(99);

    assertEquals(1, deaths[0]);
    assertEquals(1, manager.getMonsters().size());
  }

  @Test
  void aPlayerKillPaysLootAndKillCallbacksOnlyOnce() throws Exception {
    MonsterManager manager = new MonsterManager(null);
    int[] loot = {0};
    int[] kills = {0};
    manager.setLootCallback(m -> loot[0]++);
    manager.setPlayerKillCallback(m -> kills[0]++);
    TestMonster monster = new TestMonster(0, 0);
    manager.addMonster(monster);

    manager.notifyKilledByPlayer(monster);
    assertEquals(0, loot[0], "a living monster pays nothing");
    monster.takeDamage(99);
    manager.notifyKilledByPlayer(monster);
    manager.notifyKilledByPlayer(monster);

    assertEquals(1, loot[0]);
    assertEquals(1, kills[0]);
  }

  @Test
  void updatingTheManagerDropsFinishedMonstersWithoutRespawn() throws Exception {
    MonsterManager manager = new MonsterManager(null);
    TestMonster monster = new TestMonster(0, 0);
    monster.disableRespawn();
    manager.addMonster(monster);
    monster.takeDamage(99);

    manager.update(5f, new Vector2(0, 0));

    assertTrue(manager.getMonsters().isEmpty());
  }

  @Test
  void summoningAKnownMonsterWithoutRespawnAddsOneAndAnUnknownNameNothing() {
    MonsterManager manager = new MonsterManager(null);
    assertTrue(manager.spawnMonster("Skeleton", 640f, 320f, false));
    assertEquals(1, manager.getMonsters().size());
    assertFalse(manager.spawnMonster("No Such Creature", 640f, 320f, false));
    assertEquals(1, manager.getMonsters().size());
  }

  @Test
  void attackingADeadMonsterOrWithoutAPlayerDoesNothing() throws Exception {
    MonsterManager manager = new MonsterManager(null);
    TestMonster monster = new TestMonster(0, 0);
    manager.addMonster(monster);
    assertFalse(manager.attackMonster(monster, null));
    monster.takeDamage(99);
    assertFalse(manager.attackMonster(monster, new Player()));
  }

  // --- npc ---

  private static BaseNPC npc() throws Exception {
    BaseNPC npc = new LighthavenSamaritan(new NpcContext(null));
    npc.setSpawnPosition(640f, 320f);
    return npc;
  }

  private static Player playerAt(float x, float y) throws Exception {
    Player player = new Player();
    player.getCoordinates().setX(x);
    player.getCoordinates().setY(y);
    return player;
  }

  @Test
  void clickingAnNpcInRangeStartsAConversationAndFarAwayDoesNot() throws Exception {
    BaseNPC near = npc();
    assertTrue(near.onClick(playerAt(640f + 32f, 320f)));
    assertTrue(near.isInteracting());

    BaseNPC far = npc();
    assertFalse(far.onClick(playerAt(640f + 3200f, 320f)));
    assertFalse(far.isInteracting());
  }

  @Test
  void aStationaryNpcStaysWhereItIsWhenUpdated() throws Exception {
    BaseNPC npc = npc();
    npc.setStationary(true);
    npc.update(1f, new Vector2(0, 0));
    assertEquals(640f, npc.getPosition().x);
    assertEquals(320f, npc.getPosition().y);
  }

  @Test
  void aKilledNpcIsRemovedFromTheManager() throws Exception {
    NPCManager manager = new NPCManager(null);
    BaseNPC npc = npc();
    manager.addNPC(npc);
    assertNotNull(npc);

    boolean killed = manager.damageNpc(npc, 5_000_000, playerAt(640f + 32f, 320f));

    assertTrue(killed);
    assertTrue(manager.getNPCs().isEmpty());
  }

  @Test
  void summoningAKnownNpcAddsItAndAnUnknownNameDoesNot() {
    NPCManager manager = new NPCManager(null);
    String id = com.perso.T4C.npc.core.NpcFactoryRegistry.registrations().get(0).id();
    assertTrue(manager.spawnNPC(id, 100f, 100f));
    assertEquals(1, manager.getNPCs().size());
    assertFalse(manager.spawnNPC("NoSuchNpc", 100f, 100f));
    assertEquals(1, manager.getNPCs().size());
  }

  @Test
  void poppingUpNpcsKeepsThemInTheManager() throws Exception {
    NPCManager manager = new NPCManager(null);
    manager.addNPC(npc());
    manager.triggerPopupEvents(new Player());
    assertEquals(1, manager.getNPCs().size());
  }
}
