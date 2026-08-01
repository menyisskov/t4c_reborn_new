package com.perso.T4C.npc;

/** Behaviour the player assigns to their companion through dialogue. */
public enum CompanionMode {
    /** Follows the player and never engages, even when struck. */
    PASSIVE,
    /** Engages any monster it spots within its detection radius. */
    AGGRESSIVE,
    /** Default: only joins fights the player starts. */
    SUPPORT
}
