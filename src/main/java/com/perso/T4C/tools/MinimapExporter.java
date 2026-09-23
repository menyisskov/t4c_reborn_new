package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import com.perso.T4C.teleport.NamedLocation;
import com.perso.T4C.teleport.NamedLocations;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * T4C-0023: renders a stylized top-down "minimap" PNG for each of the fork's own zones (read from
 * the hand-authored {@code zones.json}, the same list the compendium website already documents),
 * colored by the real ground-tile art at each world tile (sampled average color per sprite, not
 * invented), plus a JSON sidecar with every NPC/monster spawn and nearby named landmark inside
 * each zone's cropped area - the compendium website overlays these as clickable pins.
 *
 * <p>Deliberately scoped down from "the whole worldmap": the populated area of {@code
 * worldmap.mapbin} is ~2800x2500 tiles, which at real pixel resolution is several gigapixels -
 * far too large to ship as a web image. Cropping to a padded box around each zone's own
 * {@code worldmapCenter} keeps each output image small (a few hundred KB) while still being
 * accurately colored from the game's real tile art, not a schematic placeholder.
 *
 * <p>Ground layer only (no decor overlay) - a deliberate simplification, see class javadoc above;
 * still gives real color variation (grass/water/stone/sand are different sprites with different
 * sampled colors).
 */
public final class MinimapExporter {
  private static final int PADDING_TILES = 50;
  private static final int PX_PER_TILE = 4;
  private static final int[] VOID_COLOR = {18, 18, 22};
  /** A monster type with this many or fewer spawns inside a zone's crop is treated as a
   * "boss" tier pin (bigger, always labeled); more than this is "trash" tier. Purely a spawn-count
   * heuristic - this codebase has no explicit boss/unique flag on MonsterDef to key off instead. */
  private static final int BOSS_SPAWN_COUNT_THRESHOLD = 2;

  private MinimapExporter() {}

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    Path outDir = Path.of(args.length > 0 ? args[0] : "compendium/data");
    Path mapsDir = outDir.resolve("maps");
    Files.createDirectories(mapsDir);

    List<ZoneCrop> zones = loadZoneCrops(outDir.resolve("zones.json"));
    if (zones.isEmpty()) {
      System.out.println("No zones with a worldmapCenter found in zones.json; nothing to render");
      return;
    }

    try (MapReader map = new MapReader(new File(Paths.MAP))) {
      Map<String, String[]> tileGridsByZone = new LinkedHashMap<>();
      Set<String> neededSpriteKeys = new HashSet<>();
      for (ZoneCrop z : zones) {
        String[] grid = new String[z.w * z.h];
        for (int dx = 0; dx < z.w; dx++) {
          int wx = z.minX + dx;
          if (wx < 0 || wx >= map.getWidth()) continue;
          for (int dy = 0; dy < z.h; dy++) {
            int wy = z.minY + dy;
            if (wy < 0 || wy >= map.getHeight()) continue;
            String name = map.getGroundSpriteName(wx, wy);
            grid[dy * z.w + dx] = name;
            if (name != null) neededSpriteKeys.add(SpriteBinIO.key(name));
          }
        }
        tileGridsByZone.put(z.id, grid);
      }

      Map<String, int[]> colorByKey = sampleColors(neededSpriteKeys);

      for (ZoneCrop z : zones) {
        String[] grid = tileGridsByZone.get(z.id);
        BufferedImage img =
            new BufferedImage(z.w * PX_PER_TILE, z.h * PX_PER_TILE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        for (int dx = 0; dx < z.w; dx++) {
          for (int dy = 0; dy < z.h; dy++) {
            String name = grid[dy * z.w + dx];
            int[] c =
                name != null ? colorByKey.getOrDefault(SpriteBinIO.key(name), VOID_COLOR) : VOID_COLOR;
            g.setColor(new Color(c[0], c[1], c[2]));
            g.fillRect(dx * PX_PER_TILE, dy * PX_PER_TILE, PX_PER_TILE, PX_PER_TILE);
          }
        }
        g.dispose();
        File pngFile = mapsDir.resolve(z.id + ".png").toFile();
        ImageIO.write(img, "png", pngFile);
        System.out.println("Wrote " + pngFile);
      }
    }

    writeMapsJson(outDir, zones);
  }

  private static void writeMapsJson(Path outDir, List<ZoneCrop> zones) throws Exception {
    Map<String, String> npcDisplayNames = new HashMap<>();
    for (NpcFactoryRegistry.Registration reg : NpcFactoryRegistry.registrations()) {
      npcDisplayNames.put(reg.id(), resolveOrFallback(reg.displayName(), reg.id()));
    }

    List<SpawnDefinition> npcSpawns = SpawnRegistry.npcs();
    List<SpawnDefinition> monsterSpawns = SpawnRegistry.monsters();
    List<NamedLocation> namedLocations = NamedLocations.all();

    List<Map<String, Object>> mapEntries = new ArrayList<>();
    for (ZoneCrop z : zones) {
      List<Map<String, Object>> npcsOut = new ArrayList<>();
      for (SpawnDefinition s : npcSpawns) {
        if (s.z() != 0 || !z.contains(s.x(), s.y())) continue;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.type());
        m.put("displayName", npcDisplayNames.getOrDefault(s.type(), s.type()));
        m.put("x", s.x());
        m.put("y", s.y());
        npcsOut.add(m);
      }

      Map<String, List<SpawnDefinition>> byResolvedName = new LinkedHashMap<>();
      for (SpawnDefinition s : monsterSpawns) {
        if (s.z() != 0 || !z.contains(s.x(), s.y())) continue;
        MonsterDef def = MonsterRegistry.findByName(s.type());
        String resolvedName = def != null ? def.getName() : s.type();
        byResolvedName.computeIfAbsent(resolvedName, k -> new ArrayList<>()).add(s);
      }
      List<Map<String, Object>> monstersOut = new ArrayList<>();
      for (Map.Entry<String, List<SpawnDefinition>> e : byResolvedName.entrySet()) {
        String name = e.getKey();
        List<SpawnDefinition> spawns = e.getValue();
        MonsterDef def = MonsterRegistry.findByName(spawns.get(0).type());
        String displayName = def != null ? resolveOrFallback(def.getDisplayName(), name) : name;
        String tier = spawns.size() <= BOSS_SPAWN_COUNT_THRESHOLD ? "boss" : "trash";
        for (SpawnDefinition s : spawns) {
          Map<String, Object> m = new LinkedHashMap<>();
          m.put("name", name);
          m.put("displayName", displayName);
          m.put("x", s.x());
          m.put("y", s.y());
          m.put("tier", tier);
          monstersOut.add(m);
        }
      }

      List<Map<String, Object>> namedLocsOut = new ArrayList<>();
      for (NamedLocation loc : namedLocations) {
        if (loc.worldZ() != 0 || !z.contains(loc.tileX(), loc.tileY())) continue;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", loc.displayName());
        m.put("x", loc.tileX());
        m.put("y", loc.tileY());
        namedLocsOut.add(m);
      }

      Map<String, Object> entry = new LinkedHashMap<>();
      entry.put("zoneId", z.id);
      entry.put("image", "maps/" + z.id + ".png");
      entry.put("imageWidth", z.w * PX_PER_TILE);
      entry.put("imageHeight", z.h * PX_PER_TILE);
      entry.put("originX", z.minX);
      entry.put("originY", z.minY);
      entry.put("pxPerTile", PX_PER_TILE);
      entry.put("npcs", npcsOut);
      entry.put("monsters", monstersOut);
      entry.put("namedLocations", namedLocsOut);
      mapEntries.add(entry);
    }

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    Path jsonPath = outDir.resolve("maps.json");
    Files.writeString(jsonPath, gson.toJson(mapEntries), StandardCharsets.UTF_8);
    System.out.println("Wrote " + jsonPath);
  }

  private static String resolveOrFallback(String key, String fallback) {
    String resolved = key == null ? null : I18n.resolve(key);
    return resolved == null || resolved.isBlank() || resolved.equals(key) ? fallback : resolved;
  }

  private static Map<String, int[]> sampleColors(Set<String> neededKeys) throws Exception {
    Map<String, int[]> colors = new HashMap<>();
    SpriteBinIO.readAll(
        Path.of(Paths.SPRITE_DIR),
        Paths.SPRITE_BIN_BASE,
        packed -> {
          String key = SpriteBinIO.key(packed.name());
          if (!neededKeys.contains(key) || colors.containsKey(key)) return;
          try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(packed.png()));
            if (img != null) colors.put(key, averageColor(img));
          } catch (Exception ignored) {
            // leave unresolved sprites to fall back to VOID_COLOR at paint time
          }
        });
    return colors;
  }

  private static int[] averageColor(BufferedImage img) {
    long r = 0, g = 0, b = 0, count = 0;
    int w = img.getWidth(), h = img.getHeight();
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int argb = img.getRGB(x, y);
        int a = (argb >>> 24) & 0xff;
        if (a < 16) continue;
        r += (argb >> 16) & 0xff;
        g += (argb >> 8) & 0xff;
        b += argb & 0xff;
        count++;
      }
    }
    if (count == 0) return VOID_COLOR;
    return new int[] {(int) (r / count), (int) (g / count), (int) (b / count)};
  }

  private static List<ZoneCrop> loadZoneCrops(Path zonesJsonPath) throws Exception {
    List<ZoneCrop> crops = new ArrayList<>();
    if (!Files.exists(zonesJsonPath)) return crops;
    try (FileReader reader = new FileReader(zonesJsonPath.toFile(), StandardCharsets.UTF_8)) {
      JsonElement parsed = JsonParser.parseReader(reader);
      JsonArray arr = parsed.getAsJsonArray();
      for (JsonElement el : arr) {
        JsonObject obj = el.getAsJsonObject();
        if (!obj.has("worldmapCenter")) continue;
        String id = obj.get("id").getAsString();
        JsonObject center = obj.getAsJsonObject("worldmapCenter");
        int cx = center.get("x").getAsInt();
        int cy = center.get("y").getAsInt();
        int radius = center.get("radius").getAsInt();
        crops.add(new ZoneCrop(id, cx, cy, radius));
      }
    }
    return crops;
  }

  private static final class ZoneCrop {
    final String id;
    final int minX;
    final int minY;
    final int w;
    final int h;

    ZoneCrop(String id, int centerX, int centerY, int radius) {
      this.id = id;
      int reach = radius + PADDING_TILES;
      int rawMinX = centerX - reach;
      int rawMinY = centerY - reach;
      int rawMaxX = centerX + reach;
      int rawMaxY = centerY + reach;
      this.minX = Math.max(0, rawMinX);
      this.minY = Math.max(0, rawMinY);
      this.w = Math.max(1, rawMaxX - this.minX);
      this.h = Math.max(1, rawMaxY - this.minY);
    }

    boolean contains(int worldX, int worldY) {
      return worldX >= minX && worldX < minX + w && worldY >= minY && worldY < minY + h;
    }
  }
}
