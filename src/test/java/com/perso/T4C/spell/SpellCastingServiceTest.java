package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

  // T4C-0060: casting is instant, but the gap it leaves behind is not. A successful cast still
  // banks the spell's exhaustion, which is the only thing pacing one cast against the next now that
  // nothing waits before the spell goes off.
  @Test
  void aSuccessfulCastStillBanksItsExhaustion() throws Exception {
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
    assertFalse(caster.isMentallyExhausted(), "A rested caster should start unexhausted");
    SpellCastingService.Result result =
        SpellCastingService.begin(
            new SpellCastingService.Request(
                spell,
                caster,
                SpellCastingService.TargetKind.HOSTILE_UNIT,
                1f,
                true,
                false,
                false));
    assertTrue(result.success(), () -> "Unexpected cast failure: " + result.failure());
    // "1000 + 250 * self.level" at level 12 is 4 seconds of mental exhaustion, and the physical and
    // attack readings are shorter, so the caster is held by the longest of the three.
    assertTrue(caster.isMentallyExhausted(), "The cast should have banked its mental exhaustion");
    assertTrue(
        caster.isPhysicallyExhausted(), "The cast should have banked its physical exhaustion");
  }
}
