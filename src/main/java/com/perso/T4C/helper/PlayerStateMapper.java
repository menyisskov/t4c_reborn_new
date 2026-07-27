package com.perso.T4C.helper;

import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Class representing PlayerStateMapper.
 */

public final class PlayerStateMapper {
    private PlayerStateMapper() {
    }

    public static PlayerStateDto fromPlayer(Player player) {
        return fromPlayer(player, 7f);
    }

    public static PlayerStateDto fromPlayer(Player player, float dayNightHour) {
        PlayerStateDto state = new PlayerStateDto();
        state.dayNightHour = dayNightHour;
        state.x = player.getCoordinates().getX() / com.perso.T4C.config.GameConstants.GRID_W;
        state.y = player.getCoordinates().getY() / com.perso.T4C.config.GameConstants.GRID_H;
        state.z = player.getCoordinates().getZ();
        state.strength = player.getStrength();
        state.dexterity = player.getDexterity();
        state.endurance = player.getEndurance();
        state.intelligence = player.getIntelligence();
        state.wisdom = player.getWisdom();
        state.armorClass = player.getArmorClass();
        state.gold = player.getGold();
        state.karma = player.getKarma();
        state.maxHp = player.getBaseMaxHp();
        state.currentHp = player.getCurrentHp();
        state.maxMana = player.getMaxMana();
        state.mana = player.getMana();
        state.level = player.getLevel();
        state.currentXp = player.getCurrentXp();
        state.xpToNextLevel = player.getXpToNextLevel();
        state.statPoints = player.getStatPoints();
        state.skillPoints = player.getSkillPoints();
        state.rebirthCount = player.getRebirthCount();
        state.spells = player.getSpells();
        state.quickSlots = player.getQuickSlots();
        state.activeBuffs = toActiveBuffStates(player);
        state.inventory = player.getInventory();
        state.equipment = toEquipmentMap(player);
        state.skills = player.getSkills() != null ? new HashMap<>(player.getSkills()) : new HashMap<>();
        state.itemCharges = new HashMap<>(player.getItemCharges());
        state.questFlags = new HashMap<>(player.getQuestFlags());
        state.respawnPointDefined = player.isRespawnPointDefined();
        state.respawnWorldX = player.getRespawnWorldX();
        state.respawnWorldY = player.getRespawnWorldY();
        state.respawnWorldZ = player.getRespawnWorldZ();
        state.hiddenRemainingMillis = player.getHiddenRemainingMillis();
        return state;
    }

    public static void applyToPlayer(PlayerStateDto state, Player player) {
        if (state == null || player == null) {
            return;
        }
        player.setStrength(state.strength);
        player.setDexterity(state.dexterity);
        player.setEndurance(state.endurance);
        player.setIntelligence(state.intelligence);
        player.setWisdom(state.wisdom);
        player.setArmorClass(state.armorClass);
        player.setGold(state.gold);
        player.setKarma(state.karma);
        player.setMaxHp(state.maxHp);
        player.setCurrentHp(state.currentHp);
        player.setMaxMana(state.maxMana);
        player.setMana(state.mana);
        player.setLevel(state.level);
        player.setCurrentXp(state.currentXp);
        player.setXpToNextLevel(state.xpToNextLevel);
        player.setStatPoints(state.statPoints);
        player.setSkillPoints(state.skillPoints);
        player.setRebirthCount(state.rebirthCount);
        player.setSpells(state.spells);
        player.setQuickSlots(state.quickSlots);
        applyActiveBuffs(state, player);
        player.setInventory(state.inventory);
        player.setEquippedItems(toEquippedItems(state));
        if (state.skills != null) {
            player.setSkills(new HashMap<>(state.skills));
        }
        player.setItemCharges(state.itemCharges);
        player.setQuestFlags(state.questFlags);
        if (state.respawnPointDefined) {
            player.setRespawnPoint(state.respawnWorldX, state.respawnWorldY, state.respawnWorldZ);
        }
        if (state.hiddenRemainingMillis == Long.MAX_VALUE) player.setHidden(true);
        else if (state.hiddenRemainingMillis > 0L) player.setHiddenFor(state.hiddenRemainingMillis);
    }

    private static List<PlayerStateDto.ActiveBuffState> toActiveBuffStates(Player player) {
        List<PlayerStateDto.ActiveBuffState> states = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Player.ActiveBuff buff : player.getActiveBuffs()) {
            if (buff == null) {
                continue;
            }
            PlayerStateDto.ActiveBuffState state = new PlayerStateDto.ActiveBuffState();
            state.spellName = buff.getSpellName();
            state.description = buff.getDescription();
            state.iconId = buff.getIconId();
            state.unlimited = buff.getExpiresAtMillis() == Long.MAX_VALUE;
            state.remainingSeconds = state.unlimited
                    ? Long.MAX_VALUE
                    : Math.max(0L, (buff.getExpiresAtMillis() - now + 999L) / 1000L);
            state.totalDurationSeconds = state.unlimited || buff.getDurationMillis() == Long.MAX_VALUE
                    ? Long.MAX_VALUE
                    : Math.max(1L, buff.getDurationMillis() / 1000L);
            state.effects = toPersistedEffects(buff.getEffects());
            if (state.unlimited || state.remainingSeconds > 0L) {
                states.add(state);
            }
        }
        return states;
    }

    private static List<PlayerStateDto.ActiveBuffState.PersistedEffect> toPersistedEffects(List<SpellData.SpellEffect> effects) {
        if (effects == null || effects.isEmpty()) return Collections.emptyList();
        List<PlayerStateDto.ActiveBuffState.PersistedEffect> out = new ArrayList<>(effects.size());
        for (SpellData.SpellEffect e : effects) {
            if (e != null) out.add(new PlayerStateDto.ActiveBuffState.PersistedEffect(e.getType(), e.getAttribute(), e.getAmount()));
        }
        return out;
    }

    private static List<SpellData.SpellEffect> fromPersistedEffects(List<PlayerStateDto.ActiveBuffState.PersistedEffect> persisted) {
        if (persisted == null || persisted.isEmpty()) return Collections.emptyList();
        List<SpellData.SpellEffect> out = new ArrayList<>(persisted.size());
        for (PlayerStateDto.ActiveBuffState.PersistedEffect e : persisted) {
            if (e != null) out.add(new SpellData.SpellEffect(e.type, e.attribute, e.amount, ""));
        }
        return out;
    }

    private static void applyActiveBuffs(PlayerStateDto state, Player player) {
        if (state.activeBuffs == null || state.activeBuffs.isEmpty()) {
            return;
        }
        for (PlayerStateDto.ActiveBuffState buff : state.activeBuffs) {
            if (buff == null || buff.spellName == null || buff.spellName.isEmpty()) {
                continue;
            }
            if (!buff.unlimited && buff.remainingSeconds <= 0L) {
                continue;
            }
            long restoredDuration = buff.totalDurationSeconds > 0L ? buff.totalDurationSeconds : buff.remainingSeconds;
            Integer durationSeconds = buff.unlimited || restoredDuration > Integer.MAX_VALUE
                    ? null
                    : (int) restoredDuration;
            List<SpellData.SpellEffect> effects = fromPersistedEffects(buff.effects);
            player.applyBuff(buff.spellName, buff.description, buff.iconId, durationSeconds, buff.unlimited, effects);
            if (!buff.unlimited && buff.remainingSeconds < restoredDuration) {
                player.adjustBuffRemaining(buff.spellName, buff.remainingSeconds);
            }
        }
    }

    private static Map<String, String> toEquipmentMap(Player player) {
        Map<String, String> equipment = new HashMap<>();
        for (Map.Entry<BodyPart, String> entry : player.getEquippedItems().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                equipment.put(entry.getKey().name(), entry.getValue());
            }
        }
        return equipment;
    }

    private static Map<BodyPart, String> toEquippedItems(PlayerStateDto state) {
        Map<BodyPart, String> equippedItems = new HashMap<>();
        if (state.equipment == null) {
            return equippedItems;
        }
        for (Map.Entry<String, String> entry : state.equipment.entrySet()) {
            try {
                BodyPart part = BodyPart.valueOf(entry.getKey());
                equippedItems.put(part, entry.getValue());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return equippedItems;
    }
}
