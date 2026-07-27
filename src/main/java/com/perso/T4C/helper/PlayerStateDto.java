package com.perso.T4C.helper;

import com.perso.T4C.model.QuickSlotEntry;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
/**
 * Class representing PlayerStateDto.
 */

@NoArgsConstructor
public class PlayerStateDto {
    /** Persisted tile coordinates. */
    public float x;
    public float y;
    public int z;
    public int strength;
    public int dexterity;
    public int endurance;
    public int intelligence;
    public int wisdom;
    public int armorClass;
    public int gold;
    public int karma;
    public int maxHp;
    public int currentHp;
    public int maxMana;
    public int mana;
    public int level;
    public int currentXp;
    public int xpToNextLevel;
    public int statPoints;
    public int skillPoints;
    public int rebirthCount;
    public List<String> spells;
    public List<QuickSlotEntry> quickSlots;
    public List<ActiveBuffState> activeBuffs;
    public List<String> inventory;
    public Map<String, String> equipment;
    public Map<String, Integer> skills;
    public Map<String, Integer> itemCharges;
    public Map<String, Integer> questFlags;
    public boolean respawnPointDefined;
    public float respawnWorldX;
    public float respawnWorldY;
    public int respawnWorldZ;
    public long hiddenRemainingMillis;
    public float dayNightHour = 7f;

    @NoArgsConstructor
    public static class ActiveBuffState {
        public String spellName;
        public String description;
        public String iconId;
        public long remainingSeconds;
        public long totalDurationSeconds;
        public boolean unlimited;
        public List<PersistedEffect> effects;

        @NoArgsConstructor
        @AllArgsConstructor
        public static class PersistedEffect {
            public String type;
            public String attribute;
            public String amount;
        }
    }
}
