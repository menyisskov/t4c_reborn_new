package com.perso.T4C.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PlayerProgressionTest {
  @Test
  void largeCombatAwardsKeepPersonalBonusesAndSavedProgressWithoutChangingQuestAwards() {
    Player player = new Player();
    player.setLevel(226);
    XpCurve.loadDefault().applyToPlayer(player);
    player.applyBuff(
        "test experience",
        "",
        "",
        60,
        false,
        java.util.List.of(
            new com.perso.T4C.spell.SpellData.SpellEffect("ATTRIBUTE", "exp", "25", "")));
    player.addXp(4200000000L, null);
    assertEquals(5250000000L, player.getCurrentXp());
    player.addXpExact(100, null);
    assertEquals(5250000100L, player.getCurrentXp());
    Player restored = new Player();
    com.perso.T4C.helper.PlayerStateMapper.applyToPlayer(
        com.perso.T4C.helper.PlayerStateMapper.fromPlayer(player), restored);
    assertEquals(player.getCurrentXp(), restored.getCurrentXp());
  }

  @Test
  void reviewedCurveHasConsistentLongTotalsAndCrossesAHighLevelThresholdOnce() {
    var curve = XpCurve.loadDefault();
    for (int level = 1; level < 400; level++) {
      assertTrue(curve.getXpToNextLevel(level) > 0);
      assertEquals(
          curve.getTotalXp(level + 1) - curve.getTotalXp(level), curve.getXpToNextLevel(level));
    }
    assertEquals(543_954_839_026L, curve.getTotalXp(215));
    assertEquals(1_222_647_191_025L, curve.getTotalXp(234));
    assertEquals(2_002_088_069_939L, curve.getTotalXp(320));
    var player = new Player();
    player.setLevel(200);
    curve.applyToPlayer(player);
    player.setCurrentXp(player.getXpToNextLevel() - 1);
    player.addXpExact(1, curve);
    assertEquals(201, player.getLevel());
    assertEquals(0, player.getCurrentXp());
    assertEquals(5, player.getStatPoints());
    assertEquals(15, player.getSkillPoints());
  }

  @Test
  void originalT4cResourceBasesFollowAttributeThresholds() {
    assertEquals(6, PlayerProgression.hitPointGainBase(19));
    assertEquals(7, PlayerProgression.hitPointGainBase(20));
    assertEquals(11, PlayerProgression.hitPointGainBase(100));
    assertEquals(3, PlayerProgression.manaGainBase(29, 59));
    assertEquals(4, PlayerProgression.manaGainBase(30, 59));
    assertEquals(5, PlayerProgression.manaGainBase(30, 60));
  }

  @Test
  void levelUpNotifiesTheEffectCallbackOncePerGainedLevel() {
    Player player = new Player();
    player.setLevel(1);
    player.setXpToNextLevel(100);
    player.setMaxHp(100);
    player.setCurrentHp(1);
    player.setMaxMana(100);
    player.setMana(1);
    java.util.List<Integer> levels = new java.util.ArrayList<>();
    player.setLevelUpCallback(
        level -> {
          levels.add(level);
          assertEquals(
              player.getMaxHp(), player.getCurrentHp(), "refill before notifying or saving");
          assertEquals(player.getMaxMana(), player.getMana(), "refill at every gained level");
        });
    new PlayerProgression(new Random(7)).addXp(player, 100_000, XpCurve.load("java"), false);
    assertTrue(levels.size() >= 2, "each level gained must fire the callback");
    assertEquals(player.getLevel(), levels.get(levels.size() - 1));
    assertEquals(java.util.List.of(2, 3), levels.subList(0, 2));
  }

  @Test
  void levelUpKeepsOriginalMaximumGainsAndFullyRestoresWoundedResources() {
    Player player = new Player();
    player.setLevel(1);
    player.setXpToNextLevel(100);
    player.setEndurance(100);
    player.setIntelligence(90);
    player.setWisdom(120);
    player.setMaxHp(100);
    player.setCurrentHp(60);
    player.setMaxMana(100);
    player.setMana(40);
    // 100 raw XP at the standard server rate triggers exactly one level-up so this
    // test isolates a single roll of the original per-level gain formula.
    new PlayerProgression(new Random(7)).addXp(player, 100, XpCurve.load("java"), false);
    int hpGain = player.getMaxHp() - 100;
    int manaGain = player.getMaxMana() - 100;
    assertTrue(hpGain >= 11 && hpGain <= 13);
    assertTrue(manaGain >= 8 && manaGain <= 10);
    assertEquals(player.getMaxHp(), player.getCurrentHp());
    assertEquals(player.getMaxMana(), player.getMana());
    assertEquals(2, player.getLevel());
  }

  @Test
  void levelUpKeepsAFullCharacterFull() {
    Player player = new Player();
    player.setLevel(1);
    player.setXpToNextLevel(100);
    player.setEndurance(100);
    player.setIntelligence(90);
    player.setWisdom(120);
    player.setMaxHp(100);
    player.setCurrentHp(100);
    player.setMaxMana(100);
    player.setMana(100);
    new PlayerProgression(new Random(7)).addXp(player, 100, XpCurve.load("java"), false);
    assertEquals(player.getMaxHp(), player.getCurrentHp());
    assertEquals(player.getMaxMana(), player.getMana());
    assertTrue(player.getMaxHp() > 100);
    assertTrue(player.getMaxMana() > 100);
  }

  @Test
  void earningXpWithoutGainingALevelDoesNotRestoreResources() {
    Player player = new Player();
    player.setLevel(1);
    player.setXpToNextLevel(100_000);
    player.setMaxHp(100);
    player.setCurrentHp(60);
    player.setMaxMana(100);
    player.setMana(40);
    new PlayerProgression(new Random(7)).addXp(player, 1, XpCurve.load("java"), false);
    assertEquals(1, player.getLevel());
    assertEquals(60, player.getCurrentHp());
    assertEquals(40, player.getMana());
  }

  @Test
  void levelingStopsAtTheLevelCap() {
    int cap = com.perso.T4C.config.GameConstants.MAX_PLAYER_LEVEL;
    XpCurve curve = XpCurve.loadDefault();
    assertEquals(0, curve.getXpToNextLevel(cap));
    assertEquals(0, curve.getXpToNextLevel(cap + 1));
    assertTrue(curve.getXpToNextLevel(cap - 1) > 0);

    Player player = new Player();
    player.setLevel(cap - 1);
    player.setXpToNextLevel(curve.getXpToNextLevel(cap - 1));
    player.setCurrentXp(player.getXpToNextLevel() - 1);
    new PlayerProgression(new Random(7)).addXp(player, Integer.MAX_VALUE, curve, false);
    assertEquals(cap, player.getLevel());
    assertEquals(player.getMaxHp(), player.getCurrentHp());
    assertEquals(player.getMaxMana(), player.getMana());
    assertEquals(0, player.getXpToNextLevel());

    player.setCurrentHp(1);
    player.setMana(1);
    new PlayerProgression(new Random(7)).addXp(player, Integer.MAX_VALUE, curve, false);
    assertEquals(cap, player.getLevel());
    assertEquals(1, player.getCurrentHp(), "XP at the cap must not trigger a level-up refill");
    assertEquals(1, player.getMana());
  }
}
