package com.perso.T4C.combat;

import java.util.random.RandomGenerator;

/**
 * Stealth upkeep, ported from Sneak.cpp.
 *
 * <p>Sneak is not an activation skill: it hooks HOOK_MOVE and only runs while the
 * unit is already hidden. Every step rolls against the skill, and a failed roll
 * unhides the unit. Nearby witnesses make the roll harder.
 */
public final class StealthRules {

    /** Sneak.cpp queries local units within this range before rolling. */
    public static final int WITNESS_RANGE = 40;

    private StealthRules() {
    }

    /**
     * Sneak.cpp: {@code nSuccess = sneakSkill + AGI / 6 - (witnesses - 1) * 10},
     * and the unit stays hidden while {@code rnd(1, 100) < nSuccess}.
     *
     * @param sneakSkill points in the sneak skill
     * @param agility    the unit's AGI
     * @param witnesses  units within {@link #WITNESS_RANGE}, excluding the sneaker
     * @param random     source of randomness
     * @return {@code true} when the unit keeps its cover this step
     */
    public static boolean staysHidden(int sneakSkill, int agility, int witnesses, RandomGenerator random) {
        int penalty = witnesses <= 0 ? 0 : (witnesses - 1) * 10;
        int success = Math.max(0, sneakSkill) + Math.max(0, agility) / 6 - penalty;
        return roll(random, 100) < success;
    }

    private static int roll(RandomGenerator random, int faces) {
        return faces <= 1 ? 1 : random.nextInt(faces) + 1;
    }
}
