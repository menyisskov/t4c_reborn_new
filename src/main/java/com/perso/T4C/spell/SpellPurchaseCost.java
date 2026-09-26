package com.perso.T4C.spell;

/**
 * The skill-point cost to learn a spell at Lighthaven's spell seller (T4C-0054). Previously every
 * spell cost a flat 5 points regardless of tier, so a level-400 nuke and a level-2 bolt were
 * equally cheap. This scales with the spell's own {@code minLevel} instead, capped at 100 per the
 * owner's rule that no single purchase should ever cost more than that. Calibrated against
 * t4cbible.com/Spells' skill-point column (roughly 5 points at the lowest tiers, high 20s/low 30s
 * by level 100) and stretched out so it reaches the 100 cap only at the level cap (400).
 */
public final class SpellPurchaseCost {
  private static final int MIN_COST = 5;
  private static final int MAX_COST = 100;

  private SpellPurchaseCost() {}

  public static int skillPoints(SpellData spell) {
    return skillPointsForLevel(spell == null ? 0 : spell.getMinLevel());
  }

  public static int skillPointsForLevel(int minLevel) {
    int level = Math.max(0, minLevel);
    int raw = MIN_COST + (level * 6 + 12) / 25;
    return Math.min(MAX_COST, Math.max(MIN_COST, raw));
  }
}
