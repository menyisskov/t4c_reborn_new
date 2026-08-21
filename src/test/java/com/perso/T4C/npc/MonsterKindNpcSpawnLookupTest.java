package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import org.junit.jupiter.api.Test;

class MonsterKindNpcSpawnLookupTest {

  @Test
  void hostileNpcSpawnTypesResolveAsNpcsNotMonsterDefs() {
    assertNotNull(NpcFactoryRegistry.find("SKRAUGBIGBRUDDALBASHAH"));
    assertNotNull(NpcFactoryRegistry.find("SkraugBigbruddalbashah"));
    assertNotNull(NpcFactoryRegistry.find("MOBHunter2"));
    assertNull(MonsterRegistry.findByName("SKRAUGBIGBRUDDALBASHAH"));
  }
}
