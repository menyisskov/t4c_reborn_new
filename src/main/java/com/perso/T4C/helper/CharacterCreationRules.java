package com.perso.T4C.helper;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;

public final class CharacterCreationRules {
  public static final int MIN_NAME_LENGTH = 2;
  public static final int MAX_NAME_LENGTH = 8;
  public static final int AFFINITY_COUNT = 5;
  private static final Pattern VALID_NAME =
      Pattern.compile("[\\p{L}][\\p{L} '-]{0,18}[\\p{L}]|[\\p{L}]{2}");

  private CharacterCreationRules() {}

  public static boolean isValidName(String value) {
    if (value == null) return false;
    String name = value.trim();
    boolean punctuatedClassicName = name.indexOf('\'') >= 0;
    return name.length() >= MIN_NAME_LENGTH
        && (name.length() <= MAX_NAME_LENGTH || punctuatedClassicName)
        && VALID_NAME.matcher(name).matches()
        && !name.contains("  ")
        && !name.contains("--")
        && !name.contains("''");
  }

  public static String normalizeName(String value) {
    if (value == null) return "";
    String trimmed = value.trim().replaceAll("\\s+", " ");
    if (trimmed.isEmpty()) return trimmed;
    return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
  }

  public static Stats roll(int[] affinities, Random random) {
    Objects.requireNonNull(random, "random");
    if (affinities == null || affinities.length != AFFINITY_COUNT) {
      throw new IllegalArgumentException("Five questionnaire affinities are required");
    }
    int strength = attribute(affinities[0], random);
    int endurance = attribute(affinities[1], random);
    int dexterity = attribute(affinities[2], random);
    int wisdom = attribute(affinities[3], random);
    int intelligence = attribute(affinities[4], random);
    int maxHp = 18 + endurance / 2 + random.nextInt(6);
    int maxMana = 6 + (wisdom + intelligence) / 5;
    return new Stats(strength, endurance, dexterity, wisdom, intelligence, maxHp, maxMana);
  }

  private static int attribute(int affinity, Random random) {
    return 10 + random.nextInt(7) + Math.max(0, affinity) * 2;
  }

  public record Stats(
      int strength,
      int endurance,
      int dexterity,
      int wisdom,
      int intelligence,
      int maxHp,
      int maxMana) {}
}
