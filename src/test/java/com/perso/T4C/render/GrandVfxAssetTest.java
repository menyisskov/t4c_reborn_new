package com.perso.T4C.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.tools.GrandVfxGenerator;
import com.perso.T4C.tools.MythicVfxPacker;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/**
 * T4C-0083: the level-300 "Grand" impacts - the original game's top-tier spell animations - are
 * all packed, and each alpha-blended copy made by {@code tools.GrandVfxGenerator} matches its
 * additive original frame for frame (count, play order, size, draw offsets) with the black field
 * that caused a halo actually gone.
 */
class GrandVfxAssetTest {
  private static final int MIN_FRAMES = 8;

  @Test
  void everyGrandImpactIsPacked() throws Exception {
    List<SpriteBinIO.Packed> all = readAll();
    for (String base : HighTierSpellCurve.GRAND_IMPACTS.values()) {
      assertTrue(framesOf(all, base).size() >= MIN_FRAMES, base + " is missing or too short");
    }
  }

  @Test
  void everyConvertedCopyMatchesItsOriginalFrameForFrame() throws Exception {
    List<SpriteBinIO.Packed> all = readAll();
    for (GrandVfxGenerator.Conversion c : GrandVfxGenerator.CONVERSIONS) {
      List<SpriteBinIO.Packed> source = framesOf(all, c.source());
      List<SpriteBinIO.Packed> copy = framesOf(all, c.target());
      assertEquals(source.size(), copy.size(), c.target() + " frame count");
      for (int i = 0; i < source.size(); i++) {
        SpriteBinIO.Packed s = source.get(i);
        SpriteBinIO.Packed t = copy.get(i);
        assertEquals(MythicVfxPacker.frameName(c.target(), i), t.name());
        assertEquals(s.width(), t.width(), t.name() + " width");
        assertEquals(s.height(), t.height(), t.name() + " height");
        assertEquals(s.off1X(), t.off1X(), t.name() + " off1X");
        assertEquals(s.off1Y(), t.off1Y(), t.name() + " off1Y");
        assertEquals(s.off2X(), t.off2X(), t.name() + " off2X");
        assertEquals(s.off2Y(), t.off2Y(), t.name() + " off2Y");
      }
      // The peak frame must have no opaque near-black pixels left: that is the halo.
      SpriteBinIO.Packed peak = copy.get(copy.size() / 2);
      BufferedImage img = ImageIO.read(new ByteArrayInputStream(peak.png()));
      int opaqueDark = 0;
      for (int y = 0; y < img.getHeight(); y++) {
        for (int x = 0; x < img.getWidth(); x++) {
          int argb = img.getRGB(x, y);
          int max = Math.max((argb >> 16) & 0xFF, Math.max((argb >> 8) & 0xFF, argb & 0xFF));
          if ((argb >>> 24) > 200 && max < 40) opaqueDark++;
        }
      }
      assertEquals(0, opaqueDark, peak.name() + " still has an opaque black halo");
    }
  }

  private static List<SpriteBinIO.Packed> readAll() throws Exception {
    List<SpriteBinIO.Packed> all = new ArrayList<>();
    SpriteBinIO.readAll(Path.of("assets/sprites"), SpriteBinIO.DEFAULT_BASE_NAME, all::add);
    return all;
  }

  private static List<SpriteBinIO.Packed> framesOf(List<SpriteBinIO.Packed> all, String base) {
    List<SpriteBinIO.Packed> out = new ArrayList<>();
    for (SpriteBinIO.Packed p : all) if (MythicVfxPacker.isFrameOf(p.name(), base)) out.add(p);
    out.sort(Comparator.comparingInt(p -> MythicVfxPacker.frameOrder(p.name(), base)));
    return out;
  }
}
