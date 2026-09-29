package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionType;
import com.perso.T4C.helper.GroundMosaicCatalog;
import com.perso.T4C.helper.MapReader;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * Rebuilds Avalon's sanctuary and first hunting region from existing game art.
 *
 * <p>The edit mask is the inclusive settlement rectangle (1295,1440)-(1390,1545), union the Wilds
 * circle (1265,1400), radius 110. Everything outside that mask is preserved. Source polygons follow
 * complete Lighthaven buildings, including boundary wall anchors; their doors, NPCs, teleport links
 * and interactive objects are deliberately not imported. Run with {@code --apply} to write the
 * three world-map binaries. Without arguments this builds and validates in memory only. Re-running
 * gives identical tiles and metadata.
 */
public final class AvalonSanctuaryBuilder {
  private AvalonSanctuaryBuilder() {}

  private static final int[][] TEMPLE = {
    {2950, 1032}, {2951, 1033}, {2952, 1032}, {2958, 1038},
    {2964, 1032}, {2972, 1040}, {2966, 1046}, {2972, 1052},
    {2971, 1053}, {2972, 1054}, {2947, 1079}, {2946, 1078},
    {2945, 1079}, {2925, 1059}, {2926, 1058}, {2925, 1057}
  };
  private static final int[][] COTTAGE = {{2939, 1117}, {2947, 1125}, {2938, 1134}, {2930, 1126}};
  private static final int[][] STOREHOUSE = {
    {2928, 1132}, {2935, 1139}, {2927, 1147}, {2920, 1140}
  };
  // Each polyline bends through open country; widths are in tiles, not sprite pixels.
  private static final int[][][] ROADS = {
    {{1334, 1485}, {1324, 1488}, {1302, 1480}, {1285, 1468}, {1265, 1460}},
    {{1265, 1460}, {1250, 1441}, {1241, 1416}, {1254, 1387}, {1250, 1345}},
    {{1241, 1416}, {1224, 1410}, {1205, 1400}},
    {{1254, 1387}, {1275, 1379}, {1300, 1390}},
    {{1324, 1488}, {1314, 1494}, {1304, 1502}},
    {{1324, 1488}, {1336, 1501}, {1350, 1500}, {1364, 1506}}
  };
  private static final int[][] GROVES = {
    {1195, 1357}, {1230, 1319}, {1292, 1320}, {1339, 1360},
    {1350, 1424}, {1212, 1442}, {1178, 1390}, {1297, 1431}
  };
  private static final int[][] OPEN_AREAS = {
    {1250, 1345, 24}, {1300, 1390, 24}, {1205, 1400, 24}, {1265, 1460, 23}
  };

  public static boolean inEditMask(int x, int y) {
    return inTown(x, y) || distanceSquared(x, y, 1265, 1400) <= 110 * 110;
  }

  private static boolean inTown(int x, int y) {
    return x >= 1295 && x <= 1390 && y >= 1440 && y <= 1545;
  }

  public static void main(String[] args) throws Exception {
    if (args.length > 1 || (args.length == 1 && !args[0].equals("--apply"))) {
      throw new IllegalArgumentException("Usage: AvalonSanctuaryBuilder [--apply]");
    }
    boolean apply = args.length == 1;
    File mapFile = new File(Paths.MAP);
    File colFile = new File(Paths.COLLISION_MAP);
    GroundMosaicCatalog catalog = GroundMosaicCatalog.load();
    try (MapReader map = new MapReader(mapFile)) {
      var collision = CollisionMapIO.read(colFile);
      if (map.getWidth() != collision.getWidth() || map.getHeight() != collision.getHeight()) {
        throw new IllegalStateException("Map and collision dimensions differ");
      }
      byte[] data = collision.getData();
      int width = collision.getWidth();
      int changed = 0;
      for (int y = 1290; y <= 1545; y++) {
        for (int x = 1155; x <= 1390; x++) {
          if (!inEditMask(x, y)) continue;
          boolean road = roadDistance(x, y) <= 2.5;
          boolean plaza = distanceSquared(x, y, 1332, 1495) <= 21 * 21;
          boolean boss = distanceSquared(x, y, 1265, 1460) <= 20 * 20;
          String family = road || plaza || boss ? "EarthTile" : "Grass";
          setGround(map, catalog, x, y, family);
          map.setDecorSpriteName(x, y, null);
          map.setScale(x, y, 1, 1);
          map.setOffset(x, y, 0, 0);
          map.setZOrder(x, y, 0);
          data[y * width + x] = (byte) (inTown(x, y) ? 6 : 0);
          changed++;
        }
      }
      // Trees and shrubs from fixed, unmodified donor tiles keep their packed and tile offsets.
      // Full trees use CityTree1/2 (roughly 300px wide), mixed with small understory. Their sparse
      // grove placement leaves broad visible glades and wide sightlines.
      int trees = 0;
      for (int y = 1300; y < 1507; y += 6) {
        for (int x = 1162; x < 1374; x += 6) {
          int hash = Math.floorMod(x * 7349 + y * 1933, 997);
          int tx = x + hash % 3;
          int ty = y + hash / 7 % 3;
          if (!inEditMask(tx, ty) || inTown(tx, ty) || roadDistance(tx, ty) < 8) continue;
          if (Arrays.stream(OPEN_AREAS)
              .anyMatch(p -> distanceSquared(tx, ty, p[0], p[1]) <= p[2] * p[2])) continue;
          if (Arrays.stream(GROVES).noneMatch(p -> distanceSquared(tx, ty, p[0], p[1]) <= 18 * 18))
            continue;
          if (hash % 4 == 0) continue;
          // Large canopies reach about 15 tiles above their anchor; protect the whole crown.
          if (hash % 5 < 2
              && (roadDistance(tx, ty) < 17
                  || Arrays.stream(OPEN_AREAS)
                      .anyMatch(
                          p -> distanceSquared(tx, ty, p[0], p[1]) <= (p[2] + 15) * (p[2] + 15))))
            continue;
          int sx =
              switch (hash % 5) {
                case 0 -> 2937;
                case 1 -> 2948;
                case 2 -> 2933;
                case 3 -> 2938;
                default -> 2941;
              };
          int sy =
              switch (hash % 5) {
                case 0 -> 1112;
                case 1 -> 1022;
                case 2 -> 1116;
                case 3 -> 1036;
                default -> 1114;
              };
          copyScenery(map, sx, sy, tx, ty);
          // Decorative small groves remain traversable; monster spawn circles stay valid.
          trees++;
        }
      }
      // Sparse rocks mark the boss clearing without hiding its spawn or path.
      for (int[] p : new int[][] {{1243, 1455}, {1248, 1479}, {1280, 1479}}) {
        copyScenery(map, 2921, 1052, p[0], p[1]);
      }
      // Low shrubs frame the village lanes; the courtyard and gateways stay visible.
      for (int[] p : new int[][] {{1318, 1524}, {1349, 1527}, {1378, 1494}, {1300, 1474}}) {
        copyScenery(map, 2933, 1116, p[0], p[1]);
      }
      copyBuilding(map, data, width, TEMPLE, -1601, 415);
      copyBuilding(map, data, width, COTTAGE, -1629, 379);
      copyBuilding(map, data, width, STOREHOUSE, -1560, 370);
      // Remove only the two stair anchors; retain every surrounding wall collision.
      for (int[] p : new int[][] {{2931, 1057}, {2964, 1037}}) {
        int x = p[0] - 1601, y = p[1] + 415;
        String decor = map.getDecorSpriteName(x, y);
        if (decor == null || !decor.startsWith("Stair")) {
          throw new IllegalStateException("Expected donor stair at " + Arrays.toString(p));
        }
        map.setDecorSpriteName(x, y, null);
        map.setScale(x, y, 1, 1);
        map.setOffset(x, y, 0, 0);
        map.setZOrder(x, y, 0);
        data[y * width + x] = 7;
      }
      validateConnectivity(data, width, collision.getHeight());
      if (apply) {
        map.writeCompact(mapFile);
        CollisionMapIO.write(colFile, width, collision.getHeight(), data);
      }
      System.out.printf(
          "%s %,d terrain tiles, %d sparse grove decorations and 3 complete buildings; routes verified.%n",
          apply ? "Saved" : "Validated (dry run)", changed, trees);
    }
  }

  private static void setGround(
      MapReader map, GroundMosaicCatalog catalog, int x, int y, String family) {
    String name = catalog.tileName(family, x, y);
    if (name == null) throw new IllegalStateException("Missing terrain family: " + family);
    map.setGroundSpriteName(x, y, name);
  }

  private static void copyScenery(MapReader map, int sx, int sy, int x, int y) {
    if (map.getDecorSpriteName(sx, sy) == null) {
      throw new IllegalStateException("Missing scenery donor at " + sx + "," + sy);
    }
    copyDecor(map, sx, sy, x, y);
  }

  private static void copyDecor(MapReader map, int sx, int sy, int x, int y) {
    map.setDecorSpriteName(x, y, map.getDecorSpriteName(sx, sy));
    map.setScale(x, y, map.getScaleX(sx, sy), map.getScaleY(sx, sy));
    map.setOffset(x, y, map.getOffsetX(sx, sy), map.getOffsetY(sx, sy));
    map.setZOrder(x, y, map.getZOrder(sx, sy));
  }

  private static void copyBuilding(
      MapReader map, byte[] data, int width, int[][] polygon, int dx, int dy) {
    int minX = Arrays.stream(polygon).mapToInt(p -> p[0]).min().orElseThrow();
    int maxX = Arrays.stream(polygon).mapToInt(p -> p[0]).max().orElseThrow();
    int minY = Arrays.stream(polygon).mapToInt(p -> p[1]).min().orElseThrow();
    int maxY = Arrays.stream(polygon).mapToInt(p -> p[1]).max().orElseThrow();
    for (int sy = minY; sy <= maxY; sy++) {
      for (int sx = minX; sx <= maxX; sx++) {
        if (!insidePolygon(polygon, sx, sy)) continue;
        int x = sx + dx, y = sy + dy;
        if (!inEditMask(x, y)) throw new IllegalStateException("Building exceeds edit mask");
        map.setGroundSpriteName(x, y, map.getGroundSpriteName(sx, sy));
        copyDecor(map, sx, sy, x, y);
        int sourceCollision = data[sy * width + sx] & 255;
        data[y * width + x] =
            (byte)
                (CollisionType.fromValue(sourceCollision).isBlocksMovement() ? sourceCollision : 7);
      }
    }
  }

  /** Ray crossing with an explicit boundary test so corner and wall tiles are never dropped. */
  static boolean insidePolygon(int[][] polygon, int x, int y) {
    boolean inside = false;
    for (int i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
      int ax = polygon[j][0], ay = polygon[j][1], bx = polygon[i][0], by = polygon[i][1];
      long cross = (long) (x - ax) * (by - ay) - (long) (y - ay) * (bx - ax);
      if (cross == 0
          && x >= Math.min(ax, bx)
          && x <= Math.max(ax, bx)
          && y >= Math.min(ay, by)
          && y <= Math.max(ay, by)) return true;
      if ((ay > y) != (by > y) && x < (double) (bx - ax) * (y - ay) / (by - ay) + ax)
        inside = !inside;
    }
    return inside;
  }

  private static int distanceSquared(int x, int y, int cx, int cy) {
    return (x - cx) * (x - cx) + (y - cy) * (y - cy);
  }

  private static double roadDistance(int x, int y) {
    double best = Double.POSITIVE_INFINITY;
    for (int[][] road : ROADS) {
      for (int i = 1; i < road.length; i++) {
        int ax = road[i - 1][0], ay = road[i - 1][1];
        int dx = road[i][0] - ax, dy = road[i][1] - ay;
        double t =
            Math.max(
                0, Math.min(1, ((x - ax) * dx + (y - ay) * dy) / (double) (dx * dx + dy * dy)));
        best = Math.min(best, Math.hypot(x - ax - t * dx, y - ay - t * dy));
      }
    }
    return best;
  }

  private static void validateConnectivity(byte[] data, int width, int height) {
    boolean[] visited = new boolean[data.length];
    ArrayDeque<Integer> queue = new ArrayDeque<>();
    int start = 1477 * width + 1340;
    queue.add(start);
    visited[start] = true;
    while (!queue.isEmpty()) {
      int current = queue.removeFirst();
      int x = current % width, y = current / width;
      for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
        int nx = x + d[0], ny = y + d[1];
        if (nx < 0 || nx >= width || ny < 0 || ny >= height || !inEditMask(nx, ny)) continue;
        int next = ny * width + nx;
        if (!visited[next] && !CollisionType.fromValue(data[next] & 255).isBlocksMovement()) {
          visited[next] = true;
          queue.add(next);
        }
      }
    }
    for (int[] p :
        new int[][] {
          {1334, 1485},
          {1344, 1462},
          {1347, 1465},
          {1307, 1505},
          {1368, 1510},
          {1305, 1516},
          {1342, 1503},
          {1361, 1518},
          {1321, 1483},
          {1374, 1522},
          {1353, 1465},
          {1250, 1345},
          {1300, 1390},
          {1205, 1400},
          {1265, 1460},
          {1255, 1455},
          {1275, 1455},
          {1265, 1472}
        }) {
      if (!visited[p[1] * width + p[0]])
        throw new IllegalStateException("Unreachable destination " + Arrays.toString(p));
    }
  }
}
