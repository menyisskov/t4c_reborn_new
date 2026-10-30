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
    var thalindra = catalog.content().vendors().stream()
        .filter(vendor -> vendor.npcId().equals("ArchmageThalindra")).findFirst().orElseThrow();
    assertEquals(
        java.util.List.of(
            "item.pyromancers_mantle",
            "item.tidecallers_mantle",
            "item.geomancers_mantle",
            "item.windweavers_mantle",
            "item.lightbringers_mantle",
            "item.shadowmancers_mantle"),
        thalindra.offers().stream().map(NpcShopContent.Offer::itemKey).toList());
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
