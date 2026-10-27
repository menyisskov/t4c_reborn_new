package com.perso.T4C.death;

import java.util.random.RandomGenerator;

/** Pure original death rules shared by desktop and authoritative online play. */
public final class DeathRules {
  private DeathRules() {}
  public static int backpackPercent(boolean pvp) { return pvp ? 1 : 5; }
  public static int experiencePercent(boolean pvp) { return pvp ? 1 : 5; }
  public static int respawnHealth(int maximum) { return Math.max(1, maximum / 2); }
  public static boolean rollPercent(RandomGenerator random, int percent) {
    return percent > 0 && random.nextInt(100) < percent;
  }
  public static long percentage(long value, int percent) {
    long positive=Math.max(0,value);
    // Equivalent integer truncation without overflowing a long cumulative ledger.
    return (positive/100)*percent + (positive%100)*percent/100;
  }
}
