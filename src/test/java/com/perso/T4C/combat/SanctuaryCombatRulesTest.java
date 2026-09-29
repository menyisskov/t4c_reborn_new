package com.perso.T4C.combat;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.helper.SanctuaryCombatRules;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellCastingService;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellEffectManager;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("CollisionManager")
class SanctuaryCombatRulesTest {
  @TempDir Path tempDir;

  @BeforeEach
  void initializeMap() throws Exception {
    byte[] collision = new byte[16];
    collision[5] = 6;
    collision[6] = 7;
    Path file = tempDir.resolve("sanctuary.colbin");
    CollisionMapIO.write(file.toFile(), 4, 4, collision);
    CollisionManager.getInstance().initialize(new CollisionReader(file.toFile()));
  }

  @AfterEach
  void clearMap() {
    CollisionManager.getInstance().clear();
  }

  @Test
  void bothSafeTileTypesProtectBothEndpoints() {
    Vector2 outside = new Vector2(0, 0);
    for (int x : new int[] {1, 2}) {
      Vector2 sanctuary = new Vector2(x * GRID_W, GRID_H);
      assertFalse(SanctuaryCombatRules.canFight(outside, sanctuary));
      assertFalse(SanctuaryCombatRules.canFight(sanctuary, outside));
    }
    assertTrue(SanctuaryCombatRules.canFight(outside, new Vector2(3 * GRID_W, GRID_H)));
  }

  @Test
  void enteringSanctuaryBeforeImpactCancelsDamageAndLeavingRestoresCombat() throws Exception {
    Player player = playerAt(0, 0);
    player.setCurrentHp(100);
    Runnable pendingProjectile = () -> player.takeCombatDamage(20);
    player.setWorldPosition(GRID_W, GRID_H, 0);
    pendingProjectile.run();
    assertEquals(100, player.getCurrentHp());
    assertEquals(0, player.getLastDamageTaken());
    player.setWorldPosition(0, 0, 0);
    pendingProjectile.run();
    assertEquals(80, player.getCurrentHp());
    assertEquals(20, player.getLastDamageTaken());
  }

  @Test
  void scriptedHealthCostsRemainEffectiveInsideSanctuary() throws Exception {
    Player player = playerAt(GRID_W, GRID_H);
    player.setCurrentHp(100);
    player.takeDamage(20);
    assertEquals(80, player.getCurrentHp());
  }

  @Test
  void enteringSanctuaryAlsoCancelsOutgoingPlayerDamage() throws Exception {
    Player player = playerAt(GRID_W, GRID_H);
    TestMonster monster = new TestMonster();
    monster.applyPlayerDamage(3, player, null);
    assertEquals(10, monster.getHealth());
    player.setWorldPosition(0, 0, 0);
    monster.applyPlayerDamage(3, player, null);
    assertEquals(7, monster.getHealth());
  }

  @Test
  void hostileCastingIsRejectedBeforeManaOrCooldownButSelfSpellsStillWork() throws Exception {
    Player player = playerAt(GRID_W, GRID_H);
    player.setMana(100);
    SpellData spell =
        new SpellData(
            "Barrier", "", "10", 0, 59, 24, 21, false, true, "", "", "", 0, 0, "", "", 0, "120000",
            0, null);
    var blocked =
        SpellCastingService.begin(
            new SpellCastingService.Request(
                spell,
                player,
                SpellCastingService.TargetKind.HOSTILE_UNIT,
                1f,
                true,
                false,
                false));
    assertEquals(SpellCastingService.Failure.SAFE_HAVEN, blocked.failure());
    assertEquals(100, player.getMana());
    assertFalse(player.isSpellOnCooldown(spell.getName()));
    var healing =
        SpellCastingService.begin(
            new SpellCastingService.Request(
                spell, player, SpellCastingService.TargetKind.SELF, 0f, true, false, false));
    assertTrue(healing.success());
    assertEquals(90, player.getMana());
  }

  @Test
  void hostilePositionSpellsIncludingSummonsAreRejectedBeforeActivation() throws Exception {
    for (int targetType : new int[] {6, 16, 19}) {
      Player player = playerAt(GRID_W, GRID_H);
      player.setMana(100);
      SpellData spell = positionSpell(true, targetType);
      assertEquals(
          1,
          new SpellEffectManager().resolvePositionSummons(spell).size(),
          "fixture must exercise a position spell that would create a summon after activation");
      var result =
          SpellCastingService.begin(
              new SpellCastingService.Request(
                  spell, player, SpellCastingService.TargetKind.POSITION, 1f, true, false, false));
      assertFalse(result.success(), "position effects and summons require successful activation");
      assertEquals(SpellCastingService.Failure.SAFE_HAVEN, result.failure());
      assertEquals(0, result.manaSpent());
      assertEquals(100, player.getMana());
      assertFalse(player.isSpellOnCooldown(spell.getName()));
      assertFalse(player.isMentallyExhausted());
      assertFalse(player.isPhysicallyExhausted());
      assertTrue(player.isAttackReady());
      player.setWorldPosition(0, 0, 0);
      assertTrue(
          SpellCastingService.begin(
                  new SpellCastingService.Request(
                      spell,
                      player,
                      SpellCastingService.TargetKind.POSITION,
                      1f,
                      true,
                      false,
                      false))
              .success(),
          "the same attack must remain usable outside sanctuary");
    }
  }

  @Test
  void nonHostilePositionSpellsRemainAvailableInsideSanctuary() throws Exception {
    Player player = playerAt(GRID_W, GRID_H);
    player.setMana(100);
    var result =
        SpellCastingService.begin(
            new SpellCastingService.Request(
                positionSpell(false, 6),
                player,
                SpellCastingService.TargetKind.POSITION,
                1f,
                true,
                false,
                false));
    assertTrue(result.success());
    assertEquals(90, player.getMana());
  }

  private SpellData positionSpell(boolean attack, int targetType) {
    return new SpellData(
        "Position test",
        "",
        "10",
        3,
        0,
        0,
        0,
        attack,
        true,
        "",
        "",
        "",
        0,
        0,
        "",
        "",
        10,
        "0",
        null,
        0,
        null,
        1,
        0,
        targetType,
        0,
        "100",
        "1000",
        "1000",
        "1000",
        0,
        0,
        false,
        attack
            ? List.of(
                new SpellData.T4cEffect(
                    6,
                    List.of(
                        new SpellData.T4cEffect.EffectParam(1, "monster"),
                        new SpellData.T4cEffect.EffectParam(2, "Moonlit Stalker"))))
            : List.of());
  }

  private Player playerAt(float x, float y) throws Exception {
    Player player = new Player();
    player.setMapBounds(4 * GRID_W, 4 * GRID_H);
    player.setWorldPosition(x, y, 0);
    return player;
  }

  private static final class TestMonster extends BaseMonster {
    TestMonster() throws GameException {
      super("Brown Rat", 0f, 0f, 10, 0, 0, 0, 1, 1, 1_000L, null, null, null, null, null);
    }
  }
}
