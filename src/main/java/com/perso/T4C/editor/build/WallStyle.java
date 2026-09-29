package com.perso.T4C.editor.build;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Vetted straight/corner tiles sampled from actual buildings, including their draw offsets. */
public record WallStyle(
    String name,
    MapStamp.Tile descending,
    MapStamp.Tile ascending,
    MapStamp.Tile north,
    MapStamp.Tile east,
    MapStamp.Tile south,
    MapStamp.Tile west) {
  public static WallStyle load(Path path) throws IOException {
    WallStyle style = new Gson().fromJson(Files.readString(path), WallStyle.class);
    if (style == null
        || style.descending == null
        || style.ascending == null
        || style.north == null
        || style.east == null
        || style.south == null
        || style.west == null) throw new IOException("Incomplete wall style");
    return style;
  }

  public List<BuildEdits.Placement> line(int x0, int y0, int x1, int y1) {
    var points = BuildGeometry.line(x0, y0, x1, y1, 1, true);
    var end = points.getLast();
    MapStamp.Tile tile = (long) (end.x() - x0) * (end.y() - y0) >= 0 ? descending : ascending;
    return points.stream().map(p -> place(p, tile)).toList();
  }

  public List<BuildEdits.Placement> room(int x0, int y0, int x1, int y1) {
    var corners = BuildGeometry.roomCorners(x0, y0, x1, y1);
    List<BuildEdits.Placement> result = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      var a = corners.get(i);
      var b = corners.get((i + 1) % 4);
      result.addAll(line(a.x(), a.y(), b.x(), b.y()));
    }
    // The same named corner is used regardless of which direction the user dragged.
    for (var p : corners) {
      var tile =
          p.y() == corners.stream().mapToInt(BuildGeometry.Point::y).min().orElseThrow()
              ? north
              : p.y() == corners.stream().mapToInt(BuildGeometry.Point::y).max().orElseThrow()
                  ? south
                  : p.x() == corners.stream().mapToInt(BuildGeometry.Point::x).max().orElseThrow()
                      ? east
                      : west;
      result.add(place(p, tile));
    }
    return result;
  }

  private static BuildEdits.Placement place(BuildGeometry.Point p, MapStamp.Tile t) {
    return new BuildEdits.Placement(
        p.x(),
        p.y(),
        t.decor(),
        false,
        t.scaleX(),
        t.scaleY(),
        t.offsetX(),
        t.offsetY(),
        t.depth(),
        true);
  }
}
