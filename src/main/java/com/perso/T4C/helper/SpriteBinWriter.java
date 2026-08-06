package com.perso.T4C.helper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fusionne des sprites dans un fichier {@code sprites.bin} existant, au format T4C1 lu par
 * {@link SpriteLoader}.
 *
 * <p>Complète {@link DdaExtractor}, qui n'écrit que ce qu'il extrait lui-même de la librairie
 * .dda : ce writer accepte des images de n'importe quelle provenance (par exemple les masques
 * d'alpha lus dans une librairie .vsf) et remplace en place les sprites de même nom.
 */
public final class SpriteBinWriter {

    /** Un sprite à écrire : nom, dimensions, offsets de dessin et image. */
    public record Entry(String name, int width, int height, int off1X, int off1Y,
                        int off2X, int off2Y, int type, BufferedImage image) {

        public Entry(String name, int width, int height, int off1X, int off1Y,
                int off2X, int off2Y, BufferedImage image) {
            this(name, width, height, off1X, off1Y, off2X, off2Y,
                    SpriteBinIO.spriteType(width, height), image);
        }
    }

    private SpriteBinWriter() {
    }

    /**
     * Ajoute ou remplace les sprites donnés dans {@code spriteBin}, en conservant l'ordre et le
     * contenu des sprites existants.
     *
     * @return le nombre de sprites écrits
     */
    public static int merge(Path spriteBin, List<Entry> entries) throws IOException {
        Path dir = spriteBin.getParent() != null ? spriteBin.getParent() : Path.of(".");
        String baseName = baseName(spriteBin);

        List<SpriteBinIO.Packed> sprites = new ArrayList<>();
        Map<String, Integer> indexByName = new HashMap<>();
        SpriteBinIO.readAll(dir, baseName, packed -> {
            indexByName.putIfAbsent(SpriteBinIO.key(packed.name()), sprites.size());
            sprites.add(packed);
        });

        for (Entry entry : entries) {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            ImageIO.write(entry.image(), "png", png);
            SpriteBinIO.Packed packed = new SpriteBinIO.Packed(entry.name(), entry.width(),
                    entry.height(), entry.off1X(), entry.off1Y(), entry.off2X(), entry.off2Y(),
                    entry.type(), png.toByteArray());
            Integer existing = indexByName.get(SpriteBinIO.key(entry.name()));
            if (existing != null) {
                sprites.set(existing, packed);
            } else {
                indexByName.put(SpriteBinIO.key(entry.name()), sprites.size());
                sprites.add(packed);
            }
        }

        SpriteBinIO.writeSharded(dir, baseName, sprites);
        return entries.size();
    }

    /** {@code .../sprites.bin} → {@code sprites}, the base name of the shards. */
    private static String baseName(Path spriteBin) {
        String fileName = spriteBin.getFileName().toString();
        return fileName.endsWith(".bin")
                ? fileName.substring(0, fileName.length() - ".bin".length())
                : fileName;
    }
}
