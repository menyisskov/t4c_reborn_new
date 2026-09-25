package com.perso.T4C.mirror;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

/** T4C-0042: the Mirror of Echoes' trial ladder, scaling and rewards. */
class MirrorTrialsTest {

  @Test
  void trialsRunFromOneToTenThenRepeatTheLastOne() {
    Player player = new Player();
    assertEquals(1, MirrorTrials.nextTier(player));
    player.setQuestFlag(MirrorTrials.FLAG_TIER, 4);
    assertEquals(5, MirrorTrials.nextTier(player));
    player.setQuestFlag(MirrorTrials.FLAG_TIER, MirrorTrials.MAX_TIER);
    assertEquals(MirrorTrials.MAX_TIER, MirrorTrials.nextTier(player));
    assertTrue(MirrorTrials.hasBoundEcho(player));
    player.setQuestFlag(MirrorTrials.FLAG_TIER, 99);
    assertEquals(MirrorTrials.MAX_TIER, MirrorTrials.clearedTier(player));
  }

  @Test
  void eachTrialIsStrongerAndLongerThanTheLast() {
    for (int tier = 2; tier <= MirrorTrials.MAX_TIER; tier++) {
      assertTrue(MirrorTrials.tierMultiplier(tier) > MirrorTrials.tierMultiplier(tier - 1));
      assertTrue(MirrorTrials.blowsToWin(tier) > MirrorTrials.blowsToWin(tier - 1));
    }
    assertEquals(0, MirrorTrials.tierStrengthPercent(1));
    assertEquals(108, MirrorTrials.tierStrengthPercent(10));
  }

  @Test
  void hitsAreMeasuredAgainstTheMakersOwnLife() {
    int[] first = MirrorTrials.hitRange(10_000, 1);
    assertEquals(600, first[0]);
    assertEquals(1000, first[1]);
    int[] last = MirrorTrials.hitRange(10_000, 10);
    assertEquals(1248, last[0]);
    assertEquals(2080, last[1]);
    // A tiny character still takes at least one point per hit.
    assertEquals(1, MirrorTrials.hitRange(1, 1)[0]);
  }

  @Test
  void goldStaysUnderTheEndgameQuestCeiling() {
    assertEquals(250 * 20, MirrorTrials.goldReward(20, 1));
    assertEquals(1_000_000, MirrorTrials.goldReward(400, 10));
    assertEquals(MirrorTrials.MAX_GOLD_REWARD, MirrorTrials.goldReward(1000, 10));
  }

  @Test
  void xpIsAShareOfTheCurrentLevelAndStopsAtTheCap() {
    assertEquals(250, MirrorTrials.xpReward(1000, 10, 1));
    assertEquals(700, MirrorTrials.xpReward(1000, 10, 10));
    assertEquals(0, MirrorTrials.xpReward(1000, GameConstants.MAX_PLAYER_LEVEL, 5));
    assertEquals(Integer.MAX_VALUE, MirrorTrials.xpReward(Long.MAX_VALUE, 10, 10));
  }

  @Test
  void archetypeFollowsTheDominantStat() {
    assertEquals(MirrorTrials.Archetype.WARRIOR, MirrorTrials.archetypeOf(300, 100, 50, 50));
    assertEquals(MirrorTrials.Archetype.ARCHER, MirrorTrials.archetypeOf(100, 300, 50, 50));
    assertEquals(
        MirrorTrials.Archetype.INTELLIGENCE_MAGE, MirrorTrials.archetypeOf(50, 50, 600, 100));
    assertEquals(MirrorTrials.Archetype.WISDOM_MAGE, MirrorTrials.archetypeOf(50, 50, 100, 600));
    assertEquals(MirrorTrials.Archetype.HYBRID_MAGE, MirrorTrials.archetypeOf(50, 50, 500, 550));
  }

  @Test
  void lightSpellsAreNotFiftyTimesWeakerAgainstTheirOwnCaster() {
    assertEquals(400, MirrorTrials.spellRawDamage(400, 2));
    assertEquals(20_000, MirrorTrials.spellRawDamage(400, 5));
  }

  @Test
  void firstWinOfATrialAdvancesTheLadderAndPays() {
    Player player = new Player();
    player.setLevel(100);
    player.setXpToNextLevel(10_000L);
    int goldBefore = player.getGold();

    MirrorTrials.Reward reward =
        MirrorTrials.recordVictory(player, 1, XpCurve.loadDefault());

    assertTrue(reward.firstClear());
    assertFalse(reward.boundEcho());
    assertEquals(1, MirrorTrials.clearedTier(player));
    assertEquals(MirrorTrials.goldReward(100, 1), player.getGold() - goldBefore);
    assertEquals(2_500, reward.xp());
    assertEquals(2_500, player.getCurrentXp(), "the shown XP is exactly what is credited");
    assertEquals(1, player.getQuestFlag(MirrorTrials.FLAG_VICTORIES));
  }

  @Test
  void xpIsAShareOfWhatTheLevelStillNeedsNotItsWholeThreshold() {
    Player player = new Player();
    player.setLevel(100);
    player.setXpToNextLevel(10_000L);
    player.setCurrentXp(9_000L);

    MirrorTrials.Reward reward = MirrorTrials.recordVictory(player, 1, XpCurve.loadDefault());

    assertEquals(250, reward.xp());
    assertEquals(9_250, player.getCurrentXp());
  }

  @Test
  void rematchesPayOnlyASmallPurse() {
    Player player = new Player();
    player.setLevel(100);
    player.setQuestFlag(MirrorTrials.FLAG_TIER, 3);
    int goldBefore = player.getGold();

    MirrorTrials.Reward reward = MirrorTrials.recordVictory(player, 2, XpCurve.loadDefault());

    assertFalse(reward.firstClear());
    assertEquals(0, reward.xp());
    assertEquals(3, MirrorTrials.clearedTier(player));
    assertEquals(MirrorTrials.rematchGold(100), player.getGold() - goldBefore);
  }

  @Test
  void winningTheTenthTrialBindsTheEcho() {
    Player player = new Player();
    player.setLevel(GameConstants.MAX_PLAYER_LEVEL);
    player.setQuestFlag(MirrorTrials.FLAG_TIER, 9);

    MirrorTrials.Reward reward = MirrorTrials.recordVictory(player, 10, XpCurve.loadDefault());

    assertTrue(reward.boundEcho());
    assertEquals(0, reward.xp(), "no XP at the level cap");
    assertTrue(MirrorTrials.hasBoundEcho(player));
  }

  @Test
  void theLoginWhisperIsShownOnlyOnce() {
    Player player = new Player();
    player.setName("Lynania");
    String whisper = MirrorTrials.takeLoginWhisper(player);
    assertNotNull(whisper);
    assertTrue(whisper.contains("Lynania"), whisper);
    assertNull(MirrorTrials.takeLoginWhisper(player));
  }

  @Test
  void everyEchoLineResolvesWithThePlayersOwnDetails() {
    Player player = new Player();
    player.setName("Lynania");
    player.setLevel(321);
    player.setRebirthCount(7);
    player.setGold(1_234_567);
    for (int tier = 1; tier <= MirrorTrials.MAX_TIER; tier++) {
      String key = MirrorTrials.introKey(tier);
      assertTrue(I18n.has(key), key);
      String line = MirrorTrials.line(key, player, tier);
      assertFalse(line.contains("%"), line);
    }
    assertTrue(MirrorTrials.line(MirrorTrials.introKey(2), player, 2).contains("321"));
    assertTrue(MirrorTrials.line(MirrorTrials.introKey(5), player, 5).contains("7"));
    assertTrue(MirrorTrials.line(MirrorTrials.introKey(6), player, 6).contains("1,234,567"));
    for (MirrorTrials.Archetype archetype : MirrorTrials.Archetype.values()) {
      assertTrue(I18n.has(MirrorTrials.tauntKey(archetype)), archetype.name());
    }
    for (String key :
        new String[] {
          "mirror.echo.harder",
          "mirror.echo.bleeding",
          "mirror.echo.won",
          "mirror.echo.fled",
          "mirror.echo.bored",
          "mirror.echo.defeated",
          "mirror.echo.defeated.final",
          "mirror.player_fell"
        }) {
      assertTrue(I18n.has(key), key);
      assertFalse(MirrorTrials.line(key, player, 3).contains("%"), key);
    }
  }

  @Test
  void theBoundEchoIsACompanionDressedLikeItsMaker() {
    Player player = new Player();
    player.setName("Lynania");
    var def = MirrorTrials.echoCompanion(player);
    assertEquals(MirrorTrials.ECHO_COMPANION_ID, def.getId());
    assertEquals("Echo of Lynania", def.getDisplayName());
    long visibleParts =
        player.getAnimations().getPartMap().values().stream()
            .filter(sprite -> sprite != null && !sprite.isBlank())
            .count();
    assertEquals(visibleParts, def.getParts().size(), "every visible part is copied");
    assertFalse(def.getSpells().isEmpty());
  }
}
