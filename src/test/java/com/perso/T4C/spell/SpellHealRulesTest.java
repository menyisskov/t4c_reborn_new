package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.DiceFormula;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SpellHealRulesTest {
  private static final DiceFormula.Context CONTEXT =
      new DiceFormula.Context(0, 0, 0, 0, 0, 0, 0, 0);

  @Test
  void firstNonZeroFormulaWinsInAuthoredOrder() {
    int delta =
        SpellHealRules.healthDelta(
            List.of("0", "7", "9"), 1, 2, CONTEXT, new Random(1));
    assertEquals(7, delta);
  }

  @Test
  void fallsBackToMinMaxRollWhenNoFormulaHeals() {
    assertEquals(5, SpellHealRules.healthDelta(List.of(), 5, 5, CONTEXT, new Random(1)));
    int rolled = SpellHealRules.healthDelta(List.of("0"), 3, 6, CONTEXT, new Random(1));
    assertTrue(rolled >= 3 && rolled <= 6, "roll within inclusive min..max, got " + rolled);
  }
}
