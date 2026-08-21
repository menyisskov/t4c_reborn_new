package com.perso.T4C.spell;

import java.util.Locale;

public final class SpellVisualResolver {
  public record Visuals(String projectile, String impact, String launchSound, String impactSound) {}

  private SpellVisualResolver() {}

  public static Visuals resolve(SpellData spell) {
    if (spell == null) {
      return new Visuals(null, "Flak1-", "Explosion.wav", "Explosion.wav");
    }
    String key = spell.getKey() == null ? "" : spell.getKey().toLowerCase(Locale.ROOT);
    String name = spell.getName() == null ? "" : spell.getName().toLowerCase(Locale.ROOT);
    String blob = key + " " + name;
    String projectile = firstNonBlank(spell.getProjectileSpell());
    projectile = SpellProjectilePalette.projectileFor(spell.getVisualEffect(), projectile);
    if (isBlank(projectile)) {
      projectile = SpellProjectilePalette.projectileFor(spell.getVisualEffect(), null);
      if (isBlank(projectile) || projectile.equals("null")) {
        projectile = defaultProjectile(spell, blob);
      }
    }
    String impact = firstNonBlank(spell.getImpactSpell());
    if (isBlank(impact)) {
      impact = defaultImpact(spell, blob, projectile);
    }
    String launch = withWav(firstNonBlank(spell.getSound()));
    if (isBlank(launch)) {
      launch = defaultLaunchSound(spell, blob);
    }
    String impactSound = withWav(firstNonBlank(spell.getSoundImpact()));
    if (isBlank(impactSound)) {
      impactSound = defaultImpactSound(spell, blob, launch);
    }
    return new Visuals(projectile, impact, launch, impactSound);
  }

  private static String defaultProjectile(SpellData spell, String blob) {
    if (containsAny(blob, "gateway", "teleport", "blink", "recall")) {
      return "Flak1-";
    }
    if (containsAny(blob, "potion", "heal", "regen", "bless", "cure")) {
      return "HealingSpell-";
    }
    return switch (spell.getElement()) {
      case 1 -> "64kSpellEnergyBallYellow-";
      case 2 -> "StoneShard";
      case 3 -> "64kSpellEnergyBallYellow-";
      case 4 -> "IceShard";
      case 6 -> "64kSpellEnergyBallBlack-";
      default -> spell.isAttack() ? "64kSpellEnergyBallBlue-" : "BlueWipe-";
    };
  }

  private static String defaultImpact(SpellData spell, String blob, String projectile) {
    if (containsAny(blob, "gateway", "teleport", "blink", "recall")) {
      return "Flak1-";
    }
    if (containsAny(blob, "potion", "heal", "regen", "bless", "cure")) {
      return "HealingSpell-";
    }
    if (containsAny(blob, "lightning", "bolt")) {
      return "GreatBolt-";
    }
    if (!isBlank(projectile) && projectile.endsWith("-")) {
      return projectile;
    }
    return switch (spell.getElement()) {
      case 1 -> "SmallExplosion-";
      case 2 -> "RockyFly-";
      case 3 -> "ElectricShield-";
      case 4 -> "IceCloud-";
      case 6 -> "Curse-";
      default -> "BlueWipe-";
    };
  }

  private static String defaultLaunchSound(SpellData spell, String blob) {
    if (containsAny(blob, "gateway", "teleport", "blink", "recall")) {
      return "Gateway.wav";
    }
    if (containsAny(blob, "potion", "heal", "regen", "bless", "cure")) {
      return "Healing.wav";
    }
    if (containsAny(blob, "lightning", "electric")) {
      return "Lightning.wav";
    }
    if (containsAny(blob, "curse", "drain", "poison")) {
      return "Curse.wav";
    }
    if (spell.isAttack() || spell.isLineOfSight()) {
      return "Small Projectile.wav";
    }
    return "Healing.wav";
  }

  private static String defaultImpactSound(SpellData spell, String blob, String launch) {
    if (containsAny(blob, "gateway", "teleport", "blink", "recall")) {
      return "Explosion.wav";
    }
    if (containsAny(blob, "potion", "heal", "regen", "bless", "cure")) {
      return "Healing.wav";
    }
    return switch (spell.getElement()) {
      case 1 -> "Explosion.wav";
      case 2 -> "Rocks fly.wav";
      case 3 -> "Lightning.wav";
      case 4 -> "Ice Cloud.wav";
      case 6 -> "Curse.wav";
      default -> isBlank(launch) ? "Spark.wav" : launch;
    };
  }

  private static boolean containsAny(String blob, String... tokens) {
    for (String token : tokens) {
      if (blob.contains(token)) {
        return true;
      }
    }
    return false;
  }

  private static String firstNonBlank(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty() || "0".equals(trimmed)) {
      return null;
    }
    return trimmed;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank() || "0".equals(value.trim()) || "null".equals(value);
  }

  private static String withWav(String sound) {
    if (isBlank(sound)) {
      return null;
    }
    String file = sound.toLowerCase(Locale.ROOT).endsWith(".wav") ? sound : sound + ".wav";
    return switch (file.toLowerCase(Locale.ROOT)) {
      case "lightningbolt.wav", "lightning bolt.wav" -> "Lightning.wav";
      case "fire circle.wav", "firecircle.wav" -> "Fire Circle.wav";
      case "rocks fly.wav", "rocksfly.wav" -> "Rocks fly.wav";
      default -> file;
    };
  }
}
