package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import org.junit.jupiter.api.Test;

class WdaItemDefinitionMigrationTest {
  @Test
  void skeletonShieldUsesOnlyItsVettedPlayableDonor() {
    ItemDefinition skeleton = require("Skeleton shield");
    assertEquals(5d, skeleton.getArmorClass());
    assertEquals(20L, skeleton.getMinEnd());
    assertEquals(2L, skeleton.getWeight());
  }

  @Test
  void sharedAppearanceDoesNotLeakUnrelatedGameplayStats() {
    assertZeroGameplay("Cloth vest");
    assertZeroGameplay("Body cloth");
    assertZeroGameplay("Shaman helm");
    assertZeroGameplay("Centaur Shield 1");
    assertZeroGameplay("Centaur shield 2");
    assertZeroGameplay("Skaven shield 1");
    assertZeroGameplay("Skaven shield 2");
    assertZeroGameplay("Skaven shield 3");
  }

  @Test
  void allWdaEquipmentBoostsAreStoredInTheBinaryRegistry() {
    int count = ItemRegistry.load().stream().mapToInt(item -> item.getBoosts().size()).sum();
    assertEquals(929, count);
    ItemDefinition sword = require("Fine steel short sword 2");
    assertEquals(1, sword.getBoosts().size());
    assertEquals(296, sword.getBoosts().get(0).getBoostId());
    assertEquals(8, sword.getBoosts().get(0).getStatId());
    assertEquals("self.true_attack*30/100", sword.getBoosts().get(0).getExpression());
  }

  private static void assertZeroGameplay(String key) {
    ItemDefinition item = require(key);
    assertEquals(0d, item.getArmorClass(), key);
    assertEquals(0L, item.getMinEnd(), key);
    assertEquals(0L, item.getReqStr(), key);
    assertEquals(0L, item.getReqAgi(), key);
    assertEquals(0L, item.getReqAttack(), key);
  }

  private static ItemDefinition require(String key) {
    ItemDefinition definition = ItemRegistry.findByKey(key);
    assertNotNull(definition, key);
    return definition;
  }
}
