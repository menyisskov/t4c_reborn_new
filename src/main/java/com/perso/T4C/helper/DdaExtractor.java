package com.perso.T4C.helper;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
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
import java.util.function.Predicate;
import java.util.function.Function;

/**
 * Orchestrateur de la librairie graphique de The 4th Coming.
 *
 * <p>Relie les trois formats :
 * <ul>
 *   <li>{@link DidReader} : l'index (quel sprite est dans quel .dda, à quel offset),</li>
 *   <li>{@link DdaReader} : le décryptage/décompression d'un sprite (pixels indicés),</li>
 *   <li>{@link DpdReader} : les palettes (index → couleur RGB).</li>
 * </ul>
 *
 * <p>Produit des {@link BufferedImage} ARGB (la couleur transparente du sprite est
 * rendue avec un alpha nul) et permet l'extraction complète vers des fichiers PNG.
 *
 * <p>Le format ne stocke pas d'index de palette par sprite : on utilise la palette 0
 * par défaut (surchargeable via les variantes {@code paletteIndex}).
 */
@Slf4j
public class DdaExtractor implements AutoCloseable {

    private static final String DID_FILE = "v2datai.did";
    private static final String DPD_FILE = "v2colori.dpd";

    private final Path ddaDir;
    private final String ddaPrefix;
    private final DidReader did;
    private final DpdReader dpd;
    private final Map<Long, DdaReader> ddaCache = new HashMap<>();

    /**
     * @param ddaDir dossier contenant v2datai.did, v2colori.dpd et les v2dataNN.dda
     */
    public DdaExtractor(Path ddaDir) throws IOException {
        this(ddaDir, false);
    }

    /** Opens either the base V2 library or the separate NMS extension library. */
    public DdaExtractor(Path ddaDir, boolean nmsLibrary) throws IOException {
        this.ddaDir = ddaDir;
        this.ddaPrefix = nmsLibrary ? "v2nmsdata" : "v2data";
        this.did = new DidReader(ddaDir.resolve(nmsLibrary ? "v2nmsdatai.did" : DID_FILE));
        this.dpd = new DpdReader(ddaDir.resolve(nmsLibrary ? "v2nmscolori.dpd" : DPD_FILE));
        log.info("DDA library loaded: {} indexed sprites, {} palettes",
                did.size(), dpd.paletteCount());
    }

    public DidReader getDid() { return did; }

    public DpdReader getDpd() { return dpd; }

    /** Image ARGB d'une entrée d'index (palette 0). */
    public BufferedImage getSpriteImage(DidReader.Entry entry) throws IOException {
        return getSpriteImage(entry, 0);
    }

    /** Image ARGB d'une entrée d'index avec la palette donnée. */
    public BufferedImage getSpriteImage(DidReader.Entry entry, int paletteIndex) throws IOException {
        DdaReader reader = ddaReaderFor(entry.numDda);
        DdaReader.Sprite sprite = reader.readSprite(entry.spriteOffset());
        return toImage(sprite, dpd.getPaletteForSpriteName(entry.name, paletteIndex));
    }

    /**
     * Couleur d'un pixel d'ombre portée : noir à 50 % d'alpha. Le client d'origine trame du noir
     * pur un pixel sur deux ({@code DrawNCKSprite}), faute de canal alpha sur ses surfaces 16
     * bits ; un vrai alpha rend la même densité sans moirage. Même valeur que le pipeline
     * {@code tools/merge_vsf_to_sprite_bin.py}.
     */
    private static final int SHADOW_ARGB = 0x80000000;

    /**
     * Convertit un sprite (pixels indicés) en image ARGB via une palette, en composant l'ombre
     * portée éventuelle sous les pixels du sprite.
     */
    public static BufferedImage toImage(DdaReader.Sprite sprite, DpdReader.Palette palette) {
        int w = sprite.getLargeur();
        int h = sprite.getHauteur();
        BufferedImage img = new BufferedImage(Math.max(1, w), Math.max(1, h),
                BufferedImage.TYPE_INT_ARGB);
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
                    // L'ombre n'apparaît que là où le sprite est transparent : elle se projette
                    // sur le décor, jamais par-dessus l'objet lui-meme.
                    argb = SHADOW_ARGB;
                } else {
                    argb = 0x00000000;
                }
                img.setRGB(x, y, argb);
            }
        }
        return img;
    }

    /**
     * Convertit un sprite "masque alpha" (poids de transparence brut par pixel, un octet =
     * un poids 0-255, pas une palette) en image en niveaux de gris opaque. Utilisé par les
     * sprites de mask de sorts nommés {@code <Base>A-<frame>} en compagnon de {@code <Base>-<frame>},
     * exactement comme le client C++ d'origine lit {@code Data.lpbChunck} en poids d'alpha bruts
     * (voir {@code TransAlphaMask2} dans V2Sprite.cpp) plutôt que de les indexer dans une palette.
     */
    public static BufferedImage toGrayscaleMaskImage(DdaReader.Sprite sprite) {
        int w = sprite.getLargeur();
        int h = sprite.getHauteur();
        BufferedImage img = new BufferedImage(Math.max(1, w), Math.max(1, h),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int weight = sprite.pixels[y * w + x] & 0xFF;
                int argb = 0xFF000000 | (weight << 16) | (weight << 8) | weight;
                img.setRGB(x, y, argb);
            }
        }
        return img;
    }

    /**
     * Un sprite est un "masque" si son nom s'obtient en insérant un 'A' juste avant le
     * dernier segment de nom de frame (ex. {@code BoulderFireA-a} pour la base
     * {@code BoulderFire-a}), et que ce sprite de base existe dans le même index.
     */
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

    /**
     * Extrait tous les sprites de l'index vers des fichiers PNG.
     *
     * @param outputDir dossier de sortie (créé si besoin)
     * @return le nombre de sprites écrits
     */
    public int extractAll(Path outputDir) throws IOException {
        return extractAll(outputDir, 0);
    }

    public int extractAll(Path outputDir, int paletteIndex) throws IOException {
        return extractAll(outputDir, paletteIndex, entry -> true);
    }

    public int extractAll(Path outputDir, int paletteIndex, Predicate<DidReader.Entry> filter) throws IOException {
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
        log.info("Extraction complete: {} written, {} errors (out of {})",
                written, errors, did.size());
        return written;
    }

    /**
     * Ecrit tous les sprites de l'index dans le meme format que sprites.bin :
     * magic T4C1, version, nombre de sprites, puis metadata + payload PNG.
     *
     * @param outputFile fichier de sortie (ex. {@code assets/sprites/sprites2.bin})
     * @param paletteIndex palette utilisée pour le rendu
     * @return le nombre de sprites effectivement encodés (longueur non nulle)
     */
    public int writeSpriteBin(Path outputFile, int paletteIndex) throws IOException {
        return writeSpriteBin(outputFile, paletteIndex, entry -> true);
    }

    public int writeSpriteBin(Path outputFile, int paletteIndex, Predicate<DidReader.Entry> filter) throws IOException {
        return writeSpriteBin(outputFile, paletteIndex, filter, false);
    }

    public int mergeSpriteBin(Path outputFile, int paletteIndex, Predicate<DidReader.Entry> filter) throws IOException {
        return writeSpriteBin(outputFile, paletteIndex, filter, true);
    }

    /**
     * Merges palette-qualified copies of sprite families. A mapping such as
     * {@code PupPlateBody -> 6} writes {@code PupPlateBody__pal6000-a}, keeping
     * palette variants addressable independently by the animation loader.
     */
    public int mergeSpriteBinPaletteVariants(Path outputFile, Map<String, Integer> variants) throws IOException {
        List<PaletteVariant> normalized = new ArrayList<>();
        variants.forEach((qualifiedBase, palette) -> {
            String marker = "__pal" + palette;
            String sourceBase = qualifiedBase.endsWith(marker)
                    ? qualifiedBase.substring(0, qualifiedBase.length() - marker.length()) : qualifiedBase;
            normalized.add(new PaletteVariant(sourceBase, qualifiedBase, palette));
        });
        Function<DidReader.Entry, List<SpriteEncoding>> resolver = entry -> {
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
        if (suffix.length() >= 3 && Character.isDigit(suffix.charAt(0))
                && Character.isDigit(suffix.charAt(1)) && Character.isDigit(suffix.charAt(2))) return true;
        return suffix.length() >= 4 && (suffix.charAt(0) == 'a' || suffix.charAt(0) == 'b')
                && Character.isDigit(suffix.charAt(1)) && Character.isDigit(suffix.charAt(2))
                && Character.isDigit(suffix.charAt(3));
    }

    private int writeSpriteBin(Path outputFile, int paletteIndex, Predicate<DidReader.Entry> filter, boolean mergeExisting) throws IOException {
        return writeSpriteBin(outputFile,
                entry -> filter.test(entry) ? List.of(new SpriteEncoding(entry.name, paletteIndex)) : List.of(),
                mergeExisting);
    }

    private int writeSpriteBin(Path outputFile, Function<DidReader.Entry, List<SpriteEncoding>> resolver,
            boolean mergeExisting) throws IOException {
        int encoded = 0;
        int errors = 0;
        int filtered = 0;
        List<SpriteBinIO.Packed> sprites = new ArrayList<>();
        Map<String, Integer> firstIndexByName = new HashMap<>();

        Path outputDir = outputFile.getParent() != null ? outputFile.getParent() : Path.of(".");
        Files.createDirectories(outputDir);
        String outputBase = spriteBinBaseName(outputFile);

        if (mergeExisting) {
            SpriteBinIO.readAll(outputDir, outputBase, packed -> {
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

                    SpriteBinIO.Packed packed = new SpriteBinIO.Packed(encoding.name,
                            sprite.header.largeur, sprite.header.hauteur,
                            sprite.header.offsetX, sprite.header.offsetY, sprite.header.offsetX2, sprite.header.offsetY2,
                            spriteType(sprite), baos.toByteArray());
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

        log.info("{} written as {} shard(s): {} sprites encoded, {} total sprites, {} skipped",
                outputBase, shardCount, encoded, sprites.size(), errors + filtered);
        return encoded;
    }

    public int writeSpriteBin(Path outputFile) throws IOException {
        return writeSpriteBin(outputFile, 0);
    }

    private static String spriteKey(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /** {@code .../sprites.bin} → {@code sprites}, the base name of the shards. */
    private static String spriteBinBaseName(Path spriteBin) {
        String fileName = spriteBin.getFileName().toString();
        return fileName.endsWith(".bin")
                ? fileName.substring(0, fileName.length() - ".bin".length())
                : fileName;
    }

    private record SpriteEncoding(String name, int palette) {
    }

    private record PaletteVariant(String sourceBase, String outputBase, int palette) {
    }

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

    /** Nom de fichier d'un sprite : index zéro-paddé + nom lisible nettoyé. */
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
