package com.perso.T4C.monster.core;

/** Combat XP only; quest rewards and existing saved progress retain their authored values. */
public final class MonsterExperience {
  public static final int MULTIPLIER = 2;

  private MonsterExperience() {}

  public static long reward(int baseExperience) {
    return (long) Math.max(0, baseExperience) * MULTIPLIER;
  }

  public static long damageReward(int experiencePerDamage, int effectiveDamage) {
    return reward(experiencePerDamage) * Math.max(0, effectiveDamage);
  }
}
