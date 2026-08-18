package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcDef;
import java.util.List;
import org.junit.jupiter.api.Test;

class LighthavenSamaritanAppearanceMigrationTest {
  @Test
  void appliesEffectiveOriginalSimpleAppearanceWithoutChangingDialogueOrIdentity() {
    NpcDef source =
        new NpcDef(
            "LighthavenSamaritan",
            "${npc.lighthavensamaritan}",
            List.of(),
            "PaysanModel1",
            3,
            List.of("shout"),
            "welcome",
            List.of(new NpcDef.DialogTopic(List.of("travail"), null, List.of())));
    NpcDef migrated = LighthavenSamaritanAppearanceMigration.update(source);
    assertEquals(source.getName(), migrated.getName());
    assertEquals(source.getDisplayName(), migrated.getDisplayName());
    assertEquals(source.getPatrolRadiusTiles(), migrated.getPatrolRadiusTiles());
    assertEquals(source.getFleeShouts(), migrated.getFleeShouts());
    assertEquals(source.getWelcomeText(), migrated.getWelcomeText());
    assertEquals(source.getTopics(), migrated.getTopics());
    assertEquals("PaysanModel1", migrated.getSpriteBase());
    assertEquals(List.of(), migrated.getParts());
  }
}
