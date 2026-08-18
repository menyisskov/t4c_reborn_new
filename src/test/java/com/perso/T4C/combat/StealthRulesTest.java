package com.perso.T4C.combat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class StealthRulesTest {
  private static final RandomGenerator MINIMUM_ROLL =
      new RandomGenerator() {
        @Override
        public long nextLong() {
          return 0L;
        }

        @Override
        public int nextInt(int bound) {
          return 0;
        }
      };
  private static final RandomGenerator MAXIMUM_ROLL =
      new RandomGenerator() {
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

  @Test
  void theFirstWitnessCostsNothing() {
    assertTrue(StealthRules.staysHidden(15, 0, 1, MINIMUM_ROLL));
  }

  @Test
  void extraWitnessesErodeTheSuccessChance() {
    assertFalse(StealthRules.staysHidden(15, 0, 3, MINIMUM_ROLL));
  }

  @Test
  void agilityContributesOneSixthOfItsValue() {
    assertTrue(StealthRules.staysHidden(0, 60, 0, MINIMUM_ROLL));
  }
}
