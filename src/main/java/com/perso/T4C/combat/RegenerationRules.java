package com.perso.T4C.combat;

import java.util.random.RandomGenerator;

public final class RegenerationRules {
  public static final float REGEN_INTERVAL_SECONDS = 2f;
  private static final int REGEN_CHANCE_EXCLUSIVE = 61;

  private RegenerationRules() {}

  public static int regenerationMultiplier(int skillPoints) {
    return Math.max(1, (Math.max(0, skillPoints) + 25) / 25);
  }

  public static int regenerateHp(int currentHp, int maxHp, int endurance, RandomGenerator random) {
    return regenerateHp(currentHp, maxHp, endurance, 0, random);
  }

  public static int regenerateHp(
      int currentHp, int maxHp, int endurance, int rapidHealingPoints, RandomGenerator random) {
    if (currentHp < maxHp) {
      if (rnd(random, 1, 100) >= REGEN_CHANCE_EXCLUSIVE) {
        return currentHp;
      }
      int gain =
          (rnd(random, 0, Math.max(0, endurance) / 40) + 1)
              * regenerationMultiplier(rapidHealingPoints);
      return Math.min(maxHp, currentHp + gain);
    }
    if (currentHp > maxHp) {
      int drain = (int) Math.ceil(maxHp * 5d / 100d);
      return Math.max(maxHp, currentHp - drain);
    }
    return currentHp;
  }

  public static int regenerateMana(
      int mana, int maxMana, int intelligence, int wisdom, RandomGenerator random) {
    return regenerateMana(mana, maxMana, intelligence, wisdom, 0, random);
  }

  public static int regenerateMana(
      int mana,
      int maxMana,
      int intelligence,
      int wisdom,
      int meditatePoints,
      RandomGenerator random) {
    if (mana < maxMana) {
      if (rnd(random, 1, 100) >= REGEN_CHANCE_EXCLUSIVE) {
        return mana;
      }
      int intSteps = Math.max(0, intelligence) / 160;
      int wisSteps = Math.max(0, wisdom) / 160;
      int gain =
          (rnd(random, 0, intSteps) + intSteps + rnd(random, 0, wisSteps) + wisSteps + 1)
              * regenerationMultiplier(meditatePoints);
      return Math.min(maxMana, mana + gain);
    }
    if (mana > maxMana) {
      int drain = (int) Math.ceil(maxMana * 2d / 100d);
      return Math.max(maxMana, mana - drain);
    }
    return mana;
  }

  private static int rnd(RandomGenerator random, int min, int max) {
    return max <= min ? min : random.nextInt(min, max + 1);
  }
}
