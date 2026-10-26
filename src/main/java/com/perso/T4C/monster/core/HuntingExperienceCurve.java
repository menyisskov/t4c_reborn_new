package com.perso.T4C.monster.core;

/**
 * Minimum base kill XP for the level-150-to-400 hunting ladder. T4C Fantasy is the scale reference,
 * calibrated to our actual hunting pace and extended through our level-400 cap. Health scales the
 * reference reward so a large boss pays more than a small creature at the same level.
 *
 * <p>This changes positive authored kill awards only. Damage XP, intentionally unrewarding
 * encounters, quests, saved progress and above-cap challenge monsters keep their own rules.
 */
public final class HuntingExperienceCurve {
  // Live feedback: 234 -> 274 in ten minutes. At the same kill rate, an 18x reduction
  // targets the midpoint (7.5 hours per 100 levels) of the owner's 5-10-hour range.
  private static final long HUNTING_PACE_DIVISOR = 18;

  private record Tier(int level, int health, int experience) {}

  private static final Tier[] TIERS = {
    new Tier(150, 10000, 3175000),
    new Tier(175, 15000, 25000000),
    new Tier(200, 30000, 150000000),
    new Tier(225, 43000, 250000000),
    new Tier(250, 53000, 320000000),
    new Tier(300, 70000, 500000000),
    new Tier(350, 100000, 800000000),
    new Tier(400, 140000, 1300000000)
  };

  private HuntingExperienceCurve() {}

  public static MonsterDef apply(MonsterDef definition) {
    if (definition == null || !definition.isCanAttack() || definition.getXpOnDeath() <= 0) {
      return definition;
    }
    int experience =
        Math.max(
            definition.getXpOnDeath(),
            minimumBaseKillExperience(definition.getLevel(), definition.getHealth()));
    return experience == definition.getXpOnDeath()
        ? definition
        : definition.withXpOnDeath(experience);
  }

  public static int minimumBaseKillExperience(int level, int health) {
    if (level < TIERS[0].level() || level > TIERS[TIERS.length - 1].level() || health <= 0) {
      return 0;
    }
    for (int i = 1; i < TIERS.length; i++) {
      Tier upper = TIERS[i];
      if (level > upper.level()) continue;
      Tier lower = TIERS[i - 1];
      int width = upper.level() - lower.level();
      int offset = level - lower.level();
      long experience =
          lower.experience() + (long) (upper.experience() - lower.experience()) * offset / width;
      long referenceHealth =
          lower.health() + (long) (upper.health() - lower.health()) * offset / width;
      // Authored fields are ints; the combat multiplier and player progression use longs.
      return (int)
          Math.min(
              Integer.MAX_VALUE, experience * health / (referenceHealth * HUNTING_PACE_DIVISOR));
    }
    throw new IllegalStateException("Missing hunting experience tier for " + level);
  }
}
