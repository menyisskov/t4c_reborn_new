package com.perso.T4C.npc.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.combat.SeraphAuraService;
import com.perso.T4C.config.GameConstants;
import org.junit.jupiter.api.Test;

/** T4C-0026: the per-rebirth numbers the compendium's Rebirths page publishes. */
class RebirthBehaviorTableTest {
  @Test
  void firstAndLastRebirthNumbers() {
    assertEquals(75, RebirthBehavior.requiredLevelFor(1));
    assertEquals(25, RebirthBehavior.startingAttributeFor(1));
    assertEquals(10, RebirthBehavior.energyPointsFor(1));

    int last = GameConstants.REBIRTH_MAX_REMORTS;
    assertEquals(320, RebirthBehavior.requiredLevelFor(last));
    assertEquals(270, RebirthBehavior.startingAttributeFor(last));
    assertEquals(255, RebirthBehavior.energyPointsFor(last));
  }

  @Test
  void everyRebirthIsReachableBelowTheLevelCap() {
    for (int n = 1; n <= GameConstants.REBIRTH_MAX_REMORTS; n++)
      assertTrue(
          RebirthBehavior.requiredLevelFor(n) <= GameConstants.MAX_PLAYER_LEVEL, "rebirth " + n);
  }

  @Test
  void auraChancesClimbAndCapAtOneHundred() {
    assertEquals(0, SeraphAuraService.healingChance(0));
    assertEquals(5, SeraphAuraService.healingChance(1));
    assertEquals(5, SeraphAuraService.retaliationChance(1));
    assertEquals(100, SeraphAuraService.retaliationChance(20));
    assertEquals(100, SeraphAuraService.retaliationChance(50));
    assertEquals(50, SeraphAuraService.areaBurstChance(50));
  }

  @Test
  void effectivePercentMatchesTheInclusiveHundredRoll() {
    assertEquals(0d, SeraphAuraService.effectivePercent(0));
    assertEquals(2.0d, SeraphAuraService.effectivePercent(1)); // 2/101
    assertEquals(5.9d, SeraphAuraService.effectivePercent(5)); // 6/101
    assertEquals(50.5d, SeraphAuraService.effectivePercent(50)); // 51/101
    assertEquals(100d, SeraphAuraService.effectivePercent(100));
    assertEquals(100d, SeraphAuraService.effectivePercent(120));
  }
}
