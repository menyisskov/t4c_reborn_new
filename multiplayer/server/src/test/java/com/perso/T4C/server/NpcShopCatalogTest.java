package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NpcShopCatalogTest {
  @Test
  void bundledVendorStockResolvesAgainstTheSharedItemDefinitions() throws Exception {
    var catalog = NpcShopCatalog.loadBundled();
    assertFalse(catalog.content().vendors().isEmpty());
    assertTrue(catalog.content().vendors().stream().anyMatch(vendor -> vendor.npcId().equals("Fali")));
  }
}
