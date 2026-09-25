package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.player.BodyPart;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The themed sets written by {@code tools/ArmorSetGenerator} after the two 8-flavor tiers: the
 * Centaur Slaying archer set (with a quiver) for the Bow of Centaur Slaying, and the zone sets for
 * The Sunken Chancel (Drowned Inquisition, water) and Cinderreach Hills (Cinderforged, fire). Each
 * is complete, of the right class and element, and dropped by its zone's boss.
 */
class ThemedArmorSetsTest {
  private static final List<String> SLOTS =
      List.of("armor", "boots", "gauntlets", "helmet", "leggings", "protector");
  private static final int FIRE_POWER = 17, WATER_POWER = 18;

  @BeforeEach
  void load() {
    ItemJsonLoader.loadAndRegister("assets/items");
  }

  @AfterEach
  void reset() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  @Test
  void centaurSlayingIsAFullArcherSetWithAWorkingQuiver() {
    List<String> keys = keys("centaur_slaying_", true);
    for (String key : keys) {
      ItemDefinition item = piece(key);
      assertEquals(ItemBalance.Archetype.ARCHER, ItemBalance.archetype(item), key + " class");
    }
    ItemDefinition quiver = piece("centaur_slaying_quiver");
    assertEquals(BodyPart.WEAPON2, quiver.getBodyPart());
    assertEquals(GameConstants.QUIVER_STRUCTURE_ID, quiver.getStructure(), "a bow needs a quiver");
    assertEquals(0.0, quiver.getArmorClass(), 0.0001);
    assertDroppedBy("Centaur King", keys);
  }

  @Test
  void drownedInquisitionIsTheSunkenChancelsWaterSet() {
    List<String> keys = keys("drowned_inquisition_", false);
    for (String key : keys) assertMageSetPiece(key, WATER_POWER);
    assertDroppedBy("Mordrenn the Drowned Inquisitor", keys);
  }

  @Test
  void cinderforgedIsCinderreachHillsFireSet() {
    List<String> keys = keys("cinderforged_", false);
    for (String key : keys) assertMageSetPiece(key, FIRE_POWER);
    assertDroppedBy("Ignarok the Emberfang", keys);
  }

  private static List<String> keys(String prefix, boolean quiver) {
    List<String> keys = new java.util.ArrayList<>();
    for (String slot : SLOTS) keys.add(prefix + slot);
    if (quiver) keys.add(prefix + "quiver");
    for (String key : keys) assertTrue(ItemBalance.isGeneratedSetPiece(key), key + " is a set piece");
    return keys;
  }

  private static ItemDefinition piece(String key) {
    ItemDefinition item = ItemRegistry.findByKey(key);
    assertNotNull(item, key + " should load");
    return item;
  }

  private static void assertMageSetPiece(String key, int powerStatId) {
    ItemDefinition item = piece(key);
    assertEquals(ItemBalance.Archetype.INT_MAGE, ItemBalance.archetype(item), key + " class");
    assertTrue(
        item.getBoosts().stream().anyMatch(b -> b.getStatId() == powerStatId),
        key + " should carry its zone's element power");
  }

  private static void assertDroppedBy(String monster, List<String> keys) {
    MonsterDef def = MonsterRegistry.findByName(monster);
    assertNotNull(def, monster);
    Set<String> loot = new HashSet<>();
    for (MonsterDef.LootDrop drop : def.getLoot()) loot.add(drop.getItem());
    for (String key : keys) assertTrue(loot.contains(key), monster + " should drop " + key);
  }
}
