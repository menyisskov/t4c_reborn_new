package com.perso.T4C.helper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DdaExtractor implements AutoCloseable {
  private static final String DID_FILE = "v2datai.did";
  private static final String DPD_FILE = "v2colori.dpd";
  private final Path ddaDir;
  private final String ddaPrefix;
  private final DidReader did;
  private final DpdReader dpd;
  private final Map<Long, DdaReader> ddaCache = new HashMap<>();

  public DdaExtractor(Path ddaDir) throws IOException {
    this(ddaDir, false);
  }

  public DdaExtractor(Path ddaDir, boolean nmsLibrary) throws IOException {
    this.ddaDir = ddaDir;
    this.ddaPrefix = nmsLibrary ? "v2nmsdata" : "v2data";
    this.did = new DidReader(ddaDir.resolve(nmsLibrary ? "v2nmsdatai.did" : DID_FILE));
    this.dpd = new DpdReader(ddaDir.resolve(nmsLibrary ? "v2nmscolori.dpd" : DPD_FILE));
    log.info("DDA library loaded: {} indexed sprites, {} palettes", did.size(), dpd.paletteCount());
  }

  public DidReader getDid() {
    return did;
  }

  public DpdReader getDpd() {
    return dpd;
  }

  public BufferedImage getSpriteImage(DidReader.Entry entry) throws IOException {
    return getSpriteImage(entry, 0);
  }

  public BufferedImage getSpriteImage(DidReader.Entry entry, int paletteIndex) throws IOException {
    DdaReader reader = ddaReaderFor(entry.numDda);
    DdaReader.Sprite sprite = reader.readSprite(entry.spriteOffset());
    return toImage(sprite, dpd.getPaletteForSpriteName(entry.name, paletteIndex));
  }

  private static final int SHADOW_ARGB = 0x80000000;

  public static BufferedImage toImage(DdaReader.Sprite sprite, DpdReader.Palette palette) {
    int w = sprite.getLargeur();
    int h = sprite.getHauteur();
    BufferedImage img =
        new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
    int trans = sprite.header.couleurTrans & 0xFF;
    byte[] shadow = sprite.shadow;
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int pos = y * w + x;
        int idx = sprite.pixels[pos] & 0xFF;
        int argb;
        if (idx != trans) {
          argb = palette.color(idx);
        } else if (shadow != null && shadow[pos] != 0) {
          argb = SHADOW_ARGB;
        } else {
          argb = 0x00000000;
        }
        img.setRGB(x, y, argb);
      }
    }
    return img;
  }

  public static BufferedImage toGrayscaleMaskImage(DdaReader.Sprite sprite) {
    int w = sprite.getLargeur();
    int h = sprite.getHauteur();
    BufferedImage img =
        new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int weight = sprite.pixels[y * w + x] & 0xFF;
        int argb = 0xFF000000 | (weight << 16) | (weight << 8) | weight;
        img.setRGB(x, y, argb);
      }
    }
    return img;
  }

  public static boolean isMaskSprite(String name, java.util.Set<String> allNamesLower) {
    String lower = name.toLowerCase(Locale.ROOT);
    int dash = lower.lastIndexOf('-');
    if (dash <= 0) {
      return false;
    }
    String prefix = lower.substring(0, dash);
    if (!prefix.endsWith("a")) {
      return false;
    }
    String baseName = prefix.substring(0, prefix.length() - 1) + lower.substring(dash);
    return allNamesLower.contains(baseName);
  }

  public int extractAll(Path outputDir) throws IOException {
    return extractAll(outputDir, 0);
  }

  public int extractAll(Path outputDir, int paletteIndex) throws IOException {
    return extractAll(outputDir, paletteIndex, entry -> true);
  }

  public int extractAll(Path outputDir, int paletteIndex, Predicate<DidReader.Entry> filter)
      throws IOException {
    Files.createDirectories(outputDir);
    int written = 0;
    int errors = 0;
    int n = 0;
    for (DidReader.Entry entry : did.getEntries()) {
      if (!filter.test(entry)) {
        n++;
        continue;
      }
      try {
        DdaReader reader = ddaReaderFor(entry.numDda);
        DdaReader.Sprite sprite = reader.readSprite(entry.spriteOffset());
        DpdReader.Palette palette = dpd.getPaletteForSpriteName(entry.name, paletteIndex);
        BufferedImage img = toImage(sprite, palette);
        Path out = outputDir.resolve(fileName(entry, n));
        ImageIO.write(img, "png", out.toFile());
        written++;
      } catch (Exception e) {
        errors++;
        log.warn("Sprite not extracted: {} ({})", entry, e.getMessage());
      }
      n++;
    }
    log.info("Extraction complete: {} written, {} errors (out of {})", written, errors, did.size());
    return written;
  }

  public int writeSpriteBin(Path outputFile, int paletteIndex) throws IOException {
    return writeSpriteBin(outputFile, paletteIndex, entry -> true);
  }

  public int writeSpriteBin(Path outputFile, int paletteIndex, Predicate<DidReader.Entry> filter)
      throws IOException {
    return writeSpriteBin(outputFile, paletteIndex, filter, false);
  }

  public int mergeSpriteBin(Path outputFile, int paletteIndex, Predicate<DidReader.Entry> filter)
      throws IOException {
    return writeSpriteBin(outputFile, paletteIndex, filter, true);
  }

  public int mergeSpriteBinPaletteVariants(Path outputFile, Map<String, Integer> variants)
      throws IOException {
    List<PaletteVariant> normalized = new ArrayList<>();
    variants.forEach(
        (qualifiedBase, palette) -> {
          String marker = "__pal" + palette;
          String sourceBase =
              qualifiedBase.endsWith(marker)
                  ? qualifiedBase.substring(0, qualifiedBase.length() - marker.length())
                  : qualifiedBase;
          normalized.add(new PaletteVariant(sourceBase, qualifiedBase, palette));
        });
    Function<DidReader.Entry, List<SpriteEncoding>> resolver =
        entry -> {
          String lower = entry.name.toLowerCase(Locale.ROOT);
          List<SpriteEncoding> encodings = new ArrayList<>();
          for (PaletteVariant variant : normalized) {
            if (!belongsToBase(lower, variant.sourceBase.toLowerCase(Locale.ROOT))) continue;
            String renamed = variant.outputBase + entry.name.substring(variant.sourceBase.length());
            encodings.add(new SpriteEncoding(renamed, variant.palette));
          }
          return encodings;
        };
    return writeSpriteBin(outputFile, resolver, true);
  }

  private static boolean belongsToBase(String spriteName, String base) {
    if (!spriteName.startsWith(base)) return false;
    String suffix = spriteName.substring(base.length());
    if (suffix.isEmpty()) return true;
    if (suffix.length() >= 3
        && Character.isDigit(suffix.charAt(0))
        && Character.isDigit(suffix.charAt(1))
        && Character.isDigit(suffix.charAt(2))) return true;
    return suffix.length() >= 4
        && (suffix.charAt(0) == 'a' || suffix.charAt(0) == 'b')
        && Character.isDigit(suffix.charAt(1))
        && Character.isDigit(suffix.charAt(2))
        && Character.isDigit(suffix.charAt(3));
  }

  private int writeSpriteBin(
      Path outputFile, int paletteIndex, Predicate<DidReader.Entry> filter, boolean mergeExisting)
      throws IOException {
    return writeSpriteBin(
        outputFile,
        entry ->
            filter.test(entry) ? List.of(new SpriteEncoding(entry.name, paletteIndex)) : List.of(),
        mergeExisting);
  }

  private int writeSpriteBin(
      Path outputFile,
      Function<DidReader.Entry, List<SpriteEncoding>> resolver,
      boolean mergeExisting)
      throws IOException {
    int encoded = 0;
    int errors = 0;
    int filtered = 0;
    List<SpriteBinIO.Packed> sprites = new ArrayList<>();
    Map<String, Integer> firstIndexByName = new HashMap<>();
    Path outputDir = outputFile.getParent() != null ? outputFile.getParent() : Path.of(".");
    Files.createDirectories(outputDir);
    String outputBase = spriteBinBaseName(outputFile);
    if (mergeExisting) {
      SpriteBinIO.readAll(
          outputDir,
          outputBase,
          packed -> {
            firstIndexByName.putIfAbsent(spriteKey(packed.name()), sprites.size());
            sprites.add(packed);
          });
    }
    java.util.Set<String> allNamesLower = new java.util.HashSet<>();
    for (DidReader.Entry entry : did.getEntries()) {
      allNamesLower.add(entry.name.toLowerCase(Locale.ROOT));
    }
    for (DidReader.Entry entry : did.getEntries()) {
      List<SpriteEncoding> encodings = resolver.apply(entry);
      if (encodings == null || encodings.isEmpty()) {
        filtered++;
        continue;
      }
      for (SpriteEncoding encoding : encodings) {
        try {
          DdaReader reader = ddaReaderFor(entry.numDda);
          DdaReader.Sprite sprite = reader.readSprite(entry.spriteOffset());
          BufferedImage img;
          if (isMaskSprite(entry.name, allNamesLower)) {
            img = toGrayscaleMaskImage(sprite);
          } else {
            DpdReader.Palette palette = dpd.getPaletteForSpriteName(entry.name, encoding.palette);
            img = toImage(sprite, palette);
          }
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ImageIO.write(img, "png", baos);
          SpriteBinIO.Packed packed =
              new SpriteBinIO.Packed(
                  encoding.name,
                  sprite.header.largeur,
                  sprite.header.hauteur,
                  sprite.header.offsetX,
                  sprite.header.offsetY,
                  sprite.header.offsetX2,
                  sprite.header.offsetY2,
                  spriteType(sprite),
                  baos.toByteArray());
          Integer existingIndex = firstIndexByName.get(spriteKey(encoding.name));
          if (existingIndex != null) {
            sprites.set(existingIndex, packed);
          } else {
            firstIndexByName.put(spriteKey(encoding.name), sprites.size());
            sprites.add(packed);
          }
          encoded++;
        } catch (Exception e) {
          errors++;
          log.warn("Sprite not encoded: {} as {} ({})", entry, encoding.name, e.getMessage());
        }
      }
    }
    int shardCount = SpriteBinIO.writeSharded(outputDir, outputBase, sprites);
    log.info(
        "{} written as {} shard(s): {} sprites encoded, {} total sprites, {} skipped",
        outputBase,
        shardCount,
        encoded,
        sprites.size(),
        errors + filtered);
    return encoded;
  }

  public int writeSpriteBin(Path outputFile) throws IOException {
    return writeSpriteBin(outputFile, 0);
  }

  private static String spriteKey(String name) {
    return name.toLowerCase(Locale.ROOT);
  }

  private static String spriteBinBaseName(Path spriteBin) {
    String fileName = spriteBin.getFileName().toString();
    return fileName.endsWith(".bin")
        ? fileName.substring(0, fileName.length() - ".bin".length())
        : fileName;
  }

  private record SpriteEncoding(String name, int palette) {}

  private record PaletteVariant(String sourceBase, String outputBase, int palette) {}

  private static int spriteType(DdaReader.Sprite sprite) {
    return sprite.header.largeur == 32 && sprite.header.hauteur == 16 ? 0 : 1;
  }

  private DdaReader ddaReaderFor(long numDda) throws IOException {
    DdaReader r = ddaCache.get(numDda);
    if (r == null) {
      Path f = ddaDir.resolve(String.format(ddaPrefix + "%02d.dda", numDda));
      r = new DdaReader(f);
      ddaCache.put(numDda, r);
    }
    return r;
  }

  private static String fileName(DidReader.Entry entry, int index) {
    String safe = entry.name.replaceAll("[^a-zA-Z0-9._-]", "_").trim();
    return safe.isEmpty()
        ? String.format("sprite_%05d.png", index)
        : String.format("%05d_%s.png", index, safe);
  }

  @Override
  public void close() {
    ddaCache.clear();
  }
}
