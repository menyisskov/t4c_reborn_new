package com.perso.T4C.teleport;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.helper.CollisionReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

/** Find a clear arrival near the landmark rather than placing the player on its NPC or chest. */
public final class TravelArrival {
  private static final Map<Integer, CollisionReader> COLLISIONS = new HashMap<>();

  private TravelArrival() {}

  public record Tile(int x, int y) {}

  public static Tile find(TravelCatalogue.Destination destination) {
    if (!destination.mapped()) return null;
    CollisionReader reader;
    synchronized (COLLISIONS) {
      reader = COLLISIONS.get(destination.z());
      if (reader == null) {
        try {
          reader =
              new CollisionReader(
                  new File(MapDefinition.fromZ(destination.z()).getCollisionPath()));
        } catch (IOException e) {
          throw new IllegalStateException("Cannot read travel destination collision map", e);
        }
        COLLISIONS.put(destination.z(), reader);
      }
    }
    return find(destination.x(), destination.y(), reader::hasCollision);
  }

  static Tile find(int originX, int originY, BiPredicate<Integer, Integer> blocked) {
    // Two tiles leave room for the target NPC/chest while remaining in normal talking range.
    for (int radius = 2; radius <= 32; radius++) {
      for (int y = originY - radius; y <= originY + radius; y++) {
        for (int x = originX - radius; x <= originX + radius; x++) {
          if (Math.max(Math.abs(x - originX), Math.abs(y - originY)) != radius) continue;
          boolean clear = true;
          for (int dx = -GameConstants.PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
              dx <= GameConstants.PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
              dx++) {
            for (int dy = -GameConstants.PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;
                dy <= GameConstants.PLAYER_COLLISION_FOOTPRINT_BELOW_TILES;
                dy++) {
              if (blocked.test(x + dx, y + dy)) clear = false;
            }
          }
          if (clear) return new Tile(x, y);
        }
      }
    }
    return null;
  }
}
