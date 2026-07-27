package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinWriter;
import com.perso.T4C.helper.VsfReader;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Importe les masques d'alpha d'un effet de sort depuis une librairie .vsf déchiffrée vers
 * {@code sprites.bin}.
 *
 * <p>Le client d'origine dessine ces effets en deux couches : un sprite couleur
 * ({@code BoulderFire-a}) et un masque de transparence 8 bits ({@code BoulderFireA-a}) appliqué
 * par pixel. Le pipeline .dda ne sait pas produire ces masques : il interprète chaque octet
 * comme un index de palette, ce qui transforme un masque en image quasi vide. Cet outil lit
 * donc les masques depuis le .vsf, où ils sont marqués {@code kind=4}, et les écrit en niveaux
 * de gris (le poids d'alpha est recopié dans R, G et B).
 *
 * <p>Les noms de la table des ressources du .vsf sont partiellement désordonnés : quelques
 * libellés sont permutés d'une famille à l'autre. La séquence est donc reconstruite par
 * {@code treeId} (une catégorie par famille) puis par offset croissant, les records étant
 * stockés dans l'ordre des frames.
 *
 * <p>Usage : {@code VsfSpellMaskImport <fichier .dsf> <sprites.bin> <préfixe> [--apply]}
 * <br>Exemple : {@code VsfSpellMaskImport t4cgamefile14.dsf assets/sprites/sprites.bin BoulderFire --apply}
 */
public final class VsfSpellMaskImport {

    /** Suffixe des frames de masque : {@code BoulderFire} + {@code A} + {@code -a}. */
    private static final String MASK_INFIX = "A";

    private VsfSpellMaskImport() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: VsfSpellMaskImport <decrypted .dsf> <sprites.bin> <sprite name prefix> [--apply]");
        }
        Path vsfFile = Path.of(args[0]);
        Path spriteBin = Path.of(args[1]);
        String prefix = args[2];
        boolean apply = args.length > 3 && "--apply".equals(args[3]);

        VsfReader reader = new VsfReader(vsfFile);
        List<Frame> masks = collectFrames(reader, prefix, true);
        List<Frame> colors = collectFrames(reader, prefix, false);

        if (masks.isEmpty()) {
            System.out.println("No alpha mask found for prefix '" + prefix + "'.");
            return;
        }
        System.out.printf("%s: %d colour frames, %d alpha masks%n", prefix, colors.size(), masks.size());
        if (masks.size() != colors.size()) {
            System.out.printf("WARNING: frame counts differ, naming falls back to mask order only.%n");
        }

        // Les masques sont stockés dans l'ordre des frames, mais quelques libellés de l'index
        // sont permutés : les noms sont donc réattribués par position, en suivant la séquence de
        // frames du client ('-a' à '-z' puis '-2a' à '-2e').
        List<String> frameSuffixes = frameSequence(masks.size());
        List<SpriteBinWriter.Entry> entries = new ArrayList<>();
        for (int i = 0; i < masks.size(); i++) {
            Frame mask = masks.get(i);
            String name = prefix + MASK_INFIX + frameSuffixes.get(i);
            VsfReader.Sprite sprite = reader.readSprite(mask.offset());
            BufferedImage image = toGrayscale(sprite);
            entries.add(new SpriteBinWriter.Entry(name, sprite.header().width(), sprite.header().height(),
                    sprite.header().originX(), sprite.header().originY(),
                    sprite.header().originX(), sprite.header().originY(), image));
            System.out.printf("  %-22s %4dx%-4d origin(%5d,%5d) from %s%n", name,
                    sprite.header().width(), sprite.header().height(),
                    sprite.header().originX(), sprite.header().originY(), mask.name());
        }

        if (apply) {
            int written = SpriteBinWriter.merge(spriteBin, entries);
            System.out.printf("%s updated: %d mask sprites merged.%n", spriteBin, written);
        } else {
            System.out.println("Dry run only - re-run with --apply to write " + spriteBin + ".");
        }
    }

    /** Une frame retenue dans l'index des ressources du .vsf. */
    private record Frame(String name, int offset, int width) {
    }

    /**
     * Rassemble les frames d'une famille. Le {@code treeId} du record distingue les masques des
     * sprites couleur de façon fiable là où les libellés de l'index sont parfois permutés.
     *
     * <p>L'animation est une onde de choc qui s'étend : les frames se suivent donc par surface
     * croissante. Les frames vides (le sprite 32x16 de remplissage, présent au début de
     * l'animation et sur la toute dernière frame) n'entrent pas dans cette progression et sont
     * réparties d'après leur position de stockage.
     */
    private static List<Frame> collectFrames(VsfReader reader, String prefix, boolean masks) throws Exception {
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        List<Frame> stubs = new ArrayList<>();
        List<Frame> drawn = new ArrayList<>();
        for (VsfReader.Resource resource : reader.getResources()) {
            if (!resource.isSprite()) {
                continue;
            }
            if (!resource.name().toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) {
                continue;
            }
            VsfReader.SpriteHeader header = reader.readSpriteHeader(resource.offset());
            if (header.isAlphaMask() != masks) {
                continue;
            }
            Frame frame = new Frame(resource.name(), resource.offset(), header.width());
            if (isStub(header)) {
                stubs.add(frame);
            } else {
                drawn.add(frame);
            }
        }
        // La largeur croît de façon strictement monotone sur toute l'animation, alors que la
        // hauteur redescend sur les dernières frames : c'est donc elle qui ordonne la séquence,
        // l'offset départageant les frames de même largeur.
        drawn.sort(Comparator.comparingInt(Frame::width).thenComparingInt(Frame::offset));
        stubs.sort(Comparator.comparingInt(Frame::offset));

        // Les frames vides ouvrent l'animation, sauf la dernière qui la referme.
        List<Frame> frames = new ArrayList<>();
        int leading = Math.max(0, stubs.size() - 1);
        frames.addAll(stubs.subList(0, leading));
        frames.addAll(drawn);
        frames.addAll(stubs.subList(leading, stubs.size()));
        return frames;
    }

    /** Une frame de remplissage : le sprite 32x16 sans pixels, hors de la progression. */
    private static boolean isStub(VsfReader.SpriteHeader header) {
        return header.width() == 32 && header.height() == 16;
    }

    /**
     * Séquence des suffixes de frame du client : {@code -a} à {@code -z}, puis {@code -2a} à
     * {@code -2e}.
     */
    private static List<String> frameSequence(int count) {
        List<String> suffixes = new ArrayList<>();
        for (char c = 'a'; c <= 'z' && suffixes.size() < count; c++) {
            suffixes.add("-" + c);
        }
        for (char c = 'a'; suffixes.size() < count; c++) {
            suffixes.add("-2" + c);
        }
        return suffixes;
    }

    /**
     * Convertit un masque (un octet = un poids d'alpha 0-255) en image opaque en niveaux de gris.
     * Le shader de rendu lit ce poids dans le canal rouge.
     */
    private static BufferedImage toGrayscale(VsfReader.Sprite sprite) {
        int w = Math.max(1, sprite.header().width());
        int h = Math.max(1, sprite.header().height());
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        byte[] pixels = sprite.pixels();
        for (int y = 0; y < sprite.header().height(); y++) {
            for (int x = 0; x < sprite.header().width(); x++) {
                int weight = pixels[y * sprite.header().width() + x] & 0xFF;
                image.setRGB(x, y, 0xFF000000 | (weight << 16) | (weight << 8) | weight);
            }
        }
        return image;
    }
}
