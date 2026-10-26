package com.perso.T4C.combat;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.player.BodyPart;
import org.junit.jupiter.api.Test;

class WeaponDamageRulesTest {
  private final WeaponDamageRules.Stats stats = new WeaponDamageRules.Stats(40, 30, 50, 20, 10, 7);

  @Test void bowFormulaReceivesQuiverDamageAndEffectiveStats() {
    var bow = new WeaponDamageRules.Weapon("item.bow", BodyPart.WEAPON, true, 0, "10+3*arrow_dmg+self.level");
    var quiver = new WeaponDamageRules.Weapon("item.quiver", BodyPart.WEAPON2, false, 8, "7");
    assertEquals(44, WeaponDamageRules.bow(stats, bow, quiver, 2));
    var shield = new WeaponDamageRules.Weapon("item.shield", BodyPart.WEAPON2, false, 2, "7");
    assertEquals(23, WeaponDamageRules.bow(stats, bow, shield, 2));
  }

  @Test void meleeAndOffHandShareNaturalDamageButMirroredWeaponsDoNotHitTwice() {
    var sword = new WeaponDamageRules.Weapon("item.sword", BodyPart.WEAPON, false, 0, "self.str/2");
    var dagger = new WeaponDamageRules.Weapon("item.dagger", BodyPart.WEAPON2, false, 0, "5");
    assertEquals(27, WeaponDamageRules.melee(stats, sword, dagger, 3));
    assertEquals(12, WeaponDamageRules.offHand(stats, sword, dagger, 3));
    assertEquals(0, WeaponDamageRules.offHand(stats, sword, sword, 3));
    assertEquals(0, WeaponDamageRules.offHand(stats, sword, null, 3));
  }
}
