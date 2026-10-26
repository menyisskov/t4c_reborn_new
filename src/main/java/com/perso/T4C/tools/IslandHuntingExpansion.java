package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.perso.T4C.divine.DivinePantheon;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionType;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.spawn.HuntingGrounds;
import com.perso.T4C.spawn.SpawnRegistry;
import com.perso.T4C.teleport.NamedLocations;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Adds spaced anchors on connected native terrain while retaining every existing encounter. */
public final class IslandHuntingExpansion {
  private final MapReader terrain;
  private final byte[] collision;
  private final int width, height;

  private IslandHuntingExpansion(MapReader terrain) throws Exception {
    this.terrain = terrain;
    var map = CollisionMapIO.read(new File("assets/maps/worldmap/worldmap.colbin"));
    collision = map.getData();
    width = map.getWidth();
    height = map.getHeight();
  }

  private boolean walk(int x, int y) {
    return x >= 1
        && y >= 1
        && x < width - 1
        && y < height - 1
        && !CollisionType.fromValue(collision[y * width + x] & 255).isBlocksMovement();
  }

  private boolean clearOfGhostCanopy(int x, int y) {
    for (int dy = -23; dy <= 23; dy++) {
      for (int dx = -23; dx <= 23; dx++) {
        if (dx * dx + dy * dy >= 24 * 24) continue;
        String decor = terrain.getDecorSpriteName(x + dx, y + dy);
        if (decor == null) continue;
        String name = decor.toLowerCase(Locale.ROOT);
        if (name.contains("deadtree") || name.contains("deadforesttree")
            || name.contains("darktree") || name.contains("leaflesstree")) return false;
      }
    }
    return true;
  }

  private boolean usable(HuntingGrounds.Ground field, int x, int y) {
    if (!clearOfGhostCanopy(x, y)) return false;
    if (!walk(x, y) || !walk(x - 1, y) || !walk(x + 1, y) || !walk(x, y - 1) || !walk(x, y + 1))
      return false;
    int c = collision[y * width + x] & 255;
    if (c == 6 || c == 7 || c == 16) return false;
    String ground = terrain.getGroundSpriteName(x, y);
    if (ground == null || ground.equals("Black Tile") || ground.startsWith("Ground_Water"))
      return false;
    for (var npc : SpawnRegistry.npcs())
      if (npc.z() == 0 && Math.hypot(npc.x() - x, npc.y() - y) < 55) return false;
    for (var god : DivinePantheon.all())
      if (Math.hypot(god.x() - x, god.y() - y) < 55) return false;
    for (var spawn : SpawnRegistry.monsters())
      if (spawn.z() == 0
          && !HuntingGrounds.isHuntingType(spawn.type())
          && !Objects.equals(field.bossType(), spawn.type())
          && Math.hypot(spawn.x() - x, spawn.y() - y) < 55) return false;
    for (var location : NamedLocations.all()) {
      boolean huntingLanding =
          HuntingGrounds.all().stream()
              .anyMatch(g -> g.x() == location.tileX() && g.y() == location.tileY());
      if (location.worldZ() == 0
          && !huntingLanding
          && Math.hypot(location.tileX() - x, location.tileY() - y) < 55) return false;
    }
    return QuestRegistry.load().stream()
        .filter(q -> field.monster().equals(q.getTargetMonster()) && q.getTargetWorldZ() == 0)
        .allMatch(
            q ->
                Math.hypot(x - q.getAreaCenterX(), y - q.getAreaCenterY())
                    <= q.getAreaRadiusTiles());
  }

  private Set<HuntingGrounds.Point> connected(HuntingGrounds.Ground field) {
    Set<HuntingGrounds.Point> reached = new HashSet<>();
    ArrayDeque<HuntingGrounds.Point> queue = new ArrayDeque<>();
    var root = new HuntingGrounds.Point(field.x(), field.y());
    reached.add(root);
    queue.add(root);
    while (!queue.isEmpty()) {
      var p = queue.remove();
      for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
        var next = new HuntingGrounds.Point(p.x() + d[0], p.y() + d[1]);
        if (Math.abs(next.x() - root.x()) <= 56
            && Math.abs(next.y() - root.y()) <= 56
            && walk(next.x(), next.y())
            && reached.add(next)) queue.add(next);
      }
    }
    return reached;
  }

  private HuntingGrounds.Ground expand(HuntingGrounds.Ground field) {
    if (field.x() < 3072) return field;
    List<HuntingGrounds.Point> chosen = new ArrayList<>(field.points().stream()
        .filter(p -> clearOfGhostCanopy(p.x(), p.y())).toList());
    if (chosen.size() >= field.targetPopulation()) return field;
    Set<HuntingGrounds.Point> reachable = connected(field);
    List<HuntingGrounds.Point> candidates = new ArrayList<>();
    for (int dy = -48; dy <= 48; dy += 4)
      for (int dx = -48; dx <= 48; dx += 4) {
        var point = new HuntingGrounds.Point(field.x() + dx, field.y() + dy);
        if (reachable.contains(point) && usable(field, point.x(), point.y())) candidates.add(point);
      }
    // Farthest-point sampling spreads the extra creatures over a larger area.
    while (chosen.size() < field.targetPopulation()) {
      HuntingGrounds.Point best = null;
      double separation = 9;
      for (var candidate : candidates) {
        double distance =
            chosen.stream()
                .mapToDouble(
                    p -> Math.pow(p.x() - candidate.x(), 2) + Math.pow(p.y() - candidate.y(), 2))
                .min()
                .orElse(0);
        if (distance > separation) {
          separation = distance;
          best = candidate;
        }
      }
      if (best == null)
        throw new IllegalStateException(
            "Insufficient safe terrain for " + field.monster() + ": " + chosen.size());
      chosen.add(best);
      candidates.remove(best);
    }
    System.out.println(field.monster() + ": " + field.points().size() + " -> " + chosen.size());
    return new HuntingGrounds.Ground(
        field.monster(),
        field.x(),
        field.y(),
        List.copyOf(chosen),
        field.trainingTier(),
        field.family());
  }

  public static void main(String[] args) throws Exception {
    try (var terrain = new MapReader(new File("assets/maps/worldmap/worldmap.mapbin"))) {
      var builder = new IslandHuntingExpansion(terrain);
      List<HuntingGrounds.Ground> expanded =
          HuntingGrounds.all().stream().map(builder::expand).toList();
      var catalog = new HuntingGrounds.Catalog(expanded, HuntingGrounds.camps());
      Path destination =
          Path.of(
              args.length > 0 && args[0].equals("--apply")
                  ? "assets/maps/worldmap/hunting-grounds.json"
                  : "target/island-hunting-expansion.json");
      Files.writeString(
          destination, new GsonBuilder().setPrettyPrinting().create().toJson(catalog) + "\n");
      System.out.println("Wrote " + destination);
    }
  }
}
