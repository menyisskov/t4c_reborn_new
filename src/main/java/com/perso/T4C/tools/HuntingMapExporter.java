package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.spawn.HuntingGrounds;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.imageio.ImageIO;

/** Actual terrain palette and runtime coordinates for the hunting atlas. */
public final class HuntingMapExporter {
  private HuntingMapExporter() {}

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    try (var map = MapReader.spriteNamesOnly(new File("assets/maps/worldmap/worldmap.mapbin"))) {
      int step = 4, w = map.getWidth() / step, h = map.getHeight() / step;
      Set<String> needed = new HashSet<>();
      for (int y = 0; y < h; y++)
        for (int x = 0; x < w; x++) {
          String sprite = map.getGroundSpriteName(x * step, y * step);
          if (sprite != null) needed.add(SpriteBinIO.key(sprite));
        }
      var colors = MinimapExporter.sampleColors(needed);
      var image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      for (int y = 0; y < h; y++)
        for (int x = 0; x < w; x++) {
          String sprite = map.getGroundSpriteName(x * step, y * step);
          int[] color =
              sprite == null
                  ? new int[] {20, 45, 60}
                  : colors.getOrDefault(SpriteBinIO.key(sprite), new int[] {20, 45, 60});
          image.setRGB(x, y, (color[0] << 16) | (color[1] << 8) | color[2]);
        }
      ImageIO.write(image, "png", new File("docs/images/hunting-world-terrain.png"));
      List<Map<String, Object>> sites = new ArrayList<>();
      int index = 0;
      for (var ground : HuntingGrounds.all()) {
        var row = new LinkedHashMap<String, Object>();
        row.put("id", "hunt-" + index);
        row.put("label", Integer.toString(++index));
        row.put("name", ground.monster());
        row.put("x", ground.x());
        row.put("y", ground.y());
        row.put("kind", ground.trainingTier() > 0 ? "training" : "hunting");
        if (ground.trainingTier() > 0) {
          row.put("armor", ground.trainingTier() == 1 ? 150 : 250);
          row.put("health", ground.trainingTier() == 1 ? 100000 : 250000);
          row.put("gold", ground.trainingTier() == 1 ? "100,000-125,000" : "200,000-250,000");
          row.put(
              "xp",
              String.format(
                  Locale.ROOT,
                  "%,d",
                  Math.round(
                      com.perso.T4C.monster.core.MonsterExperience.reward(
                              com.perso
                                  .T4C
                                  .spawn
                                  .TrainingGroundContent
                                  .boss(ground.trainingTier(), ground.family())
                                  .getXpOnDeath())
                          * (double) com.perso.T4C.config.GameConstants.SERVER_XP_RATE)));
        }
        row.put("population", ground.points().size());
        row.put("level", MonsterRegistry.findByName(ground.monster()).getLevel());
        row.put(
            "region",
            ground.x() >= 5120
                ? "threnody"
                : ground.x() >= 3072
                    ? "witness"
                    : ground.x() >= 2100 && ground.y() >= 2300 ? "kraanhold" : "mainland");
        sites.add(row);
      }
      Files.writeString(
          Path.of("target/hunting-atlas-data.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(sites));
      System.out.println("Exported terrain and " + sites.size() + " hunting/training locations");
    }
  }
}
