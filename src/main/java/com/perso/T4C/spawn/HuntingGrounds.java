package com.perso.T4C.spawn;

import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Authored dense fields: one creature per anchor, independent of the global spawn multiplier. */
public final class HuntingGrounds {
  public static final int PACK_SIZE = 40;
  public static final int NEW_ISLAND_PACK_SIZE = 100;

  public record Point(int x, int y) {}

  public record Ground(
      String monster, int x, int y, List<Point> points, int trainingTier, String family) {
    public Ground(String monster, int x, int y, List<Point> points) {
      this(monster, x, y, points, 0, null);
    }

    public int targetPopulation() {
      return x >= 3072 ? NEW_ISLAND_PACK_SIZE : PACK_SIZE;
    }

    public int areaRadiusTiles() {
      return x >= 3072 ? 56 : 40;
    }

    public int leashRadiusTiles() {
      return areaRadiusTiles() + 12;
    }

    public String locationKey() {
      return "hunting_" + monster.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
    }

    public String unlock() {
      if (x >= 5120) return "threnody_reach";
      if (x >= 3072) return "avalon_sanctuary";
      if (x >= 2100 && y >= 2300) return "windhowl_marches";
      return null;
    }

    public String bossType() {
      return trainingTier > 0 ? "Training " + trainingTier + " " + family : null;
    }

    public String scrollKey() {
      return "training_ground_"
          + trainingTier
          + ("Tarantula".equals(family) ? "" : "_" + family.toLowerCase(java.util.Locale.ROOT))
          + "_return_scroll";
    }

    public int familyIndex() {
      return List.of("Tarantula", "Skraug", "Skeleton").indexOf(family);
    }

    public int spellId() {
      return "Tarantula".equals(family)
          ? 19020 + trainingTier
          : 19030 + (trainingTier - 1) * 3 + familyIndex();
    }

    public int itemId() {
      return "Tarantula".equals(family)
          ? 59500 + trainingTier
          : 59510 + (trainingTier - 1) * 3 + familyIndex();
    }
  }

  public record Camp(int tier, int x, int y, List<Point> bosses) {
    public String scrollKey() {
      return "training_ground_" + tier + "_return_scroll";
    }

    public int spellId() {
      return 19020 + tier;
    }

    public String unlock() {
      return tier == 1 ? "avalon_sanctuary" : "threnody_reach";
    }
  }

  public record Catalog(List<Ground> grounds, List<Camp> camps) {}

  private static final Catalog CATALOG = read();
  private static final Map<String, Ground> BY_ANCHOR = index();

  private HuntingGrounds() {}

  private static Catalog read() {
    try {
      return new Gson()
          .fromJson(
              Files.readString(Path.of("assets/maps/worldmap/hunting-grounds.json")),
              Catalog.class);
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Cannot load hunting grounds", e);
    }
  }

  private static String key(String name, int x, int y) {
    return name + ":" + x + ":" + y;
  }

  private static Map<String, Ground> index() {
    Map<String, Ground> result = new HashMap<>();
    for (Ground ground : all())
      for (Point point : ground.points()) {
        if (result.put(key(ground.monster(), point.x(), point.y()), ground) != null)
          throw new IllegalStateException("Duplicate hunting anchor: " + ground.monster());
      }
    return Map.copyOf(result);
  }

  public static List<Ground> all() {
    return CATALOG.grounds();
  }

  public static List<Camp> camps() {
    return CATALOG.camps();
  }

  public static Ground at(String monster, int x, int y) {
    return BY_ANCHOR.get(key(monster, x, y));
  }

  public static boolean isHuntingType(String name) {
    return all().stream().anyMatch(g -> g.monster().equals(name));
  }

  public static List<SpawnDefinition> spawns() {
    return all().stream()
        .flatMap(
            g ->
                g.points().stream()
                    .map(p -> new SpawnDefinition(g.monster(), p.x(), p.y(), 0, false, true)))
        .toList();
  }

  public static int copies(String type, int x, int y, int z) {
    return z == 0 && (at(type, x, y) != null || type.startsWith("Training "))
        ? 1
        : com.perso.T4C.config.GameConstants.SPAWN_COUNT_MULTIPLIER;
  }
}
