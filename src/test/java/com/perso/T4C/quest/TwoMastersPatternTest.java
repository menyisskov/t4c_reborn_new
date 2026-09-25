package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.player.Player;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * T4C-0046: the "Two Masters" quest pattern (see DESIGN_GUIDELINES.md), exercised on a stand-in
 * quest with the same shape as {@code passage_to_kraanhold} - an immediate master (the quest's
 * own {@code giverNpc}, normal {@link QuestService#turnInReadyQuests}) and a second master using
 * {@link QuestService#completeWithAlternateReward}, mutually exclusive.
 */
class TwoMastersPatternTest {
  private static final QuestDef QUEST =
      new QuestDef(
          "two_masters_test_quest",
          "Title",
          "ImmediateMaster",
          "Toll Troll",
          3,
          0,
          1000,
          1000,
          50,
          4000,
          250000,
          "Offer",
          "Completion",
          "Already completed",
          null,
          null,
          0,
          "test_zone",
          null,
          0);

  private static QuestService service(Runnable persist) {
    return new QuestService(XpCurve.loadDefault(), persist, null, () -> List.of(QUEST));
  }

  private static Player readyPlayer() {
    Player player = new Player();
    player.setQuestFlag(QuestService.statusFlag(QUEST), QuestService.STATUS_ACTIVE);
    player.setQuestFlag(QuestService.killsFlag(QUEST), QUEST.getRequiredKills());
    return player;
  }

  @Test
  void alternateMasterRefusesBeforeTheQuestIsReady() {
    Player player = new Player();
    player.setQuestFlag(QuestService.statusFlag(QUEST), QuestService.STATUS_ACTIVE);
    player.setQuestFlag(QuestService.killsFlag(QUEST), 1); // short of the 3 required
    QuestService service = service(null);
    AtomicInteger perkGrants = new AtomicInteger();

    String result =
        service.completeWithAlternateReward(QUEST.getId(), player, perkGrants::incrementAndGet);

    assertNull(result);
    assertEquals(0, perkGrants.get());
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, QUEST));
  }

  @Test
  void alternateMasterGrantsThePerkInsteadOfGoldAndXpAndUnlocksTheZone() {
    Player player = readyPlayer();
    int goldBefore = player.getGold();
    long xpBefore = player.getCurrentXp();
    AtomicInteger saves = new AtomicInteger();
    AtomicInteger perkGrants = new AtomicInteger();
    QuestService service = service(saves::incrementAndGet);

    String result =
        service.completeWithAlternateReward(QUEST.getId(), player, perkGrants::incrementAndGet);

    assertEquals("Completion", result);
    assertEquals(1, perkGrants.get());
    assertEquals(goldBefore, player.getGold(), "alternate reward replaces gold, not adds to it");
    assertEquals(xpBefore, player.getCurrentXp(), "alternate reward replaces XP, not adds to it");
    assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(player, QUEST));
    assertTrue(QuestService.hasUnlockedZone(player, "test_zone"), "still unlocks the zone");
    assertEquals(1, saves.get());
  }

  @Test
  void whicheverMasterCompletesFirstLocksOutTheOther() {
    // The immediate master (the quest's own giverNpc) pays out first, through the normal path.
    Player player = readyPlayer();
    QuestService service = service(null);
    String turnIn = service.turnInReadyQuests(QUEST.getGiverNpc(), player);
    assertEquals("Completion", turnIn);
    assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(player, QUEST));

    // The alternate master can no longer claim it - the quest is no longer STATUS_ACTIVE.
    AtomicInteger perkGrants = new AtomicInteger();
    String alternate =
        service.completeWithAlternateReward(QUEST.getId(), player, perkGrants::incrementAndGet);
    assertNull(alternate);
    assertEquals(0, perkGrants.get());
  }

  @Test
  void theAlternateMasterAlsoLocksOutTheImmediateOne() {
    Player player = readyPlayer();
    QuestService service = service(null);

    String alternate =
        service.completeWithAlternateReward(QUEST.getId(), player, () -> {});
    assertEquals("Completion", alternate);

    int goldBefore = player.getGold();
    String turnIn = service.turnInReadyQuests(QUEST.getGiverNpc(), player);
    assertFalse(
        "Completion".equals(turnIn), "immediate master must not also pay out after the alternate claimed it");
    assertEquals(goldBefore, player.getGold());
  }
}
