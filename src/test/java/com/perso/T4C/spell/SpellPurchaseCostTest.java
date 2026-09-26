package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.config.GameConstants;
import org.junit.jupiter.api.Test;

class SpellPurchaseCostTest {
  @Test
  void neverBelowFiveOrAboveOneHundred() {
    for (int level = 0; level <= GameConstants.MAX_PLAYER_LEVEL; level++) {
      int cost = SpellPurchaseCost.skillPointsForLevel(level);
      assertTrue(cost >= 5, "cost below floor at level " + level);
      assertTrue(cost <= 100, "cost above cap at level " + level);
    }
  }

  @Test
  void risesWithLevelAndCapsAtTheLevelCap() {
    assertEquals(5, SpellPurchaseCost.skillPointsForLevel(2));
    assertEquals(29, SpellPurchaseCost.skillPointsForLevel(100));
    assertEquals(100, SpellPurchaseCost.skillPointsForLevel(GameConstants.MAX_PLAYER_LEVEL));
  }

  @Test
  void neverExceedsCapEvenPastTheLevelCap() {
    assertEquals(100, SpellPurchaseCost.skillPointsForLevel(GameConstants.MAX_PLAYER_LEVEL + 100));
  }
}
