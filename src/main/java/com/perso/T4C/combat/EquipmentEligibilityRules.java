package com.perso.T4C.combat;

import com.perso.T4C.player.BodyPart;

/** Shared equipment eligibility; possession and transactional mutation belong to each runtime. */
public final class EquipmentEligibilityRules {
  private EquipmentEligibilityRules() {}

  public record Requirements(long endurance, long strength, long dexterity, long intelligence, long wisdom, long attack) {}

  public static boolean meets(WeaponDamageRules.Stats base, int trainedAttack, Requirements required) {
    int attack = trainedAttack <= 0 ? base.level() + base.dexterity() : trainedAttack;
    return base.endurance() >= required.endurance() && base.strength() >= required.strength()
        && base.dexterity() >= required.dexterity() && base.intelligence() >= required.intelligence()
        && base.wisdom() >= required.wisdom() && attack >= required.attack();
  }

  public static boolean slotMatches(BodyPart requested, BodyPart defined) {
    if (requested == null || defined == null) return false;
    if (requested == defined) return true;
    if ((requested == BodyPart.RING1 || requested == BodyPart.RING2)
        && (defined == BodyPart.RING1 || defined == BodyPart.RING2)) return true;
    return requested == BodyPart.SHIELD && defined == BodyPart.WEAPON2;
  }

  public static boolean isQuiver(String key) {
    return key != null && (key.contains("quiver") || key.contains("arrow"));
  }
}
