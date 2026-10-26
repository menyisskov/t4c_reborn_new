package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import com.perso.T4C.spawn.definition.SpawnGroups;
import org.junit.jupiter.api.Test;

class OracleProgressionDropsTest {
  @Test
  void allSixGuardianRoomsCanYieldTheirShardAndTheKillableGatekeeperDropsItsKey() {
    for (SpawnGroupDefinition group : SpawnGroups.all()) {
      if (!group.name().startsWith("Oracle Guardian ")) continue;
      for (String type : group.creatures()) {
        MonsterDef guardian = MonsterRegistry.findByName(type);
        assertNotNull(guardian, type);
        assertEquals(
            1.0f,
            dropChance(guardian, "oddly_shaped_shard_of_stone"),
            "The guardian in " + group.name() + " must yield its shard");
      }
    }

    MonsterDef gatekeeper = MonsterRegistry.findByName("ORACLEVULNERABLEGUARDIAN");
    assertNotNull(gatekeeper);
    assertEquals(1.0f, dropChance(gatekeeper, "gleaming_orichalcum_key"));
  }

  private static float dropChance(MonsterDef monster, String item) {
    return monster.getLoot().stream()
        .filter(drop -> item.equals(drop.getItem()))
        .map(MonsterDef.LootDrop::getChance)
        .findFirst()
        .orElse(0.0f);
  }
}
