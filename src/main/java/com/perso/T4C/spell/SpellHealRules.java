package com.perso.T4C.spell;

import com.perso.T4C.helper.DiceFormula;
import java.util.List;
import java.util.random.RandomGenerator;

/** Pure healing amount for a spell's own heal formulas, falling back to its min/max damage roll. */
public final class SpellHealRules {
  private SpellHealRules() {}

  /**
   * The first heal formula that evaluates to a non-zero amount wins, in authored order. When none
   * does, the amount is a roll between the spell's min and max damage (or the fixed value if equal).
   */
  public static int healthDelta(
      List<String> healFormulas,
      int minDamage,
      int maxDamage,
      DiceFormula.Context context,
      RandomGenerator random) {
    for (String formula : healFormulas) {
      if (formula == null) continue;
      int delta = DiceFormula.of(formula).evaluate(context);
      if (delta != 0) return delta;
    }
    int low = Math.min(minDamage, maxDamage);
    int high = Math.max(minDamage, maxDamage);
    return low == high ? low : random.nextInt(low, high + 1);
  }
}
