package com.perso.T4C.monster.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.exception.GameException;
import org.junit.jupiter.api.Test;

class MonsterManagerHuntingRespawnTest {
  @Test
  void returningToClearedHuntingAreaRefillsItAfterTheCorpseAnimation() throws Exception {
    MonsterManager manager = new MonsterManager(null);
    TestMonster exile = new TestMonster("Ashbound Exile");
    manager.addMonster(exile);
    manager.updateVisible(0f, null, 0, 0, 0, 0, 0);
    exile.takeDamage(10);
    exile.makeDeathSixSecondsOld();

    manager.updateVisible(1f, null, 100, 101, 100, 101, 0);
    assertTrue(exile.isDead());
    manager.updateVisible(1f, null, 0, 0, 0, 0, 0);

    assertFalse(exile.isDead());
  }

  @Test
  void returningToALegacyAreaDoesNotShortenItsRespawn() throws Exception {
    MonsterManager manager = new MonsterManager(null);
    TestMonster rat = new TestMonster("Brown Rat");
    manager.addMonster(rat);
    manager.updateVisible(0f, null, 0, 0, 0, 0, 0);
    rat.takeDamage(10);
    rat.makeDeathSixSecondsOld();

    manager.updateVisible(1f, null, 100, 101, 100, 101, 0);
    manager.updateVisible(1f, null, 0, 0, 0, 0, 0);

    assertTrue(rat.isDead());
  }

  private static final class TestMonster extends BaseMonster {
    private TestMonster(String name) throws GameException {
      super(name, 0f, 0f, 10, 0, 0, 0, 1, 1, 30_000L, null, null, null, null, null);
    }

    private void makeDeathSixSecondsOld() {
      deathTime = System.currentTimeMillis() - 6_000L;
    }
  }
}
