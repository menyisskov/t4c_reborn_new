package com.perso.T4C.tmpl3;

import com.badlogic.gdx.graphics.Pixmap;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.SpriteLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Tmpl3MaskLoader {
  private static final int GREEN_RGB = 0x00A800;
  private static final int BLUE_RGB = 0x0000A8;
  private final SpriteLoader spriteLoader;
  private final String templateName;
  private final Pattern spriteNamePattern;

  public Tmpl3MaskLoader(SpriteLoader spriteLoader) {
    this(spriteLoader, "Tmpl3");
  }

  public Tmpl3MaskLoader(SpriteLoader spriteLoader, String templateName) {
    this.spriteLoader = spriteLoader;
    this.templateName = templateName;
    this.spriteNamePattern =
        Pattern.compile(
            "^\\s*" + Pattern.quote(templateName) + "\\s+(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);
  }

  public List<Tmpl3Mask> load() throws GameException {
    List<Tmpl3Mask> masks = new ArrayList<>();
    for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
      Matcher matcher = spriteNamePattern.matcher(sprite.name);
      if (!matcher.matches()) {
        continue;
      }
      int id = Integer.parseInt(matcher.group(1));
      if (sprite.width != Tmpl3Mask.WIDTH || sprite.height != Tmpl3Mask.HEIGHT) {
        log.warn(
            "Skipping {} sprite with invalid dimensions: {} ({}x{})",
            templateName,
            sprite.name,
            sprite.width,
            sprite.height);
        continue;
      }
      Pixmap pixmap = null;
      try {
        pixmap = spriteLoader.createPixmapForSprite(sprite.name);
        if (pixmap == null) {
          log.warn("Skipping {} sprite without pixmap: {}", templateName, sprite.name);
          continue;
        }
        masks.add(new Tmpl3Mask(id, sprite.name, toMask(pixmap)));
      } finally {
        if (pixmap != null) {
          pixmap.dispose();
        }
      }
    }
    masks.sort(Comparator.comparingInt(Tmpl3Mask::id));
    log.info("Loaded {} {} masks from SpriteLoader", masks.size(), templateName);
    return List.copyOf(masks);
  }

  private MaskColor[][] toMask(Pixmap pixmap) {
    MaskColor[][] pixels = new MaskColor[Tmpl3Mask.HEIGHT][Tmpl3Mask.WIDTH];
    for (int y = 0; y < Tmpl3Mask.HEIGHT; y++) {
      for (int x = 0; x < Tmpl3Mask.WIDTH; x++) {
        int rgba = pixmap.getPixel(x, y);
        int alpha = rgba & 0xFF;
        int rgb = (rgba >>> 8) & 0xFFFFFF;
        if (templateName.equalsIgnoreCase("Tmpl1") || templateName.equalsIgnoreCase("Tmpl4")) {
          int red = (rgb >>> 16) & 0xFF;
          int green = (rgb >>> 8) & 0xFF;
          int blue = rgb & 0xFF;
          int luminance = red * 299 + green * 587 + blue * 114;
          pixels[y][x] = alpha == 0 || luminance < 128_000 ? MaskColor.BLUE : MaskColor.GREEN;
        } else if (alpha == 0) {
          pixels[y][x] = MaskColor.UNKNOWN;
        } else if (rgb == GREEN_RGB) {
          pixels[y][x] = MaskColor.GREEN;
        } else if (rgb == BLUE_RGB) {
          pixels[y][x] = MaskColor.BLUE;
        } else {
          pixels[y][x] = MaskColor.UNKNOWN;
        }
      }
    }
    return pixels;
  }
}
