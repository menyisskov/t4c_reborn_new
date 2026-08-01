package com.perso.T4C.npc;

/** Tactical rule deciding when a companion casts one of its spells. */
public enum CompanionSpellTrigger {
    /** Offensive spell aimed at the monster the companion is fighting. */
    ATTACK,
    /** Restores the companion itself once its health drops below the threshold. */
    HEAL_SELF,
    /** Restores the owner once their health drops below the threshold. */
    HEAL_OWNER
}
