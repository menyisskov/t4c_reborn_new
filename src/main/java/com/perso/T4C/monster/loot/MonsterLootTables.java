package com.perso.T4C.monster.loot;

import com.perso.T4C.monster.core.MonsterDef;

/** Builds the loot table authored by a monster definition. Shared by offline and online kills. */
public final class MonsterLootTables {
  private MonsterLootTables() {}

  public static LootTable of(MonsterDef definition) {
    LootTable.Builder builder = LootTable.builder().gold(definition.getGoldMin(), definition.getGoldMax());
    if (definition.getLoot() != null) {
      for (MonsterDef.LootDrop drop : definition.getLoot()) {
        if (drop != null && drop.getItem() != null) {
          builder.item(drop.getItem(), drop.getChance());
        }
      }
    }
    return builder.build();
  }
}
