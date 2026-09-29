package com.perso.T4C.helper;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import java.util.function.BiPredicate;

/** Migrates a decoded save before map selection; the next normal save persists the version. */
public final class PlayerWorldLayoutMigration {
  private PlayerWorldLayoutMigration() {}

  public static PlayerStateDto migrate(PlayerStateDto state) {
    return migrate(state, AvalonWorldLayout::isLegacyAvalonTile);
  }

  static PlayerStateDto migrate(PlayerStateDto state, BiPredicate<Integer, Integer> legacyTile) {
    if (state == null || state.worldLayoutVersion >= AvalonWorldLayout.WORLD_LAYOUT_VERSION)
      return state;
    if (state.z == 0 && owns(legacyTile, state.x, state.y)) {
      state.x += AvalonWorldLayout.SHIFT_X;
    }
    if (state.respawnPointDefined
        && state.respawnWorldZ == 0
        && owns(legacyTile, state.respawnWorldX / GRID_W, state.respawnWorldY / GRID_H)) {
      state.respawnWorldX += AvalonWorldLayout.SHIFT_X * GRID_W;
    }
    state.worldLayoutVersion = AvalonWorldLayout.WORLD_LAYOUT_VERSION;
    return state;
  }

  private static boolean owns(BiPredicate<Integer, Integer> legacyTile, float x, float y) {
    return Float.isFinite(x)
        && Float.isFinite(y)
        && legacyTile.test((int) Math.floor(x), (int) Math.floor(y));
  }
}
