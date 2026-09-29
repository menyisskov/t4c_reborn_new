package com.perso.T4C.helper;

import com.badlogic.gdx.graphics.Pixmap;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Adds new world terrain to the map window without changing the native RT map's binary layout. */
public final class WorldMapExtension {
  public static final int ORIGIN_X = 3072;
  public static final String IMAGE_PATH = "assets/maps/worldmap/east-overview.png";
  private static BufferedImage overview;

  private WorldMapExtension() {}

  static void draw(Pixmap target, int world, int tileX, int tileY) throws IOException {
    if (world != 0 || tileX + target.getWidth() / 8 < ORIGIN_X) return;
    BufferedImage source = image();
    for (int py = 0; py < target.getHeight(); py++) {
      int sy = tileY + Math.floorDiv(py - target.getHeight() / 2, 2);
      if (sy < 0 || sy >= source.getHeight()) continue;
      for (int px = 0; px < target.getWidth(); px++) {
        int sx = tileX + Math.floorDiv(px - target.getWidth() / 2, 4) - ORIGIN_X;
        if (sx < 0 || sx >= source.getWidth()) continue;
        target.drawPixel(px, py, (source.getRGB(sx, sy) << 8) | 0xff);
      }
    }
  }

  private static synchronized BufferedImage image() throws IOException {
    if (overview == null) {
      overview = ImageIO.read(new File(IMAGE_PATH));
      if (overview == null) throw new IOException("Invalid world overview: " + IMAGE_PATH);
    }
    return overview;
  }
}
