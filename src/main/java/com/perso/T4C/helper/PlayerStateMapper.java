package com.perso.T4C.helper;

import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellEffectManager;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.combat.SeraphAuraService;

import java.util.ArrayList;
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
        state.name = player.getName();
        state.gender = player.getGender();
        state.dayNightHour = dayNightHour;
        state.x = player.getCoordinates().getX() / com.perso.T4C.config.GameConstants.GRID_W;
        state.y = player.getCoordinates().getY() / com.perso.T4C.config.GameConstants.GRID_H;
        state.z = player.getCoordinates().getZ();
        state.strength = player.getStrength();
        state.dexterity = player.getDexterity();
        state.endurance = player.getEndurance();
        state.intelligence = player.getIntelligence();
        state.wisdom = player.getWisdom();
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
        com.perso.T4C.item.ItemDurabilityService.synchronize(player);
        state.inventoryDurability = new ArrayList<>(player.getInventoryDurability());
        state.equipmentDurability = toEquipmentDurabilityMap(player);
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
        player.setName(state.name);
        player.setGender(state.gender);
        player.setStrength(state.strength);
        player.setDexterity(state.dexterity);
        player.setEndurance(state.endurance);
        player.setIntelligence(state.intelligence);
        player.setWisdom(state.wisdom);
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
        player.setInventoryDurability(state.inventoryDurability);
        player.setEquippedDurability(toEquippedDurability(state));
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
            SpellData spell = SpellRegistry.findByName(buff.getSpellName());
            state.spellName = spell != null ? spell.getKey() : canonicalSpecialBuffKey(buff.getSpellName());
            boolean unlimited = buff.getExpiresAtMillis() == Long.MAX_VALUE;
            state.remainingSeconds = unlimited
                    ? Long.MAX_VALUE
                    : Math.max(0L, (buff.getExpiresAtMillis() - now + 999L) / 1000L);
            state.totalDurationSeconds = unlimited || buff.getDurationMillis() == Long.MAX_VALUE
                    ? Long.MAX_VALUE
                    : Math.max(1L, buff.getDurationMillis() / 1000L);
            if (unlimited || state.remainingSeconds > 0L) {
                states.add(state);
            }
        }
        return states;
    }

    private static String canonicalSpecialBuffKey(String name) {
        return SeraphAuraService.AURA_NAME.equals(name) || "Remort aura".equals(name)
                ? SeraphAuraService.AURA_NAME : name;
    }

    private static void applyActiveBuffs(PlayerStateDto state, Player player) {
        if (state.activeBuffs == null || state.activeBuffs.isEmpty()) {
            return;
        }
        for (PlayerStateDto.ActiveBuffState buff : state.activeBuffs) {
            if (buff == null || buff.spellName == null || buff.spellName.isEmpty()) {
                continue;
            }
            boolean unlimited = buff.remainingSeconds == Long.MAX_VALUE || buff.totalDurationSeconds == Long.MAX_VALUE;
            if (!unlimited && buff.remainingSeconds <= 0L) {
                continue;
            }
            long restoredDuration = buff.totalDurationSeconds > 0L ? buff.totalDurationSeconds : buff.remainingSeconds;
            Integer durationSeconds = unlimited || restoredDuration > Integer.MAX_VALUE
                    ? null
                    : (int) restoredDuration;
            boolean seraphAura = SeraphAuraService.AURA_NAME.equals(buff.spellName)
                    || "Remort aura".equals(buff.spellName);
            SpellData spell = seraphAura ? null : SpellRegistry.findByName(buff.spellName);
            String description;
            String iconId;
            String runtimeSpellName = buff.spellName;
            List<SpellData.SpellEffect> effects;
            if (spell != null) {
                runtimeSpellName = spell.getName();
                description = spell.getDescription();
                iconId = spell.getIconId();
                effects = new SpellEffectManager().resolvePlayerBuffEffects(spell, player);
                if (effects.isEmpty() && spell.getBuff() != null && spell.getBuff().getEffects() != null) {
                    effects = spell.getBuff().getEffects();
                }
            } else if (seraphAura) {
                runtimeSpellName = SeraphAuraService.AURA_NAME;
                description = SeraphAuraService.AURA_DESCRIPTION;
                iconId = SeraphAuraService.AURA_ICON;
                effects = List.of();
            } else {
                continue;
            }
            player.applyBuff(runtimeSpellName, description, iconId, durationSeconds, unlimited, effects);
            if (!unlimited && buff.remainingSeconds < restoredDuration) {
                player.adjustBuffRemaining(runtimeSpellName, buff.remainingSeconds);
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

    private static Map<String, Double> toEquipmentDurabilityMap(Player player) {
        Map<String, Double> result = new HashMap<>();
        player.getEquippedDurability().forEach((slot, value) -> result.put(slot.name(), value));
        return result;
    }

    private static Map<BodyPart, Double> toEquippedDurability(PlayerStateDto state) {
        Map<BodyPart, Double> result = new HashMap<>();
        if (state.equipmentDurability != null) state.equipmentDurability.forEach((key, value) -> {
            try { result.put(BodyPart.valueOf(key), value); } catch (IllegalArgumentException ignored) {}
        });
        return result;
    }
}
