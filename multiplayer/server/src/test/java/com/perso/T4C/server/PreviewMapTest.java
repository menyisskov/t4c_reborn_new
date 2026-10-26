package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.world.CollisionRules;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PreviewMapTest {
  @TempDir Path temporary;

  @Test void sightUsesTerrainInsteadOfMovementClearanceAndRejectsCornerShots() {
    var terrain = new byte[6][6];
    terrain[2][2] = 9; // Force fields are walkable, but block sight.
    var map = new PreviewMap(new boolean[6][6], terrain, 100, 200, 1, 1);
    terrain[2][2] = 0; // The map owns an immutable copy.
    assertTrue(map.canStand(2.5, 2.5));
    assertFalse(map.hasLineOfSight(1.5, 2.5, 4.5, 2.5));
    assertFalse(map.hasLineOfSight(2.5, 2.5, 2.5, 2.5));
    assertFalse(map.hasLineOfSight(1.5, 2.5, 2.5, 1.5), "Corner grazing cannot bypass walls");
    assertFalse(map.hasLineOfSight(1.5, 2, 4.5, 2), "Grid-edge rays touch both rows");
    assertFalse(map.hasLineOfSight(2, 2, 1.5, 1.5), "A boundary endpoint touches the wall");
    assertFalse(map.hasLineOfSight(1.5, 1.5, 2, 2));
    assertTrue(map.hasLineOfSight(1.5, 1.5, 4.5, 1.5));
    assertTrue(map.hasLineOfSight(4.5, 1.5, 1.5, 1.5));
    assertFalse(map.hasLineOfSight(Double.NaN, 1, 2, 2));
    assertFalse(map.hasLineOfSight(1, 1, map.width, 2));
  }

  @Test void sanctuariesProtectBothParticipantsWithoutTurningPvpProtectionIntoPveImmunity() {
    var terrain = new byte[6][6];
    terrain[1][1] = 6;
    terrain[1][2] = 7;
    terrain[3][3] = 16;
    var map = new PreviewMap(new boolean[6][6], terrain, 100, 200, 1, 1);
    assertFalse(map.allowsCombat(1.5, 1.5, 4.5, 4.5, false));
    assertFalse(map.allowsCombat(4.5, 4.5, 2.5, 1.5, false));
    assertFalse(map.allowsCombat(3.5, 3.5, 4.5, 4.5, true));
    assertFalse(map.allowsCombat(4.5, 4.5, 3.5, 3.5, true));
    assertTrue(map.allowsCombat(3.5, 3.5, 4.5, 4.5, false));
    assertFalse(map.allowsCombat(-0.1, 1, 4.5, 4.5, false));
  }

  @Test void realStartingAreaLoadsWithoutDesktopRuntime() throws Exception {
    var map = PreviewMap.load(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    assertTrue(map.canStand(map.spawnX, map.spawnY));
    boolean blocked = false;
    for (var row : map.cells()) for (boolean value : row) blocked |= value;
    assertTrue(blocked, "The real town must contain collision obstacles");
    assertFalse(map.canStand(-0.1, map.spawnY));
    assertFalse(map.canStand(Double.NaN, map.spawnY));
    assertFalse(map.canStand(map.width, map.spawnY));
    // Validate the loaded crop against the original terrain, not constructor-only fixtures.
    try (var in = new DataInputStream(BinaryIOUtils.openInputStream(
        Path.of("../../assets/maps/worldmap/worldmap.colbin").toFile(), 1 << 20))) {
      int width = BinaryIOUtils.readIntLE(in);
      int height = BinaryIOUtils.readIntLE(in);
      byte[] terrain = in.readNBytes(width * height);
      boolean sanctuaryFound = false;
      for (int y = 0; y < map.height && !sanctuaryFound; y++) {
        for (int x = 0; x < map.width && !sanctuaryFound; x++) {
          int code = terrain[(map.originY + y) * width + map.originX + x] & 255;
          if (CollisionRules.isPvpSafeHaven(code)) {
            assertTrue(map.hasLineOfSight(x + .5, y + .5, x + .5, y + .5));
            assertFalse(map.allowsCombat(x + .5, y + .5, x + .5, y + .5, true));
            sanctuaryFound = true;
          }
        }
      }
      assertTrue(sanctuaryFound, "The starting temple's protection must survive loading");
    }
  }

  @Test void truncatedAndOversizedFilesFailBeforeWorldStartup() throws Exception {
    var file = temporary.resolve("broken.colbin").toFile();
    try (var out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1024))) {
      BinaryIOUtils.writeIntLE(out, 16); BinaryIOUtils.writeIntLE(out, 16);
      out.write(0);
    }
    assertThrows(IOException.class, () -> PreviewMap.load(file.toPath()));
    try (var out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1024))) {
      BinaryIOUtils.writeIntLE(out, Integer.MAX_VALUE); BinaryIOUtils.writeIntLE(out, Integer.MAX_VALUE);
    }
    assertThrows(IOException.class, () -> PreviewMap.load(file.toPath()));
  }
}
