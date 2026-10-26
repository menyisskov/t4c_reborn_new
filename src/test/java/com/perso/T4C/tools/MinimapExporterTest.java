package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.perso.T4C.spawn.HuntingGrounds;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinimapExporterTest {
  @TempDir Path scratch;

  @Test
  void edgeCropsStayInsideTheNativeWorld() {
    var crop = new MinimapExporter.ZoneCrop("edge", 100, 2900, 200, 6144, 3072);
    assertEquals(0, crop.minX);
    assertEquals(2650, crop.minY);
    assertEquals(350, crop.w);
    assertEquals(422, crop.h);
    assertTrue(crop.contains(100, 3071));
    assertTrue(!crop.contains(100, 3072));
  }

  @Test
  void websiteMapsCoverEveryAuthoredHuntingAnchor() throws Exception {
    var crops = MinimapExporter.loadZoneCrops(Path.of("compendium/data/zones.json"), 6144, 3072);
    for (var ground : HuntingGrounds.all()) {
      for (var point : ground.points()) {
        assertTrue(
            crops.stream().anyMatch(crop -> crop.contains(point.x(), point.y())),
            () -> ground.monster() + " has an unmapped hunting anchor at " + point);
      }
    }
  }

  @Test
  void mapsOnlyRebundlePreservesExistingDataAndLongValues() throws Exception {
    var data = Files.createDirectories(scratch.resolve("data"));
    String monsters = "[{\"name\":\"Existing\",\"xpOnDeath\":9007199254740993}]";
    String maps = "[{\"zoneId\":\"new-island\",\"image\":\"maps/new-island.png\"}]";
    Files.writeString(data.resolve("monsters.json"), monsters);
    Files.writeString(data.resolve("maps.json"), maps);
    CompendiumExporter.bundleExistingData(data);
    String js = Files.readString(scratch.resolve("data.js"));
    var bundle =
        JsonParser.parseString(js.substring(js.indexOf("= ") + 2, js.lastIndexOf(';')))
            .getAsJsonObject();
    assertEquals(JsonParser.parseString(monsters), bundle.get("monsters"));
    assertEquals(JsonParser.parseString(maps), bundle.get("maps"));
    assertEquals(monsters, Files.readString(data.resolve("monsters.json")));
  }
}
