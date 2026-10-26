package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MonsterLootRegistryIntegrityTest {
  @Test
  void everyMonsterDropResolvesToAnItem() {
    ItemJsonLoader.loadAndRegister();
    MonsterJsonLoader.loadAndRegister();
    List<String> missing = new ArrayList<>();
    for (MonsterDef monster : MonsterRegistry.load()) {
      for (MonsterDef.LootDrop drop : monster.getLoot()) {
        if (ItemDefinition.get(drop.getItem()) == null)
          missing.add(monster.getName() + " -> " + drop.getItem());
      }
    }
    assertTrue(missing.isEmpty(), "Unobtainable monster drops: " + missing);
  }
}
