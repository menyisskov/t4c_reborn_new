package com.perso.T4C.combat;

import java.util.random.RandomGenerator;

/**
 * Natural HP and mana regeneration, ported from GAME_RULES::HPregen and
 * GAME_RULES::ManaRegen.
 *
 * <p>Both routines pull the pool back towards its maximum: below the cap they
 * roll a small gain that only lands 60% of the time, above the cap (reachable
 * through buffs that raise then drop the maximum) they bleed the surplus off.
 * PlayerManager.cpp drives them every {@code 2 SECONDS}.
 */
public final class RegenerationRules {

    /** PlayerManager.cpp: {@code lpPlayer->dwRegenTime = 2 SECONDS TDELAY}. */
    public static final float REGEN_INTERVAL_SECONDS = 2f;

    /** Both routines only apply their gain when {@code rnd(1, 100) < 61}. */
    private static final int REGEN_CHANCE_EXCLUSIVE = 61;

    private RegenerationRules() {
    }

    /**
     * FastHealing.cpp and Meditate.cpp scale the natural roll by
     * {@code (skillPoints + 25) / 25} on their HOOK_REGEN pass. Integer division
     * is deliberate: the multiplier stays 1 until the skill reaches 25 points,
     * then steps up one whole factor every 25 points.
     *
     * @param skillPoints points in the regeneration skill, 0 when unlearned
     * @return the multiplier to apply to a natural regeneration roll
     */
    public static int regenerationMultiplier(int skillPoints) {
        return Math.max(1, (Math.max(0, skillPoints) + 25) / 25);
    }

    /**
     * GAME_RULES::HPregen. Returns the new HP value for the given pool.
     *
     * @param currentHp current hit points
     * @param maxHp     maximum hit points
     * @param endurance the unit's END, driving the size of the roll
     * @param random    source of randomness
     * @return the HP value after this regeneration tick
     */
    public static int regenerateHp(int currentHp, int maxHp, int endurance, RandomGenerator random) {
        return regenerateHp(currentHp, maxHp, endurance, 0, random);
    }

    /**
     * GAME_RULES::HPregen with the FastHealing HOOK_REGEN multiplier applied to
     * the rolled gain, as in FastHealing.cpp.
     *
     * @param rapidHealingPoints points in the rapid healing skill, 0 when unlearned
     */
    public static int regenerateHp(int currentHp, int maxHp, int endurance, int rapidHealingPoints,
                                   RandomGenerator random) {
        if (currentHp < maxHp) {
            if (rnd(random, 1, 100) >= REGEN_CHANCE_EXCLUSIVE) {
                return currentHp;
            }
            int gain = (rnd(random, 0, Math.max(0, endurance) / 40) + 1)
                    * regenerationMultiplier(rapidHealingPoints);
            return Math.min(maxHp, currentHp + gain);
        }
        if (currentHp > maxHp) {
            // ceil() guarantees a small pool still loses at least one point.
            int drain = (int) Math.ceil(maxHp * 5d / 100d);
            return Math.max(maxHp, currentHp - drain);
        }
        return currentHp;
    }

    /**
     * GAME_RULES::ManaRegen. Returns the new mana value for the given pool.
     *
     * @param mana         current mana
     * @param maxMana      maximum mana
     * @param intelligence the unit's INT
     * @param wisdom       the unit's WIS
     * @param random       source of randomness
     * @return the mana value after this regeneration tick
     */
    public static int regenerateMana(int mana, int maxMana, int intelligence, int wisdom,
                                     RandomGenerator random) {
        return regenerateMana(mana, maxMana, intelligence, wisdom, 0, random);
    }

    /**
     * GAME_RULES::ManaRegen with the Meditate HOOK_REGEN multiplier applied to the
     * rolled gain. Meditate.cpp only regenerates while the player is actually
     * meditating, so callers pass 0 points when the flag is not set.
     *
     * @param meditatePoints points in the meditate skill while meditating, else 0
     */
    public static int regenerateMana(int mana, int maxMana, int intelligence, int wisdom,
                                     int meditatePoints, RandomGenerator random) {
        if (mana < maxMana) {
            if (rnd(random, 1, 100) >= REGEN_CHANCE_EXCLUSIVE) {
                return mana;
            }
            int intSteps = Math.max(0, intelligence) / 160;
            int wisSteps = Math.max(0, wisdom) / 160;
            int gain = (rnd(random, 0, intSteps) + intSteps
                    + rnd(random, 0, wisSteps) + wisSteps + 1)
                    * regenerationMultiplier(meditatePoints);
            return Math.min(maxMana, mana + gain);
        }
        if (mana > maxMana) {
            int drain = (int) Math.ceil(maxMana * 2d / 100d);
            return Math.max(maxMana, mana - drain);
        }
        return mana;
    }

    /** Random::operator()(min, max), inclusive on both bounds. */
    private static int rnd(RandomGenerator random, int min, int max) {
        return max <= min ? min : random.nextInt(min, max + 1);
    }
}
