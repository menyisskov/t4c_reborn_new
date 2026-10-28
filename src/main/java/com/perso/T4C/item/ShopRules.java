package com.perso.T4C.item;

/**
 * Pure vendor arithmetic shared by single-player (ShopScreen, ItemSaleService) and the online
 * server. No game objects: callers pass plain numbers so both sides run exactly the same code.
 */
public final class ShopRules {
  /** Largest number of units a single online buy or sell request may name. */
  public static final int MAX_TRADE_QUANTITY = 99;

  /** Single-player gold is an int, so a sale never credits past this ceiling. */
  public static final long GOLD_CEILING = Integer.MAX_VALUE;

  /** Marker returned by {@link #baseSellPrice} when the price depends on monster drop tables. */
  public static final long NEEDS_DROP_TABLE = -1L;

  private ShopRules() {}

  /** Cost of {@code quantity} units; overflow throws instead of wrapping. */
  public static long purchaseCost(long unitPrice, int quantity) {
    if (unitPrice < 0 || quantity < 0) throw new IllegalArgumentException("Invalid purchase");
    return Math.multiplyExact(unitPrice, (long) quantity);
  }

  /** A purchase is allowed when the balance covers the whole cost (exactly equal is allowed). */
  public static boolean canAfford(long gold, long cost) {
    return gold >= cost;
  }

  public static long balanceAfterPurchase(long gold, long cost) {
    return gold - cost;
  }

  /**
   * The vendor resale price that does not need monster drop tables, exactly as ItemSalePricing
   * computes it: undroppable items are worth nothing; anything with a shop price (or that is not
   * rarity priced) sells for half its price, at least 1. Returns {@link #NEEDS_DROP_TABLE} for
   * rarity-priced items with no shop price, whose resale value comes from their drop chance.
   */
  public static long baseSellPrice(long price, boolean rarityPriced, boolean undroppable) {
    if (undroppable) return 0L;
    if (price > 0 || !rarityPriced) return Math.max(1L, price / 2L);
    return NEEDS_DROP_TABLE;
  }

  /** Only items worth gold can be sold. */
  public static boolean sellable(long unitSellPrice) {
    return unitSellPrice > 0;
  }

  /**
   * Gold after being paid for one unit: never past {@link #GOLD_CEILING}, the surplus is lost
   * (as in single-player, one unit at a time).
   */
  public static long creditOneUnit(long gold, long unitPrice) {
    long room = Math.max(0L, GOLD_CEILING - gold);
    return gold + Math.min(room, unitPrice);
  }

  /** Gold after selling {@code units} units one at a time. */
  public static long balanceAfterSale(long gold, long unitPrice, int units) {
    long balance = gold;
    for (int i = 0; i < units; i++) balance = creditOneUnit(balance, unitPrice);
    return balance;
  }
}
