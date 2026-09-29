package com.perso.T4C.editor.build;

import java.util.*;

/** The game's walls run along tile diagonals; no image rotation is used. */
public final class BuildGeometry {
  private BuildGeometry() {}

  public record Point(int x, int y) {}

  public static List<Point> line(int x0, int y0, int x1, int y1, int spacing, boolean diagonal) {
    if (spacing < 1) throw new IllegalArgumentException("Invalid spacing");
    if (diagonal) {
      int length = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
      x1 = x0 + (x1 < x0 ? -length : length);
      y1 = y0 + (y1 < y0 ? -length : length);
    }
    int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
    if (steps > 4096) throw new IllegalArgumentException("editor.build.too_large");
    List<Point> out = new ArrayList<>();
    if (steps == 0) return List.of(new Point(x0, y0));
    for (int i = 0; i <= steps; i += spacing)
      out.add(
          new Point(
              Math.round(x0 + (x1 - x0) * (i / (float) steps)),
              Math.round(y0 + (y1 - y0) * (i / (float) steps))));
    return out;
  }

  public static List<Point> roomCorners(int x0, int y0, int x1, int y1) {
    int a = Math.round(((x1 - x0) + (y1 - y0)) / 2f), b = Math.round(((y1 - y0) - (x1 - x0)) / 2f);
    if (a == 0) a = 1;
    if (b == 0) b = 1;
    return List.of(
        new Point(x0, y0),
        new Point(x0 + a, y0 + a),
        new Point(x0 + a - b, y0 + a + b),
        new Point(x0 - b, y0 + b));
  }
}
