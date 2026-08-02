package com.perso.T4C.npc;

import com.perso.T4C.player.BodyPart;
import lombok.Getter;

import java.util.List;

/**
 * Data-driven definition of an ally companion: appearance, level-scaled combat
 * kit, and the spell book its AI picks from. Stored in
 * {@link com.perso.T4C.config.Paths#COMPANIONS_BIN} so new companions are pure
 * content, requiring no code change.
 */
@Getter
public final class CompanionDef {
    /** Stable id referenced by an NPC's SUMMON_COMPANION dialogue action. */
    private final String id;
    private final String displayName;
    private final List<Part> parts;
    private final String spriteBase;

    // Combat kit. Base values apply at level 1 and grow with the companion's
    // level, which tracks the owner's, so a companion never becomes obsolete.
    private final int baseHp;
    private final float hpPerLevel;
    private final int damageMin;
    private final int damageMax;
    private final float damagePerLevel;
    private final float attackCooldown;
    private final List<SpellEntry> spells;

    public CompanionDef(String id, String displayName, List<Part> parts, String spriteBase,
                        int baseHp, float hpPerLevel, int damageMin, int damageMax,
                        float damagePerLevel, float attackCooldown,
                        List<SpellEntry> spells) {
        this.id = id;
        this.displayName = displayName;
        this.parts = List.copyOf(parts == null ? List.of() : parts);
        this.spriteBase = spriteBase;
        this.baseHp = baseHp;
        this.hpPerLevel = hpPerLevel;
        this.damageMin = damageMin;
        this.damageMax = damageMax;
        this.damagePerLevel = damagePerLevel;
        this.attackCooldown = attackCooldown;
        this.spells = List.copyOf(spells == null ? List.of() : spells);
    }

    /** Maximum hit points for a companion serving an owner of the given level. */
    public int resolveMaxHp(int level) {
        return Math.max(1, baseHp + Math.round(hpPerLevel * (Math.max(1, level) - 1)));
    }

    /** One body-part sprite layer, mirroring {@link NpcDef.Part}. */
    @Getter
    public static final class Part {
        private final BodyPart bodyPart;
        private final String spriteBase;

        public Part(BodyPart bodyPart, String spriteBase) {
            this.bodyPart = bodyPart;
            this.spriteBase = spriteBase;
        }
    }

    /**
     * A spell the companion may cast, with the tactical rule that gates it.
     * The AI scans entries by descending priority and casts the first one whose
     * condition holds and whose cooldown elapsed; melee is the fallback.
     */
    @Getter
    public static final class SpellEntry {
        /** Spell key resolved through {@link com.perso.T4C.spell.SpellRegistry}. */
        private final String spellKey;
        private final CompanionSpellTrigger trigger;
        /** Priority: higher wins when several triggers hold at once. */
        private final int priority;
        private final float cooldownSeconds;
        /**
         * Health fraction (0..1) below which a HEAL trigger fires. Ignored by
         * other triggers.
         */
        private final float healthThreshold;
        private final int minDamage;
        private final int maxDamage;
        private final float damagePerLevel;
        /** Maximum cast distance in tiles. */
        private final float rangeTiles;

        public SpellEntry(String spellKey, CompanionSpellTrigger trigger, int priority,
                          float cooldownSeconds, float healthThreshold,
                          int minDamage, int maxDamage, float damagePerLevel, float rangeTiles) {
            this.spellKey = spellKey;
            this.trigger = trigger;
            this.priority = priority;
            this.cooldownSeconds = cooldownSeconds;
            this.healthThreshold = healthThreshold;
            this.minDamage = minDamage;
            this.maxDamage = maxDamage;
            this.damagePerLevel = damagePerLevel;
            this.rangeTiles = rangeTiles;
        }

        /** Rolled effect amount (damage or healing) at the given level. */
        public int rollAmount(java.util.random.RandomGenerator random, int level) {
            int low = Math.min(minDamage, maxDamage);
            int high = Math.max(minDamage, maxDamage);
            int base = low >= high ? low : low + random.nextInt(high - low + 1);
            return Math.max(0, base + Math.round(damagePerLevel * (Math.max(1, level) - 1)));
        }
    }
}
