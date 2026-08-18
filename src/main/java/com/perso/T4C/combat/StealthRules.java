package com.perso.T4C.combat;

import java.util.random.RandomGenerator;

public final class StealthRules {
  public static final int WITNESS_RANGE = 40;

  private StealthRules() {}

  public static boolean staysHidden(
      int sneakSkill, int agility, int witnesses, RandomGenerator random) {
    int penalty = witnesses <= 0 ? 0 : (witnesses - 1) * 10;
    int success = Math.max(0, sneakSkill) + Math.max(0, agility) / 6 - penalty;
    return roll(random, 100) < success;
  }

  private static int roll(RandomGenerator random, int faces) {
    return faces <= 1 ? 1 : random.nextInt(faces) + 1;
  }
}
