package com.perso.T4C.harvest;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.ItemRegistry;
import org.junit.jupiter.api.Test;

class HerbManagerTest {
  @Test
  void activeHerbRemainsInTheViewportWhileCameraMoves() {
    HerbDefinition definition = new HerbDefinition("test", "item.test", "64kTest", 1);
    HerbNode node = new HerbNode(definition, 240, 652, 0f, 0f);
    assertTrue(HerbManager.isInsideViewport(node, 230, 250, 640, 660));
    assertTrue(HerbManager.isInsideViewport(node, 231, 251, 641, 661));
    assertFalse(HerbManager.isInsideViewport(node, 241, 261, 641, 661));
  }

  @Test
  void acceptsOnlyConfiguredGrassNames() {
    assertTrue(HerbManager.isAllowedGround("Grass"));
    assertTrue(HerbManager.isAllowedGround("64kNormalGrass"));
    assertTrue(HerbManager.isAllowedGround("64kNormalGrass (9, 15)"));
    assertTrue(HerbManager.isAllowedGround("Grass (2, 4)"));
    assertTrue(HerbManager.isAllowedGround("grass"));
    assertFalse(HerbManager.isAllowedGround("64kDarkGrass"));
    assertFalse(HerbManager.isAllowedGround(null));
  }

  @Test
  void everyHerbHasAnInventoryDefinition() {
    HerbRegistry.invalidate();
    ItemRegistry.invalidate();
    assertFalse(HerbRegistry.load().isEmpty());
    for (HerbDefinition herb : HerbRegistry.load()) {
      assertNotNull(ItemRegistry.findByKey(herb.getItemKey()));
      assertFalse(herb.getWorldSprite().isBlank());
      assertTrue(herb.getSpawnWeight() > 0);
    }
  }
}
