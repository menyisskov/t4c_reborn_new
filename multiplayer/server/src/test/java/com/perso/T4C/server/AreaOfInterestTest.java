package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class AreaOfInterestTest {
  private static final int W = 6144, H = 3072;

  @Test void aWindowIsCentredOnTheViewerTileAndHasTheDesignExtent() {
    var window = AreaOfInterest.window(2944.5, 1059.5, AreaOfInterest.SNAPSHOT_HALF_EXTENT, W, H);
    assertEquals(new AreaOfInterest.Window(2896, 1011, 97, 97), window);
    assertTrue(window.contains(2944.5, 1059.5));
    assertTrue(AreaOfInterest.SIMULATION_HALF_EXTENT > AreaOfInterest.SNAPSHOT_HALF_EXTENT,
        "nothing visible may be frozen: the simulation window must contain the snapshot window");
    assertEquals(129, AreaOfInterest.window(3000, 1000, AreaOfInterest.SIMULATION_HALF_EXTENT, W, H).width());
  }

  @Test void aWindowIsClampedToTheRegionAtEveryEdgeAndCorner() {
    assertEquals(new AreaOfInterest.Window(0, 0, 52, 52), AreaOfInterest.window(3.5, 3.5, 48, W, H));
    assertEquals(new AreaOfInterest.Window(0, 0, 49, 49), AreaOfInterest.window(0.0, 0.0, 48, W, H));
    assertEquals(new AreaOfInterest.Window(W - 49, H - 49, 49, 49), AreaOfInterest.window(W - 0.01, H - 0.01, 48, W, H));
    assertEquals(new AreaOfInterest.Window(W - 49, 0, 49, 52), AreaOfInterest.window(W - 0.5, 3.5, 48, W, H));
    // A region smaller than the window is covered whole.
    assertEquals(new AreaOfInterest.Window(0, 0, 10, 10), AreaOfInterest.window(5, 5, 48, 10, 10));
    assertEquals(new AreaOfInterest.Window(0, 0, 1, 1), AreaOfInterest.window(0, 0, 48, 1, 1));
  }

  @Test void aViewerOutsideTheRegionGetsTheWindowOfTheNearestTileAndBadInputIsRefused() {
    assertEquals(AreaOfInterest.window(0, 0, 48, W, H), AreaOfInterest.window(-500, -9, 48, W, H));
    assertEquals(AreaOfInterest.window(W - 1, H - 1, 48, W, H), AreaOfInterest.window(1e12, 1e12, 48, W, H));
    for (double bad : new double[] {Double.NaN, Double.POSITIVE_INFINITY})
      assertThrows(IllegalArgumentException.class, () -> AreaOfInterest.window(bad, 1, 48, W, H));
    assertThrows(IllegalArgumentException.class, () -> AreaOfInterest.window(1, 1, -1, W, H));
    assertThrows(IllegalArgumentException.class, () -> AreaOfInterest.window(1, 1, 48, 0, H));
  }

  @Test void containmentIsHalfOpenSoNeighbouringWindowsNeverShareATile() {
    var window = new AreaOfInterest.Window(10, 20, 5, 4);
    assertTrue(window.contains(10, 20));
    assertTrue(window.contains(14.99, 23.99));
    assertFalse(window.contains(15, 20));
    assertFalse(window.contains(10, 24));
    assertFalse(window.contains(9.99, 20));
    assertFalse(window.contains(Double.NaN, 20));
  }

  @Test void everyWindowStaysInsideTheRegionForRandomViewers() {
    var random = new Random(9);
    for (int i = 0; i < 20_000; i++) {
      double x = random.nextDouble() * (W + 200) - 100, y = random.nextDouble() * (H + 200) - 100;
      var window = AreaOfInterest.window(x, y, random.nextBoolean() ? 48 : 64, W, H);
      assertTrue(window.x() >= 0 && window.y() >= 0 && window.right() <= W && window.bottom() <= H);
      int tx = Math.max(0, Math.min(W - 1, (int) Math.floor(x))), ty = Math.max(0, Math.min(H - 1, (int) Math.floor(y)));
      assertTrue(window.contains(tx + .5, ty + .5), "the viewer's own tile is always inside its window");
    }
  }

  @Test void cellsCoveringAreAscendingUniqueAndExactlyTheCellsTheWindowTouches() {
    var window = AreaOfInterest.window(2944.5, 1059.5, 48, W, H);
    var cells = AreaOfInterest.cellsCovering(window);
    assertEquals(new HashSet<>(cells).size(), cells.size());
    assertEquals(cells.stream().sorted().toList(), cells);
    // 97 tiles starting at 2896 touch tile columns 2896..2992 = cells 90..93 and rows 1011..1107 = cells 31..34.
    assertEquals(16, cells.size());
    assertEquals(AreaOfInterest.cellKey(90, 31), cells.getFirst());
    assertEquals(AreaOfInterest.cellKey(93, 34), cells.getLast());
    assertEquals(4, AreaOfInterest.cellsCovering(AreaOfInterest.window(0, 0, 48, W, H)).size());
    assertEquals(1, AreaOfInterest.cellsCovering(new AreaOfInterest.Window(32, 32, 32, 32)).size());
    assertEquals(4, AreaOfInterest.cellsCovering(new AreaOfInterest.Window(31, 31, 2, 2)).size());
    for (long key : cells) {
      assertEquals(key, AreaOfInterest.cellKey(AreaOfInterest.cellX(key), AreaOfInterest.cellY(key)));
    }
    assertEquals(192, AreaOfInterest.cellsAcross(W));
    assertEquals(96, AreaOfInterest.cellsAcross(H));
    assertEquals(1, AreaOfInterest.cellsAcross(1));
  }

  @Test void aCellIsCoveredAtOnceAndReleasedOnlyAfterTheHysteresisDelay() {
    var coverage = new AreaOfInterest.Coverage();
    var here = AreaOfInterest.window(100, 100, 48, W, H);
    long key = AreaOfInterest.cellKey(AreaOfInterest.cell(100), AreaOfInterest.cell(100));
    assertFalse(coverage.covered(key));
    assertEquals(List.of(), coverage.update(0, List.of(here)));
    assertTrue(coverage.covered(key));
    // The viewer leaves; the cell holds for 15 s (510 ticks) from the last tick that touched it.
    assertEquals(List.of(), coverage.update(1, List.of()));
    assertEquals(List.of(), coverage.update(AreaOfInterest.RELEASE_TICKS - 1, List.of()));
    assertTrue(coverage.covered(key));
    var released = coverage.update(AreaOfInterest.RELEASE_TICKS, List.of());
    assertTrue(released.contains(key));
    assertFalse(coverage.covered(key));
    assertEquals(0, coverage.size());
  }

  @Test void touchingACellAgainRestartsItsHysteresisAndAnEdgePacingViewerNeverThrashes() {
    var coverage = new AreaOfInterest.Coverage();
    var west = AreaOfInterest.window(1000, 500, 48, W, H);
    var east = AreaOfInterest.window(1031, 500, 48, W, H); // paces back and forth across a cell boundary
    var releasedEver = new HashSet<Long>();
    for (long tick = 0; tick < 3000; tick += 5) {
      releasedEver.addAll(coverage.update(tick, List.of((tick / 5) % 2 == 0 ? west : east)));
    }
    var both = new HashSet<>(AreaOfInterest.cellsCovering(west));
    both.addAll(AreaOfInterest.cellsCovering(east));
    for (long key : both) assertTrue(coverage.covered(key));
    assertTrue(releasedEver.isEmpty(), "cells touched within the delay are never released: " + releasedEver);
  }

  @Test void coverageOfTwentyWindowsIsTheUnionAndIsDeterministic() {
    var random = new Random(4);
    var windows = new ArrayList<AreaOfInterest.Window>();
    for (int i = 0; i < 20; i++)
      windows.add(AreaOfInterest.window(random.nextInt(W), random.nextInt(H), AreaOfInterest.SIMULATION_HALF_EXTENT, W, H));
    var union = new HashSet<Long>();
    for (var window : windows) union.addAll(AreaOfInterest.cellsCovering(window));
    var first = new AreaOfInterest.Coverage();
    var second = new AreaOfInterest.Coverage();
    first.update(7, windows);
    second.update(7, windows.reversed());
    assertEquals(union.stream().sorted().toList(), first.keys());
    assertEquals(first.keys(), second.keys(), "window order must not change the result");
    // Same history, same releases, in ascending order.
    var a = first.update(7 + AreaOfInterest.RELEASE_TICKS, List.of());
    var b = second.update(7 + AreaOfInterest.RELEASE_TICKS, List.of());
    assertEquals(a, b);
    assertEquals(a.stream().sorted().toList(), a);
    assertEquals(union.size(), a.size());
    assertEquals(0, first.size());
  }

  @Test void aZeroDelayStillKeepsTheCellsTouchedNowAndRejectsNegativeDelays() {
    var coverage = new AreaOfInterest.Coverage(0);
    var window = new AreaOfInterest.Window(0, 0, 4, 4);
    assertEquals(List.of(), coverage.update(5, List.of(window)));
    assertEquals(1, coverage.size());
    assertEquals(List.of(AreaOfInterest.cellKey(0, 0)), coverage.update(6, List.of()));
    assertThrows(IllegalArgumentException.class, () -> new AreaOfInterest.Coverage(-1));
  }
}
