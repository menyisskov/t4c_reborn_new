package com.perso.T4C.npc.catalog;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.ItemKeys;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.ItemSalePricing;
import com.perso.T4C.npc.behavior.ShopBehavior;
import com.perso.T4C.tools.OnlineNpcShopExporter;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ShopCatalogTest {
  @Test
  void exportedInventoriesMatchTheOfflineGenericVendorBehavior() {
    Map<String, List<String>> inventories = ShopCatalog.allShopInventories();
    assertFalse(inventories.isEmpty());
    inventories.forEach(
        (npcId, itemKeys) -> {
          assertInstanceOf(ShopBehavior.class, ShopCatalog.get(npcId));
          assertEquals(itemKeys, ((ShopBehavior) ShopCatalog.get(npcId)).items());
          assertTrue(itemKeys.stream().allMatch(key -> ItemKeys.normalize(key).startsWith("item.")));
        });

    var exported = OnlineNpcShopExporter.content();
    long exportedVendors =
        inventories.values().stream()
            .filter(items -> items.stream().anyMatch(key -> ItemRegistry.findByKey(key) != null))
            .count();
    assertEquals(exportedVendors, exported.vendors().size());
    exported.vendors().forEach(
        vendor ->
            assertEquals(
                inventories.get(vendor.npcId()).stream()
                    .filter(key -> ItemRegistry.findByKey(key) != null)
                    .map(ItemKeys::normalize)
                    .toList(),
                vendor.offers().stream().map(com.perso.T4C.online.model.NpcShopContent.Offer::itemKey).toList()));
    exported.vendors().forEach(
        vendor ->
            vendor.offers().forEach(
                offer -> {
                  var definition = ItemRegistry.findByKey(offer.itemKey());
                  assertEquals(definition.getPrice(), offer.buyPrice());
                  assertEquals(ItemSalePricing.sellPrice(definition), offer.sellPrice());
                }));
    assertThrows(UnsupportedOperationException.class, () -> inventories.clear());
    assertThrows(UnsupportedOperationException.class, () -> inventories.values().iterator().next().clear());
    try {
      assertEquals(
          OnlineNpcShopExporter.json(),
          // A Windows checkout (core.autocrlf) turns the committed LF file into CRLF on disk.
          Files.readString(Path.of("multiplayer/server/src/main/resources/content/npc-shops-v2.json"))
              .replace("\r\n", "\n"));
    } catch (java.io.IOException error) {
      fail("Could not read generated NPC shop content", error);
    }
  }
}
