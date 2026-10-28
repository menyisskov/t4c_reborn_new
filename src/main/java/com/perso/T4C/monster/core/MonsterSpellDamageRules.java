package com.perso.T4C.monster.core;

import java.util.Random;

/** Raw damage rolls shared by offline monsters and authoritative online retaliation. */
public final class MonsterSpellDamageRules {
  private MonsterSpellDamageRules() {}

  /** Keeps the legacy monster-only roll formulas; spell balancing is applied by the caller. */
  public static int rollDamage(int spellId, int intelligence, int level, Random random) {
    int scale = Math.max(1, Math.max(15, intelligence) - 14);
    return switch (spellId) {
      case 10091 -> scale * 5 / 6 + 1 + random.nextInt(Math.max(1, scale * 5 / 6));
      case 10120 -> scale * 15 / 32 + 1 + random.nextInt(Math.max(1, scale * 15 / 16));
      default -> Math.max(1, level + random.nextInt(Math.max(1, level)));
    };
  }

  /** Elemental monster damage scaling from MonsterSpellRules, for spells without special budgets. */
  public static int balanceElementalDamage(int level, int rawDamage) {
    long scaled = (Math.max(0L, rawDamage) + 3) / 4;
    int maximum = (int) Math.max(1, Math.round(Math.max(0, level) * 0.5));
    return (int) Math.min(scaled, maximum);
  }

  /** Matches the one named-caster budget used by legacy monster spell balancing. */
  public static int balanceElementalDamage(int spellId, int level, int rawDamage, String casterName) {
    if (spellId == 10095 && "Arch Drake".equals(casterName))
      return Math.max(0, Math.min(rawDamage, 1800));
    return balanceElementalDamage(level, rawDamage);
  }

  /** Elemental resistance arithmetic used by MonsterSpellRules.damageAfterResistance. */
  public static int damageAfterResistance(int rawDamage, int resistance) {
    return (int) Math.min(
        Integer.MAX_VALUE, Math.max(0L, rawDamage) * 100 / Math.max(1, resistance));
  }
}
