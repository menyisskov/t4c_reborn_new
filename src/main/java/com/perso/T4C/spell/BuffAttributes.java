package com.perso.T4C.spell;

import java.util.Set;

/**
 * The attribute names a timed spell buff can raise, shared by single-player and the online server.
 * {@link #normalize} is the exact switch single-player's {@code SpellEffectManager} always used to
 * turn an authored effect-2 attribute into the key {@code Player} keeps its buff totals under, so a
 * buff means the same thing on both sides. {@link #MODELLED} lists the normalised keys the online
 * server folds into its combat and spell numbers; a buff with any other key is refused online rather
 * than half applied.
 */
public final class BuffAttributes {
  private BuffAttributes() {}

  /** Normalised keys the online server applies: stats, armor class, max HP, skills, powers, resistances, radiance. */
  public static final Set<String> MODELLED =
      Set.of(
          "str", "dex", "end", "int", "wis", "armorClass", "maxHp", "skill:attack", "skill:dodge",
          "skill:archery", "power:air", "power:fire", "power:water", "power:earth", "power:light",
          "power:dark", "resist:air", "resist:fire", "resist:water", "resist:earth", "resist:light",
          "resist:dark", "radiance");

  /** The key {@code Player} stores a boost under, or null when the attribute is not a known boost. */
  public static String normalize(String attribute) {
    if (attribute == null) return null;
    return switch (attribute.trim().toLowerCase()) {
      case "str", "strength" -> "str";
      case "agi", "agility", "dex", "dexterity" -> "dex";
      case "end", "endurance" -> "end";
      case "int", "intelligence" -> "int";
      case "wis", "wisdom" -> "wis";
      case "ac", "armor class" -> "armorClass";
      case "ac percent" -> "armorClassPercent";
      case "healing received" -> "healingReceivedPercent";
      case "max hp", "maxhp" -> "maxHp";
      case "attack" -> "skill:attack";
      case "dodge" -> "skill:dodge";
      case "skill 35", "archery" -> "skill:archery";
      case "skill 9" -> "skill:9";
      case "skill 29" -> "skill:29";
      case "air", "fire", "water", "earth", "light", "dark" ->
          "power:" + attribute.trim().toLowerCase();
      case "mana" -> "mana";
      case "radiance" -> "radiance";
      case "r_air" -> "resist:air";
      case "r_fire" -> "resist:fire";
      case "r_water" -> "resist:water";
      case "r_earth" -> "resist:earth";
      case "r_light" -> "resist:light";
      case "r_dark" -> "resist:dark";
      case "exp", "experience", "xp" -> "exp";
      case "unlimited" -> "unlimited";
      default -> null;
    };
  }
}
