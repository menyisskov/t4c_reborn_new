package com.perso.T4C.render;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public final class CollisionOverlayRenderer {
  private CollisionOverlayRenderer() {}

  public static void render(
      ShapeRenderer renderer,
      int startX,
      int endX,
      int startY,
      int endY,
      BiPredicate<Integer, Integer> blocked,
      BiFunction<Integer, Integer, float[]> color) {
    renderer.begin(ShapeRenderer.ShapeType.Filled);
    for (int y = startY; y <= endY; y++)
      for (int x = startX; x <= endX; x++) {
        if (!blocked.test(x, y)) continue;
        float[] c = color.apply(x, y);
        renderer.setColor(c[0], c[1], c[2], c[3]);
        renderer.rect(x * GRID_W, y * GRID_H, GRID_W, GRID_H);
      }
    renderer.end();
  }
}
