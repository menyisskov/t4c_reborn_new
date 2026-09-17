package com.perso.T4C.player;

import com.perso.T4C.helper.XpCurve;
import java.util.random.RandomGenerator;

public final class PlayerProgression {
  private final RandomGenerator random;

  PlayerProgression() {
    this(RandomGenerator.getDefault());
  }

  PlayerProgression(RandomGenerator random) {
    this.random = random;
  }

  void addXp(Player player, int amount, XpCurve xpCurve, boolean applyMultiplier) {
    if (player == null || amount <= 0) {
      return;
    }
    amount = Math.round(amount * com.perso.T4C.config.GameConstants.SERVER_XP_RATE);
    if (applyMultiplier) {
      amount = Math.round(amount * (1f + player.getBuffXpMultiplier()));
    }
    player.setCurrentXp(player.getCurrentXp() + amount);
    if (xpCurve == null) {
      return;
    }
    if (player.getLevel() <= 0) {
      player.setLevel(1);
    }
    while (player.getXpToNextLevel() > 0 && player.getCurrentXp() >= player.getXpToNextLevel()) {
      player.setCurrentXp(player.getCurrentXp() - player.getXpToNextLevel());
      player.setLevel(player.getLevel() + 1);
      int[] gains = applyOriginalT4cResourceGains(player);
      player.setStatPoints(player.getStatPoints() + 5);
      player.setSkillPoints(player.getSkillPoints() + 15);
      player.showLevelUpMessage(player.getLevel(), gains[0], gains[1]);
      player.notifyLevelUp(player.getLevel());
      int next = xpCurve.getXpToNextLevel(player.getLevel());
      if (next > 0) {
        player.setXpToNextLevel(next);
      } else {
        break;
      }
    }
  }

  private int[] applyOriginalT4cResourceGains(Player player) {
    int hpGain = hitPointGainBase(player.getEndurance()) + random.nextInt(3);
    int manaGain = manaGainBase(player.getIntelligence(), player.getWisdom()) + random.nextInt(3);
    player.setMaxHp(player.getMaxHp() + hpGain);
    player.setCurrentHp(player.getCurrentHp() + hpGain);
    player.setMaxMana(player.getMaxMana() + manaGain);
    player.setMana(player.getMana() + manaGain);
    return new int[] {hpGain, manaGain};
  }

  public static int hitPointGainBase(int endurance) {
    return 6 + Math.max(0, endurance) / 20;
  }

  public static int manaGainBase(int intelligence, int wisdom) {
    return 3 + Math.max(0, intelligence) / 30 + Math.max(0, wisdom) / 60;
  }
}
