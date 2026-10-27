package com.perso.T4C.monster.core;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.MONSTER_AGGRO_LEASH_RANGE;
import static com.perso.T4C.config.GameConstants.MONSTER_AGGRO_RANGE;
import static com.perso.T4C.config.GameConstants.MONSTER_RETALIATION_LEASH_RANGE;

import java.util.List;

/** Scalar pursuit decisions shared with the offline monster caller. Coordinates and distances are pixels. */
public final class MonsterPursuitRules {
  private MonsterPursuitRules() {}

  public static boolean exceedsHuntingGroundLeash(
      float playerX, float playerY, int fieldX, int fieldY, int leashRadiusTiles) {
    return Math.max(Math.abs(playerX / GRID_W - fieldX), Math.abs(playerY / GRID_H - fieldY))
        > leashRadiusTiles;
  }

  /** Range and aggression prefilter; callers still check line of sight and sanctuary. */
  public static boolean inNaturalAcquisitionRange(
      boolean aggressive, float distancePixels, boolean huntingGround) {
    return aggressive
        && distancePixels <= (huntingGround ? 12f * GRID_W : MONSTER_AGGRO_RANGE);
  }

  public static boolean withinNaturalLeash(float distancePixels) {
    return distancePixels <= MONSTER_AGGRO_LEASH_RANGE;
  }

  public static boolean withinPlayerLeash(float distancePixels, boolean retaliation) {
    return distancePixels
        <= (retaliation ? MONSTER_RETALIATION_LEASH_RANGE : MONSTER_AGGRO_LEASH_RANGE);
  }

  /** Minimum one tile; authored spell range may extend the maximum reach. */
  public static float attackReachPixels(List<MonsterDef.Attack> attacks) {
    int maximumTiles = 1;
    for (MonsterDef.Attack attack : attacks) {
      if (attack != null) maximumTiles = Math.max(maximumTiles, attack.getMaxRangeTiles());
    }
    return maximumTiles * Math.max(GRID_W, GRID_H);
  }

  public static boolean withinAttackReach(float distancePixels, List<MonsterDef.Attack> attacks) {
    return distancePixels <= attackReachPixels(attacks);
  }

  /** Existing attack selection rounds pixel distance to the nearest widest-grid tile. */
  public static int tileRange(float distancePixels) {
    return (int) Math.round(distancePixels / Math.max(GRID_W, GRID_H));
  }
}
