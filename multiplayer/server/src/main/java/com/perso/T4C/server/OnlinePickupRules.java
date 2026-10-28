package com.perso.T4C.server;

import com.perso.T4C.item.CarryCapacityRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import java.util.Optional;
import java.util.UUID;

/**
 * Pickup decisions for ground loot. Pure: inputs are the trusted server state. The database
 * re-checks the same rules inside the claim transaction.
 */
public final class OnlinePickupRules {
  /** Matches the offline OBJECT_MAX_INTERACTION_DISTANCE, in tiles (Chebyshev). */
  public static final double MAX_TILE_DISTANCE = 8d;
  private static final int STRENGTH_STAT_ID = 3;

  private OnlinePickupRules() {}

  public enum Refusal {
    LOOT_UNAVAILABLE("loot_unavailable"),
    LOOT_RESERVED("loot_reserved"),
    OUT_OF_RANGE("out_of_range"),
    UNSUPPORTED_ITEM("unsupported_item"),
    TOO_HEAVY("too_heavy");

    private final String code;

    Refusal(String code) {
      this.code = code;
    }

    public String code() {
      return code;
    }
  }

  public record Loot(UUID id, String key, int world, double x, double y, UUID ownerCharacter, long ownerUntil,
      long expiresAt) {}

  public record Pickup(UUID character, int world, double x, double y, long now) {}

  /** Empty when the pickup may proceed. Checks run in the same order as the database claim. */
  public static Optional<Refusal> refusal(Loot loot, Pickup pickup, CharacterProgress progress, ItemContent content) {
    if (pickup.now() >= loot.expiresAt() || loot.world() != pickup.world()) {
      return Optional.of(Refusal.LOOT_UNAVAILABLE);
    }
    if (pickup.now() < loot.ownerUntil() && !loot.ownerCharacter().equals(pickup.character())) {
      return Optional.of(Refusal.LOOT_RESERVED);
    }
    double distance = Math.max(Math.abs(pickup.x() - loot.x()), Math.abs(pickup.y() - loot.y()));
    if (!(distance <= MAX_TILE_DISTANCE)) return Optional.of(Refusal.OUT_OF_RANGE);
    ItemContent.Definition item = content.items().get(loot.key());
    if (item == null || item.unique()) return Optional.of(Refusal.UNSUPPORTED_ITEM);
    long maximum = CarryCapacityRules.maximumWeight(effectiveStrength(progress, content, pickup.now()));
    if (!CarryCapacityRules.canAdd(carriedWeight(progress, content), item.weight(), maximum)) {
      return Optional.of(Refusal.TOO_HEAVY);
    }
    return Optional.empty();
  }

  /**
   * Equipped gear is also an inventory instance in committed progress, so each instance is counted
   * once here. Storage is not carried weight.
   */
  static long carriedWeight(CharacterProgress progress, ItemContent content) {
    long total = 0L;
    for (CharacterProgress.Item item : progress.inventory()) {
      ItemContent.Definition definition = content.items().get(item.key());
      if (definition != null) total += Math.max(0L, definition.weight()) * item.quantity();
    }
    return total;
  }

  static int effectiveStrength(CharacterProgress progress, ItemContent content, long now) {
    OnlineBuffs.requireModelled(progress, now);
    return progress.attributes().strength()
        + OnlineBuffs.totals(progress, now).getOrDefault("str", 0)
        + OnlineEquipmentBonuses.resolve(progress, content).getOrDefault(STRENGTH_STAT_ID, 0);
  }
}
