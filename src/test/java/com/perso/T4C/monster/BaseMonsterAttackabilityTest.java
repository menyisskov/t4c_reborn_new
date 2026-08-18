package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.BaseMonster;
import org.junit.jupiter.api.Test;

class BaseMonsterAttackabilityTest {
  @Test
  void passiveMonsterRemainsAttackableWithoutCombatMode() throws GameException {
    TestMonster monster = new TestMonster();
    monster.makePassive();
    assertTrue(monster.canBeAttackedByPlayer());
  }

  @Test
  void stationaryNonCombatEntityRemainsProtected() throws GameException {
    TestMonster monster = new TestMonster();
    monster.makePassive();
    monster.setStationary(true);
    assertFalse(monster.canBeAttackedByPlayer());
  }

  private static final class TestMonster extends BaseMonster {
    private TestMonster() throws GameException {
      super("Test monster", 0f, 0f, 10, 0, 0, 0, 1, 1, 1_000L, null, null, null, null, null);
    }

    private void makePassive() {
      setAggressive(false);
    }
  }
}
