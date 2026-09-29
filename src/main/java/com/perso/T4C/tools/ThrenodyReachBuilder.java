package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.editor.build.MapStamp;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionType;
import com.perso.T4C.helper.GroundMosaicCatalog;
import com.perso.T4C.helper.MapReader;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/** One-time eastward world expansion for the level 300–400 Threnody Reach campaign. */
public final class ThrenodyReachBuilder {
  private static final int OLD_WIDTH = 5120;
  private static final int WIDTH = 6144;
  private static final int HEIGHT = 3072;
  private static final int WATER = CollisionType.DEEP_WATER.getValue();
  private static final int FREE = CollisionType.NONE.getValue();

  private ThrenodyReachBuilder() {}

  public static void main(String[] args) throws Exception {
    if (args.length > 1 || (args.length == 1 && !args[0].equals("--apply")))
      throw new IllegalArgumentException("Usage: ThrenodyReachBuilder [--apply]");
    boolean apply = args.length == 1;
    File mapFile = new File(Paths.MAP);
    File collisionFile = new File(Paths.COLLISION_MAP);
    try (MapReader old = new MapReader(mapFile)) {
      var oldCollision = CollisionMapIO.read(collisionFile);
      if (old.getWidth() != OLD_WIDTH
          || old.getHeight() != HEIGHT
          || oldCollision.getWidth() != OLD_WIDTH
          || oldCollision.getHeight() != HEIGHT)
        throw new IllegalStateException("Expected the pre-expansion 5120x3072 world");
      try (MapReader map = old.expandedCopy(WIDTH, HEIGHT, "Ground_Water (1, 1)")) {
        byte[] collision = new byte[WIDTH * HEIGHT];
        Arrays.fill(collision, (byte) WATER);
        for (int y = 0; y < HEIGHT; y++)
          System.arraycopy(oldCollision.getData(), y * OLD_WIDTH, collision, y * WIDTH, OLD_WIDTH);
        Build build = new Build(map, collision, GroundMosaicCatalog.load());
        build.paintOcean();
        build.paintLand();
        build.paintLandmarks();
        build.validate();
        if (apply) {
          map.writeCompact(mapFile);
          CollisionMapIO.write(collisionFile, WIDTH, HEIGHT, collision);
        }
        System.out.printf(
            "%s Threnody Reach: %d land tiles, %d scenery anchors; mainland preserved and arena isolated.%n",
            apply ? "Saved" : "Validated (dry run)", build.landTiles, build.decorAnchors);
      }
    }
  }

  private record Donor(int x, int y) {}

  private static final class Build {
    private final MapReader map;
    private final byte[] collision;
    private final GroundMosaicCatalog catalog;
    private final Map<String, Donor> donors = new HashMap<>();
    private int landTiles;
    private int decorAnchors;

    Build(MapReader map, byte[] collision, GroundMosaicCatalog catalog) {
      this.map = map;
      this.collision = collision;
      this.catalog = catalog;
    }

    void paintOcean() {
      for (int y = 0; y < HEIGHT; y++)
        for (int x = OLD_WIDTH; x < WIDTH; x++) setGround(x, y, "Ground_Water");
    }

    void paintLand() {
      for (int y = 825; y <= 2570; y++)
        for (int x = 5150; x < WIDTH; x++) {
          double shape =
              Math.min(
                  ellipse(x, y, 5580, 1500, 420, 650),
                  Math.min(
                      ellipse(x, y, 5420, 1110, 260, 220), ellipse(x, y, 5680, 2160, 350, 390)));
          double coast =
              shape + .035 * Math.sin(x * .047 + y * .018) + .025 * Math.sin(x * .015 - y * .039);
          if (coast >= 1) continue;
          String family =
              coast > .89 ? "RockFloor" : y < 1370 ? "Grass" : y < 1910 ? "Dgrass" : "Hardrock";
          setGround(x, y, family);
          collision[index(x, y)] = (byte) FREE;
          landTiles++;
        }
    }

    void paintLandmarks() throws Exception {
      // A sanctuary camp, three distinct hunting districts, and the outer court of the final seal.
      plaza(5505, 1150, 65, "RockFloor");
      plaza(5700, 1440, 58, "EarthTile");
      plaza(5480, 1700, 60, "RockFloor");
      plaza(5800, 1970, 60, "EarthTile");
      plaza(5640, 2120, 56, "RockFloor");
      road(5505, 1150, 5700, 1440, 5);
      road(5700, 1440, 5480, 1700, 5);
      road(5480, 1700, 5800, 1970, 5);
      road(5800, 1970, 5640, 2120, 5);
      building("temple", 5480, 1090);
      building("cottage", 5540, 1150);
      building("storehouse", 5680, 1438);
      building("cottage", 5475, 1690);
      building("storehouse", 5790, 1970);
      safeCamp(5505, 1150, 42);
      standingRing(5700, 1440, 30, 12);
      standingRing(5480, 1700, 27, 10);
      standingRing(5800, 1970, 30, 12);
      // The final arena is an island within this land. Its water ring is the physical quest gate;
      // the named travel destination inside it is revealed only by the penultimate quest.
      sealArena(5650, 2290, 86, 122);
      scatter(5450, 1240, 220, "SmallTree1", 19, 47);
      scatter(5720, 1480, 265, "Rock2", 21, 43);
      scatter(5450, 1730, 245, "DarkTree1", 24, 47);
      scatter(5750, 2070, 290, "SmallRockPack1", 22, 43);
      standingRing(5650, 2290, 66, 16);
    }

    private void plaza(int cx, int cy, int radius, String family) {
      for (int y = cy - radius; y <= cy + radius; y++)
        for (int x = cx - radius; x <= cx + radius; x++) {
          if (Math.hypot(x - cx, y - cy) > radius + 2 * Math.sin(x * .11 + y * .07)) continue;
          int i = index(x, y);
          if (collision[i] != FREE || map.getDecorSpriteName(x, y) != null) continue;
          setGround(x, y, family);
        }
    }

    private void road(int ax, int ay, int bx, int by, int halfWidth) {
      for (int y = Math.min(ay, by) - halfWidth; y <= Math.max(ay, by) + halfWidth; y++)
        for (int x = Math.min(ax, bx) - halfWidth; x <= Math.max(ax, bx) + halfWidth; x++) {
          if (segmentDistance(x, y, ax, ay, bx, by) > halfWidth) continue;
          if (collision[index(x, y)] != FREE || map.getDecorSpriteName(x, y) != null) continue;
          setGround(x, y, "EarthTile");
        }
    }

    private void building(String template, int x, int y) throws Exception {
      MapStamp stamp = MapStamp.load(Path.of("assets/editor/templates", template + ".json"));
      for (var cell : stamp.cells()) {
        int tx = x + cell.x(), ty = y + cell.y();
        if (collision[index(tx, ty)] != FREE || map.getDecorSpriteName(tx, ty) != null)
          throw new IllegalStateException("Building footprint occupied at " + tx + "," + ty);
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
                    : tile.collision() == CollisionType.INDOOR_SAFE_HAVEN.getValue()
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

    private void safeCamp(int cx, int cy, int radius) {
      for (int y = cy - radius; y <= cy + radius; y++)
        for (int x = cx - radius; x <= cx + radius; x++)
          if (Math.hypot(x - cx, y - cy) <= radius
              && collision[index(x, y)] == FREE
              && map.getDecorSpriteName(x, y) == null)
            collision[index(x, y)] = (byte) CollisionType.SAFE_HAVEN.getValue();
    }

    private void sealArena(int cx, int cy, int inner, int outer) {
      for (int y = cy - outer; y <= cy + outer; y++)
        for (int x = cx - outer; x <= cx + outer; x++) {
          double distance = Math.hypot(x - cx, y - cy);
          if (distance > outer) continue;
          int i = index(x, y);
          if (distance > inner) {
            if (map.getDecorSpriteName(x, y) != null)
              throw new IllegalStateException("Arena moat crosses scenery at " + x + "," + y);
            setGround(x, y, "Ground_Water");
            collision[i] = (byte) WATER;
          } else if (collision[i] == FREE) {
            setGround(x, y, "RockFloor");
          }
        }
    }

    private void standingRing(int cx, int cy, int radius, int count) {
      Donor source = donor("Stone1h");
      for (int n = 0; n < count; n++) {
        if (n == count / 4) continue;
        double angle = n * Math.PI * 2 / count;
        int x = cx + (int) Math.round(radius * Math.cos(angle));
        int y = cy + (int) Math.round(radius * Math.sin(angle));
        if (collision[index(x, y)] == FREE && map.getDecorSpriteName(x, y) == null)
          copyDecor(source, x, y);
      }
    }

    private void scatter(int cx, int cy, int radius, String sprite, int step, int modulus) {
      Donor source = donor(sprite);
      for (int y = cy - radius; y <= cy + radius; y += step)
        for (int x = cx - radius; x <= cx + radius; x += step) {
          if (x < OLD_WIDTH || x >= WIDTH || y < 0 || y >= HEIGHT) continue;
          int hash = Math.floorMod(x * 7349 + y * 1933, 1009);
          if (hash % modulus > 11 || Math.hypot(x - cx, y - cy) > radius) continue;
          // Keep the Nullguard court readable: large dark canopies hide actors and loot.
          if (sprite.equals("DarkTree1") && Math.hypot(x - 5470, y - 1760) < 90) continue;
          if (collision[index(x, y)] != FREE || map.getDecorSpriteName(x, y) != null) continue;
          String ground = map.getGroundSpriteName(x, y);
          if (ground == null || ground.startsWith("Ground_Water") || ground.startsWith("EarthTile"))
            continue;
          copyDecor(source, x, y);
        }
    }

    private Donor donor(String name) {
      Donor cached = donors.get(name);
      if (cached != null) return cached;
      for (int y = 0; y < HEIGHT; y++)
        for (int x = 0; x < 3072; x++)
          if (name.equals(map.getDecorSpriteName(x, y))) {
            Donor found = new Donor(x, y);
            donors.put(name, found);
            return found;
          }
      throw new IllegalStateException("Missing donor sprite " + name);
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

    private void validate() {
      BitSet visited = new BitSet(WIDTH * HEIGHT);
      ArrayDeque<Integer> queue = new ArrayDeque<>();
      int start = index(5505, 1150);
      visited.set(start);
      queue.add(start);
      while (!queue.isEmpty()) {
        int i = queue.removeFirst();
        int x = i % WIDTH, y = i / WIDTH;
        if (x > 0) visit(i - 1, visited, queue);
        if (x + 1 < WIDTH) visit(i + 1, visited, queue);
        if (y > 0) visit(i - WIDTH, visited, queue);
        if (y + 1 < HEIGHT) visit(i + WIDTH, visited, queue);
      }
      for (int[] point : new int[][] {{5700, 1440}, {5480, 1700}, {5800, 1970}, {5640, 2120}})
        if (!visited.get(index(point[0], point[1])))
          throw new IllegalStateException("Unreachable Reach site " + point[0] + "," + point[1]);
      if (visited.get(index(5650, 2290)))
        throw new IllegalStateException("The final arena must require its quest-gated travel link");
      if (collision[index(5505, 1150)] != CollisionType.SAFE_HAVEN.getValue())
        throw new IllegalStateException("Arrival camp is not safe");
    }

    private void visit(int i, BitSet visited, ArrayDeque<Integer> queue) {
      if (!visited.get(i) && !CollisionType.fromValue(collision[i] & 255).isBlocksMovement()) {
        visited.set(i);
        queue.add(i);
      }
    }

    private void setGround(int x, int y, String family) {
      String tile = catalog.tileName(family, x, y);
      if (tile == null) throw new IllegalStateException("Unknown ground family " + family);
      map.setGroundSpriteName(x, y, tile);
    }
  }

  private static int index(int x, int y) {
    return y * WIDTH + x;
  }

  private static double ellipse(int x, int y, int cx, int cy, int rx, int ry) {
    double dx = (x - cx) / (double) rx, dy = (y - cy) / (double) ry;
    return dx * dx + dy * dy;
  }

  private static double segmentDistance(int x, int y, int ax, int ay, int bx, int by) {
    int dx = bx - ax, dy = by - ay;
    double t =
        Math.max(0, Math.min(1, ((x - ax) * dx + (y - ay) * dy) / (double) (dx * dx + dy * dy)));
    return Math.hypot(x - ax - t * dx, y - ay - t * dy);
  }
}
