package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Importe une famille de tuiles de sol depuis les .bmp d'origine vers {@code sprites.bin}.
 *
 * <p>Ces .bmp accompagnent les librairies .vsf et sont la source de référence pour les sols :
 * ils portent leur propre palette et, surtout, <b>le bon nom</b>. La table des ressources du
 * .vsf, elle, associe les libellés aux records dans un ordre permuté — le même désordre que
 * celui déjà constaté sur les masques de sorts (cf. {@link VsfSpellMaskImport}). Importer
 * {@code Hardrock} depuis le .vsf donne ainsi les 90 bonnes tuiles sous 89 mauvaises
 * coordonnées, ce qui casse le raccord des motifs continus (les veines de fissures) d'une
 * tuile à l'autre.
 *
 * <p>Le placement sur la carte est {@code colonne = x % largeur + 1}, {@code ligne = y % hauteur + 1} :
 * une tuile mal nommée atterrit donc au mauvais endroit du motif.
 *
 * <p>Usage : {@code BmpGroundImport <dossier bmp> <sprites.bin> <base> [--apply]}
 * <br>Exemple : {@code BmpGroundImport "Version 2.00 Graphics" assets/sprites/sprites.bin Hardrock --apply}
 */
public final class BmpGroundImport {

    /** Une tuile de sol est toujours 32x16 dans les librairies d'origine. */
    private static final int TILE_WIDTH = 32;
    private static final int TILE_HEIGHT = 16;

    private BmpGroundImport() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: BmpGroundImport <bmp directory> <sprites.bin> <ground base name> [--apply]");
        }
        File directory = new File(args[0]);
        Path spriteBin = Path.of(args[1]);
        String base = args[2];
        boolean apply = args.length > 3 && "--apply".equals(args[3]);

        File[] files = directory.listFiles();
        if (files == null) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }
        // Le nom du fichier est autoritaire : c'est lui qui porte les coordonnées correctes.
        Pattern pattern = Pattern.compile("^" + Pattern.quote(base) + " \\((\\d+), (\\d+)\\)\\.bmp$",
                Pattern.CASE_INSENSITIVE);

        List<SpriteBinWriter.Entry> entries = new ArrayList<>();
        int maxColumn = 0;
        int maxRow = 0;
        for (File file : files) {
            Matcher matcher = pattern.matcher(file.getName());
            if (!matcher.matches()) {
                continue;
            }
            BufferedImage source = ImageIO.read(file);
            if (source == null || source.getWidth() != TILE_WIDTH || source.getHeight() != TILE_HEIGHT) {
                System.out.printf("  SKIP %-24s unexpected size%n", file.getName());
                continue;
            }
            int column = Integer.parseInt(matcher.group(1));
            int row = Integer.parseInt(matcher.group(2));
            maxColumn = Math.max(maxColumn, column);
            maxRow = Math.max(maxRow, row);
            String name = String.format("%s (%d, %d)", base, column, row);
            entries.add(new SpriteBinWriter.Entry(name, TILE_WIDTH, TILE_HEIGHT,
                    0, 0, 0, 0, toOpaqueArgb(source)));
        }
        if (entries.isEmpty()) {
            System.out.println("No .bmp tile found for base '" + base + "' in " + directory);
            return;
        }
        System.out.printf("%s: %d tiles, grid %dx%d, from %s%n",
                base, entries.size(), maxColumn, maxRow, directory);

        if (apply) {
            int written = SpriteBinWriter.merge(spriteBin, entries);
            System.out.printf("%s updated: %d ground tiles merged.%n", spriteBin, written);
        } else {
            System.out.println("Dry run only - re-run with --apply to write " + spriteBin + ".");
        }
    }

    /**
     * Recopie la tuile en ARGB opaque. Un sol n'a pas de couleur de transparence : la palette
     * indexée du .bmp est résolue par {@link ImageIO}, il ne reste qu'à forcer l'alpha.
     */
    private static BufferedImage toOpaqueArgb(BufferedImage source) {
        BufferedImage image = new BufferedImage(TILE_WIDTH, TILE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < TILE_HEIGHT; y++) {
            for (int x = 0; x < TILE_WIDTH; x++) {
                image.setRGB(x, y, 0xFF000000 | (source.getRGB(x, y) & 0xFFFFFF));
            }
        }
        return image;
    }
}
