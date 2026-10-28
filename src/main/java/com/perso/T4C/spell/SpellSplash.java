package com.perso.T4C.spell;

import com.perso.T4C.config.GameConstants;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * The pure target-selection and splash arithmetic shared by single-player ({@link
 * SpellAreaTargeting} delegates here, behaviour unchanged) and the online spell engine. It holds
 * no spell, player or monster types so the server's domain module can compile it.
 */
public final class SpellSplash {
  public static final int MAX_TARGETS = 8;
  public static final double RESIDUAL_DAMAGE_FACTOR = 0.25;

  private SpellSplash() {}

  /** Select before applying damage: deaths may mutate the live monster collection. */
  public static <T> List<T> nearest(
      Collection<T> candidates, Predicate<T> eligible, ToDoubleFunction<T> distance, int reserved) {
    return candidates.stream()
        .filter(eligible)
        .distinct()
        .sorted(Comparator.comparingDouble(distance))
        .limit(Math.max(0, MAX_TARGETS - reserved))
        .toList();
  }

  /** The eight neighbouring tiles, including diagonals; coordinates are world pixels. */
  public static boolean isAdjacent(float cx, float cy, float tx, float ty) {
    return Math.abs(tx - cx) <= GameConstants.GRID_W && Math.abs(ty - cy) <= GameConstants.GRID_H;
  }

  /** A quarter of the rolled damage, at least 1; zero when the roll was not damage. */
  public static int residualDamage(int healthDelta) {
    return healthDelta < 0 ? Math.max(1, (int) (-healthDelta * RESIDUAL_DAMAGE_FACTOR)) : 0;
  }
}
