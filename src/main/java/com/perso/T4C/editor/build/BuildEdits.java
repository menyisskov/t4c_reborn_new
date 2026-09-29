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

  /** Moves decor and its blocking collision, leaving terrain and area rules in place. */
  public static Edit moveDecor(
      MapReader map, byte[] collision, Collection<BuildGeometry.Point> selection, int dx, int dy) {
    if (selection.size() > MapStamp.MAX_CELLS)
      throw new IllegalArgumentException("editor.build.too_large");
    Map<BuildGeometry.Point, MapStamp.Tile> sources = new LinkedHashMap<>();
    Map<BuildGeometry.Point, MapStamp.Tile> before = new LinkedHashMap<>();
    for (var p : selection) {
      checkBounds(map, p.x(), p.y());
      var tile = MapStamp.Tile.read(map, collision, p.x(), p.y());
      if (tile.decor() == null || tile.decor().isBlank())
        throw new IllegalArgumentException("editor.build.selection_changed");
      sources.put(p, tile);
      before.put(p, tile);
    }
    if (dx == 0 && dy == 0) return new Edit(List.of());
    for (var p : sources.keySet()) {
      long x = (long) p.x() + dx, y = (long) p.y() + dy;
      if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight())
        throw new IllegalArgumentException("editor.build.outside");
      var dest = new BuildGeometry.Point((int) x, (int) y);
      before.putIfAbsent(dest, MapStamp.Tile.read(map, collision, dest.x(), dest.y()));
      if (!sources.containsKey(dest)
          && before.get(dest).decor() != null
          && !before.get(dest).decor().isBlank())
        throw new IllegalArgumentException("editor.build.occupied");
    }
    var after = new LinkedHashMap<>(before);
    // Clear every source first so overlapping translations do not erase earlier placements.
    for (var entry : sources.entrySet()) {
      var t = entry.getValue();
      after.put(
          entry.getKey(),
          new MapStamp.Tile(
              t.ground(), null, 1, 1, 0, 0, 0, decorCollision(t.collision()) ? 0 : t.collision()));
    }
    for (var entry : sources.entrySet()) {
      var dest = new BuildGeometry.Point(entry.getKey().x() + dx, entry.getKey().y() + dy);
      var floor = after.get(dest);
      var t = entry.getValue();
      // A tile has only one collision value: never overwrite water, safe zones or existing
      // barriers.
      if (decorCollision(t.collision()) && floor.collision() != 0)
        throw new IllegalArgumentException("editor.build.collision_conflict");
      after.put(
          dest,
          new MapStamp.Tile(
              floor.ground(),
              t.decor(),
              t.scaleX(),
              t.scaleY(),
              t.offsetX(),
              t.offsetY(),
              t.depth(),
              decorCollision(t.collision()) ? t.collision() : floor.collision()));
    }
    List<Change> changes = new ArrayList<>();
    for (var entry : before.entrySet()) {
      var p = entry.getKey();
      if (!entry.getValue().equals(after.get(p)))
        changes.add(new Change(p.x(), p.y(), entry.getValue(), after.get(p)));
    }
    return new Edit(changes);
  }

  private static boolean decorCollision(int value) {
    return value == 1 || value == 2 || value == 9;
  }

  private static void checkBounds(MapReader map, int x, int y) {
    if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight())
      throw new IllegalArgumentException("editor.build.outside");
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
