package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** T4C-0035: a quest can now also require a minimum character level to TURN IN (not to accept or
 * to make kill progress) - see QuestService.meetsMinLevel(). Built for quest/definition/
 * TheWakingRite.java, the Avalon rebirth-rite shortcut, which the owner locked to level 125. */
class QuestServiceMinLevelTest {
  private static final QuestDef LEVEL_GATED_QUEST =
      new QuestDef(
          "test_level_gated_quest",
          "Title",
          "GiverNpc",
          "Test Monster",
          2,
          1,
          100,
          100,
          10,
          500,
          300,
          "Offer",
          "Success",
          "Already completed",
          null,
          null,
          0,
          null,
          null,
          125);

  @Test
  void turnInIsBlockedBelowTheMinLevelAndSucceedsOnceLeveledUp() {
    QuestService service =
        new QuestService(XpCurve.loadDefault(), null, null, () -> List.of(LEVEL_GATED_QUEST));
    Player player = new Player();
    player.setLevel(100);
    service.giveOrReport(LEVEL_GATED_QUEST.getId(), LEVEL_GATED_QUEST.getGiverNpc(), player);
    service.recordKill(player, "Test Monster", 1, 100, 100);
    service.recordKill(player, "Test Monster", 1, 100, 100);
    assertEquals(2, player.getQuestFlag(QuestService.killsFlag(LEVEL_GATED_QUEST)));

    assertNull(
        service.turnInReadyQuests(LEVEL_GATED_QUEST.getGiverNpc(), player),
        "kills alone must not complete a quest that also requires a minimum level");
    assertEquals(
        QuestService.STATUS_ACTIVE,
        player.getQuestFlag(QuestService.statusFlag(LEVEL_GATED_QUEST)));

    player.setLevel(125);
    assertEquals(
        "Success", service.turnInReadyQuests(LEVEL_GATED_QUEST.getGiverNpc(), player));
    assertEquals(
        QuestService.STATUS_COMPLETED,
        player.getQuestFlag(QuestService.statusFlag(LEVEL_GATED_QUEST)));
  }

  @Test
  void reachingTheKillCountUnderLeveledReportsTheLevelRequirementInsteadOfClaimingReady() {
    List<String> messages = new ArrayList<>();
    QuestService service =
        new QuestService(
            XpCurve.loadDefault(), null, messages::add, () -> List.of(LEVEL_GATED_QUEST));
    Player player = new Player();
    player.setLevel(100);
    service.giveOrReport(LEVEL_GATED_QUEST.getId(), LEVEL_GATED_QUEST.getGiverNpc(), player);

    service.recordKill(player, "Test Monster", 1, 100, 100);
    service.recordKill(player, "Test Monster", 1, 100, 100);

    // messages: [0] quest accepted, [1] kill 1/2 progress, [2] kill 2/2 - still under-leveled.
    assertEquals(3, messages.size());
    String finalMessage = messages.get(2);
    assertTrue(
        finalMessage.contains("125"),
        "kills-complete-but-under-leveled must name the required level: " + finalMessage);
    assertFalse(
        finalMessage.toLowerCase(java.util.Locale.ROOT).contains("return to see"),
        "must not use the plain quest_ready wording while under-leveled");
  }

  @Test
  void progressDialogReportsTheLevelRequirementOnceKillsAreDone() {
    QuestService service =
        new QuestService(XpCurve.loadDefault(), null, null, () -> List.of(LEVEL_GATED_QUEST));
    Player player = new Player();
    player.setLevel(100);
    service.giveOrReport(LEVEL_GATED_QUEST.getId(), LEVEL_GATED_QUEST.getGiverNpc(), player);
    service.recordKill(player, "Test Monster", 1, 100, 100);
    service.recordKill(player, "Test Monster", 1, 100, 100);

    String dialog =
        service.giveOrReport(LEVEL_GATED_QUEST.getId(), LEVEL_GATED_QUEST.getGiverNpc(), player);

    assertTrue(dialog.contains("125"), "must report the required level: " + dialog);
  }

  @Test
  void questsWithNoMinLevelAreUnaffected() {
    QuestDef killOnly =
        new QuestDef(
            "test_kill_only_no_level",
            "Title",
            "GiverNpc",
            "Test Monster",
            1,
            1,
            100,
            100,
            10,
            10,
            10,
            "Offer",
            "Success",
            "Already completed");
    QuestService service =
        new QuestService(XpCurve.loadDefault(), null, null, () -> List.of(killOnly));
    Player player = new Player();
    player.setLevel(1);
    service.giveOrReport(killOnly.getId(), killOnly.getGiverNpc(), player);
    service.recordKill(player, "Test Monster", 1, 100, 100);
    assertEquals("Success", service.turnInReadyQuests(killOnly.getGiverNpc(), player));
  }
}
