package com.perso.T4C.combat;

public record PhysicalAttackRequest(
    CombatProfile attacker,
    CombatProfile target,
    int baseDamage,
    int offHandDamage,
    boolean ranged) {
  public PhysicalAttackRequest {
    if (attacker == null || target == null) {
      throw new IllegalArgumentException("Attacker and target are required");
    }
    baseDamage = Math.max(0, baseDamage);
    offHandDamage = Math.max(0, offHandDamage);
  }
}
