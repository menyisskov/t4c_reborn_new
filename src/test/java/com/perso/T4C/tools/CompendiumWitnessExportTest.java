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
  @Test
  void nativeWarriorWeaponsExportBonusesAndTheTwoHandedContract() {
    var tier = com.perso.T4C.item.WarriorEquipment.TIERS.getFirst();
    String key = "item.warrior_" + tier.key() + "_greatsword";
    var item =
        CompendiumExporter.exportItems().stream()
            .filter(row -> key.equals(row.get("key")))
            .findFirst()
            .orElseThrow();
    assertFalse(((List<?>) item.get("boosts")).isEmpty());
    assertEquals(com.perso.T4C.player.BodyPart.WEAPON2, item.get("secondaryBodyPart"));
    assertNotNull(item.get("atkDelay"));
    assertFalse(((String) item.get("name")).startsWith("${"));
  }

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
    assertEquals(
        2L
            * com.perso
                .T4C
                .monster
                .core
                .MonsterRegistry
                .findByName("Moonwake Revenant")
                .getXpOnDeath(),
        revenant.get("xpOnDeath"),
        "The reference must publish the doubled combat reward");

    Map<String, Object> boss =
        CompendiumExporter.exportMonsters(Map.of()).stream()
            .filter(monster -> "The Pale Cantor".equals(monster.get("name")))
            .findFirst()
            .orElseThrow();
    assertFalse(boss.containsKey("movementRefillMs"));
  }

  @Test
  void divineTrialsExportCurrentHealthAttacksAndResolvableRewards() {
    ItemJsonLoader.loadAndRegister("assets/items");
    var itemKeys =
        CompendiumExporter.exportItems().stream()
            .map(item -> com.perso.T4C.item.ItemDefinition.normalizeKey((String) item.get("key")))
            .collect(java.util.stream.Collectors.toSet());
    var monsters = CompendiumExporter.exportMonsters(Map.of());
    for (String name : List.of("Divine Artherk", "Divine Anubis")) {
      var monster =
          monsters.stream().filter(row -> name.equals(row.get("name"))).findFirst().orElseThrow();
      assertEquals(
          com.perso.T4C.monster.core.MonsterRegistry.findByName(name).getHealth(),
          monster.get("health"));
      @SuppressWarnings("unchecked")
      var loot = (List<Map<String, Object>>) monster.get("loot");
      for (var drop : loot) {
        assertTrue(
            itemKeys.contains(
                com.perso.T4C.item.ItemDefinition.normalizeKey((String) drop.get("item"))),
            name + ": " + drop.get("item"));
      }
    }
  }
}
