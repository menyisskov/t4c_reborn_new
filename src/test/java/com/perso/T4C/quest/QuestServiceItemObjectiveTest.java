package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.player.Player;
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
