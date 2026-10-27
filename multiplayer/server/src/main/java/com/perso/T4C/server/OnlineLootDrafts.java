package com.perso.T4C.server;

import com.perso.T4C.item.ItemKeys;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.loot.LootResult;
import com.perso.T4C.monster.loot.MonsterLootTables;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.LootDropDraft;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * One loot roll for a lethal strike. Uses the same table and the same random draws as the offline
 * kill, then fails closed for any item the online path cannot grant safely.
 */
final class OnlineLootDrafts {
  private OnlineLootDrafts() {}

  record Roll(int gold, List<LootDropDraft> drops) {
    Roll {
      drops = List.copyOf(drops);
    }
  }

  static Roll roll(MonsterDef definition, ItemContent items, Random random, int world, double x, double y) {
    LootResult result = MonsterLootTables.of(definition).roll(random);
    var drops = new ArrayList<LootDropDraft>();
    for (String name : result.itemNames()) {
      String key = ItemKeys.normalize(name);
      ItemContent.Definition item = items.items().get(key);
      if (item == null || item.unique()) {
        throw new IllegalArgumentException("Unsupported loot item");
      }
      drops.add(new LootDropDraft(UUID.randomUUID(), drops.size(), key, 1, charges(item), world, x, y));
    }
    return new Roll(result.gold(), drops);
  }

  /** Mirrors InventoryService.add: only charge-limited, non-unlimited items carry charges. */
  static int charges(ItemContent.Definition item) {
    return item.unlimitedUse() || item.nbCharges() <= 0 ? 0 : item.nbCharges();
  }
}
