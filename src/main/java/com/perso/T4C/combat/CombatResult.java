package com.perso.T4C.combat;

public record CombatResult(
    boolean hit,
    int precision,
    int rawDamage,
    int damage,
    boolean parried,
    boolean powerfulBlow,
    boolean armorPenetration,
    boolean dualWeapon,
    long stunDurationMillis) {
  public static CombatResult miss(int precision, int rawDamage) {
    return new CombatResult(
        false, precision, Math.max(0, rawDamage), 0, false, false, false, false, 0L);
  }
}
