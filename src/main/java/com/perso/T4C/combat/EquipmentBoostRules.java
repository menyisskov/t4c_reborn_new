package com.perso.T4C.combat;

import com.perso.T4C.helper.DiceFormula;
import java.util.LinkedHashMap;
import java.util.Map;

/** Shared equipment boost evaluation over base stats; active spell buffs never feed themselves. */
public final class EquipmentBoostRules {
  private EquipmentBoostRules() {}

  public record Boost(int id, int stat, String expression, int minWisdom, int minIntelligence) {}
  private record Active(int stat, int amount) {}

  /** Input order determines the winner for a repeated boost ID, matching the offline registry rule. */
  public static Map<Integer, Integer> bonuses(WeaponDamageRules.Stats base, Map<String, Integer> skills,
      Iterable<Boost> equipped) {
    var active = new LinkedHashMap<Integer, Active>();
    for (var boost : equipped) {
      if (base.intelligence() < boost.minIntelligence() || base.wisdom() < boost.minWisdom()) continue;
      int amount = evaluate(base, skills, boost.expression());
      active.put(boost.id(), new Active(boost.stat(), amount));
    }
    var totals = new LinkedHashMap<Integer, Long>();
    active.values().forEach(boost -> totals.merge(boost.stat(), (long) boost.amount(), Long::sum));
    var result = new LinkedHashMap<Integer, Integer>();
    totals.forEach((stat, value) -> result.put(stat, (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, value))));
    return Map.copyOf(result);
  }

  private static int evaluate(WeaponDamageRules.Stats base, Map<String, Integer> skills, String expression) {
    int light = skills.getOrDefault("light", 0);
    String formula = expression == null ? "0" : expression
        .replace("self.true_attack", Integer.toString(skills.getOrDefault("attack", 0)))
        .replace("self.true_dodge", Integer.toString(skills.getOrDefault("dodge", 0)))
        .replace("self.true_light", Integer.toString(light <= 0 ? 100 : light))
        .replaceAll("true_skill\\(35\\)", Integer.toString(skills.getOrDefault("archery", 0)));
    var context = new DiceFormula.Context(base.strength(), base.endurance(), base.dexterity(),
        base.intelligence(), 0, base.wisdom(), 0, base.level());
    return DiceFormula.of(formula).evaluate(context);
  }
}
