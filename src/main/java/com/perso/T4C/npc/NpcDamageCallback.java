package com.perso.T4C.npc;

/**
 * Callback interface for a hostile NPC applying damage to the player.
 */
@FunctionalInterface
public interface NpcDamageCallback {
    /**
     * Apply damage to the player.
     * @param damage The amount of damage to apply
     */
    void applyDamage(BaseNPC attacker, int damage);
}
