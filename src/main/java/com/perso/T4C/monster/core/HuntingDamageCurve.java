package com.perso.T4C.monster.core;

import com.perso.T4C.content.ForkContent;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.spawn.HuntingGrounds;
import java.util.ArrayList;

/** Raw attack budget for new regular monsters; does not scale with a player's aura. */
public final class HuntingDamageCurve {
  private static final double MAXIMUM_DAMAGE_PER_LEVEL = 2.5;

  private HuntingDamageCurve() {}

  public static int maximumRawDamage(int level) {
    return (int)
        Math.min(Integer.MAX_VALUE, Math.max(1, Math.round(level * MAXIMUM_DAMAGE_PER_LEVEL)));
  }

  public static boolean appliesTo(MonsterDef definition) {
    return definition != null
        && definition.isCanAttack()
        && !ForkContent.BOSS_MONSTER_NAMES.contains(definition.getName())
        && (ForkContent.NEW_MONSTER_NAMES.contains(definition.getName())
            || MonsterRegistry.isAdditionalDefinition(definition.getName())
            || HuntingGrounds.isHuntingType(definition.getName()));
  }

  public static MonsterDef apply(MonsterDef definition) {
    if (!appliesTo(definition)) {
      return definition;
    }
    int cap = maximumRawDamage(definition.getLevel());
    if (definition.getAttacks() == null || definition.getAttacks().isEmpty()) {
      int high = definition.getHitDamageMax();
      return high <= cap
          ? definition
          : definition
              .withHitDamageMin(
                  Math.max(1, (int) ((long) definition.getHitDamageMin() * cap / high)))
              .withHitDamageMax(cap);
    }
    var context =
        new DiceFormula.Context(
            definition.getStr(),
            definition.getEnd(),
            definition.getAgi(),
            definition.getIntel(),
            definition.getWill(),
            definition.getWis(),
            definition.getLuck(),
            definition.getLevel());
    var attacks = new ArrayList<MonsterDef.Attack>();
    boolean changed = false;
    int minimum = Integer.MAX_VALUE;
    int maximum = 0;
    boolean simpleMelee = true;
    for (var attack : definition.getAttacks()) {
      if (attack.isSpell()) {
        attacks.add(attack);
        continue;
      }
      // Preserve special formulas and spell IDs. The runtime cap also covers these
      // paths without rewriting mechanics or accidentally enabling an unsupported formula.
      if (attack.getName() == null || !attack.getName().matches("(?i)[0-9]+d[0-9]+([+-][0-9]+)?")) {
        attacks.add(attack);
        simpleMelee = false;
        continue;
      }
      var formula = DiceFormula.of(attack.getName());
      int low = formula.min(context);
      int high = formula.max(context);
      if (high > cap) {
        low = Math.max(1, (int) ((long) low * cap / high));
        high = cap;
        attack =
            new MonsterDef.Attack(
                "1d" + (high - low + 1) + "+" + (low - 1),
                attack.getValue1(),
                attack.getValue2(),
                attack.getValue3(),
                attack.getValue4(),
                attack.getValue5());
        changed = true;
      }
      attacks.add(attack);
      minimum = Math.min(minimum, low);
      maximum = Math.max(maximum, high);
    }
    if (!changed) return definition;
    var result = definition.withAttacks(java.util.List.copyOf(attacks));
    return simpleMelee ? result.withHitDamageMin(minimum).withHitDamageMax(maximum) : result;
  }
}
