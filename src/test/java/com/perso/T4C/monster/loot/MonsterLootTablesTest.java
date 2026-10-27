package com.perso.T4C.monster.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import com.perso.T4C.item.json.ItemJsonLoader;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MonsterLootTablesTest {
  @Test
  void sharedBuilderMatchesDataMonsterTableForSeededRolls() throws Exception {
    ItemJsonLoader.loadAndRegister();
    MonsterJsonLoader.loadAndRegister();
    MonsterDef skeleton = MonsterRegistry.findByName("Skeleton");
    DataMonster monster = new DataMonster(skeleton, 0, 0);
    assertEquals(
        monster.getLootTable().getGoldMin(), MonsterLootTables.of(skeleton).getGoldMin());
    assertEquals(
        monster.getLootTable().getGoldMax(), MonsterLootTables.of(skeleton).getGoldMax());
    assertEquals(
        monster.getLootTable().getEntries(), MonsterLootTables.of(skeleton).getEntries());
    for (long seed = 1; seed <= 50; seed++) {
      assertEquals(
          monster.getLootTable().roll(new Random(seed)),
          MonsterLootTables.of(skeleton).roll(new Random(seed)));
    }
  }
}
