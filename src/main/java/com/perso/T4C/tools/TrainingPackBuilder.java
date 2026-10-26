package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.spawn.HuntingGrounds;
import com.perso.T4C.spawn.SpawnRegistry;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Adds family-specific training packs without moving the existing 21 hunting clearings. */
public final class TrainingPackBuilder {
  private final MapReader terrain;
  private final int width, height;
  private final byte[] collision;
  private final BitSet blocked = new BitSet();
  private final List<HuntingGrounds.Ground> fields = new ArrayList<>();
  private final List<HuntingGrounds.Camp> camps = new ArrayList<>();

  private TrainingPackBuilder(MapReader terrain) throws Exception {
    this.terrain = terrain;
    var map = CollisionMapIO.read(new File("assets/maps/worldmap/worldmap.colbin"));
    width = map.getWidth();
    height = map.getHeight();
    collision = map.getData();
    fields.addAll(HuntingGrounds.all().stream().filter(g -> g.trainingTier() == 0).toList());
    for (var npc : SpawnRegistry.npcs())
      if (npc.z() == 0 && npc.x() >= 3072) protect(npc.x(), npc.y(), 40);
    for (var god : com.perso.T4C.divine.DivinePantheon.all()) protect(god.x(), god.y(), 50);
    for (var spawn : SpawnRegistry.monsters()) {
      if (spawn.z() != 0
          || spawn.x() < 3072
          || spawn.type().startsWith("Training ")
          || HuntingGrounds.at(spawn.type(), spawn.x(), spawn.y()) != null) continue;
      protect(spawn.x(), spawn.y(), spawn.aggressive() ? 35 : 18);
    }
    for (int y = 900; y < 1850; y++)
      for (int x = 3500; x < 5800; x++) {
        String art = terrain.getDecorSpriteName(x, y);
        if (art != null
            && (art.startsWith("DeadTree")
                || art.startsWith("DeadForestTree")
                || art.startsWith("DarkTree")
                || art.startsWith("LeafLessTree"))) protect(x, y, 24);
      }
  }

  private void protect(int x, int y, int radius) {
    for (int yy = Math.max(0, y - radius * 2); yy <= Math.min(height - 1, y + radius * 2); yy++)
      for (int xx = Math.max(0, x - radius); xx <= Math.min(width - 1, x + radius); xx++)
        if (Math.hypot(xx - x, (yy - y) * .5) <= radius) blocked.set(yy * width + xx);
  }

  private boolean walk(int x, int y) {
    if (x < 2 || y < 2 || x >= width - 2 || y >= height - 2) return false;
    int c = collision[y * width + x] & 255;
    return c != 1 && c != 2 && c != 3 && c != 4 && c != 9;
  }

  private boolean usable(int x, int y) {
    if (!walk(x, y) || blocked.get(y * width + x)) return false;
    int c = collision[y * width + x] & 255;
    String tile = terrain.getGroundSpriteName(x, y);
    return c != 6
        && c != 7
        && tile != null
        && !tile.equals("Black Tile")
        && !tile.startsWith("Ground_Water")
        && walk(x - 1, y)
        && walk(x + 1, y)
        && walk(x, y - 1)
        && walk(x, y + 1);
  }

  private BitSet flood(int x, int y, int radius) {
    BitSet seen = new BitSet(collision.length);
    int[] queue =
        new int
            [Math.min(
                collision.length,
                (int) Math.min(Integer.MAX_VALUE, (long) (radius * 2 + 1) * (radius * 2 + 1)))];
    int head = 0, tail = 0;
    queue[tail++] = y * width + x;
    seen.set(y * width + x);
    while (head < tail) {
      int at = queue[head++], px = at % width, py = at / width;
      for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
        int xx = px + d[0], yy = py + d[1];
        if (Math.abs(xx - x) > radius || Math.abs(yy - y) > radius || !walk(xx, yy)) continue;
        int next = yy * width + xx;
        if (!seen.get(next)) {
          seen.set(next);
          queue[tail++] = next;
        }
      }
    }
    return seen;
  }

  private HuntingGrounds.Point addField(
      int tier, String family, int desiredX, int desiredY, BitSet region) {
    for (int radius = 0; radius <= 220; radius += 6)
      for (int dy = -radius; dy <= radius; dy += 6)
        for (int dx = -radius; dx <= radius; dx += 6) {
          if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) continue;
          int x = desiredX + dx, y = desiredY + dy;
          if (!usable(x, y)
              || !region.get(y * width + x)
              || fields.stream()
                  .anyMatch(g -> Math.max(Math.abs(g.x() - x), Math.abs(g.y() - y)) < 90)) continue;
          HuntingGrounds.Point boss = null;
          for (int by : new int[] {8, -8, 4, -4, 0})
            if (usable(x, y + by)) {
              boss = new HuntingGrounds.Point(x, y + by);
              break;
            }
          if (boss == null) continue;
          BitSet local = flood(x, y, 30);
          if (local.cardinality() < 600 || !local.get(boss.y() * width + boss.x())) continue;
          List<HuntingGrounds.Point> points = new ArrayList<>();
          for (int py = -18; py <= 18; py += 3)
            for (int px = -18; px <= 18; px += 3)
              if (usable(x + px, y + py)
                  && region.get((y + py) * width + x + px)
                  && local.get((y + py) * width + x + px)
                  && Math.hypot(x + px - boss.x(), (y + py - boss.y()) * .5) > 5.5)
                points.add(new HuntingGrounds.Point(x + px, y + py));
          if (points.size() < HuntingGrounds.PACK_SIZE) continue;
          List<HuntingGrounds.Point> chosen = new ArrayList<>();
          for (int i = 0; i < HuntingGrounds.PACK_SIZE; i++)
            chosen.add(points.get(i * points.size() / HuntingGrounds.PACK_SIZE));
          fields.add(
              new HuntingGrounds.Ground(
                  (tier == 1 ? "Witness " : "Threnody ") + family,
                  x,
                  y,
                  List.copyOf(chosen),
                  tier,
                  family));
          System.out.println(
              tier + " " + family + " pack " + x + "," + y + " boss " + boss.x() + "," + boss.y());
          return boss;
        }
    throw new IllegalStateException("No training clearing for " + tier + " " + family);
  }

  private void island(HuntingGrounds.Camp old) {
    var region = flood(old.x(), old.y(), width + height);
    int t = old.tier();
    var tarantula = addField(t, "Tarantula", old.x(), old.y(), region);
    var skraug = addField(t, "Skraug", old.x() - 105, old.y() + (t == 1 ? 0 : 70), region);
    var skeleton =
        addField(
            t, "Skeleton", old.x() - (t == 1 ? 105 : 0), old.y() + (t == 1 ? -130 : 210), region);
    camps.add(new HuntingGrounds.Camp(t, old.x(), old.y(), List.of(tarantula, skraug, skeleton)));
  }

  public static void main(String[] args) throws Exception {
    try (var map = new MapReader(new File("assets/maps/worldmap/worldmap.mapbin"))) {
      var builder = new TrainingPackBuilder(map);
      for (var camp : HuntingGrounds.camps()) builder.island(camp);
      Files.writeString(
          Path.of("assets/maps/worldmap/hunting-grounds.json"),
          new GsonBuilder()
                  .setPrettyPrinting()
                  .create()
                  .toJson(new HuntingGrounds.Catalog(builder.fields, builder.camps))
              + "\n");
    }
  }
}
