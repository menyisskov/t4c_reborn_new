package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.audio.SoundSpy;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.npc.LighthavenSamaritan;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.npc.core.NPCManager;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * The online puppet switch (T4C-0289): in puppet mode every logic entry point of a monster, an NPC
 * and their registries is inert, while the default mode (pinned by {@link
 * PuppetSwitchCharacterizationTest}) is untouched.
 */
@ResourceLock("sound.manager")
class PuppetSwitchTest {
  private static final class TestMonster extends BaseMonster {
    TestMonster(float x, float y) throws GameException {
      super("Puppet test", x, y, 10, 0, 3, 50, 1, 1, 1_000L, null, null, null, "attack.wav", "die.wav");
    }
  }

  @AfterEach
  void removeSoundListener() {
    SoundSpy.remove();
  }

  private static TestMonster puppet() throws Exception {
    TestMonster monster = new TestMonster(4000, 4000);
    monster.setPuppet(true);
    return monster;
  }

  @Test
  void aPuppetMonsterIgnoresEveryLocalDamageAndAggroEntryPoint() throws Exception {
    TestMonster monster = puppet();
    int[] deaths = {0};
    monster.setDeathCallback(m -> deaths[0]++);
    Player player = new Player();
    player.getCoordinates().setX(4020);
    player.getCoordinates().setY(4000);
    long xp = player.getCurrentXp();
    SoundSpy sounds = SoundSpy.install();

    monster.takeDamage(99);
    monster.applyPlayerDamage(99, player, XpCurve.loadDefault());
    monster.applyClanDamage(99, null);
    monster.aggroOn(new Vector2(4010, 4000));
    monster.aggroOnCompanion(null);
    monster.heal(5);
    monster.respawn();

    assertFalse(monster.isDead());
    assertEquals(10, monster.getHealth());
    assertFalse(monster.isAggro());
    assertEquals(0, deaths[0]);
    assertEquals(xp, player.getCurrentXp());
    assertTrue(sounds.asked().isEmpty());
    assertFalse(monster.shouldRespawn());
    assertFalse(monster.shouldRemoveAfterDeath());
  }

  @Test
  void aPuppetNeverRunsItsAiNoMatterHowLongItIsUpdated() throws Exception {
    TestMonster monster = puppet();
    monster.puppetPlace(4000, 4000);
    for (int i = 0; i < 200; i++) monster.update(0.1f, new Vector2(4010, 4000), List.of(monster));
    assertEquals(4000f, monster.getPosition().x);
    assertEquals(4000f, monster.getPosition().y);
    assertFalse(monster.isAggro());
  }

  @Test
  void snapshotHealthIsTheOnlyWayAPuppetDiesAndItDoesSoSilently() throws Exception {
    TestMonster monster = puppet();
    int[] deaths = {0};
    monster.setDeathCallback(m -> deaths[0]++);
    SoundSpy sounds = SoundSpy.install();

    monster.puppetHealth(4, 10);
    assertEquals(4, monster.getHealth());
    assertFalse(monster.isDead());

    monster.puppetHealth(0, 10);
    assertTrue(monster.isDead());
    assertEquals(0, monster.getHealth());
    assertEquals(0, deaths[0], "no death callback: no loot, experience or quest credit");
    assertTrue(sounds.asked().isEmpty(), "no death sound from a state change");

    monster.update(5f, null, null);
    assertTrue(monster.getAnimations().isDeadComplete(), "the normal death animation ran");
    assertFalse(monster.shouldRemoveAfterDeath(), "the snapshot removes it, not a local timer");

    monster.puppetHealth(10, 10);
    assertFalse(monster.isDead(), "a living snapshot after a death is a server respawn");
    assertEquals(10, monster.getHealth());
  }

  @Test
  void aPuppetGlidesToTheSnapshotPositionAndFacesTheMove() throws Exception {
    TestMonster monster = puppet();
    monster.puppetPlace(3200, 1600);
    monster.puppetPlace(3200 + 64, 1600);
    String angle = monster.getMovement().getCurrentAngle();
    assertTrue(monster.getMovement().isMoving());

    monster.update(0.016f, null, null);
    float x = monster.getPosition().x;
    assertTrue(x > 3200f && x < 3264f, "partway after one frame: " + x);

    monster.update(2f, null, null);
    assertEquals(3264f, monster.getPosition().x);
    assertFalse(monster.getMovement().isMoving());
    assertEquals(angle, monster.getMovement().getCurrentAngle(), "facing is kept at rest");
  }

  @Test
  void aFarJumpSnapsInsteadOfGliding() throws Exception {
    TestMonster monster = puppet();
    monster.puppetPlace(0, 0);
    monster.puppetPlace(3200, 1600);
    monster.update(0.016f, null, null);
    assertEquals(3200f, monster.getPosition().x);
  }

  @Test
  void puppetModeCanBeSwitchedBackOff() throws Exception {
    TestMonster monster = puppet();
    monster.setPuppet(false);
    monster.takeDamage(4);
    assertEquals(6, monster.getHealth());
  }

  // --- registries ---

  @Test
  void aPuppetMonsterRegistryMakesPuppetsAndNeverSimulatesRespawnsScriptsOrSummons() throws Exception {
    MonsterManager manager = new MonsterManager(null);
    manager.setPuppetMode(true);
    int[] calls = {0};
    manager.setDeathCallback(m -> calls[0]++);
    manager.setLootCallback(m -> calls[0]++);
    manager.setPlayerKillCallback(m -> calls[0]++);
    manager.setScriptEffectsCallback(e -> calls[0]++);
    manager.setPlayerDamageCallback((a, d) -> calls[0]++);
    manager.setScriptPlayer(new Player());
    TestMonster monster = new TestMonster(4000, 4000);
    manager.addMonster(monster);

    assertTrue(monster.isPuppet());
    monster.puppetHealth(0, 10);
    manager.notifyKilledByPlayer(monster);
    manager.update(10f, new Vector2(4000, 4000));
    manager.updateVisible(10f, new Vector2(4000, 4000), 0, 500, 0, 500, 5);
    manager.notifyPlayerDamagedMonster(monster, new Player());
    manager.notifyMonsterAttackHit(monster, new Player());

    assertEquals(0, calls[0]);
    assertEquals(1, manager.getMonsters().size(), "only the snapshot removes a monster");
    assertFalse(manager.spawnMonster("Skeleton", 640f, 320f, false));
    assertFalse(manager.attackMonster(monster, new Player()));
  }

  @Test
  void aPuppetNpcRegistryRunsNoPopupsSummonsCombatOrConversations() throws Exception {
    NPCManager manager = new NPCManager(null);
    manager.setPuppetMode(true);
    int[] calls = {0};
    manager.setPlayerDamageCallback((a, d) -> calls[0]++);
    BaseNPC npc = new LighthavenSamaritan(new NpcContext(null));
    manager.addNPC(npc);
    npc.puppetPlace(640f, 320f);
    Player player = new Player();
    player.getCoordinates().setX(640f + 32f);
    player.getCoordinates().setY(320f);

    manager.triggerPopupEvents(player);
    assertFalse(manager.onClick(npc.getPosition().x, npc.getPosition().y, player));
    assertFalse(manager.damageNpc(npc, 5_000_000, player));
    manager.onNpcAttacked(npc, player);
    assertFalse(manager.talkToActiveNpc("hello", player));
    assertFalse(manager.spawnNPC("Anything", 1f, 1f));
    manager.update(10f, new Vector2(0, 0));
    manager.updateVisible(10f, new Vector2(0, 0), 0, 500, 0, 500, 5);

    assertTrue(npc.isPuppet());
    assertEquals(1, manager.getNPCs().size());
    assertFalse(npc.isInteracting());
    assertFalse(npc.isHostile());
    assertNotEquals(0, npc.getCurrentHp());
    assertEquals(0, calls[0]);
  }

  @Test
  void aPuppetNpcIsInertButPlacedAndAnimatedFromTheSnapshot() throws Exception {
    BaseNPC npc = new LighthavenSamaritan(new NpcContext(null));
    npc.setPuppet(true);
    npc.puppetPlace(640f, 320f);
    Player player = new Player();
    player.getCoordinates().setX(640f + 32f);
    player.getCoordinates().setY(320f);
    SoundSpy sounds = SoundSpy.install();

    assertFalse(npc.onClick(player));
    npc.onAttacked(player);
    npc.onHit(player);
    npc.onDeath(player);
    npc.onDestroy(player);
    npc.onAttack(player);
    npc.onAttackHit(player);
    npc.onInitialise(player);
    npc.onPopup(player);
    npc.provoke();
    npc.fleeFrom(player);
    assertFalse(npc.talk("hello", player));

    assertFalse(npc.isInteracting());
    assertFalse(npc.isHostile());
    assertTrue(sounds.asked().isEmpty());

    npc.puppetPlace(640f + 64f, 320f);
    npc.update(0.016f, null);
    assertTrue(npc.getPosition().x > 640f && npc.getPosition().x < 704f);
    npc.update(2f, null);
    assertEquals(704f, npc.getPosition().x);
  }
}
