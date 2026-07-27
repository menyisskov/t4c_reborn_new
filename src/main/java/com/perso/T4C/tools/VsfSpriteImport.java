package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinWriter;
import com.perso.T4C.helper.VsfReader;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Importe une famille de sprites libres (décor, portes, objets) depuis une librairie .vsf
 * déchiffrée vers {@code sprites.bin}.
 *
 * <p>Un sprite indexé du .vsf est accompagné d'une palette stockée comme une ressource de
 * type 2 : 4 octets d'en-tête puis 256 triplets RGB, repris sans mise à l'échelle comme le
 * fait {@code DpdReader} pour le .dda. L'index donne à cette palette le nom d'une des frames
 * de la famille : pour {@code RockDoor}, l'entrée libellée {@code RockDoor9} est en réalité
 * la palette du groupe, ce qui explique qu'il n'y ait que 10 frames pour 11 libellés. C'est
 * le même désordre de libellés que celui documenté sur les masques de sorts
 * (cf. {@link VsfSpellMaskImport}).
 *
 * <p>La palette est donc résolue en cherchant une ressource de type 2 <b>portant le préfixe
 * de la famille</b> ; à défaut, la dernière palette rangée avant son premier record.
 * {@code --palette <nom>} force un autre choix si besoin.
 *
 * <p>L'index 0 est la couleur de transparence des sprites libres (contrairement aux tuiles de
 * sol, opaques) : il est écrit en alpha nul.
 *
 * <p>Usage : {@code VsfSpriteImport <fichier .dsf> <sprites.bin> <préfixe> [--palette <nom>] [--apply]}
 * <br>Exemple : {@code VsfSpriteImport t4cgamefile12.dsf assets/sprites/sprites.bin RockDoor --apply}
 */
public final class VsfSpriteImport {

    private static final int PALETTE_HEADER_SIZE = 4;
    private static final int PALETTE_COLORS = 256;

    /** Index servant de couleur de transparence dans les sprites libres. */
    private static final int TRANSPARENT_INDEX = 0;

    private VsfSpriteImport() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: VsfSpriteImport <decrypted .dsf> <sprites.bin> <name prefix> "
                            + "[--palette <name>] [--apply]");
        }
        Path vsfFile = Path.of(args[0]);
        Path spriteBin = Path.of(args[1]);
        String prefix = args[2];
        String forcedPalette = null;
        boolean apply = false;
        boolean sameTreeOnly = false;
        for (int i = 3; i < args.length; i++) {
            if ("--apply".equals(args[i])) {
                apply = true;
            } else if ("--same-tree".equals(args[i])) {
                sameTreeOnly = true;
            } else if ("--palette".equals(args[i]) && i + 1 < args.length) {
                forcedPalette = args[++i];
            }
        }

        byte[] raw = Files.readAllBytes(vsfFile);
        VsfReader reader = new VsfReader(raw);

        List<VsfReader.Resource> family = new ArrayList<>();
        for (VsfReader.Resource resource : reader.getResources()) {
            if (resource.isSprite()
                    && resource.name().toLowerCase(Locale.ROOT)
                            .startsWith(prefix.toLowerCase(Locale.ROOT))) {
                family.add(resource);
            }
        }
        if (family.isEmpty()) {
            System.out.println("No sprite found for prefix '" + prefix + "'.");
            return;
        }
        if (sameTreeOnly) {
            // Une même racine de nom peut couvrir deux assets distincts rangés dans des
            // catégories différentes, chacun avec sa palette : RockDoor1..11 est l'animation
            // de porte (noeud "decoration") alors que RockDoor est une arche de pierre
            // (noeud "Doors"). Le treeId du premier record fait alors référence.
            int treeId = reader.readSpriteHeader(family.get(0).offset()).treeId();
            List<VsfReader.Resource> filtered = new ArrayList<>();
            for (VsfReader.Resource resource : family) {
                if (reader.readSpriteHeader(resource.offset()).treeId() == treeId) {
                    filtered.add(resource);
                } else {
                    System.out.printf("  SKIP %-22s other category (treeId=%d)%n", resource.name(),
                            reader.readSpriteHeader(resource.offset()).treeId());
                }
            }
            family = filtered;
        }

        VsfReader.Resource paletteResource = forcedPalette != null
                ? findPaletteByName(reader, forcedPalette)
                : resolvePalette(reader, prefix, family.get(0).offset());
        int[] palette = readPalette(raw, paletteResource.offset());
        System.out.printf("%s: %d sprites, palette '%s'%s%n", prefix, family.size(),
                paletteResource.name(), forcedPalette == null ? " (preceding record)" : " (forced)");

        List<SpriteBinWriter.Entry> entries = new ArrayList<>();
        for (VsfReader.Resource resource : family) {
            VsfReader.Sprite sprite = reader.readSprite(resource.offset());
            VsfReader.SpriteHeader header = sprite.header();
            // Le client dessine le sprite à l'origine portée par son en-tête ; sprites.bin
            // stocke ce décalage dans les deux paires d'offsets de dessin.
            entries.add(new SpriteBinWriter.Entry(resource.name(), header.width(), header.height(),
                    header.originX(), header.originY(), header.originX(), header.originY(),
                    toImage(sprite, palette)));
            System.out.printf("  %-22s %3dx%-4d origin(%4d,%5d)%n", resource.name(),
                    header.width(), header.height(), header.originX(), header.originY());
        }

        if (apply) {
            int written = SpriteBinWriter.merge(spriteBin, entries);
            System.out.printf("%s updated: %d sprites merged.%n", spriteBin, written);
        } else {
            System.out.println("Dry run only - re-run with --apply to write " + spriteBin + ".");
        }
    }

    /**
     * Palette de la famille : d'abord une ressource de type 2 portant le préfixe (l'index lui
     * a donné le nom d'une frame), sinon la dernière palette rangée avant le premier record.
     */
    private static VsfReader.Resource resolvePalette(VsfReader reader, String prefix, int firstSpriteOffset) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        for (VsfReader.Resource resource : reader.getResources()) {
            if (!resource.isSprite() && resource.name().toLowerCase(Locale.ROOT).startsWith(lower)) {
                return resource;
            }
        }
        return findPaletteBefore(reader, firstSpriteOffset);
    }

    /** Dernière palette rangée avant le premier record de la famille. */
    private static VsfReader.Resource findPaletteBefore(VsfReader reader, int firstSpriteOffset) {
        VsfReader.Resource best = null;
        for (VsfReader.Resource resource : reader.getResources()) {
            if (resource.isSprite() || resource.offset() > firstSpriteOffset) {
                continue;
            }
            if (best == null || resource.offset() > best.offset()) {
                best = resource;
            }
        }
        if (best == null) {
            throw new IllegalArgumentException(
                    "No palette found before offset " + firstSpriteOffset + "; pass --palette <name>");
        }
        return best;
    }

    private static VsfReader.Resource findPaletteByName(VsfReader reader, String name) {
        for (VsfReader.Resource resource : reader.getResources()) {
            if (!resource.isSprite() && resource.name().equalsIgnoreCase(name)) {
                return resource;
            }
        }
        throw new IllegalArgumentException("Palette '" + name + "' not found in the VSF file");
    }

    private static int[] readPalette(byte[] raw, int offset) {
        int base = offset + PALETTE_HEADER_SIZE;
        int[] argb = new int[PALETTE_COLORS];
        for (int i = 0; i < PALETTE_COLORS; i++) {
            int r = raw[base + i * 3] & 0xFF;
            int g = raw[base + i * 3 + 1] & 0xFF;
            int b = raw[base + i * 3 + 2] & 0xFF;
            argb[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        return argb;
    }

    /** Convertit un sprite indexé en ARGB, l'index 0 devenant transparent. */
    private static BufferedImage toImage(VsfReader.Sprite sprite, int[] palette) {
        int w = Math.max(1, sprite.header().width());
        int h = Math.max(1, sprite.header().height());
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        byte[] pixels = sprite.pixels();
        for (int y = 0; y < sprite.header().height(); y++) {
            for (int x = 0; x < sprite.header().width(); x++) {
                int index = pixels[y * sprite.header().width() + x] & 0xFF;
                image.setRGB(x, y, index == TRANSPARENT_INDEX ? 0 : palette[index]);
            }
        }
        return image;
    }
}
