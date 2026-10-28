package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.spell.SpellFormulaContexts.Caster;
import com.perso.T4C.spell.SpellFormulaContexts.Elements;
import com.perso.T4C.spell.SpellFormulaContexts.Stats;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Pins the three shared builders to the exact context constructions single-player used before they
 * were extracted (copied here verbatim as the reference), for casters with every input distinct.
 */
class SpellFormulaContextsTest {
  private static Caster caster(int shift, boolean buffed) {
    var trueStats = new Stats(40 + shift, 41 + shift, 42 + shift, 43 + shift, 44 + shift);
    var effective = buffed ? new Stats(50 + shift, 51 + shift, 52 + shift, 53 + shift, 54 + shift) : trueStats;
    var truePower = new Elements(110 + shift, 120 + shift, 130 + shift, 140 + shift, 150 + shift, 160 + shift);
    var power = buffed ? new Elements(210 + shift, 220 + shift, 230 + shift, 240 + shift, 250 + shift, 260 + shift) : truePower;
    return new Caster(7 + shift, effective, trueStats, 61 + shift, 62 + shift, 63 + shift, power, truePower,
        new Elements(301, 302, 303, 304, 5000 + shift, 306), Map.of(2001, 1, 2002, 2));
  }

  /** The cast-rule context exactly as {@code SpellCastingService.context} built it. */
  private static DiceFormula.Context legacyCastRules(Caster c) {
    return new DiceFormula.Context(c.effective().strength(), c.effective().endurance(),
        c.effective().dexterity(), c.effective().intelligence(), 0, c.effective().wisdom(), 0, c.level())
        .withSelfTrue(legacySelfTrue(c)).withEquippedItems(c.equippedItems());
  }

  /** The caster-side context exactly as {@code SpellEffectManager.casterContext} built it. */
  private static DiceFormula.Context legacySelf(Caster c) {
    var base = legacyCastRules(c);
    return new DiceFormula.Context(base.str, base.end, base.agi, base.intel, base.wil, base.wis, base.luck,
        base.level, 0, 100, 100, 100, 100, 100, 100,
        c.power().fire(), c.power().earth(), c.power().air(), c.power().water(), c.power().light(), c.power().dark())
        .withSelfTrue(base.selfTrue).withEquippedItems(base.equippedItems);
  }

  /** The target context exactly as {@code SpellEffectManager.evaluate} built it. */
  private static DiceFormula.Context legacyAgainst(Caster c, int fire, int earth, int air, int water, int light, int dark) {
    return new DiceFormula.Context(c.effective().strength(), c.effective().endurance(),
        c.effective().dexterity(), c.effective().intelligence(), 0, c.effective().wisdom(), 0, c.level(), 0,
        fire, earth, air, water, light, dark,
        c.power().fire(), c.power().earth(), c.power().air(), c.power().water(), c.power().light(), c.power().dark())
        .withSelfTrue(legacySelfTrue(c)).withEquippedItems(c.equippedItems());
  }

  private static Map<String, Integer> legacySelfTrue(Caster c) {
    Map<String, Integer> readings = new LinkedHashMap<>();
    readings.put("str", c.trueStats().strength());
    readings.put("end", c.trueStats().endurance());
    readings.put("agi", c.trueStats().dexterity());
    readings.put("int", c.trueStats().intelligence());
    readings.put("wis", c.trueStats().wisdom());
    readings.put("level", c.level());
    readings.put("attack", c.trueAttack());
    readings.put("archery", c.trueArchery());
    readings.put("dodge", c.trueDodge());
    for (String element : java.util.List.of("fire", "earth", "air", "water", "light", "dark")) {
      readings.put(element, c.truePower().of(element));
      readings.put("r_" + element, c.trueResistance().of(element));
    }
    return readings;
  }

  @Test
  void buildersReproduceTheSinglePlayerContextsFieldForField() {
    for (int shift : new int[] {0, 9, 250}) {
      for (boolean buffed : new boolean[] {false, true}) {
        var c = caster(shift, buffed);
        assertEquals(SpellFormulaContexts.describe(legacyCastRules(c)),
            SpellFormulaContexts.describe(SpellFormulaContexts.forCastRules(c)));
        assertEquals(SpellFormulaContexts.describe(legacySelf(c)),
            SpellFormulaContexts.describe(SpellFormulaContexts.forSelf(c)));
        assertEquals(SpellFormulaContexts.describe(legacyAgainst(c, 100, 250, 7, 100, 100, 3)),
            SpellFormulaContexts.describe(SpellFormulaContexts.against(c, new Elements(100, 250, 7, 100, 100, 3))));
      }
    }
  }

  @Test
  void castRulesStayNeutralOnPowersButBuffsReachEffectiveStatsOnly() {
    var c = caster(0, true);
    var rules = SpellFormulaContexts.forCastRules(c);
    assertEquals(100, rules.selfLight, "cast-rule formulas never see element power");
    assertEquals(c.effective().wisdom(), rules.wis);
    assertEquals(c.trueStats().wisdom(), rules.selfTrue.get("wis"), "self.true_wis ignores buffs");
    var effects = SpellFormulaContexts.forSelf(c);
    assertEquals(c.power().light(), effects.selfLight);
    assertEquals(c.truePower().light(), effects.selfTrue.get("light"));
  }

  @Test
  void trueReadingsDriveSelfTrueVariablesInFormulas() {
    var c = caster(0, true);
    var ctx = SpellFormulaContexts.forSelf(c);
    assertEquals(c.trueStats().wisdom() + c.effective().wisdom(),
        DiceFormula.of("self.true_wis+self.wis").evaluate(ctx));
    assertEquals(c.truePower().fire() + c.trueResistance().earth(),
        DiceFormula.of("self.true_fire+self.true_r_earth").evaluate(ctx));
    assertEquals(2, DiceFormula.of("equipcount(2002)").evaluate(ctx));
  }

  @Test
  void elementRulesComposeTheSameTermsPlayerAlwaysDid() {
    assertEquals(100, ElementRules.power(0, 0, 0, 0));
    assertEquals(100, ElementRules.power(-5, 0, 0, 0));
    assertEquals(180 + 10 + 7 + 3, ElementRules.power(180, 10, 7, 3));
    assertEquals(5000 + 1, ElementRules.resistance("Light", 0, 0, 1));
    assertEquals(100 + 4, ElementRules.resistance("fire", 1, 2, 1));
    assertEquals(17, ElementRules.powerStatId("FIRE"));
    assertEquals(13, ElementRules.resistanceStatId("fire"));
    assertEquals(Integer.MIN_VALUE, ElementRules.powerStatId(null));
    assertEquals(100, ElementRules.targetResistance(0));
    assertEquals(100, ElementRules.targetResistance(-3));
    assertEquals(250, ElementRules.targetResistance(250));
  }
}
