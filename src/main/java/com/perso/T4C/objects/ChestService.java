package com.perso.T4C.objects;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.loot.LootResult;
import com.perso.T4C.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

/** Runtime behaviour of GoN container objects. */
public final class ChestService {
    public enum Failure { NONE, NOT_A_CHEST, EMPTY, COOLDOWN }
    public record Result(boolean opened, Failure failure, List<String> itemKeys, int gold) { }

    private final Map<ObjectPos, Long> nextAvailableAt = new HashMap<>();

    public Result open(Player player, ObjectPos chest, GroundItemManager groundItems) {
        ItemDefinition definition = chest == null ? null : ItemRegistry.findByKey(chest.name());
        if (definition == null || definition.getStructure() != 3) return new Result(false, Failure.NOT_A_CHEST, null, 0);
        long now = System.currentTimeMillis();
        if (now < nextAvailableAt.getOrDefault(chest, 0L)) return new Result(false, Failure.COOLDOWN, null, 0);
        List<String> rewards = drawOneFromEachGroup(definition.getContainerLootGroups());
        if (rewards.isEmpty()) return new Result(false, Failure.EMPTY, List.of(), 0);
        int gold = Math.max(0, definition.getContainerGold());
        groundItems.spawnFromLoot(new LootResult(gold, rewards), chest.x() * GRID_W, chest.y() * GRID_H, player::addGold);
        long delaySeconds = Math.max(0L, definition.getLocalRespawn())
                + ThreadLocalRandom.current().nextLong(-600L, 601L);
        nextAvailableAt.put(chest, now + Math.max(0L, delaySeconds) * 1000L);
        return new Result(true, Failure.NONE, rewards, gold);
    }

    public String message(Result result) {
        String items = result.itemKeys().stream()
                .map(key -> I18n.key("item." + I18n.normalizedKey(key), key))
                .reduce((a, b) -> a + ", " + b).orElse("");
        if (result.gold() > 0) return I18n.message("message.chest_loot_gold",  items, result.gold());
        return I18n.message("message.chest_loot",  items);
    }

    private static List<String> drawOneFromEachGroup(List<ItemDefinition.ContainerLootGroup> groups) {
        if (groups == null || groups.isEmpty()) return List.of();
        List<String> result = new ArrayList<>();
        for (ItemDefinition.ContainerLootGroup group : groups) {
            List<String> items = group.getItemKeys();
            if (items != null && !items.isEmpty()) result.add(items.get(ThreadLocalRandom.current().nextInt(items.size())));
        }
        return result;
    }
}
