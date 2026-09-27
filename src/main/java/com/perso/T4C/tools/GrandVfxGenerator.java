package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.SpriteBinWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * One-off generator (T4C-0083) for the level-300 rung of the high-tier spell ladder: the original
 * game's own top-tier ("NM", nightmare) spell animations, made drawable by this renderer.
 *
 * <p>Those animations were painted for additive blending - every pixel opaque, the effect glowing
 * on a black field - but {@code SpellRenderer} draws with normal alpha blending, so used as-is
 * they show a thick black halo. This tool copies each one under a new name with
 * {@link MythicVfxPacker#lumaAlpha} applied (alpha = brightness, which reproduces the additive
 * look exactly), keeping every frame's size, draw offsets and play order. Originals that already
 * render correctly (BoulderFire has its own transparency mask, iceTree is not additive) are used
 * directly and are not copied. Run once; the copies are committed like any other asset.
 */
public final class GrandVfxGenerator {
  private GrandVfxGenerator() {}

  /** Original additive animation, and the name its alpha-blended copy is packed under. */
  public record Conversion(String source, String target) {}

  public static final List<Conversion> CONVERSIONS =
      List.of(
          new Conversion("NM_Fire000", "GrandFire"),
          new Conversion("thunderstorm", "GrandAir"),
          new Conversion("NMS_2SupraHeal", "GrandLight"),
          new Conversion("NM_Poison000", "GrandDark"));

  public static void main(String[] args) throws IOException {
    Path spriteDir = Path.of(args.length > 0 ? args[0] : "assets/sprites");
    List<SpriteBinIO.Packed> all = new ArrayList<>();
    SpriteBinIO.readAll(spriteDir, SpriteBinIO.DEFAULT_BASE_NAME, all::add);

    List<SpriteBinWriter.Entry> entries = new ArrayList<>();
    for (Conversion c : CONVERSIONS) {
      List<SpriteBinIO.Packed> frames = new ArrayList<>();
      for (SpriteBinIO.Packed p : all) {
        if (MythicVfxPacker.isFrameOf(p.name(), c.source())) frames.add(p);
      }
      if (frames.isEmpty()) {
        throw new IllegalStateException("No frames found for " + c.source());
      }
      frames.sort(Comparator.comparingInt(p -> MythicVfxPacker.frameOrder(p.name(), c.source())));
      for (int i = 0; i < frames.size(); i++) {
        SpriteBinIO.Packed f = frames.get(i);
        BufferedImage converted =
            MythicVfxPacker.lumaAlpha(ImageIO.read(new ByteArrayInputStream(f.png())));
        entries.add(
            new SpriteBinWriter.Entry(
                MythicVfxPacker.frameName(c.target(), i),
                f.width(),
                f.height(),
                f.off1X(),
                f.off1Y(),
                f.off2X(),
                f.off2Y(),
                converted));
      }
      System.out.println(
          "Converted " + frames.size() + " frame(s): " + c.source() + " -> " + c.target());
    }

    // Bare "sprites.bin" anchor (see MythicVfxPacker); each target family is replaced wholesale.
    Path anchor = spriteDir.resolve(SpriteBinIO.legacyName(SpriteBinIO.DEFAULT_BASE_NAME));
    int written =
        SpriteBinWriter.replaceMatching(
            anchor,
            entries,
            name ->
                CONVERSIONS.stream().anyMatch(c -> MythicVfxPacker.isFrameOf(name, c.target())));
    System.out.println("Wrote " + written + " grand VFX sprite entries to " + spriteDir);
  }
}
