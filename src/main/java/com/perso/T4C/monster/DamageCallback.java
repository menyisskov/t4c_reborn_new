package com.perso.T4C.monster;

/**
 * Callback interface for applying damage to the player.
 */
@FunctionalInterface
public interface DamageCallback {
    /**
     * Apply damage to the player.
     * @param damage The amount of damage to apply
     */
    void applyDamage(BaseMonster attacker, int damage);

    /** Apply a monster spell; default keeps older callbacks source-compatible. */
    default void applySpell(BaseMonster attacker, int spellId, int damage) {
        applyDamage(attacker, damage);
    }
}

