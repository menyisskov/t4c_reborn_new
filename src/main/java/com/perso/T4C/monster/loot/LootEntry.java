package com.perso.T4C.monster.loot;

/**
 * One possible item drop in a {@link LootTable}.
 *
 * @param itemName item key stored in {@code assets/items/items.bin} (matches {@code ItemDefinition.get})
 * @param chance   probability the item drops, in [0..1]
 */
public record LootEntry(String itemName, float chance) {
}
