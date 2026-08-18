package com.perso.T4C.tmpl3;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Tmpl3DebugExporter {
  private static final int SCALE = 4;
  private static final int TILE_W = Tmpl3Mask.WIDTH * SCALE;
  private static final int TILE_H = Tmpl3Mask.HEIGHT * SCALE;
  private static final int GAP = 8;
  private static final int LABEL_H = 22;

  public File exportComparison(
      File outputDir,
      Tmpl3Point position,
      TerrainContext context,
      MaskColor[][] expected,
      List<Tmpl3Candidate> candidates,
      Tmpl3Candidate chosen) {
    if (!outputDir.exists() && !outputDir.mkdirs()) {
      log.warn("Failed to create Tmpl3 debug directory: {}", outputDir);
      return null;
    }
    int count = Math.min(5, candidates.size());
    int width = (count + 1) * TILE_W + count * GAP;
    int height = TILE_H + LABEL_H;
    Pixmap image = new Pixmap(width, height, Pixmap.Format.RGBA8888);
    try {
      image.setColor(0.12f, 0.12f, 0.12f, 1f);
      image.fill();
      drawMask(image, expected, 0, 0);
      for (int i = 0; i < count; i++) {
        drawMask(image, candidates.get(i).mask().pixels(), (i + 1) * (TILE_W + GAP), 0);
      }
      File output = new File(outputDir, position.x() + "_" + position.y() + ".png");
      PixmapIO.writePNG(new FileHandle(output), image);
      log.info(
          "Tmpl3 debug ({}, {}): families {}|{}, chosen {}, candidates {}",
          position.x(),
          position.y(),
          context.familyA(),
          context.familyB(),
          chosen != null ? chosen.id() : null,
          candidates.stream().limit(5).map(c -> c.id() + "=" + c.finalScore()).toList());
      return output;
    } finally {
      image.dispose();
    }
  }

  private static void drawMask(Pixmap image, MaskColor[][] mask, int ox, int oy) {
    for (int y = 0; y < Tmpl3Mask.HEIGHT; y++) {
      for (int x = 0; x < Tmpl3Mask.WIDTH; x++) {
        MaskColor color = mask[y][x];
        if (color == MaskColor.GREEN) {
          image.setColor(0f, 0.659f, 0f, 1f);
        } else if (color == MaskColor.BLUE) {
          image.setColor(0f, 0f, 0.659f, 1f);
        } else {
          image.setColor(0.45f, 0.45f, 0.45f, 1f);
        }
        image.fillRectangle(ox + x * SCALE, oy + y * SCALE, SCALE, SCALE);
      }
    }
  }
}
