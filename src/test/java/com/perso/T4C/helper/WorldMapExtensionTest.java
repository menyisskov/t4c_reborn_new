package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxNativesLoader;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class WorldMapExtensionTest {
  @Test
  void overviewCentersOnRelocatedPlayerAndDoesNotPaintLegacyOrDungeonPixels() throws Exception {
    GdxNativesLoader.load();
    BufferedImage image = ImageIO.read(new File(WorldMapExtension.IMAGE_PATH));
    assertEquals(6144 - 3072, image.getWidth());
    assertEquals(3072, image.getHeight());
    Pixmap view = new Pixmap(640, 448, Pixmap.Format.RGBA8888);
    try {
      view.setColor(0, 0, 0, 1);
      view.fill();
      WorldMapExtension.draw(view, 0, 4040, 1477);
      int expected = (image.getRGB(4040 - 3072, 1477) << 8) | 0xff;
      assertNotEquals(0x000000ff, expected, "arrival should have real map terrain");
      assertEquals(expected, view.getPixel(320, 224));
      view.setColor(0, 0, 0, 1);
      view.fill();
      WorldMapExtension.draw(view, 1, 4040, 1477);
      assertEquals(0x000000ff, view.getPixel(320, 224));
      WorldMapExtension.draw(view, 0, 3072, 1477);
      assertEquals(
          0x000000ff, view.getPixel(319, 224), "original map pixels must remain untouched");
      assertEquals((image.getRGB(0, 1477) << 8) | 0xff, view.getPixel(320, 224));
    } finally {
      view.dispose();
    }
  }
}
