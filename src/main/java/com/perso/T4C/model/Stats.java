package com.perso.T4C.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stats {
  protected int strength;
  protected int dexterity;
  protected int endurance;
  protected int intelligence;
  protected int wisdom;
  protected int gold;
  protected int karma;
  protected int maxHp;
  protected int currentHp;
  protected int maxMana;
  protected int mana;
  protected int level;
  protected int currentXp;
  protected int xpToNextLevel;
  protected int statPoints;
  protected int skillPoints;
  protected List<String> spells;
  protected List<QuickSlotEntry> quickSlots;
  protected Map<String, Integer> skills = new HashMap<>();

  public int getSkillLevel(String skillId) {
    if (skillId == null || skills == null) return 0;
    return skills.getOrDefault(skillId, 0);
  }

  public void setSkillLevel(String skillId, int level) {
    if (skillId == null) return;
    if (skills == null) skills = new HashMap<>();
    skills.put(skillId, Math.max(0, level));
  }
}
