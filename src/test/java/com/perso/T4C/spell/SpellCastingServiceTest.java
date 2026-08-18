package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class SpellCastingServiceTest {
  @Test
  void castingDoesNotRecheckLearningRequirements() throws Exception {
    Player caster = new Player();
    caster.setMana(100);
    SpellData barrier =
        new SpellData(
            "Barrier", "", "10", 0, 59, 24, 21, false, true, "", "", "", 0, 0, "", "", 0, "120000",
            0, null);
    SpellCastingService.Result result =
        SpellCastingService.begin(
            new SpellCastingService.Request(
                barrier, caster, SpellCastingService.TargetKind.SELF, 0f, true, false, false));
    assertTrue(result.success(), () -> "Unexpected cast failure: " + result.failure());
  }

  @Test
  void evaluatesLongestCastExhaustionInMilliseconds() throws Exception {
    Player caster = new Player();
    caster.setLevel(12);
    SpellData spell =
        new SpellData(
            "Burn",
            "",
            "0",
            0,
            0,
            0,
            0,
            true,
            true,
            "",
            "",
            "",
            0,
            0,
            "",
            "",
            0,
            "0",
            null,
            0,
            null,
            1,
            0,
            2,
            2,
            "100",
            "1000 + 250 * self.level",
            "1500",
            "2750",
            0,
            0,
            false,
            null);
    assertEquals(4000L, SpellCastingService.evaluateCastDurationMillis(spell, caster));
  }
}
