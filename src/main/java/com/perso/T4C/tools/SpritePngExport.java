package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinIO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Small command-line exporter used to inspect packed UI sprites. */
public final class SpritePngExport {
    private SpritePngExport() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: <sprite-name> <output.png>");
        String wanted = args[0].toLowerCase(Locale.ROOT);
        Path output = Path.of(args[1]);
        boolean[] found = {false};
        SpriteBinIO.readAll(Path.of("assets/sprites"), "sprites", packed -> {
            if (!packed.name().toLowerCase(Locale.ROOT).equals(wanted)) return;
            try {
                Files.createDirectories(output.getParent());
                Files.write(output, packed.png());
                found[0] = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        if (!found[0]) throw new IllegalArgumentException("Sprite not found: " + args[0]);
    }
}
