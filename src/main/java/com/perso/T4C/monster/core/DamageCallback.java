package com.perso.T4C.monster.core;

@FunctionalInterface
public interface DamageCallback {
  void applyDamage(BaseMonster attacker, int damage);

  default void applySpell(BaseMonster attacker, int spellId, int damage) {
    applyDamage(attacker, damage);
  }
}
