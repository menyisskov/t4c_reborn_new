package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.core.MonsterDef;
import org.junit.jupiter.api.Test;

class MakrshPtangh2AttackRangeTest {

  @Test
  void originalSpellAttacksReachTwentyFiveTilesAndMeleeStaysClose() {
    MonsterDef definition = MakrshPtangh2.definition();
    MonsterDef.Attack physical = definition.getAttacks().get(0);
    MonsterDef.Attack boulders = definition.getAttacks().get(1);

    assertFalse(physical.isSpell());
    assertTrue(physical.isInRange(1));
    assertFalse(physical.isInRange(8));

    assertEquals(10708, boulders.getSpellId());
    assertEquals(0, boulders.getMinRangeTiles());
    assertEquals(25, boulders.getMaxRangeTiles());
    assertTrue(boulders.isInRange(20));
    assertFalse(boulders.isInRange(26));
  }
}
