package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.server.OnlineShopTrades.Refusal;
import com.perso.T4C.server.OnlineShopTrades.Side;
import com.perso.T4C.server.OnlineShopTrades.Trade;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OnlineShopTradesTest {
  private static ItemContent items;
  private static final String POTION = "item.healing_potion"; // price 42, weight 2, one charge
  private static final String CLUB = "item.wooden_club"; // price 9, weight 8, weapon
  private static final long LOTS = 100_000L;

  @BeforeAll static void load() throws IOException { items = ItemCatalog.loadBundled(); }

  private static CharacterProgress with(List<CharacterProgress.Item> inventory, Map<String, UUID> equipment) {
    return new CharacterProgress(1, "warrior", "male", new CharacterProgress.Attributes(95, 85, 75, 75, 75),
        new CharacterProgress.Vitals(41, 60, 17, 25), new CharacterProgress.Advancement(2, 3, 5, 0, 100),
        inventory, equipment, Map.of(), Set.of(), Map.of(), List.of(), 0,
        new CharacterProgress.Respawn(0, 20, 30), List.of(), Map.of(), 0, null);
  }

  private static Refusal refusalOf(Runnable action) {
    return assertThrows(OnlineShopTrades.Refused.class, action::run).refusal();
  }

  @Test void buyingChargesTheServerPriceAndStartsAStackWithTheItemsCharges() {
    var start = with(List.of(), Map.of());
    var id = UUID.randomUUID();
    var outcome = OnlineShopTrades.apply(start, 500, new Trade(Side.BUY, POTION, 3, 42), items, LOTS, id);
    assertEquals(500 - 126, outcome.gold());
    assertEquals(126, outcome.total());
    assertEquals(List.of(new CharacterProgress.Item(id, POTION, 3, 1)), outcome.progress().inventory());
  }

  @Test void buyingMergesIntoAnUnequippedStackOfTheSameItemAndNeverIntoWornGear() {
    var stack = new CharacterProgress.Item(UUID.randomUUID(), POTION, 2, 1);
    var worn = new CharacterProgress.Item(UUID.randomUUID(), CLUB, 1, 0);
    var start = with(List.of(stack, worn), Map.of("WEAPON", worn.id()));
    var merged = OnlineShopTrades.apply(start, 500, new Trade(Side.BUY, POTION, 2, 42), items, LOTS, UUID.randomUUID());
    assertEquals(List.of(new CharacterProgress.Item(stack.id(), POTION, 4, 1), worn), merged.progress().inventory());
    var club = OnlineShopTrades.apply(start, 500, new Trade(Side.BUY, CLUB, 1, 9), items, LOTS, UUID.randomUUID());
    assertEquals(3, club.progress().inventory().size(), "a worn club is not stacked onto");
    assertEquals(1, club.progress().equipment().size());
  }

  @Test void exactlyEnoughGoldBuysAndOneShortRefusesWithNothingChanged() {
    var start = with(List.of(), Map.of());
    assertEquals(0, OnlineShopTrades.apply(start, 84, new Trade(Side.BUY, POTION, 2, 42), items, LOTS,
        UUID.randomUUID()).gold());
    assertEquals(Refusal.INSUFFICIENT_GOLD, refusalOf(() -> OnlineShopTrades.apply(start, 83,
        new Trade(Side.BUY, POTION, 2, 42), items, LOTS, UUID.randomUUID())));
  }

  @Test void carryWeightIsInclusiveAndCountsEquippedGear() {
    var worn = new CharacterProgress.Item(UUID.randomUUID(), CLUB, 1, 0); // weight 8
    var start = with(List.of(worn), Map.of("WEAPON", worn.id()));
    // 8 carried + 2 potions at weight 2 each = 12.
    assertNotNull(OnlineShopTrades.apply(start, 500, new Trade(Side.BUY, POTION, 2, 42), items, 12, UUID.randomUUID()));
    assertEquals(Refusal.TOO_HEAVY, refusalOf(() -> OnlineShopTrades.apply(start, 500,
        new Trade(Side.BUY, POTION, 2, 42), items, 11, UUID.randomUUID())));
  }

  @Test void freeStockCanBeTakenForNothing() {
    var outcome = OnlineShopTrades.apply(with(List.of(), Map.of()), 0, new Trade(Side.BUY, POTION, 1, 0), items,
        LOTS, UUID.randomUUID());
    assertEquals(0, outcome.total());
  }

  @Test void uniqueItemsAndUnknownKeysFailClosed() {
    var unique = items.items().values().stream().filter(ItemContent.Definition::unique).findFirst().orElseThrow();
    assertEquals(Refusal.UNSUPPORTED_ITEM, refusalOf(() -> OnlineShopTrades.apply(with(List.of(), Map.of()), LOTS,
        new Trade(Side.BUY, unique.key(), 1, 1), items, LOTS, UUID.randomUUID())));
    assertEquals(Refusal.UNSUPPORTED_ITEM, refusalOf(() -> OnlineShopTrades.apply(with(List.of(), Map.of()), LOTS,
        new Trade(Side.BUY, "item.no_such_thing", 1, 1), items, LOTS, UUID.randomUUID())));
  }

  @Test void sellingTakesUnitsFromStacksInOrderAndKeepsTheRemainder() {
    var first = new CharacterProgress.Item(UUID.randomUUID(), POTION, 2, 1);
    var second = new CharacterProgress.Item(UUID.randomUUID(), POTION, 3, 1);
    var other = new CharacterProgress.Item(UUID.randomUUID(), CLUB, 1, 0);
    var start = with(List.of(first, other, second), Map.of());
    var outcome = OnlineShopTrades.apply(start, 10, new Trade(Side.SELL, POTION, 3, 21), items, LOTS, UUID.randomUUID());
    assertEquals(10 + 63, outcome.gold());
    assertEquals(63, outcome.total());
    assertEquals(List.of(other, new CharacterProgress.Item(second.id(), POTION, 2, 1)), outcome.progress().inventory());
  }

  @Test void sellingAWholeStackRemovesItAndSellingMoreThanOwnedRefuses() {
    var stack = new CharacterProgress.Item(UUID.randomUUID(), POTION, 2, 1);
    var start = with(List.of(stack), Map.of());
    assertTrue(OnlineShopTrades.apply(start, 0, new Trade(Side.SELL, POTION, 2, 21), items, LOTS,
        UUID.randomUUID()).progress().inventory().isEmpty());
    assertEquals(Refusal.ITEM_UNAVAILABLE, refusalOf(() -> OnlineShopTrades.apply(start, 0,
        new Trade(Side.SELL, POTION, 3, 21), items, LOTS, UUID.randomUUID())));
  }

  @Test void equippedUnitsCannotBeSold() {
    var worn = new CharacterProgress.Item(UUID.randomUUID(), CLUB, 1, 0);
    var start = with(List.of(worn), Map.of("WEAPON", worn.id()));
    assertEquals(Refusal.ITEM_UNAVAILABLE, refusalOf(() -> OnlineShopTrades.apply(start, 0,
        new Trade(Side.SELL, CLUB, 1, 4), items, LOTS, UUID.randomUUID())));
  }

  @Test void worthlessItemsAreNotSellable() {
    var stack = new CharacterProgress.Item(UUID.randomUUID(), POTION, 1, 1);
    assertEquals(Refusal.NOT_SELLABLE, refusalOf(() -> OnlineShopTrades.apply(with(List.of(stack), Map.of()), 0,
        new Trade(Side.SELL, POTION, 1, 0), items, LOTS, UUID.randomUUID())));
  }

  @Test void salesStopAtTheSinglePlayerGoldCeiling() {
    var stack = new CharacterProgress.Item(UUID.randomUUID(), POTION, 3, 1);
    var outcome = OnlineShopTrades.apply(with(List.of(stack), Map.of()), Integer.MAX_VALUE - 30,
        new Trade(Side.SELL, POTION, 3, 21), items, LOTS, UUID.randomUUID());
    assertEquals(Integer.MAX_VALUE, outcome.gold());
    assertEquals(30, outcome.total(), "the surplus is lost, exactly as single-player loses it");
    assertTrue(outcome.progress().inventory().isEmpty(), "all units still leave the inventory");
  }

  @Test void tradeShapeIsValidated() {
    assertThrows(IllegalArgumentException.class, () -> new Trade(Side.BUY, POTION, 0, 1));
    assertThrows(IllegalArgumentException.class, () -> new Trade(Side.BUY, POTION, 100, 1));
    assertThrows(IllegalArgumentException.class, () -> new Trade(Side.BUY, POTION, 1, -1));
    assertThrows(IllegalArgumentException.class, () -> new Trade(Side.BUY, "healing_potion", 1, 1));
  }
}
