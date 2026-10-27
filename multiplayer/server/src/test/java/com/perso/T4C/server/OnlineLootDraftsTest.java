package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.Gson;
import com.perso.T4C.item.ItemKeys;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.loot.MonsterLootTables;
import com.perso.T4C.online.model.ItemContent;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class OnlineLootDraftsTest {
  @Test void seededRollMatchesOfflineGoldAndItemsAndFailsClosedForUnsupportedDrops() throws Exception {
    var items = ItemCatalog.loadBundled();
    var skeleton = MonsterCatalog.loadBundled().monsters().get("Skeleton");
    for (long seed = 1; seed <= 200; seed++) {
      var offline = MonsterLootTables.of(skeleton).roll(new Random(seed));
      var online = OnlineLootDrafts.roll(skeleton, items, new Random(seed), 3, 10.5, 20.5);
      assertEquals(offline.gold(), online.gold());
      assertEquals(offline.itemNames().stream().map(ItemKeys::normalize).toList(),
          online.drops().stream().map(d -> d.key()).toList());
    }
    assertEquals(1, OnlineLootDrafts.charges(items.items().get("item.healing_potion")));

    var gson = new Gson();
    var unknown = gson.toJsonTree(skeleton).getAsJsonObject();
    unknown.add("loot", gson.toJsonTree(List.of(new MonsterDef.LootDrop("Not a real item", 1f))));
    var unknownDef = gson.fromJson(unknown, MonsterDef.class);
    assertThrows(IllegalArgumentException.class,
        () -> OnlineLootDrafts.roll(unknownDef, items, new Random(1), 0, 0, 0));

    var uniqueContent = gson.toJsonTree(items).getAsJsonObject();
    uniqueContent.getAsJsonObject("items").getAsJsonObject("item.leather_gloves").addProperty("unique", true);
    var unique = gson.fromJson(uniqueContent, ItemContent.class);
    var gloves = gson.toJsonTree(skeleton).getAsJsonObject();
    gloves.add("loot", gson.toJsonTree(List.of(new MonsterDef.LootDrop("Leather gloves", 1f))));
    var glovesDef = gson.fromJson(gloves, MonsterDef.class);
    assertThrows(IllegalArgumentException.class,
        () -> OnlineLootDrafts.roll(glovesDef, unique, new Random(1), 0, 0, 0));
  }
}
