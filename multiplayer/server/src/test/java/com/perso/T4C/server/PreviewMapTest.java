package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.BinaryIOUtils;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PreviewMapTest {
  @TempDir Path temporary;

  @Test void realStartingAreaLoadsWithoutDesktopRuntime() throws Exception {
    var map = PreviewMap.load(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    assertTrue(map.canStand(map.spawnX, map.spawnY));
    boolean blocked = false;
    for (var row : map.cells()) for (boolean value : row) blocked |= value;
    assertTrue(blocked, "The real town must contain collision obstacles");
    assertFalse(map.canStand(-0.1, map.spawnY));
    assertFalse(map.canStand(Double.NaN, map.spawnY));
    assertFalse(map.canStand(map.width, map.spawnY));
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
