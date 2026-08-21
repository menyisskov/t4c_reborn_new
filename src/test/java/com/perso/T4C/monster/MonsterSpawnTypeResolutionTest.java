package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class MonsterSpawnTypeResolutionTest {

  @Test
  void spawnTypeAndDefinitionNameAreIndexedAsIdentities() {
    assertNotNull(MonsterRegistry.findByName("MOBYOGGOTHWORM"));
    assertEquals("Yoggoth Worm", MonsterRegistry.findByName("MOBYOGGOTHWORM").getName());
    assertNotNull(MonsterRegistry.findByName("MOBDARKNOBLE"));
    assertEquals("MOBDARKNOBLE", MonsterRegistry.findByName("MOBDARKNOBLE").getName());
  }

  @Test
  void everyMonsterSpawnTypeResolvesToADefinitionOrNpc() {
    TreeSet<String> unresolved = new TreeSet<>();
    for (SpawnDefinition spawn : SpawnRegistry.monsters()) {
      String type = spawn.type();
      if (type == null || type.isBlank() || "SUNDIAL".equalsIgnoreCase(type)) {
        continue;
      }
      if (MonsterRegistry.findByName(type) == null && NpcFactoryRegistry.find(type) == null) {
        unresolved.add(type);
      }
    }
    assertEquals(new TreeSet<>(), unresolved);
  }
}
