package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.BaseMonster;
import org.junit.jupiter.api.Test;

/** T4C-0080: a dead monster's corpse fades from view a few seconds after death, well before the
 * monster actually respawns. */
class CorpseVisibilityTest {
  @Test
  void corpseStaysVisibleImmediatelyAfterDeath() throws GameException {
    TestMonster monster = new TestMonster();
    monster.takeDamage(100);
    assertTrue(monster.isCorpseVisible());
  }

  @Test
  void corpseHidesWithinFiveSecondsOfDeath() throws GameException {
    TestMonster monster = new TestMonster();
    monster.takeDamage(100);
    monster.forceCorpseHiddenNow();
    assertFalse(monster.isCorpseVisible());
  }

  @Test
  void livingMonsterAlwaysHasAVisibleCorpseState() throws GameException {
    TestMonster monster = new TestMonster();
    assertTrue(monster.isCorpseVisible());
  }

  @Test
  void respawnMakesTheCorpseVisibleAgainAsALivingMonster() throws GameException {
    TestMonster monster = new TestMonster();
    monster.takeDamage(100);
    monster.forceCorpseHiddenNow();
    assertFalse(monster.isCorpseVisible());
    monster.respawn();
    assertTrue(monster.isCorpseVisible());
  }

  private static final class TestMonster extends BaseMonster {
    private TestMonster() throws GameException {
      super("Test monster", 0f, 0f, 10, 0, 0, 0, 1, 1, 1_000L, null, null, null, null, null);
    }

    /** Simulates the corpse-visible window having already elapsed, without a real sleep. */
    private void forceCorpseHiddenNow() {
      corpseHiddenAtMs = System.currentTimeMillis() - 1;
    }
  }
}
