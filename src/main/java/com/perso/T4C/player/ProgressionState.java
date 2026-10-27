package com.perso.T4C.player;

import com.perso.T4C.helper.XpCurve;

/** The mutable progression boundary shared by desktop play and server-authored reward drafts. */
public interface ProgressionState extends XpCurve.Progress {
  long getCurrentXp();
  int getEndurance();
  int getIntelligence();
  int getWisdom();
  int getMaxHp();
  void setMaxHp(int value);
  void setCurrentHp(int value);
  int getMaxMana();
  void setMaxMana(int value);
  void setMana(int value);
  int getStatPoints();
  void setStatPoints(int value);
  int getSkillPoints();
  void setSkillPoints(int value);
  float getBuffXpMultiplier();
  void showLevelUpMessage(int level, int hpGain, int manaGain);
  void notifyLevelUp(int level);
}
