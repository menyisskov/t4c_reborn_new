package com.perso.T4C.editor.build;

import com.perso.T4C.helper.MapReader;
import java.util.*;

/** Validates an entire gesture before applying it, and retains exact before/after state. */
public final class BuildEdits {
  private BuildEdits() {}

  public record Change(int x, int y, MapStamp.Tile before, MapStamp.Tile after) {}

  public record Edit(List<Change> changes) {
    public Edit {
      changes = List.copyOf(changes);
    }

    public void apply(MapReader map, byte[] collision, boolean forward) {
      for (Change c : changes) (forward ? c.after : c.before).write(map, collision, c.x, c.y);
    }
  }

  public static Edit stamp(
      MapReader map, byte[] collision, MapStamp stamp, int x, int y, boolean replace) {
    List<Change> changes = new ArrayList<>();
    for (var cell : stamp.cells()) {
      int tx = x + cell.x(), ty = y + cell.y();
      if (tx < 0 || ty < 0 || tx >= map.getWidth() || ty >= map.getHeight())
        throw new IllegalArgumentException("editor.build.outside");
      var before = MapStamp.Tile.read(map, collision, tx, ty);
      if (!replace
          && before.decor() != null
          && !before.decor().isBlank()
          && !before.equals(cell.tile()))
        throw new IllegalArgumentException("editor.build.occupied");
      if (!before.equals(cell.tile())) changes.add(new Change(tx, ty, before, cell.tile()));
    }
    return new Edit(changes);
  }

  public static Edit paint(
      MapReader map, byte[] collision, List<Placement> placements, boolean replace) {
    if (placements.size() > MapStamp.MAX_CELLS)
      throw new IllegalArgumentException("editor.build.too_large");
    Map<BuildGeometry.Point, Placement> unique = new LinkedHashMap<>();
    for (var p : placements) unique.put(new BuildGeometry.Point(p.x, p.y), p);
    List<Change> changes = new ArrayList<>();
    for (var p : unique.values()) {
      if (p.x < 0 || p.y < 0 || p.x >= map.getWidth() || p.y >= map.getHeight())
        throw new IllegalArgumentException("editor.build.outside");
      var before = MapStamp.Tile.read(map, collision, p.x, p.y);
      if (!p.ground
          && !replace
          && before.decor() != null
          && !before.decor().isBlank()
          && !before.decor().equals(p.sprite))
        throw new IllegalArgumentException("editor.build.occupied");
      var after =
          new MapStamp.Tile(
              p.ground ? p.sprite : before.ground(),
              p.ground ? before.decor() : p.sprite,
              p.ground ? before.scaleX() : p.scaleX,
              p.ground ? before.scaleY() : p.scaleY,
              p.ground ? before.offsetX() : p.offsetX,
              p.ground ? before.offsetY() : p.offsetY,
              p.ground ? before.depth() : p.depth,
              p.blocking ? 1 : before.collision());
      if (!before.equals(after)) changes.add(new Change(p.x, p.y, before, after));
    }
    return new Edit(changes);
  }

  public record Placement(
      int x,
      int y,
      String sprite,
      boolean ground,
      float scaleX,
      float scaleY,
      float offsetX,
      float offsetY,
      int depth,
      boolean blocking) {}
}
