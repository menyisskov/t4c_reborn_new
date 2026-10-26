package com.perso.T4C.tools;

import com.perso.T4C.divine.DivinePantheon;
import com.perso.T4C.helper.WorldMapExtension;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Annotates the shipped terrain overview with positions from the runtime god catalog. */
public final class DivineMapExporter {
  private DivineMapExporter() {}

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    BufferedImage terrain = ImageIO.read(new File(WorldMapExtension.IMAGE_PATH));
    int minX = 3550, minY = 500, cropW = 2500, cropH = 2200;
    int mapW = 1250, mapH = 1100, left = 40, top = 115;
    BufferedImage result = new BufferedImage(1690, 1290, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = result.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(new Color(14, 22, 32));
    g.fillRect(0, 0, result.getWidth(), result.getHeight());
    g.setColor(Color.WHITE);
    g.setFont(new Font("SansSerif", Font.BOLD, 34));
    g.drawString("THE DIVINE CONCORD", 40, 53);
    g.setFont(new Font("SansSerif", Font.PLAIN, 18));
    g.setColor(new Color(178, 194, 209));
    g.drawString("All 10 shrines | World-map tile coordinates | North is up", 40, 85);
    g.drawImage(
        terrain,
        left,
        top,
        left + mapW,
        top + mapH,
        minX - WorldMapExtension.ORIGIN_X,
        minY,
        minX - WorldMapExtension.ORIGIN_X + cropW,
        minY + cropH,
        null);
    g.setFont(new Font("SansSerif", Font.BOLD, 17));
    g.setColor(new Color(230, 215, 164));
    g.drawString("WITNESS ISLES", 100, 160);
    g.drawString("THRENODY REACH", 980, 160);
    Color[] colors = {new Color(125, 219, 156), new Color(255, 203, 108), new Color(176, 197, 255)};
    int number = 0;
    for (var god : DivinePantheon.all()) {
      number++;
      Color color = colors[god.tier()];
      int px = left + (god.x() - minX) * mapW / cropW;
      int py = top + (god.y() - minY) * mapH / cropH;
      int labelX = px + 23, labelY = py - 18;
      // Keep neighboring Moonwake shrine labels legible.
      if (god.id().equals("apollo")) labelY = py + 35;
      if (god.id().equals("ogrimar")) labelX = px - 140;
      if (god.id().equals("njord")) {
        labelX = px - 35;
        labelY = py + 38;
      }
      g.setStroke(new BasicStroke(2));
      g.setColor(color);
      g.drawLine(px, py, labelX, labelY);
      String label = number + ". " + god.name();
      int textWidth = g.getFontMetrics().stringWidth(label);
      g.setColor(new Color(9, 16, 24, 230));
      g.fillRoundRect(labelX - 6, labelY - 20, textWidth + 12, 28, 8, 8);
      g.setColor(color);
      g.drawString(label, labelX, labelY);
      g.fillOval(px - 8, py - 8, 16, 16);
      g.setColor(Color.WHITE);
      g.drawOval(px - 9, py - 9, 18, 18);
      int row = 162 + (number - 1) * 96;
      g.setColor(color);
      g.setFont(new Font("SansSerif", Font.BOLD, 21));
      g.drawString(number + ". " + god.name(), 1320, row);
      g.setFont(new Font("SansSerif", Font.PLAIN, 17));
      g.setColor(new Color(207, 218, 230));
      g.drawString("Level " + god.level() + " | " + god.school(), 1320, row + 25);
      g.drawString(
          "(" + god.x() + ", " + god.y() + ") | " + god.respawnMillis() / 60000.0 + "m",
          1320,
          row + 49);
      g.setFont(new Font("SansSerif", Font.BOLD, 17));
    }
    g.setColor(new Color(178, 194, 209));
    g.setFont(new Font("SansSerif", Font.PLAIN, 16));
    g.drawString(
        "Terrain overview from the shipped map. Pins mark exact shrine tiles; labels are offset for readability.",
        40,
        1250);
    g.dispose();
    File output = new File(args.length == 0 ? "docs/images/divine-god-locations.png" : args[0]);
    output.getAbsoluteFile().getParentFile().mkdirs();
    ImageIO.write(result, "png", output);
    System.out.println("Wrote " + output.getAbsolutePath());
  }
}
