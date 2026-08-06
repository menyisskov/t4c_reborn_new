package com.perso.T4C.combat;

import org.junit.jupiter.api.Test;

import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the port of Sneak.cpp's HOOK_MOVE upkeep roll. */
class StealthRulesTest {

    /** rnd(1,100) yields 1, the most favourable roll. */
    private static final RandomGenerator MINIMUM_ROLL = new RandomGenerator() {
        @Override
        public long nextLong() {
            return 0L;
        }

        @Override
        public int nextInt(int bound) {
            return 0;
        }
    };

    /** rnd(1,100) yields 100, the least favourable roll. */
    private static final RandomGenerator MAXIMUM_ROLL = new RandomGenerator() {
        @Override
        public long nextLong() {
            return 0L;
        }

        @Override
        public int nextInt(int bound) {
            return bound - 1;
        }
    };

    @Test
    void anUnskilledSneakAlwaysLosesCover() {
        // success = 0 + 0/6 - 0 = 0, and rnd(1,100) is never < 0
        assertFalse(StealthRules.staysHidden(0, 0, 0, MINIMUM_ROLL));
    }

    @Test
    void aStrongSneakKeepsCoverOnAGoodRoll() {
        assertTrue(StealthRules.staysHidden(80, 60, 0, MINIMUM_ROLL));
    }

    @Test
    void evenAStrongSneakFailsOnTheWorstRoll() {
        assertFalse(StealthRules.staysHidden(80, 60, 0, MAXIMUM_ROLL));
    }

    /** A lone witness costs nothing: the penalty is (witnesses - 1) * 10. */
    @Test
    void theFirstWitnessCostsNothing() {
        assertTrue(StealthRules.staysHidden(15, 0, 1, MINIMUM_ROLL));
    }

    @Test
    void extraWitnessesErodeTheSuccessChance() {
        // success = 15 + 0 - (3 - 1) * 10 = -5, so cover is always lost
        assertFalse(StealthRules.staysHidden(15, 0, 3, MINIMUM_ROLL));
    }

    @Test
    void agilityContributesOneSixthOfItsValue() {
        // success = 0 + 60/6 - 0 = 10, a roll of 1 stays under it
        assertTrue(StealthRules.staysHidden(0, 60, 0, MINIMUM_ROLL));
    }
}
