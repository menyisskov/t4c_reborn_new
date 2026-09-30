package com.perso.T4C.spell;

import com.perso.T4C.config.GameConstants;

/** Targeting rules for attack spells that radiate out from their caster. */
public final class SpellAreaTargeting {
  private SpellAreaTargeting() {}

  public static boolean isSelfCenteredAttack(SpellData spell) {
    return spell != null && spell.isAttack() && spell.getTargetType() == 18 && spell.getRadius() > 0;
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
