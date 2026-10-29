package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import com.perso.T4C.player.Player;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Single-player and the online server price shop trades with {@link ShopRules}. Proven equal here:
 * the vendor resale price (every registered item), the per-unit sale credit through the real
 * {@link ItemSaleService}, and that the GL-bound ShopScreen takes its affordability and debit from
 * ShopRules. Not provable headless: ShopScreen's basket UI itself (see the source pin below).
 */
class ShopRulesSinglePlayerEquivalenceTest {
  @BeforeEach void loadContent() {
    ItemJsonLoader.loadAndRegister();
    MonsterJsonLoader.loadAndRegister();
  }

  @AfterEach void resetContent() {
    ItemRegistry.resetAdditionalDefinitions();
    MonsterRegistry.resetToGeneratedDefinitions();
  }

  @Test void resalePriceWithoutDropTablesIsExactlyWhatSinglePlayerPays() {
    int compared = 0;
    for (ItemDefinition item : ItemRegistry.allByKey().values()) {
      long base = ShopRules.baseSellPrice(item.getPrice(), item.isRarityPriced(), item.isUndroppable());
      if (base == ShopRules.NEEDS_DROP_TABLE) continue;
      assertEquals(base, ItemSalePricing.sellPrice(item), item.getKey());
      compared++;
    }
    assertTrue(compared > 1000, "the sweep covers the real catalogue, not a sample");
  }

  @Test void dropOnlyItemsAreTheOnlyOnesTheSharedRuleLeavesToTheDropTables() {
    for (ItemDefinition item : ItemRegistry.allByKey().values()) {
      long base = ShopRules.baseSellPrice(item.getPrice(), item.isRarityPriced(), item.isUndroppable());
      if (base == ShopRules.NEEDS_DROP_TABLE) {
        assertTrue(item.isRarityPriced() && item.getPrice() <= 0 && !item.isUndroppable(), item.getKey());
      }
    }
  }

  @Test void perUnitSaleCreditMatchesItemSaleServiceIncludingNearTheGoldCeiling() {
    ItemDefinition sellable = ItemRegistry.allByKey().values().stream()
        .filter(i -> i.getPrice() >= 100 && !i.isUndroppable() && !i.isRarityPriced())
        .findFirst().orElseThrow();
    long price = ItemSalePricing.sellPrice(sellable);
    for (int startGold : new int[] {0, 1234, Integer.MAX_VALUE - (int) price - 3, Integer.MAX_VALUE - 1,
        Integer.MAX_VALUE}) {
      for (int units = 1; units <= 4; units++) {
        Player player = new Player();
        player.setGold(startGold);
        for (int i = 0; i < units; i++) player.getInventory().add(sellable.getKey());
        long paid = ItemSaleService.sell(player, sellable.getKey(), units);
        assertEquals(ShopRules.balanceAfterSale(startGold, price, units), player.getGold(),
            "gold after " + units + " sales from " + startGold);
        assertEquals(player.getGold() - startGold, paid);
      }
    }
  }

  @Test void shopScreenTakesItsAffordabilityAndDebitFromTheSharedRules() throws Exception {
    // buyBasket is private and GL-bound, so this pins the wiring rather than running the screen.
    String source = Files.readString(Path.of("src/main/java/com/perso/T4C/gui/screen/ShopScreen.java"));
    assertTrue(source.contains("ShopRules.canAfford(player.getGold(), cost)"));
    assertTrue(source.contains("ShopRules.balanceAfterPurchase(player.getGold(), cost)"));
    assertTrue(source.contains("ShopRules.canAfford(player.getGold(), basketCost() + entry.effectivePrice)"));
    assertFalse(source.contains("player.getGold() < cost"), "the old inline comparison is gone");
  }
}
