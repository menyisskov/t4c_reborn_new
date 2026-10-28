package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.NpcShopContent;
import org.junit.jupiter.api.Test;

class NpcShopCatalogTest {
  @Test
  void bundledVendorStockResolvesAgainstTheSharedItemDefinitions() throws Exception {
    var catalog = NpcShopCatalog.loadBundled();
    assertFalse(catalog.content().vendors().isEmpty());
    assertTrue(catalog.content().vendors().stream().anyMatch(vendor -> vendor.npcId().equals("Fali")));
  }

  @Test
  void rejectsOffersWhoseBuyPriceDiffersFromTheBundledItemDefinition() throws Exception {
    var bundled = NpcShopCatalog.loadBundled().content();
    var items = ItemCatalog.loadBundled();
    var vendor = bundled.vendors().getFirst();
    var offer = vendor.offers().getFirst();
    var mismatched =
        new NpcShopContent(
            NpcShopContent.VERSION,
            java.util.List.of(
                new NpcShopContent.Vendor(
                    vendor.npcId(),
                    java.util.List.of(
                        new NpcShopContent.Offer(
                            offer.itemKey(), offer.buyPrice() + 1, offer.sellPrice())))));
    assertThrows(IllegalArgumentException.class, () -> new NpcShopCatalog(mismatched, items));
  }

  @Test
  void rejectsNegativeOfferPrices() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new NpcShopContent.Offer("item.torch", 1, -1));
  }

  @Test
  void rejectsResalePriceThatDisagreesWithBundledDefinition() throws Exception {
    var bundled = NpcShopCatalog.loadBundled().content();
    var items = ItemCatalog.loadBundled();
    var vendor = bundled.vendors().getFirst();
    var offer = vendor.offers().getFirst();
    var definition = items.items().get(offer.itemKey());
    long wrongSellPrice = definition.undroppable() ? 1 : 0;
    var mismatched =
        new NpcShopContent(
            NpcShopContent.VERSION,
            java.util.List.of(
                new NpcShopContent.Vendor(
                    vendor.npcId(),
                    java.util.List.of(
                        new NpcShopContent.Offer(
                            offer.itemKey(), offer.buyPrice(), wrongSellPrice)))));
    assertThrows(IllegalArgumentException.class, () -> new NpcShopCatalog(mismatched, items));
  }
}
