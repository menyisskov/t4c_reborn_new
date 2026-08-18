package com.perso.T4C.render;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Collection;

public final class TeleportOverlayRenderer {
  private TeleportOverlayRenderer() {}

  public interface Entry {
    int sourceZ();

    int sourceX();

    int sourceY();

    int targetZ();

    int targetX();

    int targetY();
  }

  public static void render(
      ShapeRenderer renderer,
      Collection<? extends Entry> entries,
      int currentZ,
      int startX,
      int endX,
      int startY,
      int endY) {
    renderer.begin(ShapeRenderer.ShapeType.Filled);
    for (Entry entry : entries) {
      renderer.setColor(0.05f, 0.95f, 0.25f, 0.42f);
      if (entry.sourceZ() == currentZ
          && visible(entry.sourceX(), entry.sourceY(), startX, endX, startY, endY))
        renderer.rect(entry.sourceX() * GRID_W, entry.sourceY() * GRID_H, GRID_W, GRID_H);
      if (entry.targetZ() == currentZ
          && visible(entry.targetX(), entry.targetY(), startX, endX, startY, endY))
        renderer.rect(entry.targetX() * GRID_W, entry.targetY() * GRID_H, GRID_W, GRID_H);
    }
    renderer.end();
  }

  private static boolean visible(int x, int y, int startX, int endX, int startY, int endY) {
    return x >= startX - 1 && x <= endX + 1 && y >= startY - 1 && y <= endY + 1;
  }
}
