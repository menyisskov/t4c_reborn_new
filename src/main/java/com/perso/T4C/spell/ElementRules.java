package com.perso.T4C.spell;

import java.util.List;
import java.util.Locale;

/**
 * Element power and resistance arithmetic shared by single-player {@code Player} and the online
 * server, so both read the same stat ids and compose the same terms. Callers supply the four
 * ingredients (trained skill, spell buff, legacy quest bonus, equipment bonus); this class owns
 * only how they combine.
 */
public final class ElementRules {
  /** Element order used by every spell formula context. */
  public static final List<String> ELEMENTS = List.of("fire", "earth", "air", "water", "light", "dark");

  private ElementRules() {}

  /** Equipment boost stat id that raises the element's power; {@code Integer.MIN_VALUE} if unknown. */
  public static int powerStatId(String element) {
    return switch (normalize(element)) {
      case "air" -> 16;
      case "fire" -> 17;
      case "water" -> 18;
      case "earth" -> 19;
      case "light" -> 23;
      case "dark" -> 24;
      default -> Integer.MIN_VALUE;
    };
  }

  /** Equipment boost stat id that raises the element's resistance; {@code Integer.MIN_VALUE} if unknown. */
  public static int resistanceStatId(String element) {
    return switch (normalize(element)) {
      case "air" -> 12;
      case "fire" -> 13;
      case "water" -> 14;
      case "earth" -> 15;
      case "light" -> 21;
      case "dark" -> 22;
      default -> Integer.MIN_VALUE;
    };
  }

  /** A trained skill of zero or less still counts as the neutral 100. */
  public static int power(int trainedSkill, int buff, int legacyBonus, int equipmentBonus) {
    return (trainedSkill <= 0 ? 100 : trainedSkill) + buff + legacyBonus + equipmentBonus;
  }

  /** Light starts from 5000, every other element from 100. */
  public static int resistance(String element, int buff, int legacyBonus, int equipmentBonus) {
    int base = normalize(element).equals("light") ? 5000 : 100;
    return base + buff + legacyBonus + equipmentBonus;
  }

  /** A monster resistance of zero or less means "unset" and is read as 100 by spell formulas. */
  public static int targetResistance(int monsterResistance) {
    return monsterResistance <= 0 ? 100 : monsterResistance;
  }

  private static String normalize(String element) {
    return element == null ? "" : element.toLowerCase(Locale.ROOT);
  }
}
