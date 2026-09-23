package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.spell.definition.SpellDefinitions;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * T4C-0025: every school (fire, earth, air, water, light, dark) has exactly one attack spell at
 * each high tier (150-400), with the same requirements and the same damage at those requirements
 * for the same shape, and no spell asks for a level or stat a character can't actually have.
 * Reference damage context matches CompendiumExporter#damageAtReference: the caster at exactly
 * the spell's own min stat/level requirements, untrained (100) elemental power, neutral (100)
 * target resistance.
 */
class HighTierSpellLadderTest {
  private static final int[] ELEMENTS = {1, 2, 3, 4, 5, 6};

  @Test
  void everySchoolHasExactlyOneAttackSpellAtEveryTier() {
    for (int tier : HighTierSpellCurve.TIERS) {
      for (int element : ELEMENTS) {
        List<String> found = new ArrayList<>();
        for (SpellData spell : highTierAttacks())
          if (spell.getMinLevel() == tier && spell.getElement() == element)
            found.add(spell.getName());
        assertEquals(
            1, found.size(), "element " + element + " at level " + tier + " has " + found);
      }
    }
  }

  @Test
  void spellsOfTheSameTierAndShapeDealTheSameDamageAtTheirRequirements() {
    for (int tier : HighTierSpellCurve.TIERS) {
      for (boolean area : new boolean[] {false, true}) {
        Double reference = null;
        for (SpellData spell : highTierAttacks()) {
          if (spell.getMinLevel() != tier || (spell.getRadius() > 0) != area) continue;
          double damage = averageDamageAtRequirements(spell);
          if (reference == null) reference = damage;
          assertEquals(
              reference, damage, reference * 0.01, spell.getName() + " at level " + tier);
        }
      }
    }
  }

  @Test
  void damageAndRequirementsClimbWithEveryTier() {
    for (int element : ELEMENTS) {
      SpellData previous = null;
      for (int tier : HighTierSpellCurve.TIERS) {
        SpellData spell = attackAt(element, tier);
        if (previous != null) {
          assertTrue(
              spell.getMinInt() + spell.getMinWis() > previous.getMinInt() + previous.getMinWis(),
              spell.getName() + " must ask for more than " + previous.getName());
          double perShape = spell.getRadius() > 0 ? 5 : 6;
          double previousPerShape = previous.getRadius() > 0 ? 5 : 6;
          assertTrue(
              averageDamageAtRequirements(spell) / perShape
                  > averageDamageAtRequirements(previous) / previousPerShape,
              spell.getName() + " must hit harder per cast than " + previous.getName());
        }
        previous = spell;
      }
    }
  }

  @Test
  void noPlayerSpellIsOutOfReachAtItsOwnLevel() {
    for (SpellData spell : SpellDefinitions.all()) {
      if (!isPlayerSpell(spell)) continue;
      assertTrue(
          spell.getMinLevel() <= GameConstants.MAX_PLAYER_LEVEL,
          spell.getName() + " needs level " + spell.getMinLevel() + " above the level cap");
      if (spell.getMinLevel() < 150) continue;
      // A never-reborn character at the spell's own level: base attributes plus 5 stat points
      // per level gained. The spell may use at most two thirds of those points.
      int base = GameConstants.REBIRTH_BASE_ATTRIBUTE;
      int earned = 5 * (spell.getMinLevel() - 1);
      int needed = Math.max(0, spell.getMinInt() - base) + Math.max(0, spell.getMinWis() - base);
      assertTrue(
          needed <= earned * 2 / 3,
          spell.getName() + " needs " + needed + " of the " + earned + " points earned by level "
              + spell.getMinLevel());
    }
  }

  @Test
  void levelCapRequirementsMatchAWellBuiltCaster() {
    assertEquals(1000, HighTierSpellCurve.primaryRequirement(GameConstants.MAX_PLAYER_LEVEL));
  }

  private static SpellData attackAt(int element, int tier) {
    for (SpellData spell : highTierAttacks())
      if (spell.getMinLevel() == tier && spell.getElement() == element) return spell;
    throw new AssertionError("No attack spell for element " + element + " at level " + tier);
  }

  private static List<SpellData> highTierAttacks() {
    List<SpellData> out = new ArrayList<>();
    for (SpellData spell : SpellDefinitions.all()) {
      if (!spell.isAttack() || spell.getMinLevel() < HighTierSpellCurve.TIERS.get(0)) continue;
      if (!isPlayerSpell(spell)) continue;
      if (damageFormula(spell) != null) out.add(spell);
    }
    return out;
  }

  /** Same naming rule SpellRegistry#isPlayerCastable uses to hide item/monster/NPC spells. */
  private static boolean isPlayerSpell(SpellData spell) {
    String name = spell.getName();
    if (name == null || !name.startsWith("${spell.") || name.endsWith("_effect}")) return false;
    for (String prefix : List.of("item_", "mob_", "test_", "npc_"))
      if (name.startsWith("${spell." + prefix)) return false;
    return true;
  }

  private static String damageFormula(SpellData spell) {
    for (SpellData.T4cEffect effect : spell.getT4cEffects())
      if (effect.getEffectType() == 1 && effect.getParameters() != null
          && !effect.getParameters().isEmpty())
        return effect.getParameters().get(0).getExpression();
    return null;
  }

  private static double averageDamageAtRequirements(SpellData spell) {
    String formula = damageFormula(spell);
    String magnitude = formula.startsWith("-") ? formula.substring(1) : formula;
    DiceFormula.Context ctx =
        new DiceFormula.Context(
            0, 0, 0, spell.getMinInt(), 0, spell.getMinWis(), 0, spell.getMinLevel(),
            0, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100);
    return (DiceFormula.of(magnitude).min(ctx) + DiceFormula.of(magnitude).max(ctx)) / 2.0;
  }
}
