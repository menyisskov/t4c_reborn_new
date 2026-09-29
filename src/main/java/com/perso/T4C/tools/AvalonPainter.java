package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.AvalonWorldLayout;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.GroundMosaicCatalog;
import com.perso.T4C.helper.MapReader;
import java.io.File;
import java.util.List;

/**
 * T4C-0096: repaints and substantially enlarges the Avalon island landmass, following the same
 * pattern {@link ContinentPainter} (T4C-0024) established for Kraanhold - real ground art plus
 * walkable collision written directly into {@code worldmap.mapbin}/{@code .colbin}, not just spawns
 * dropped on whatever ground happens to already be there.
 *
 * <p><b>Root cause of the player-reported "solid black rectangles":</b> a survey of the region (see
 * the now-removed {@code AvalonSurvey} scratch tool used to investigate this pass) found that most
 * of Avalon's nominal footprint was never actually painted - it still carries the literal ground
 * sprite {@code "Black Tile"}, a placeholder the game's own map editor ({@code
 * MapEditorScreen.isUsableGroundForRepair}) explicitly excludes as "not real ground". Roughly
 * 63,000 of those Black Tile cells in the Avalon area also carry {@code ABSOLUTE} collision, i.e.
 * they render solid black *and* block movement - this is exactly the bug the player described, and
 * it is also why the explorable area felt tiny despite a much larger nominal footprint: a large
 * fraction of that footprint was an invisible wall of black void, not rendering/streaming pop-in.
 * This pass repaints every Black Tile cell found within the painted territories below with real
 * terrain and (where nothing else is on top of it) walkable collision.
 *
 * <p><b>Never touches already-good art:</b> unlike a from-scratch continent, most of Avalon's
 * existing small hand-painted patch (the Avalon Sanctuary settlement plus its immediate
 * surroundings) is already correct - real ground art, trees/buildings as decor, hard-cut coastline.
 * This painter only ever repaints a tile that is currently the "Black Tile" void or open {@code
 * Ground_Water}; any tile that already carries other real ground art is left completely alone. That
 * protects the existing settlement look and every existing coastline while still letting new
 * territory be claimed from open ocean to grow the island.
 *
 * <p><b>Territories</b> (same circle-with-sine-wobble + straight-road-bridge approach as {@link
 * ContinentPainter}, checked in priority order so a smaller, more specific territory always wins
 * over a larger "reach" that happens to overlap it - this is what produces the hard-cut borders
 * between sub-zones without any dedicated transition art, since this game has none):
 *
 * <ul>
 *   <li>{@code WildsCore} / {@code WildsReach} - The Avalon Wilds, the fey grove ({@code
 *       avalon_wilds_vigil}'s own canon center/radius, plus a new north-western extension of the
 *       same lush grass terrain to grow the grove considerably).
 *   <li>{@code VeilCore} / {@code VeilReach} - The Fading Veil, the blighted woodland ({@code
 *       fading_veil_reckoning}'s own canon center/radius, plus a new south-eastern extension). Uses
 *       {@code Hardrock} - its own zone summary calls it "the hardest ground on the island", so the
 *       harsh rocky tile family already in the catalog is a literal match, not just a mood match,
 *       and it reads as visually distinct from the Wilds' grass at a glance.
 *   <li>{@code Town} - Avalon Sanctuary, grown from a ~75x80 cramped NPC cluster to a real town
 *       footprint (radius 150, ~300x300) using the {@code EarthTile} trodden-earth ground the
 *       existing settlement patch already uses, so the enlarged town reads as one continuous place
 *       rather than a patchwork.
 *   <li>{@code TideWest} / {@code TideSouth} - two new coastal/wetland accent territories (west
 *       shore strand on {@code RockFloor}, southern marsh on {@code Dgrass}) that round out the
 *       island to five visually distinct ground families total and give the hub-and-spoke road
 *       network real destinations beyond the two existing lore zones.
 * </ul>
 *
 * <p>Every non-town territory is connected back to the town by a straight dirt-road bridge (the
 * same {@code Town Road Dale} tile family {@link ContinentPainter} used), so the enlarged
 * settlement is a real hub with paths radiating out to every area - including, implicitly, the
 * {@code spell.avalon_gateway} landing tile (1340, 1477), which already sits inside the town's own
 * territory.
 *
 * <p>A small rectangular exclusion (with a buffer) around the "Passage to Avalon" mainland crossing
 * (roughly 1440-1660, 1190-1410) is skipped entirely - that shore is a separate landmass reached by
 * boat/NPC vouching, not part of the island, and must not be repainted.
 */
public final class AvalonPainter {
  private AvalonPainter() {}

  private record ZoneSite(
      String name,
      int cx,
      int cy,
      int radius,
      String terrainBase,
      double freq1,
      double amp1,
      double phase1,
      double freq2,
      double amp2,
      double phase2) {}

  // Priority order matters: earlier entries win when circles overlap, so small/specific
  // territories are listed before the large "Reach" extensions that were deliberately placed to
  // overlap them and stitch the landmass together.
  private static final List<ZoneSite> ZONES =
      List.of(
          // Canon lore-zone cores - centers/radii match avalon_wilds_vigil and
          // fading_veil_reckoning exactly, so the already-painted heart of each zone is
          // reaffirmed, not reinterpreted.
          new ZoneSite("WildsCore", 1265, 1400, 110, "Grass", 3, 14, 0.4, 7, 9, 1.2),
          new ZoneSite("VeilCore", 1420, 1560, 130, "Hardrock", 4, 16, 1.1, 8, 10, 2.1),
          // The settlement, grown from ~40 to 150 radius (~40x40 -> ~300x300).
          new ZoneSite("Town", 1327, 1480, 150, "EarthTile", 5, 10, 0.2, 9, 6, 1.6),
          // New coastal/wetland accent territories - round the island out to 5 distinct biomes.
          new ZoneSite("TideWest", 900, 1650, 220, "RockFloor", 3, 18, 0.7, 6, 11, 2.4),
          new ZoneSite("TideSouth", 1800, 2000, 220, "Dgrass", 4, 20, 1.9, 9, 12, 0.5),
          // Large "Reach" extensions of the two lore zones - checked last, so they only fill in
          // territory the smaller circles above didn't already claim.
          new ZoneSite("WildsReach", 1000, 1250, 260, "Grass", 5, 24, 2.6, 11, 15, 1.0),
          new ZoneSite("VeilReach", 1550, 1900, 300, "Hardrock", 6, 26, 0.3, 13, 17, 2.8));

  // Hub-and-spoke: every other territory gets a road back to the town (index 2).
  private static final int TOWN = 2;
  private static final int[][] BRIDGES = {
    {TOWN, 0}, {TOWN, 1}, {TOWN, 3}, {TOWN, 4}, {TOWN, 5}, {TOWN, 6}
  };
  private static final int BRIDGE_HALF_WIDTH = 22;
  private static final String BRIDGE_TERRAIN = "Town Road Dale";
  private static final int NONE_COLLISION = 0;

  // "Passage to Avalon" mainland crossing - a separate landmass, never repainted. Buffered a
  // little beyond its own quest-zone bounds (1440-1660, 1190-1410) so no painted tile touches it.
  private static final int MAINLAND_MIN_X = 1400;
  private static final int MAINLAND_MAX_X = 1700;
  private static final int MAINLAND_MIN_Y = 1150;
  private static final int MAINLAND_MAX_Y = 1450;

  public static void main(String[] args) throws Exception {
    if (args.length != 1 || !"--apply".equals(args[0])) {
      throw new IllegalArgumentException("Usage: AvalonPainter --apply (expanded world only)");
    }
    int minX = 650, minY = 1100, maxX = 2100, maxY = 2250;

    GroundMosaicCatalog catalog = GroundMosaicCatalog.load();
    File mapFile = new File(Paths.MAP);
    File colFile = new File(Paths.COLLISION_MAP);

    int paintedGround = 0;
    int paintedRoad = 0;
    int collisionCleared = 0;

    try (MapReader map = new MapReader(mapFile)) {
      if (map.getWidth() < 5120)
        throw new IllegalStateException("Expand the world before painting Avalon");
      CollisionMapIO.CollisionMap col = CollisionMapIO.read(colFile);
      byte[] colData = col.getData();
      int colWidth = col.getWidth();

      for (int x = minX; x < maxX && x < map.getWidth(); x++) {
        for (int y = minY; y < maxY && y < map.getHeight(); y++) {
          if (isMainlandExclusion(x, y)) {
            continue;
          }
          int targetX = x + AvalonWorldLayout.SHIFT_X;
          String existing = map.getGroundSpriteName(targetX, y);
          if (!isVoidOrWater(existing)) {
            // Real, already-painted ground (existing settlement patch, existing forest, existing
            // coastline) - never touched.
            continue;
          }

          String terrainBase = terrainFor(x, y);
          boolean isRoad = false;
          if (terrainBase == null) {
            String bridgeTerrain = bridgeFor(x, y);
            if (bridgeTerrain == null) {
              continue; // stays open water / untouched void -> the hard-cut coastline
            }
            terrainBase = bridgeTerrain;
            isRoad = true;
          }

          String spriteName = catalog.tileName(terrainBase, x, y);
          if (spriteName == null) {
            continue;
          }
          map.setGroundSpriteName(targetX, y, spriteName);
          if (isRoad) {
            paintedRoad++;
          } else {
            paintedGround++;
          }

          String decorHere = map.getDecorSpriteName(targetX, y);
          if (decorHere == null || decorHere.isBlank()) {
            colData[y * colWidth + targetX] = (byte) NONE_COLLISION;
            collisionCleared++;
          }
        }
      }

      map.writeCompact(mapFile);
      CollisionMapIO.write(colFile, col.getWidth(), col.getHeight(), colData);
    }

    System.out.println(
        "Painted "
            + paintedGround
            + " biome tiles + "
            + paintedRoad
            + " road tiles across "
            + ZONES.size()
            + " territories + "
            + BRIDGES.length
            + " connecting roads; cleared collision on "
            + collisionCleared
            + " tiles.");
  }

  static boolean isMainlandExclusion(int x, int y) {
    return x >= MAINLAND_MIN_X && x < MAINLAND_MAX_X && y >= MAINLAND_MIN_Y && y < MAINLAND_MAX_Y;
  }

  private static boolean isVoidOrWater(String spriteName) {
    if (spriteName == null || spriteName.isBlank()) {
      return true;
    }
    String base = spriteName.replaceAll("\\s*\\(.*", "").trim();
    return base.equalsIgnoreCase("Black Tile") || base.equalsIgnoreCase("Ground_Water");
  }

  static String terrainFor(int x, int y) {
    for (ZoneSite z : ZONES) {
      double dx = x - z.cx();
      double dy = y - z.cy();
      double dist = Math.sqrt(dx * dx + dy * dy);
      double angle = Math.atan2(dy, dx);
      double wobble =
          z.amp1() * Math.sin(z.freq1() * angle + z.phase1())
              + z.amp2() * Math.sin(z.freq2() * angle + z.phase2());
      if (dist <= z.radius() + wobble) {
        return z.terrainBase();
      }
    }
    return null;
  }

  static String bridgeFor(int x, int y) {
    for (int[] bridge : BRIDGES) {
      ZoneSite a = ZONES.get(bridge[0]);
      ZoneSite b = ZONES.get(bridge[1]);
      if (pointToSegmentDistance(x, y, a.cx(), a.cy(), b.cx(), b.cy()) <= BRIDGE_HALF_WIDTH) {
        return BRIDGE_TERRAIN;
      }
    }
    return null;
  }

  private static double pointToSegmentDistance(
      double px, double py, double ax, double ay, double bx, double by) {
    double abx = bx - ax, aby = by - ay;
    double lenSq = abx * abx + aby * aby;
    double t = lenSq == 0 ? 0 : ((px - ax) * abx + (py - ay) * aby) / lenSq;
    t = Math.max(0, Math.min(1, t));
    double cx = ax + t * abx, cy = ay + t * aby;
    double dx = px - cx, dy = py - cy;
    return Math.sqrt(dx * dx + dy * dy);
  }
}
