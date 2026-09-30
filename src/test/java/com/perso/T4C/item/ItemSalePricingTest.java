package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.gui.widget.ItemTooltipText;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.item.json.ItemJsonDef;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import com.perso.T4C.player.Player;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;

class ItemSalePricingTest {
  @BeforeEach
  void loadContent() {
    ItemJsonLoader.loadAndRegister();
    MonsterJsonLoader.loadAndRegister();
  }

  @AfterEach
  void resetContent() {
    ItemRegistry.resetAdditionalDefinitions();
    MonsterRegistry.resetToGeneratedDefinitions();
  }

  @Test
  void everyNewDroppableItemHasMeaningfulResale() throws Exception {
    try (var paths = Files.list(Path.of("assets/items"))) {
      var files = paths.filter(p -> p.toString().endsWith(".json")).toList();
      assertFalse(files.isEmpty());
      for (Path path : files) {
        var json = new Gson().fromJson(Files.readString(path), ItemJsonDef.class);
        var item = ItemRegistry.findByKey(json.key);
        assertNotNull(item, path.toString());
        assertTrue(item.isRarityPriced(), item.getKey());
        if (!item.isUndroppable()) assertTrue(ItemSalePricing.sellPrice(item) > 1, item.getKey());
        if (item.getPrice() > 0)
          assertEquals(Math.max(1, item.getPrice() / 2), ItemSalePricing.sellPrice(item));
      }
    }
    for (var item : ItemRegistry.allByKey().values()) {
      if (item.isRarityPriced() && !item.isUndroppable())
        assertTrue(ItemSalePricing.sellPrice(item) > 1, item.getKey());
    }
    for (String key :
        List.of(
            "item.rootcrown_wyrm_scale",
            "item.flyers_barbed_key",
            "item.unsigned_letter",
            "item.wyrmforged_ember",
            "item.veiled_aether_shard",
            "item.moonwake_bell_shard",
            "item.last_witness_seal",
            "item.tempered_godcore",
            "item.bound_godsigil")) {
      var item = ItemRegistry.findByKey(key);
      assertNotNull(item, key);
      assertTrue(ItemSalePricing.sellPrice(item) > 1, key);
    }
  }

  @Test
  void newStockCannotBeBoughtAndResoldForProfit() {
    var shop =
        (com.perso.T4C.npc.behavior.ShopBehavior)
            com.perso.T4C.npc.catalog.ShopCatalog.get("LordoftheShops");
    for (String key : shop.items()) {
      var item = ItemRegistry.findByKey(key);
      if (item != null && item.isRarityPriced()) {
        assertTrue(item.getPrice() > ItemSalePricing.sellPrice(item), key);
        assertTrue(ItemSalePricing.sellPrice(item) > 1, key);
      }
    }
  }

  @Test
  void rarerDropsSellForMore() {
    double[] chances = {.08, .04, .025, .01, .003};
    long[] values = {1250, 2500, 4000, 10000, 33333};
    for (int i = 0; i < chances.length; i++)
      assertEquals(values[i], ItemSalePricing.priceForDropChance(chances[i]));
    assertThrows(IllegalArgumentException.class, () -> ItemSalePricing.priceForDropChance(0));
    assertThrows(
        IllegalArgumentException.class, () -> ItemSalePricing.priceForDropChance(Double.NaN));
  }

  @Test
  void celestialAndEmpyreanArmorKeepTheirEndgameResaleValue() {
    long ancient =
        ItemSalePricing.sellPrice(ItemRegistry.findByKey("item.ancient_celestial_fire_armor"));
    long empyrean =
        ItemSalePricing.sellPrice(ItemRegistry.findByKey("item.empyrean_fire_armor"));
    long rarerEmpyrean =
        ItemSalePricing.sellPrice(ItemRegistry.findByKey("item.empyrean_light_armor"));

    assertEquals(200_000, ancient);
    assertTrue(empyrean > ancient);
    assertTrue(rarerEmpyrean > empyrean);
  }

  @Test
  void easiestSourceIncludesRepeatedIndependentRollsAndRefreshesAfterReload() {
    var base = MonsterRegistry.load().getFirst();
    var easy =
        base.withLoot(
            List.of(
                new MonsterDef.LootDrop("resale_test", .1f),
                new MonsterDef.LootDrop("item.resale_test", .1f)));
    var rare = base.withLoot(List.of(new MonsterDef.LootDrop("item.resale_test", .01f)));
    MonsterRegistry.registerDefinitions(List.of(easy, rare));
    assertEquals(.19, ItemSalePricing.easiestDropChance("resale_test"), .000001);
    MonsterRegistry.registerDefinitions(List.of(rare));
    assertEquals(.01, ItemSalePricing.easiestDropChance("resale_test"), .000001);
  }

  @Test
  void buyPricesLegacyItemsAndProtectedItemsKeepTheirContracts() {
    var json = new ItemJsonDef();
    json.key = "resale_test";
    json.bodyPart = "BODY";
    var item = json.toItemDefinition();
    assertEquals(2500, ItemSalePricing.sellPrice(item));
    item.setRarityPriced(false);
    assertEquals(1, ItemSalePricing.sellPrice(item));
    json.price = 450;
    assertEquals(225, ItemSalePricing.sellPrice(json.toItemDefinition()));
    json.undroppable = true;
    assertEquals(0, ItemSalePricing.sellPrice(json.toItemDefinition()));
    assertEquals(0, ItemSalePricing.sellPrice(null));
    json.price = 0;
    json.undroppable = false;
    json.unique = true;
    assertEquals(25000, ItemSalePricing.sellPrice(json.toItemDefinition()));
    json.key = "godsforged_resale_test";
    assertEquals(100000, ItemSalePricing.sellPrice(json.toItemDefinition()));
  }

  @Test
  void existingSavedInventoryReceivesCurrentPriceAndOnlyOwnedUnitsArePaid() {
    String key = "item.ancient_celestial_fire_armor";
    Player old = new Player();
    old.setGold(30);
    old.setInventory(new ArrayList<>(List.of(key, key, key)));
    var save = PlayerStateMapper.fromPlayer(old);
    Player loaded = new Player();
    PlayerStateMapper.applyToPlayer(save, loaded);
    long price = ItemSalePricing.sellPrice(ItemRegistry.findByKey(key));
    assertTrue(price > 1);
    assertEquals(price * 2, ItemSaleService.sell(loaded, key, 2));
    assertEquals(1, InventoryService.count(loaded, key));
    assertEquals(price, ItemSaleService.sell(loaded, key, 10));
    assertEquals(30 + 3 * price, loaded.getGold());
    assertEquals(0, ItemSaleService.sell(loaded, key, 10));
    assertTrue(ItemTooltipText.build(key).contains("Sell value: " + price + " gold"));
  }

  @Test
  void salesRespectGoldCapAndRejectUnknownOrProtectedItems() {
    String key = "item.ancient_celestial_fire_armor";
    Player player = new Player();
    player.setGold(Integer.MAX_VALUE - 10);
    player.setInventory(new ArrayList<>(List.of(key)));
    assertEquals(0, ItemSaleService.sell(player, key, 0));
    assertEquals(0, ItemSaleService.sell(player, "item.missing_test", 1));
    assertEquals(10, ItemSaleService.sell(player, key, 1));
    assertEquals(Integer.MAX_VALUE, player.getGold());
    assertEquals(0, InventoryService.count(player, key));
    var json = new ItemJsonDef();
    json.key = "protected_test";
    json.bodyPart = "BODY";
    json.undroppable = true;
    ItemRegistry.registerAdditionalDefinitions(List.of(json.toItemDefinition()));
    player.getInventory().add("item.protected_test");
    assertEquals(0, ItemSaleService.sell(player, "protected_test", 1));
    assertEquals(1, InventoryService.count(player, "protected_test"));
  }
}
