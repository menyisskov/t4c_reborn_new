package com.perso.T4C.combat;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.player.BodyPart;

/** Shared damage rolls over server/offline-resolved effective stats and equipment definitions. */
public final class WeaponDamageRules {
  private WeaponDamageRules() {}

  public record Stats(int strength, int endurance, int dexterity, int intelligence, int wisdom, int level) {}

  public record Weapon(String key, BodyPart bodyPart, boolean bow, int structure, String formula) {}

  public static int melee(Stats stats, Weapon primary, Weapon secondary, int flatBonus) {
    int weapon = roll(stats, primary, secondary);
    int natural = stats.strength() >= 20 ? (stats.strength() - 20) / 5 : 0;
    return Math.max(1, weapon + natural + flatBonus);
  }

  public static int bow(Stats stats, Weapon primary, Weapon secondary, int flatBonus) {
    Weapon selected = primary != null && primary.bow() ? primary : secondary;
    int weapon = roll(stats, selected, secondary);
    int natural = (stats.strength() >= 20 ? (stats.strength() - 20) / 20 : 0)
        + (stats.dexterity() >= 20 ? (stats.dexterity() - 20) / 10 : 0);
    return Math.max(1, weapon + natural + flatBonus);
  }

  public static int offHand(Stats stats, Weapon primary, Weapon secondary, int flatBonus) {
    if (secondary == null || secondary.bow()
        || (primary != null && secondary.key().equals(primary.key()))) return 0;
    return melee(stats, secondary, secondary, flatBonus);
  }

  private static int roll(Stats stats, Weapon weapon, Weapon secondary) {
    if (weapon == null || weapon.formula() == null || weapon.formula().isEmpty()
        || "0".equals(weapon.formula())) return (int) (Math.random() * 4) + 1;
    int arrowDamage = 0;
    if (weapon.bow() && weapon.bodyPart() == BodyPart.WEAPON
        && secondary != null && secondary.structure() == 8 && secondary.formula() != null) {
      var arrowContext = new DiceFormula.Context(stats.strength(), stats.endurance(), stats.dexterity(),
          stats.intelligence(), 0, stats.wisdom(), 0, stats.level());
      arrowDamage = Math.max(0, DiceFormula.of(secondary.formula()).roll(arrowContext));
    }
    var context = new DiceFormula.Context(stats.strength(), stats.endurance(), stats.dexterity(),
        stats.intelligence(), 0, stats.wisdom(), 0, stats.level(), arrowDamage, 100, 100);
    return DiceFormula.of(weapon.formula()).roll(context);
  }
}
