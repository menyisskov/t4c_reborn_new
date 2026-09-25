package com.perso.T4C.mirror;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** T4C-0048: tier selection, timing, and best-time bookkeeping for the Hourglass Trials. */
class HourglassTrialsTest {
  @AfterEach
  void restoreRealClock() {
    HourglassTrials.useClock(null);
  }
  @Test
  void tierRisesOneStepEveryTenRebirthsAndCapsAtFive() throws Exception {
    Player player = new Player();
    assertEquals(1, HourglassTrials.tierFor(player));

    player.setRebirthCount(9);
    assertEquals(1, HourglassTrials.tierFor(player));

    player.setRebirthCount(10);
    assertEquals(2, HourglassTrials.tierFor(player));

    player.setRebirthCount(49);
    assertEquals(5, HourglassTrials.tierFor(player));

    player.setRebirthCount(200);
    assertEquals(HourglassTrials.MAX_TIER, HourglassTrials.tierFor(player));
  }

  @Test
  void monsterNameRoundTripsThroughTierOfMonsterName() {
    for (int tier = HourglassTrials.MIN_TIER; tier <= HourglassTrials.MAX_TIER; tier++) {
      String name = HourglassTrials.monsterNameForTier(tier);
      assertEquals(tier, HourglassTrials.tierOfMonsterName(name));
    }
    assertEquals(0, HourglassTrials.tierOfMonsterName("Centaur Warrior"));
  }

  @Test
  void finishWithNoActiveTrialReturnsNull() throws Exception {
    Player player = new Player();
    assertNull(HourglassTrials.finish(player));
  }

  @Test
  void firstClearIsAlwaysANewBestAndPersistsItAsMilliseconds() throws Exception {
    Player player = new Player();
    AtomicLong now = new AtomicLong(10_000L);
    HourglassTrials.useClock(now::get);

    HourglassTrials.start(player, 3);
    assertTrue(HourglassTrials.hasActiveTrial(player));
    now.set(10_000L + 7_500L);

    HourglassTrials.Result result = HourglassTrials.finish(player);

    assertTrue(result.newBest());
    assertEquals(3, result.tier());
    assertEquals(7_500L, result.elapsedMillis());
    assertFalse(HourglassTrials.hasActiveTrial(player), "trial clears once finished");
    assertEquals(
        7_500, player.getQuestFlag(HourglassTrials.bestTimeFlag(3)),
        "best time is persisted as a plain quest flag");
  }

  @Test
  void onlyAFasterRunReplacesTheBest() throws Exception {
    Player player = new Player();
    player.setQuestFlag(HourglassTrials.bestTimeFlag(1), 5_000);
    AtomicLong now = new AtomicLong(0L);
    HourglassTrials.useClock(now::get);

    HourglassTrials.start(player, 1);
    now.set(6_000L); // slower than the existing 5,000ms best

    HourglassTrials.Result result = HourglassTrials.finish(player);

    assertFalse(result.newBest());
    assertEquals(5_000, HourglassTrials.bestTimeMillis(player, 1));
  }

  @Test
  void abandonClearsTheTimerWithoutRecordingAnything() throws Exception {
    Player player = new Player();
    HourglassTrials.start(player, 2);
    HourglassTrials.abandon(player);
    assertFalse(HourglassTrials.hasActiveTrial(player));
    assertNull(HourglassTrials.finish(player));
  }

  @Test
  void formatMillisReadsAsMinutesSecondsTenths() {
    assertEquals("0:07.5", HourglassTrials.formatMillis(7_500));
    assertEquals("1:03.0", HourglassTrials.formatMillis(63_000));
  }
}
