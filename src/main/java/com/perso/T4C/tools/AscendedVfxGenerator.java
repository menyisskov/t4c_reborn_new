package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.SpriteBinWriter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * One-off generator (T4C-0077) that derives a palette-shifted "Ascended" variant of each
 * high-tier attack spell's impact animation, so a level 250+ ultra nuke reads as a visibly
 * different, more intense effect than the level-27 spell it otherwise shares art with - not just
 * the same burst multiplied (see {@link com.perso.T4C.spell.GrandImpactShower}, T4C-0069).
 *
 * <p>This is a straight palette remap of the existing legacy frames (same shapes, same timing,
 * same alpha silhouette - only hue/saturation/brightness change), not hand-drawn new art: there is
 * no artist source or wired art pipeline in this repo (see the graphic-designer skill), so a
 * recolor of the real animation is the honest way to make a genuinely distinct sprite without
 * inventing pixels from nothing. Run once; the derived frames are packed into the sprite bin and
 * committed like any other asset - this generator does not run at game startup.
 */
public final class AscendedVfxGenerator {
  private AscendedVfxGenerator() {}

  /**
   * A duotone color grade: source luminance 0-1 is remapped onto the gradient from {@code dark}
   * to {@code bright}, alpha preserved untouched. A plain hue *shift* does nothing to these
   * sprites - most of the source frames (explosion whites, rock grays, curse near-blacks) are
   * already close to zero saturation, so multiplying that saturation leaves it at zero. A duotone
   * grade instead assigns brand new color at every luminance level, so the result is dramatically
   * different regardless of how saturated the source was.
   */
  private record Recolor(String sourceBase, String targetBase, Color dark, Color bright) {}

  private static final List<Recolor> RECOLORS =
      List.of(
          // Fire: black-red core rising to searing white-gold, instead of orange-red on white.
          new Recolor(
              "GreatExplosion", "GreatExplosion-Ascended",
              new Color(40, 0, 0), new Color(255, 248, 210)),
          new Recolor(
              "64kSpellFireCircle", "64kSpellFireCircle-Ascended",
              new Color(40, 0, 0), new Color(255, 248, 210)),
          // Earth: near-black obsidian rising to a glowing magma magenta-red, instead of gray rock.
          new Recolor(
              "64kSpellBoulders", "64kSpellBoulders-Ascended",
              new Color(10, 0, 5), new Color(220, 40, 90)),
          new Recolor(
              "Flak1", "Flak1-Ascended", new Color(10, 0, 5), new Color(220, 40, 90)),
          // Air: dark indigo rising to bright violet-white plasma, instead of yellow spark.
          new Recolor(
              "GreatBolt", "GreatBolt-Ascended", new Color(15, 0, 35), new Color(215, 190, 255)),
          new Recolor(
              "ElectricShield", "ElectricShield-Ascended",
              new Color(15, 0, 35), new Color(215, 190, 255)),
          // Water: near-black blue rising to an intense violet-blue, instead of pale ice-blue.
          new Recolor(
              "64kSpellGlacier", "64kSpellGlacier-Ascended",
              new Color(0, 5, 25), new Color(110, 130, 255)),
          new Recolor(
              "IceCloud", "IceCloud-Ascended", new Color(0, 5, 25), new Color(110, 130, 255)),
          // Light: dark amber rising to radiant gold, instead of plain white heal-burst.
          new Recolor(
              "HealingSpell", "HealingSpell-Ascended",
              new Color(35, 15, 0), new Color(255, 210, 90)),
          // Dark: near-black rising to violent violet-magenta, instead of murky green/black.
          new Recolor("Curse", "Curse-Ascended", new Color(10, 0, 15), new Color(200, 40, 220)));

  public static void main(String[] args) throws IOException {
    Path spriteDir = Path.of(args.length > 0 ? args[0] : "assets/sprites");
    // SpriteBinWriter derives the shard base name by stripping ".bin" from this path's filename,
    // so it must be the bare base pattern ("sprites"), not an existing shard like "sprites_0.bin"
    // (which would produce the base "sprites_0" and write sprites_0_0.bin/sprites_0_1.bin instead
    // of re-sharding "sprites_0/1/2.bin" in place).
    Path anchor = spriteDir.resolve(SpriteBinIO.legacyName(SpriteBinIO.DEFAULT_BASE_NAME));

    Map<String, SpriteBinIO.Packed> byName = new HashMap<>();
    SpriteBinIO.readAll(spriteDir, "sprites", packed -> byName.put(SpriteBinIO.key(packed.name()), packed));

    List<SpriteBinWriter.Entry> entries = new ArrayList<>();
    for (Recolor recolor : RECOLORS) {
      List<SpriteBinIO.Packed> frames = framesOf(byName, recolor.sourceBase());
      if (frames.isEmpty()) {
        throw new IllegalStateException("No frames found for base sprite: " + recolor.sourceBase());
      }
      for (SpriteBinIO.Packed frame : frames) {
        String suffix = frame.name().substring(recolor.sourceBase().length());
        String newName = recolor.targetBase() + suffix;
        BufferedImage recolored =
            recolor(ImageIO.read(new ByteArrayInputStream(frame.png())), recolor.dark(), recolor.bright());
        entries.add(
            new SpriteBinWriter.Entry(
                newName,
                frame.width(),
                frame.height(),
                frame.off1X(),
                frame.off1Y(),
                frame.off2X(),
                frame.off2Y(),
                recolored));
      }
      System.out.println(
          "Recolored " + frames.size() + " frame(s): " + recolor.sourceBase() + " -> " + recolor.targetBase());
    }

    int written = SpriteBinWriter.merge(anchor, entries);
    System.out.println("Wrote " + written + " ascended VFX sprite entries to " + spriteDir);
  }

  /**
   * Every packed sprite whose name is exactly {@code base} or starts with {@code base + "-"} -
   * excluding anything already tagged {@code -Ascended}, so re-running this generator against a
   * sprite bin that already has derived frames in it never recolors its own output.
   */
  private static List<SpriteBinIO.Packed> framesOf(Map<String, SpriteBinIO.Packed> byName, String base) {
    String lowerBase = base.toLowerCase(Locale.ROOT);
    List<SpriteBinIO.Packed> frames = new ArrayList<>();
    for (SpriteBinIO.Packed packed : byName.values()) {
      String lower = packed.name().toLowerCase(Locale.ROOT);
      if (lower.contains("-ascended")) {
        continue;
      }
      if (lower.equals(lowerBase) || lower.startsWith(lowerBase + "-")) {
        frames.add(packed);
      }
    }
    frames.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
    return frames;
  }

  private static BufferedImage recolor(BufferedImage src, Color dark, Color bright) {
    int width = src.getWidth();
    int height = src.getHeight();
    BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = src.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
          out.setRGB(x, y, argb);
          continue;
        }
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        // Perceptual luminance, 0-1, drives where this pixel falls on the dark->bright gradient.
        float luminance = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
        int newR = lerp(dark.getRed(), bright.getRed(), luminance);
        int newG = lerp(dark.getGreen(), bright.getGreen(), luminance);
        int newB = lerp(dark.getBlue(), bright.getBlue(), luminance);
        out.setRGB(x, y, (alpha << 24) | (newR << 16) | (newG << 8) | newB);
      }
    }
    return out;
  }

  private static int lerp(int from, int to, float t) {
    return Math.round(from + (to - from) * t);
  }
}
