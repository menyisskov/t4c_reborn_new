package com.perso.T4C.tools;

import com.perso.T4C.npc.NpcDef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LighthavenSamaritanAppearanceMigrationTest {
    @Test
    void appliesEffectiveOriginalSimpleAppearanceWithoutChangingDialogueOrIdentity() {
        NpcDef source = new NpcDef(
                "LighthavenSamaritan",
                "${npc.lighthavensamaritan}",
                List.of(),
                "PaysanModel1",
                3,
                List.of("shout"),
                "welcome",
                List.of(new NpcDef.DialogTopic(List.of("travail"), null, List.of()))
        );

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
