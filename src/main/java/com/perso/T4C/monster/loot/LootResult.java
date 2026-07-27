package com.perso.T4C.monster.loot;

import java.util.List;

/**
 * Outcome of rolling a {@link LootTable}: the gold amount and the item names that dropped.
 *
 * @param gold      gold amount dropped (>= 0)
 * @param itemNames item names that dropped (may be empty, never null)
 */
public record LootResult(int gold, List<String> itemNames) {

    public boolean isEmpty() {
        return gold <= 0 && (itemNames == null || itemNames.isEmpty());
    }
}
