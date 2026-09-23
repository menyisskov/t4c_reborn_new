package com.perso.T4C.combat;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class SeraphAuraService {
  public static final int AURA_SPELL_ID = 10696;
  public static final int SINGLE_BLAST_SPELL_ID = 10697;
  public static final int AREA_BLAST_SPELL_ID = 10698;
  public static final int HEAL_SPELL_ID = 10699;
  public static final String AURA_NAME = "spell.remort_aura";
  private static final String LEGACY_AURA_NAME = "Remort aura";
  public static final String AURA_DESCRIPTION = I18n.placeholder("spell.description.remort_aura");
  public static final String AURA_ICON = "64kSpellIconNoneDefense";
  public static final String SINGLE_PROJECTILE = "64kSpellEnergyBall-";
  public static final String SINGLE_IMPACT = "SmallExplosion-";
  public static final String AREA_CENTER_IMPACT = "GreatExplosion-";
  public static final String AREA_PROJECTILE = "64kSpellFireBall";
  public static final String HEAL_IMPACT = "HealingSpell-";
  public static final String HEAL_RADIAL_EFFECT = "64kSpellEnergyBallWhite-";
  public static final String HEAL_SOUND = "Healing.wav";
  public static final String EXPLOSION_SOUND = "Explosion.wav";
  public static final String FIREBALL_SOUND = "FireBall 2.wav";
  public static final int AREA_RADIUS = 6;
  public static final int FIRE_RESIST_IMMUNITY = 5000;
  public static final double MAX_DAMAGEABLE_ARMOR_CLASS = 64_999d;
  private static final String WHITE_WING_PREFIX = "PupSeraphWhiteWings";
  private static final String BLACK_WING_PREFIX = "PupSeraphBlackWings";

  private SeraphAuraService() {}

  public static boolean synchronize(Player player) {
    Objects.requireNonNull(player, "player");
    boolean changed = false;
    while (player.dispelBuff(LEGACY_AURA_NAME)) {
      changed = true;
    }
    if (player.getRebirthCount() <= 0 && hasSeraphWings(player)) {
      player.setRebirthCount(1);
      changed = true;
    }
    if (player.getRebirthCount() <= 0) {
      while (player.dispelBuff(AURA_NAME)) {
        changed = true;
      }
      return changed;
    }
    if (!player.hasBuff(AURA_NAME)) {
      player.applyBuff(AURA_NAME, AURA_DESCRIPTION, AURA_ICON, null, true);
      changed = true;
    }
    return changed;
  }

  /** Percent chance, per hit taken, that the aura heals the Seraph and nearby allies. */
  public static int healingChance(int rebirthCount) {
    return rebirthCount <= 0 ? 0 : Math.min(100, rebirthCount + 4);
  }

  /** Percent chance, per hit taken, that the aura burns the attacker. */
  public static int retaliationChance(int rebirthCount) {
    return rebirthCount <= 0 ? 0 : Math.min(100, rebirthCount * 5);
  }

  /** Percent chance, per hit landed, of a fire burst around the Seraph. */
  public static int areaBurstChance(int rebirthCount) {
    return rebirthCount <= 0 ? 0 : Math.min(100, rebirthCount);
  }

  public static boolean hasSeraphWings(Player player) {
    if (player == null || player.getEquippedItems() == null) {
      return false;
    }
    String itemKey = player.getEquippedItems().get(BodyPart.BACK);
    if (itemKey == null || itemKey.isBlank()) {
      return false;
    }
    if (isSeraphWingAppearance(itemKey)) {
      return true;
    }
    ItemDefinition definition = ItemRegistry.findByKey(itemKey);
    return definition != null
        && isSeraphWingAppearance(definition.getAppearanceEquippedFor(BodyPart.BACK));
  }

  public static boolean isSeraphWingAppearance(String appearance) {
    return appearance != null
        && (startsWithIgnoreCase(appearance, WHITE_WING_PREFIX)
            || startsWithIgnoreCase(appearance, BLACK_WING_PREFIX));
  }

  public static OnHitResult onHit(Player player, int attackerFireResistance) {
    return onHit(player, attackerFireResistance, ThreadLocalRandom.current());
  }

  public static OnHitResult onHit(
      Player player, int attackerFireResistance, RandomGenerator random) {
    return onHit(player, attackerFireResistance, 0d, random);
  }

  public static OnHitResult onHit(
      Player player,
      int attackerFireResistance,
      double attackerArmorClass,
      RandomGenerator random) {
    Objects.requireNonNull(player, "player");
    return onHit(
        player.getRebirthCount(),
        EffectiveStats.from(player),
        attackerFireResistance,
        attackerArmorClass,
        random);
  }

  public static OnHitResult onHit(
      int rebirthCount, EffectiveStats stats, int attackerFireResistance, RandomGenerator random) {
    return onHit(rebirthCount, stats, attackerFireResistance, 0d, random);
  }

  public static OnHitResult onHit(
      int rebirthCount,
      EffectiveStats stats,
      int attackerFireResistance,
      double attackerArmorClass,
      RandomGenerator random) {
    Objects.requireNonNull(stats, "stats");
    Objects.requireNonNull(random, "random");
    if (rebirthCount <= 0) {
      return new OnHitResult(false, 0, 0, false, 0);
    }
    boolean healingTriggered = succeeds(healingChance(rebirthCount), random);
    int centralHealing = 0;
    int radialHealing = 0;
    if (healingTriggered) {
      int baseHealing = stats.sum() / 5;
      centralHealing = baseHealing + d5(random);
      radialHealing = baseHealing + d5(random);
    }
    boolean retaliationTriggered = succeeds(retaliationChance(rebirthCount), random);
    int retaliationDamage = 0;
    if (retaliationTriggered && attackerFireResistance < FIRE_RESIST_IMMUNITY) {
      int rolledDamage = stats.sum() / 10 + d5(random);
      if (attackerArmorClass <= MAX_DAMAGEABLE_ARMOR_CLASS) {
        retaliationDamage = rolledDamage;
      }
    }
    return new OnHitResult(
        healingTriggered, centralHealing, radialHealing, retaliationTriggered, retaliationDamage);
  }

  public static OnAttackHitResult onAttackHit(Player player) {
    return onAttackHit(player, ThreadLocalRandom.current());
  }

  public static OnAttackHitResult onAttackHit(Player player, RandomGenerator random) {
    Objects.requireNonNull(player, "player");
    return onAttackHit(player.getRebirthCount(), random);
  }

  public static OnAttackHitResult onAttackHit(int rebirthCount, RandomGenerator random) {
    Objects.requireNonNull(random, "random");
    return new OnAttackHitResult(succeeds(areaBurstChance(rebirthCount), random), AREA_RADIUS);
  }

  public static int rollAreaDamage(Player player, int targetFireResistance) {
    return rollAreaDamage(player, targetFireResistance, ThreadLocalRandom.current());
  }

  public static int rollAreaDamage(
      Player player, int targetFireResistance, RandomGenerator random) {
    return rollAreaDamage(player, targetFireResistance, 0d, random);
  }

  public static int rollAreaDamage(
      Player player, int targetFireResistance, double targetArmorClass, RandomGenerator random) {
    Objects.requireNonNull(player, "player");
    return rollAreaDamage(
        EffectiveStats.from(player), targetFireResistance, targetArmorClass, random);
  }

  public static int rollAreaDamage(
      EffectiveStats stats, int targetFireResistance, RandomGenerator random) {
    return rollAreaDamage(stats, targetFireResistance, 0d, random);
  }

  public static int rollAreaDamage(
      EffectiveStats stats,
      int targetFireResistance,
      double targetArmorClass,
      RandomGenerator random) {
    Objects.requireNonNull(stats, "stats");
    Objects.requireNonNull(random, "random");
    if (targetFireResistance >= FIRE_RESIST_IMMUNITY) {
      return 0;
    }
    int rolledDamage = stats.sum() / 5 - d5(random);
    return targetArmorClass > MAX_DAMAGEABLE_ARMOR_CLASS ? 0 : rolledDamage;
  }

  /**
   * The real percent probability that {@link #succeeds} passes for a given chance value: it rolls
   * 0-100 inclusive (101 outcomes) and passes on {@code <= chance}, so a chance of {@code c}
   * (0 &lt; c &lt; 100) fires on {@code (c + 1) / 101} of rolls, e.g. 1 -> ~1.98%. Rounded to one
   * decimal place.
   */
  public static double effectivePercent(int chance) {
    if (chance <= 0) return 0d;
    if (chance >= 100) return 100d;
    return Math.round((chance + 1) * 1000d / 101d) / 10d;
  }

  private static boolean succeeds(int chance, RandomGenerator random) {
    return chance > 0 && random.nextInt(101) <= chance;
  }

  private static int d5(RandomGenerator random) {
    return random.nextInt(5) + 1;
  }

  private static boolean startsWithIgnoreCase(String value, String prefix) {
    return value.length() >= prefix.length()
        && value.regionMatches(true, 0, prefix, 0, prefix.length());
  }

  public record EffectiveStats(
      int strength, int agility, int endurance, int intelligence, int wisdom) {
    public static EffectiveStats from(Player player) {
      Objects.requireNonNull(player, "player");
      return new EffectiveStats(
          player.getEffectiveStrength(),
          player.getEffectiveDexterity(),
          player.getEffectiveEndurance(),
          player.getEffectiveIntelligence(),
          player.getEffectiveWisdom());
    }

    public int sum() {
      return strength + agility + endurance + intelligence + wisdom;
    }
  }

  public record OnHitResult(
      boolean healingTriggered,
      int centralHealing,
      int radialHealing,
      boolean retaliationTriggered,
      int retaliationDamage) {
    public int totalHealing() {
      return centralHealing + radialHealing;
    }
  }

  public record OnAttackHitResult(boolean triggered, int radius) {}
}
