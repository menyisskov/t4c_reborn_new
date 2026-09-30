package com.perso.T4C.tools;

import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionType;
import com.perso.T4C.helper.OriginalZoneMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Narrows Lighthaven's imported town-wide safety to PvP protection within its temple. */
public final class LighthavenSanctuaryBuilder {
  static final int[][] TEMPLE_POLYGON = {
    {2950, 1032}, {2951, 1033}, {2952, 1032}, {2958, 1038}, {2964, 1032}, {2972, 1040},
    {2966, 1046}, {2972, 1052}, {2971, 1053}, {2972, 1054}, {2947, 1079}, {2946, 1078},
    {2945, 1079}, {2925, 1059}, {2926, 1058}, {2925, 1057}
  };

  private LighthavenSanctuaryBuilder() {}

  public static void main(String[] args) throws Exception {
    Path path = Path.of(MapDefinition.WORLDMAP.getCollisionPath());
    CollisionMapIO.CollisionMap map = CollisionMapIO.read(path.toFile());
    byte[] collision = map.getData();
    int width = map.getWidth();
    int height = map.getHeight();
    int formerTownSanctuaryTiles = 0;
    for (int y = 0; y < Math.min(height, 3072); y++) {
      for (int x = 0; x < Math.min(width, 3072); x++) {
        int index = y * width + x;
        int value = collision[index] & 255;
        if ((value != CollisionType.SAFE_HAVEN.getValue()
                && value != CollisionType.INDOOR_SAFE_HAVEN.getValue())
            || OriginalZoneMap.zoneId(0, x, y) != 0) continue;
        collision[index] =
            (byte)
                (value == CollisionType.INDOOR_SAFE_HAVEN.getValue()
                    ? CollisionType.BUILDING.getValue()
                    : CollisionType.NONE.getValue());
        formerTownSanctuaryTiles++;
      }
    }
    int templeTiles = 0;
    for (int y = 1032; y <= 1079; y++) {
      for (int x = 2925; x <= 2972; x++) {
        int index = y * width + x;
        int value = collision[index] & 255;
        if (!AvalonSanctuaryBuilder.insidePolygon(TEMPLE_POLYGON, x, y)
            || CollisionType.fromValue(value).isBlocksMovement()) continue;
        collision[index] = (byte) CollisionType.PVP_SANCTUARY.getValue();
        templeTiles++;
      }
    }
    Path output = Files.createTempFile(path.getParent(), "lighthaven-sanctuary-", ".colbin");
    try {
      CollisionMapIO.write(output.toFile(), width, height, collision);
      Files.move(output, path, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(output);
    }
    System.out.printf(
        "Converted %d Lighthaven town sanctuary tiles; marked %d temple PvP sanctuary tiles.%n",
        formerTownSanctuaryTiles, templeTiles);
  }
}
