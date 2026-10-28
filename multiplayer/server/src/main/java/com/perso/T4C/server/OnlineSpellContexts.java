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
 * quest bonus and gear, and the equipped item counts. Active spell buffs are not integrated online
 * yet (casting fails closed while any is active), so effective and true readings are equal here.
 */
final class OnlineSpellContexts {
  private OnlineSpellContexts() {}

  static SpellFormulaContexts.Caster caster(CharacterProgress progress, ItemContent items) {
    var bonuses = OnlineEquipmentBonuses.resolve(progress, items);
    var base = progress.attributes();
    var stats = new SpellFormulaContexts.Stats(
        base.strength() + bonuses.getOrDefault(3, 0),
        base.endurance() + bonuses.getOrDefault(2, 0),
        base.dexterity() + bonuses.getOrDefault(6, 0),
        base.intelligence() + bonuses.getOrDefault(1, 0),
        base.wisdom() + bonuses.getOrDefault(4, 0));
    var power = new int[ElementRules.ELEMENTS.size()];
    var resistance = new int[ElementRules.ELEMENTS.size()];
    for (int i = 0; i < power.length; i++) {
      String element = ElementRules.ELEMENTS.get(i);
      power[i] = ElementRules.power(progress.skills().getOrDefault(element, 0), 0,
          progress.questFlags().getOrDefault("legacy:power:" + element, 0),
          bonuses.getOrDefault(ElementRules.powerStatId(element), 0));
      resistance[i] = ElementRules.resistance(element, 0,
          progress.questFlags().getOrDefault("legacy:resist:" + element, 0),
          bonuses.getOrDefault(ElementRules.resistanceStatId(element), 0));
    }
    var powers = new SpellFormulaContexts.Elements(power[0], power[1], power[2], power[3], power[4], power[5]);
    return new SpellFormulaContexts.Caster(
        progress.advancement().level(), stats, stats,
        skill(progress, bonuses, "attack"), skill(progress, bonuses, "archery"), skill(progress, bonuses, "dodge"),
        powers, powers,
        new SpellFormulaContexts.Elements(resistance[0], resistance[1], resistance[2], resistance[3],
            resistance[4], resistance[5]),
        equippedItems(progress, items));
  }

  /** Cast-rule formulas (mana cost, exhaustion, success): the context offline uses for the same. */
  static DiceFormula.Context forCastRules(CharacterProgress progress, ItemContent items) {
    return SpellFormulaContexts.forCastRules(caster(progress, items));
  }

  /** Caster-side effect formulas such as self heals. */
  static DiceFormula.Context forSelf(CharacterProgress progress, ItemContent items) {
    return SpellFormulaContexts.forSelf(caster(progress, items));
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
