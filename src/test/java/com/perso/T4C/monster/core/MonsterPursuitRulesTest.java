package com.perso.T4C.monster.core;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.MONSTER_AGGRO_LEASH_RANGE;
import static com.perso.T4C.config.GameConstants.MONSTER_AGGRO_RANGE;
import static com.perso.T4C.config.GameConstants.MONSTER_RETALIATION_LEASH_RANGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MonsterPursuitRulesTest {
  @Test
  void naturalAcquisitionAndCurrentPositionLeashesKeepTheirInclusiveBoundaries() {
    assertTrue(MonsterPursuitRules.inNaturalAcquisitionRange(true, MONSTER_AGGRO_RANGE, false));
    assertFalse(MonsterPursuitRules.inNaturalAcquisitionRange(
        true, Math.nextUp(MONSTER_AGGRO_RANGE), false));
    assertFalse(MonsterPursuitRules.inNaturalAcquisitionRange(false, 0f, false));
    assertTrue(MonsterPursuitRules.inNaturalAcquisitionRange(true, 12f * GRID_W, true));
    assertFalse(MonsterPursuitRules.inNaturalAcquisitionRange(
        true, Math.nextUp(12f * GRID_W), true));

    assertTrue(MonsterPursuitRules.withinNaturalLeash(MONSTER_AGGRO_LEASH_RANGE));
    assertFalse(MonsterPursuitRules.withinNaturalLeash(Math.nextUp(MONSTER_AGGRO_LEASH_RANGE)));
    assertFalse(MonsterPursuitRules.withinPlayerLeash(
        Math.nextUp(MONSTER_AGGRO_LEASH_RANGE), false));
    assertTrue(MonsterPursuitRules.withinPlayerLeash(
        MONSTER_RETALIATION_LEASH_RANGE, true));
    assertFalse(MonsterPursuitRules.withinPlayerLeash(
        Math.nextUp(MONSTER_RETALIATION_LEASH_RANGE), true));

    assertFalse(MonsterPursuitRules.exceedsHuntingGroundLeash(
        100f * GRID_W, 152f * GRID_H, 100, 100, 52));
    assertTrue(MonsterPursuitRules.exceedsHuntingGroundLeash(
        100f * GRID_W, 153f * GRID_H, 100, 100, 52));
  }

  @Test
  void attackReachUsesAuthoredMaximumButSelectionKeepsRoundedTileDistance() {
    var physical = new MonsterDef.Attack("1d3", 1, 100, 0, 0, 20);
    var spell = new MonsterDef.Attack("", 1, 100, 123, 0, 3);
    assertEquals(GRID_W, MonsterPursuitRules.attackReachPixels(List.of(physical)));
    assertEquals(3f * GRID_W,
        MonsterPursuitRules.attackReachPixels(List.of(physical, spell)));
    assertTrue(MonsterPursuitRules.withinAttackReach(3f * GRID_W, List.of(spell)));
    assertFalse(MonsterPursuitRules.withinAttackReach(
        Math.nextUp(3f * GRID_W), List.of(spell)));
    assertEquals(1, MonsterPursuitRules.tileRange(Math.nextDown(1.5f * GRID_W)));
    assertEquals(2, MonsterPursuitRules.tileRange(1.5f * GRID_W));
  }
}
