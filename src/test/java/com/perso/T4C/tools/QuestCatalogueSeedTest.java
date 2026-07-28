package com.perso.T4C.tools;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.quest.QuestDef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestCatalogueSeedTest {
    @Test
    void upsertIsIdempotent() {
        List<QuestDef> once = QuestCatalogueSeed.upsert(List.of());
        List<QuestDef> twice = QuestCatalogueSeed.upsert(once);

        assertEquals(1, once.size());
        assertEquals(1, twice.size());
        assertEquals(QuestCatalogueSeed.QUEST_ID, twice.get(0).getId());
        assertEquals(10, twice.get(0).getRequiredKills());
        assertEquals(500, twice.get(0).getRewardGold());
        assertEquals(300, twice.get(0).getRewardXp());
    }

    @Test
    void npcCatalogueSeedKeepsTheQuestEnabledSamaritan() {
        NpcDef samaritan = NpcCatalogueSeed.lighthavenSamaritan();

        assertEquals("LighthavenSamaritan", samaritan.getName());
        assertEquals(15, samaritan.getTopics().size());
        assertEquals(List.of("travail", "que faites-vous", "occupation"),
                samaritan.getTopics().get(1).getKeywords().stream().map(I18n::resolve).toList());
        assertEquals(ActionType.GIVE_QUEST,
                samaritan.getTopics().get(1).getActions().get(0).getType());
        assertEquals(List.of(QuestCatalogueSeed.QUEST_ID),
                samaritan.getTopics().get(1).getActions().get(0).getTargets());
        assertEquals(LighthavenSamaritanAppearanceMigration.originalParts().stream()
                        .map(part -> part.getBodyPart() + ":" + part.getSpriteBase()).toList(),
                samaritan.getParts().stream()
                        .map(part -> part.getBodyPart() + ":" + part.getSpriteBase()).toList());
    }
}
