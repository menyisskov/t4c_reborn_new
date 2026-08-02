package com.perso.T4C.combat;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * Rules for spell 10696, the permanent seraph/remort aura.
 *
 * <p>The formulas intentionally preserve the original WDA behaviour, including
 * its two healing values and its inclusive {@code 0..100 <= chance} roll.</p>
 */
public final class SeraphAuraService {
    public static final int AURA_SPELL_ID = 10696;
    public static final int SINGLE_BLAST_SPELL_ID = 10697;
    public static final int AREA_BLAST_SPELL_ID = 10698;
    public static final int HEAL_SPELL_ID = 10699;

    public static final String AURA_NAME = "spell.remort_aura";
    private static final String LEGACY_AURA_NAME = "Remort aura";
    /** English fallback; the displayed text comes from {@code spell.description.remort_aura}. */
    public static final String AURA_DESCRIPTION =
            "Permanent offensive and defensive aura only usable by seraphs "
                    + "(effectiveness increases with each rebirth).";
    public static final String AURA_ICON = "64kSpellIconNoneDefense";

    public static final String SINGLE_PROJECTILE = "64kSpellEnergyBall-";
    public static final String SINGLE_IMPACT = "SmallExplosion-";
    public static final String AREA_CENTER_IMPACT = "GreatExplosion-";
    public static final String AREA_PROJECTILE = "64kSpellFireBall";
    public static final String HEAL_IMPACT = "HealingSpell-";
    public static final String HEAL_RADIAL_EFFECT = "64kSpellEnergyBallWhite-";
    public static final String HEAL_SOUND = "Healing.wav";
    public static final String EXPLOSION_SOUND = "Explosion.wav";
    public static final String FIREBALL_SOUND = "FireBall 2.wav";

    public static final int AREA_RADIUS = 6;
    public static final int FIRE_RESIST_IMMUNITY = 5000;
    public static final double MAX_DAMAGEABLE_ARMOR_CLASS = 64_999d;

    private static final String WHITE_WING_PREFIX = "PupSeraphWhiteWings";
    private static final String BLACK_WING_PREFIX = "PupSeraphBlackWings";

    private SeraphAuraService() {
    }

    /**
     * Restores the permanent aura after loading a player.
     *
     * <p>Old Java saves did not store flag 30419. For those saves, equipped
     * original seraph wings are used once to infer the first rebirth. Once the
     * count is known, removing the wings does not remove the aura.</p>
     *
     * @return {@code true} only when the synchronization changed player state
     */
    public static boolean synchronize(Player player) {
        Objects.requireNonNull(player, "player");

        boolean changed = false;
        // Collapse the pre-canonical English identity left by older saves.
        while (player.dispelBuff(LEGACY_AURA_NAME)) {
            changed = true;
        }
        if (player.getRebirthCount() <= 0 && hasSeraphWings(player)) {
            player.setRebirthCount(1);
            changed = true;
        }
        if (player.getRebirthCount() <= 0) {
            while (player.dispelBuff(AURA_NAME)) {
                changed = true;
            }
            return changed;
        }
        if (!player.hasBuff(AURA_NAME)) {
            player.applyBuff(AURA_NAME, AURA_DESCRIPTION, AURA_ICON, null, true);
            changed = true;
        }
        return changed;
    }

    /** Detects the original white/black seraph appearance in the BACK slot. */
    public static boolean hasSeraphWings(Player player) {
        if (player == null || player.getEquippedItems() == null) {
            return false;
        }
        String itemKey = player.getEquippedItems().get(BodyPart.BACK);
        if (itemKey == null || itemKey.isBlank()) {
            return false;
        }
        // Some legacy saves stored the puppet appearance directly rather than
        // an object-definition key.
        if (isSeraphWingAppearance(itemKey)) {
            return true;
        }
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        return definition != null
                && isSeraphWingAppearance(definition.getAppearanceEquippedFor(BodyPart.BACK));
    }

    /** Pure appearance predicate used both by save migration and tests. */
    public static boolean isSeraphWingAppearance(String appearance) {
        return appearance != null
                && (startsWithIgnoreCase(appearance, WHITE_WING_PREFIX)
                || startsWithIgnoreCase(appearance, BLACK_WING_PREFIX));
    }

    public static OnHitResult onHit(Player player, int attackerFireResistance) {
        return onHit(player, attackerFireResistance, ThreadLocalRandom.current());
    }

    public static OnHitResult onHit(Player player, int attackerFireResistance, RandomGenerator random) {
        return onHit(player, attackerFireResistance, 0d, random);
    }

    public static OnHitResult onHit(Player player, int attackerFireResistance,
                                    double attackerArmorClass, RandomGenerator random) {
        Objects.requireNonNull(player, "player");
        return onHit(player.getRebirthCount(), EffectiveStats.from(player), attackerFireResistance,
                attackerArmorClass, random);
    }

    /** Resolves the two independent OnHit procs (heal 10699 and blast 10697). */
    public static OnHitResult onHit(int rebirthCount, EffectiveStats stats,
                                    int attackerFireResistance, RandomGenerator random) {
        return onHit(rebirthCount, stats, attackerFireResistance, 0d, random);
    }

    public static OnHitResult onHit(int rebirthCount, EffectiveStats stats,
                                    int attackerFireResistance, double attackerArmorClass,
                                    RandomGenerator random) {
        Objects.requireNonNull(stats, "stats");
        Objects.requireNonNull(random, "random");
        if (rebirthCount <= 0) {
            return new OnHitResult(false, 0, 0, false, 0);
        }

        boolean healingTriggered = succeeds(rebirthCount + 4, random);
        int centralHealing = 0;
        int radialHealing = 0;
        if (healingTriggered) {
            int baseHealing = stats.sum() / 5;
            // Both WDA parameters are evaluated. The original therefore rolls
            // two separate d5 values and heals the central target twice.
            centralHealing = baseHealing + d5(random);
            radialHealing = baseHealing + d5(random);
        }

        boolean retaliationTriggered = succeeds(rebirthCount * 5, random);
        int retaliationDamage = 0;
        if (retaliationTriggered && attackerFireResistance < FIRE_RESIST_IMMUNITY) {
            int rolledDamage = stats.sum() / 10 + d5(random);
            // HealthEffect evaluates the WDA expression first, then applies
            // the server's high-AC spell-immunity sentinel.
            if (attackerArmorClass <= MAX_DAMAGEABLE_ARMOR_CLASS) {
                retaliationDamage = rolledDamage;
            }
        }
        return new OnHitResult(healingTriggered, centralHealing, radialHealing,
                retaliationTriggered, retaliationDamage);
    }

    public static OnAttackHitResult onAttackHit(Player player) {
        return onAttackHit(player, ThreadLocalRandom.current());
    }

    public static OnAttackHitResult onAttackHit(Player player, RandomGenerator random) {
        Objects.requireNonNull(player, "player");
        return onAttackHit(player.getRebirthCount(), random);
    }

    /**
     * Rolls spell 10698 once for the attack. Damage is deliberately resolved
     * later, once per target, through {@link #rollAreaDamage}.
     */
    public static OnAttackHitResult onAttackHit(int rebirthCount, RandomGenerator random) {
        Objects.requireNonNull(random, "random");
        return new OnAttackHitResult(succeeds(rebirthCount, random), AREA_RADIUS);
    }

    public static int rollAreaDamage(Player player, int targetFireResistance) {
        return rollAreaDamage(player, targetFireResistance, ThreadLocalRandom.current());
    }

    public static int rollAreaDamage(Player player, int targetFireResistance, RandomGenerator random) {
        return rollAreaDamage(player, targetFireResistance, 0d, random);
    }

    public static int rollAreaDamage(Player player, int targetFireResistance,
                                     double targetArmorClass, RandomGenerator random) {
        Objects.requireNonNull(player, "player");
        return rollAreaDamage(EffectiveStats.from(player), targetFireResistance, targetArmorClass, random);
    }

    /** Resolves the 10698 damage expression separately for each target. */
    public static int rollAreaDamage(EffectiveStats stats, int targetFireResistance, RandomGenerator random) {
        return rollAreaDamage(stats, targetFireResistance, 0d, random);
    }

    public static int rollAreaDamage(EffectiveStats stats, int targetFireResistance,
                                     double targetArmorClass, RandomGenerator random) {
        Objects.requireNonNull(stats, "stats");
        Objects.requireNonNull(random, "random");
        if (targetFireResistance >= FIRE_RESIST_IMMUNITY) {
            return 0;
        }
        int rolledDamage = stats.sum() / 5 - d5(random);
        return targetArmorClass > MAX_DAMAGEABLE_ARMOR_CLASS ? 0 : rolledDamage;
    }

    private static boolean succeeds(int chance, RandomGenerator random) {
        // The C++ evaluator uses an inclusive 0..100 roll. Keep the explicit
        // zero guard: otherwise roll 0 would make a zero-percent spell proc.
        return chance > 0 && random.nextInt(101) <= chance;
    }

    private static int d5(RandomGenerator random) {
        return random.nextInt(5) + 1;
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return value.length() >= prefix.length()
                && value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /** The five effective attributes referenced by the original WDA formulas. */
    public record EffectiveStats(int strength, int agility, int endurance,
                                 int intelligence, int wisdom) {
        public static EffectiveStats from(Player player) {
            Objects.requireNonNull(player, "player");
            return new EffectiveStats(player.getEffectiveStrength(), player.getEffectiveDexterity(),
                    player.getEffectiveEndurance(), player.getEffectiveIntelligence(),
                    player.getEffectiveWisdom());
        }

        public int sum() {
            return strength + agility + endurance + intelligence + wisdom;
        }
    }

    /** Result of the two independent effects bound to the aura's OnHit event. */
    public record OnHitResult(boolean healingTriggered, int centralHealing, int radialHealing,
                              boolean retaliationTriggered, int retaliationDamage) {
        public int totalHealing() {
            return centralHealing + radialHealing;
        }
    }

    /** Global spell-10698 proc; its damage must then be rolled per target. */
    public record OnAttackHitResult(boolean triggered, int radius) {
    }
}
