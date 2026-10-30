package com.perso.T4C.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Index of a streamed region's hosted spawn anchors over the 32-tile cell grid of {@link AreaOfInterest}
 * (WORLD_STREAMING_DESIGN.md 3.3/3.4). Spawns are identified by their position in the region's
 * hosted spawn list ("index"); the index holds nothing but those integers, so a dormant creature costs
 * a few bytes. A creature displaced from its home by a shutdown mid-chase is additionally indexed in
 * the cell it actually stands in, so a player walking up to it wakes it. Pure: no clocks or I/O.
 * Used by the simulation thread only.
 */
final class CreatureCellIndex {
  private static final int[] NONE = new int[0];
  private final long[] homeKey;
  private final Map<Long, int[]> atHome;
  private final Map<Integer, Long> displacedKey = new HashMap<>();
  private final Map<Long, List<Integer>> displacedAt = new HashMap<>();

  /** {@code tileX[i], tileY[i]} are the region-local home tiles of hosted spawn {@code i}. */
  CreatureCellIndex(int[] tileX, int[] tileY) {
    if (tileX.length != tileY.length) throw new IllegalArgumentException("Mismatched anchors");
    int n = tileX.length;
    homeKey = new long[n];
    var grouped = new HashMap<Long, List<Integer>>();
    for (int i = 0; i < n; i++) {
      if (tileX[i] < 0 || tileY[i] < 0) throw new IllegalArgumentException("Anchor outside the region");
      homeKey[i] = AreaOfInterest.cellKey(AreaOfInterest.cell(tileX[i]), AreaOfInterest.cell(tileY[i]));
      grouped.computeIfAbsent(homeKey[i], key -> new ArrayList<>()).add(i);
    }
    atHome = new HashMap<>(grouped.size() * 2);
    for (var entry : grouped.entrySet()) atHome.put(entry.getKey(), entry.getValue().stream().mapToInt(Integer::intValue).toArray());
  }

  int size() { return homeKey.length; }

  /** Cell key of the spawn's home anchor. */
  long homeCell(int index) { return homeKey[index]; }

  /** Spawns anchored in this cell, ascending; never null. */
  int[] atHome(long cellKey) { return atHome.getOrDefault(cellKey, NONE); }

  /** Number of cells that hold at least one anchor. */
  int occupiedCells() { return atHome.size(); }

  /** Spawn indices currently standing in this cell away from home (displaced overlay), ascending. */
  int[] displacedIn(long cellKey) {
    var list = displacedAt.get(cellKey);
    if (list == null || list.isEmpty()) return NONE;
    var out = list.stream().mapToInt(Integer::intValue).toArray();
    Arrays.sort(out);
    return out;
  }

  /** Every spawn a covered cell owes an activation: its anchors plus the displaced creatures standing in it. */
  int[] spawnsIn(long cellKey) {
    var home = atHome(cellKey);
    var away = displacedIn(cellKey);
    if (away.length == 0) return home;
    var out = Arrays.copyOf(home, home.length + away.length);
    int fill = home.length;
    for (int index : away) if (Arrays.binarySearch(home, index) < 0) out[fill++] = index;
    out = Arrays.copyOf(out, fill);
    Arrays.sort(out);
    return out;
  }

  /** Records that spawn {@code index} stands at region-local {@code (x, y)}; a position in its home cell clears the overlay. */
  void displace(int index, double x, double y) {
    clearDisplaced(index);
    long key = AreaOfInterest.cellKey(AreaOfInterest.cell((int) Math.floor(x)), AreaOfInterest.cell((int) Math.floor(y)));
    if (key == homeKey[index]) return;
    displacedKey.put(index, key);
    displacedAt.computeIfAbsent(key, k -> new ArrayList<>()).add(index);
  }

  void clearDisplaced(int index) {
    var key = displacedKey.remove(index);
    if (key == null) return;
    var list = displacedAt.get(key);
    if (list != null) { list.remove(Integer.valueOf(index)); if (list.isEmpty()) displacedAt.remove(key); }
  }

  /** The cell a displaced spawn stands in, or its home cell. */
  long currentCell(int index) {
    var key = displacedKey.get(index);
    return key != null ? key : homeKey[index];
  }

  int displacedCount() { return displacedKey.size(); }
}
