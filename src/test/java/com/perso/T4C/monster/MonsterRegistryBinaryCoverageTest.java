package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MonsterRegistryBinaryCoverageTest {

  @Test
  void everyMonstersBinDefinitionCreatesItsDedicatedRuntimeClass() throws Exception {
    var definitions = MonsterRegistry.load();
    assertFalse(definitions.isEmpty(), "monsters.bin must contain definitions");

    Set<String> names = new HashSet<>();
    List<String> unspecialized = new ArrayList<>();
    for (MonsterDef definition : definitions) {
      assertNotNull(definition);
      assertNotNull(definition.getName());
      assertTrueUnique(names, definition.getName());

      BaseMonster runtime = MonsterRegistry.create(definition, 100f, 100f);
      assertNotNull(runtime, definition.getName());
      if (runtime.getClass() == DataMonster.class) unspecialized.add(definition.getName());
      assertEquals(definition.getName(), runtime.getCanonicalName());

      DataMonster dataMonster = (DataMonster) runtime;

      assertSame(definition, dataMonster.getDefinition());
      assertEquals(definition.getHealth(), runtime.getMaxHealth());
      assertEquals(definition.getMana(), runtime.getMaxMana());
      assertEquals(definition.getLoot(), dataMonster.getDefinition().getLoot());
      assertEquals(definition.getSpawnAliases(), dataMonster.getDefinition().getSpawnAliases());

      for (String alias : definition.getSpawnAliases()) {
        assertSame(
            definition,
            MonsterRegistry.findByName(alias),
            "Spawn alias does not resolve: " + alias);
      }
    }
    assertEquals(List.of(), unspecialized, "Unspecialized monsters.bin definitions");
  }

  private static void assertTrueUnique(Set<String> names, String name) {
    if (!names.add(name)) {
      throw new AssertionError("Duplicate monsters.bin definition: " + name);
    }
  }
}
