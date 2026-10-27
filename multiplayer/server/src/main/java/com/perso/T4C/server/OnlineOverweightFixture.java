package com.perso.T4C.server;

import com.perso.T4C.item.CarryCapacityRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

/**
 * Sizes an explicit over-weight inventory fixture for a character that keeps its normal starting kit
 * (used by {@code NativeLootProbe}). The stack is added to the existing inventory and never replaces
 * it, so every equipped instance stays valid and keeps its strength and damage contribution. The
 * size is derived from the shared {@link CarryCapacityRules} and the same carried-weight and
 * effective-strength helpers the server pickup path uses; no number is copied from them. It only
 * builds saved state: the server's pickup guards are not touched.
 */
final class OnlineOverweightFixture {
  /** The stack to add and the weights that justify it. */
  record Fixture(String itemKey, int quantity, long carriedBefore, long carriedAfter, long maximum) {}

  private OnlineOverweightFixture() {}

  /**
   * The authored item used for the stack: the heaviest droppable, non-unique item, ties broken by key
   * so the choice is reproducible. A heavy item keeps the stack small.
   */
  static ItemContent.Definition fixtureItem(ItemContent content) {
    return content.items().values().stream()
        .filter(d -> d.weight() > 0 && !d.unique() && !d.undroppable())
        .max(Comparator.comparingLong(ItemContent.Definition::weight)
            .thenComparing(Comparator.comparing(ItemContent.Definition::key).reversed()))
        .orElseThrow(() -> new IllegalArgumentException("Precondition failed: no weighted droppable item in content"));
  }

  /**
   * Smallest stack that leaves the character strictly above its carry maximum, so any later pickup,
   * whatever its weight, is refused as too heavy. Effective strength includes equipment bonuses.
   */
  static Fixture size(CharacterProgress progress, ItemContent content) {
    ItemContent.Definition item = fixtureItem(content);
    long maximum = CarryCapacityRules.maximumWeight(OnlinePickupRules.effectiveStrength(progress, content, 0L));
    long carried = OnlinePickupRules.carriedWeight(progress, content);
    long needed = Math.max(0L, maximum - carried) / item.weight() + 1L;
    if (needed > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Precondition failed: over-weight stack would exceed the quantity range");
    }
    return new Fixture(item.key(), (int) needed, carried, carried + needed * item.weight(), maximum);
  }

  /** The same progress with the fixture stack appended; equipment and the rest of the kit are untouched. */
  static CharacterProgress apply(CharacterProgress p, Fixture fixture) {
    var inventory = new ArrayList<>(p.inventory());
    inventory.add(new CharacterProgress.Item(UUID.randomUUID(), fixture.itemKey(), fixture.quantity(), 0));
    return new CharacterProgress(p.version(), p.characterClass(), p.gender(), p.attributes(), p.vitals(),
        p.advancement(), inventory, p.equipment(), p.skills(), p.spells(), p.questFlags(), p.storage(),
        p.storageGold(), p.respawn(), p.buffs(), p.cooldowns(), p.hiddenUntil(), p.companion());
  }
}
