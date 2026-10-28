package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.item.CarryCapacityRules;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Permanent guard from the T4C-0246 audit: a fresh character must be able to loot. Online, a
 * character carrying more than its limit has every ground pickup and corpse recovery refused.
 */
class OnlineStarterKitWeightTest {
  /**
   * Spare carry capacity every fresh kit must keep, so the first few drops still fit. Measured
   * 2026-10-28 after the arrow decision: spare is 90 Warrior, 36 Archer, 63 Paladin, 32 Cleric,
   * 40 Healer, 33 Mage, 61 Battle Mage.
   */
  static final long MIN_HEADROOM = 30L;

  @Test void everyClassStartsWithRoomToLoot() throws Exception {
    var content = ItemCatalog.loadBundled();
    for (CharacterClass type : CharacterClass.values()) {
      for (long seed = 1; seed <= 25; seed++) {
        var progress = OnlineCharacterCreator.prepare(type, "female", false, new Random(seed)).progress();
        long maximum = CarryCapacityRules.maximumWeight(OnlinePickupRules.effectiveStrength(progress, content, 0L));
        long carried = OnlinePickupRules.carriedWeight(progress, content);
        assertTrue(carried + MIN_HEADROOM <= maximum,
            type + " (seed " + seed + ") carries " + carried + " of " + maximum + ": needs " + MIN_HEADROOM + " spare");
      }
    }
  }

  @Test void arrowsAreWeightlessAndQuiversStayLightButNeverWeightless() throws Exception {
    var content = ItemCatalog.loadBundled();
    int arrows = 0;
    int quivers = 0;
    for (var item : content.items().values()) {
      if (item.structure() != com.perso.T4C.config.GameConstants.QUIVER_STRUCTURE_ID) continue;
      String key = item.key();
      if (key.endsWith("_arrow")) {
        arrows++;
        assertEquals(0L, item.weight(), key + ": arrows weigh nothing (owner, 2026-10-28)");
      } else if (key.contains("quiver") || key.startsWith("item.carquois")) {
        quivers++;
        assertTrue(item.weight() >= 1L && item.weight() <= 2L, key + ": quivers keep a small fixed weight");
      }
    }
    assertTrue(arrows >= 4, "wooden, flight, pinpoint and bone-tipped arrows are all covered");
    assertTrue(quivers >= 20, "the quiver sweep must actually find the quivers");
  }
}
