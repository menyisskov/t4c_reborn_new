package com.perso.T4C.world;

import static com.perso.T4C.config.GameConstants.NEW_CHARACTER_START_TILE_X;
import static com.perso.T4C.config.GameConstants.NEW_CHARACTER_START_TILE_Y;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.helper.CollisionType;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Server-rule parity facts that need the real world collision map (desktop-only test). */
class MovementRulesRealMapTest {
  private static final String MAP = "assets/maps/worldmap/worldmap.colbin";

  @Test
  void theServerAndDesktopCollisionCodesAgreeForEveryByte() {
    for (int code = 0; code < 256; code++) {
      assertEquals(
          CollisionType.fromValue(code).isBlocksMovement(),
          CollisionRules.blocksMovement(code),
          "movement code " + code);
    }
  }

  @Test
  void standsOnMatchesTheServersPrecomputedFootprintGridOnTheRealMap() throws IOException {
    CollisionReader reader = new CollisionReader(new File(MAP));
    WorldCollision world =
        new WorldCollision(
            reader.getWidth(), reader.getHeight(), reader::hasCollision, reader::blocksLineOfSight);
    // Literal copy of PreviewMap.load's cell construction around the new-character start.
    int ox = NEW_CHARACTER_START_TILE_X - 40;
    int oy = NEW_CHARACTER_START_TILE_Y - 30;
    boolean[][] cells = new boolean[60][80];
    for (int y = 0; y < 60; y++) {
      for (int x = 0; x < 80; x++) {
        for (int above = 0; above <= 1; above++) {
          cells[y][x] |=
              !world.contains(ox + x, oy + y - above) || world.blocksMovement(ox + x, oy + y - above);
        }
      }
    }
    Random random = new Random(5);
    for (int i = 0; i < 20000; i++) {
      double x = random.nextDouble() * 80;
      double y = random.nextDouble() * 60;
      assertEquals(
          !cells[(int) y][(int) x],
          MovementRules.Swept.standsOn(world, ox + x, oy + y),
          "tile " + (int) x + "," + (int) y);
    }
  }
}
