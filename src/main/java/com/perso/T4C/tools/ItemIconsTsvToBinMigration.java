package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.ItemIconBinaryIO;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * One-shot converter from the legacy {@code item_icons.tsv} table to
 * {@code assets/mappings/item_icons.bin}.
 *
 * <p>Reads the historical text format — {@code #} comment lines, then
 * {@code appearanceId<TAB>sprite} rows — and rewrites it through
 * {@link ItemIconBinaryIO}. Once the binary asset is committed the TSV can be deleted;
 * {@link CppItemIconsMigration} regenerates the binary directly from the C++ sources.
 *
 * <p>Usage: {@code ItemIconsTsvToBinMigration [item_icons.tsv] [--dry-run]}
 * (defaults to {@code assets/mappings/item_icons.tsv}, relative to the repo root).
 */
public final class ItemIconsTsvToBinMigration {
    private static final String DEFAULT_TSV = "assets/mappings/item_icons.tsv";

    private ItemIconsTsvToBinMigration() {
    }

    public static void main(String[] args) throws Exception {
        boolean dryRun = false;
        String tsv = DEFAULT_TSV;
        for (String arg : args) {
            if ("--dry-run".equals(arg)) {
                dryRun = true;
            } else {
                tsv = arg;
            }
        }

        Map<Integer, String> icons = new TreeMap<>();
        int skipped = 0;
        for (String line : Files.readAllLines(Path.of(tsv), StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            int tab = line.indexOf('\t');
            if (tab <= 0) {
                skipped++;
                continue;
            }
            String sprite = line.substring(tab + 1).trim();
            try {
                int appearanceId = Integer.parseInt(line.substring(0, tab).trim());
                if (appearanceId <= 0 || sprite.isEmpty()) {
                    skipped++;
                    continue;
                }
                String previous = icons.put(appearanceId, sprite);
                if (previous != null && !previous.equals(sprite)) {
                    System.out.printf("DUP id=%d %s -> %s%n", appearanceId, previous, sprite);
                }
            } catch (NumberFormatException e) {
                skipped++;
                System.out.println("SKIP " + line);
            }
        }

        System.out.printf("source=%s rows=%d skipped=%d%n", tsv, icons.size(), skipped);
        if (dryRun) {
            System.out.println("dry run - rien ecrit");
            return;
        }
        ItemIconBinaryIO.write(new File(Paths.ITEM_ICONS_BIN), icons);
        System.out.println("written: " + Paths.ITEM_ICONS_BIN);
    }
}
