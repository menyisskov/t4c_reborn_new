package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.spawn.HuntingGrounds;
import com.perso.T4C.spawn.SpawnRegistry;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Select connected clearings on the finished terrain; never repaints the map or moves existing
 * actors.
 */
public final class HuntingGroundsBuilder {
  private static final List<String> TYPES =
      List.of(
          "Fey Warden",
          "Moonlit Stalker",
          "Veilbound Wraith",
          "Sundered Sentinel",
          "Moonwake Revenant",
          "Emberglass Ashguard",
          "Ashbound Exile",
          "Nullguard",
          "Rift Wraith",
          "Barrow Wight",
          "Lesser Drake",
          "Greater Drake",
          "Ashfang Stalker",
          "Cinder Whelp",
          "Centaur Warrior",
          "Deep One",
          "Drowned Acolyte",
          "Kraanian Dragonguard",
          "Kraanian Wyrmling",
          "Tideclaw Crab",
          "Tideworn Reaver");
  private final MapReader terrain;
  private final byte[] collision;
  private final int width, height;
  private final BitSet protectedTiles = new BitSet();
  private final List<BitSet> components = new ArrayList<>();
  private final List<HuntingGrounds.Ground> grounds = new ArrayList<>();
  private final List<HuntingGrounds.Camp> camps = new ArrayList<>();

  private HuntingGroundsBuilder(MapReader terrain) throws Exception {
    this.terrain = terrain;
    var map = CollisionMapIO.read(new File("assets/maps/worldmap/worldmap.colbin"));
    collision = map.getData();
    width = map.getWidth();
    height = map.getHeight();
    for (var npc : SpawnRegistry.npcs())
      if (npc.z() == 0) protect(npc.x(), npc.y(), npc.x() >= 3072 ? 80 : 45);
    for (var god : com.perso.T4C.divine.DivinePantheon.all()) protect(god.x(), god.y(), 80);
    for (var spawn : SpawnRegistry.monsters())
      if (spawn.z() == 0
          && spawn.x() >= 3072
          && !TYPES.contains(spawn.type())
          && !spawn.type().startsWith("Training ")
          && !HuntingGrounds.isHuntingType(spawn.type())) protect(spawn.x(), spawn.y(), 65);
    for (var location : com.perso.T4C.teleport.NamedLocations.all())
      if (location.worldZ() == 0
          && HuntingGrounds.all().stream()
              .noneMatch(g -> g.x() == location.tileX() && g.y() == location.tileY())
          && HuntingGrounds.camps().stream()
              .noneMatch(c -> c.x() == location.tileX() && c.y() == location.tileY()))
        protect(location.tileX(), location.tileY(), 55);
    for (int y = 0; y < height; y++)
      for (int x = 3072; x < width; x++) {
        String art = terrain.getDecorSpriteName(x, y);
        if (art != null
            && (art.startsWith("DeadTree")
                || art.startsWith("DeadForestTree")
                || art.startsWith("DarkTree")
                || art.startsWith("LeafLessTree"))) protect(x, y, 24);
      }
  }

  private void protect(int x, int y, int radius) {
    for (int yy = Math.max(0, y - radius); yy <= Math.min(height - 1, y + radius); yy++)
      for (int xx = Math.max(0, x - radius); xx <= Math.min(width - 1, x + radius); xx++)
        if ((xx - x) * (xx - x) + (yy - y) * (yy - y) <= radius * radius)
          protectedTiles.set(yy * width + xx);
  }

  private boolean walk(int x, int y) {
    if (x < 2 || y < 2 || x >= width - 2 || y >= height - 2) return false;
    int c = collision[y * width + x] & 255;
    return c != 1 && c != 2 && c != 3 && c != 4 && c != 9;
  }

  private boolean usable(int x, int y) {
    if (!walk(x, y) || protectedTiles.get(y * width + x)) return false;
    int c = collision[y * width + x] & 255;
    if (c == 6 || c == 7) return false;
    String ground = terrain.getGroundSpriteName(x, y);
    return ground != null
        && !ground.equals("Black Tile")
        && !ground.startsWith("Ground_Water")
        && walk(x - 1, y)
        && walk(x + 1, y)
        && walk(x, y - 1)
        && walk(x, y + 1);
  }

  private BitSet component(int x, int y) {
    for (BitSet existing : components) if (existing.get(y * width + x)) return existing;
    BitSet seen = flood(x, y, width + height);
    components.add(seen);
    return seen;
  }

  private BitSet flood(int x, int y, int radius) {
    BitSet seen = new BitSet(collision.length);
    int[] queue =
        new int
            [Math.min(collision.length, (radius * 2 + 1) * (long) (radius * 2 + 1))
                    > Integer.MAX_VALUE
                ? collision.length
                : (int) Math.min(collision.length, (radius * 2 + 1) * (long) (radius * 2 + 1))];
    int head = 0, tail = 0;
    queue[tail++] = y * width + x;
    seen.set(y * width + x);
    while (head < tail) {
      int at = queue[head++], xx = at % width, yy = at / width;
      for (int[] delta : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
        int nx = xx + delta[0], ny = yy + delta[1];
        if (Math.abs(nx - x) > radius || Math.abs(ny - y) > radius || !walk(nx, ny)) continue;
        int next = ny * width + nx;
        if (!seen.get(next)) {
          seen.set(next);
          queue[tail++] = next;
        }
      }
    }
    return seen;
  }

  private boolean inQuest(String type, int x, int y) {
    return QuestRegistry.load().stream()
        .filter(q -> type.equals(q.getTargetMonster()) && q.getTargetWorldZ() == 0)
        .allMatch(
            q ->
                Math.hypot(x - q.getAreaCenterX(), y - q.getAreaCenterY())
                    <= q.getAreaRadiusTiles());
  }

  private void field(String type) {
    var seeds =
        SpawnRegistry.monsters().stream()
            .filter(
                s ->
                    s.z() == 0
                        && type.equals(s.type())
                        && walk(s.x(), s.y())
                        && inQuest(type, s.x(), s.y())
                        && HuntingGrounds.at(type, s.x(), s.y()) == null)
            .toList();
    if (seeds.isEmpty()) throw new IllegalStateException("No connected seed for " + type);
    Set<Integer> oldAnchors = new HashSet<>();
    seeds.forEach(s -> oldAnchors.add(s.y() * width + s.x()));
    for (var seed : seeds) {
      BitSet region = component(seed.x(), seed.y());
      for (int radius = 0; radius <= 144; radius += 12)
        for (int dy = -radius; dy <= radius; dy += 12)
          for (int dx = -radius; dx <= radius; dx += 12) {
            if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) continue;
            int x = seed.x() + dx, y = seed.y() + dy;
            if (!usable(x, y)
                || !region.get(y * width + x)
                || grounds.stream().anyMatch(g -> Math.hypot(g.x() - x, g.y() - y) < 85)) continue;
            List<HuntingGrounds.Point> points = new ArrayList<>();
            for (int py = type.equals("Drowned Acolyte") ? -32 : -20;
                py <= (type.equals("Drowned Acolyte") ? 32 : 20);
                py += type.equals("Drowned Acolyte") ? 2 : 4)
              for (int px = type.equals("Drowned Acolyte") ? -32 : -20;
                  px <= (type.equals("Drowned Acolyte") ? 32 : 20);
                  px += type.equals("Drowned Acolyte") ? 2 : 4)
                if (!oldAnchors.contains((y + py) * width + x + px)
                    && usable(x + px, y + py)
                    && region.get((y + py) * width + x + px)
                    && inQuest(type, x + px, y + py))
                  points.add(new HuntingGrounds.Point(x + px, y + py));
            if (points.size() < HuntingGrounds.PACK_SIZE) continue;
            BitSet local = flood(x, y, type.equals("Drowned Acolyte") ? 40 : 28);
            points.removeIf(p -> !local.get(p.y() * width + p.x()));
            if (points.size() < HuntingGrounds.PACK_SIZE
                || local.cardinality() < (type.equals("Drowned Acolyte") ? 300 : 600)) continue;
            // Sample across the whole clearing rather than placing every anchor in its first rows.
            List<HuntingGrounds.Point> chosen = new ArrayList<>();
            for (int i = 0; i < HuntingGrounds.PACK_SIZE; i++)
              chosen.add(points.get(i * points.size() / HuntingGrounds.PACK_SIZE));
            grounds.add(new HuntingGrounds.Ground(type, x, y, List.copyOf(chosen)));
            System.out.println(type + " at " + x + "," + y);
            return;
          }
    }
    throw new IllegalStateException("No safe pack clearing for " + type);
  }

  private void camp(int tier, int desiredX, int desiredY, int rootX, int rootY) {
    BitSet region = component(rootX, rootY);
    for (int radius = 0; radius <= 100; radius += 5)
      for (int dy = -radius; dy <= radius; dy += 5)
        for (int dx = -radius; dx <= radius; dx += 5) {
          if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) continue;
          int x = desiredX + dx, y = desiredY + dy;
          List<HuntingGrounds.Point> bosses =
              List.of(
                  new HuntingGrounds.Point(x - 22, y + 25),
                  new HuntingGrounds.Point(x + 22, y + 25),
                  new HuntingGrounds.Point(x, y + 50));
          if (!usable(x, y)
              || !region.get(y * width + x)
              || bosses.stream()
                  .anyMatch(p -> !usable(p.x(), p.y()) || !region.get(p.y() * width + p.x())))
            continue;
          List<HuntingGrounds.Point> approach = new ArrayList<>(bosses);
          approach.add(new HuntingGrounds.Point(x, y));
          if (SpawnRegistry.monsters().stream()
              .anyMatch(
                  m ->
                      m.z() == 0
                          && m.aggressive()
                          && HuntingGrounds.at(m.type(), m.x(), m.y()) == null
                          && approach.stream()
                              .anyMatch(p -> Math.hypot(m.x() - p.x(), (m.y() - p.y()) * .5) < 45)))
            continue;
          BitSet local = flood(x, y, 65);
          if (bosses.stream().anyMatch(p -> !local.get(p.y() * width + p.x()))) continue;
          camps.add(new HuntingGrounds.Camp(tier, x, y, bosses));
          protect(x, y, 105);
          for (var boss : bosses) protect(boss.x(), boss.y(), 90);
          System.out.println("Training " + tier + " at " + x + "," + y);
          return;
        }
    throw new IllegalStateException("No accessible training camp " + tier);
  }

  public static void main(String[] args) throws Exception {
    try (var map = new MapReader(new File("assets/maps/worldmap/worldmap.mapbin"))) {
      var builder = new HuntingGroundsBuilder(map);
      builder.camp(1, 3810, 1580, 4040, 1477);
      builder.camp(2, 5385, 1220, 5505, 1150);
      for (String type : TYPES) builder.field(type);
      Files.writeString(
          Path.of("assets/maps/worldmap/hunting-grounds.json"),
          new GsonBuilder()
                  .setPrettyPrinting()
                  .create()
                  .toJson(new HuntingGrounds.Catalog(builder.grounds, builder.camps))
              + "\n");
    }
  }
}
