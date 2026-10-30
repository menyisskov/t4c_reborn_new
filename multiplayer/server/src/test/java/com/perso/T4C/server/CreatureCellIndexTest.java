package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** No database. The cell index over spawn anchors and its interplay with AreaOfInterest.Coverage (hysteresis, unions, overlay). */
class CreatureCellIndexTest {
  private static long cell(int tileX, int tileY) {
    return AreaOfInterest.cellKey(AreaOfInterest.cell(tileX), AreaOfInterest.cell(tileY));
  }

  @Test void everyAnchorIsInExactlyItsHomeCellAndNothingIsLost() {
    var random = new Random(7);
    int n = 5000;
    var xs = new int[n];
    var ys = new int[n];
    for (int i = 0; i < n; i++) { xs[i] = random.nextInt(6144); ys[i] = random.nextInt(3072); }
    var index = new CreatureCellIndex(xs, ys);
    int total = 0;
    for (int i = 0; i < n; i++) {
      assertEquals(cell(xs[i], ys[i]), index.homeCell(i));
      assertTrue(Arrays.binarySearch(index.atHome(index.homeCell(i)), i) >= 0, "anchor " + i + " is in its cell");
    }
    var seen = new java.util.HashSet<Long>();
    for (int i = 0; i < n; i++) if (seen.add(index.homeCell(i))) total += index.atHome(index.homeCell(i)).length;
    assertEquals(n, total, "the cells partition the anchors");
    assertEquals(seen.size(), index.occupiedCells());
    assertEquals(0, index.atHome(AreaOfInterest.cellKey(500, 500)).length, "an empty cell lists nothing");
  }

  @Test void aWindowWakesExactlyTheSpawnsOfItsCellsAndDisplacedCreaturesWakeFromWhereTheyStand() {
    // Spawn 0 homes at (10,10), spawn 1 at (40,10), spawn 2 at (200,200).
    var index = new CreatureCellIndex(new int[] {10, 40, 200}, new int[] {10, 10, 200});
    var window = AreaOfInterest.window(20, 20, AreaOfInterest.SIMULATION_HALF_EXTENT, 6144, 3072);
    var woken = new java.util.TreeSet<Integer>();
    for (long key : AreaOfInterest.cellsCovering(window)) for (int i : index.spawnsIn(key)) woken.add(i);
    assertEquals(java.util.Set.of(0, 1), woken, "spawn 2 is far outside the 129 x 129 window");
    // A creature whose shutdown left it standing near the player is woken through the cell it stands in.
    index.displace(2, 25.5, 25.5);
    assertEquals(cell(25, 25), index.currentCell(2));
    assertTrue(Arrays.binarySearch(index.spawnsIn(cell(25, 25)), 2) >= 0);
    assertEquals(cell(200, 200), index.homeCell(2), "home is unchanged");
    index.clearDisplaced(2);
    assertEquals(cell(200, 200), index.currentCell(2));
    assertEquals(0, index.displacedCount());
    // A position inside the home cell is not an overlay.
    index.displace(0, 12.5, 14.5);
    assertEquals(0, index.displacedCount());
  }

  @Test void coverageIsTheUnionOfWindowsAndReleasesOnlyAfterTheHysteresis() {
    var index = new CreatureCellIndex(new int[] {10, 3000}, new int[] {10, 1500});
    var coverage = new AreaOfInterest.Coverage();
    var near = AreaOfInterest.window(10, 10, AreaOfInterest.SIMULATION_HALF_EXTENT, 6144, 3072);
    var far = AreaOfInterest.window(3000, 1500, AreaOfInterest.SIMULATION_HALF_EXTENT, 6144, 3072);
    coverage.update(0, List.of(near, far));
    assertTrue(coverage.covered(index.homeCell(0)));
    assertTrue(coverage.covered(index.homeCell(1)));
    // Only the first player remains: the far cells survive the hysteresis, then go.
    var released = coverage.update(AreaOfInterest.RELEASE_TICKS - 1, List.of(near));
    assertTrue(released.isEmpty());
    assertTrue(coverage.covered(index.homeCell(1)), "a cell is held for 15 s after its last window");
    released = coverage.update(AreaOfInterest.RELEASE_TICKS, List.of(near));
    assertFalse(released.isEmpty());
    assertFalse(coverage.covered(index.homeCell(1)));
    assertTrue(coverage.covered(index.homeCell(0)), "the near window keeps its own cell");
  }

  @Test void twentyWindowsAtTheMapEdgesClampAndStayStable() {
    var windows = new java.util.ArrayList<AreaOfInterest.Window>();
    for (int i = 0; i < 20; i++) windows.add(AreaOfInterest.window(i % 2 == 0 ? 0 : 6143, i % 3 == 0 ? 0 : 3071, 64, 6144, 3072));
    var coverage = new AreaOfInterest.Coverage();
    coverage.update(5, windows);
    var first = coverage.keys();
    var again = new AreaOfInterest.Coverage();
    again.update(5, windows);
    assertEquals(first, again.keys(), "the same inputs give the same cells");
    for (long key : first) {
      assertTrue(AreaOfInterest.cellX(key) >= 0 && AreaOfInterest.cellX(key) < AreaOfInterest.cellsAcross(6144));
      assertTrue(AreaOfInterest.cellY(key) >= 0 && AreaOfInterest.cellY(key) < AreaOfInterest.cellsAcross(3072));
    }
  }
}
