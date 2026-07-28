package com.perso.T4C.tools;

import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.player.BodyPart;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LighthavenSamaritanQuestMigrationTest {
    @Test
    void changesOnlyTheWorkTopicPayload() {
        NpcDef source = new NpcDef(
                "LighthavenSamaritan",
                "${npc.lighthavensamaritan}",
                List.of(new NpcDef.Part(BodyPart.BODY, "OriginalBody")),
                "PaysanModel1",
                7,
                List.of("${npc.flee_shout.test.0}"),
                "${npc.welcome.lighthavensamaritan}",
                List.of(
                        new NpcDef.DialogTopic(List.of("nom"), "Nom", List.of()),
                        new NpcDef.DialogTopic(List.of("travail", "occupation"), "Ancienne réponse",
                                List.of(new NpcDef.Action(ActionType.HEAL))),
                        new NpcDef.DialogTopic(List.of("partir"), "Au revoir",
                                List.of(new NpcDef.Action(ActionType.END_CONVERSATION)))
                )
        );

        NpcDef migrated = LighthavenSamaritanQuestMigration.update(source);

        assertEquals(source.getName(), migrated.getName());
        assertEquals(source.getDisplayName(), migrated.getDisplayName());
        assertEquals(source.getSpriteBase(), migrated.getSpriteBase());
        assertEquals(source.getPatrolRadiusTiles(), migrated.getPatrolRadiusTiles());
        assertEquals(source.getParts().get(0).getBodyPart(), migrated.getParts().get(0).getBodyPart());
        assertEquals(source.getParts().get(0).getSpriteBase(), migrated.getParts().get(0).getSpriteBase());
        assertEquals(source.getFleeShouts(), migrated.getFleeShouts());
        assertEquals(source.getWelcomeText(), migrated.getWelcomeText());
        assertEquals(source.getTopics().get(0).getResponse(), migrated.getTopics().get(0).getResponse());
        assertEquals(source.getTopics().get(2).getResponse(), migrated.getTopics().get(2).getResponse());

        NpcDef.DialogTopic work = migrated.getTopics().get(1);
        assertEquals(source.getTopics().get(1).getKeywords(), work.getKeywords());
        assertNull(work.getResponse());
        assertEquals(List.of(ActionType.HEAL, ActionType.GIVE_QUEST),
                work.getActions().stream().map(NpcDef.Action::getType).toList());
        assertEquals(List.of(QuestCatalogueSeed.QUEST_ID),
                work.getActions().get(1).getTargets());
    }
}
