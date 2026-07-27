package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinWriter;
import com.perso.T4C.helper.VsfReader;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Importe une famille de tuiles de sol depuis une librairie .vsf déchiffrée vers
 * {@code sprites.bin}.
 *
 * <p>Contrairement aux sprites libres, un sol est une grille de tuiles 32x16 nommées
 * {@code Base (colonne, ligne)} et accompagnée d'une palette dédiée, stockée dans le .vsf comme
 * une ressource de type 2 : 4 octets d'en-tête puis 256 triplets RGB. Les composantes sont
 * reprises telles quelles, comme le fait {@code DpdReader} pour le format .dda.
 *
 * <p><b>Les libellés de palette sont décalés d'un cran par rapport aux familles</b> : la palette
 * réelle d'un sol est celle qui <em>précède</em> celle qui porte son nom. Le décalage se vérifie
 * sur les familles dont la couleur ne prête pas à discussion — {@code Ground_Water} n'est bleu
 * que sous {@code RockFloorP}, {@code 64KNormalGrass} n'est vert que sous {@code HardrockP} —
 * et {@code Hardrock} est donc une roche brune sous {@code EarthTileP}, la palette qui le précède.
 * C'est le même désordre de libellés que celui déjà constaté sur les masques de sorts
 * (cf. {@link VsfSpellMaskImport}), où les noms de l'index sont permutés d'une famille à l'autre.
 * L'option {@code --palette} permet de forcer un nom si une famille échappe à la règle.
 *
 * <p>Les tuiles sont opaques : l'index 0 est une couleur du décor, pas une couleur de
 * transparence. Aucun canal alpha n'est donc dérivé de l'index.
 *
 * <p>Les librairies ne sont pas toutes bâties pareil, d'où deux options de compatibilité :
 * <ul>
 *   <li>{@code --label-shift <n>} : dans certaines librairies (t4cgamefile7 de la 1.50), le
 *       record d'une tuile est rangé au rang <em>précédent</em> celui qui porte son nom.
 *       {@code -1} lit alors le bon record. Le décalage se vérifie en comptant les libellés
 *       dont le record voisin a bien la géométrie d'une tuile (32x16).</li>
 *   <li>{@code --raw} : les pixels y sont stockés bruts ({@code dataSize == 32*16}, {@code kind=0})
 *       au lieu d'être encodés en RLE. Sans cette option, le décodeur RLE produirait du bruit.</li>
 *   <li>{@code --palette-shift <n>} : le même décalage affecte <em>aussi</em> les libellés de
 *       palette, indépendamment de {@code --label-shift}. Dans t4cgamefile7, les octets de la
 *       palette {@code Bright1} sont rangés sous le libellé qui la précède ({@code Pmenu}) ;
 *       {@code --palette Bright1 --palette-shift -1} lit donc la bonne.</li>
 * </ul>
 *
 * <p>Usage : {@code VsfGroundImport <fichier .dsf> <sprites.bin> <base> [--palette <nom>]
 * [--label-shift <n>] [--raw] [--apply]}
 * <br>Exemple : {@code VsfGroundImport Decrypt.dsf assets/sprites/sprites.bin Hardrock --apply}
 */
public final class VsfGroundImport {

    /** Une tuile de sol est toujours 32x16 dans les librairies d'origine. */
    private static final int TILE_WIDTH = 32;
    private static final int TILE_HEIGHT = 16;

    /** Taille de l'en-tête d'un record de palette, avant les 256 triplets RGB. */
    private static final int PALETTE_HEADER_SIZE = 4;

    private static final int PALETTE_COLORS = 256;

    /** Suffixe du nom de la palette associée à une famille de sol. */
    private static final String PALETTE_SUFFIX = "P";

    private VsfGroundImport() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: VsfGroundImport <decrypted .dsf> <sprites.bin> <ground base name> "
                            + "[--palette <name>] [--apply]");
        }
        Path vsfFile = Path.of(args[0]);
        Path spriteBin = Path.of(args[1]);
        String base = args[2];
        String forcedPalette = null;
        boolean apply = false;
        boolean rawPixels = false;
        int labelShift = 0;
        int paletteShift = 0;
        for (int i = 3; i < args.length; i++) {
            if ("--apply".equals(args[i])) {
                apply = true;
            } else if ("--raw".equals(args[i])) {
                rawPixels = true;
            } else if ("--label-shift".equals(args[i]) && i + 1 < args.length) {
                labelShift = Integer.parseInt(args[++i]);
            } else if ("--palette-shift".equals(args[i]) && i + 1 < args.length) {
                paletteShift = Integer.parseInt(args[++i]);
            } else if ("--palette".equals(args[i]) && i + 1 < args.length) {
                forcedPalette = args[++i];
            }
        }

        byte[] raw = Files.readAllBytes(vsfFile);
        VsfReader reader = new VsfReader(raw);
        String paletteName = forcedPalette != null ? forcedPalette : resolvePaletteName(reader, base);
        int[] palette = readPalette(raw, reader, paletteName, paletteShift);
        List<Tile> tiles = collectTiles(reader, base, labelShift);
        if (tiles.isEmpty()) {
            System.out.println("No ground tile found for base '" + base + "'.");
            return;
        }

        int maxColumn = tiles.stream().mapToInt(Tile::column).max().orElse(0);
        int maxRow = tiles.stream().mapToInt(Tile::row).max().orElse(0);
        System.out.printf("%s: %d tiles, grid %dx%d, palette '%s'%s%n",
                base, tiles.size(), maxColumn, maxRow, paletteName,
                forcedPalette == null ? " (shifted from '" + base + PALETTE_SUFFIX + "')" : " (forced)");

        List<SpriteBinWriter.Entry> entries = new ArrayList<>();
        for (Tile tile : tiles) {
            VsfReader.SpriteHeader header = reader.readSpriteHeader(tile.offset());
            if (header.width() != TILE_WIDTH || header.height() != TILE_HEIGHT) {
                System.out.printf("  SKIP %-22s unexpected size %dx%d%n", tile.name(),
                        header.width(), header.height());
                continue;
            }
            byte[] pixels = rawPixels
                    ? readRawPixels(raw, tile.offset())
                    : reader.readSprite(tile.offset()).pixels();
            entries.add(new SpriteBinWriter.Entry(tile.name(), TILE_WIDTH, TILE_HEIGHT,
                    0, 0, 0, 0, toImage(pixels, palette)));
        }
        System.out.printf("  %d tiles decoded.%n", entries.size());

        if (apply) {
            int written = SpriteBinWriter.merge(spriteBin, entries);
            System.out.printf("%s updated: %d ground tiles merged.%n", spriteBin, written);
        } else {
            System.out.println("Dry run only - re-run with --apply to write " + spriteBin + ".");
        }
    }

    /** Une tuile retenue dans l'index des ressources du .vsf. */
    private record Tile(String name, int offset, int column, int row) {
    }

    /**
     * Rassemble les tuiles {@code Base (colonne, ligne)} d'une famille. Le filtre est exact sur
     * la base : {@code RockFloor} et {@code Rockflor} sont deux sols distincts et une comparaison
     * laxiste mélangerait leurs tuiles.
     */
    private static List<Tile> collectTiles(VsfReader reader, String base, int labelShift) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(base) + " \\((\\d+), (\\d+)\\)$");
        List<VsfReader.Resource> resources = reader.getResources();
        List<Tile> tiles = new ArrayList<>();
        for (int i = 0; i < resources.size(); i++) {
            Matcher matcher = pattern.matcher(resources.get(i).name());
            if (!matcher.matches()) {
                continue;
            }
            // Le nom est autoritaire, mais les pixels peuvent être au rang voisin.
            int recordIndex = i + labelShift;
            if (recordIndex < 0 || recordIndex >= resources.size()
                    || !resources.get(recordIndex).isSprite()) {
                continue;
            }
            tiles.add(new Tile(resources.get(i).name(), resources.get(recordIndex).offset(),
                    Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
        }
        return tiles;
    }

    /**
     * Détermine la palette d'une famille de sol : celle qui précède immédiatement, dans l'ordre
     * de l'index, la palette portant le nom de la famille. Voir l'en-tête de classe pour la
     * justification de ce décalage d'un cran.
     */
    private static String resolvePaletteName(VsfReader reader, String base) {
        List<String> palettes = new ArrayList<>();
        for (VsfReader.Resource resource : reader.getResources()) {
            if (!resource.isSprite()) {
                palettes.add(resource.name());
            }
        }
        String labelled = base + PALETTE_SUFFIX;
        int index = -1;
        for (int i = 0; i < palettes.size(); i++) {
            if (palettes.get(i).equalsIgnoreCase(labelled)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException("No palette labelled '" + labelled
                    + "' in the VSF file; pass --palette <name> explicitly");
        }
        if (index == 0) {
            throw new IllegalArgumentException("Palette '" + labelled
                    + "' is the first of the index, so the shift cannot be applied; "
                    + "pass --palette <name> explicitly");
        }
        return palettes.get(index - 1);
    }

    /**
     * Lit la palette dédiée d'une famille de sol : 4 octets d'en-tête puis 256 triplets RGB
     * repris sans mise à l'échelle, comme {@code DpdReader} le fait pour le .dda.
     */
    private static int[] readPalette(byte[] raw, VsfReader reader, String paletteName, int paletteShift) {
        List<VsfReader.Resource> palettes = new ArrayList<>();
        for (VsfReader.Resource resource : reader.getResources()) {
            if (!resource.isSprite()) {
                palettes.add(resource);
            }
        }
        for (int i = 0; i < palettes.size(); i++) {
            if (!palettes.get(i).name().equalsIgnoreCase(paletteName)) {
                continue;
            }
            // Les libellés de palette peuvent être décalés comme ceux des sprites : les octets
            // de la palette voulue sont alors rangés sous le libellé voisin.
            int target = i + paletteShift;
            if (target < 0 || target >= palettes.size()) {
                throw new IllegalArgumentException("Palette shift " + paletteShift
                        + " puts '" + paletteName + "' outside the index");
            }
            VsfReader.Resource resource = palettes.get(target);
            if (paletteShift != 0) {
                System.out.printf("  palette '%s' read from the record labelled '%s' (shift %+d)%n",
                        paletteName, resource.name(), paletteShift);
            }
            int base = resource.offset() + PALETTE_HEADER_SIZE;
            int[] argb = new int[PALETTE_COLORS];
            for (int c = 0; c < PALETTE_COLORS; c++) {
                int r = raw[base + c * 3] & 0xFF;
                int g = raw[base + c * 3 + 1] & 0xFF;
                int b = raw[base + c * 3 + 2] & 0xFF;
                argb[c] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
            return argb;
        }
        throw new IllegalArgumentException("Palette '" + paletteName + "' not found in the VSF file");
    }

    /**
     * Lit les pixels d'une tuile stockée sans compression : les {@code 32*16} octets suivent
     * directement l'en-tête de 32 octets du record.
     */
    private static byte[] readRawPixels(byte[] raw, int offset) {
        byte[] pixels = new byte[TILE_WIDTH * TILE_HEIGHT];
        System.arraycopy(raw, offset + VsfReader.SPRITE_HEADER_SIZE, pixels, 0, pixels.length);
        return pixels;
    }

    /**
     * Convertit une tuile indexée en image opaque. Un sol n'a pas de couleur de transparence :
     * l'index 0 est une couleur comme une autre.
     */
    private static BufferedImage toImage(byte[] pixels, int[] palette) {
        BufferedImage image = new BufferedImage(TILE_WIDTH, TILE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < TILE_HEIGHT; y++) {
            for (int x = 0; x < TILE_WIDTH; x++) {
                image.setRGB(x, y, palette[pixels[y * TILE_WIDTH + x] & 0xFF]);
            }
        }
        return image;
    }
}
