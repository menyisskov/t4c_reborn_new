package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcDef;
import com.perso.T4C.quest.QuestDef;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuestCatalogueSeedTest {
  @Test
  void upsertIsIdempotent() {
    List<QuestDef> once = QuestCatalogueSeed.upsert(List.of());
    List<QuestDef> twice = QuestCatalogueSeed.upsert(once);
    assertEquals(2, once.size());
    assertEquals(2, twice.size());
    QuestDef rats =
        twice.stream()
            .filter(quest -> QuestCatalogueSeed.QUEST_ID.equals(quest.getId()))
            .findFirst()
            .orElseThrow();
    assertEquals(15, rats.getRequiredKills());
    assertEquals(0, rats.getRewardGold());
    assertEquals(2_500, rats.getRewardXp());
    QuestDef goblins =
        twice.stream()
            .filter(quest -> QuestCatalogueSeed.ORTANALAS_GOBLINS_QUEST_ID.equals(quest.getId()))
            .findFirst()
            .orElseThrow();
    assertEquals(15, goblins.getRequiredKills());
    assertEquals(1_000, goblins.getRewardGold());
    assertEquals(750, goblins.getRewardXp());
  }

  @Test
  void npcCatalogueSeedKeepsTheQuestEnabledSamaritan() {
    NpcDef samaritan = NpcCatalogueSeed.lighthavenSamaritan();
    assertEquals("LighthavenSamaritan", samaritan.getName());
    assertEquals(15, samaritan.getTopics().size());
    assertEquals(3, samaritan.getTopics().get(1).getKeywords().size());
    assertEquals(ActionType.GIVE_QUEST, samaritan.getTopics().get(1).getActions().get(0).getType());
    assertEquals(
        List.of(QuestCatalogueSeed.QUEST_ID),
        samaritan.getTopics().get(1).getActions().get(0).getTargets());
    assertEquals(
        LighthavenSamaritanAppearanceMigration.originalParts().stream()
            .map(part -> part.getBodyPart() + ":" + part.getSpriteBase())
            .toList(),
        samaritan.getParts().stream()
            .map(part -> part.getBodyPart() + ":" + part.getSpriteBase())
            .toList());
  }

  @Test
  void npcCatalogueSeedAddsOrtanalasQuestWithoutRemovingTraining() {
    NpcDef ortanalas = NpcCatalogueSeed.ortanalas();
    assertEquals("Ortanalas", ortanalas.getName());
    assertEquals(3, ortanalas.getTopics().size());
    assertEquals(
        ActionType.OPEN_SKILL_LEARNING, ortanalas.getTopics().get(1).getActions().get(0).getType());
    assertEquals(ActionType.GIVE_QUEST, ortanalas.getTopics().get(2).getActions().get(0).getType());
    assertEquals(
        List.of(QuestCatalogueSeed.ORTANALAS_GOBLINS_QUEST_ID),
        ortanalas.getTopics().get(2).getActions().get(0).getTargets());
  }
}
