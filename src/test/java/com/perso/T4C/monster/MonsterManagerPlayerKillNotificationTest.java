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
      super("Brown Rat", 0f, 0f, 10, 0, 0, 0, 1, 1, 1_000L, null, null, null, null, null);
    }
  }
}
