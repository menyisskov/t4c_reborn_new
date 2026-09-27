package com.perso.T4C.monster.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** T4C-0081 (owner's call): every monster level 300+ has a 5% shot each at the two general-vendor
 * gold sinks, mana_prism and critical_healing_potion, on top of whatever else it drops. */
class EndgameUtilityLootTest {
  @Test
  void everyMonsterAtOrAboveLevel300DropsBothUtilityItemsAtFivePercent() {
    List<MonsterDef> level300Plus =
        MonsterRegistry.load().stream().filter(def -> def.getLevel() >= 300).toList();
    assertFalse(level300Plus.isEmpty(), "expected at least one level-300+ monster to check");
    for (MonsterDef def : level300Plus) {
      assertTrue(
          hasDrop(def, "item.mana_prism", 0.05f),
          () -> def.getName() + " (level " + def.getLevel() + ") should drop item.mana_prism at 5%");
      assertTrue(
          hasDrop(def, "item.critical_healing_potion", 0.05f),
          () ->
              def.getName()
                  + " (level "
                  + def.getLevel()
                  + ") should drop item.critical_healing_potion at 5%");
    }
  }

  @Test
  void monstersBelowLevel300DoNotGetTheEndgameUtilityDrops() {
    List<MonsterDef> underLevel300 =
        MonsterRegistry.load().stream().filter(def -> def.getLevel() > 0 && def.getLevel() < 300).toList();
    assertFalse(underLevel300.isEmpty(), "expected at least one sub-300 monster to check");
    for (MonsterDef def : underLevel300) {
      assertFalse(hasDrop(def, "item.mana_prism", 0.05f), def.getName() + " is under level 300");
    }
  }

  private static boolean hasDrop(MonsterDef def, String itemKey, float chance) {
    for (MonsterDef.LootDrop drop : def.getLoot()) {
      if (itemKey.equals(drop.getItem())) {
        assertEquals(chance, drop.getChance(), 0.0001f, itemKey + " chance for " + def.getName());
        return true;
      }
    }
    return false;
  }
}
