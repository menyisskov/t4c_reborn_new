package com.perso.T4C.death;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/** Character::Death / GAME_RULES::DeathPenalties port with separate PvE/PvP rates. */
public final class DeathPenaltyService {
    public record Rates(int backpackPercent, int equippedPercent, int xpPercent, int goldPercent, int goldDropPercent) {
        public Rates {
            backpackPercent = clamp(backpackPercent);
            equippedPercent = clamp(equippedPercent);
            xpPercent = clamp(xpPercent);
            goldPercent = clamp(goldPercent);
            goldDropPercent = clamp(goldDropPercent);
        }

        private static int clamp(int value) { return Math.max(0, Math.min(100, value)); }
    }

    public record Config(Rates pve, Rates pvp) {
        public static Config originalDefaults() {
            return new Config(new Rates(5, 0, 5, 0, 0), new Rates(1, 0, 1, 0, 0));
        }
    }

    public record Result(boolean pvp, int xpLost, int goldLost, int goldDropped,
                         List<String> droppedItems, List<Integer> droppedItemCharges,
                         List<String> destroyedItems) {
    }

    private final Config config;

    public DeathPenaltyService(Config config) {
        this.config = config == null ? Config.originalDefaults() : config;
    }

    public Result apply(Player player, boolean pvp, XpCurve curve, RandomGenerator random) {
        if (player == null || random == null) throw new IllegalArgumentException("Player and random generator are required");
        Rates rates = pvp ? config.pvp() : config.pve();
        List<String> dropped = new ArrayList<>();
        List<String> destroyed = new ArrayList<>();
        List<Integer> droppedCharges = new ArrayList<>();

        for (int i = player.getInventory().size() - 1; i >= 0; i--) {
            if (rollPercent(random, rates.backpackPercent())) {
                String item = player.getInventory().get(i);
                int charges = InventoryService.chargesForNextInstance(player, item);
                InventoryService.remove(player, i, item);
                dropped.add(item);
                droppedCharges.add(charges);
            }
        }
        List<Map.Entry<BodyPart, String>> equipment = new ArrayList<>(player.getEquippedItems().entrySet());
        for (Map.Entry<BodyPart, String> entry : equipment) {
            ItemDefinition definition = ItemRegistry.findByKey(entry.getValue());
            if (definition != null && definition.getSecondaryBodyPart() == entry.getKey()
                    && entry.getValue().equals(player.getEquippedItems().get(definition.getBodyPart()))) {
                continue;
            }
            if (rollPercent(random, rates.equippedPercent()) && player.getEquippedItems().remove(entry.getKey(), entry.getValue())) {
                int charges = InventoryService.chargesForNextInstance(player, entry.getValue());
                if (definition != null && definition.getSecondaryBodyPart() != null) {
                    player.getEquippedItems().remove(definition.getSecondaryBodyPart(), entry.getValue());
                }
                InventoryService.removeExtractedCharges(player, entry.getValue(), charges);
                dropped.add(entry.getValue());
                droppedCharges.add(charges);
            }
        }

        // Java persists XP relative to the current level (PlayerProgression subtracts
        // each completed level), which is already the C++ "above level floor" value.
        int earnedThisLevel = Math.max(0, player.getCurrentXp());
        int xpLost = percentage(earnedThisLevel, rates.xpPercent());
        player.setCurrentXp(Math.max(0, player.getCurrentXp() - xpLost));

        int goldLost = percentage(player.getGold(), rates.goldPercent());
        int goldDropped = percentage(goldLost, rates.goldDropPercent());
        player.setGold(Math.max(0, player.getGold() - goldLost));
        return new Result(pvp, xpLost, goldLost, goldDropped,
                List.copyOf(dropped), List.copyOf(droppedCharges), List.copyOf(destroyed));
    }

    private static boolean rollPercent(RandomGenerator random, int percent) {
        return percent > 0 && random.nextInt(100) < percent;
    }

    private static int percentage(int value, int percent) {
        return (int) Math.min(Integer.MAX_VALUE, (long) Math.max(0, value) * percent / 100L);
    }
}
