package com.perso.T4C.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Audit and loader for the exact set of world-0 authored monster spawns no creature can stand on
 * (WORLD_STREAMING_DESIGN.md 3.4, risk L13). A persisted creature must start on a tile the hosted
 * collision rules let it stand on, otherwise startup would refuse; the streamed world-0 plan therefore
 * leaves exactly these spawns out. The list is the bundled resource {@code /content/unhostable-spawns-v1.json},
 * produced by this tool and recomputed by {@code UnhostableSpawnsTest} from the real collision file, so it
 * can neither miss an unstandable spawn nor list a standable one.
 *
 * Usage: {@code UnhostableSpawnAudit COLLISION_FILE [OUTPUT_JSON]}.
 */
public final class UnhostableSpawnAudit {
  static final String RESOURCE = "/content/unhostable-spawns-v1.json";
  static final int VERSION = 1;

  public record Entry(String key, int x, int y, String reason) {}
  /**
   * {@code footBlocked}: anchors on a tile the foot rule blocks (the design's 153); {@code noSprite}: no walk
   * pattern or no definition, which the creature world refuses to host. One reason per key, in that order. A
   * creature's home answers to single-player's foot rule only (T4C-0372): a wall directly above the tile does not
   * matter, as it does not for the player standing there.
   */
  public record Document(int version, int world, int footBlocked, int noSprite, List<Entry> entries) {}

  private UnhostableSpawnAudit() {}

  /** Recomputes the list from the real world-0 collision file and the bundled monster content. */
  public static Document audit(Path worldCollision) throws IOException {
    var grid = SharedCollision.load(worldCollision, 0, List.of());
    var spec = new RegionMapLoader.Spec(0, "start", 0, 0, HostedRegionPlan.WORLD0_WIDTH, HostedRegionPlan.WORLD0_HEIGHT, 2944, 1059, true);
    var map = RegionMapLoader.loadStreamed(grid, spec);
    var monsters = HostedWorlds.withoutNpcOwnedSpawns(MonsterCatalog.loadBundled());
    var entries = new ArrayList<Entry>();
    int foot = 0, noSprite = 0;
    for (var spawn : monsters.region(0, 0, 0, spec.width(), spec.height())) {
      var definition = spawn.definition() == null ? null : monsters.monsters().get(spawn.definition());
      if (map.blocksFoot(spawn.x(), spawn.y())) { foot++; entries.add(new Entry(spawn.persistenceKey(), spawn.x(), spawn.y(), "foot_blocked")); }
      else if (definition == null || definition.getWalkPattern() == null || definition.getWalkPattern().isBlank()) {
        noSprite++;
        entries.add(new Entry(spawn.persistenceKey(), spawn.x(), spawn.y(), definition == null ? "no_definition" : "no_walk_pattern"));
      }
    }
    entries.sort(Comparator.comparing(Entry::key));
    return new Document(VERSION, 0, foot, noSprite, List.copyOf(entries));
  }

  /** The bundled list. */
  public static Document bundled() throws IOException {
    try (InputStream in = UnhostableSpawnAudit.class.getResourceAsStream(RESOURCE)) {
      if (in == null) throw new IOException("Missing unhostable spawn list");
      var bytes = in.readNBytes(4 * 1024 * 1024 + 1);
      if (bytes.length > 4 * 1024 * 1024) throw new IOException("Unhostable spawn list too large");
      var document = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), Document.class);
      if (document == null || document.version() != VERSION || document.entries() == null)
        throw new IOException("Invalid unhostable spawn list");
      return document;
    }
  }

  static Set<String> bundledKeys() throws IOException {
    return bundled().entries().stream().map(Entry::key).collect(Collectors.toUnmodifiableSet());
  }

  static String toJson(Document document) {
    return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(document) + "\n";
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 1) throw new IllegalArgumentException("Usage: UnhostableSpawnAudit COLLISION_FILE [OUTPUT_JSON]");
    var json = toJson(audit(Path.of(args[0])));
    if (args.length > 1) Files.writeString(Path.of(args[1]), json, StandardCharsets.UTF_8);
    else System.out.print(json);
  }
}
