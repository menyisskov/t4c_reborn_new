package com.perso.T4C.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PlayerProgressionTest {
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
    java.util.List<Integer> levels = new java.util.ArrayList<>();
    player.setLevelUpCallback(levels::add);
    new PlayerProgression(new Random(7)).addXp(player, 100_000, XpCurve.load("java"), false);
    assertTrue(levels.size() >= 2, "each level gained must fire the callback");
    assertEquals(player.getLevel(), levels.get(levels.size() - 1));
    assertEquals(java.util.List.of(2, 3), levels.subList(0, 2));
  }

  @Test
  void levelUpRaisesMaximumAndCurrentResourcesWithinOriginalRollRanges() {
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
    // 20 raw XP * SERVER_XP_RATE (5x) = exactly 100, triggering exactly one level-up so this
    // test isolates a single roll of the original per-level gain formula.
    new PlayerProgression(new Random(7)).addXp(player, 20, XpCurve.load("java"), false);
    int hpGain = player.getMaxHp() - 100;
    int manaGain = player.getMaxMana() - 100;
    assertTrue(hpGain >= 11 && hpGain <= 13);
    assertTrue(manaGain >= 8 && manaGain <= 10);
    assertEquals(60 + hpGain, player.getCurrentHp());
    assertEquals(40 + manaGain, player.getMana());
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
}
