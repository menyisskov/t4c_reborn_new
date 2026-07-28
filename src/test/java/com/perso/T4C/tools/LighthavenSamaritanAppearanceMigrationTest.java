package com.perso.T4C.tools;

import com.perso.T4C.npc.NpcDef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LighthavenSamaritanAppearanceMigrationTest {
    @Test
    void appliesOriginalCppOutfitWithoutChangingDialogueOrIdentity() {
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
        assertNull(migrated.getSpriteBase());
        assertEquals(List.of(
                        "BODY:PupBodyClothSet1",
                        "FEET:PupBlackLeatherBoots",
                        "HEAD:PupElvenHat",
                        "LEGS:PupLeatherPants"),
                migrated.getParts().stream()
                        .map(part -> part.getBodyPart() + ":" + part.getSpriteBase())
                        .toList());
    }
}
