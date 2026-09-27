package com.perso.T4C.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.tools.MythicVfxPacker;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * T4C-0082: every element that {@link HighTierSpellCurve#MYTHIC_IMPACTS} points at has a complete,
 * packed, newly drawn impact animation (see {@code tools.MythicVfxPacker}) - contiguous frames
 * {@code -a, -b, ...} with no gaps, offsets that mirror around the tile like every legacy sprite,
 * and a base name no other sprite family shares - so a level 350+ spell never points at a missing
 * or half-packed animation.
 */
class MythicVfxAssetTest {
  private static final int MIN_FRAMES = 8;
  private static final int MAX_FRAMES = 64;

  @Test
  void everyMythicImpactIsCompleteAndWellFormed() throws Exception {
    Map<Integer, String> mythic = HighTierSpellCurve.MYTHIC_IMPACTS;
    assertEquals(
        mythic.size(), new HashSet<>(mythic.values()).size(), "Two elements share one animation");
    List<SpriteBinIO.Packed> all = new ArrayList<>();
    SpriteBinIO.readAll(Path.of("assets/sprites"), SpriteBinIO.DEFAULT_BASE_NAME, all::add);

    for (var entry : mythic.entrySet()) {
      int element = entry.getKey();
      String base = entry.getValue();
      assertTrue(
          element >= HighTierSpellCurve.FIRE && element <= HighTierSpellCurve.DARK,
          "Unknown element " + element);
      List<SpriteBinIO.Packed> frames = new ArrayList<>();
      for (SpriteBinIO.Packed p : all) {
        if (!p.name().regionMatches(true, 0, base + "-", 0, base.length() + 1)) continue;
        // Anything else under this prefix would be picked up by the renderer's frame lookup.
        assertTrue(
            MythicVfxPacker.isFrameOf(p.name(), base),
            p.name() + " shares the " + base + "- prefix but is not one of its frames");
        frames.add(p);
      }
      assertTrue(
          frames.size() >= MIN_FRAMES && frames.size() <= MAX_FRAMES,
          base + " has " + frames.size() + " frames");
      Set<String> names = new HashSet<>();
      for (SpriteBinIO.Packed p : frames) names.add(SpriteBinIO.key(p.name()));
      for (int i = 0; i < frames.size(); i++) {
        String expected = MythicVfxPacker.frameName(base, i);
        assertTrue(names.contains(SpriteBinIO.key(expected)), base + " is missing frame " + expected);
      }
      for (SpriteBinIO.Packed p : frames) {
        assertTrue(
            p.width() > 0 && p.height() > 0 && p.width() <= 384 && p.height() <= 384,
            p.name() + " is " + p.width() + "x" + p.height());
        assertEquals(
            MythicVfxPacker.TILE_MIRROR - p.width() - p.off1X(), p.off2X(), p.name() + " off2X");
        assertEquals(p.off1Y(), p.off2Y(), p.name() + " off2Y");
      }
    }
  }
}
