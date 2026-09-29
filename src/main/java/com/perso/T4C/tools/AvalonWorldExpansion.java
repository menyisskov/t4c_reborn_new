package com.perso.T4C.tools;

import com.perso.T4C.helper.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * One-time migration of Avalon into new ocean east of the original world. Historical semantic
 * deltas, rather than a bounding rectangle, restore the old world. Requires the repository's
 * historical commits and --apply to write the result.
 */
public final class AvalonWorldExpansion {
  private static final int OLD_WIDTH = 3072;
  private static final int HEIGHT = 3072;
  private static final int NEW_WIDTH = 5120;
  private static final int SHIFT = AvalonWorldLayout.SHIFT_X;
  private static final Path WORLD = Path.of("assets/maps/worldmap");
  private static final Path HISTORY = Path.of("target/avalon-expansion-history");
  private static final String[] EXTENSIONS = {"mapbin", "decorbin", "colbin", "musiczones.bin"};

  private record Snapshot(MapReader map, byte[] collision) {}

  private record Changes(BitSet ground, BitSet decor, BitSet metadata, BitSet collision) {
    Changes() {
      this(new BitSet(), new BitSet(), new BitSet(), new BitSet());
    }

    BitSet all() {
      BitSet b = (BitSet) ground.clone();
      b.or(decor);
      b.or(metadata);
      b.or(collision);
      return b;
    }
  }

  private AvalonWorldExpansion() {}

  public static void main(String[] args) throws Exception {
    if (args.length > 1 || (args.length == 1 && !args[0].equals("--apply")))
      throw new IllegalArgumentException("Usage: AvalonWorldExpansion [--apply]");
    boolean apply = args.length == 1;
    MapReader current = new MapReader(WORLD.resolve("worldmap.mapbin").toFile());
    if (current.getWidth() != OLD_WIDTH || current.getHeight() != HEIGHT)
      throw new IllegalStateException(
          "Migration requires the original 3072x3072 world; already expanded?");
    byte[] oldCollision = CollisionMapIO.read(WORLD.resolve("worldmap.colbin").toFile()).getData();
    export("base", "21273765^");
    export("first", "21273765");
    export("expand-before", "2d804017^");
    export("expand", "2d804017");
    export("sanctuary-before", "d18b1772^");
    export("sanctuary", "d18b1772");
    Changes changes = new Changes();
    BitSet first = addChanges(read("base"), read("first"), changes);
    addChanges(read("expand-before"), read("expand"), changes);
    addChanges(read("sanctuary-before"), read("sanctuary"), changes);
    Snapshot baseline = read("base");
    BitSet owned = changes.all();
    BitSet saveMask = (BitSet) owned.clone();
    for (int i = saveMask.nextSetBit(0); i >= 0; i = saveMask.nextSetBit(i + 1)) {
      String ground = baseline.map.getGroundSpriteName(i % OLD_WIDTH, i / OLD_WIDTH);
      if (!isVacant(ground)) saveMask.clear(i);
    }
    protectLegacyEntrances(saveMask);
    MapReader expanded = current.expandedCopy(NEW_WIDTH, HEIGHT, "Ground_Water (1, 1)");
    byte[] collision = new byte[NEW_WIDTH * HEIGHT];
    Arrays.fill(collision, (byte) 3);
    for (int y = 0; y < HEIGHT; y++)
      System.arraycopy(oldCollision, y * OLD_WIDTH, collision, y * NEW_WIDTH, OLD_WIDTH);
    GroundMosaicCatalog catalog = GroundMosaicCatalog.load();
    // Give the new ocean its normal repeating tile pattern instead of a repeated single frame.
    for (int y = 0; y < HEIGHT; y++)
      for (int x = OLD_WIDTH; x < NEW_WIDTH; x++)
        expanded.setGroundSpriteName(x, y, tile(catalog, "Ground_Water", x, y));
    int land = 0;
    for (int y = 1100; y < 2250; y++)
      for (int x = 650; x < 2100; x++) {
        if (AvalonPainter.isMainlandExclusion(x, y)) continue;
        String terrain = AvalonPainter.terrainFor(x, y);
        if (terrain == null) terrain = AvalonPainter.bridgeFor(x, y);
        if (terrain == null) continue;
        expanded.setGroundSpriteName(x + SHIFT, y, tile(catalog, terrain, x + SHIFT, y));
        collision[y * NEW_WIDTH + x + SHIFT] = 0;
        land++;
      }
    // Only the initial Avalon paint and the fully authored sanctuary mask are transplanted.
    // Existing library/cave art between them is never selected by a broad rectangle.
    int copied = 0;
    for (int y = 1100; y < 2250; y++)
      for (int x = 650; x < 2100; x++) {
        if (!first.get(y * OLD_WIDTH + x) && !AvalonSanctuaryBuilder.inEditMask(x, y)) continue;
        copyTile(current, expanded, x, y, x + SHIFT, y);
        collision[y * NEW_WIDTH + x + SHIFT] = oldCollision[y * OLD_WIDTH + x];
        copied++;
      }
    // Restore each field actually modified by an Avalon authoring commit. Other world content,
    // including later Kraanhold work and original library entrances, stays in place.
    for (int i = owned.nextSetBit(0); i >= 0; i = owned.nextSetBit(i + 1)) {
      int x = i % OLD_WIDTH, y = i / OLD_WIDTH;
      if (changes.ground.get(i))
        expanded.setGroundSpriteName(x, y, baseline.map.getGroundSpriteName(x, y));
      if (changes.decor.get(i))
        expanded.setDecorSpriteName(x, y, baseline.map.getDecorSpriteName(x, y));
      if (changes.metadata.get(i)) copyMetadata(baseline.map, expanded, x, y, x, y);
      if (changes.collision.get(i)) collision[y * NEW_WIDTH + x] = baseline.collision[i];
    }
    int removed = thinVeil(expanded, collision);
    auditOriginal(current, oldCollision, expanded, collision, baseline, owned);
    List<MusicZoneBinaryIO.Entry> music = relocatedMusic();
    if (apply) {
      expanded.writeCompact(WORLD.resolve("worldmap.mapbin").toFile());
      CollisionMapIO.write(WORLD.resolve("worldmap.colbin").toFile(), NEW_WIDTH, HEIGHT, collision);
      MusicZoneBinaryIO.write(WORLD.resolve("worldmap.musiczones.bin").toFile(), music);
      try (DataOutputStream out =
          new DataOutputStream(Files.newOutputStream(WORLD.resolve("avalon-legacy-tiles.bin")))) {
        out.write("T4CAVAL1".getBytes(StandardCharsets.US_ASCII));
        out.writeInt(OLD_WIDTH);
        out.writeInt(HEIGHT);
        out.write(saveMask.toByteArray());
      }
    }
    System.out.printf(
        "%s: %d new land tiles; %d authored Avalon tiles copied; %d old cells restored; %d migration-mask cells; %d ghost trees removed.%n",
        apply ? "Saved" : "Validated",
        land,
        copied,
        owned.cardinality(),
        saveMask.cardinality(),
        removed);
  }

  private static void auditOriginal(
      MapReader before,
      byte[] beforeCol,
      MapReader after,
      byte[] afterCol,
      Snapshot baseline,
      BitSet owned) {
    int outside = 0, baselineMismatches = 0;
    for (int y = 0; y < HEIGHT; y++)
      for (int x = 0; x < OLD_WIDTH; x++) {
        int i = y * OLD_WIDTH + x;
        MapReader reference = owned.get(i) ? baseline.map : before;
        byte expected = owned.get(i) ? baseline.collision[i] : beforeCol[i];
        boolean different =
            !Objects.equals(reference.getGroundSpriteName(x, y), after.getGroundSpriteName(x, y))
                || !Objects.equals(
                    reference.getDecorSpriteName(x, y), after.getDecorSpriteName(x, y))
                || reference.getScaleX(x, y) != after.getScaleX(x, y)
                || reference.getScaleY(x, y) != after.getScaleY(x, y)
                || reference.getOffsetX(x, y) != after.getOffsetX(x, y)
                || reference.getOffsetY(x, y) != after.getOffsetY(x, y)
                || reference.getZOrder(x, y) != after.getZOrder(x, y)
                || expected != afterCol[y * NEW_WIDTH + x];
        if (different) {
          if (owned.get(i)) baselineMismatches++;
          else outside++;
        }
      }
    System.out.println(
        "AUDIT original world: outside-owned changes="
            + outside
            + ", owned baseline mismatches="
            + baselineMismatches);
    if (outside != 0 || baselineMismatches != 0)
      throw new IllegalStateException("Original-world restoration audit failed");
    for (int[] p :
        new int[][] {{985, 1465}, {1080, 1400}, {1000, 1464}, {1155, 1418}, {1058, 1606}})
      System.out.println(
          "LIBRARY "
              + p[0]
              + ","
              + p[1]
              + " ground="
              + after.getGroundSpriteName(p[0], p[1])
              + " decor="
              + after.getDecorSpriteName(p[0], p[1])
              + " collision="
              + afterCol[p[1] * NEW_WIDTH + p[0]]);
  }

  private static boolean isVacant(String name) {
    return name == null || name.equals("Black Tile") || name.startsWith("Ground_Water");
  }

  private static String tile(GroundMosaicCatalog catalog, String family, int x, int y) {
    String name = catalog.tileName(family, x, y);
    if (name == null) throw new IllegalStateException("Unknown ground family " + family);
    return name;
  }

  private static void copyTile(MapReader source, MapReader dest, int sx, int sy, int x, int y) {
    dest.setGroundSpriteName(x, y, source.getGroundSpriteName(sx, sy));
    dest.setDecorSpriteName(x, y, source.getDecorSpriteName(sx, sy));
    copyMetadata(source, dest, sx, sy, x, y);
  }

  private static void copyMetadata(MapReader source, MapReader dest, int sx, int sy, int x, int y) {
    dest.setScale(x, y, source.getScaleX(sx, sy), source.getScaleY(sx, sy));
    dest.setOffset(x, y, source.getOffsetX(sx, sy), source.getOffsetY(sx, sy));
    dest.setZOrder(x, y, source.getZOrder(sx, sy));
  }

  private static Changes difference(Snapshot a, Snapshot b) {
    Changes out = new Changes();
    for (int y = 0; y < HEIGHT; y++)
      for (int x = 0; x < OLD_WIDTH; x++) {
        int i = y * OLD_WIDTH + x;
        if (!Objects.equals(a.map.getGroundSpriteName(x, y), b.map.getGroundSpriteName(x, y)))
          out.ground.set(i);
        if (!Objects.equals(a.map.getDecorSpriteName(x, y), b.map.getDecorSpriteName(x, y)))
          out.decor.set(i);
        if (a.map.getScaleX(x, y) != b.map.getScaleX(x, y)
            || a.map.getScaleY(x, y) != b.map.getScaleY(x, y)
            || a.map.getOffsetX(x, y) != b.map.getOffsetX(x, y)
            || a.map.getOffsetY(x, y) != b.map.getOffsetY(x, y)
            || a.map.getZOrder(x, y) != b.map.getZOrder(x, y)) out.metadata.set(i);
        if (a.collision[i] != b.collision[i]) out.collision.set(i);
      }
    return out;
  }

  private static BitSet addChanges(Snapshot a, Snapshot b, Changes target) {
    Changes delta = difference(a, b);
    target.ground.or(delta.ground);
    target.decor.or(delta.decor);
    target.metadata.or(delta.metadata);
    target.collision.or(delta.collision);
    return delta.all();
  }

  private static Snapshot read(String name) throws Exception {
    Path dir = HISTORY.resolve(name);
    return new Snapshot(
        new MapReader(dir.resolve("worldmap.mapbin").toFile()),
        CollisionMapIO.read(dir.resolve("worldmap.colbin").toFile()).getData());
  }

  private static void export(String name, String revision) throws Exception {
    Path dir = HISTORY.resolve(name);
    Files.createDirectories(dir);
    for (String ext : EXTENSIONS) {
      Path output = dir.resolve("worldmap." + ext);
      Process process =
          new ProcessBuilder("git", "show", revision + ":assets/maps/worldmap/worldmap." + ext)
              .redirectOutput(output.toFile())
              .redirectError(ProcessBuilder.Redirect.INHERIT)
              .start();
      if (process.waitFor() != 0)
        throw new IOException("Cannot read historical map at " + revision);
    }
  }

  private static List<int[]> positions(String directory, Pattern pattern, boolean teleports)
      throws IOException {
    List<int[]> points = new ArrayList<>();
    try (var files = Files.walk(Path.of(directory))) {
      for (Path path : files.filter(p -> p.toString().endsWith(".java")).toList()) {
        Matcher m = pattern.matcher(Files.readString(path));
        while (m.find()) {
          if (teleports) {
            for (int offset : new int[] {1, 4})
              if (Integer.parseInt(m.group(offset)) == 0)
                points.add(
                    new int[] {
                      Integer.parseInt(m.group(offset + 1)), Integer.parseInt(m.group(offset + 2))
                    });
          } else if (Integer.parseInt(m.group(3)) == 0)
            points.add(new int[] {Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))});
        }
      }
    }
    return points;
  }

  private static void protectLegacyEntrances(BitSet mask) throws IOException {
    Pattern pattern =
        Pattern.compile(
            "new TeleportDefinition\\(\\s*\\d+\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");
    for (int[] p : positions("src/main/java/com/perso/T4C/teleport/definition", pattern, true)) {
      if (p[0] >= OLD_WIDTH) continue;
      for (int dy = -4; dy <= 4; dy++)
        for (int dx = -4; dx <= 4; dx++) {
          int x = p[0] + dx, y = p[1] + dy;
          if (x >= 0 && x < OLD_WIDTH && y >= 0 && y < HEIGHT) mask.clear(y * OLD_WIDTH + x);
        }
    }
  }

  private static int thinVeil(MapReader map, byte[] collision) throws IOException {
    Pattern pattern =
        Pattern.compile(
            "@Spawn\\([^)]*?x\\s*=\\s*(\\+?\\d+)\\s*,\\s*y\\s*=\\s*(\\d+)\\s*,\\s*z\\s*=\\s*(\\d+)",
            Pattern.DOTALL);
    List<int[]> spawns = positions("src/main/java/com/perso/T4C/monster", pattern, false);
    spawns.addAll(positions("src/main/java/com/perso/T4C/npc", pattern, false));
    spawns.removeIf(p -> p[0] < OLD_WIDTH);
    int removed = 0, kept = 0;
    List<int[]> crowns = new ArrayList<>();
    for (int y = 1100; y < 2250; y++)
      for (int x = 650 + SHIFT; x < 2100 + SHIFT; x++) {
        String decor = map.getDecorSpriteName(x, y);
        if (decor == null) continue;
        String lower = decor.toLowerCase(Locale.ROOT);
        if (!(lower.contains("deadtree")
            || lower.contains("deadforesttree")
            || lower.contains("darktree")
            || lower.contains("leaflesstree"))) continue;
        boolean blocked = AvalonPainter.bridgeFor(x - SHIFT, y) != null;
        // 24-tile radius exceeds the largest ghost canopy in both axes. Also protect every
        // spawn and its immediate wandering/loot area, not just the tree's trunk tile.
        for (int[] p : spawns)
          if (Math.hypot(x - p[0], y - p[1]) < 24) {
            blocked = true;
            break;
          }
        if (!blocked)
          for (int[] p : crowns)
            if (Math.hypot(x - p[0], y - p[1]) < 13) {
              blocked = true;
              break;
            }
        boolean cluster =
            ((x - SHIFT - 1420) * (x - SHIFT - 1420) + (y - 1560) * (y - 1560) > 95 * 95);
        if (!blocked && cluster && Math.floorMod(x * 7349 + y * 1933, 43) == 0) {
          crowns.add(new int[] {x, y});
          kept++;
          continue;
        }
        map.setDecorSpriteName(x, y, null);
        map.setScale(x, y, 1, 1);
        map.setOffset(x, y, 0, 0);
        map.setZOrder(x, y, 0);
        if (CollisionType.fromValue(collision[y * NEW_WIDTH + x] & 255).isBlocksMovement())
          collision[y * NEW_WIDTH + x] = 0;
        removed++;
      }
    System.out.println(
        "Fading Veil ghost trees: kept "
            + kept
            + ", removed "
            + removed
            + ", protected "
            + spawns.size()
            + " spawn anchors");
    return removed;
  }

  private static List<MusicZoneBinaryIO.Entry> relocatedMusic() throws Exception {
    Set<String> existing = new HashSet<>();
    for (var e : MusicZoneBinaryIO.read(HISTORY.resolve("base/worldmap.musiczones.bin").toFile()))
      existing.add(e.name);
    Set<String> added = new HashSet<>();
    for (var e : MusicZoneBinaryIO.read(HISTORY.resolve("first/worldmap.musiczones.bin").toFile()))
      if (!existing.contains(e.name)) added.add(e.name);
    List<MusicZoneBinaryIO.Entry> music =
        MusicZoneBinaryIO.read(WORLD.resolve("worldmap.musiczones.bin").toFile());
    for (var e : music)
      if (added.contains(e.name)) {
        e.x1 += SHIFT;
        e.x2 += SHIFT;
      }
    return music;
  }
}
