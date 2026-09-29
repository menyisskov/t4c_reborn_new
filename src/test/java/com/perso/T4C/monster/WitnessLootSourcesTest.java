package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.ItemSalePricing;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.spawn.SpawnRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WitnessLootSourcesTest {
  @BeforeEach
  void loadItems() {
    ItemJsonLoader.loadAndRegister("assets/items");
  }

  @AfterEach
  void resetItems() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  @Test
  void everyWitnessItemHasAReachableTierAppropriateDropSource() throws Exception {
    Set<String> liveNames = new HashSet<>();
    SpawnRegistry.monsters().stream().filter(s -> s.z() == 0).forEach(s -> liveNames.add(s.type()));
    List<MonsterDef> liveMonsters =
        MonsterRegistry.load().stream().filter(def -> liveNames.contains(def.getName())).toList();
    List<String> keys;
    try (var files = Files.list(Path.of("assets/items"))) {
      keys =
          files
              .filter(p -> p.getFileName().toString().matches("witness_.*\\.json"))
              .map(p -> p.getFileName().toString().replaceFirst("\\.json$", ""))
              .toList();
    }
    assertEquals(51, keys.size());
    for (String key : keys) {
      ItemDefinition item = ItemRegistry.findByKey(key);
      assertNotNull(item, key);
      assertTrue(ItemSalePricing.sellPrice(item) > 1, key);
      String element = key.split("_")[1];
      int minLevel = minimumLevel(element);
      int maxLevel = maximumLevel(element);
      assertTrue(
          liveMonsters.stream()
              .filter(def -> def.getLevel() >= minLevel && def.getLevel() <= maxLevel)
              .flatMap(def -> def.getLoot().stream())
              .anyMatch(drop -> key.equals(drop.getItem()) && drop.getChance() > 0f),
          key + " needs a reachable drop source in its campaign tier");
    }
  }

  private static int minimumLevel(String element) {
    return switch (element) {
      case "fire" -> 200;
      case "water" -> 230;
      case "air" -> 260;
      case "earth" -> 300;
      case "light" -> 340;
      case "dark" -> 375;
      default -> throw new IllegalArgumentException(element);
    };
  }

  private static int maximumLevel(String element) {
    return switch (element) {
      case "fire" -> 260;
      case "water" -> 270;
      case "air" -> 300;
      case "earth" -> 345;
      case "light" -> 380;
      case "dark" -> 400;
      default -> throw new IllegalArgumentException(element);
    };
  }
}
