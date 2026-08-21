package com.perso.T4C.combat;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.helper.CollisionManager;

public final class CombatGeometry {
  private CombatGeometry() {}

  public static boolean hasLineOfSight(Vector2 from, Vector2 to) {
    return traceLineOfSight(from, to, false);
  }

  public static boolean hasTalkLineOfSight(Vector2 from, Vector2 to) {
    return traceLineOfSight(from, to, true);
  }

  private static boolean traceLineOfSight(Vector2 from, Vector2 to, boolean forTalking) {
    if (from == null || to == null || !CollisionManager.getInstance().isInitialized()) return true;
    float distance = from.dst(to);
    if (distance <= 0.001f) return true;
    int destTileX = (int) (to.x / GRID_W);
    int destTileY = (int) (to.y / GRID_H);
    int steps = Math.max(1, (int) Math.ceil(distance / (Math.min(GRID_W, GRID_H) * 0.5f)));
    for (int i = 1; i < steps; i++) {
      float alpha = i / (float) steps;
      float x = from.x + (to.x - from.x) * alpha;
      float y = from.y + (to.y - from.y) * alpha;
      if (forTalking
          && (int) (x / GRID_W) == destTileX
          && (int) (y / GRID_H) == destTileY) {
        continue;
      }
      boolean blocked =
          forTalking
              ? CollisionManager.getInstance().blocksTalkLineOfSight(x, y)
              : CollisionManager.getInstance().blocksLineOfSight(x, y);
      if (blocked) return false;
    }
    return true;
  }
}
