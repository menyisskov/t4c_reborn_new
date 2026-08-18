package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.helper.MusicZoneBinaryIO;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LegacyMusicZoneImporterTest {
  private static final int WORLD_SIZE = 3072;
  @TempDir Path tempDir;

  @Test
  void binaryRectanglesPreserveEveryOriginalMusicTile() throws Exception {
    for (int world = 0; world < 4; world++) {
      List<MusicZoneBinaryIO.Entry> generated =
          LegacyMusicZoneImporter.buildEntries(world, WORLD_SIZE, WORLD_SIZE);
      File file = tempDir.resolve("world-" + world + ".musiczones.bin").toFile();
      MusicZoneBinaryIO.write(file, generated);
      byte[] decoded = decode(MusicZoneBinaryIO.read(file), WORLD_SIZE, WORLD_SIZE);
      for (int y = 0; y < WORLD_SIZE; y++) {
        for (int x = 0; x < WORLD_SIZE; x++) {
          assertEquals(
              LegacyMusicZoneImporter.resolveMusicId(world, x, y),
              decoded[y * WORLD_SIZE + x],
              "Mismatch at world=" + world + ", x=" + x + ", y=" + y);
        }
      }
    }
  }

  @Test
  void knownClientDefaultsAndOverridesArePreserved() {
    assertEquals(2, LegacyMusicZoneImporter.resolveMusicId(0, 0, 0));
    assertEquals(3, LegacyMusicZoneImporter.resolveMusicId(1, 0, 0));
    assertEquals(4, LegacyMusicZoneImporter.resolveMusicId(2, 0, 0));
    assertEquals(2, LegacyMusicZoneImporter.resolveMusicId(3, 0, 0));
    assertEquals(3, LegacyMusicZoneImporter.resolveMusicId(3, 100, 2000));
    assertEquals(1, LegacyMusicZoneImporter.resolveMusicId(0, 1000, 1000));
  }

  private static byte[] decode(List<MusicZoneBinaryIO.Entry> entries, int width, int height) {
    byte[] result = new byte[width * height];
    Arrays.fill(result, (byte) -1);
    for (MusicZoneBinaryIO.Entry entry : entries) {
      int musicId = musicId(entry.music);
      for (int y = entry.y1; y <= entry.y2; y++) {
        Arrays.fill(result, y * width + entry.x1, y * width + entry.x2 + 1, (byte) musicId);
      }
    }
    return result;
  }

  private static int musicId(String music) {
    for (int i = 0; i < LegacyMusicZoneImporter.MUSIC_NAMES.length; i++) {
      if (music.equals(LegacyMusicZoneImporter.MUSIC_NAMES[i])) {
        return i;
      }
    }
    return -1;
  }
}
