package com.perso.T4C;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.helper.QuestDefBinaryIO;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.quest.QuestDef;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class T4CContentStudioQuestValidationTest {
    @Test
    void giveQuestAcceptsOneKnownQuestOnly() {
        T4CContentStudio studio = new T4CContentStudio();

        assertDoesNotThrow(() -> studio.validateActionTargets(
                "LighthavenSamaritan", ActionType.GIVE_QUEST,
                List.of("lighthaven_samaritan_rats")));

        RuntimeException missingTarget = assertThrows(RuntimeException.class,
                () -> studio.validateActionTargets(
                        "LighthavenSamaritan", ActionType.GIVE_QUEST, List.of()));
        assertTrue(missingTarget.getMessage().contains("exactly one quest"));

        RuntimeException unknownQuest = assertThrows(RuntimeException.class,
                () -> studio.validateActionTargets(
                        "LighthavenSamaritan", ActionType.GIVE_QUEST,
                        List.of("unknown_quest")));
        assertTrue(unknownQuest.getMessage().contains("unknown quest"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void editorPayloadsExposeResolvedTextAndQuestChoices() throws Exception {
        T4CContentStudio studio = new T4CContentStudio();
        NpcDef samaritan = NpcDefBinaryIO.read(new File(Paths.NPCS_BIN)).stream()
                .filter(definition -> "LighthavenSamaritan".equals(definition.getName()))
                .findFirst()
                .orElseThrow();
        Map<String, Object> npcPayload = studio.npcToMap(samaritan);
        List<Map<String, Object>> topics = (List<Map<String, Object>>) npcPayload.get("topics");
        Map<String, Object> work = topics.get(1);

        assertEquals(List.of("travail", "que faites-vous", "occupation"), work.get("keywords"));
        assertTrue(((List<String>) work.get("keywords")).stream()
                .noneMatch(value -> value.contains("${")));
        List<Map<String, Object>> actions = (List<Map<String, Object>>) work.get("actions");
        assertEquals("GIVE_QUEST", actions.get(0).get("type"));
        assertEquals(List.of("lighthaven_samaritan_rats"), actions.get(0).get("targets"));

        QuestDef quest = QuestDefBinaryIO.read(new File(Paths.QUESTS_BIN)).get(0);
        Map<String, Object> questPayload = studio.questToMap(quest);
        assertEquals("Les rats du sous-sol du temple", questPayload.get("title"));
        assertFalse(String.valueOf(questPayload.get("offerText")).contains("${"));
        assertFalse(String.valueOf(questPayload.get("completionText")).contains("${"));
        assertFalse(String.valueOf(questPayload.get("completedText")).contains("${"));
    }
}
