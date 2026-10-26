package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.definition.Earthquake;
import com.perso.T4C.spell.definition.FlameWave;
import org.junit.jupiter.api.Test;

class SpellAreaTargetingTest {
  @Test
  void selfCenteredSpellsCastOnSelfAndUseTileDistanceOnBothAxes() {
    SpellData earthquake = Earthquake.definition();
    SpellData flameWave = FlameWave.definition();
    float centerX = 320;
    float centerY = 320;

    assertTrue(SpellAreaTargeting.isSelfCenteredAttack(earthquake));
    assertTrue(SpellAreaTargeting.isSelfCenteredAttack(flameWave));
    assertTrue(
        SpellAreaTargeting.contains(
            earthquake, centerX, centerY, centerX + 7 * GameConstants.GRID_W, centerY));
    assertTrue(
        SpellAreaTargeting.contains(
            earthquake, centerX, centerY, centerX, centerY - 7 * GameConstants.GRID_H));
    assertFalse(
        SpellAreaTargeting.contains(
            earthquake, centerX, centerY, centerX, centerY + 8 * GameConstants.GRID_H));
    assertFalse(
        SpellAreaTargeting.contains(
            flameWave, centerX, centerY, centerX + 6 * GameConstants.GRID_W, centerY));

    Player caster = new Player();
    caster.setMana(100);
    SpellCastingService.Result result =
        SpellCastingService.begin(
            new SpellCastingService.Request(
                earthquake, caster, SpellCastingService.TargetKind.SELF, 0f, true, false, false));
    assertTrue(result.success(), () -> "Self cast failed: " + result.failure());
  }

  @Test
  void areaCapFiltersBeforeChoosingNearestAndReservesThePrimarySlot() {
    var candidates = java.util.List.of(20, 9, 8, 7, 6, 5, 4, 3, 2, 1, 1, 0);
    var area = SpellAreaTargeting.nearest(candidates, n -> n > 0 && n < 20, n -> n, 0);
    assertEquals(java.util.List.of(1, 2, 3, 4, 5, 6, 7, 8), area);
    var splash = SpellAreaTargeting.nearest(candidates, n -> n > 0 && n < 20, n -> n, 1);
    assertEquals(java.util.List.of(1, 2, 3, 4, 5, 6, 7), splash);
    // Taking damage may alter the source collection; selected targets must remain a snapshot.
    var live = new java.util.ArrayList<>(candidates);
    var snapshot = SpellAreaTargeting.nearest(live, n -> n > 0, n -> n, 0);
    live.clear();
    assertEquals(area, snapshot);
  }

  @Test
  void residualSplashIncludesDiagonalNeighboursAndDealsDamageOnly() {
    assertTrue(SpellAreaTargeting.isAdjacent(0, 0, GameConstants.GRID_W, GameConstants.GRID_H));
    assertFalse(SpellAreaTargeting.isAdjacent(0, 0, 2 * GameConstants.GRID_W, 0));
    assertFalse(SpellAreaTargeting.isAdjacent(0, 0, 0, 2 * GameConstants.GRID_H));
    assertEquals(25, SpellAreaTargeting.residualDamage(-100));
    assertEquals(1, SpellAreaTargeting.residualDamage(-1));
    assertEquals(0, SpellAreaTargeting.residualDamage(100));
    assertEquals(0, SpellAreaTargeting.residualDamage(0));
    SpellData comet = com.perso.T4C.spell.definition.EmberqueensWrath.definition();
    assertTrue(comet.isAttack());
    assertEquals(0, comet.getRadius());
    assertEquals(11, comet.getTargetType());
    assertEquals(null, SpellVisualResolver.resolve(comet).projectile());
    assertTrue(
        SpellVisualResolver.hasUnifiedAreaImpact(
            com.perso.T4C.spell.definition.DrowningDeep.definition()));
  }
}
