package com.perso.T4C.world;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class WorldCollisionTest {
  @Test
  void separateWorldsKeepTheirOwnTerrainAndSightRules() {
    var wall = new WorldCollision(8, 8, (x, y) -> x == 3, (x, y) -> x == 3);
    var water = new WorldCollision(8, 8, (x, y) -> y == 4, (x, y) -> false);
    assertTrue(wall.blocksMovement(3, 2));
    assertFalse(water.blocksMovement(3, 2));
    assertTrue(wall.blocksLineOfSight(3, 2));
    assertFalse(water.blocksLineOfSight(3, 2));
    assertTrue(water.blocksMovement(2, 4));
    assertFalse(wall.blocksMovement(2, 4));
    assertFalse(water.blocksLineOfSight(2, 4));
  }

  @Test
  void continuousWallFootAllowsMovementButStillObscuresSight() {
    var world =
        new WorldCollision(
            8, 8, (x, y) -> y == 3 || y == 4, (x, y) -> y == 3 || y == 4);
    assertFalse(world.blocksMovement(3, 3));
    assertTrue(world.blocksMovement(3, 4));
    assertTrue(world.blocksLineOfSight(3, 3));
  }

  @Test
  void outOfBoundsProbesDoNotReadTheMap() {
    var world =
        new WorldCollision(
            8,
            8,
            (x, y) -> {
              assertTrue(x >= 0 && x < 8 && y >= 0 && y < 8);
              return false;
            },
            (x, y) -> fail("Out-of-bounds sight must not read the source"));
    assertFalse(world.contains(-1, 3));
    assertTrue(world.blocksMovement(-1, 3));
    assertTrue(world.blocksLineOfSight(8, 3));
  }
}
