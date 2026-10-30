package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.ShopRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.ShopOffersReply;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OnlineShopsTest {
  private static ItemContent items;

  @BeforeAll static void load() throws IOException { items = ItemCatalog.loadBundled(); }

  private static CharacterProgress with(List<CharacterProgress.Item> inventory, Map<String, UUID> equipment) {
    return new CharacterProgress(1, "warrior", "male", new CharacterProgress.Attributes(95, 85, 75, 75, 75),
        new CharacterProgress.Vitals(41, 60, 17, 25), new CharacterProgress.Advancement(2, 3, 5, 0, 100),
        inventory, equipment, Map.of(), Set.of(), Map.of(), List.of(), 0,
        new CharacterProgress.Respawn(0, 20, 30), List.of(), Map.of(), 0, null);
  }

  @Test void sigfriedIsAShopkeeperAndUnknownNpcsAreNot() {
    assertTrue(OnlineShops.vendor("Sigfried").isPresent());
    assertTrue(OnlineShops.vendor("Kilhiam").isEmpty());
    assertTrue(OnlineShops.vendor("Nobody").isEmpty());
  }

  @Test void buyPricesComeOnlyFromTheVendorsOwnStock() {
    var sigfried = OnlineShops.vendor("Sigfried").orElseThrow();
    assertEquals(9L, OnlineShops.buyPrice(sigfried, "item.rusted_short_sword").orElseThrow());
    assertTrue(OnlineShops.buyPrice(sigfried, "item.healing_potion").isEmpty(), "Sigfried does not stock potions");
  }

  @Test void everyVendorPricesTheSameItemTheSameWhenBuyingFromYou() {
    var seen = new HashMap<String, Long>();
    for (var vendor : OnlineShops.catalog().content().vendors())
      for (var offer : vendor.offers()) {
        var previous = seen.putIfAbsent(offer.itemKey(), offer.sellPrice());
        if (previous != null) assertEquals(previous, offer.sellPrice(), offer.itemKey());
      }
  }

  @Test void stockedItemsResellAtTheExportedSinglePlayerPriceAndOthersAtHalfPrice() {
    var potion = items.items().get("item.healing_potion");
    assertEquals(21L, OnlineShops.sellPrice(potion));
    var stocked = new java.util.HashSet<String>();
    for (var vendor : OnlineShops.catalog().content().vendors())
      for (var offer : vendor.offers()) stocked.add(offer.itemKey());
    var plain = items.items().values().stream()
        .filter(i -> !stocked.contains(i.key()) && i.price() > 0 && !i.undroppable()).findFirst().orElseThrow();
    assertEquals(Math.max(1L, plain.price() / 2L), OnlineShops.sellPrice(plain));
  }

  @Test void dropOnlyItemsHaveNoOnlineResalePriceYet() {
    var stocked = new java.util.HashSet<String>();
    for (var vendor : OnlineShops.catalog().content().vendors())
      for (var offer : vendor.offers()) stocked.add(offer.itemKey());
    var rare = items.items().values().stream()
        .filter(i -> i.rarityPriced() && i.price() == 0 && !i.undroppable() && !stocked.contains(i.key()))
        .findFirst().orElseThrow();
    assertTrue(OnlineShops.sellPrice(rare) > 0);
  }

  @Test void bundledDropPricedItemHasEquivalentOfflineAndOnlineResaleFormula() throws IOException {
    var monsterContent = MonsterCatalog.loadBundled();
    var chances = ShopRules.indexDropChances(List.copyOf(monsterContent.monsters().values()));
    var stocked = new java.util.HashSet<String>();
    for (var vendor : OnlineShops.catalog().content().vendors())
      for (var offer : vendor.offers()) stocked.add(offer.itemKey());
    var rare = items.items().values().stream()
        .filter(i -> i.rarityPriced() && i.price() == 0 && !i.undroppable() && !stocked.contains(i.key()))
        .filter(i -> ShopRules.dropChance(chances, i.key()) > 0)
        .findFirst().orElseThrow();
    long sharedPrice = ShopRules.rarityDropPrice(rare.key(), rare.unique(), ShopRules.dropChance(chances, rare.key()));
    assertEquals(sharedPrice, OnlineShops.sellPrice(rare));
    assertTrue(sharedPrice > 0);
  }

  @Test void undroppableItemsAreNeverBought() {
    var quest = items.items().values().stream().filter(ItemContent.Definition::undroppable).findFirst().orElseThrow();
    assertEquals(0L, OnlineShops.sellPrice(quest));
  }

  @Test void listingShowsStockAndOnlyUnequippedSellableGoods() {
    var sigfried = OnlineShops.vendor("Sigfried").orElseThrow();
    var potions = new CharacterProgress.Item(UUID.randomUUID(), "item.healing_potion", 4, 1);
    var more = new CharacterProgress.Item(UUID.randomUUID(), "item.healing_potion", 1, 1);
    var worn = new CharacterProgress.Item(UUID.randomUUID(), "item.wooden_club", 1, 0);
    var spare = new CharacterProgress.Item(UUID.randomUUID(), "item.wooden_club", 2, 0);
    var progress = with(List.of(potions, more, worn, spare), Map.of("WEAPON", worn.id()));
    ShopOffersReply reply = OnlineShops.offers(7, 123, "Sigfried", sigfried, progress, items);
    assertEquals(7, reply.revision());
    assertEquals(123, reply.gold());
    assertEquals(sigfried.offers().size(), reply.buy().size());
    assertEquals(new ShopOffersReply.Line("item.healing_potion", 21, 5), reply.sell().get(0), "stacks are merged");
    assertEquals(new ShopOffersReply.Line("item.wooden_club", 4, 2), reply.sell().get(1), "the worn club is excluded");
  }
}
