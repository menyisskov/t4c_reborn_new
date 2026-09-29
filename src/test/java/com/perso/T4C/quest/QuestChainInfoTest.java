package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.i18n.I18n;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuestChainInfoTest {
  @Test
  void avalonJournalStagesMatchOphirasAcceptanceGate() {
    var wilds = QuestChainInfo.stageFor("avalon_wilds_vigil");
    var veil = QuestChainInfo.stageFor("fading_veil_reckoning");
    assertNotNull(wilds);
    assertNotNull(veil);
    assertEquals(wilds.chainName(), veil.chainName());
    assertFalse(I18n.resolve(wilds.chainName()).contains("${"));
    assertEquals(1, wilds.stageNumber());
    assertEquals(2, veil.stageNumber());
    assertEquals(2, wilds.totalStages());
    assertEquals(2, veil.totalStages());
    assertTrue(wilds.prerequisiteQuestIds().isEmpty());
    assertEquals(List.of("avalon_wilds_vigil"), veil.prerequisiteQuestIds());
    assertNotNull(QuestRegistry.findById(veil.prerequisiteQuestIds().getFirst()));
    assertNull(QuestChainInfo.stageFor("the_waking_rite"));
  }

  @Test
  void otherChainsRetainTheirExistingPrerequisites() {
    assertEquals(
        List.of("tideworn_shore_scouts"),
        QuestChainInfo.stageFor("passage_to_avalon").prerequisiteQuestIds());
    assertEquals(
        List.of("forge_the_godcore", "bind_the_godsigil"),
        QuestChainInfo.stageFor("forge_godsforged_warblade").prerequisiteQuestIds());
  }
}
