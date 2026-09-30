package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.MonsterManager;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CompendiumWitnessExportTest {
  @AfterEach
  void resetItems() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  @Test
  void witnessItemsExportReadableNames() {
    ItemJsonLoader.loadAndRegister("assets/items");
    List<Map<String, Object>> items =
        CompendiumExporter.exportItems().stream()
            .filter(item -> ((String) item.get("key")).startsWith("witness_"))
            .toList();

    assertEquals(51, items.size());
    for (Map<String, Object> item : items) {
      String name = (String) item.get("name");
      assertNotNull(name);
      assertFalse(name.startsWith("${"), item.get("key") + " has an unresolved name");
    }
    assertTrue(items.stream().anyMatch(item -> "Ember Witness Plate".equals(item.get("name"))));
  }

  @Test
  void huntingMonstersExportTheRuntimeRefillSchedule() {
    Map<String, Object> revenant =
        CompendiumExporter.exportMonsters(Map.of()).stream()
            .filter(monster -> "Moonwake Revenant".equals(monster.get("name")))
            .findFirst()
            .orElseThrow();
    assertEquals(MonsterManager.huntingRespawnMinMillis(), revenant.get("respawnMinMs"));
    assertEquals(MonsterManager.huntingRespawnMaxMillis(), revenant.get("respawnMaxMs"));
    assertEquals(MonsterManager.huntingReentryRespawnMillis(), revenant.get("movementRefillMs"));

    Map<String, Object> boss =
        CompendiumExporter.exportMonsters(Map.of()).stream()
            .filter(monster -> "The Pale Cantor".equals(monster.get("name")))
            .findFirst()
            .orElseThrow();
    assertFalse(boss.containsKey("movementRefillMs"));
  }
}
