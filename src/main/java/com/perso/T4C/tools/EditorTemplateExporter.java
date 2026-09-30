package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.editor.build.MapStamp;
import com.perso.T4C.editor.build.WallStyle;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.MapReader;
import java.io.File;
import java.nio.file.*;
import java.util.*;

/** Rebuilds the bundled scenery library from checked-in maps; never modifies source maps. */
public final class EditorTemplateExporter {
  private EditorTemplateExporter() {}

  private static final Path OUTPUT = Path.of("assets/editor");

  public static void main(String[] args) throws Exception {
    Files.createDirectories(OUTPUT.resolve("templates"));
    try (var map = new MapReader(new File(MapDefinition.WORLDMAP.getMapPath()), true)) {
      byte[] collision =
          CollisionMapIO.read(new File(MapDefinition.WORLDMAP.getCollisionPath())).getData();
      export(
          map,
          collision,
          "temple",
          "buildings",
          "worldmap",
          LighthavenSanctuaryBuilder.TEMPLE_POLYGON);
      export(
          map,
          collision,
          "cottage",
          "buildings",
          "worldmap",
          new int[][] {{2939, 1117}, {2947, 1125}, {2938, 1134}, {2930, 1126}});
      export(
          map,
          collision,
          "storehouse",
          "buildings",
          "worldmap",
          new int[][] {{2928, 1132}, {2935, 1139}, {2927, 1147}, {2920, 1140}});
      var style =
          new WallStyle(
              "Lighthaven brick",
              MapStamp.Tile.read(map, collision, 2970, 1038),
              MapStamp.Tile.read(map, collision, 2949, 1033),
              MapStamp.Tile.read(map, collision, 2950, 1032),
              MapStamp.Tile.read(map, collision, 2972, 1040),
              MapStamp.Tile.read(map, collision, 2951, 1033),
              MapStamp.Tile.read(map, collision, 2925, 1057));
      Files.writeString(
          OUTPUT.resolve("brick-wall.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(style) + "\n");
    }
    try (var map = new MapReader(new File(MapDefinition.DUNGEON.getMapPath()), true)) {
      byte[] collision =
          CollisionMapIO.read(new File(MapDefinition.DUNGEON.getCollisionPath())).getData();
      export(
          map,
          collision,
          "underground",
          "underground",
          "dungeon",
          new int[][] {{305, 371}, {340, 406}, {320, 426}, {285, 391}});
    }
    try (var map = new MapReader(new File(MapDefinition.CAVERN.getMapPath()), true)) {
      byte[] collision =
          CollisionMapIO.read(new File(MapDefinition.CAVERN.getCollisionPath())).getData();
      // A passage module with open ends, ready to join to further cave sections.
      export(
          map,
          collision,
          "cavern",
          "caves",
          "cavern",
          new int[][] {{278, 368}, {320, 368}, {320, 416}, {278, 416}});
    }
  }

  private static void export(
      MapReader map, byte[] collision, String id, String category, String source, int[][] polygon)
      throws Exception {
    int x = Arrays.stream(polygon).mapToInt(p -> p[0]).min().orElseThrow();
    int y = Arrays.stream(polygon).mapToInt(p -> p[1]).min().orElseThrow();
    int w = Arrays.stream(polygon).mapToInt(p -> p[0]).max().orElseThrow() - x + 1;
    int h = Arrays.stream(polygon).mapToInt(p -> p[1]).max().orElseThrow() - y + 1;
    var stamp =
        MapStamp.capture(
            map,
            collision,
            "${editor.build.template_" + id + "}",
            category,
            source + " (" + x + ", " + y + ")",
            x,
            y,
            w,
            h,
            (tx, ty) -> AvalonSanctuaryBuilder.insidePolygon(polygon, tx, ty));
    Files.writeString(
        OUTPUT.resolve("templates/" + id + ".json"),
        new GsonBuilder().setPrettyPrinting().create().toJson(stamp) + "\n");
    System.out.println(id + ": " + stamp.cells().size() + " cells");
  }
}
