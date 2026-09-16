package com.perso.T4C.monster.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MonsterJsonLoaderTest {

  @AfterEach
  void resetRegistry() {
    MonsterRegistry.resetToGeneratedDefinitions();
  }

  @Test
  void loadsMonsterDefinitionsFromJsonDirectory() {
    MonsterJsonLoader.loadAndRegister("assets/monsters");

    MonsterDef def = MonsterRegistry.findByName("Sewer Rat");
    assertTrue(def != null, "Sewer Rat should be registered from assets/monsters/sewer_rat.json");
    assertEquals(45, def.getHealth());
    assertEquals(3, def.getLevel());
    assertEquals(1, def.getAttacks().size());
    assertEquals("1d6+2", def.getAttacks().get(0).getName());
    assertTrue(def.canBeTamedBy(12));
  }

  @Test
  void missingNameFailsFast() {
    MonsterJsonDef json = new MonsterJsonDef();
    try {
      json.toMonsterDef();
      throw new AssertionError("Expected IllegalStateException for missing name");
    } catch (IllegalStateException expected) {
      // expected
    }
  }
}
