package com.perso.T4C.online.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroundLootModelsTest {
  @Test
  void dropsAndGroundViewsRequireNormalizedKeysAndSaneQuantities() {
    assertThrows(IllegalArgumentException.class,
        () -> new LootDropDraft(UUID.randomUUID(), 0, "Leather gloves", 1, 0, 0, 1, 1));
    assertThrows(IllegalArgumentException.class,
        () -> new LootDropDraft(UUID.randomUUID(), 0, "item.leather_gloves", 0, 0, 0, 1, 1));
    assertThrows(IllegalArgumentException.class,
        () -> new GroundLootView(UUID.randomUUID(), 1, 1, "item.leather_gloves", null, -1));
    // Another owner's window: the server names no owner but still reports its remaining time.
    assertEquals(5, new GroundLootView(UUID.randomUUID(), 1, 1, "item.leather_gloves", null, 5).ownerRemainingMs());
  }
}
