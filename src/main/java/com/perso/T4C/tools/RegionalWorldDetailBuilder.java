package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.perso.T4C.config.Paths;
import com.perso.T4C.divine.DivinePantheon;
import com.perso.T4C.editor.build.MapStamp;
import com.perso.T4C.helper.*;
import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import java.io.File;
import java.nio.file.*;
import java.util.*;

/** Authored settlements, connected roads and hunting districts on the fork's new land. */
public final class RegionalWorldDetailBuilder {
  public record Site(String name, int x, int y, int radius, int hubX, int hubY) {}

  public record Building(String site, String template, int x, int y) {}

  public record Hunt(String type, int x, int y, int radius, int count, boolean aggressive) {}

  public record Layout(List<Site> forts, List<Building> buildings, List<SpawnDefinition> hunting) {}

  public static final List<Site> FORTS =
      List.of(
          new Site("Oathwood Castle", 3750, 1190, 48, 4040, 1477),
          new Site("Sanctuary East Keep", 4250, 1660, 42, 4040, 1477),
          new Site("Moonwake Castle", 3925, 610, 42, 3890, 740),
          new Site("Emberglass Citadel", 4300, 2700, 52, 4320, 2600),
          new Site("Sable Fen Watch", 4400, 2030, 36, 4450, 2050),
          new Site("Threnody Gatehouse", 5470, 1235, 42, 5505, 1150),
          new Site("Bellward Castle", 5680, 1530, 50, 5700, 1440),
          new Site("Black Oath Keep", 5510, 1820, 46, 5480, 1700),
          new Site("Regent's Bastion", 5760, 2050, 44, 5800, 1970),
          new Site("Hollow Crown", 5650, 2290, 40, 5650, 2290),
          new Site("Marchwarden Castle", 2400, 2470, 40, 2380, 2430),
          new Site("Drakewatch Bastion", 2650, 2880, 42, 2650, 2880));

  private static final List<Site> HAMLETS =
      List.of(
          new Site("Crescent Lakeside", 3560, 1340, 0, 3610, 1320),
          new Site("Western Orchard", 3710, 1515, 0, 3820, 1430),
          new Site("Ophira's Outlying Farms", 4020, 1590, 0, 4040, 1477),
          new Site("Veil Road Refuge", 4270, 1840, 0, 4450, 2050),
          new Site("Moonwake Harbor", 3780, 745, 0, 3890, 740),
          new Site("Eastern Shoal Watch", 4130, 700, 0, 4050, 675),
          new Site("Emberglass Workers", 4220, 2500, 0, 4320, 2600),
          new Site("Ashroad Village", 4520, 2730, 0, 4460, 2670),
          new Site("Northern Witness Camp", 5370, 1120, 0, 5505, 1150),
          new Site("Bellward Barracks", 5820, 1530, 0, 5700, 1440),
          new Site("Exiles' Refuge", 5350, 1660, 0, 5480, 1700),
          new Site("Southern Siege Camp", 5790, 2180, 0, 5640, 2120),
          new Site("Windhowl March Farms", 2310, 2430, 0, 2380, 2430),
          new Site("Barrow Road Hamlet", 2500, 2650, 0, 2500, 2700),
          new Site("Aerie Outpost", 2350, 2860, 0, 2350, 2900));

  public static final List<Hunt> HUNTS =
      List.of(
          new Hunt("Fey Warden", 3840, 1320, 240, 40, false),
          new Hunt("Moonlit Stalker", 3965, 1400, 240, 40, true),
          new Hunt("Veilbound Wraith", 4120, 1560, 290, 40, true),
          new Hunt("Sundered Sentinel", 4330, 1850, 250, 40, true),
          new Hunt("Moonwake Revenant", 3930, 750, 210, 40, true),
          new Hunt("Emberglass Ashguard", 4360, 2620, 260, 40, true),
          new Hunt("Ashbound Exile", 5690, 1410, 230, 40, true),
          new Hunt("Nullguard", 5470, 1760, 250, 40, true),
          new Hunt("Rift Wraith", 5630, 2100, 220, 40, true),
          new Hunt("Barrow Wight", 2500, 2700, 110, 20, true),
          new Hunt("Lesser Drake", 2350, 2900, 115, 20, true),
          new Hunt("Greater Drake", 2650, 2880, 115, 20, true));

  private final MapReader map;
  private final byte[] collision;
  private final int width;
  private final GroundMosaicCatalog ground = GroundMosaicCatalog.load();
  private final Map<String, MapStamp.Tile> donors = new HashMap<>();
  private final BitSet reserved;
  private final BitSet roads;
  private final BitSet ghostCanopies;
  private final BitSet encounterClearings;
  private final List<Site> forts = new ArrayList<>();
  private final List<Building> buildings = new ArrayList<>();
  private final List<SpawnDefinition> hunting = new ArrayList<>();
  private int scenery;
  // These are the existing Verdant Warden escorts, separate from the Vigil hunting population.
  private static final int[][] VERDANT_ESCORTS = {{4150, 1595}, {4170, 1595}, {4160, 1613}};

  private RegionalWorldDetailBuilder(MapReader map, byte[] collision) {
    this.map = map;
    this.collision = collision;
    width = map.getWidth();
    reserved = new BitSet(collision.length);
    roads = new BitSet(collision.length);
    ghostCanopies = new BitSet(collision.length);
    encounterClearings = new BitSet(collision.length);
    for (int[] point : VERDANT_ESCORTS) {
      reserve(point[0], point[1], 5);
      markCircle(encounterClearings, point[0], point[1], 24);
    }
    for (var npc : SpawnRegistry.npcs()) if (npc.z() == 0) reserve(npc.x(), npc.y(), 12);
    for (var monster : SpawnRegistry.monsters())
      if (monster.z() == 0) reserve(monster.x(), monster.y(), 3);
    for (var god : DivinePantheon.all()) reserve(god.x(), god.y(), 17);
    for (var location : com.perso.T4C.teleport.NamedLocations.all())
      if (location.worldZ() == 0) reserve(location.tileX(), location.tileY(), 8);
    for (var link : com.perso.T4C.teleport.TeleportRegistry.load()) {
      if (link.sourceZ() == 0) reserve(link.sourceX(), link.sourceY(), 5);
      if (link.targetZ() == 0) reserve(link.targetX(), link.targetY(), 5);
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length > 1
        || args.length == 1 && !List.of("--apply", "--rebuild-from-backup").contains(args[0]))
      throw new IllegalArgumentException(
          "Usage: RegionalWorldDetailBuilder [--apply|--rebuild-from-backup]");
    boolean apply = args.length == 1;
    boolean rebuild = apply && args[0].equals("--rebuild-from-backup");
    if (!rebuild && Files.exists(Path.of("assets/maps/worldmap/world-detail-layout.json")))
      throw new IllegalStateException(
          "Detail pass already applied; restore its source-map backup before rebuilding");
    try (var map =
        new MapReader(
            rebuild
                ? new File("target/world-detail-backup/worldmap.mapbin")
                : new File(Paths.MAP))) {
      var data =
          CollisionMapIO.read(
              rebuild
                  ? new File("target/world-detail-backup/worldmap.colbin")
                  : new File(Paths.COLLISION_MAP));
      if (map.getWidth() != 6144 || map.getHeight() != 3072 || data.getWidth() != map.getWidth())
        throw new IllegalStateException("Expected the expanded campaign world");
      var build = new RegionalWorldDetailBuilder(map, data.getData());
      build.build();
      build.validate();
      var layout = new Layout(build.forts, build.buildings, build.hunting);
      Path output = Path.of("target/world-detail-output");
      Files.createDirectories(output);
      Files.writeString(
          output.resolve("layout.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(layout));
      if (apply) {
        map.writeCompact(output.resolve("worldmap.mapbin").toFile());
        CollisionMapIO.write(
            output.resolve("worldmap.colbin").toFile(),
            map.getWidth(),
            map.getHeight(),
            build.collision);
        for (String extension : List.of("mapbin", "decorbin", "colbin"))
          Files.copy(
              output.resolve("worldmap." + extension),
              Path.of("assets/maps/worldmap/worldmap." + extension),
              StandardCopyOption.REPLACE_EXISTING);
        Files.copy(
            output.resolve("layout.json"),
            Path.of("assets/maps/worldmap/world-detail-layout.json"),
            StandardCopyOption.REPLACE_EXISTING);
      }
      System.out.printf(
          "%s %d fortifications, %d buildings, %d scenery anchors and %d hunting spawns.%n",
          apply ? "Saved" : "Validated",
          build.forts.size(),
          build.buildings.size(),
          build.scenery,
          build.hunting.size());
    }
  }

  private void build() throws Exception {
    // Capture art before editing; original-map samples are never moved or changed.
    Set<String> needed =
        new HashSet<>(
            List.of(
                "CastleWallCornerN",
                "CastleWallCorner",
                "CastleWallCornerv",
                "CastleWallCorner2",
                "SmallTree1",
                "SmallTree2",
                "AppleTree1",
                "DeadTree1",
                "DarkTree1",
                "Rock1",
                "Rock2",
                "Stone1h",
                "WellM",
                "Object_LightHaven_Fountain"));
    for (int n = 1; n <= 5; n++) {
      needed.add("CastleWall " + n);
      needed.add("CastleDarkWall " + n);
    }
    for (int y = 0; y < map.getHeight() && !needed.isEmpty(); y++)
      for (int x = 0; x < 3072 && !needed.isEmpty(); x++) {
        String name = map.getDecorSpriteName(x, y);
        if (needed.remove(name)) donors.put(name, MapStamp.Tile.read(map, collision, x, y));
      }
    if (!needed.isEmpty()) throw new IllegalStateException("Missing native art: " + needed);
    for (Site site : FORTS) fortress(site);
    for (Site site : HAMLETS) {
      plaza(site.x(), site.y(), 15, "EarthTile");
      for (int n = 0; n < 6; n++)
        building(site, n % 3 == 0 ? "storehouse" : "cottage", 24 + n * 6, n * 2.399);
      connect(site.x(), site.y(), site.hubX(), site.hubY());
      furniture(site.x() + 15, site.y() + 9, "WellM");
      orchard(site.x() - 42, site.y() + 35);
    }
    planHunting();
    landscape();
  }

  private void fortress(Site desired) throws Exception {
    Site chosen = null;
    for (int attempt = 0; attempt < 240 && chosen == null; attempt++) {
      int dx =
          attempt == 0 ? 0 : (int) Math.round(Math.cos(attempt * 2.399) * Math.sqrt(attempt) * 3);
      int dy =
          attempt == 0 ? 0 : (int) Math.round(Math.sin(attempt * 2.399) * Math.sqrt(attempt) * 3);
      Site candidate =
          new Site(
              desired.name(),
              desired.x() + dx,
              desired.y() + dy,
              desired.radius(),
              desired.hubX(),
              desired.hubY());
      if (canFortify(candidate)) chosen = candidate;
    }
    if (chosen == null)
      throw new IllegalStateException("No viable castle footprint for " + desired.name());
    Site s = chosen;
    forts.add(s);
    int r = s.radius();
    for (int y = s.y() - r; y <= s.y() + r; y++)
      for (int x = s.x() - r; x <= s.x() + r; x++) {
        int distance = Math.abs(x - s.x()) + Math.abs(y - s.y());
        if (distance > r) continue;
        if (editable(x, y) && !reserved.get(index(x, y))) {
          clearNatural(x, y);
          if (walkable(x, y)) setGround(x, y, "Town Road Dale");
        }
        if (distance != r || gate(s, x, y)) continue;
        int dx = x - s.x(), dy = y - s.y();
        String art = dx * dy <= 0 ? "CastleWall " : "CastleDarkWall ";
        art += Math.floorMod(x, 5) + 1;
        if (x == s.x()) art = y < s.y() ? "CastleWallCornerN" : "CastleWallCornerv";
        if (y == s.y()) art = x > s.x() ? "CastleWallCorner" : "CastleWallCorner2";
        place(x, y, art, true);
      }
    building(s, r >= 46 ? "temple" : "storehouse", 8, 0);
    for (int n = 0; n < 5; n++) building(s, "cottage", 23, n * 2.1 + .5);
    furniture(s.x() + 10, s.y() + 12, "Object_LightHaven_Fountain");
    // The gate crossings remain wide enough for ordinary movement and chase navigation.
    int half = r / 2;
    for (int sign : new int[] {-1, 1}) {
      int gx = s.x() + sign * half, gy = s.y() + sign * (r - half);
      connect(s.x(), s.y(), gx, gy);
      connect(gx, gy, s.hubX(), s.hubY());
    }
  }

  private boolean canFortify(Site s) {
    int r = s.radius();
    int usable = 0;
    for (int y = s.y() - r; y <= s.y() + r; y++)
      for (int x = s.x() - r; x <= s.x() + r; x++) {
        int d = Math.abs(x - s.x()) + Math.abs(y - s.y());
        if (d > r) continue;
        if (!owned(x, y) || water(x, y)) return false;
        if (d == r && !gate(s, x, y) && (!editable(x, y) || reserved.get(index(x, y))))
          return false;
        if (walkable(x, y)) usable++;
      }
    return usable > r * r;
  }

  private static boolean gate(Site s, int x, int y) {
    int half = s.radius() / 2;
    return Math.abs(Math.abs(x - s.x()) - half) <= 3
        && Math.abs(Math.abs(y - s.y()) - (s.radius() - half)) <= 3;
  }

  private void building(Site site, String name, int distance, double angle) throws Exception {
    MapStamp stamp = MapStamp.load(Path.of("assets/editor/templates", name + ".json"));
    for (int attempt = 0; attempt < 120; attempt++) {
      double a = angle + attempt * 2.399;
      int radius = distance + (int) Math.sqrt(attempt) * 2;
      int x = site.x() + (int) (Math.cos(a) * radius) - stamp.width() / 2;
      int y = site.y() + (int) (Math.sin(a) * radius) - stamp.height() / 2;
      boolean valid = true;
      for (var cell : stamp.cells()) {
        int tx = x + cell.x(), ty = y + cell.y();
        if (!editable(tx, ty)
            || reserved.get(index(tx, ty))
            || roads.get(index(tx, ty))
            || site.radius() > 0
                && Math.abs(tx - site.x()) + Math.abs(ty - site.y()) > site.radius() - 7) {
          valid = false;
          break;
        }
      }
      if (!valid) continue;
      for (var cell : stamp.cells()) {
        int tx = x + cell.x(), ty = y + cell.y();
        clearNatural(tx, ty);
        var tile = cell.tile();
        boolean doorway =
            tile.decor() != null && tile.decor().toLowerCase(Locale.ROOT).contains("door");
        if (tile.decor() != null
            && !doorway
            && !tile.decor().toLowerCase(Locale.ROOT).startsWith("stair")) {
          map.setDecorSpriteName(tx, ty, tile.decor());
          map.setScale(tx, ty, tile.scaleX(), tile.scaleY());
          map.setOffset(tx, ty, tile.offsetX(), tile.offsetY());
          map.setZOrder(tx, ty, tile.depth());
        }
        if (tile.ground() != null && !tile.ground().equals("Black Tile"))
          map.setGroundSpriteName(tx, ty, tile.ground());
        boolean wall =
            tile.decor() != null
                && tile.decor().toLowerCase(Locale.ROOT).contains("wall")
                && !doorway;
        collision[index(tx, ty)] =
            (byte)
                (wall
                    ? 1
                    : CollisionType.fromValue(tile.collision()).isBlocksMovement() && !doorway
                        ? tile.collision()
                        : 0);
        reserved.set(index(tx, ty));
      }
      buildings.add(new Building(site.name(), name, x, y));
      return;
    }
    // A site may have fewer cottages where existing quest scenery occupies its courtyard.
  }

  private void plaza(int cx, int cy, int r, String family) {
    for (int y = cy - r; y <= cy + r; y++)
      for (int x = cx - r; x <= cx + r; x++)
        if (Math.hypot(x - cx, y - cy) <= r && editable(x, y) && !reserved.get(index(x, y))) {
          clearNatural(x, y);
          setGround(x, y, family);
        }
  }

  private void connect(int ax, int ay, int bx, int by) {
    int start = nearestOpen(ax, ay), end = nearestOpen(bx, by);
    if (start < 0 || end < 0)
      throw new IllegalStateException("Road endpoint blocked: " + ax + "," + ay);
    int[] parents = new int[collision.length];
    Arrays.fill(parents, -1);
    int[] queue = new int[collision.length];
    int head = 0, tail = 0;
    queue[tail++] = start;
    parents[start] = start;
    while (head < tail && parents[end] < 0) {
      int at = queue[head++], x = at % width, y = at / width;
      for (int next :
          new int[] {
            at - width - 1,
            at - width + 1,
            at + width - 1,
            at + width + 1,
            at - 1,
            at + 1,
            at - width,
            at + width
          }) {
        int nx = next % width, ny = next / width;
        if (next < 0
            || next >= collision.length
            || Math.max(Math.abs(nx - x), Math.abs(ny - y)) != 1
            || parents[next] >= 0) continue;
        if (!walkable(nx, ny) || !walkable(nx, y) || !walkable(x, ny)) continue;
        parents[next] = at;
        queue[tail++] = next;
      }
    }
    if (parents[end] < 0)
      throw new IllegalStateException(
          "Settlement road is disconnected: " + ax + "," + ay + " -> " + bx + "," + by);
    for (int at = end; ; at = parents[at]) {
      int x = at % width, y = at / width;
      for (int dy = -2; dy <= 2; dy++)
        for (int dx = -2; dx <= 2; dx++) {
          int tx = x + dx, ty = y + dy;
          if (owned(tx, ty) && walkable(tx, ty) && map.getDecorSpriteName(tx, ty) == null) {
            setGround(tx, ty, "EarthTile");
            roads.set(index(tx, ty));
          }
        }
      if (at == start) break;
    }
  }

  private void planHunting() {
    for (int y = 500; y < map.getHeight(); y++)
      for (int x = 3300; x < width; x++)
        if (ghostTree(map.getDecorSpriteName(x, y))) markCircle(ghostCanopies, x, y, 24);
    Random rng = new Random(20261024L);
    for (Hunt h : HUNTS) {
      int placed = 0;
      for (int attempt = 0; attempt < 40000 && placed < h.count(); attempt++) {
        double a = rng.nextDouble() * Math.PI * 2;
        double r = Math.sqrt(rng.nextDouble()) * h.radius();
        int x = h.x() + (int) (Math.cos(a) * r), y = h.y() + (int) (Math.sin(a) * r);
        if (Math.hypot(x - 5650, y - 2290) < 100) continue;
        if (inBounds(x, y) && (DeathSafe(x, y) || ghostCanopies.get(index(x, y)))) continue;
        if (x >= 3988 && x <= 4097 && y >= 1433 && y <= 1552) continue;
        if (!owned(x, y)
            || !walkable(x, y)
            || reserved.get(index(x, y))
            || roads.get(index(x, y))
            || !clearance(x, y, 3)) continue;
        if (forts.stream()
            .anyMatch(s -> Math.abs(s.x() - x) + Math.abs(s.y() - y) < s.radius() + 8)) continue;
        if (HAMLETS.stream().anyMatch(s -> Math.hypot(x - s.x(), y - s.y()) < 50)) continue;
        if (SpawnRegistry.npcs().stream()
            .anyMatch(s -> s.z() == 0 && Math.hypot(x - s.x(), y - s.y()) < 50)) continue;
        if (DivinePantheon.all().stream().anyMatch(g -> Math.hypot(x - g.x(), y - g.y()) < 28))
          continue;
        if (hunting.stream().anyMatch(s -> Math.hypot(x - s.x(), y - s.y()) < 16)) continue;
        hunting.add(new SpawnDefinition(h.type(), x, y, 0, false, h.aggressive()));
        reserve(x, y, 8);
        placed++;
      }
      if (placed < h.count())
        throw new IllegalStateException(
            "Insufficient hunting space for " + h.type() + ": " + placed);
    }
    placeCastleGuards(rng);
    for (var point : SpawnRegistry.monsters())
      if (point.z() == 0) markCircle(encounterClearings, point.x(), point.y(), 24);
    for (var point : hunting) markCircle(encounterClearings, point.x(), point.y(), 24);
  }

  private void placeCastleGuards(Random rng) {
    Map<String, String> garrisons =
        Map.of(
            "Oathwood Castle",
            "Fey Warden",
            "Sanctuary East Keep",
            "Veilbound Wraith",
            "Moonwake Castle",
            "Moonwake Revenant",
            "Emberglass Citadel",
            "Emberglass Ashguard",
            "Sable Fen Watch",
            "Sundered Sentinel",
            "Bellward Castle",
            "Ashbound Exile",
            "Black Oath Keep",
            "Nullguard",
            "Regent's Bastion",
            "Rift Wraith",
            "Drakewatch Bastion",
            "Greater Drake");
    for (Site site : forts) {
      String type = garrisons.get(site.name());
      if (type == null) continue;
      int placed = 0;
      for (int attempt = 0; attempt < 5000 && placed < 3; attempt++) {
        int x = site.x() + rng.nextInt(site.radius() * 2) - site.radius();
        int y = site.y() + rng.nextInt(site.radius() * 2) - site.radius();
        if (Math.abs(x - site.x()) + Math.abs(y - site.y()) > site.radius() - 9
            || !clearance(x, y, 3)
            || reserved.get(index(x, y))
            || ghostCanopies.get(index(x, y))) continue;
        if (hunting.stream().anyMatch(s -> Math.hypot(x - s.x(), y - s.y()) < 16)) continue;
        if (SpawnRegistry.npcs().stream()
            .anyMatch(n -> n.z() == 0 && Math.hypot(x - n.x(), y - n.y()) < 50)) continue;
        for (int i = hunting.size() - 1; i >= 0; i--) {
          SpawnDefinition old = hunting.get(i);
          if (old.type().equals(type)
              && forts.stream()
                  .noneMatch(
                      f -> Math.abs(old.x() - f.x()) + Math.abs(old.y() - f.y()) < f.radius())) {
            hunting.set(i, new SpawnDefinition(type, x, y, 0, false, old.aggressive()));
            reserve(x, y, 8);
            placed++;
            break;
          }
        }
      }
      if (placed == 0) throw new IllegalStateException("Castle has no garrison: " + site.name());
    }
  }

  private void landscape() {
    for (int y = 540; y < 2960; y += 9)
      for (int x = 2220; x < 6070; x += 9) {
        int hash = Math.floorMod(x * 7349 + y * 1933, 1009);
        int tx = x + hash % 5 - 2, ty = y + hash / 7 % 5 - 2;
        if (!owned(tx, ty)
            || !walkable(tx, ty)
            || reserved.get(index(tx, ty))
            || roads.get(index(tx, ty))
            || !clearance(tx, ty, 2)) continue;
        String terrain = map.getGroundSpriteName(tx, ty);
        if (terrain == null
            || terrain.startsWith("EarthTile")
            || terrain.startsWith("Town Road")
            || DeathSafe(tx, ty)) continue;
        if (forts.stream()
            .anyMatch(s -> Math.abs(tx - s.x()) + Math.abs(ty - s.y()) < s.radius() + 12)) continue;
        if (HAMLETS.stream().anyMatch(s -> Math.hypot(tx - s.x(), ty - s.y()) < 60)) continue;
        if (hash % 10 > 3) continue;
        String sprite =
            terrain.startsWith("Grass") || terrain.startsWith("64kNormalGrass")
                ? (hash % 2 == 0 ? "SmallTree1" : "SmallTree2")
                : terrain.startsWith("Dgrass") ? "DeadTree1" : hash % 2 == 0 ? "Rock1" : "Rock2";
        if (ghostTree(sprite) && encounterClearings.get(index(tx, ty))) continue;
        place(tx, ty, sprite, true);
      }
  }

  private void orchard(int cx, int cy) {
    for (int y = 0; y < 4; y++)
      for (int x = 0; x < 4; x++) {
        int tx = cx + x * 8, ty = cy + y * 8;
        if (editable(tx, ty)
            && !reserved.get(index(tx, ty))
            && !roads.get(index(tx, ty))
            && clearance(tx, ty, 2)) place(tx, ty, "AppleTree1", true);
      }
  }

  private void furniture(int x, int y, String art) {
    if (editable(x, y) && !reserved.get(index(x, y)) && !roads.get(index(x, y)))
      place(x, y, art, true);
  }

  private void validate() {
    for (var spawn : SpawnRegistry.npcs())
      if (spawn.z() == 0 && owned(spawn.x(), spawn.y()) && !walkable(spawn.x(), spawn.y()))
        throw new IllegalStateException("NPC blocked: " + spawn);
    for (var spawn : SpawnRegistry.monsters())
      if (spawn.z() == 0 && owned(spawn.x(), spawn.y()) && !walkable(spawn.x(), spawn.y()))
        throw new IllegalStateException("Monster blocked: " + spawn);
    for (var spawn : hunting)
      if (!clearance(spawn.x(), spawn.y(), 2))
        throw new IllegalStateException("Hunting spawn obstructed: " + spawn);
    for (var god : DivinePantheon.all())
      if (!walkable(god.x(), god.y()) || !walkable(god.x() + 3, god.y() + 3))
        throw new IllegalStateException("God shrine blocked: " + god.id());
    for (var location : com.perso.T4C.teleport.NamedLocations.all())
      if (location.worldZ() == 0
          && owned(location.tileX(), location.tileY())
          && !walkable(location.tileX(), location.tileY()))
        throw new IllegalStateException("Fast-travel arrival blocked: " + location.displayName());
  }

  private void reserve(int cx, int cy, int r) {
    for (int y = cy - r; y <= cy + r; y++)
      for (int x = cx - r; x <= cx + r; x++) if (inBounds(x, y)) reserved.set(index(x, y));
  }

  private void markCircle(BitSet target, int cx, int cy, int radius) {
    for (int y = cy - radius; y <= cy + radius; y++)
      for (int x = cx - radius; x <= cx + radius; x++)
        if (inBounds(x, y) && Math.hypot(x - cx, y - cy) <= radius) target.set(index(x, y));
  }

  private static boolean ghostTree(String name) {
    if (name == null) return false;
    String s = name.toLowerCase(Locale.ROOT);
    return s.contains("deadtree")
        || s.contains("deadforesttree")
        || s.contains("darktree")
        || s.contains("leaflesstree");
  }

  private boolean clearance(int x, int y, int r) {
    for (int yy = y - r; yy <= y + r; yy++)
      for (int xx = x - r; xx <= x + r; xx++) if (!walkable(xx, yy)) return false;
    return true;
  }

  private int nearestOpen(int x, int y) {
    for (int r = 0; r < 15; r++)
      for (int yy = y - r; yy <= y + r; yy++)
        for (int xx = x - r; xx <= x + r; xx++) if (walkable(xx, yy)) return index(xx, yy);
    return -1;
  }

  private boolean inBounds(int x, int y) {
    return x >= 0 && y >= 0 && x < width && y < map.getHeight();
  }

  public static boolean owned(int x, int y) {
    if (x >= 3300 && x < 6144 && y >= 500 && y < 2960) return true;
    return Math.hypot(x - 2380, y - 2430) < 125
        || Math.hypot(x - 2500, y - 2700) < 135
        || Math.hypot(x - 2350, y - 2900) < 140
        || Math.hypot(x - 2650, y - 2880) < 150;
  }

  private boolean walkable(int x, int y) {
    return inBounds(x, y)
        && !CollisionType.fromValue(collision[index(x, y)] & 255).isBlocksMovement();
  }

  private boolean water(int x, int y) {
    String s = inBounds(x, y) ? map.getGroundSpriteName(x, y) : null;
    return s == null || s.startsWith("Ground_Water") || s.equals("Black Tile");
  }

  private boolean DeathSafe(int x, int y) {
    int c = collision[index(x, y)] & 255;
    return c == 6 || c == 7;
  }

  private static boolean natural(String name) {
    if (name == null) return false;
    String n = name.toLowerCase(Locale.ROOT);
    return n.contains("tree")
        || n.startsWith("rock")
        || n.startsWith("stone1")
        || n.startsWith("smallrock");
  }

  private boolean editable(int x, int y) {
    if (!inBounds(x, y) || !owned(x, y) || water(x, y) || DeathSafe(x, y)) return false;
    String d = map.getDecorSpriteName(x, y);
    return d == null ? walkable(x, y) : natural(d);
  }

  private int index(int x, int y) {
    return y * width + x;
  }

  private void clearNatural(int x, int y) {
    if (natural(map.getDecorSpriteName(x, y))) {
      map.setDecorSpriteName(x, y, null);
      map.setScale(x, y, 1, 1);
      map.setOffset(x, y, 0, 0);
      map.setZOrder(x, y, 0);
      collision[index(x, y)] = 0;
    }
  }

  private void setGround(int x, int y, String family) {
    String tile = ground.tileName(family, x, y);
    if (tile == null) throw new IllegalStateException("Unknown ground " + family);
    map.setGroundSpriteName(x, y, tile);
  }

  private void place(int x, int y, String name, boolean blocking) {
    if (!owned(x, y)) throw new IllegalStateException("Edit outside new terrain");
    var tile = donors.get(name);
    if (tile == null) throw new IllegalArgumentException("Unknown scenery " + name);
    map.setDecorSpriteName(x, y, name);
    map.setScale(x, y, tile.scaleX(), tile.scaleY());
    map.setOffset(x, y, tile.offsetX(), tile.offsetY());
    map.setZOrder(x, y, tile.depth());
    if (blocking) collision[index(x, y)] = (byte) CollisionType.ABSOLUTE.getValue();
    scenery++;
  }
}
