package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.WorldMapExtension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

/** Rebuilds the in-game eastern overview from the shipped map's actual ground art. */
public final class WorldOverviewExporter {
  private WorldOverviewExporter() {}

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    try (MapReader map = MapReader.spriteNamesOnly(new File(Paths.MAP))) {
      int width = map.getWidth() - WorldMapExtension.ORIGIN_X;
      if (width <= 0) throw new IllegalStateException("World has no eastern extension");
      Set<String> keys = new HashSet<>();
      for (int y = 0; y < map.getHeight(); y++) {
        for (int x = WorldMapExtension.ORIGIN_X; x < map.getWidth(); x++) {
          String name = map.getGroundSpriteName(x, y);
          if (name != null) keys.add(SpriteBinIO.key(name));
        }
      }
      Map<String, int[]> colors = MinimapExporter.sampleColors(keys);
      BufferedImage image = new BufferedImage(width, map.getHeight(), BufferedImage.TYPE_INT_RGB);
      int[] water = {20, 45, 60};
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < width; x++) {
          String name = map.getGroundSpriteName(x + WorldMapExtension.ORIGIN_X, y);
          int[] color = name == null ? water : colors.getOrDefault(SpriteBinIO.key(name), water);
          image.setRGB(x, y, (color[0] << 16) | (color[1] << 8) | color[2]);
        }
      }
      ImageIO.write(image, "png", new File(WorldMapExtension.IMAGE_PATH));
      System.out.println(
          "Wrote " + WorldMapExtension.IMAGE_PATH + " (" + width + "x" + image.getHeight() + ")");
    }
  }
}
