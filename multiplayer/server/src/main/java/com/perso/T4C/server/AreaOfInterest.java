package com.perso.T4C.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Pure area-of-interest arithmetic for streamed regions (WORLD_STREAMING_DESIGN.md 3.3): per-viewer
 * windows centred on a committed position and clamped to the region, a 32-tile cell grid, and a
 * coverage tracker with release hysteresis. No clocks, randomness or I/O: the same inputs always
 * give the same outputs, so tests and the simulation thread agree. Coordinates are region-local tiles.
 */
final class AreaOfInterest {
  private AreaOfInterest() {}

  /** What a snapshot lists: a 97 x 97 square around the viewer (the screen shows about 60 x 67 tiles). */
  static final int SNAPSHOT_HALF_EXTENT = 48;
  /** What is simulated: the snapshot window plus 16 so nothing visible is frozen. */
  static final int SIMULATION_HALF_EXTENT = 64;
  /** Side of one index cell in tiles; a power of two so cell maths is shifts. */
  static final int CELL = 32;
  private static final int CELL_SHIFT = 5;
  /** A cell stays covered this many ticks (15 s at 34 Hz) after the last window that touched it. */
  static final long RELEASE_TICKS = 510;

  /** Inclusive-exclusive tile rectangle {@code [x, x+width) x [y, y+height)}; never empty. */
  record Window(int x, int y, int width, int height) {
    Window {
      if (x < 0 || y < 0 || width < 1 || height < 1) throw new IllegalArgumentException("Invalid window");
    }
    boolean contains(double px, double py) {
      return px >= x && py >= y && px < (long) x + width && py < (long) y + height;
    }
    int right() { return x + width; }
    int bottom() { return y + height; }
  }

  /**
   * The square window of the given half extent around a viewer at {@code (cx, cy)} (any finite
   * position), clamped to {@code [0, regionWidth) x [0, regionHeight)}. A viewer outside the region
   * gets the window of the nearest tile. The window is never empty and never leaves the region.
   */
  static Window window(double cx, double cy, int halfExtent, int regionWidth, int regionHeight) {
    if (halfExtent < 0 || regionWidth < 1 || regionHeight < 1 || !Double.isFinite(cx) || !Double.isFinite(cy))
      throw new IllegalArgumentException("Invalid window request");
    int tx = clamp((int) Math.floor(cx), regionWidth - 1);
    int ty = clamp((int) Math.floor(cy), regionHeight - 1);
    int x0 = Math.max(0, tx - halfExtent);
    int y0 = Math.max(0, ty - halfExtent);
    int x1 = (int) Math.min((long) regionWidth, (long) tx + halfExtent + 1);
    int y1 = (int) Math.min((long) regionHeight, (long) ty + halfExtent + 1);
    return new Window(x0, y0, x1 - x0, y1 - y0);
  }

  private static int clamp(int value, int max) { return Math.max(0, Math.min(max, value)); }

  /** Cell column or row of a region-local tile coordinate. */
  static int cell(int tile) { return tile >> CELL_SHIFT; }

  /** Stable key of a cell: row in the high word, column in the low word. */
  static long cellKey(int cellX, int cellY) { return ((long) cellY << 32) | (cellX & 0xFFFFFFFFL); }

  static int cellX(long key) { return (int) key; }

  static int cellY(long key) { return (int) (key >> 32); }

  /** Number of cells along an edge of {@code tiles}. */
  static int cellsAcross(int tiles) { return (tiles + CELL - 1) >> CELL_SHIFT; }

  /** Keys of every cell the window touches, in ascending key order (row-major), without duplicates. */
  static List<Long> cellsCovering(Window window) {
    int cx0 = cell(window.x()), cx1 = cell(window.right() - 1);
    int cy0 = cell(window.y()), cy1 = cell(window.bottom() - 1);
    var keys = new ArrayList<Long>((cx1 - cx0 + 1) * (cy1 - cy0 + 1));
    for (int cy = cy0; cy <= cy1; cy++) for (int cx = cx0; cx <= cx1; cx++) keys.add(cellKey(cx, cy));
    return keys;
  }

  /** Chebyshev distance in tiles between two region-local points. */
  static double distance(double ax, double ay, double bx, double by) {
    return Math.max(Math.abs(ax - bx), Math.abs(ay - by));
  }

  /**
   * Which cells are covered, with hysteresis: a cell becomes covered at once when a window touches
   * it and is released only {@link #RELEASE_TICKS} ticks after the last window that touched it.
   * Used by the simulation thread only (not thread-safe).
   */
  static final class Coverage {
    private final long releaseTicks;
    private final Map<Long, Long> lastTouched = new HashMap<>();

    Coverage() { this(RELEASE_TICKS); }

    Coverage(long releaseTicks) {
      if (releaseTicks < 0) throw new IllegalArgumentException("Invalid release delay");
      this.releaseTicks = releaseTicks;
    }

    /**
     * Records the windows alive at {@code tick} (ticks must not go backwards) and returns the keys
     * newly released by this call, ascending. Cells touched now are covered immediately.
     */
    List<Long> update(long tick, Collection<Window> windows) {
      for (var window : windows) for (long key : cellsCovering(window)) lastTouched.merge(key, tick, Math::max);
      var released = new TreeSet<Long>();
      for (Iterator<Map.Entry<Long, Long>> it = lastTouched.entrySet().iterator(); it.hasNext();) {
        var entry = it.next();
        if (entry.getValue() != tick && tick - entry.getValue() >= releaseTicks) { released.add(entry.getKey()); it.remove(); }
      }
      return List.copyOf(released);
    }

    boolean covered(long key) { return lastTouched.containsKey(key); }

    int size() { return lastTouched.size(); }

    /** Covered keys in ascending order. */
    List<Long> keys() { return List.copyOf(new TreeSet<>(lastTouched.keySet())); }
  }
}
