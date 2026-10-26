package com.perso.T4C.gui.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.ui.FontManager;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import javax.imageio.ImageIO;

/** Shaded obsidian surfaces, worn bronze accents and ivory typography. */
public final class JournalTheme {
  public static final float MANA_START_ANGLE = 120;
  public static final float MANA_SWEEP_ANGLE = 300;
  public static final float MANA_RADIUS_RATIO = .43f;
  public static final Color GOLD = Color.valueOf("DDC28B");
  public static final Color TEXT = Color.valueOf("F0ECE3");
  public static final Color DIM = Color.valueOf("AEB5B6");
  public static final Color LINE = Color.valueOf("454F54");
  public static final Color INK = Color.valueOf("141A20");
  public static final Color ACCENT = Color.valueOf("80C6C4");
  private final List<Texture> textures = new ArrayList<>();
  private final Map<String, TextureRegion[]> buttonArt = new HashMap<>();
  private final TextureRegion pixel;
  private final NinePatch inset;
  private final NinePatch accent;

  public JournalTheme() {
    BufferedImage white = canvas(1, 1);
    white.setRGB(0, 0, 0xffffffff);
    pixel = upload(white);
    inset = new NinePatch(surface(48, 48, false), 10, 10, 10, 10);
    accent = new NinePatch(surface(48, 48, true), 10, 10, 10, 10);
  }

  private static BufferedImage canvas(int w, int h) {
    return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
  }

  private static Graphics2D graphics(BufferedImage image) {
    Graphics2D g = image.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    return g;
  }

  private TextureRegion upload(BufferedImage image) {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ImageIO.write(image, "png", bytes);
      byte[] png = bytes.toByteArray();
      Pixmap pixmap = new Pixmap(png, 0, png.length);
      Texture texture = new Texture(pixmap);
      pixmap.dispose();
      texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      textures.add(texture);
      return new TextureRegion(texture);
    } catch (IOException e) {
      throw new GdxRuntimeException("Cannot create journal artwork", e);
    }
  }

  private TextureRegion surface(int w, int h, boolean strong) {
    BufferedImage image = canvas(w, h);
    Graphics2D g = graphics(image);
    g.setColor(new java.awt.Color(0, 0, 0, 130));
    g.fillRoundRect(0, 1, w, h - 1, 10, 10);
    g.setPaint(
        new GradientPaint(
            0,
            2,
            new java.awt.Color(strong ? 58 : 31, strong ? 51 : 39, 40),
            0,
            h - 2,
            new java.awt.Color(14, 20, 26)));
    g.fillRoundRect(1, 1, w - 3, h - 4, 8, 8);
    g.setColor(new java.awt.Color(strong ? 150 : 65, strong ? 125 : 78, strong ? 79 : 86, 180));
    g.drawRoundRect(1, 1, w - 3, h - 4, 8, 8);
    g.setColor(new java.awt.Color(255, 255, 255, 20));
    g.drawLine(8, 3, w - 9, 3);
    g.dispose();
    return upload(image);
  }

  public TextureRegion frame(int width, int height) {
    return frame(width, height, true, true);
  }

  /** Standalone services and dialogs have one title band, without personal-journal tabs. */
  public TextureRegion windowFrame(int width, int height) {
    return frame(width, height, false, true);
  }

  public TextureRegion plainFrame(int width, int height) {
    return frame(width, height, false, false);
  }

  private TextureRegion frame(int width, int height, boolean journalTabs, boolean headerBand) {
    BufferedImage image = canvas(width, height);
    Graphics2D g = graphics(image);
    g.setColor(new java.awt.Color(0, 0, 0, 170));
    g.fillRoundRect(0, 0, width, height, 18, 18);
    g.setPaint(
        new GradientPaint(
            0,
            0,
            new java.awt.Color(35, 43, 49),
            width * .6f,
            height,
            new java.awt.Color(13, 19, 25)));
    g.fillRoundRect(3, 3, width - 6, height - 6, 13, 13);
    Random grain = new Random(417);
    for (int y = 5; y < height - 5; y++) {
      for (int x = 5; x < width - 5; x++) {
        int noise = grain.nextInt(9) - 4;
        int rgb = image.getRGB(x, y);
        int r = Math.max(0, Math.min(255, ((rgb >> 16) & 255) + noise));
        int gr = Math.max(0, Math.min(255, ((rgb >> 8) & 255) + noise));
        int b = Math.max(0, Math.min(255, (rgb & 255) + noise));
        image.setRGB(x, y, 0xff000000 | r << 16 | gr << 8 | b);
      }
    }
    if (height > 140 && headerBand) {
      g.setPaint(
          new GradientPaint(
              0,
              10,
              new java.awt.Color(141, 108, 57, 80),
              width * .7f,
              40,
              new java.awt.Color(26, 33, 40, 0)));
      g.fillRoundRect(9, 9, width - 18, 45, 8, 8);
      g.setColor(new java.awt.Color(135, 117, 82, 130));
      g.drawLine(24, 54, width - 25, 54);
      g.setColor(new java.awt.Color(109, 121, 130, 60));
      if (journalTabs && height > 300) {
        g.drawLine(24, 96, width - 25, 96);
        g.drawLine(24, height - 43, width - 25, height - 43);
      }
    }
    g.setColor(new java.awt.Color(118, 113, 96));
    g.drawRoundRect(3, 3, width - 7, height - 7, 13, 13);
    g.setColor(new java.awt.Color(205, 185, 135, 110));
    g.drawLine(18, 4, width - 19, 4);
    // Swept corner inlays rather than a bright outline around every control.
    for (int side = 0; side < 2; side++) {
      for (int end = 0; end < 2; end++) {
        Graphics2D corner = (Graphics2D) g.create();
        corner.translate(side == 0 ? 8 : width - 8, end == 0 ? 8 : height - 8);
        corner.scale(side == 0 ? 1 : -1, end == 0 ? 1 : -1);
        corner.setColor(new java.awt.Color(171, 146, 94));
        Path2D path = new Path2D.Float();
        path.moveTo(0, 30);
        path.curveTo(0, 8, 8, 0, 30, 0);
        path.moveTo(4, 22);
        path.curveTo(4, 10, 10, 4, 22, 4);
        corner.draw(path);
        corner.fillOval(9, 9, 3, 3);
        corner.dispose();
      }
    }
    g.dispose();
    return upload(image);
  }

  public TextureRegion healthOrb(int size, boolean fill) {
    BufferedImage image = canvas(size, size);
    Graphics2D g = graphics(image);
    if (fill) {
      g.setPaint(
          new java.awt.RadialGradientPaint(
              size * .36f,
              size * .28f,
              size * .78f,
              new float[] {0, .6f, 1},
              new java.awt.Color[] {
                new java.awt.Color(228, 66, 43),
                new java.awt.Color(158, 21, 24),
                new java.awt.Color(61, 6, 15)
              }));
      g.fillOval(8, 8, size - 16, size - 16);
      g.setColor(new java.awt.Color(255, 167, 118, 40));
      g.fillOval(size / 5, size / 7, size / 2, size / 5);
    } else {
      g.setPaint(
          new GradientPaint(
              0, 0, new java.awt.Color(183, 166, 128), 0, size, new java.awt.Color(43, 50, 58)));
      g.fillOval(1, 1, size - 2, size - 2);
      g.setColor(new java.awt.Color(8, 12, 18));
      g.fillOval(6, 6, size - 12, size - 12);
      g.setColor(new java.awt.Color(219, 197, 144));
      g.setStroke(new BasicStroke(2));
      g.drawArc(3, 3, size - 6, size - 6, 30, 120);
    }
    g.dispose();
    return upload(image);
  }

  /** Static artwork for the mana track or its filled 300-degree horseshoe. */
  public TextureRegion manaHorseshoe(int size, boolean filled) {
    BufferedImage image = canvas(size, size);
    Graphics2D g = graphics(image);
    double radius = size * MANA_RADIUS_RATIO;
    double inset = size / 2d - radius;
    var arc =
        new java.awt.geom.Arc2D.Double(
            inset,
            inset,
            radius * 2,
            radius * 2,
            -MANA_START_ANGLE,
            -MANA_SWEEP_ANGLE,
            java.awt.geom.Arc2D.OPEN);
    if (!filled) {
      g.setPaint(
          new GradientPaint(
              0,
              0,
              new java.awt.Color(213, 193, 148),
              size * .6f,
              size,
              new java.awt.Color(46, 57, 70)));
      g.fillOval(1, 1, size - 2, size - 2);
      g.setColor(new java.awt.Color(10, 17, 25));
      g.fillOval(4, 4, size - 8, size - 8);
      g.setColor(new java.awt.Color(27, 51, 72));
    } else {
      g.setColor(new java.awt.Color(52, 169, 235, 36));
      g.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g.draw(arc);
      g.setPaint(
          new GradientPaint(
              0, 0, new java.awt.Color(160, 232, 255), 0, size, new java.awt.Color(19, 97, 162)));
    }
    g.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g.draw(arc);
    g.dispose();
    return upload(image);
  }

  public TextureRegion manaHorseshoeCap() {
    BufferedImage image = canvas(16, 16);
    Graphics2D g = graphics(image);
    g.setColor(java.awt.Color.WHITE);
    g.fillOval(3, 3, 10, 10);
    g.dispose();
    return upload(image);
  }

  public GuiButton button(
      float x, float y, int width, int height, Supplier<String> label, Runnable action) {
    return button(x, y, width, height, label, action, false);
  }

  public GuiButton button(
      float x,
      float y,
      int width,
      int height,
      Supplier<String> label,
      Runnable action,
      boolean selected) {
    TextureRegion[] art =
        buttonArt.computeIfAbsent(
            width + ":" + height,
            key -> {
              TextureRegion[] states = new TextureRegion[3];
              for (int i = 0; i < 3; i++) states[i] = surface(width, height, i != 0);
              return states;
            });
    return new GuiButton(selected ? art[2] : art[0], art[1], art[2], x, y, action)
        .withLabel(
            FontManager.getInstance().getTahomaFont(12, selected ? GOLD : TEXT, false), label);
  }

  public void rect(SpriteBatch batch, float x, float y, float w, float h, Color color) {
    float previous = batch.getPackedColor();
    batch.setColor(color);
    batch.draw(pixel, x, y, w, h);
    batch.setPackedColor(previous);
  }

  public void well(SpriteBatch batch, float x, float y, float w, float h) {
    inset.draw(batch, x, y, w, h);
  }

  public void accentWell(SpriteBatch batch, float x, float y, float w, float h) {
    accent.draw(batch, x, y, w, h);
  }

  public TextureRegion control(int width, int height, boolean active) {
    TextureRegion[] art =
        buttonArt.computeIfAbsent(
            width + ":" + height,
            key ->
                new TextureRegion[] {
                  surface(width, height, false),
                  surface(width, height, true),
                  surface(width, height, true)
                });
    return art[active ? 1 : 0];
  }

  public TextureRegion checkbox(boolean checked, boolean hover) {
    String key = "check:" + checked + ":" + hover;
    TextureRegion[] cached = buttonArt.get(key);
    if (cached != null) return cached[0];
    BufferedImage image = canvas(18, 18);
    Graphics2D g = graphics(image);
    g.setColor(new java.awt.Color(17, 26, 34));
    g.fillRoundRect(1, 1, 16, 16, 4, 4);
    g.setColor(
        new java.awt.Color(
            hover || checked ? 180 : 88,
            hover || checked ? 156 : 99,
            hover || checked ? 108 : 103));
    g.drawRoundRect(1, 1, 16, 16, 4, 4);
    if (checked) {
      g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g.drawLine(5, 9, 8, 12);
      g.drawLine(8, 12, 13, 5);
    }
    g.dispose();
    TextureRegion result = upload(image);
    buttonArt.put(key, new TextureRegion[] {result});
    return result;
  }

  public void dispose() {
    textures.forEach(Texture::dispose);
    textures.clear();
  }
}
