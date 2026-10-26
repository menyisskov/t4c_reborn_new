package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.definition.ConcordCampaign;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestJournalTest {
  @BeforeEach
  void loadItems() {
    ItemJsonLoader.loadAndRegister();
  }

  @AfterEach
  void resetItems() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  @Test
  void refreshIncludesNewQuestsAndTracksBothKillsAndItemsWithoutChangingProgress() {
    Player player = new Player();
    player.setLevel(400);
    QuestDef quest = QuestRegistry.findById("passage_to_avalon");
    assertTrue(QuestJournal.entries(player).isEmpty());
    player.setQuestFlag(QuestService.statusFlag(quest), QuestService.STATUS_ACTIVE);
    player.setQuestFlag(QuestService.killsFlag(quest), quest.getRequiredKills());
    Map<String, Integer> flags = Map.copyOf(player.getQuestFlags());

    var entry = QuestJournal.entries(player).getFirst();
    String itemName = I18n.resolve(ItemRegistry.findByKey(quest.getRequiredItemKey()).getName());
    assertEquals(QuestJournal.Kind.MAIN_STORY, entry.kind());
    assertTrue(entry.objectives().stream().anyMatch(text -> text.contains(itemName)));
    assertEquals(
        I18n.message("journal.collect", quest.getRequiredItemQty(), itemName), entry.nextStep());

    player.getInventory().add(quest.getRequiredItemKey());
    assertEquals(I18n.key("journal.ready"), QuestJournal.entryFor(player, quest).stage());
    player.getInventory().clear();
    assertEquals(entry.nextStep(), QuestJournal.entryFor(player, quest).nextStep());
    assertEquals(flags, player.getQuestFlags(), "reading the journal must never advance a quest");

    player.setQuestFlag(QuestService.statusFlag(quest), QuestService.STATUS_COMPLETED);
    entry = QuestJournal.entries(player).getFirst();
    assertTrue(entry.completed());
    assertEquals(
        java.util.List.of(I18n.key("journal.complete")),
        entry.objectives(),
        "consumed proof must not look like an unfinished objective after completion");
  }

  @Test
  void finishedObjectivesStillRespectTurnInLevel() {
    Player player = new Player();
    QuestDef quest = QuestRegistry.findById("the_waking_rite");
    player.setLevel(quest.getMinLevel() - 1);
    player.setQuestFlag(QuestService.statusFlag(quest), QuestService.STATUS_ACTIVE);
    player.setQuestFlag(QuestService.killsFlag(quest), quest.getRequiredKills());
    var entry = QuestJournal.entryFor(player, quest);
    assertEquals(QuestJournal.Kind.SIDE_QUEST, entry.kind());
    assertEquals(I18n.key("journal.level_required"), entry.stage());
    assertEquals(I18n.message("journal.reach_level", quest.getMinLevel()), entry.nextStep());
    player.setLevel(quest.getMinLevel());
    assertEquals(I18n.key("journal.ready"), QuestJournal.entryFor(player, quest).stage());
  }

  @Test
  void investigationMovesFromEvidenceToDecisionToRewardAndReport() {
    Player player = new Player();
    player.setLevel(400);
    QuestDef quest = ConcordCampaign.all().getFirst();
    player.setQuestFlag(QuestService.statusFlag(quest), QuestService.STATUS_ACTIVE);
    var entry = QuestJournal.entryFor(player, quest);
    assertTrue(entry.nextStep().contains("\"record\""));
    player.setQuestFlag(ConcordCampaign.flag(0, "site0"), 1);
    assertTrue(QuestJournal.entryFor(player, quest).nextStep().contains("\"echo\""));
    player.setQuestFlag(ConcordCampaign.flag(0, "site0"), 3);
    assertEquals(I18n.key("journal.decision"), QuestJournal.entryFor(player, quest).stage());
    player.getInventory().add(quest.getRequiredItemKey());
    assertEquals(I18n.key("journal.reward_choice"), QuestJournal.entryFor(player, quest).stage());
    player.setQuestFlag("concord.style", 1);
    assertEquals(
        I18n.message("journal.concord_report", QuestService.giverDisplayName(quest)),
        QuestJournal.entryFor(player, quest).nextStep());
  }

  @Test
  void completedBranchPointsToConvergingQuestWithoutDemandingBothBranches() {
    Player player = new Player();
    QuestDef quest = QuestRegistry.findById("pale_cantor");
    QuestDef next = QuestRegistry.findById("ashbound_exiles");
    player.setQuestFlag(QuestService.statusFlag(quest), QuestService.STATUS_COMPLETED);
    assertEquals(
        I18n.message(
            "journal.continue_with",
            QuestService.giverDisplayName(next),
            I18n.resolve(next.getTitle())),
        QuestJournal.entryFor(player, quest).nextStep());
    assertEquals(
        QuestJournal.Kind.SIDE_QUEST,
        QuestJournal.kind(QuestRegistry.findById("forge_godsforged_warblade")));
  }

  @Test
  void legacyDialogueStagesAndExpiredTimedTasksRemainVisible() {
    Player player = new Player();
    AllianceQuest.stage(player, AllianceQuest.ANSWER_RIDDLE);
    AudienceToKingQuest.accept(
        player, AudienceToKingQuest.nowSeconds() - AudienceToKingQuest.TIME_LIMIT_SECONDS - 60);
    player.setQuestFlag(UnsignedLetterQuest.FLAG_STAGE, UnsignedLetterQuest.STAGE_RESOLVED);
    var entries = QuestJournal.entries(player);
    var alliance =
        entries.stream().filter(e -> e.id().equals("alliance")).findFirst().orElseThrow();
    assertEquals(I18n.key("journal.alliance.next.7"), alliance.nextStep());
    var audience =
        entries.stream().filter(e -> e.id().equals("king_audience")).findFirst().orElseThrow();
    assertEquals(I18n.key("journal.expired"), audience.stage());
    assertEquals(I18n.key("journal.audience.restart"), audience.nextStep());
    assertTrue(entries.stream().anyMatch(e -> e.id().equals("unsigned_letter") && e.completed()));
    assertTrue(
        entries.stream().noneMatch(e -> (e.title() + e.stage() + e.nextStep()).contains("${")));
  }
}
