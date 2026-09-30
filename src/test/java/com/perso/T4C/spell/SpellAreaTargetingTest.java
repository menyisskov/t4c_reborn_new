package com.perso.T4C.spell;

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
}
