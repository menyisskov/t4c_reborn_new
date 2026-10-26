package com.perso.T4C.spell;

import com.perso.T4C.config.GameConstants;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/** Targeting rules for attack spells that radiate out from their caster. */
public final class SpellAreaTargeting {
  public static final int MAX_TARGETS = 8;
  public static final double RESIDUAL_DAMAGE_FACTOR = 0.25;

  private SpellAreaTargeting() {}

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

  /** The eight neighbouring tiles, including diagonals. */
  public static boolean isAdjacent(float cx, float cy, float tx, float ty) {
    return Math.abs(tx - cx) <= GameConstants.GRID_W && Math.abs(ty - cy) <= GameConstants.GRID_H;
  }

  public static int residualDamage(int healthDelta) {
    return healthDelta < 0 ? Math.max(1, (int) (-healthDelta * RESIDUAL_DAMAGE_FACTOR)) : 0;
  }

  public static boolean isSelfCenteredAttack(SpellData spell) {
    return spell != null
        && spell.isAttack()
        && spell.getTargetType() == 18
        && spell.getRadius() > 0;
  }

  /** World coordinates use rectangular tiles, so each axis must use its own tile dimension. */
  public static double distanceInTiles(float centerX, float centerY, float targetX, float targetY) {
    double x = (targetX - centerX) / GameConstants.GRID_W;
    double y = (targetY - centerY) / GameConstants.GRID_H;
    return Math.hypot(x, y);
  }

  public static boolean contains(
      SpellData spell, float centerX, float centerY, float targetX, float targetY) {
    return spell != null
        && distanceInTiles(centerX, centerY, targetX, targetY) <= spell.getRadius();
  }
}
