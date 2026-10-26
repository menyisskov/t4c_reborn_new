package com.perso.T4C.tools;

import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.GroundMosaicCatalog;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.SpriteBinIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

/** Stages the three-boss island in empty ocean, leaving shipped map assets untouched. */
public final class ShatteredIsleBuilder {
  public static final String ZONE_ID = "shattered_isle";
  public static final int ARRIVAL_X = 3450;
  public static final int ARRIVAL_Y = 2705;
  public static final int DRAGON_X = 3360;
  public static final int DRAGON_Y = 2635;
  public static final int GRIFFIN_X = 3540;
  public static final int GRIFFIN_Y = 2635;
  public static final int HYDRA_X = 3450;
  public static final int HYDRA_Y = 2790;

  private static final int MIN_X = 3220;
  private static final int MAX_X = 3700;
  private static final int MIN_Y = 2480;
  private static final int MAX_Y = 2910;
  private static final Path SOURCE = Path.of("assets/maps/worldmap");
  private static final Path STAGE = Path.of("target/shattered-isle-stage");
  private static final Path PREVIEW =
      Path.of("docs/content-ideas/shattered-isle/island-preview.png");

  private ShatteredIsleBuilder() {}

  public static void main(String[] args) throws Exception {
    if (args.length == 1 && args[0].equals("--render-only")) {
      Files.createDirectories(PREVIEW.getParent());
      try (MapReader stage = new MapReader(STAGE.resolve("worldmap.mapbin").toFile())) {
        renderPreview(stage, PREVIEW.toFile());
      }
      System.out.println("Rendered staged island preview at " + PREVIEW);
      return;
    }
    if (args.length != 0)
      throw new IllegalArgumentException("Usage: ShatteredIsleBuilder [--render-only]");
    Files.createDirectories(STAGE);
    Files.createDirectories(PREVIEW.getParent());
    try (MapReader map = new MapReader(SOURCE.resolve("worldmap.mapbin").toFile())) {
      var collision = CollisionMapIO.read(SOURCE.resolve("worldmap.colbin").toFile());
      if (map.getWidth() != collision.getWidth() || map.getHeight() != collision.getHeight())
        throw new IOException("Terrain and collision dimensions differ");
      verifyEmptyOcean(map, collision.getData(), collision.getWidth());
      paint(map, collision.getData(), collision.getWidth());
      map.writeCompact(STAGE.resolve("worldmap.mapbin").toFile());
      CollisionMapIO.write(
          STAGE.resolve("worldmap.colbin").toFile(),
          collision.getWidth(),
          collision.getHeight(),
          collision.getData());
      renderPreview(map, PREVIEW.toFile());
    }
    System.out.println("Staged island map in " + STAGE + " and preview at " + PREVIEW);
  }

  private static void verifyEmptyOcean(MapReader map, byte[] collision, int width) {
    for (int y = MIN_Y; y <= MAX_Y; y++)
      for (int x = MIN_X; x <= MAX_X; x++) {
        String ground = map.getGroundSpriteName(x, y);
        if (ground == null
            || !ground.startsWith("Ground_Water")
            || (collision[y * width + x] & 255) != 3
            || map.getDecorSpriteName(x, y) != null)
          throw new IllegalStateException("Island footprint is occupied at " + x + "," + y);
      }
  }

  private static void paint(MapReader map, byte[] collision, int width) {
    GroundMosaicCatalog palette = GroundMosaicCatalog.load();
    for (int y = MIN_Y; y <= MAX_Y; y++)
      for (int x = MIN_X; x <= MAX_X; x++) {
        double edge = edge(x, y);
        if (edge > 1.0 || pond(x, y)) continue;
        String family = family(x, y, edge);
        String frame = palette.tileName(family, x, y);
        if (frame == null) throw new IllegalStateException("Missing ground family: " + family);
        map.setGroundSpriteName(x, y, frame);
        collision[y * width + x] = 0;
      }
    for (int[] point :
        new int[][] {
          {ARRIVAL_X, ARRIVAL_Y},
          {DRAGON_X, DRAGON_Y},
          {GRIFFIN_X, GRIFFIN_Y},
          {HYDRA_X, HYDRA_Y}
        })
      if ((collision[point[1] * width + point[0]] & 255) != 0)
        throw new IllegalStateException("Island site is blocked at " + point[0] + "," + point[1]);
  }

  private static String family(int x, int y, double edge) {
    if (edge > .91) return "RockFloor";
    if (distance(x, y, DRAGON_X, DRAGON_Y) < 99) return "RockFloor";
    if (distance(x, y, HYDRA_X, HYDRA_Y) < 101) return "Grass";
    if (distance(x, y, GRIFFIN_X, GRIFFIN_Y) < 107) return "EarthTile";
    return "EarthTile";
  }

  private static boolean pond(int x, int y) {
    return distance(x, y, 3395, 2825) < 17
        || distance(x, y, 3510, 2820) < 14
        || distance(x, y, 3488, 2850) < 8;
  }

  private static double edge(int x, int y) {
    double central = ellipse(x, y, 3450, 2710, 124, 115);
    double dragon = ellipse(x, y, 3365, 2635, 100, 83);
    double griffin = ellipse(x, y, 3535, 2635, 105, 83);
    double hydra = ellipse(x, y, 3450, 2790, 104, 91);
    double outline = Math.min(Math.min(central, dragon), Math.min(griffin, hydra));
    double wobble = .035 * Math.sin(x * .083 + y * .047) + .018 * Math.sin(x * .151 - y * .097);
    return outline + wobble;
  }

  private static double ellipse(int x, int y, int cx, int cy, int rx, int ry) {
    double dx = (x - cx) / (double) rx, dy = (y - cy) / (double) ry;
    return Math.sqrt(dx * dx + dy * dy);
  }

  private static double distance(int x, int y, int cx, int cy) {
    return Math.hypot(x - cx, y - cy);
  }

  private static void renderPreview(MapReader map, File output) throws Exception {
    Set<String> keys = new HashSet<>();
    for (int y = MIN_Y; y <= MAX_Y; y++)
      for (int x = MIN_X; x <= MAX_X; x++)
        keys.add(SpriteBinIO.key(map.getGroundSpriteName(x, y)));
    Map<String, int[]> colors = MinimapExporter.sampleColors(keys);
    int scale = 2, margin = 26, legend = 390;
    int mapWidth = (MAX_X - MIN_X + 1) * scale;
    int mapHeight = (MAX_Y - MIN_Y + 1) * scale;
    BufferedImage image =
        new BufferedImage(mapWidth + legend + margin * 3, mapHeight + margin * 2,
            BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(new Color(20, 27, 33));
    g.fillRect(0, 0, image.getWidth(), image.getHeight());
    for (int y = MIN_Y; y <= MAX_Y; y++)
      for (int x = MIN_X; x <= MAX_X; x++) {
        int[] color =
            colors.getOrDefault(SpriteBinIO.key(map.getGroundSpriteName(x, y)), new int[] {20, 45, 60});
        g.setColor(new Color(color[0], color[1], color[2]));
        g.fillRect(margin + (x - MIN_X) * scale, margin + (y - MIN_Y) * scale, scale, scale);
      }
    g.setColor(new Color(220, 227, 220));
    g.setFont(new Font("SansSerif", Font.BOLD, 18));
    g.drawString("SHATTERED ISLE", mapWidth + margin * 2, 62);
    g.setFont(new Font("SansSerif", Font.PLAIN, 13));
    g.drawString("Staged native terrain", mapWidth + margin * 2, 82);
    g.drawString("Quest: defeat 3 Kraanian Dragonguards", mapWidth + margin * 2, 105);
    g.drawString("Giver: Outrider Kaelis, Drake's Lair", mapWidth + margin * 2, 127);
    g.drawString("Reward: permanent island access", mapWidth + margin * 2, 149);
    marker(g, ARRIVAL_X, ARRIVAL_Y, "Arrival", new Color(236, 221, 155), margin, scale);
    marker(g, DRAGON_X, DRAGON_Y, "Obsidian Dragon", new Color(212, 92, 85), margin, scale);
    marker(g, GRIFFIN_X, GRIFFIN_Y, "Bronze Griffin", new Color(238, 179, 72), margin, scale);
    marker(g, HYDRA_X, HYDRA_Y, "Swamp Hydra", new Color(118, 201, 113), margin, scale);
    g.setColor(new Color(166, 180, 185));
    g.drawString("Ocean gap preserves quest access.", mapWidth + margin * 2, 207);
    g.drawString("Native rock, earth, grass and water tiles.", mapWidth + margin * 2, 229);
    g.drawString("Preview only — game map is unchanged.", mapWidth + margin * 2, 251);
    g.dispose();
    ImageIO.write(image, "png", output);
  }

  private static void marker(
      Graphics2D g, int x, int y, String name, Color color, int margin, int scale) {
    int px = margin + (x - MIN_X) * scale, py = margin + (y - MIN_Y) * scale;
    g.setColor(new Color(0, 0, 0, 150));
    g.fillOval(px - 13, py - 13, 26, 26);
    g.setColor(color);
    g.setStroke(new BasicStroke(2f));
    g.drawOval(px - 9, py - 9, 18, 18);
    g.setFont(new Font("SansSerif", Font.BOLD, 13));
    g.drawString(name, px + 15, py + 5);
  }
}
