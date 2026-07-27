package com.perso.T4C.npc;

/**
 * Action triggered when the player clicks an NPC's dialog keyword.
 * {@link #HEAL} heals the player and {@link #TEACH} opens the spell-learning
 * screen; {@link #SHOP} and {@link #WAGER} are display-only placeholders
 * (room for future behavior).
 */
public enum KeywordActionType {
    NONE,
    HEAL,
    SHOP,
    WAGER,
    TEACH,
    TRAIN,
    CAST
}
