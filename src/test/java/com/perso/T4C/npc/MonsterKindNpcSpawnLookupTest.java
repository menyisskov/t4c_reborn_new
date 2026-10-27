package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.NpcSpawnOwnership;
import org.junit.jupiter.api.Test;

class MonsterKindNpcSpawnLookupTest {

  @Test
  void hostileNpcSpawnTypesResolveAsNpcsNotMonsterDefs() {
    assertNotNull(NpcFactoryRegistry.find("SKRAUGBIGBRUDDALBASHAH"));
    assertNotNull(NpcFactoryRegistry.find("SkraugBigbruddalbashah"));
    assertNotNull(NpcFactoryRegistry.find("MOBHunter2"));
    assertNull(MonsterRegistry.findByName("SKRAUGBIGBRUDDALBASHAH"));
    assertNotNull(NpcSpawnOwnership.resolveStrict(" sUnDiAl "));
    assertNotNull(NpcSpawnOwnership.resolveStrict("MOBHunter2"));
    assertNull(NpcSpawnOwnership.resolveStrict("unknown-ordinary-monster"));
  }

  @Test
  void npcAliasWithMonsterDefinitionKeepsOfflineMonsterSpawn() throws Exception {
    String type = "SKRAUGGRUBBRINGAH";
    assertNotNull(NpcFactoryRegistry.find(type));
    assertNotNull(NpcSpawnOwnership.resolveStrict(type));
    var definition = MonsterRegistry.findByName(type);
    assertNotNull(definition);
    var source = com.perso.T4C.spawn.SpawnRegistry.monsters().stream()
        .filter(spawn -> type.equals(spawn.type()) && spawn.z() == 0)
        .findFirst().orElseThrow();
    var manager = new MonsterManager(null);
    manager.initializeMonstersFromMap("assets/maps/worldmap/worldmap.mapbin");
    manager.updateVisible(0f, null, source.x(), source.x(), source.y(), source.y(), 0);
    assertTrue(manager.getMonsters().stream().anyMatch(monster ->
        definition.getName().equals(monster.getCanonicalName())
            && monster.getSpawnTileX() == source.x()
            && monster.getSpawnTileY() == source.y()),
        "Offline MonsterManager must retain a defined monster despite its NPC alias");
  }
}
