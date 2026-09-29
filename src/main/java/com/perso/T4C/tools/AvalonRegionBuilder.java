package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.editor.build.MapStamp;
import com.perso.T4C.helper.AvalonWorldLayout;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionType;
import com.perso.T4C.helper.GroundMosaicCatalog;
import com.perso.T4C.helper.MapReader;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds two traversable islands and landmarks to the already expanded Avalon world. The two island
 * footprints must be untouched ocean; mainland detailing changes only empty, ordinary walkable
 * ground. All coordinates are world-map tiles and no existing entrance or actor is moved.
 */
public final class AvalonRegionBuilder {
  private AvalonRegionBuilder() {}

  private static final int WIDTH = 5120;
  private static final int HEIGHT = 3072;
  private static final int WATER = CollisionType.DEEP_WATER.getValue();
  private static final int FREE = CollisionType.NONE.getValue();

  public record Site(String name, int x, int y, int radius) {}

  public static final List<Site> SITES =
      List.of(
          new Site("Moonwake Shoals", 3930, 715, 300),
          new Site("Emberglass Crown", 4320, 2600, 370),
          new Site("Crescent Pools", 3610, 1320, 55),
          new Site("Oathstone Grove", 3820, 1430, 55),
          new Site("Sable Fen", 4450, 2050, 58));

  private record Donor(int x, int y) {}

  private static final int[][] NORTH_ROUTE = {
    {3800, 1130}, {3800, 1080}, {3830, 1000}, {3835, 855}, {3900, 740}, {4050, 675}
  };
  private static final int[][] SOUTH_ROUTE = {
    {4300, 2220}, {4310, 2320}, {4315, 2410}, {4330, 2500}, {4320, 2600}, {4460, 2670}
  };

  public static void main(String[] args) throws Exception {
    if (args.length > 1 || (args.length == 1 && !args[0].equals("--apply"))) {
      throw new IllegalArgumentException("Usage: AvalonRegionBuilder [--apply]");
    }
    boolean apply = args.length == 1;
    File mapFile = new File(Paths.MAP);
    File colFile = new File(Paths.COLLISION_MAP);
    try (MapReader map = new MapReader(mapFile)) {
      var collisions = CollisionMapIO.read(colFile);
      if (map.getWidth() != WIDTH
          || map.getHeight() != HEIGHT
          || collisions.getWidth() != WIDTH
          || collisions.getHeight() != HEIGHT)
        throw new IllegalStateException("Expected the existing 5120x3072 world map");
      byte[] collision = collisions.getData();
      var catalog = GroundMosaicCatalog.load();
      var build = new Build(map, collision, catalog);
      build.paintIslands();
      build.paintCauseway(NORTH_ROUTE);
      build.paintCauseway(SOUTH_ROUTE);
      build.paintLandmarks();
      build.validateRoutes();
      if (apply) {
        map.writeCompact(mapFile);
        CollisionMapIO.write(colFile, WIDTH, HEIGHT, collision);
      }
      System.out.printf(
          "%s: %d new island tiles, %d route tiles, %d landmark tiles, %d scenery anchors; routes connected.%n",
          apply ? "Saved" : "Validated (dry run)",
          build.islandTiles,
          build.routeTiles,
          build.landmarkTiles,
          build.decorAnchors);
    }
  }

  static final class Build {
    private final MapReader map;
    private final byte[] collision;
    private final GroundMosaicCatalog catalog;
    private final Map<String, Donor> donors = new HashMap<>();
    private int islandTiles, routeTiles, landmarkTiles, decorAnchors;

    Build(MapReader map, byte[] collision, GroundMosaicCatalog catalog) {
      this.map = map;
      this.collision = collision;
      this.catalog = catalog;
    }

    private void paintIslands() {
      // Each coast is a union of lobes with small irregularities, rather than a smooth ellipse.
      for (int y = 470; y <= 955; y++)
        for (int x = 3560; x <= 4305; x++) {
          double shape =
              Math.min(
                  ellipse(x, y, 3890, 715, 280, 205),
                  Math.min(ellipse(x, y, 4100, 675, 170, 145), ellipse(x, y, 3700, 755, 125, 135)));
          double coast =
              shape + 0.035 * Math.sin(x * .049 + y * .021) + 0.026 * Math.sin(x * .018 - y * .041);
          if (coast >= 1 || ellipse(x, y, 4010, 715, 65, 45) < 1) continue;
          String family =
              coast > .87
                  ? "RockFloor"
                  : (ellipse(x, y, 4105, 645, 115, 80) < 1 ? "Hardrock" : "Grass");
          claimOcean(x, y, family);
        }
      for (int y = 2315; y <= 2900; y++)
        for (int x = 3890; x <= 4780; x++) {
          double shape =
              Math.min(
                  ellipse(x, y, 4310, 2605, 365, 245),
                  Math.min(
                      ellipse(x, y, 4075, 2530, 140, 150), ellipse(x, y, 4565, 2670, 145, 150)));
          double coast =
              shape + 0.030 * Math.sin(x * .041 + y * .024) + 0.028 * Math.sin(x * .018 - y * .034);
          if (coast >= 1 || ellipse(x, y, 4395, 2545, 69, 42) < 1) continue;
          String family =
              coast > .89
                  ? "RockFloor"
                  : (ellipse(x, y, 4460, 2735, 180, 95) < 1 ? "Dgrass" : "Hardrock");
          claimOcean(x, y, family);
        }
    }

    private void claimOcean(int x, int y, String family) {
      int i = index(x, y);
      if (!water(map.getGroundSpriteName(x, y))
          || collision[i] != WATER
          || map.getDecorSpriteName(x, y) != null)
        throw new IllegalStateException("New island overlaps existing content at " + x + "," + y);
      setGround(x, y, family);
      collision[i] = (byte) FREE;
      islandTiles++;
    }

    private void paintCauseway(int[][] nodes) {
      for (int n = 1; n < nodes.length; n++) road(nodes[n - 1], nodes[n], 5, true);
    }

    private void paintLandmarks() throws Exception {
      // Main Avalon keeps its sanctuary, spawn clearings and handwritten scenery. These plazas
      // give the currently empty western grass, outer forest and southern wetland a readable goal.
      plaza(3610, 1320, 42, "RockFloor");
      plaza(3820, 1430, 38, "EarthTile");
      plaza(4450, 2050, 45, "RockFloor");
      road(new int[] {3610, 1320}, new int[] {3740, 1370}, 4, false);
      road(new int[] {3740, 1370}, new int[] {3905, 1400}, 4, false);
      road(new int[] {3820, 1430}, new int[] {3950, 1480}, 4, false);
      road(new int[] {4450, 2050}, new int[] {4360, 1950}, 4, false);
      road(new int[] {4360, 1950}, new int[] {4300, 1860}, 4, false);

      plaza(3890, 740, 40, "RockFloor");
      plaza(4050, 675, 34, "EarthTile");
      plaza(4320, 2600, 50, "EarthTile");
      plaza(4460, 2670, 35, "RockFloor");
      road(new int[] {3890, 740}, new int[] {3735, 760}, 4, false);
      road(new int[] {4320, 2600}, new int[] {4080, 2530}, 4, false);

      // Existing complete buildings are reused as scenery; stair sprites are omitted because
      // they are not functional travel links. Only original blocking walls retain collision.
      building("temple", 3868, 692);
      building("cottage", 3727, 765);
      building("storehouse", 4070, 605);
      building("cottage", 4305, 2585);
      building("storehouse", 4450, 2684);
      building("cottage", 3604, 1317);
      building("storehouse", 4440, 2050);

      pond(3584, 1296, 13, 7);
      pond(3644, 1352, 12, 8);
      pond(4415, 2078, 17, 8);
      pond(4490, 2012, 16, 7);
      standingRing(3820, 1430, 20, 12);
      standingRing(4320, 2600, 72, 18);
      standingRing(4050, 675, 28, 10);

      scatter(3630, 1305, 85, "SmallTree1", 21, 5, 39);
      scatter(3820, 1430, 80, "SmallTree3", 22, 5, 43);
      scatter(4450, 2050, 90, "SmallTree2", 22, 5, 41);
      scatter(3880, 715, 240, "SmallTree1", 24, 8, 53);
      scatter(4100, 675, 145, "Rock2", 21, 7, 43);
      scatter(4310, 2600, 310, "Rock2", 23, 7, 47);
      scatter(4480, 2735, 160, "DarkTree1", 26, 6, 59);
      scatter(4320, 2600, 260, "SmallRockPack1", 25, 6, 61);
    }

    private void plaza(int cx, int cy, int radius, String family) {
      for (int y = cy - radius; y <= cy + radius; y++)
        for (int x = cx - radius; x <= cx + radius; x++) {
          double distance = Math.hypot(x - cx, y - cy);
          double edge =
              radius + 2.0 * Math.sin(x * .13 + y * .07) + 1.5 * Math.sin(x * .047 - y * .11);
          if (distance > edge) continue;
          int i = index(x, y);
          if (collision[i] != FREE || map.getDecorSpriteName(x, y) != null) continue;
          if (water(map.getGroundSpriteName(x, y))) continue;
          setGround(x, y, family);
          landmarkTiles++;
        }
    }

    private void road(int[] a, int[] b, int halfWidth, boolean allowOcean) {
      int minX = Math.min(a[0], b[0]) - halfWidth - 1;
      int maxX = Math.max(a[0], b[0]) + halfWidth + 1;
      int minY = Math.min(a[1], b[1]) - halfWidth - 1;
      int maxY = Math.max(a[1], b[1]) + halfWidth + 1;
      for (int y = minY; y <= maxY; y++)
        for (int x = minX; x <= maxX; x++) {
          if (segmentDistance(x, y, a, b) > halfWidth) continue;
          int i = index(x, y);
          String existing = map.getGroundSpriteName(x, y);
          if (water(existing) && !allowOcean)
            throw new IllegalStateException("Mainland route meets water at " + x + "," + y);
          if (map.getDecorSpriteName(x, y) != null) continue;
          if (water(existing)) {
            if (collision[i] != WATER)
              throw new IllegalStateException("Causeway meets protected water at " + x + "," + y);
            collision[i] = (byte) FREE;
          } else if (collision[i] != FREE) continue;
          setGround(x, y, "EarthTile");
          routeTiles++;
        }
    }

    private void building(String templateName, int x, int y) throws Exception {
      MapStamp stamp = MapStamp.load(Path.of("assets/editor/templates", templateName + ".json"));
      for (var cell : stamp.cells()) {
        int tx = x + cell.x(), ty = y + cell.y();
        int i = index(tx, ty);
        if (water(map.getGroundSpriteName(tx, ty))
            || collision[i] != FREE
            || map.getDecorSpriteName(tx, ty) != null)
          throw new IllegalStateException("Building footprint is occupied at " + tx + "," + ty);
      }
      for (var cell : stamp.cells()) {
        int tx = x + cell.x(), ty = y + cell.y();
        var tile = cell.tile();
        if (tile.ground() != null && !tile.ground().equals("Black Tile"))
          map.setGroundSpriteName(tx, ty, tile.ground());
        collision[index(tx, ty)] =
            (byte)
                (CollisionType.fromValue(tile.collision()).isBlocksMovement()
                    ? tile.collision()
                    : FREE);
        if (tile.decor() == null || tile.decor().toLowerCase().startsWith("stair")) continue;
        map.setDecorSpriteName(tx, ty, tile.decor());
        map.setScale(tx, ty, tile.scaleX(), tile.scaleY());
        map.setOffset(tx, ty, tile.offsetX(), tile.offsetY());
        map.setZOrder(tx, ty, tile.depth());
        decorAnchors++;
      }
    }

    private void scatter(
        int cx, int cy, int radius, String sprite, int step, int inner, int modulus) {
      Donor donor = donor(sprite);
      for (int y = cy - radius; y <= cy + radius; y += step)
        for (int x = cx - radius; x <= cx + radius; x += step) {
          int hash = Math.floorMod(x * 7349 + y * 1933, 1009);
          int tx = x + hash % 7 - 3, ty = y + hash / 7 % 7 - 3;
          double distance = Math.hypot(tx - cx, ty - cy);
          if (distance < inner + 50 || distance > radius || hash % modulus > 11) continue;
          if (tx < 0 || ty < 0 || tx >= WIDTH || ty >= HEIGHT) continue;
          int i = index(tx, ty);
          if (collision[i] != FREE || map.getDecorSpriteName(tx, ty) != null) continue;
          String ground = map.getGroundSpriteName(tx, ty);
          if (water(ground) || ground.startsWith("EarthTile") || ground.startsWith("Town Road"))
            continue;
          copyDecor(donor, tx, ty);
        }
    }

    private void pond(int cx, int cy, int rx, int ry) {
      for (int y = cy - ry; y <= cy + ry; y++)
        for (int x = cx - rx; x <= cx + rx; x++) {
          if (ellipse(x, y, cx, cy, rx, ry) > 1) continue;
          int i = index(x, y);
          String ground = map.getGroundSpriteName(x, y);
          if (collision[i] != FREE
              || map.getDecorSpriteName(x, y) != null
              || ground == null
              || AvalonWorldLayout.isLegacyAvalonTile(x - AvalonWorldLayout.SHIFT_X, y)
              || ground.startsWith("EarthTile")
              || ground.startsWith("Town Road")) continue;
          setGround(x, y, "Ground_Water");
          collision[i] = (byte) WATER;
        }
    }

    private void standingRing(int cx, int cy, int radius, int count) {
      Donor donor = donor("Stone1h");
      for (int n = 0; n < count; n++) {
        if (n == count / 4) continue;
        double angle = n * 2 * Math.PI / count;
        int x = cx + (int) Math.round(radius * Math.cos(angle));
        int y = cy + (int) Math.round(radius * Math.sin(angle));
        int i = index(x, y);
        String ground = map.getGroundSpriteName(x, y);
        if (collision[i] == FREE && map.getDecorSpriteName(x, y) == null && !water(ground))
          copyDecor(donor, x, y);
      }
    }

    private Donor donor(String sprite) {
      Donor cached = donors.get(sprite);
      if (cached != null) return cached;
      for (int y = 0; y < HEIGHT; y++)
        for (int x = 0; x < 3072; x++)
          if (sprite.equals(map.getDecorSpriteName(x, y))) {
            Donor found = new Donor(x, y);
            donors.put(sprite, found);
            return found;
          }
      throw new IllegalStateException("No source scenery sprite: " + sprite);
    }

    private void copyDecor(Donor source, int x, int y) {
      map.setDecorSpriteName(x, y, map.getDecorSpriteName(source.x, source.y));
      map.setScale(x, y, map.getScaleX(source.x, source.y), map.getScaleY(source.x, source.y));
      map.setOffset(x, y, map.getOffsetX(source.x, source.y), map.getOffsetY(source.x, source.y));
      map.setZOrder(x, y, map.getZOrder(source.x, source.y));
      int sourceCollision = collision[index(source.x, source.y)] & 255;
      collision[index(x, y)] =
          (byte)
              (CollisionType.fromValue(sourceCollision).isBlocksMovement()
                  ? sourceCollision
                  : FREE);
      decorAnchors++;
    }

    private void validateRoutes() {
      var visited = new BitSet(WIDTH * HEIGHT);
      var queue = new ArrayDeque<Integer>();
      int start = index(4040, 1477);
      visited.set(start);
      queue.add(start);
      while (!queue.isEmpty()) {
        int i = queue.removeFirst();
        int x = i % WIDTH, y = i / WIDTH;
        for (int next : new int[] {i - 1, i + 1, i - WIDTH, i + WIDTH}) {
          if (next < 0
              || next >= WIDTH * HEIGHT
              || Math.abs(next % WIDTH - x) + Math.abs(next / WIDTH - y) != 1) continue;
          if (!visited.get(next)
              && !CollisionType.fromValue(collision[next] & 255).isBlocksMovement()) {
            visited.set(next);
            queue.add(next);
          }
        }
      }
      for (Site site : SITES)
        if (!visited.get(index(site.x, site.y)))
          throw new IllegalStateException("Unreachable location: " + site.name);
      for (int[] point : new int[][] {{4050, 675}, {3727, 765}, {4460, 2670}, {4300, 2250}})
        if (!visited.get(index(point[0], point[1])))
          throw new IllegalStateException("Unreachable route point " + point[0] + "," + point[1]);
    }

    private void setGround(int x, int y, String family) {
      String name = catalog.tileName(family, x, y);
      if (name == null) throw new IllegalStateException("Missing terrain family " + family);
      map.setGroundSpriteName(x, y, name);
    }
  }

  private static int index(int x, int y) {
    if (x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT)
      throw new IllegalArgumentException("Outside world map: " + x + "," + y);
    return y * WIDTH + x;
  }

  private static boolean water(String name) {
    return name == null || name.startsWith("Ground_Water");
  }

  private static double ellipse(int x, int y, int cx, int cy, int rx, int ry) {
    double dx = (x - cx) / (double) rx, dy = (y - cy) / (double) ry;
    return dx * dx + dy * dy;
  }

  private static double segmentDistance(int x, int y, int[] a, int[] b) {
    int dx = b[0] - a[0], dy = b[1] - a[1];
    double t =
        Math.max(
            0, Math.min(1, ((x - a[0]) * dx + (y - a[1]) * dy) / (double) (dx * dx + dy * dy)));
    return Math.hypot(x - a[0] - t * dx, y - a[1] - t * dy);
  }
}
