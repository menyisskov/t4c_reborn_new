package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.MONSTER_SPEED;
import static com.perso.T4C.config.GameConstants.PLAYER_SPEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.world.MovementRules;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * The server steps players with the single-player grid-reservation rule. These tests prove where it
 * equals the old swept loop (open ground, speed, cardinal collision decisions, rest on tile
 * centres) and where it differs on purpose (reservation glide, sliding, diagonal corner rule).
 */
class ServerMovementParityTest {
  private static final double PER_TICK = MovementRules.Swept.tilesPerTick(PLAYER_SPEED, PreviewWorld.TICK_RATE);
  private static final int CARDINAL_TILE_TICKS = (int) Math.ceil(1.0 / PER_TICK) + 2;
  private static final int DIAGONAL_TILE_TICKS = (int) Math.ceil(Math.sqrt(2.0) / PER_TICK) + 2;

  private static PreviewMap open(int w, int h, int sx, int sy) {
    return new PreviewMap(new boolean[h][w], 0, 0, sx, sy);
  }

  /** Builds a map the way production does: blocked folds in the tile above, foot keeps raw tiles. */
  private static PreviewMap production(boolean[][] raw, int sx, int sy) {
    int h = raw.length;
    int w = raw[0].length;
    boolean[][] folded = new boolean[h][w];
    for (int y = 0; y < h; y++)
      for (int x = 0; x < w; x++) folded[y][x] = raw[y][x] || (y == 0 ? true : raw[y - 1][x]);
    return new PreviewMap(folded, null, raw, null, 0, 0, sx, sy);
  }

  private static PreviewWorld world(PreviewMap map, MovementRules.Mode mode) {
    var world = new PreviewWorld(map);
    world.movementMode(mode);
    return world;
  }

  private static PreviewWorld.PlayerView view(PreviewWorld world, PreviewWorld.Joined joined) {
    return world.position(joined.token());
  }

  private static long sequence;

  private static void input(PreviewWorld world, PreviewWorld.Joined joined, int dx, int dy) {
    world.input(joined.token(), sequence++, dx, dy);
  }

  @Test
  void theServerFollowsSinglePlayersRuleByDefault() {
    assertEquals(MovementRules.Mode.GRID_RESERVATION, ServerMovement.defaultMode());
    assertEquals(MovementRules.Mode.GRID_RESERVATION, world(open(8, 8, 3, 3), MovementRules.Mode.GRID_RESERVATION).movementMode());
    assertEquals(MovementRules.Mode.GRID_RESERVATION, new PreviewWorld(open(8, 8, 3, 3)).movementMode());
  }

  @Test
  void aTickCoversTheSameDistanceAsTheTamperProbeMeasures() {
    assertEquals(75f, PLAYER_SPEED);
    assertEquals(5f / 3f, PLAYER_SPEED / MONSTER_SPEED, 0.001f);
    assertEquals(PLAYER_SPEED / GRID_W / PreviewWorld.TICK_RATE, PER_TICK, 1e-9);
    assertEquals(PER_TICK, ServerMovement.BUDGET_PER_TICK / (double) GRID_W, 1e-6);
  }

  @Test
  void holdingAnyDirectionOnOpenGroundMovesExactlyLikeTheOldLoop() {
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        if (dx == 0 && dy == 0) continue;
        var grid = world(open(80, 60, 40, 30), MovementRules.Mode.GRID_RESERVATION);
        var swept = world(open(80, 60, 40, 30), MovementRules.Mode.SWEPT_SUBSTEP);
        var g = grid.join();
        var s = swept.join();
        for (int tick = 1; tick <= 60; tick++) {
          input(grid, g, dx, dy);
          input(swept, s, dx, dy);
          grid.step();
          swept.step();
          assertEquals(view(swept, s).x(), view(grid, g).x(), 2e-4, "x dir " + dx + "," + dy + " tick " + tick);
          assertEquals(view(swept, s).y(), view(grid, g).y(), 2e-4, "y dir " + dx + "," + dy + " tick " + tick);
        }
      }
    }
  }

  @Test
  void noTickEverMovesAnAxisFurtherThanTheOldMaximum() {
    var world = world(open(80, 60, 40, 30), MovementRules.Mode.GRID_RESERVATION);
    var joined = world.join();
    var random = new Random(11);
    double lastX = view(world, joined).x();
    double lastY = view(world, joined).y();
    for (int tick = 0; tick < 4000; tick++) {
      if (tick % 7 == 0) input(world, joined, random.nextInt(3) - 1, random.nextInt(3) - 1);
      world.step();
      var now = view(world, joined);
      assertTrue(Math.abs(now.x() - lastX) <= PER_TICK + 1e-5, "x step at tick " + tick);
      assertTrue(Math.abs(now.y() - lastY) <= PER_TICK + 1e-5, "y step at tick " + tick);
      lastX = now.x();
      lastY = now.y();
    }
  }

  @Test
  void aReleasedKeyFinishesTheTileInsteadOfStoppingMidTileAndRestsOnACentre() {
    var grid = world(open(8, 8, 3, 3), MovementRules.Mode.GRID_RESERVATION);
    var swept = world(open(8, 8, 3, 3), MovementRules.Mode.SWEPT_SUBSTEP);
    var g = grid.join();
    var s = swept.join();
    input(grid, g, 1, 0);
    input(swept, s, 1, 0);
    grid.step();
    swept.step();
    double afterFirst = view(grid, g).x();
    double sweptFirst = view(swept, s).x();
    assertEquals(sweptFirst, afterFirst, 1e-4);
    input(grid, g, 0, 0);
    input(swept, s, 0, 0);
    grid.step();
    swept.step();
    assertEquals(sweptFirst, view(swept, s).x(), 1e-9, "the old loop stops at once");
    assertTrue(view(grid, g).x() > afterFirst, "released input still advances the reserved tile");
    for (int i = 0; i < CARDINAL_TILE_TICKS; i++) grid.step();
    assertEquals(4.5, view(grid, g).x(), 1e-9);
    assertEquals(3.5, view(grid, g).y(), 1e-9);
  }

  @Test
  void aReservationCannotBeReversedMidTile() {
    var world = world(open(8, 8, 3, 3), MovementRules.Mode.GRID_RESERVATION);
    var joined = world.join();
    input(world, joined, 1, 0);
    world.step();
    double previous = view(world, joined).x();
    input(world, joined, -1, 0);
    for (int tick = 0; tick < 3; tick++) {
      world.step();
      double now = view(world, joined).x();
      assertTrue(now > previous, "still gliding east to the reserved tile, tick " + tick);
      previous = now;
    }
    for (int tick = 0; tick < CARDINAL_TILE_TICKS + 5; tick++) {
      if (tick % 6 == 0) input(world, joined, -1, 0);
      world.step();
    }
    assertTrue(view(world, joined).x() < 4.5, "after arriving it follows the held west input");
  }

  @Test
  void aBlockedTileSlidesInTheOriginalOrderMinusOnePlusOneMinusTwoPlusTwo() {
    // Heading south-east (direction 4) from tile (3,3) with the destination (4,4) walled.
    // Slide order around it is -1 east, +1 south, -2 north-east, +2 south-west.
    int[][] walls = {{4, 4}, {4, 3}, {3, 4}, {4, 2}};
    int[][] expectedTile = {{4, 3}, {3, 4}, {4, 2}, {2, 4}};
    for (int blockedCount = 1; blockedCount <= 4; blockedCount++) {
      boolean[][] raw = new boolean[8][8];
      for (int i = 0; i < blockedCount; i++) raw[walls[i][1]][walls[i][0]] = true;
      var world = world(production(raw, 3, 3), MovementRules.Mode.GRID_RESERVATION);
      var joined = world.join();
      input(world, joined, 1, 1);
      world.step();
      input(world, joined, 0, 0);
      for (int i = 0; i < DIAGONAL_TILE_TICKS; i++) world.step();
      var now = view(world, joined);
      int[] want = expectedTile[blockedCount - 1];
      assertEquals(want[0] + 0.5, now.x(), 1e-9, "x with " + blockedCount + " walls");
      assertEquals(want[1] + 0.5, now.y(), 1e-9, "y with " + blockedCount + " walls");
    }
  }

  @Test
  void aFullyEnclosedPlayerDoesNotMove() {
    boolean[][] ring = new boolean[8][8];
    for (int dy = -1; dy <= 1; dy++)
      for (int dx = -1; dx <= 1; dx++) if (dx != 0 || dy != 0) ring[3 + dy][3 + dx] = true;
    var grid = world(new PreviewMap(ring, 0, 0, 3, 3), MovementRules.Mode.GRID_RESERVATION);
    var g = grid.join();
    for (int i = 0; i < 20; i++) {
      input(grid, g, 1, 1);
      grid.step();
    }
    assertEquals(3.5, view(grid, g).x(), 1e-9);
    assertEquals(3.5, view(grid, g).y(), 1e-9);
  }

  @Test
  void theDiagonalCornerRuleIsSinglePlayersNotTheOldBothAxisCheck() {
    // The tile south of the start is a wall. The old loop refuses the diagonal south-east step
    // (it also needs the south tile standable); single-player's footprint rule allows it.
    boolean[][] raw = new boolean[8][8];
    raw[4][3] = true;
    var grid = world(production(raw, 3, 3), MovementRules.Mode.GRID_RESERVATION);
    var swept = world(production(raw, 3, 3), MovementRules.Mode.SWEPT_SUBSTEP);
    var g = grid.join();
    var s = swept.join();
    for (int i = 0; i < DIAGONAL_TILE_TICKS; i++) {
      input(grid, g, 1, 1);
      input(swept, s, 1, 1);
      grid.step();
      swept.step();
    }
    assertTrue(view(swept, s).x() < 4 && view(swept, s).y() < 4, "old loop never cuts that corner");
    assertTrue(view(grid, g).x() > 4.5 && view(grid, g).y() > 4.5, "single-player's rule walks the diagonal");
  }

  @Test
  void aDiagonalIntoAWallAboveTheDestinationIsRefusedByBothRulesButTheGridSlides() {
    boolean[][] raw = new boolean[8][8];
    raw[3][4] = true; // east of the start: the tile above the diagonal destination (4,4)
    var swept = world(production(raw, 3, 3), MovementRules.Mode.SWEPT_SUBSTEP);
    var grid = world(production(raw, 3, 3), MovementRules.Mode.GRID_RESERVATION);
    var s = swept.join();
    var g = grid.join();
    for (int i = 0; i < 20; i++) {
      input(swept, s, 1, 1);
      input(grid, g, 1, 1);
      swept.step();
      grid.step();
      assertTrue(view(swept, s).x() < 4 && view(swept, s).y() < 4, "old loop never crosses the corner");
    }
    assertTrue(view(grid, g).y() > 4, "the grid rule slides south along the wall instead of stopping");
  }

  @Test
  void collisionDecisionsForCardinalStepsEqualTheOldLoopOnARandomProductionStyleMap() {
    var random = new Random(3);
    boolean[][] raw = new boolean[40][60];
    for (int y = 0; y < raw.length; y++)
      for (int x = 0; x < raw[0].length; x++) raw[y][x] = random.nextInt(100) < 25;
    raw[20][30] = false;
    raw[19][30] = false;
    var map = production(raw, 30, 20);
    var terrain = new ServerMovement.Terrain(map);
    int compared = 0;
    for (int ty = 1; ty < raw.length - 1; ty++) {
      for (int tx = 1; tx < raw[0].length - 1; tx++) {
        if (!map.canStand(tx + 0.5, ty + 0.5)) continue;
        for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
          var reservation = MovementRules.Grid.reserve(
              terrain, tx * (float) GRID_W, ty * (float) GRID_H, d[0], d[1], true,
              map.width * (float) GRID_W, map.height * (float) GRID_H);
          boolean gridEnters = reservation != null && reservation.moveX() == d[0] && reservation.moveY() == d[1];
          boolean oldEnters = map.canStand(tx + d[0] + 0.5, ty + d[1] + 0.5);
          assertEquals(oldEnters, gridEnters, "tile " + tx + "," + ty + " dir " + d[0] + "," + d[1]);
          compared++;
        }
      }
    }
    assertTrue(compared > 1000);
  }

  @Test
  void everyDiagonalTheOldLoopAllowsIsAllowedAndEveryExtraOneIsADocumentedCornerOrWallCase() {
    var random = new Random(8);
    boolean[][] raw = new boolean[40][60];
    for (int y = 0; y < raw.length; y++)
      for (int x = 0; x < raw[0].length; x++) raw[y][x] = random.nextInt(100) < 25;
    raw[1][1] = false;
    raw[0][1] = false;
    var map = production(raw, 1, 1);
    var terrain = new ServerMovement.Terrain(map);
    int extra = 0;
    for (int ty = 2; ty < raw.length - 1; ty++) {
      for (int tx = 1; tx < raw[0].length - 1; tx++) {
        if (!map.canStand(tx + 0.5, ty + 0.5)) continue;
        for (int dx = -1; dx <= 1; dx += 2) {
          for (int dy = -1; dy <= 1; dy += 2) {
            var reservation = MovementRules.Grid.reserve(
                terrain, tx * (float) GRID_W, ty * (float) GRID_H, dx, dy, true,
                map.width * (float) GRID_W, map.height * (float) GRID_H);
            boolean gridEnters = reservation != null && reservation.moveX() == dx && reservation.moveY() == dy;
            boolean oldEnters = map.canStand(tx + dx + 0.5, ty + dy + 0.5)
                && map.canStand(tx + dx + 0.5, ty + 0.5) && map.canStand(tx + 0.5, ty + dy + 0.5);
            if (oldEnters) assertTrue(gridEnters, "grid must allow what the old loop allows at " + tx + "," + ty);
            if (gridEnters && !oldEnters) {
              extra++;
              boolean sideBlocked = !map.canStand(tx + dx + 0.5, ty + 0.5) || !map.canStand(tx + 0.5, ty + dy + 0.5);
              boolean wallAdjacentException = !map.canStand(tx + dx + 0.5, ty + dy + 0.5);
              assertTrue(sideBlocked || wallAdjacentException, "undocumented difference at " + tx + "," + ty);
            }
          }
        }
      }
    }
    assertTrue(extra > 0, "the rules differ on dense maps, as documented");
  }

  @Test
  void aWalkingPlayerNeverStandsOnABlockedTileUnderRandomInput() {
    var random = new Random(21);
    for (int map = 0; map < 5; map++) {
      boolean[][] raw = new boolean[30][40];
      for (int y = 0; y < raw.length; y++)
        for (int x = 0; x < raw[0].length; x++) raw[y][x] = random.nextInt(100) < 18;
      raw[15][20] = false;
      raw[14][20] = false;
      var preview = production(raw, 20, 15);
      var world = world(preview, MovementRules.Mode.GRID_RESERVATION);
      var joined = world.join();
      for (int tick = 0; tick < 3000; tick++) {
        if (tick % 5 == 0) input(world, joined, random.nextInt(3) - 1, random.nextInt(3) - 1);
        world.step();
        var now = view(world, joined);
        assertTrue(preview.canStand(now.x(), now.y()), "standable at tick " + tick + " " + now.x() + "," + now.y());
      }
    }
  }

  @Test
  void aSavedMidTilePositionFromTheOldLoopLoadsOntoTheCentreOfItsTile() {
    var world = world(open(30, 30, 3, 3), MovementRules.Mode.GRID_RESERVATION);
    var joined = world.joinAt(12.73, 9.31);
    var at = view(world, joined);
    assertEquals(12.5, at.x(), 1e-9);
    assertEquals(9.5, at.y(), 1e-9);
    var exact = world.joinAt(5.5, 6.5);
    assertEquals(5.5, view(world, exact).x(), 0.0);
    assertEquals(6.5, view(world, exact).y(), 0.0);
    var edge = world.joinAt(7.0, 8.0);
    assertEquals(7.5, view(world, edge).x(), 1e-9);
    assertEquals(8.5, view(world, edge).y(), 1e-9);
    input(world, joined, 1, 0);
    world.step();
    assertTrue(view(world, joined).x() > 12.5);
  }

  @Test
  void aSavedPositionStillNeedsToBeLegalAndTheSweptModeKeepsItExactly() {
    boolean[][] cells = new boolean[8][8];
    cells[2][2] = true;
    var grid = world(new PreviewMap(cells, 0, 0, 5, 5), MovementRules.Mode.GRID_RESERVATION);
    assertEquals(409, assertThrows(PreviewWorld.Rejected.class, () -> grid.joinAt(2.9, 2.1)).status);
    assertEquals(409, assertThrows(PreviewWorld.Rejected.class, () -> grid.joinAt(-0.2, 1.0)).status);
    var swept = world(new PreviewMap(new boolean[8][8], 0, 0, 5, 5), MovementRules.Mode.SWEPT_SUBSTEP);
    assertEquals(3.25, view(swept, swept.joinAt(3.25, 4.75)).x(), 0.0);
  }

  @Test
  void aCheckpointTakenMidGlideReloadsOnALegalCentreAndKeepsWalking() {
    var first = world(open(30, 30, 10, 10), MovementRules.Mode.GRID_RESERVATION);
    var a = first.join();
    input(first, a, 1, 1);
    for (int i = 0; i < 2; i++) first.step();
    var saved = view(first, a);
    assertTrue(saved.x() != Math.floor(saved.x()) + 0.5, "the checkpoint is between tiles");
    var second = world(open(30, 30, 10, 10), MovementRules.Mode.GRID_RESERVATION);
    var b = second.joinAt(saved.x(), saved.y());
    double centreX = view(second, b).x();
    assertEquals(0.5, centreX - Math.floor(centreX), 1e-9);
    assertEquals(Math.floor(saved.x()), Math.floor(centreX), 0.0, "same tile");
    double before = view(second, b).y();
    input(second, b, 0, 1);
    second.step();
    assertTrue(view(second, b).y() > before, "it keeps walking from the snapped centre");
  }

  @Test
  void relocationAndTransferDropAnyGlideInProgress() {
    var world = world(open(30, 30, 3, 3), MovementRules.Mode.GRID_RESERVATION);
    var joined = world.join();
    input(world, joined, 1, 0);
    world.step();
    world.relocate(joined.token(), 20.5, 20.5);
    world.step();
    assertEquals(20.5, view(world, joined).x(), 0.0);
    assertEquals(20.5, view(world, joined).y(), 0.0);
    var other = new PreviewWorld(open(30, 30, 3, 3));
    input(world, joined, 1, 0);
    world.step();
    world.transferTo(other, joined.token(), 10.5, 10.5);
    other.freeze(joined.token(), false);
    other.step();
    assertEquals(10.5, other.position(joined.token()).x(), 0.0);
  }

  @Test
  void aPortalSourceTileIsEnterableEvenThoughItsTerrainBlocks() {
    boolean[][] foot = new boolean[8][8];
    foot[3][4] = true;
    boolean[][] passable = new boolean[8][8];
    passable[3][4] = true;
    boolean[][] folded = new boolean[8][8];
    var map = new PreviewMap(folded, null, foot, passable, 0, 0, 3, 3);
    var world = world(map, MovementRules.Mode.GRID_RESERVATION);
    var joined = world.join();
    input(world, joined, 1, 0);
    world.step();
    input(world, joined, 0, 0);
    for (int i = 0; i < CARDINAL_TILE_TICKS; i++) world.step();
    assertEquals(4.5, view(world, joined).x(), 1e-9);
    assertFalse(new ServerMovement.Terrain(map).hasPlayerCollision(4 * (float) GRID_W, 3 * (float) GRID_H));
    assertTrue(new ServerMovement.Terrain(map).hasCollision(4 * (float) GRID_W, 3 * (float) GRID_H));
  }

  @Test
  void theConversionBetweenWireTilesAndRulePixelsIsExact() {
    for (double tile = 0.5; tile < 80; tile += 0.25) {
      float px = ServerMovement.pixelX(tile);
      assertEquals(tile, ServerMovement.tileX(px), 1e-6);
      float py = ServerMovement.pixelY(tile);
      assertEquals(tile, ServerMovement.tileY(py), 1e-6);
    }
    assertEquals(3 * (float) GRID_W, ServerMovement.pixelX(3.5), 0f);
    assertEquals(5 * (float) GRID_H, ServerMovement.pixelY(5.5), 0f);
  }
}
