package com.perso.T4C.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class RegenerationRulesTest {
  private static final RandomGenerator MINIMUM_ROLL =
      new RandomGenerator() {
        @Override
        public long nextLong() {
          return 0L;
        }

        @Override
        public int nextInt(int origin, int bound) {
          return origin;
        }
      };
  private static final RandomGenerator MAXIMUM_ROLL =
      new RandomGenerator() {
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

  @Test
  void hpAboveMaximumBleedsOffFivePercent() {
    assertEquals(145, RegenerationRules.regenerateHp(150, 100, 40, MAXIMUM_ROLL));
  }

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

  @Test
  void manaGainScalesWithIntelligenceAndWisdom() {
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

  @Test
  void rapidHealingMultipliesTheHpGain() {
    assertEquals(53, RegenerationRules.regenerateHp(50, 100, 0, 50, MINIMUM_ROLL));
  }

  @Test
  void meditateMultipliesTheManaGain() {
    assertEquals(33, RegenerationRules.regenerateMana(30, 100, 0, 0, 50, MINIMUM_ROLL));
  }

  @Test
  void rapidHealingGainStillStopsAtTheMaximum() {
    assertEquals(100, RegenerationRules.regenerateHp(99, 100, 400, 200, MINIMUM_ROLL));
  }
}
