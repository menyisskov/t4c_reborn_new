package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.SpriteBinWriter;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

/** T4C-0082: the packer that turns externally drawn frames into playable, aligned sprites. */
class MythicVfxPackerTest {
  private static final int RED = 0xFFFF0000;

  @Test
  void frameNamesFollowTheLegacyMultiRowScheme() {
    assertEquals("MythicFire-a", MythicVfxPacker.frameName("MythicFire", 0));
    assertEquals("MythicFire-z", MythicVfxPacker.frameName("MythicFire", 25));
    assertEquals("MythicFire-2a", MythicVfxPacker.frameName("MythicFire", 26));
    assertEquals("MythicFire-2g", MythicVfxPacker.frameName("MythicFire", 32));
  }

  @Test
  void onlyExactFramesCountAsTheFamily() {
    assertTrue(MythicVfxPacker.isFrameOf("MythicFire-a", "MythicFire"));
    assertTrue(MythicVfxPacker.isFrameOf("mythicfire-2B", "MythicFire"));
    assertFalse(MythicVfxPacker.isFrameOf("MythicFire-Ascended-a", "MythicFire"));
    assertFalse(MythicVfxPacker.isFrameOf("MythicFireball-a", "MythicFire"));
    assertFalse(MythicVfxPacker.isFrameOf("MythicFire", "MythicFire"));
  }

  @Test
  void stripSheetSlicesIntoSquareCells() {
    List<BufferedImage> frames =
        MythicVfxPacker.sliceSheet(new BufferedImage(64, 16, BufferedImage.TYPE_INT_ARGB), 0, 0);
    assertEquals(4, frames.size());
    assertEquals(16, frames.get(3).getWidth());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            MythicVfxPacker.sliceSheet(
                new BufferedImage(30, 16, BufferedImage.TYPE_INT_ARGB), 0, 0));
  }

  @Test
  void gridSheetSlicesRowByRow() {
    BufferedImage sheet = new BufferedImage(40, 20, BufferedImage.TYPE_INT_ARGB);
    sheet.setRGB(12, 11, RED); // col 1, row 1 of a 4-column grid of 10x10 cells = frame 5
    List<BufferedImage> frames = MythicVfxPacker.sliceSheet(sheet, 7, 4);
    assertEquals(7, frames.size());
    assertEquals(RED, frames.get(5).getRGB(2, 1));
  }

  @Test
  void packDownscalesSnapsAlphaTrimsAndAlignsToTheReference() {
    // Two 4x-upscaled 16x16 canvases: a 2x2 dot, then a 6x4 blast further left and lower.
    BufferedImage a = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    fill(a, 4, 7, 7, 2, 2, RED);
    fill(a, 4, 0, 0, 1, 1, 0x40FFFFFF); // faint haze: snapped away, not trimmed around
    BufferedImage b = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    fill(b, 4, 5, 8, 6, 4, 0xC0FF8800); // semi-opaque: becomes fully opaque

    MythicVfxPacker.Bounds reference = new MythicVfxPacker.Bounds(-30, -100, 60, -10);
    List<SpriteBinWriter.Entry> entries =
        MythicVfxPacker.pack(List.of(a, b), "MythicTest", 4, reference);

    assertEquals(2, entries.size());
    SpriteBinWriter.Entry first = entries.get(0);
    SpriteBinWriter.Entry second = entries.get(1);
    assertEquals("MythicTest-a", first.name());
    assertEquals("MythicTest-b", second.name());
    assertEquals(2, first.width());
    assertEquals(2, first.height());
    assertEquals(6, second.width());
    assertEquals(4, second.height());
    assertEquals(0xFFFF8800, second.image().getRGB(0, 0));

    // Relative placement between frames is untouched: b starts 2px left and 1px lower than a.
    assertEquals(first.off1X() - 2, second.off1X());
    assertEquals(first.off1Y() + 1, second.off1Y());
    // The union box (canvas x 5..10, y 7..11) is centered on the reference center (15, -55).
    double unionCenterX = (second.off1X() + second.off1X() + second.width() - 1) / 2.0;
    double unionCenterY = (first.off1Y() + second.off1Y() + second.height() - 1) / 2.0;
    assertTrue(Math.abs(unionCenterX - 15) <= 1, "centered horizontally: " + unionCenterX);
    assertTrue(Math.abs(unionCenterY + 55) <= 1, "centered vertically: " + unionCenterY);

    for (SpriteBinWriter.Entry e : entries) {
      assertEquals(MythicVfxPacker.TILE_MIRROR - e.width() - e.off1X(), e.off2X());
      assertEquals(e.off1Y(), e.off2Y());
    }
  }

  @Test
  void frameFilesSortNumericallyNotAlphabetically() {
    List<java.nio.file.Path> files =
        new java.util.ArrayList<>(
            List.of(
                java.nio.file.Path.of("frame_10.png"),
                java.nio.file.Path.of("frame_2.png"),
                java.nio.file.Path.of("frame_1.png")));
    files.sort(MythicVfxPacker::compareFrameFiles);
    assertEquals("frame_1.png", files.get(0).toString());
    assertEquals("frame_2.png", files.get(1).toString());
    assertEquals("frame_10.png", files.get(2).toString());
  }

  @Test
  void holdRepeatsEveryFrameInPlace() {
    BufferedImage a = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    BufferedImage b = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    assertEquals(List.of(a, a, b, b), MythicVfxPacker.hold(List.of(a, b), 2));
    assertEquals(List.of(a, b), MythicVfxPacker.hold(List.of(a, b), 1));
  }

  @Test
  void stripMatteClearsOnlyTheNeutralMatteBand() {
    BufferedImage img = new BufferedImage(4, 1, BufferedImage.TYPE_INT_ARGB);
    img.setRGB(0, 0, 0xFF7F7F7F); // matte (127)
    img.setRGB(1, 0, 0xFF838080); // matte within tolerance
    img.setRGB(2, 0, 0xFF707070); // darker smoke gray: kept
    img.setRGB(3, 0, 0xFF8080C0); // tinted: kept
    BufferedImage out = MythicVfxPacker.stripMatte(img);
    assertEquals(0, out.getRGB(0, 0) >>> 24);
    assertEquals(0, out.getRGB(1, 0) >>> 24);
    assertEquals(0xFF707070, out.getRGB(2, 0));
    assertEquals(0xFF8080C0, out.getRGB(3, 0));
  }

  @Test
  void growInScalesTheFirstFrameUpFromItsBase() {
    BufferedImage first = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
    fill(first, 1, 2, 0, 4, 8, RED); // 4 wide, 8 tall, base on the bottom row
    List<BufferedImage> out = MythicVfxPacker.growIn(List.of(first), 1);
    assertEquals(2, out.size());
    assertEquals(first, out.get(1));
    int[] box = MythicVfxPacker.opaqueBox(out.get(0));
    // Half size, still standing on the same base row and centered on the same column.
    assertEquals(7, box[3]);
    assertEquals(4, box[3] - box[1] + 1);
    assertEquals(2, box[2] - box[0] + 1);
    assertEquals(3, box[0]);
  }

  @Test
  void groundAlignmentPutsTheLowestPixelOnTheGroundAndCentersOnTheTile() {
    BufferedImage a = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
    fill(a, 1, 3, 5, 6, 10, RED);
    List<SpriteBinWriter.Entry> entries =
        MythicVfxPacker.pack(List.of(a), "MythicTest", 1, null, 0, false);
    SpriteBinWriter.Entry e = entries.get(0);
    assertEquals(0, e.off1Y() + e.height() - 1);
    assertEquals(MythicVfxPacker.TILE_MIRROR / 2, e.off1X() + (e.width() - 1) / 2);
  }

  @Test
  void lumaAlphaTurnsAdditiveArtIntoTheSameLookWithoutTheBlackField() {
    BufferedImage img = new BufferedImage(3, 1, BufferedImage.TYPE_INT_ARGB);
    img.setRGB(0, 0, 0xFF000000); // opaque black background of additive art
    img.setRGB(1, 0, 0xFF804020); // dim orange glow
    img.setRGB(2, 0, 0xFFFFFFFF); // white-hot core
    BufferedImage out = MythicVfxPacker.lumaAlpha(img);
    assertEquals(0, out.getRGB(0, 0) >>> 24);
    int glow = out.getRGB(1, 0);
    assertEquals(0x80, glow >>> 24); // alpha = brightest channel
    assertEquals(0xFF, (glow >> 16) & 0xFF); // color scaled back up by it
    // Alpha-blended over black, the pixel reproduces the additive original (to rounding).
    assertTrue(Math.abs(((glow >> 8) & 0xFF) * 0x80 / 255 - 0x40) <= 1);
    assertEquals(0xFFFFFFFF, out.getRGB(2, 0));
  }

  @Test
  void softAlphaKeepsPartialAlphaButClearsInvisibleHaze() {
    BufferedImage a = new BufferedImage(6, 6, BufferedImage.TYPE_INT_ARGB);
    fill(a, 1, 2, 2, 2, 2, 0x80FF8000);
    a.setRGB(0, 0, 0x05FFFFFF); // near-invisible: must not widen the trim
    SpriteBinWriter.Entry e =
        MythicVfxPacker.pack(List.of(a), "MythicTest", 1, null, 0, true).get(0);
    assertEquals(2, e.width());
    assertEquals(0x80, e.image().getRGB(0, 0) >>> 24);
  }

  @Test
  void frameOrderMatchesTheRendererRowThenLetter() {
    List<String> names =
        new java.util.ArrayList<>(List.of("Fx-2a", "Fx-B", "Fx-z", "Fx-a", "Fx-2B"));
    names.sort(java.util.Comparator.comparingInt(n -> MythicVfxPacker.frameOrder(n, "Fx")));
    assertEquals(List.of("Fx-a", "Fx-B", "Fx-z", "Fx-2a", "Fx-2B"), names);
  }

  @Test
  void smoothScaleAveragesInsteadOfSampling() {
    BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    img.setRGB(0, 0, 0xFFFFFFFF);
    img.setRGB(1, 1, 0xFFFFFFFF); // checker of opaque white and clear
    BufferedImage out = MythicVfxPacker.smoothScale(img, 0.5);
    assertEquals(1, out.getWidth());
    int alpha = out.getRGB(0, 0) >>> 24;
    assertTrue(alpha > 100 && alpha < 156, "averaged alpha " + alpha);
  }

  @Test
  void referenceBoundsIgnoreTinyPlaceholderFrames() {
    var big = new com.perso.T4C.helper.SpriteBinIO.Packed("Fx-c", 200, 250, -90, -200, -78, -200, 0, new byte[0]);
    var placeholder =
        new com.perso.T4C.helper.SpriteBinIO.Packed("Fx-a", 32, 16, -305, -230, 305, -230, 0, new byte[0]);
    MythicVfxPacker.Bounds b = MythicVfxPacker.boundsOf(List.of(placeholder, big));
    assertEquals(new MythicVfxPacker.Bounds(-90, -200, 109, 49), b);
  }

  @Test
  void fadeOutAppendsTheLastFrameAtDecreasingOpacity() {
    BufferedImage last = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    last.setRGB(0, 0, 0xFF336699);
    List<BufferedImage> out = MythicVfxPacker.fadeOut(List.of(last), 3);
    assertEquals(4, out.size());
    assertEquals(191, out.get(1).getRGB(0, 0) >>> 24); // 3/4
    assertEquals(128, out.get(2).getRGB(0, 0) >>> 24); // 2/4
    assertEquals(64, out.get(3).getRGB(0, 0) >>> 24); // 1/4
    assertEquals(0x336699, out.get(3).getRGB(0, 0) & 0xFFFFFF);
  }

  @Test
  void emptyBeatKeepsItsSlot() {
    BufferedImage a = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
    fill(a, 1, 2, 2, 3, 3, RED);
    BufferedImage empty = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
    List<SpriteBinWriter.Entry> entries =
        MythicVfxPacker.pack(
            List.of(empty, a), "MythicTest", 1, new MythicVfxPacker.Bounds(0, 0, 10, 10));
    assertEquals(2, entries.size());
    assertEquals(1, entries.get(0).width());
    assertEquals(0, entries.get(0).image().getRGB(0, 0) >>> 24);
  }

  /** Fills a w x h block at logical (x, y) of an image upscaled by {@code k}. */
  private static void fill(BufferedImage img, int k, int x, int y, int w, int h, int argb) {
    for (int yy = y * k; yy < (y + h) * k; yy++)
      for (int xx = x * k; xx < (x + w) * k; xx++) img.setRGB(xx, yy, argb);
  }
}
