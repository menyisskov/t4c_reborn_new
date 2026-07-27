package com.perso.T4C.tools;

import com.perso.T4C.helper.DdaExtractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

/** Imports every original V3 sprite from the NMS graphic library. */
public final class V3SpriteMigration {

    private static final String PREFIX = "v3";
    private static final int[] REQUIRED_DDA_FILES = {5, 6, 7};

    private V3SpriteMigration() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "Usage: V3SpriteMigration <T4C map files directory> <sprites.bin>");
        }

        Path source = Path.of(args[0]);
        Path output = Path.of(args[1]);
        Path adaptedLibrary = Files.createTempDirectory("t4c-dialog-sprites-");
        try {
            adaptNmsLibrary(source, adaptedLibrary);
            try (DdaExtractor extractor = new DdaExtractor(adaptedLibrary)) {
                long expected = extractor.getDid().getEntries().stream()
                        .filter(entry -> entry.name.toLowerCase(Locale.ROOT).startsWith(PREFIX))
                        .count();
                // The original client requests palette number 1. Passing 0 selects
                // unrelated "...0" palettes (notably NBuisson01) for most V3 sprites.
                int imported = extractor.mergeSpriteBin(output, 1,
                        entry -> entry.name.toLowerCase(Locale.ROOT).startsWith(PREFIX));
                if (imported != expected) {
                    throw new IllegalStateException("Expected " + expected + " V3 sprites, imported " + imported);
                }
            }
        } finally {
            for (int ddaNumber : REQUIRED_DDA_FILES) {
                deleteIfExists(adaptedLibrary.resolve(String.format("v2data%02d.dda", ddaNumber)));
            }
            deleteIfExists(adaptedLibrary.resolve("v2colori.dpd"));
            deleteIfExists(adaptedLibrary.resolve("v2datai.did"));
            deleteIfExists(adaptedLibrary);
        }
    }

    private static void adaptNmsLibrary(Path source, Path target) throws Exception {
        Files.copy(source.resolve("v2nmsdatai.did"), target.resolve("v2datai.did"),
                StandardCopyOption.REPLACE_EXISTING);
        Files.copy(source.resolve("v2nmscolori.dpd"), target.resolve("v2colori.dpd"),
                StandardCopyOption.REPLACE_EXISTING);
        for (int ddaNumber : REQUIRED_DDA_FILES) {
            String fileName = String.format("v2nmsdata%02d.dda", ddaNumber);
            Files.copy(source.resolve(fileName),
                    target.resolve(String.format("v2data%02d.dda", ddaNumber)),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // Best-effort cleanup of migration-only temporary files.
        }
    }
}
