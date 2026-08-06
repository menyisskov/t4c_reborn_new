package com.perso.T4C.combat;

import org.junit.jupiter.api.Test;

import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Checks the port of GAME_RULES::HPregen and GAME_RULES::ManaRegen. */
class RegenerationRulesTest {

    /**
     * Returns the lowest value of every requested range, so rnd(1,100) yields 1
     * (the 60% gate always opens) and rnd(0,n) yields 0.
     *
     * <p>{@code nextInt(origin, bound)} is overridden directly: the default
     * implementation resamples until it gets an in-range value, which never
     * terminates against a constant {@code nextLong()}.
     */
    private static final RandomGenerator MINIMUM_ROLL = new RandomGenerator() {
        @Override
        public long nextLong() {
            return 0L;
        }

        @Override
        public int nextInt(int origin, int bound) {
            return origin;
        }
    };

    /** Returns the highest value of every range, so rnd(1,100) yields 100 and closes the gate. */
    private static final RandomGenerator MAXIMUM_ROLL = new RandomGenerator() {
        @Override
        public long nextLong() {
            return 0L;
        }

        @Override
        public int nextInt(int origin, int bound) {
            return bound - 1;
        }
    };

    @Test
    void hpGainIsAtLeastOneWhenTheChanceGateOpens() {
        assertEquals(51, RegenerationRules.regenerateHp(50, 100, 0, MINIMUM_ROLL));
    }

    @Test
    void hpDoesNotMoveWhenTheChanceGateCloses() {
        assertEquals(50, RegenerationRules.regenerateHp(50, 100, 200, MAXIMUM_ROLL));
    }

    @Test
    void hpGainNeverOvershootsTheMaximum() {
        assertEquals(100, RegenerationRules.regenerateHp(100, 100, 400, MINIMUM_ROLL));
    }

    /** Above the cap the surplus bleeds off by ceil(maxHp * 5%), gate-free. */
    @Test
    void hpAboveMaximumBleedsOffFivePercent() {
        // ceil(100 * 5%) = 5, applied without consulting the chance gate
        assertEquals(145, RegenerationRules.regenerateHp(150, 100, 40, MAXIMUM_ROLL));
    }

    /** ceil() keeps a tiny pool from stalling: ceil(10 * 5%) = 1. */
    @Test
    void hpAboveMaximumAlwaysLosesAtLeastOnePoint() {
        assertEquals(11, RegenerationRules.regenerateHp(12, 10, 40, MAXIMUM_ROLL));
    }

    @Test
    void hpAboveMaximumNeverDropsBelowTheCap() {
        assertEquals(100, RegenerationRules.regenerateHp(101, 100, 40, MAXIMUM_ROLL));
    }

    @Test
    void manaGainIsAtLeastOneWhenTheChanceGateOpens() {
        assertEquals(31, RegenerationRules.regenerateMana(30, 100, 0, 0, MINIMUM_ROLL));
    }

    /** INT and WIS each contribute value/160 twice: a fixed step plus a roll. */
    @Test
    void manaGainScalesWithIntelligenceAndWisdom() {
        // intSteps = 320/160 = 2, wisSteps = 160/160 = 1, rolls are 0
        // gain = 0 + 2 + 0 + 1 + 1 = 4
        assertEquals(34, RegenerationRules.regenerateMana(30, 100, 320, 160, MINIMUM_ROLL));
    }

    @Test
    void manaAboveMaximumBleedsOffTwoPercent() {
        assertEquals(148, RegenerationRules.regenerateMana(150, 100, 0, 0, MAXIMUM_ROLL));
    }

    @Test
    void manaAtMaximumStaysPut() {
        assertEquals(100, RegenerationRules.regenerateMana(100, 100, 320, 320, MINIMUM_ROLL));
    }

    /** (points + 25) / 25 in integer arithmetic: one whole step per 25 points. */
    @Test
    void regenerationMultiplierStepsEveryTwentyFivePoints() {
        assertEquals(1, RegenerationRules.regenerationMultiplier(0));
        assertEquals(1, RegenerationRules.regenerationMultiplier(24));
        assertEquals(2, RegenerationRules.regenerationMultiplier(25));
        assertEquals(2, RegenerationRules.regenerationMultiplier(49));
        assertEquals(3, RegenerationRules.regenerationMultiplier(50));
    }

    @Test
    void unlearnedSkillNeverShrinksTheNaturalRoll() {
        assertEquals(1, RegenerationRules.regenerationMultiplier(-10));
    }

    /** FastHealing.cpp multiplies the rolled gain, not the resulting pool. */
    @Test
    void rapidHealingMultipliesTheHpGain() {
        // base gain = rnd(0, 0) + 1 = 1, multiplier at 50 points = 3
        assertEquals(53, RegenerationRules.regenerateHp(50, 100, 0, 50, MINIMUM_ROLL));
    }

    /** Meditate.cpp applies the same scaling to the mana roll. */
    @Test
    void meditateMultipliesTheManaGain() {
        assertEquals(33, RegenerationRules.regenerateMana(30, 100, 0, 0, 50, MINIMUM_ROLL));
    }

    @Test
    void rapidHealingGainStillStopsAtTheMaximum() {
        assertEquals(100, RegenerationRules.regenerateHp(99, 100, 400, 200, MINIMUM_ROLL));
    }
}
