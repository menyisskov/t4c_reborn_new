package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.DiceFormula;
import java.util.function.IntUnaryOperator;
import org.junit.jupiter.api.Test;

class SpellCastRulesTest {
  private static final DiceFormula.Context CONTEXT =
      new DiceFormula.Context(0, 0, 0, 0, 0, 0, 0, 0);
  private static final IntUnaryOperator NO_ROLL =
      bound -> {
        throw new AssertionError("success roll must not be drawn");
      };

  /** A self heal (target type 3) with the given mana and success-rate formulas. */
  private static SpellCastRules.Spell heal(String mana, String successRate) {
    return new SpellCastRules.Spell(
        3, false, false, false, false, mana, successRate, "1000", "750", "750", 0);
  }

  /** An attack spell with no target restriction, for hostile-target cases. */
  private static SpellCastRules.Spell attack(String mana) {
    return new SpellCastRules.Spell(0, true, false, false, false, mana, "100", "0", "0", "0", 0);
  }

  private record Caster(
      boolean safeFromCombat,
      boolean safeFromPvp,
      boolean onCooldown,
      boolean exhausted,
      boolean unlimited,
      int mana,
      IntUnaryOperator roll) {}

  private static Caster calm(int mana, IntUnaryOperator roll) {
    return new Caster(false, false, false, false, false, mana, roll);
  }

  private static SpellCastRules.CastInput cast(
      SpellCastRules.Spell spell,
      SpellCastRules.TargetKind target,
      float distance,
      boolean pvpTarget,
      Caster caster) {
    return new SpellCastRules.CastInput(
        spell,
        false,
        false,
        target,
        pvpTarget,
        distance,
        true,
        caster.safeFromCombat(),
        caster.safeFromPvp(),
        caster.onCooldown(),
        caster.exhausted(),
        caster.unlimited(),
        caster.mana(),
        CONTEXT,
        caster.roll());
  }

  private static SpellCastRules.CastInput self(SpellCastRules.Spell spell, Caster caster) {
    return cast(spell, SpellCastRules.TargetKind.SELF, 0f, false, caster);
  }

  @Test
  void sanctuaryIsCheckedBeforeEveryOtherFailure() {
    var inSanctuary =
        new SpellCastRules.CastInput(
            heal("2", "100"),
            true,
            false,
            SpellCastRules.TargetKind.HOSTILE_UNIT,
            false,
            0f,
            true,
            true,
            false,
            true,
            true,
            false,
            10,
            CONTEXT,
            NO_ROLL);
    assertEquals(
        SpellCastRules.Verdict.SAFE_HAVEN, SpellCastRules.evaluate(inSanctuary).verdict());
  }

  @Test
  void cooldownAndExhaustionBlockTheCast() {
    var onCooldown = new Caster(false, false, true, false, false, 10, NO_ROLL);
    assertEquals(
        SpellCastRules.Verdict.COOLDOWN, SpellCastRules.evaluate(self(heal("2", "100"), onCooldown)).verdict());
    var exhausted = new Caster(false, false, false, true, false, 10, NO_ROLL);
    assertEquals(
        SpellCastRules.Verdict.EXHAUSTED, SpellCastRules.evaluate(self(heal("2", "100"), exhausted)).verdict());
  }

  @Test
  void expiryIsStillRunningOnlyBeforeItsInstant() {
    assertTrue(SpellCastRules.isActive(1000L, 999L));
    assertFalse(SpellCastRules.isActive(1000L, 1000L));
    assertFalse(SpellCastRules.isActive(1000L, 1001L));
  }

  @Test
  void manaExactlyEqualToCostIsAllowedAndOneLessIsRefused() {
    assertTrue(SpellCastRules.evaluate(self(heal("2", "100"), calm(2, bound -> 0))).success());
    assertEquals(
        SpellCastRules.Verdict.NOT_ENOUGH_MANA,
        SpellCastRules.evaluate(self(heal("2", "100"), calm(1, bound -> 0))).verdict());
  }

  @Test
  void unlimitedResourcesSpendNothing() {
    var unlimited = new Caster(false, false, false, false, true, 0, bound -> 0);
    var outcome = SpellCastRules.evaluate(self(heal("2", "100"), unlimited));
    assertTrue(outcome.success());
    assertEquals(0, outcome.manaCost());
  }

  @Test
  void successRollAcceptsTheRateEdgeAndRefusesOneAbove() {
    IntUnaryOperator rollsFifty =
        bound -> {
          assertEquals(101, bound);
          return 50;
        };
    assertTrue(SpellCastRules.evaluate(self(heal("2", "50"), calm(10, rollsFifty))).success());
    assertEquals(
        SpellCastRules.Verdict.ACTIVATION_FAILED,
        SpellCastRules.evaluate(self(heal("2", "50"), calm(10, bound -> 51))).verdict());
  }

  @Test
  void zeroSuccessRateNeverDrawsARoll() {
    assertEquals(
        SpellCastRules.Verdict.ACTIVATION_FAILED,
        SpellCastRules.evaluate(self(heal("2", "0"), calm(10, NO_ROLL))).verdict());
  }

  @Test
  void successReturnsExhaustionAndCooldownToSpend() {
    var spell =
        new SpellCastRules.Spell(3, false, false, false, false, "2", null, "1000", "750", "0", 5);
    var outcome = SpellCastRules.evaluate(self(spell, calm(10, bound -> 0)));
    assertTrue(outcome.success());
    assertEquals(2, outcome.manaCost());
    assertEquals(1000L, outcome.mentalMillis());
    assertEquals(750L, outcome.physicalMillis());
    assertEquals(0L, outcome.attackMillis());
    assertEquals(5, outcome.cooldownSeconds());
  }

  @Test
  void rangeEdgeIsAllowedAndBeyondIsRefused() {
    var caster = calm(10, bound -> 0);
    assertTrue(
        SpellCastRules.evaluate(
                cast(attack("2"), SpellCastRules.TargetKind.HOSTILE_UNIT, 20f, false, caster))
            .success());
    assertEquals(
        SpellCastRules.Verdict.OUT_OF_RANGE,
        SpellCastRules.evaluate(
                cast(attack("2"), SpellCastRules.TargetKind.HOSTILE_UNIT, 20.5f, false, caster))
            .verdict());
  }
}
