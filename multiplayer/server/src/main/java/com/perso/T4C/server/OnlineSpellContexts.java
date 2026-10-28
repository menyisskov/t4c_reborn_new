package com.perso.T4C.server;

import com.perso.T4C.combat.SkillBonusRules;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.spell.ElementRules;
import com.perso.T4C.spell.SpellFormulaContexts;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds the shared spell formula inputs from committed online state, with the same terms
 * single-player reads from a live {@code Player}: base attributes plus equipment bonuses, trained
 * skills plus gear for attack/archery/dodge, element powers and resistances from skill, legacy
 * quest bonus and gear, and the equipped item counts. The <em>effective</em> readings also include
 * the spell buffs running at {@code now}; the <em>true</em> readings (what {@code self.true_*}
 * formulas read) never do, so recasting a buff refreshes it instead of compounding, as offline.
 */
final class OnlineSpellContexts {
  private OnlineSpellContexts() {}

  /** The caster with no spell buffs counted: equal effective and true readings. */
  static SpellFormulaContexts.Caster caster(CharacterProgress progress, ItemContent items) {
    return caster(progress, items, Map.of());
  }

  /** The caster with the buffs running at {@code now} counted in its effective readings. */
  static SpellFormulaContexts.Caster caster(CharacterProgress progress, ItemContent items, long now) {
    OnlineBuffs.requireModelled(progress, now);
    return caster(progress, items, OnlineBuffs.totals(progress, now));
  }

  private static SpellFormulaContexts.Caster caster(CharacterProgress progress, ItemContent items, Map<String, Integer> buff) {
    var bonuses = OnlineEquipmentBonuses.resolve(progress, items);
    var base = progress.attributes();
    var trueStats = new SpellFormulaContexts.Stats(
        base.strength() + bonuses.getOrDefault(3, 0),
        base.endurance() + bonuses.getOrDefault(2, 0),
        base.dexterity() + bonuses.getOrDefault(6, 0),
        base.intelligence() + bonuses.getOrDefault(1, 0),
        base.wisdom() + bonuses.getOrDefault(4, 0));
    var stats = new SpellFormulaContexts.Stats(
        trueStats.strength() + buff.getOrDefault("str", 0),
        trueStats.endurance() + buff.getOrDefault("end", 0),
        trueStats.dexterity() + buff.getOrDefault("dex", 0),
        trueStats.intelligence() + buff.getOrDefault("int", 0),
        trueStats.wisdom() + buff.getOrDefault("wis", 0));
    var power = new int[ElementRules.ELEMENTS.size()];
    var truePower = new int[power.length];
    var resistance = new int[power.length];
    for (int i = 0; i < power.length; i++) {
      String element = ElementRules.ELEMENTS.get(i);
      truePower[i] = ElementRules.power(progress.skills().getOrDefault(element, 0), 0,
          progress.questFlags().getOrDefault("legacy:power:" + element, 0),
          bonuses.getOrDefault(ElementRules.powerStatId(element), 0));
      power[i] = truePower[i] + buff.getOrDefault("power:" + element, 0);
      resistance[i] = ElementRules.resistance(element, 0,
          progress.questFlags().getOrDefault("legacy:resist:" + element, 0),
          bonuses.getOrDefault(ElementRules.resistanceStatId(element), 0));
    }
    return new SpellFormulaContexts.Caster(
        progress.advancement().level(), stats, trueStats,
        skill(progress, bonuses, "attack"), skill(progress, bonuses, "archery"), skill(progress, bonuses, "dodge"),
        elements(power), elements(truePower), elements(resistance),
        equippedItems(progress, items));
  }

  /**
   * Element resistances including the buffs running at {@code now} (what {@code Player.getElementResistance}
   * reads); the spell formulas only ever read the true ones in {@link SpellFormulaContexts.Caster}.
   */
  static SpellFormulaContexts.Elements effectiveResistance(CharacterProgress progress, ItemContent items, long now) {
    OnlineBuffs.requireModelled(progress, now);
    var bonuses = OnlineEquipmentBonuses.resolve(progress, items);
    var buff = OnlineBuffs.totals(progress, now);
    var values = new int[ElementRules.ELEMENTS.size()];
    for (int i = 0; i < values.length; i++) {
      String element = ElementRules.ELEMENTS.get(i);
      values[i] = ElementRules.resistance(element, buff.getOrDefault("resist:" + element, 0),
          progress.questFlags().getOrDefault("legacy:resist:" + element, 0),
          bonuses.getOrDefault(ElementRules.resistanceStatId(element), 0));
    }
    return elements(values);
  }

  private static SpellFormulaContexts.Elements elements(int[] values) {
    return new SpellFormulaContexts.Elements(values[0], values[1], values[2], values[3], values[4], values[5]);
  }

  /** Cast-rule formulas (mana cost, exhaustion, success): the context offline uses for the same. */
  static DiceFormula.Context forCastRules(CharacterProgress progress, ItemContent items) {
    return SpellFormulaContexts.forCastRules(caster(progress, items));
  }

  static DiceFormula.Context forCastRules(CharacterProgress progress, ItemContent items, long now) {
    return SpellFormulaContexts.forCastRules(caster(progress, items, now));
  }

  /** Caster-side effect formulas such as self heals. */
  static DiceFormula.Context forSelf(CharacterProgress progress, ItemContent items) {
    return SpellFormulaContexts.forSelf(caster(progress, items));
  }

  static DiceFormula.Context forSelf(CharacterProgress progress, ItemContent items, long now) {
    return SpellFormulaContexts.forSelf(caster(progress, items, now));
  }

  private static int skill(CharacterProgress progress, Map<Integer, Integer> bonuses, String name) {
    return SkillBonusRules.effective(name, progress.skills().getOrDefault(name, 0), 0,
        stat -> bonuses.getOrDefault(stat, 0));
  }

  /**
   * Equipped items by numeric id. Single-player stores a two-slot item under both slots and counts
   * each, so a mirrored item counts twice here too.
   */
  static Map<Integer, Integer> equippedItems(CharacterProgress progress, ItemContent items) {
    var counts = new HashMap<Integer, Integer>();
    OnlineEquipmentBonuses.equipment(progress, items).values().forEach(item -> {
      if (item.numId() <= 0) return;
      counts.merge(item.numId(), 1, Integer::sum);
      if (item.secondaryBodyPart() != null) counts.merge(item.numId(), 1, Integer::sum);
    });
    return counts;
  }
}
