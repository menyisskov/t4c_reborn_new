package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.monster.MonsterRegistry;
import org.junit.jupiter.api.Test;

class TameableMonsterMigrationTest {
  @Test
  void packWolfCanBeTamedByAPlayerOfItsLevel() {
    MonsterDef source = MonsterRegistry.findByName("Pack Wolf");
    MonsterDef migrated = TameableMonsterMigration.migrate(java.util.List.of(source)).get(0);
    assertTrue(migrated.isTameable());
    assertEquals(40, migrated.getTameMaxLevel());
    assertTrue(migrated.canBeTamedBy(40));
  }
}
