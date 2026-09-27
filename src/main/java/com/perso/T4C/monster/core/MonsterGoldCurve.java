package com.perso.T4C.monster.core;

/**
 * The baseline gold a monster drops, as a function of its level (T4C-0071). Most of the original
 * roster already lands almost exactly on {@code goldMin ~= level * 1.8}, {@code goldMax ~=
 * level * 5.5} (checked by sampling `MonsterReportGenerator`'s output across the full level
 * range) - this just makes that existing, de-facto rule explicit so new content can be authored
 * against it instead of guessing a number.
 *
 * <p>Named bosses are expected to exceed this baseline (see DESIGN_GUIDELINES.md's "Economy"
 * section - a boss's gold-per-1000-HP should run 1.5-2x a nearby regular monster's), so this is a
 * floor content is checked against, not a ceiling.
 */
public final class MonsterGoldCurve {
  private static final float GOLD_MIN_PER_LEVEL = 1.8f;
  private static final float GOLD_MAX_PER_LEVEL = 5.5f;

  private MonsterGoldCurve() {}

  public static int goldMin(int level) {
    return Math.max(0, Math.round(level * GOLD_MIN_PER_LEVEL));
  }

  public static int goldMax(int level) {
    return Math.max(0, Math.round(level * GOLD_MAX_PER_LEVEL));
  }
}
