package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinIO;
import java.nio.file.Files;
import java.nio.file.Path;

/** Exports selected packed sprites as PNG files for visual inspection. */
public final class SpritePngExport {
    private SpritePngExport() {}
    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("sprite name required");
        Path out = Path.of("tmp/sprite-reference");
        Files.createDirectories(out);
        for (SpriteBinIO.Packed sprite : SpriteBinIO.readAllToList(Path.of("assets/sprites"), "sprites")) {
            for (String requested : args) if (requested.equalsIgnoreCase(sprite.name())) {
                Files.write(out.resolve(sprite.name() + ".png"), sprite.png());
            }
        }
    }
}
