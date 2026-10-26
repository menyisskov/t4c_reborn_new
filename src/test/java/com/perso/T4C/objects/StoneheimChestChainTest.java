package com.perso.T4C.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class StoneheimChestChainTest {
  @Test
  void lostHelmChestsContainTheNextKeyAndRequireTheCurrentOne() {
    String[] chests = {
      "ordinary_chest", "rusted_chest", "silver_chest", "bone_chest", "steel_safe"
    };
    String[] rewards = {
      "shiny_metal_key", "dark_iron_key", "chipped_bone_key", "steel_safe_key", "secret_document"
    };
    for (int i = 0; i < chests.length; i++) {
      String expectedReward = ItemDefinition.normalizeKey(rewards[i]);
      ItemDefinition chest = ItemRegistry.findByKey(chests[i]);
      assertTrue(
          chest.getContainerLootGroups().stream()
              .flatMap(group -> group.getItemKeys().stream())
              .map(ItemDefinition::normalizeKey)
              .anyMatch(expectedReward::equals));
      if (i > 0) {
        assertEquals(
            ItemDefinition.normalizeKey(rewards[i - 1]),
            ItemDefinition.normalizeKey(chest.getLockName()));
        ChestService.Result result =
            new ChestService().open(new Player(), new ObjectPos(chests[i], 1, 1, 0), null);
        assertEquals(ChestService.Failure.LOCKED, result.failure());
      }
    }
  }

  @Test
  void steelSafeOpensWithItsKey() {
    Player player = new Player();
    player.setStrength(400);
    assertTrue(InventoryService.add(player, "steel_safe_key").success());

    ChestService.Result result =
        new ChestService().open(player, new ObjectPos("steel_safe", 1, 1, 0), new GroundItemManager());

    assertTrue(result.opened());
    assertEquals("Secret document", result.itemKeys().getFirst());
  }
}
