package com.perso.T4C.server;

import com.perso.T4C.item.CarryCapacityRules;
import com.perso.T4C.item.ShopRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure vendor trade arithmetic on committed character state. The server computes the unit price
 * from its own catalog; a client never supplies one. The same function runs once as a cheap
 * pre-check against the cached state and again inside the database transaction on the locked row,
 * so the two can never disagree.
 */
public final class OnlineShopTrades {
  public enum Side { BUY, SELL }

  /** Definite refusals: nothing is written when one of these is raised. */
  public enum Refusal {
    INSUFFICIENT_GOLD("insufficient_gold"),
    TOO_HEAVY("too_heavy"),
    ITEM_UNAVAILABLE("item_unavailable"),
    UNSUPPORTED_ITEM("unsupported_item"),
    NOT_SELLABLE("not_sellable");

    private final String code;

    Refusal(String code) { this.code = code; }

    public String code() { return code; }
  }

  public static final class Refused extends RuntimeException {
    private final Refusal refusal;

    Refused(Refusal refusal) {
      super(refusal.code(), null, false, false);
      this.refusal = refusal;
    }

    public Refusal refusal() { return refusal; }
  }

  /** A priced request. {@code unitPrice} is the buy price or the per-unit sale payout. */
  public record Trade(Side side, String itemKey, int quantity, long unitPrice) {
    public Trade {
      Objects.requireNonNull(side);
      if (itemKey == null || !itemKey.matches("item\\.[a-z0-9_]{1,120}") || quantity < 1
          || quantity > ShopRules.MAX_TRADE_QUANTITY || unitPrice < 0)
        throw new IllegalArgumentException("Invalid shop trade");
    }

    /** Stable text bound into the idempotency receipt. */
    public String canonical() { return side + "\n" + itemKey + "\n" + quantity + "\n" + unitPrice; }
  }

  public record Outcome(CharacterProgress progress, long gold, long total) {}

  private OnlineShopTrades() {}

  /** Apply one trade to committed state or throw {@link Refused}. {@code newInstance} names a created stack. */
  public static Outcome apply(CharacterProgress progress, long gold, Trade trade, ItemContent items,
      long maximumWeight, UUID newInstance) {
    Objects.requireNonNull(progress); Objects.requireNonNull(trade); Objects.requireNonNull(items);
    Objects.requireNonNull(newInstance);
    var definition = items.items().get(trade.itemKey());
    if (definition == null) throw new Refused(Refusal.UNSUPPORTED_ITEM);
    return trade.side() == Side.BUY
        ? buy(progress, gold, trade, definition, items, maximumWeight, newInstance)
        : sell(progress, gold, trade);
  }

  private static Outcome buy(CharacterProgress progress, long gold, Trade trade,
      ItemContent.Definition definition, ItemContent items, long maximumWeight, UUID newInstance) {
    // Unique items need an admission engine this path does not have; fail closed like pickup does.
    if (definition.unique()) throw new Refused(Refusal.UNSUPPORTED_ITEM);
    long cost = ShopRules.purchaseCost(trade.unitPrice(), trade.quantity());
    if (!ShopRules.canAfford(gold, cost)) throw new Refused(Refusal.INSUFFICIENT_GOLD);
    long added = Math.multiplyExact(Math.max(0L, definition.weight()), (long) trade.quantity());
    if (!CarryCapacityRules.canAdd(carriedWeight(progress, items), added, maximumWeight))
      throw new Refused(Refusal.TOO_HEAVY);
    int charges = OnlineLootDrafts.charges(definition);
    var worn = new HashSet<>(progress.equipment().values());
    var inventory = new ArrayList<CharacterProgress.Item>(progress.inventory().size() + 1);
    boolean merged = false;
    for (var item : progress.inventory()) {
      if (!merged && item.key().equals(trade.itemKey()) && item.charges() == charges && !worn.contains(item.id())) {
        inventory.add(new CharacterProgress.Item(item.id(), item.key(),
            Math.addExact(item.quantity(), trade.quantity()), item.charges()));
        merged = true;
      } else inventory.add(item);
    }
    if (!merged) inventory.add(new CharacterProgress.Item(newInstance, trade.itemKey(), trade.quantity(), charges));
    return new Outcome(ProgressEdits.withInventory(progress, inventory),
        ShopRules.balanceAfterPurchase(gold, cost), cost);
  }

  private static Outcome sell(CharacterProgress progress, long gold, Trade trade) {
    if (!ShopRules.sellable(trade.unitPrice())) throw new Refused(Refusal.NOT_SELLABLE);
    var worn = new HashSet<>(progress.equipment().values());
    long owned = 0L;
    for (var item : progress.inventory())
      if (item.key().equals(trade.itemKey()) && !worn.contains(item.id())) owned += item.quantity();
    if (owned < trade.quantity()) throw new Refused(Refusal.ITEM_UNAVAILABLE);
    int remaining = trade.quantity();
    var inventory = new ArrayList<CharacterProgress.Item>(progress.inventory().size());
    for (var item : progress.inventory()) {
      if (remaining == 0 || !item.key().equals(trade.itemKey()) || worn.contains(item.id())) {
        inventory.add(item);
        continue;
      }
      int taken = Math.min(remaining, item.quantity());
      remaining -= taken;
      if (item.quantity() > taken)
        inventory.add(new CharacterProgress.Item(item.id(), item.key(), item.quantity() - taken, item.charges()));
    }
    long balance = ShopRules.balanceAfterSale(gold, trade.unitPrice(), trade.quantity());
    return new Outcome(ProgressEdits.withInventory(progress, inventory), balance, balance - gold);
  }

  /** Every carried instance counts once, equipped gear included; storage is not carried weight. */
  static long carriedWeight(CharacterProgress progress, ItemContent items) {
    long total = 0L;
    for (var item : progress.inventory()) {
      var definition = items.items().get(item.key());
      if (definition != null) total = Math.addExact(total,
          Math.multiplyExact(Math.max(0L, definition.weight()), (long) item.quantity()));
    }
    return total;
  }
}
