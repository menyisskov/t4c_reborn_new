package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OnlineNpcContentExporterTest {
  @Test void exportsTheAuthoredLighthavenSamaritanSpawnAndAppearance() {
    var npc=OnlineNpcContentExporter.snapshot().npcs().stream()
        .filter(candidate->candidate.id().equals("LighthavenSamaritan"))
        .findFirst().orElseThrow();
    assertEquals(0,npc.world());
    assertEquals(2931,npc.x());
    assertEquals(1072,npc.y());
    assertEquals("${npc.lighthavensamaritan}",npc.displayName());
    assertEquals("PaysanModel1",npc.spriteBase());
    assertTrue(npc.parts().isEmpty());
  }
}
