package com.perso.T4C.helper;

import com.perso.T4C.config.GameConstants;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;

public final class CharacterCreationRules {
  public static final int MIN_NAME_LENGTH = 2;
  public static final int MAX_NAME_LENGTH = 8;

  /**
   * What every attribute starts at before the class spread is added (T4C-0059). This is the same
   * floor a rebirth resets to, so a brand-new character sits exactly one rebirth step below a
   * once-reborn one instead of the 10-16 the old questionnaire rolled.
   */
  public static final int BASE_ATTRIBUTE = GameConstants.REBIRTH_BASE_ATTRIBUTE;

  /** Bonus points every class spreads over its attributes - identical for all of them. */
  public static final int CLASS_BONUS_POINTS = 30;

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

  /**
   * Rolls a starting character of {@code characterClass}. The five attributes are fixed - {@link
   * #BASE_ATTRIBUTE} plus the class's own spread - so rerolling only ever changes health and mana.
   */
  public static Stats roll(CharacterClass characterClass, Random random) {
    Objects.requireNonNull(characterClass, "characterClass");
    Objects.requireNonNull(random, "random");
    int strength = BASE_ATTRIBUTE + characterClass.strengthBonus();
    int endurance = BASE_ATTRIBUTE + characterClass.enduranceBonus();
    int dexterity = BASE_ATTRIBUTE + characterClass.dexterityBonus();
    int wisdom = BASE_ATTRIBUTE + characterClass.wisdomBonus();
    int intelligence = BASE_ATTRIBUTE + characterClass.intelligenceBonus();
    return new Stats(
        strength,
        endurance,
        dexterity,
        wisdom,
        intelligence,
        rollMaxHp(endurance, random),
        rollMaxMana(wisdom, intelligence, random));
  }

  /**
   * Rerolls only the health and mana of an existing roll, leaving the class attributes alone -
   * what the "Reroll" button on the creation screen does.
   */
  public static Stats rerollVitals(Stats stats, Random random) {
    Objects.requireNonNull(stats, "stats");
    Objects.requireNonNull(random, "random");
    return new Stats(
        stats.strength(),
        stats.endurance(),
        stats.dexterity(),
        stats.wisdom(),
        stats.intelligence(),
        rollMaxHp(stats.endurance(), random),
        rollMaxMana(stats.wisdom(), stats.intelligence(), random));
  }

  private static int rollMaxHp(int endurance, Random random) {
    return 18 + endurance / 2 + random.nextInt(6);
  }

  private static int rollMaxMana(int wisdom, int intelligence, Random random) {
    return 6 + (wisdom + intelligence) / 5 + random.nextInt(4);
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
