package com.perso.T4C.world;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.PLAYER_SPEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Headless tests of the shared movement step on synthetic maps. The real-map equivalence with the
 * old single-player code lives in {@code PlayerMovementGoldenTest}; the server-rule equivalence
 * is below, against a literal copy of the server's loop.
 */
class MovementRulesTest {
  private static final int TICK_RATE = 34;

  /** Blocks listed tiles (x,y) for movement; everything else is open. */
  private static MovementRules.Terrain terrain(int... blockedTiles) {
    Set<Long> blocked = new HashSet<>();
    for (int i = 0; i < blockedTiles.length; i += 2) {
      blocked.add(key(blockedTiles[i], blockedTiles[i + 1]));
    }
    return new MovementRules.Terrain() {
      @Override
      public boolean hasCollision(float wx, float wy) {
        return blocked.contains(key((int) (wx / GRID_W), (int) (wy / GRID_H)));
      }

      @Override
      public boolean hasPlayerCollision(float wx, float wy) {
        return hasCollision(wx, wy);
      }

      @Override
      public boolean canMoveForPlayer(float fx, float fy, float tx, float ty) {
        return !hasPlayerCollision(tx, ty);
      }

      @Override
      public boolean isPlayerPassableTile(int gx, int gy) {
        return false;
      }
    };
  }

  private static long key(int x, int y) {
    return ((long) x << 32) ^ (y & 0xffffffffL);
  }

  @Test
  void anOpenStepReservesTheNextTileCornerInTheInputDirection() {
    var terrain = terrain();
    var east = MovementRules.Grid.reserve(terrain, 5 * GRID_W, 5 * GRID_H, 1, 0, true, 9999, 9999);
    assertEquals(new MovementRules.Reservation(6 * GRID_W, 5 * GRID_H, 1, 0), east);
    var northWest =
        MovementRules.Grid.reserve(terrain, 5 * GRID_W, 5 * GRID_H, -1, -1, true, 9999, 9999);
    assertEquals(new MovementRules.Reservation(4 * GRID_W, 4 * GRID_H, -1, -1), northWest);
  }

  @Test
  void aMidTileStartReservesTheNextAnchorNotAFullTileFurther() {
    var terrain = terrain();
    var step =
        MovementRules.Grid.reserve(terrain, 5 * GRID_W + 10, 5 * GRID_H, 1, 0, true, 9999, 9999);
    assertEquals(6 * GRID_W, step.targetX());
    var back =
        MovementRules.Grid.reserve(terrain, 5 * GRID_W + 10, 5 * GRID_H, -1, 0, true, 9999, 9999);
    assertEquals(6 * GRID_W - GRID_W, back.targetX());
  }

  @Test
  void aBlockedStepSlidesToTheNeighbouringDirectionsInTheOriginalOrder() {
    // North is blocked at the destination; the first alternative tried is -1 (north-west).
    var terrain = terrain(5, 4);
    var step = MovementRules.Grid.reserve(terrain, 5 * GRID_W, 5 * GRID_H, 0, -1, true, 9999, 9999);
    assertNotNull(step);
    assertEquals(-1, step.moveX());
    assertEquals(-1, step.moveY());
    // With north-west also blocked the +1 side (north-east) is next.
    var both = terrain(5, 4, 4, 4);
    var step2 = MovementRules.Grid.reserve(both, 5 * GRID_W, 5 * GRID_H, 0, -1, true, 9999, 9999);
    assertEquals(1, step2.moveX());
    assertEquals(-1, step2.moveY());
  }

  @Test
  void collisionsOffIgnoresTerrainAndDoesNotSlideButStaysInsideTheMap() {
    var wall = terrain(6, 5);
    var step = MovementRules.Grid.reserve(wall, 5 * GRID_W, 5 * GRID_H, 1, 0, false, 9999, 9999);
    assertEquals(6 * GRID_W, step.targetX());
    assertNull(MovementRules.Grid.reserve(wall, 0, 0, -1, 0, false, 9999, 9999), "off the map");
    var slide = MovementRules.Grid.reserve(wall, 0, 0, -1, 0, true, 9999, 9999);
    assertEquals(1, slide.moveY(), "with collisions on it slides south along the map edge");
  }

  @Test
  void fullyEnclosedReturnsNoReservation() {
    var terrain = terrain(4, 4, 5, 4, 6, 4, 4, 5, 6, 5, 4, 6, 5, 6, 6, 6);
    assertNull(MovementRules.Grid.reserve(terrain, 5 * GRID_W, 5 * GRID_H, 1, 0, true, 9999, 9999));
  }

  @Test
  void advanceSpendsAtMostTheBudgetAndSnapsOnArrival() {
    var step = new MovementRules.Reservation(6 * GRID_W, 5 * GRID_H, 1, 0);
    var part = MovementRules.Grid.advance(5 * GRID_W, 5 * GRID_H, step, 10f);
    assertEquals(5 * GRID_W + 10f, part.x());
    assertFalse(part.arrived());
    assertEquals(10f, part.spent());
    var rest = MovementRules.Grid.advance(part.x(), part.y(), step, 100f);
    assertTrue(rest.arrived());
    assertEquals(6 * GRID_W, rest.x());
    assertEquals(GRID_W - 10f, rest.spent());
  }

  @Test
  void advanceDoesNotSnapToTheTargetBeforeItsRemainingDistanceIsPaid() {
    var step = new MovementRules.Reservation(6 * GRID_W, 5 * GRID_H, 1, 0);
    var part = MovementRules.Grid.advance(6 * GRID_W - 0.001f, 5 * GRID_H, step, 0.0005f);
    assertFalse(part.arrived());
    assertTrue(part.x() < step.targetX());
    assertEquals(0.0005f, part.spent());
  }

  @Test
  void diagonalAdvanceDoesNotEnterOnlyOneCenteredTileAtABoundary() {
    float x = (8.926465f - 0.5f) * GRID_W;
    float y = (17.9264698f - 0.5f) * GRID_H;
    var step = new MovementRules.Reservation(9 * GRID_W, 18 * GRID_H, 1, 1);
    var moved = MovementRules.Grid.advance(x, y, step, 80f / 34f);
    assertEquals((int) (x / GRID_W + 0.5f), (int) (moved.x() / GRID_W + 0.5f));
    assertEquals((int) (y / GRID_H + 0.5f), (int) (moved.y() / GRID_H + 0.5f));
  }

  @Test
  void negativeDiagonalAdvanceDoesNotEnterOnlyOneCenteredTileAtABoundary() {
    float x = (9.073535f - 0.5f) * GRID_W;
    float y = (18.073520f - 0.5f) * GRID_H;
    var step = new MovementRules.Reservation(8 * GRID_W, 17 * GRID_H, -1, -1);
    var moved = MovementRules.Grid.advance(x, y, step, 80f / 34f);
    assertEquals((int) (x / GRID_W + 0.5f), (int) (moved.x() / GRID_W + 0.5f));
    assertEquals((int) (y / GRID_H + 0.5f), (int) (moved.y() / GRID_H + 0.5f));
  }

  @Test
  void aVerticalStepCoversHalfThePixelsBecauseTilesAreTwiceAsWideAsTall() {
    var step = new MovementRules.Reservation(5 * GRID_W, 6 * GRID_H, 0, 1);
    var moved = MovementRules.Grid.advance(5 * GRID_W, 5 * GRID_H, step, 8f);
    assertEquals(5 * GRID_H + 8f * GRID_H / GRID_W, moved.y());
    assertEquals(8f, moved.spent());
  }

  @Test
  void t4cDirectionsRoundTripAndWrap() {
    for (int d = 1; d <= 8; d++) {
      int[] move = MovementRules.Grid.fromT4cDirection(d);
      assertEquals(d, MovementRules.Grid.toT4cDirection(move[0], move[1]));
    }
  }

  // ---- server rule ------------------------------------------------------------------------

  @Test
  void theServerStepIsTheRecordedTamperProbeMaximum() {
    double perTick = MovementRules.Swept.tilesPerTick(PLAYER_SPEED, TICK_RATE);
    // Derived from the shared player speed, so a speed retune cannot leave a stale pinned value.
    assertEquals(PLAYER_SPEED / GRID_W / TICK_RATE, perTick, 0.00005);
    assertEquals(PLAYER_SPEED / GRID_W / TICK_RATE, perTick, 0.0, "same float expression");
  }

  /** Literal copy of the server's per-tick movement loop (PreviewWorld.step). */
  private static double[] serverLoop(
      double x, double y, int dx, int dy, MovementRules.Swept.Stand map) {
    double distance = PLAYER_SPEED / GRID_W / TICK_RATE;
    int steps = (int) Math.ceil(distance / 0.2);
    double amount = distance / steps;
    for (int i = 0; i < steps; i++) {
      double nx = x + dx * amount, ny = y + dy * amount;
      if (map.canStand(nx, ny) && map.canStand(nx, y) && map.canStand(x, ny)) {
        x = nx;
        y = ny;
      } else break;
    }
    return new double[] {x, y};
  }

  @Test
  void sweptStepMatchesTheServerLoopOnRandomSyntheticMaps() {
    Random random = new Random(41);
    double perTick = MovementRules.Swept.tilesPerTick(PLAYER_SPEED, TICK_RATE);
    for (int map = 0; map < 40; map++) {
      boolean[][] blocked = new boolean[24][24];
      for (int i = 0; i < 90; i++) blocked[random.nextInt(24)][random.nextInt(24)] = true;
      MovementRules.Swept.Stand stand =
          (x, y) ->
              Double.isFinite(x)
                  && Double.isFinite(y)
                  && x >= 0
                  && y >= 0
                  && x < 24
                  && y < 24
                  && !blocked[(int) y][(int) x];
      for (int trial = 0; trial < 250; trial++) {
        double x = 1 + random.nextDouble() * 22;
        double y = 1 + random.nextDouble() * 22;
        if (!stand.canStand(x, y)) continue;
        int dx = random.nextInt(3) - 1;
        int dy = random.nextInt(3) - 1;
        double[] expected = serverLoop(x, y, dx, dy, stand);
        var actual = MovementRules.Swept.step(x, y, dx, dy, perTick, stand);
        assertEquals(expected[0], actual.x(), 0.0);
        assertEquals(expected[1], actual.y(), 0.0);
        double moved = Math.max(Math.abs(actual.x() - x), Math.abs(actual.y() - y));
        assertTrue(moved <= perTick + 1e-12, "never farther than one tick's distance");
      }
    }
  }

  // ---- the two rules side by side: where they agree and where they do not ------------------

  @Test
  void inOpenGroundBothRulesCoverTheSameDistancePerSecond() {
    // Each rule consumes the same configured PLAYER_SPEED movement budget.
    var terrain = terrain();
    var step = MovementRules.Grid.reserve(terrain, 0, 100 * GRID_H, 1, 0, true, 99999, 99999);
    float x = 0;
    float budget = PLAYER_SPEED;
    var res = step;
    while (budget > 0.001f) {
      var adv = MovementRules.Grid.advance(x, 100 * GRID_H, res, budget);
      x = adv.x();
      budget -= adv.spent();
      if (adv.arrived()) {
        res = MovementRules.Grid.reserve(terrain, x, 100 * GRID_H, 1, 0, true, 99999, 99999);
      }
    }
    double gridTiles = x / GRID_W;
    double sweptTiles = 0;
    double perTick = MovementRules.Swept.tilesPerTick(PLAYER_SPEED, TICK_RATE);
    var pos = new MovementRules.Swept.Position(0, 0);
    for (int tick = 0; tick < TICK_RATE; tick++) {
      pos = MovementRules.Swept.step(pos.x(), pos.y(), 1, 0, perTick, (a, b) -> true);
    }
    sweptTiles = pos.x();
    double tilesPerSecond = PLAYER_SPEED / GRID_W;
    assertEquals(tilesPerSecond, gridTiles, 0.01);
    assertEquals(tilesPerSecond, sweptTiles, 0.01);
  }

  @Test
  void againstAWallTheGridRuleSlidesAndTheSweptRuleStops() {
    // Wall tile (6,5) east of the player. Grid reserves a neighbouring direction; swept stays put.
    var grid = MovementRules.Grid.reserve(terrain(6, 5), 5 * GRID_W, 5 * GRID_H, 1, 0, true, 9999, 9999);
    assertNotNull(grid);
    assertTrue(grid.moveY() != 0, "slides to a diagonal");
    var swept =
        MovementRules.Swept.step(
            5.99, 5.5, 1, 0, 0.2757, (x, y) -> !((int) x == 6 && (int) y == 5));
    assertEquals(5.99, swept.x(), 1e-9);
  }

  @Test
  void whenInputStopsMidTileTheGridRuleFinishesTheTileAndTheSweptRuleStopsAtOnce() {
    // Grid: the reservation persists without input (PlayerMovement keeps advancing it); the
    // reservation object itself carries the whole tile. Swept has no memory of a target.
    var step = MovementRules.Grid.reserve(terrain(), 5 * GRID_W, 5 * GRID_H, 1, 0, true, 9999, 9999);
    var oneFrame = MovementRules.Grid.advance(5 * GRID_W, 5 * GRID_H, step, 3f);
    assertFalse(oneFrame.arrived());
    var swept = MovementRules.Swept.step(5.5, 5.5, 0, 0, 0.2757, (x, y) -> true);
    assertEquals(5.5, swept.x());
    assertEquals(5.5, swept.y());
  }
}
