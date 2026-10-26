package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MonsterManagerPlayerKillNotificationTest {
  @Test
  void aPlayerAttributedDeathIsPublishedOnlyOnce() throws Exception {
    MonsterManager manager = new MonsterManager(null);
    AtomicInteger lootEvents = new AtomicInteger();
    AtomicInteger questEvents = new AtomicInteger();
    manager.setLootCallback(ignored -> lootEvents.incrementAndGet());
    manager.setPlayerKillCallback(ignored -> questEvents.incrementAndGet());
    TestMonster monster = new TestMonster();
    monster.takeDamage(10);
    assertTrue(monster.isDead());
    assertEquals(0, questEvents.get(), "an unattributed death must not progress quests");
    manager.notifyKilledByPlayer(monster);
    manager.notifyKilledByPlayer(monster);
    assertEquals(1, lootEvents.get());
    assertEquals(1, questEvents.get());
  }

  private static final class TestMonster extends BaseMonster {
    private TestMonster() throws GameException {
      this(0, 0);
    }

    private TestMonster(int xpPerHit, int xpOnDeath) throws GameException {
      super(
          "Brown Rat",
          0f,
          0f,
          10,
          0,
          xpPerHit,
          xpOnDeath,
          1,
          1,
          1_000L,
          null,
          null,
          null,
          null,
          null);
    }
  }

  @Test
  void combatExperienceDoublesDamageAndLargeKillAwardsExactlyOnce() throws Exception {
    com.perso.T4C.helper.CollisionManager.getInstance().clear();
    var player = new com.perso.T4C.player.Player();
    var monster = new TestMonster(7, 2100000000);
    monster.applyPlayerDamage(1000, player, null);
    assertEquals(4200000140L, player.getCurrentXp(), "Only ten effective damage earns hit XP");
    monster.applyPlayerDamage(1000, player, null);
    assertEquals(4200000140L, player.getCurrentXp(), "A corpse cannot award XP again");
  }
}
