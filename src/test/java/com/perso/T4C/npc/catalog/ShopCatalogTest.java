package com.perso.T4C.npc.catalog;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.ItemKeys;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.npc.behavior.ShopBehavior;
import com.perso.T4C.tools.OnlineNpcShopExporter;
import java.util.List;
import java.util.Map;
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
                vendor.itemKeys()));
    assertThrows(UnsupportedOperationException.class, () -> inventories.clear());
    assertThrows(UnsupportedOperationException.class, () -> inventories.values().iterator().next().clear());
    assertEquals(OnlineNpcShopExporter.json(), OnlineNpcShopExporter.json());
  }
}
