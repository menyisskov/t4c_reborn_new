package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.item.json.ItemJsonLoader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Campaign equipment progresses from the Moonwake opening to Rhunor's inner court. */
class WitnessIslesEquipmentTest {
  private static final List<String> ELEMENTS =
      List.of("fire", "water", "air", "earth", "light", "dark");
  private static final Map<String, Integer> PRIMARY_REQUIREMENTS =
      Map.of("fire", 240, "water", 325, "air", 440, "earth", 475, "light", 525, "dark", 625);

  @BeforeEach
  void load() {
    ItemJsonLoader.loadAndRegister("assets/items");
  }

  @AfterEach
  void reset() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  @Test
  void elementalSetsCoverEveryCampaignTierAndEquipmentRole() {
    int previousRequirement = 0;
    for (String element : ELEMENTS) {
      for (String role : List.of("robe", "wings", "signet", weaponRole(element))) {
        ItemDefinition item = ItemRegistry.findByKey("witness_" + element + "_" + role);
        assertNotNull(item, element + " " + role);
        assertTrue(item.getName().startsWith("${item.witness_"), item.getKey());
        assertTrue(item.isRarityPriced(), item.getKey());
        assertTrue(item.isUnique(), item.getKey());
        assertTrue(ItemSalePricing.sellPrice(item) > 1, item.getKey());
      }
      ItemDefinition robe = ItemRegistry.findByKey("witness_" + element + "_robe");
      int requirement =
          (int)
              ItemBalance.primaryRequirement(
                  ItemBalance.archetype(robe),
                  robe.getReqStr(),
                  robe.getReqAgi(),
                  robe.getMinInt(),
                  robe.getMinWis());
      assertEquals(PRIMARY_REQUIREMENTS.get(element).intValue(), requirement, element);
      assertTrue(requirement > previousRequirement, element + " should advance the tier");
      previousRequirement = requirement;
    }
  }

  @Test
  void progressionAlsoHasPhysicalPlateAndTheRequestedJewelrySlots() {
    for (String element : ELEMENTS) {
      for (String role : List.of("plate", "bracelet", "amulet", "tiara")) {
        ItemDefinition item = ItemRegistry.findByKey("witness_" + element + "_" + role);
        assertNotNull(item, element + " " + role);
        assertTrue(item.isRarityPriced(), item.getKey());
        assertTrue(ItemSalePricing.sellPrice(item) > 1, item.getKey());
      }
    }
    for (String key : List.of("witness_fire_mace", "witness_water_staff", "witness_dark_dagger")) {
      ItemDefinition item = ItemRegistry.findByKey(key);
      assertNotNull(item, key);
      assertTrue(item.isRarityPriced(), key);
      assertTrue(ItemSalePricing.sellPrice(item) > 1, key);
    }
  }

  @Test
  void everyEquipmentSpriteIsPackedBeforeTheDefinitionsReferenceIt() throws Exception {
    Set<String> packed = new HashSet<>();
    SpriteBinIO.readAll(
        Path.of("assets/sprites"),
        SpriteBinIO.DEFAULT_BASE_NAME,
        sprite -> packed.add(SpriteBinIO.key(sprite.name())));
    for (String element : ELEMENTS) {
      for (String role : List.of("robe", "wings", "signet", weaponRole(element))) {
        ItemDefinition item = ItemRegistry.findByKey("witness_" + element + "_" + role);
        assertSpriteExists(packed, item.getAppearanceEquippedPrimary(), item.getKey());
        assertSpriteExists(packed, item.getAppearanceInventory(), item.getKey());
      }
    }
  }

  private static String weaponRole(String element) {
    return switch (element) {
      case "fire" -> "brand";
      case "water", "earth" -> "sceptre";
      case "air" -> "wand";
      case "light" -> "staff";
      case "dark" -> "rod";
      default -> throw new IllegalArgumentException(element);
    };
  }

  private static void assertSpriteExists(Set<String> packed, String sprite, String itemKey) {
    assertNotNull(sprite, itemKey + " has no sprite");
    String key = SpriteBinIO.key(sprite);
    assertTrue(
        packed.stream().anyMatch(name -> name.startsWith(key)),
        itemKey + " references missing sprite family " + sprite);
  }
}
