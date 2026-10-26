package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.SpriteBinWriter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/** Reproducible white/black regalia; preserves every legacy frame's offsets and alpha. */
public final class DivineEquipmentPaletteGenerator {
  private static final List<String> BASES =
      List.of(
          "PupPlateBody",
          "PupPlateFoot",
          "PupPlateGloveL",
          "PupPlateGloveR",
          "PupPlateHelm",
          "PupPlateLegs",
          "PupPlateArmL",
          "PupPlateArmR",
          "WoPlateBody",
          "WoPlateBoots",
          "WoPlateGloveL",
          "WoPlateGloveR",
          "WoPlateHelm",
          "WoPlateLegs",
          "PupWhiteRobe",
          "WoWhiteRobe",
          "PupSeraphWhiteWings",
          "PupNormalSword",
          "PupWoodenStaff",
          "PupLongBow",
          "64kInvPlateArmorSleeves",
          "64kInvPlateArmorFeet",
          "64kInvPlateGlove",
          "64kInvPlateArmorHelm",
          "64kInvPlateArmorLegs",
          "64kInvWhiteRobe",
          "64kInvSeraphWhiteWings",
          "64kInvNecklace 2",
          "64kInvRings 4",
          "64kInvBelt",
          "64kInvShortSword",
          "64kInvWoodenStaff",
          "64kInvLongBow");

  private DivineEquipmentPaletteGenerator() {}

  public static void main(String[] args) throws Exception {
    Path directory = Path.of("assets/sprites");
    List<SpriteBinWriter.Entry> entries = new ArrayList<>();
    Map<String, Integer> counts = new LinkedHashMap<>();
    Map<String, BufferedImage> samples = new LinkedHashMap<>();
    SpriteBinIO.readAll(
        directory,
        "sprites",
        frame -> {
          for (String base : BASES) {
            if (!frame.name().startsWith(base)) continue;
            String suffix = frame.name().substring(base.length());
            if (!suffix.isEmpty() && !suffix.matches("[A-Z]?[0-9]{3}-[a-zA-Z0-9]+")) continue;
            try {
              BufferedImage source = ImageIO.read(new ByteArrayInputStream(frame.png()));
              for (int palette = 90; palette <= 97; palette++) {
                BufferedImage result = recolor(source, palette);
                String name = base + "__pal" + palette + suffix;
                entries.add(
                    new SpriteBinWriter.Entry(
                        name,
                        frame.width(),
                        frame.height(),
                        frame.off1X(),
                        frame.off1Y(),
                        frame.off2X(),
                        frame.off2Y(),
                        frame.type(),
                        result));
                if (base.startsWith("64kInv") && palette <= 91) samples.put(name, result);
              }
              counts.merge(base, 1, Integer::sum);
            } catch (Exception e) {
              throw new IllegalStateException("Unable to recolor " + frame.name(), e);
            }
            break;
          }
        });
    for (String base : BASES)
      if (!counts.containsKey(base))
        throw new IllegalStateException("Missing source family: " + base);
    int written = SpriteBinWriter.merge(directory.resolve("sprites.bin"), entries);
    BufferedImage preview = new BufferedImage(720, 300, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = preview.createGraphics();
    g.setColor(new Color(45, 53, 64));
    g.fillRect(0, 0, preview.getWidth(), preview.getHeight());
    int index = 0;
    for (var sample : samples.entrySet()) {
      int x = index / 2 * 60;
      int y = index % 2 * 145;
      g.drawImage(sample.getValue(), x + 5, y + 20, null);
      index++;
    }
    g.dispose();
    ImageIO.write(preview, "png", Path.of("target/divine-regalia-preview.png").toFile());
    System.out.println("Wrote " + written + " divine equipment frames; " + counts);
  }

  static BufferedImage recolor(BufferedImage source, int palette) {
    BufferedImage result =
        new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
    int[] dark =
        switch (palette) {
          case 90 -> new int[] {55, 58, 65};
          case 91 -> new int[] {6, 5, 10};
          case 92 -> new int[] {12, 36, 19};
          case 93 -> new int[] {10, 23, 56};
          case 94 -> new int[] {60, 9, 5};
          case 95 -> new int[] {59, 36, 8};
          case 96 -> new int[] {23, 9, 35};
          case 97 -> new int[] {68, 53, 26};
          default -> throw new IllegalArgumentException("Unknown divine palette: " + palette);
        };
    int[] light =
        switch (palette) {
          case 90 -> new int[] {255, 254, 245};
          case 91 -> new int[] {112, 106, 125};
          case 92 -> new int[] {132, 221, 132};
          case 93 -> new int[] {132, 199, 250};
          case 94 -> new int[] {250, 125, 61};
          case 95 -> new int[] {253, 222, 106};
          case 96 -> new int[] {154, 101, 187};
          case 97 -> new int[] {255, 246, 194};
          default -> throw new IllegalArgumentException("Unknown divine palette: " + palette);
        };
    for (int y = 0; y < source.getHeight(); y++)
      for (int x = 0; x < source.getWidth(); x++) {
        int pixel = source.getRGB(x, y);
        int alpha = pixel >>> 24;
        if (alpha == 0) continue;
        double luminance =
            (((pixel >>> 16) & 255) * .2126 + ((pixel >>> 8) & 255) * .7152 + (pixel & 255) * .0722)
                / 255d;
        if (palette == 90 || palette == 97) luminance = Math.sqrt(luminance);
        int rgb = alpha << 24;
        for (int channel = 0; channel < 3; channel++)
          rgb |=
              (int) Math.round(dark[channel] + luminance * (light[channel] - dark[channel]))
                  << (16 - channel * 8);
        result.setRGB(x, y, rgb);
      }
    return result;
  }
}
