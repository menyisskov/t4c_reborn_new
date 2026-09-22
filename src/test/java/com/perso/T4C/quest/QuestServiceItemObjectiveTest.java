package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** T4C-0019: quests can now also require turning in a collected item (on top of the pre-existing
 * kill-count objective), and can set a durable "zone unlock" flag on completion. */
class QuestServiceItemObjectiveTest {
  private static final QuestDef ITEM_QUEST =
      new QuestDef(
          "test_item_quest",
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
          "rare_test_relic",
          2,
          "test_zone");

  @Test
  void turnInIsBlockedUntilTheRequiredItemQuantityIsHeld() {
    QuestService service =
        new QuestService(XpCurve.loadDefault(), null, null, () -> List.of(ITEM_QUEST));
    Player player = new Player();
    service.giveOrReport(ITEM_QUEST.getId(), ITEM_QUEST.getGiverNpc(), player);
    service.recordKill(player, "Test Monster", 1, 100, 100);
    service.recordKill(player, "Test Monster", 1, 100, 100);
    assertEquals(2, player.getQuestFlag(QuestService.killsFlag(ITEM_QUEST)));

    assertNull(
        service.turnInReadyQuests(ITEM_QUEST.getGiverNpc(), player),
        "kills alone must not complete a quest that also requires an item");

    player.getInventory().add("rare_test_relic");
    assertNull(
        service.turnInReadyQuests(ITEM_QUEST.getGiverNpc(), player),
        "only one of the required two copies is held");

    player.getInventory().add("rare_test_relic");
    assertEquals("Success", service.turnInReadyQuests(ITEM_QUEST.getGiverNpc(), player));
    assertEquals(
        QuestService.STATUS_COMPLETED, player.getQuestFlag(QuestService.statusFlag(ITEM_QUEST)));
  }

  @Test
  void completionConsumesTheExactRequiredQuantityAndSetsTheZoneUnlockFlag() {
    QuestService service =
        new QuestService(XpCurve.loadDefault(), null, null, () -> List.of(ITEM_QUEST));
    Player player = new Player();
    player.getInventory().add("rare_test_relic");
    player.getInventory().add("rare_test_relic");
    player.getInventory().add("rare_test_relic"); // a spare third copy, must survive turn-in
    player.getInventory().add("unrelated_item");
    service.giveOrReport(ITEM_QUEST.getId(), ITEM_QUEST.getGiverNpc(), player);
    service.recordKill(player, "Test Monster", 1, 100, 100);
    service.recordKill(player, "Test Monster", 1, 100, 100);

    assertFalse(QuestService.hasUnlockedZone(player, "test_zone"));
    assertEquals("Success", service.turnInReadyQuests(ITEM_QUEST.getGiverNpc(), player));

    assertEquals(
        1,
        java.util.Collections.frequency(player.getInventory(), "rare_test_relic"),
        "exactly two of the three copies should have been consumed");
    assertTrue(player.getInventory().contains("unrelated_item"));
    assertTrue(
        QuestService.hasUnlockedZone(player, "test_zone"),
        "completing an unlockZoneId quest must set the durable zone-unlock flag");
    assertEquals(
        1, player.getQuestFlag(QuestService.zoneUnlockFlag("test_zone")), "flag value is 1");
  }

  @Test
  void reachingTheKillCountWithoutTheItemDoesNotAnnounceReady() {
    List<String> messages = new ArrayList<>();
    QuestService service =
        new QuestService(XpCurve.loadDefault(), null, messages::add, () -> List.of(ITEM_QUEST));
    Player player = new Player();
    service.giveOrReport(ITEM_QUEST.getId(), ITEM_QUEST.getGiverNpc(), player);

    service.recordKill(player, "Test Monster", 1, 100, 100);
    service.recordKill(player, "Test Monster", 1, 100, 100);

    // messages: [0] quest accepted, [1] kill 1/2 progress, [2] kill 2/2 - still needs the item.
    assertEquals(3, messages.size());
    String finalMessage = messages.get(2);
    assertTrue(
        finalMessage.contains("rare_test_relic"),
        "kills-complete-but-item-missing must name the still-needed item, not claim the quest "
            + "is ready: " + finalMessage);
    assertFalse(
        finalMessage.toLowerCase(java.util.Locale.ROOT).contains("return to see"),
        "must not use the plain quest_ready wording while the item is still missing");
  }

  @Test
  void progressDialogNamesTheStillNeededItemAndHowManyAreHeld() {
    QuestService service =
        new QuestService(XpCurve.loadDefault(), null, null, () -> List.of(ITEM_QUEST));
    Player player = new Player();
    service.giveOrReport(ITEM_QUEST.getId(), ITEM_QUEST.getGiverNpc(), player);
    service.recordKill(player, "Test Monster", 1, 100, 100);
    service.recordKill(player, "Test Monster", 1, 100, 100);
    player.getInventory().add("rare_test_relic");

    String dialog = service.giveOrReport(ITEM_QUEST.getId(), ITEM_QUEST.getGiverNpc(), player);

    assertTrue(dialog.contains("2/2"), "kill progress must still be reported: " + dialog);
    assertTrue(
        dialog.contains("rare_test_relic") && dialog.contains("1/2"),
        "must report the item objective and how many of it are held: " + dialog);
  }

  @Test
  void aQuestAlreadyCompletedBeforeUnlockZoneIdExistedStillUnlocksItsZone() {
    QuestDef realQuest = QuestRegistry.findById("windhowl_marches_centaurs");
    assertNotNull(realQuest, "a real quest with a real unlockZoneId must exist for this test");
    assertNotNull(realQuest.getUnlockZoneId());
    Player player = new Player();
    // Simulate a character who finished this quest on an older save, before this pass added the
    // unlockZoneId/explicit flag - only the completion status survives, never the new flag.
    player.setQuestFlag(QuestService.statusFlag(realQuest), QuestService.STATUS_COMPLETED);

    assertTrue(
        QuestService.hasUnlockedZone(player, realQuest.getUnlockZoneId()),
        "a previously-completed quest must still grant its zone unlock, not just a fresh one");
  }

  @Test
  void questsWithNoItemObjectiveAreUnaffected() {
    QuestDef killOnly =
        new QuestDef(
            "test_kill_only",
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
    service.giveOrReport(killOnly.getId(), killOnly.getGiverNpc(), player);
    service.recordKill(player, "Test Monster", 1, 100, 100);
    assertEquals("Success", service.turnInReadyQuests(killOnly.getGiverNpc(), player));
  }
}
