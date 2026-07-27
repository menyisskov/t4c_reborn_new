package com.perso.T4C.monster;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Data-driven definition of a monster type. Replaces the former hardcoded
 * {@code com.perso.T4C.monster.types.*} subclasses; persisted in
 * {@code assets/monsters/monsters.bin} via {@link com.perso.T4C.helper.MonsterDefBinaryIO}
 * and instantiated at runtime by {@link DataMonster}.
 */
@Getter
@AllArgsConstructor
public class MonsterDef {
    /** Unique key; doubles as the spawn "type" key (was the class simple name). */
    private final String name;
    /** Name shown in-game (passed to the monster's display logic). Falls back to {@link #name}. */
    private final String displayName;
    private final int health;
    private final int mana;
    private final int xpPerHit;
    private final int xpOnDeath;
    /** Fallback melee damage min when attacks list is empty. */
    private final int hitDamageMin;
    /** Fallback melee damage max when attacks list is empty. */
    private final int hitDamageMax;
    private final long respawnTime;
    private final String walkPattern;
    private final String attackPattern;
    private final String deathPattern;
    private final String soundAttack;
    private final String soundDeath;
    private final String soundHit;
    private final int goldMin;
    private final int goldMax;
    /** Item drops; never null (empty = drops only gold/nothing). */
    private final List<LootDrop> loot;
    /** Whether the monster plays its walk animation while stationary (e.g. Dragon). */
    private final boolean animateWhileStationary;
    /** Pause between stationary animation loops, in seconds (only when animateWhileStationary). */
    private final float stationaryAnimationPauseSeconds;

    // --- T4C stats (v2 fields) ---
    private final int str;
    private final int end;
    private final int agi;
    private final int intel;
    private final int will;
    private final int wis;
    private final int luck;
    /** Damage/magic resistances; 12 values. Never null. */
    private final int[] resists;
    private final int level;
    private final int dodge;
    private final int acMin;
    private final int acMax;
    /** T4C sprite appearance ID. */
    private final int appearance;
    private final int itemBody;
    private final int itemFeet;
    private final int itemHands;
    private final int itemHead;
    private final int itemLegs;
    private final int itemWeapon;
    private final int itemShield;
    private final int itemBack;
    /** Aggro value 0-100; 0 = passive. Replaces the former boolean defaultAggressive. */
    private final int aggro;
    /** T4C clan ID; 0 = none. */
    private final int clan;
    private final int speed;
    private final boolean canAttack;
    /** T4C attack list; empty = use hitDamageMin/hitDamageMax fallback. Never null. */
    private final List<Attack> attacks;

    /** True when aggro > 0 (mirrors the former defaultAggressive boolean). */
    public boolean isDefaultAggressive() {
        return aggro > 0;
    }

    /**
     * A single probabilistic item drop.
     */
    @Getter
    @AllArgsConstructor
    public static final class LootDrop {
        private final String item;
        /** Drop probability in [0..1]. */
        private final float chance;
    }

    /**
     * A T4C monster attack entry.
     */
    @Getter
    @AllArgsConstructor
    public static final class Attack {
        /** Dice formula, e.g. "1d4+1". */
        private final String name;
        /** Primary damage/spell ref value. */
        private final int value1;
        /** Attack rate (0-100). */
        private final int value2;
        private final int value3;
        private final int value4;
        private final int value5;
    }
}
