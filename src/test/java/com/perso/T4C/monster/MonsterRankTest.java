package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.core.MonsterRank;
import com.perso.T4C.monster.core.MonsterRegistry;
import org.junit.jupiter.api.Test;

class MonsterRankTest {

  @Test
  void uniqueNamedMonstersAreBosses() {
    assertTrue(MonsterRank.isBoss(CentaurKing.CANONICAL_NAME));
    assertTrue(MonsterRank.isBoss(MonsterRegistry.findByName("Arch Drake").getName()));
  }

  @Test
  void commonMonstersAreNotBosses() {
    assertFalse(MonsterRank.isBoss(MonsterRegistry.findByName("Goblin Scout").getName()));
    assertFalse(MonsterRank.isBoss(MonsterRegistry.findByName("Brown Rat").getName()));
  }

  @Test
  void monstersWithoutSpawnPointsAreNotBosses() {
    assertFalse(MonsterRank.isBoss("No Such Monster"));
    assertFalse(MonsterRank.isBoss((String) null));
  }
}
