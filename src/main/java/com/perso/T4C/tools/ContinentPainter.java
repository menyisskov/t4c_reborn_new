package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.GroundMosaicCatalog;
import com.perso.T4C.helper.MapReader;
import java.io.File;
import java.util.List;

/**
 * T4C-0024: paints a genuinely new landmass directly into {@code worldmap.mapbin}/{@code
 * .colbin} - real ground art and walkable collision over what is currently open ocean, not just
 * placing spawns on already-drawn terrain the way every previous zone in this fork (including
 * Avalon) has done. Confirmed before writing this: this game's ground-tile system
 * ({@link GroundMosaicCatalog}) is a pure {@code (worldX mod frameWidth, worldY mod frameHeight)}
 * texture-variation lookup with no directional/adjacency autotiling, and the game's own art has
 * no dedicated coastline/beach transition sprite family at all - every existing coastline in this
 * game is already just a hard cut between a grass-family tile and a water-family tile. So a
 * painted coastline here looks exactly as "handmade" as every other coastline already does;
 * there's no smarter bar to try to clear.
 *
 * <p>Five zone "territories" (a circle per zone, radius wobbled by a couple of sine harmonics so
 * the coastline isn't a perfect circle) plus straight dirt-road "bridges" connecting them into one
 * contiguous landmass. Each territory's terrain reuses a real, already-in-use ground material
 * that matches that zone's own biome text in {@code zones.json} (grass for the plains warband,
 * dead grass for the barrow-field, hard rock for the three mountain zones) - same sprites the
 * original three islands already use, not new art.
 *
 * <p>Placement (2150-3072, 2200-3072 on the shared worldmap) was verified empty of existing
 * spawns/named locations before painting (a handful of single stray legacy points sit at the very
 * edge of one zone's outer radius; nothing a real zone's own spawns would be placed on top of).
 *
 * <p>Ground layer only - collision is set to {@code CollisionType.NONE} (ordinary walkable
 * ground, the same type the vast majority of existing outdoor terrain already uses) for every
 * painted tile that has no decor sprite on it; decor (trees/rocks, but also pre-existing
 * structures like dungeon walls this footprint happens to overlap) is intentionally left
 * untouched - both its sprite *and* its collision - since decor keeps rendering regardless of the
 * ground layer underneath, and zeroing collision under a still-rendered wall would let players
 * walk straight through it. Deliberate simplification matching {@code MinimapExporter}'s same
 * "ground layer only" scope note.
 */
public final class ContinentPainter {
  private ContinentPainter() {}

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

  private static final List<ZoneSite> ZONES =
      List.of(
          new ZoneSite("Windhowl Marches", 2380, 2430, 140, "64kNormalGrass", 3, 22, 0.3, 7, 14, 1.1),
          new ZoneSite("Hollow March", 2500, 2700, 150, "Dgrass", 4, 24, 0.9, 9, 12, 2.0),
          new ZoneSite("Lesser Drake's Aerie", 2350, 2900, 160, "Hardrock", 5, 20, 1.5, 11, 15, 0.4),
          new ZoneSite(
              "Greater Drake's Bastion", 2650, 2880, 170, "Hardrock", 3, 26, 2.2, 8, 16, 1.8),
          new ZoneSite("Drake's Lair", 2850, 2780, 180, "Hardrock", 6, 24, 0.6, 13, 13, 2.9));

  // Indices into ZONES: which pairs get a connecting road, keeping the whole landmass contiguous
  // (Hollow March is the hub - it sits roughly central and borders all the others).
  private static final int[][] BRIDGES = {{0, 1}, {1, 2}, {1, 3}, {3, 4}};
  private static final int BRIDGE_HALF_WIDTH = 30;
  private static final String BRIDGE_TERRAIN = "Town Road Dale";
  private static final int NONE_COLLISION = 0;

  public static void main(String[] args) throws Exception {
    int minX = 2150, minY = 2200, maxX = 3072, maxY = 3072;

    GroundMosaicCatalog catalog = GroundMosaicCatalog.load();
    File mapFile = new File(Paths.MAP);
    File colFile = new File(Paths.COLLISION_MAP);

    int painted = 0;
    try (MapReader map = new MapReader(mapFile)) {
      CollisionMapIO.CollisionMap col = CollisionMapIO.read(colFile);
      byte[] colData = col.getData();
      int colWidth = col.getWidth();

      for (int x = minX; x < maxX && x < map.getWidth(); x++) {
        for (int y = minY; y < maxY && y < map.getHeight(); y++) {
          String terrainBase = terrainFor(x, y);
          if (terrainBase == null) continue;
          String spriteName = catalog.tileName(terrainBase, x, y);
          if (spriteName == null) continue;
          map.setGroundSpriteName(x, y, spriteName);
          String decorHere = map.getDecorSpriteName(x, y);
          if (decorHere == null || decorHere.isBlank()) {
            colData[y * colWidth + x] = (byte) NONE_COLLISION;
          }
          painted++;
        }
      }

      map.writeCompact(mapFile);
      CollisionMapIO.write(colFile, col.getWidth(), col.getHeight(), colData);
    }
    System.out.println("Painted " + painted + " tiles across " + ZONES.size() + " zone territories + "
        + BRIDGES.length + " connecting roads.");
  }

  private static String terrainFor(int x, int y) {
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
