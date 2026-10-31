package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PathfindingAdapterTest {
  @TempDir Path temp;

  @Test
  void actualCollisionManagerRetainsOriginalDetourAndClearance() throws Exception {
    byte[] cells = new byte[25];
    cells[1 + 1 * 5] = 1;
    Path map = temp.resolve("collision.colbin");
    CollisionMapIO.write(map.toFile(), 5, 5, cells);
    CollisionManager manager = CollisionManager.getInstance();
    manager.initialize(new CollisionReader(map.toFile()));
    try {
      assertEquals("1,0 2,0 2,1 2,2 3,3 4,4",
          trace(Pathfinding.findPath(0, 0, 4, 4, 5, 5, manager)));
      assertEquals("", trace(Pathfinding.findPath(0, 0, 4, 4, 5, 5, manager, 1)));
    } finally {
      manager.clear();
    }
  }

  private static String trace(List<Pathfinding.GridPoint> path) {
    StringBuilder result = new StringBuilder();
    for (Pathfinding.GridPoint point : path) {
      if (!result.isEmpty()) result.append(' ');
      result.append(point.x).append(',').append(point.y);
    }
    return result.toString();
  }
}
