package com.perso.T4C.world;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LineOfSightRulesTest {
  private static final int W = 32, H = 16;

  /** A wall on the tile column x = 5; everything else is open. */
  private static boolean wall(float px, float py) {
    return (int) (px / W) == 5;
  }

  @Test
  void aWallBetweenTwoPointsBlocksAndOpenGroundDoesNot() {
    assertFalse(LineOfSightRules.sampled(2.5f * W, 3.5f * H, 8.5f * W, 3.5f * H, W, H, LineOfSightRulesTest::wall));
    assertTrue(LineOfSightRules.sampled(6.5f * W, 3.5f * H, 12.5f * W, 3.5f * H, W, H, LineOfSightRulesTest::wall));
  }

  @Test
  void endpointsAreNeverTestedSoAnAdjacentWallDoesNotBlock() {
    // A segment short enough to have no interior sample is visible even if it ends inside the wall.
    assertTrue(LineOfSightRules.sampled(4.9f * W, 3.5f * H, 5.1f * W, 3.5f * H, W, H, LineOfSightRulesTest::wall));
    // Longer, the interior samples reach the wall and block.
    assertFalse(LineOfSightRules.sampled(4.9f * W, 3.5f * H, 5.5f * W, 3.5f * H, W, H, LineOfSightRulesTest::wall));
  }

  @Test
  void coincidentPointsAreVisibleWithoutAskingTheBlocker() {
    assertTrue(LineOfSightRules.sampled(100f, 100f, 100f, 100f, W, H, (x, y) -> fail("must not sample")));
    assertTrue(LineOfSightRules.sampled(100f, 100f, 100.0005f, 100f, W, H, (x, y) -> fail("must not sample")));
  }

  @Test
  void samplesEveryHalfOfTheSmallerTileDimension() {
    var samples = new java.util.ArrayList<Float>();
    LineOfSightRules.sampled(0f, 0f, 40f, 0f, W, H, (x, y) -> { samples.add(x); return false; });
    // 40 px at an 8 px step: five equal intervals, four interior samples.
    assertEquals(java.util.List.of(8f, 16f, 24f, 32f), samples);
  }
}
