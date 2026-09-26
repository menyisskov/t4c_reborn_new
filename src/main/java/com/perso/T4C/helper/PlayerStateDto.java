package com.perso.T4C.helper;

import com.perso.T4C.model.QuickSlotEntry;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PlayerStateDto {
  public String name;
  public String gender;
  public float x;
  public float y;
  public int z;
  public int strength;
  public int dexterity;
  public int endurance;
  public int intelligence;
  public int wisdom;
  public int gold;
  public int karma;
  public int maxHp;
  public int currentHp;
  public int maxMana;
  public int mana;
  public int level;
  public long currentXp;
  public long xpToNextLevel;
  public int statPoints;
  public int skillPoints;
  public int rebirthCount;
  public List<String> spells;
  public List<QuickSlotEntry> quickSlots;
  public List<com.perso.T4C.config.MacroBinding> macros;
  public List<ActiveBuffState> activeBuffs;
  public List<String> inventory;
  public Map<String, String> equipment;
  public Map<String, Integer> skills;
  public Map<String, Integer> itemCharges;
  public List<Double> inventoryDurability;
  public Map<String, Double> equipmentDurability;
  public List<String> storage;
  public List<Double> storageDurability;
  public List<Integer> storageCharges;
  public int storageGold;
  public Map<String, Integer> questFlags;
  public boolean respawnPointDefined;
  public float respawnWorldX;
  public float respawnWorldY;
  public int respawnWorldZ;
  public long hiddenRemainingMillis;
  public float dayNightHour = 7f;
  public CompanionState companion;

  @NoArgsConstructor
  public static class CompanionState {
    public String speciesName;
    public boolean tamed;
    public int level;
    public int currentHp;
    public String mode;
  }

  @NoArgsConstructor
  public static class ActiveBuffState {
    public String spellName;
    public long remainingSeconds;
    public long totalDurationSeconds;
  }
}
