package com.perso.T4C.monster;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.mirror.MirrorTrials;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.DamageCallback;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/** T4C-0042: the Echo copies whoever summoned it and scales to them, not to a stat table. */
@ResourceLock("EchoOfSelf")
class EchoOfSelfTest {

  @AfterEach
  void dismissAnyLivingEcho() {
    EchoOfSelf.onMakerFell();
  }

  private static Player maker(int tier) {
    Player player = new Player();
    player.setName("Lynania");
    player.setLevel(250);
    player.setMaxHp(8_000);
    player.setCurrentHp(8_000);
    player.setStrength(400);
    player.setWorldPosition(100 * GRID_W, 100 * GRID_H, 0);
    player.setQuestFlag(MirrorTrials.FLAG_CHALLENGE, tier);
    return player;
  }

  private static EchoOfSelf summonFor(Player player) throws Exception {
    MonsterDef def = MonsterRegistry.findByName(MirrorTrials.ECHO_MONSTER_NAME);
    assertNotNull(def, "the Echo must be summonable by name");
    BaseMonster monster = MonsterRegistry.create(def, 103 * GRID_W, 100 * GRID_H);
    EchoOfSelf echo = assertInstanceOf(EchoOfSelf.class, monster);
    echo.setScriptPlayer(player);
    echo.update(0.016f, player.getPositionVector(), List.of());
    return echo;
  }

  @Test
  void copiesItsMakersNameLevelAndTrial() throws Exception {
    Player player = maker(3);
    EchoOfSelf echo = summonFor(player);

    assertEquals("Echo of Lynania", echo.getName());
    assertEquals(250, echo.getCombatLevel());
    assertEquals(3, echo.getTier());
    assertEquals(player, echo.getMaker());
    assertTrue(EchoOfSelf.isActive());
    assertFalse(echo.shouldRespawn());
  }

  @Test
  void healthIsSizedByTheMakersStrongestOpeningBlow() throws Exception {
    Player player = maker(1);
    EchoOfSelf echo = summonFor(player);

    echo.applyPlayerDamage(500, player, null);
    int expected = 500 * MirrorTrials.blowsToWin(1);
    assertEquals(expected, echo.getMaxHealth());
    assertEquals(expected - 500, echo.getHealth());

    echo.applyPlayerDamage(900, player, null);
    assertEquals(900 * MirrorTrials.blowsToWin(1), echo.getMaxHealth());
    assertEquals(900 * MirrorTrials.blowsToWin(1) - 1400, echo.getHealth());

    echo.applyPlayerDamage(100, player, null);
    assertEquals(900 * MirrorTrials.blowsToWin(1), echo.getMaxHealth(), "weaker blows never shrink it");
  }

  @Test
  void instantKillEffectsOnlyWoundIt() throws Exception {
    EchoOfSelf echo = summonFor(maker(1));
    echo.takeDamage(echo.getHealth());
    assertFalse(echo.isDead());
  }

  @Test
  void itsBlowsLandAsAShareOfTheMakersLifeThroughTheirArmor() throws Exception {
    Player player = maker(1);
    EchoOfSelf echo = summonFor(player);
    List<Integer> raw = new ArrayList<>();
    echo.setDamageCallback(
        new DamageCallback() {
          @Override
          public void applyDamage(BaseMonster attacker, int damage) {
            raw.add(damage);
          }
        });
    for (int i = 0; i < 50; i++) echo.performAttack(player.getPositionVector());

    int[] range = MirrorTrials.hitRange(player.getMaxHp(), 1);
    int armor =
        (int) com.perso.T4C.combat.CombatProfiles.fromPlayer(player).armorClass();
    for (int damage : raw) {
      assertTrue(damage >= range[0] + armor && damage <= range[1] + armor, "raw " + damage);
    }
    assertEquals(50, raw.size());
  }

  @Test
  void fadesWithoutDyingWhenItsMakerWalksAway() throws Exception {
    Player player = maker(1);
    EchoOfSelf echo = summonFor(player);
    AtomicInteger deaths = new AtomicInteger();
    echo.setDeathCallback(ignored -> deaths.incrementAndGet());

    player.setWorldPosition(400 * GRID_W, 400 * GRID_H, 0);
    echo.update(0.016f, player.getPositionVector(), List.of());

    assertTrue(echo.isDead());
    assertEquals(0, deaths.get(), "a faded Echo pays nothing");
    assertFalse(EchoOfSelf.isActive());
  }

  @Test
  void fallingToItCountsAgainstTheMakerAndEndsTheFight() throws Exception {
    Player player = maker(1);
    EchoOfSelf echo = summonFor(player);

    EchoOfSelf.onMakerFell();

    assertTrue(echo.isDead());
    assertEquals(1, player.getQuestFlag(MirrorTrials.FLAG_FALLS));
  }

  @Test
  void aKillingBlowFiresTheDeathCallbackWithItsTrial() throws Exception {
    Player player = maker(4);
    EchoOfSelf echo = summonFor(player);
    AtomicInteger deaths = new AtomicInteger();
    echo.setDeathCallback(ignored -> deaths.incrementAndGet());

    echo.applyPlayerDamage(1_000, player, null);
    while (!echo.isDead()) echo.applyPlayerDamage(1_000, player, null);

    assertEquals(1, deaths.get());
    assertEquals(4, echo.getTier());
    assertEquals(0, player.getCurrentXp(), "the Echo itself grants no kill XP");
  }

  @Test
  void summoningANewEchoRetiresTheOldOne() throws Exception {
    Player player = maker(1);
    EchoOfSelf first = summonFor(player);
    EchoOfSelf second = summonFor(player);

    assertTrue(first.isDead(), "only one reflection at a time");
    assertFalse(second.isDead());
    assertTrue(EchoOfSelf.isActive());
  }
}
