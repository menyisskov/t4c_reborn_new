package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ShopRulesTest {
  @Test void purchaseCostMultipliesAndRefusesOverflowOrNegatives() {
    assertEquals(126, ShopRules.purchaseCost(42, 3));
    assertEquals(0, ShopRules.purchaseCost(0, 99));
    assertThrows(ArithmeticException.class, () -> ShopRules.purchaseCost(Long.MAX_VALUE, 2));
    assertThrows(IllegalArgumentException.class, () -> ShopRules.purchaseCost(-1, 1));
    assertThrows(IllegalArgumentException.class, () -> ShopRules.purchaseCost(1, -1));
  }

  @Test void affordabilityIsInclusive() {
    assertTrue(ShopRules.canAfford(84, 84));
    assertFalse(ShopRules.canAfford(83, 84));
    assertTrue(ShopRules.canAfford(0, 0));
    assertEquals(0, ShopRules.balanceAfterPurchase(84, 84));
  }

  @Test void baseSellPriceMatchesTheDocumentedBranches() {
    assertEquals(0, ShopRules.baseSellPrice(500, false, true), "undroppable items are worth nothing");
    assertEquals(0, ShopRules.baseSellPrice(0, true, true));
    assertEquals(21, ShopRules.baseSellPrice(42, false, false));
    assertEquals(21, ShopRules.baseSellPrice(42, true, false), "a shop price wins over rarity pricing");
    assertEquals(1, ShopRules.baseSellPrice(1, false, false), "never below one gold");
    assertEquals(1, ShopRules.baseSellPrice(0, false, false), "a free ordinary item still resells for one");
    assertEquals(ShopRules.NEEDS_DROP_TABLE, ShopRules.baseSellPrice(0, true, false));
  }

  @Test void onlyPositivePricesAreSellable() {
    assertTrue(ShopRules.sellable(1));
    assertFalse(ShopRules.sellable(0));
    assertFalse(ShopRules.sellable(-1));
  }

  @Test void salesNeverCreditPastTheGoldCeilingAndNeverDebit() {
    assertEquals(Integer.MAX_VALUE, ShopRules.creditOneUnit(Integer.MAX_VALUE - 5, 50));
    assertEquals(Integer.MAX_VALUE, ShopRules.creditOneUnit(Integer.MAX_VALUE, 50));
    assertEquals(Integer.MAX_VALUE + 10L, ShopRules.creditOneUnit(Integer.MAX_VALUE + 10L, 50),
        "gold already past the ceiling (database gold is a long) is left alone, never reduced");
    assertEquals(Integer.MAX_VALUE, ShopRules.balanceAfterSale(Integer.MAX_VALUE - 7, 5, 3));
    assertEquals(115, ShopRules.balanceAfterSale(100, 5, 3));
    assertEquals(100, ShopRules.balanceAfterSale(100, 5, 0));
  }

  @Test void perRequestQuantityLimitIsSmall() {
    assertTrue(ShopRules.MAX_TRADE_QUANTITY >= 10 && ShopRules.MAX_TRADE_QUANTITY < 100);
  }
}
