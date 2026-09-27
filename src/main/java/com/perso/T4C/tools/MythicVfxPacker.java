package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.SpriteBinWriter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

/**
 * Packs a brand-new, externally drawn spell impact animation (T4C-0079) into the sprite bins, so
 * the level 350+ rung of {@link com.perso.T4C.spell.HighTierSpellCurve} can use genuinely new art
 * instead of a recolor of legacy frames (T4C-0077). The art comes from outside the repo (e.g. the
 * SpriteCook generator) as either a folder of per-frame PNGs or a single spritesheet; this tool
 * turns it into packed entries that {@code SpellRenderer.resolveFrameNames} can play:
 *
 * <ul>
 *   <li>frames are named {@code <Base>-a}, {@code <Base>-b}, ... then {@code <Base>-2a}, ... past
 *       26, the same scheme the legacy 33-frame {@code GreatExplosion} uses;
 *   <li>generated pixel art is often delivered upscaled - {@code --downscale k} samples one pixel
 *       per k x k block (nearest, so no new colors appear), and alpha is snapped to fully opaque or
 *       fully clear (the textures are Nearest-filtered; soft edges would read as a halo);
 *   <li>each frame is trimmed to its opaque bounds, and draw offsets are computed so the whole
 *       animation sits where a reference legacy animation ({@code --match}, default
 *       {@code GreatExplosion}) sits over the target tile. {@code off2} mirrors {@code off1}
 *       around the 32-pixel tile center, the relation every legacy sprite satisfies.
 * </ul>
 *
 * <p>Usage: {@code MythicVfxPacker <input dir|sheet.png> <TargetBase> [--frames N] [--cols C]
 * [--downscale k] [--match RefBase] [--preview dir] [--dry-run] [--sprites assets/sprites]}.
 * Re-running with the same {@code TargetBase} replaces that animation's old frames entirely (so a
 * shorter re-generation never leaves stale trailing frames). Run once; the packed frames are
 * committed like any other asset.
 */
public final class MythicVfxPacker {
  /** Legacy sprites mirror around the tile center: off2X = TILE_MIRROR - width - off1X. */
  public static final int TILE_MIRROR = 32;

  private static final int ALPHA_CUTOFF = 128;

  private MythicVfxPacker() {}

  /** Screen-space box (offsets relative to the tile) an animation's frames cover. */
  record Bounds(int minX, int minY, int maxX, int maxY) {
    int centerX() {
      return Math.floorDiv(minX + maxX, 2);
    }

    int centerY() {
      return Math.floorDiv(minY + maxY, 2);
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 3 && args[0].equals("--export")) {
      exportReference(Path.of("assets/sprites"), args[1], Path.of(args[2]));
      return;
    }
    if (args.length < 2) {
      System.err.println(
          "Usage: MythicVfxPacker <input dir|sheet.png> <TargetBase> [--frames N] [--cols C]"
              + " [--downscale k] [--hold n] [--match RefBase] [--preview dir] [--dry-run]"
              + " [--sprites assets/sprites]\n"
              + "       MythicVfxPacker --export <RefBase> <outDir>");
      System.exit(2);
    }
    Path input = Path.of(args[0]);
    String targetBase = args[1];
    int frames = 0;
    int cols = 0;
    int downscale = 1;
    int hold = 1;
    String match = "GreatExplosion";
    Path preview = null;
    boolean dryRun = false;
    Path spriteDir = Path.of("assets/sprites");
    for (int i = 2; i < args.length; i++) {
      switch (args[i]) {
        case "--frames" -> frames = Integer.parseInt(args[++i]);
        case "--cols" -> cols = Integer.parseInt(args[++i]);
        case "--downscale" -> downscale = Integer.parseInt(args[++i]);
        case "--hold" -> hold = Integer.parseInt(args[++i]);
        case "--match" -> match = args[++i];
        case "--preview" -> preview = Path.of(args[++i]);
        case "--dry-run" -> dryRun = true;
        case "--sprites" -> spriteDir = Path.of(args[++i]);
        default -> throw new IllegalArgumentException("Unknown option: " + args[i]);
      }
    }
    if (targetBase.endsWith("-") || targetBase.contains(" ")) {
      throw new IllegalArgumentException("TargetBase must be a bare name like MythicFire");
    }

    List<BufferedImage> raw = hold(readFrames(input, frames, cols), hold);
    Bounds reference = referenceBounds(spriteDir, match);
    List<SpriteBinWriter.Entry> entries = pack(raw, targetBase, downscale, reference);
    System.out.println(
        "Prepared " + entries.size() + " frame(s) for " + targetBase + ", aligned to " + match
            + " " + reference);
    for (SpriteBinWriter.Entry e : entries) {
      System.out.println(
          "  " + e.name() + " " + e.width() + "x" + e.height() + " o1=" + e.off1X() + ","
              + e.off1Y() + " o2=" + e.off2X() + "," + e.off2Y());
    }
    if (preview != null) {
      writePreview(entries, preview);
      System.out.println("Preview written to " + preview);
    }
    if (dryRun) {
      System.out.println("Dry run: sprite bins not modified.");
      return;
    }
    // Bare "sprites.bin" anchor - SpriteBinWriter strips ".bin" to get the shard base name; an
    // existing shard name like "sprites_0.bin" would mis-shard into sprites_0_0.bin (T4C-0077).
    Path anchor = spriteDir.resolve(SpriteBinIO.legacyName(SpriteBinIO.DEFAULT_BASE_NAME));
    int written = SpriteBinWriter.replaceMatching(anchor, entries, name -> isFrameOf(name, targetBase));
    System.out.println("Wrote " + written + " sprite entries to " + spriteDir);
  }

  /** Frames from a directory of PNGs (sorted by name) or a spritesheet sliced into a grid. */
  static List<BufferedImage> readFrames(Path input, int frames, int cols) throws IOException {
    if (Files.isDirectory(input)) {
      List<Path> files;
      try (Stream<Path> listing = Files.list(input)) {
        files =
            listing
                .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png"))
                .sorted(MythicVfxPacker::compareFrameFiles)
                .toList();
      }
      List<BufferedImage> out = new ArrayList<>();
      for (Path file : files) {
        out.add(toArgb(ImageIO.read(file.toFile())));
      }
      if (out.isEmpty()) throw new IllegalArgumentException("No PNG frames in " + input);
      return out;
    }
    BufferedImage sheet = toArgb(ImageIO.read(input.toFile()));
    return sliceSheet(sheet, frames, cols);
  }

  /**
   * Orders frame files by the last number in their name ("frame_2" before "frame_10"), falling
   * back to plain name order, so unpadded exports never play out of sequence.
   */
  static int compareFrameFiles(Path a, Path b) {
    long na = lastNumber(a.getFileName().toString());
    long nb = lastNumber(b.getFileName().toString());
    if (na >= 0 && nb >= 0 && na != nb) return Long.compare(na, nb);
    return a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString());
  }

  private static long lastNumber(String name) {
    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)(?!.*\\d)").matcher(name);
    return m.find() ? Long.parseLong(m.group(1)) : -1;
  }

  /**
   * Repeats every frame {@code n} times. Impact frames play at one fixed rate, so a 16-frame
   * generated animation held x2 lasts about as long as the 33-frame legacy GreatExplosion.
   */
  static List<BufferedImage> hold(List<BufferedImage> frames, int n) {
    if (n <= 1) return frames;
    List<BufferedImage> out = new ArrayList<>();
    for (BufferedImage f : frames) for (int i = 0; i < n; i++) out.add(f);
    return out;
  }

  /**
   * Writes a legacy animation's frames as plain PNGs (at native size, named in play order) - the
   * style references uploaded to the art generator, so new art matches the game's look.
   */
  static void exportReference(Path spriteDir, String refBase, Path outDir) throws IOException {
    Files.createDirectories(outDir);
    List<SpriteBinIO.Packed> frames = new ArrayList<>();
    SpriteBinIO.readAll(
        spriteDir, SpriteBinIO.DEFAULT_BASE_NAME, p -> {
          if (isFrameOf(p.name(), refBase)) frames.add(p);
        });
    if (frames.isEmpty()) throw new IllegalArgumentException("No frames for " + refBase);
    for (SpriteBinIO.Packed p : frames) {
      Files.write(outDir.resolve(p.name() + ".png"), p.png());
    }
    System.out.println("Exported " + frames.size() + " frame(s) of " + refBase + " to " + outDir);
  }

  /**
   * Slices a sheet into {@code frames} cells, {@code cols} per row. With neither given, the sheet
   * is taken as a horizontal strip of square cells.
   */
  static List<BufferedImage> sliceSheet(BufferedImage sheet, int frames, int cols) {
    if (frames <= 0 && cols <= 0) {
      if (sheet.getWidth() % sheet.getHeight() != 0) {
        throw new IllegalArgumentException(
            "Sheet is not a strip of square cells; pass --frames and --cols");
      }
      frames = sheet.getWidth() / sheet.getHeight();
      cols = frames;
    } else if (cols <= 0) {
      cols = frames;
    } else if (frames <= 0) {
      frames = cols;
    }
    int rows = (frames + cols - 1) / cols;
    if (sheet.getWidth() % cols != 0 || sheet.getHeight() % rows != 0) {
      throw new IllegalArgumentException(
          "Sheet " + sheet.getWidth() + "x" + sheet.getHeight() + " does not divide into " + cols
              + " cols x " + rows + " rows");
    }
    int cellW = sheet.getWidth() / cols;
    int cellH = sheet.getHeight() / rows;
    List<BufferedImage> out = new ArrayList<>();
    for (int i = 0; i < frames; i++) {
      out.add(sheet.getSubimage((i % cols) * cellW, (i / cols) * cellH, cellW, cellH));
    }
    return out;
  }

  /**
   * Turns raw, equally sized canvases into packed entries: downscale, snap alpha, trim, and offset
   * so the animation's overall box is centered on {@code reference}'s center. Every frame shares
   * one canvas-to-screen shift, so motion inside the canvas is preserved exactly.
   */
  static List<SpriteBinWriter.Entry> pack(
      List<BufferedImage> raw, String targetBase, int downscale, Bounds reference) {
    if (raw.isEmpty()) throw new IllegalArgumentException("No frames");
    List<BufferedImage> canvases = new ArrayList<>();
    for (BufferedImage frame : raw) {
      canvases.add(snapAlpha(downscale(frame, downscale)));
    }
    int canvasW = canvases.get(0).getWidth();
    int canvasH = canvases.get(0).getHeight();
    for (BufferedImage c : canvases) {
      if (c.getWidth() != canvasW || c.getHeight() != canvasH) {
        throw new IllegalArgumentException("All frames must share one canvas size");
      }
    }

    // Union of opaque pixels across the animation, in canvas coordinates.
    int[][] boxes = new int[canvases.size()][];
    int uMinX = Integer.MAX_VALUE, uMinY = Integer.MAX_VALUE, uMaxX = -1, uMaxY = -1;
    for (int i = 0; i < canvases.size(); i++) {
      boxes[i] = opaqueBox(canvases.get(i));
      if (boxes[i] == null) continue;
      uMinX = Math.min(uMinX, boxes[i][0]);
      uMinY = Math.min(uMinY, boxes[i][1]);
      uMaxX = Math.max(uMaxX, boxes[i][2]);
      uMaxY = Math.max(uMaxY, boxes[i][3]);
    }
    if (uMaxX < 0) throw new IllegalArgumentException("Every frame is fully transparent");
    int shiftX = reference.centerX() - Math.floorDiv(uMinX + uMaxX, 2);
    int shiftY = reference.centerY() - Math.floorDiv(uMinY + uMaxY, 2);

    List<SpriteBinWriter.Entry> entries = new ArrayList<>();
    for (int i = 0; i < canvases.size(); i++) {
      int[] box = boxes[i];
      BufferedImage image;
      int x0, y0;
      if (box == null) {
        // A deliberately empty beat (e.g. the pause before a flash): keep it as a 1x1 clear frame
        // so frame timing stays intact.
        image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        x0 = Math.floorDiv(uMinX + uMaxX, 2);
        y0 = Math.floorDiv(uMinY + uMaxY, 2);
      } else {
        x0 = box[0];
        y0 = box[1];
        image = copy(canvases.get(i).getSubimage(x0, y0, box[2] - x0 + 1, box[3] - y0 + 1));
      }
      int off1X = shiftX + x0;
      int off1Y = shiftY + y0;
      int off2X = TILE_MIRROR - image.getWidth() - off1X;
      entries.add(
          new SpriteBinWriter.Entry(
              frameName(targetBase, i), image.getWidth(), image.getHeight(), off1X, off1Y, off2X,
              off1Y, image));
    }
    return entries;
  }

  /** {@code Base-a} .. {@code Base-z}, then {@code Base-2a} .. - the legacy multi-row scheme. */
  public static String frameName(String base, int index) {
    char letter = (char) ('a' + index % 26);
    int row = index / 26;
    return base + "-" + (row == 0 ? "" : String.valueOf(row + 1)) + letter;
  }

  /** True for {@code Base-<n?><letter>} exactly - never a longer family like {@code Base-Ascended-a}. */
  public static boolean isFrameOf(String name, String base) {
    if (!name.regionMatches(true, 0, base + "-", 0, base.length() + 1)) return false;
    String rest = name.substring(base.length() + 1);
    return rest.matches("(?i)[0-9]*[a-z]");
  }

  /** Screen box of an existing legacy animation, to align new art where players expect it. */
  static Bounds referenceBounds(Path spriteDir, String refBase) throws IOException {
    int[] b = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
    SpriteBinIO.readAll(
        spriteDir,
        SpriteBinIO.DEFAULT_BASE_NAME,
        p -> {
          if (!isFrameOf(p.name(), refBase)) return;
          b[0] = Math.min(b[0], p.off1X());
          b[1] = Math.min(b[1], p.off1Y());
          b[2] = Math.max(b[2], p.off1X() + p.width() - 1);
          b[3] = Math.max(b[3], p.off1Y() + p.height() - 1);
        });
    if (b[2] == Integer.MIN_VALUE) {
      throw new IllegalArgumentException("Reference animation not found: " + refBase);
    }
    return new Bounds(b[0], b[1], b[2], b[3]);
  }

  static BufferedImage downscale(BufferedImage src, int k) {
    if (k <= 1) return src;
    int w = src.getWidth() / k;
    int h = src.getHeight() / k;
    BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        out.setRGB(x, y, src.getRGB(x * k + k / 2, y * k + k / 2));
      }
    }
    return out;
  }

  static BufferedImage snapAlpha(BufferedImage src) {
    BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < src.getHeight(); y++) {
      for (int x = 0; x < src.getWidth(); x++) {
        int argb = src.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;
        out.setRGB(x, y, alpha >= ALPHA_CUTOFF ? (argb | 0xFF000000) : 0);
      }
    }
    return out;
  }

  /** {minX, minY, maxX, maxY} of opaque pixels, or null when the frame is empty. */
  static int[] opaqueBox(BufferedImage img) {
    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = -1, maxY = -1;
    for (int y = 0; y < img.getHeight(); y++) {
      for (int x = 0; x < img.getWidth(); x++) {
        if (((img.getRGB(x, y) >>> 24) & 0xFF) != 0) {
          minX = Math.min(minX, x);
          minY = Math.min(minY, y);
          maxX = Math.max(maxX, x);
          maxY = Math.max(maxY, y);
        }
      }
    }
    return maxX < 0 ? null : new int[] {minX, minY, maxX, maxY};
  }

  private static BufferedImage toArgb(BufferedImage src) {
    if (src == null) throw new IllegalArgumentException("Unreadable image");
    if (src.getType() == BufferedImage.TYPE_INT_ARGB) return src;
    return copy(src);
  }

  private static BufferedImage copy(BufferedImage src) {
    BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = out.createGraphics();
    g.drawImage(src, 0, 0, null);
    g.dispose();
    return out;
  }

  /**
   * Writes a 3x-scaled contact sheet: every frame placed at its real draw offset over a
   * checkerboard with the target tile outlined, so alignment and motion can be eyeballed before
   * anything is packed.
   */
  static void writePreview(List<SpriteBinWriter.Entry> entries, Path dir) throws IOException {
    Files.createDirectories(dir);
    int minX = 0, minY = 0, maxX = TILE_MIRROR, maxY = 16;
    for (SpriteBinWriter.Entry e : entries) {
      minX = Math.min(minX, e.off1X());
      minY = Math.min(minY, e.off1Y());
      maxX = Math.max(maxX, e.off1X() + e.width());
      maxY = Math.max(maxY, e.off1Y() + e.height());
    }
    int cellW = maxX - minX + 4;
    int cellH = maxY - minY + 4;
    int cols = Math.min(entries.size(), 8);
    int rows = (entries.size() + cols - 1) / cols;
    int scale = 3;
    BufferedImage sheet =
        new BufferedImage(cols * cellW * scale, rows * cellH * scale, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = sheet.createGraphics();
    for (int i = 0; i < entries.size(); i++) {
      SpriteBinWriter.Entry e = entries.get(i);
      int cx = (i % cols) * cellW * scale;
      int cy = (i / cols) * cellH * scale;
      for (int y = 0; y < cellH * scale; y += 12) {
        for (int x = 0; x < cellW * scale; x += 12) {
          g.setColor(((x + y) / 12) % 2 == 0 ? new Color(60, 60, 60) : new Color(80, 80, 80));
          g.fillRect(cx + x, cy + y, 12, 12);
        }
      }
      int ox = cx + (2 - minX) * scale;
      int oy = cy + (2 - minY) * scale;
      g.setColor(new Color(0, 200, 0));
      g.drawRect(ox, oy, TILE_MIRROR * scale, 16 * scale);
      g.drawImage(
          e.image(), ox + e.off1X() * scale, oy + e.off1Y() * scale, e.width() * scale,
          e.height() * scale, null);
      g.setColor(Color.WHITE);
      g.drawString(e.name(), cx + 4, cy + 14);
      ImageIO.write(e.image(), "png", dir.resolve(e.name() + ".png").toFile());
    }
    g.dispose();
    ImageIO.write(sheet, "png", dir.resolve("_contact_sheet.png").toFile());
  }
}
