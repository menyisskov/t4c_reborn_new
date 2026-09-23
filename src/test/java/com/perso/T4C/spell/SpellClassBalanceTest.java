package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.spell.definition.Cinderburst;
import com.perso.T4C.spell.definition.Gravebreaker;
import com.perso.T4C.spell.definition.Sunscour;
import com.perso.T4C.spell.definition.VoidreaveLance;
import org.junit.jupiter.api.Test;

/** T4C-0021: the fork's two WIS-scaling attack spells (Sunscour, Gravebreaker) were tuned to
 * land close to the INT-scaling attack spells' damage-per-mana curve at a comparable level -
 * INT keeps a slight edge by design, but the gap must stay slight (not the ~5-10x gap these two
 * originally shipped with). Reference context matches CompendiumExporter#damageAtReference: the
 * caster at exactly the spell's own min stat/level requirements, untrained (100) elemental
 * skill, neutral (100) target resistance. */
class SpellClassBalanceTest {
  @Test
  void sunscourLandsBetweenItsIntNeighborsOnTheDamagePerManaCurve() {
    double cinderburstDmgPerMana = attackDamagePerMana(Cinderburst.definition()); // level 68
    double sunscourDmgPerMana = attackDamagePerMana(Sunscour.definition()); // level 120, WIS
    double voidreaveDmgPerMana = attackDamagePerMana(VoidreaveLance.definition()); // level 320

    assertTrue(
        sunscourDmgPerMana > cinderburstDmgPerMana,
        "Sunscour (level 120) must out-damage-per-mana Cinderburst (level 68), not just match "
            + "it: cinderburst=" + cinderburstDmgPerMana + " sunscour=" + sunscourDmgPerMana);
    assertTrue(
        sunscourDmgPerMana < voidreaveDmgPerMana,
        "Sunscour (level 120) must still fall short of VoidreaveLance (level 320): "
            + "sunscour=" + sunscourDmgPerMana + " voidreave=" + voidreaveDmgPerMana);
  }

  @Test
  void gravebreakerLandsBetweenItsIntNeighborsOnTheDamagePerManaCurve() {
    double cinderburstDmgPerMana = attackDamagePerMana(Cinderburst.definition()); // level 68
    double gravebreakerDmgPerMana = attackDamagePerMana(Gravebreaker.definition()); // level 260, WIS
    double voidreaveDmgPerMana = attackDamagePerMana(VoidreaveLance.definition()); // level 320

    assertTrue(
        gravebreakerDmgPerMana > cinderburstDmgPerMana * 2,
        "Gravebreaker (level 260) must clearly out-damage-per-mana Cinderburst (level 68), not "
            + "sit within a hair of it: cinderburst=" + cinderburstDmgPerMana
            + " gravebreaker=" + gravebreakerDmgPerMana);
    assertTrue(
        gravebreakerDmgPerMana < voidreaveDmgPerMana,
        "Gravebreaker (level 260) must still fall short of VoidreaveLance (level 320): "
            + "gravebreaker=" + gravebreakerDmgPerMana + " voidreave=" + voidreaveDmgPerMana);
  }

  /** Average of the spell's primary damage-effect min/max, evaluated at this spell's own
   * min stat/level requirements with an untrained elemental skill and neutral target
   * resistance - see CompendiumExporter#damageAtReference for the same reference point. */
  private static double attackDamagePerMana(SpellData spell) {
    String formula = null;
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect.getEffectType() == 1 && effect.getParameters() != null
          && !effect.getParameters().isEmpty()) {
        formula = effect.getParameters().get(0).getExpression();
        break;
      }
    }
    if (formula == null) throw new IllegalStateException("No damage effect on " + spell.getName());
    String magnitude = formula.startsWith("-") ? formula.substring(1) : formula;
    DiceFormula.Context ctx =
        new DiceFormula.Context(
            0, 0, 0, spell.getMinInt(), 0, spell.getMinWis(), 0, spell.getMinLevel(),
            0, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100);
    int min = DiceFormula.of(magnitude).min(ctx);
    int max = DiceFormula.of(magnitude).max(ctx);
    return (min + max) / 2.0 / Integer.parseInt(spell.getManaCost());
  }
}
