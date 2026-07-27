package com.perso.T4C.tools;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.perso.T4C.helper.MapReader;

/**
 * Compares the CSV produced by {@link MapSpriteExtractor} (ground truth decoded from
 * WorldMap.Map + t4c.exe) against the ported .mapbin/.decorbin.
 *
 * The CSV holds the sprite <em>family</em> name ("Ground_Water"), while the port stores
 * a concrete variant ("Ground_Water (1, 2)"), so comparison is family-level: the map's
 * name is reduced to its base by stripping a trailing "(i, j)".
 *
 * Usage: MapSpriteCsvAudit &lt;tiles.csv&gt; [mapbin]
 */
public final class MapSpriteCsvAudit {

    private MapSpriteCsvAudit() {
    }

    public static void main(String[] args) throws Exception {
        Path csv = Path.of(args.length > 0 ? args[0] : "worldmap_tiles.csv");
        File mapFile = new File(args.length > 1 ? args[1] : "assets/maps/worldmap/worldmap.mapbin");

        long total = 0;
        long match = 0;
        long csvUnnamed = 0;
        long mapEmpty = 0;
        long mismatch = 0;
        Map<String, Long> mismatchPairs = new HashMap<>();

        try (MapReader map = new MapReader(mapFile);
                BufferedReader in = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            in.readLine(); // header
            String line;
            while ((line = in.readLine()) != null) {
                int c1 = line.indexOf(';');
                int c2 = line.indexOf(';', c1 + 1);
                int c3 = line.indexOf(';', c2 + 1);
                if (c3 < 0) {
                    continue;
                }
                int x = Integer.parseInt(line, 0, c1, 10);
                int y = Integer.parseInt(line, c1 + 1, c2, 10);
                String expected = line.substring(c3 + 1);
                total++;

                if (expected.isBlank()) {
                    csvUnnamed++;
                    continue;
                }
                if (map.isInside(x, y)) { // returns true when OUT of bounds
                    continue;
                }

                String ground = base(map.getGroundSpriteName(x, y));
                String decor = base(map.getDecorSpriteName(x, y));
                if (ground == null && decor == null) {
                    mapEmpty++;
                    continue;
                }
                if (expected.equalsIgnoreCase(ground) || expected.equalsIgnoreCase(decor)) {
                    match++;
                } else {
                    mismatch++;
                    mismatchPairs.merge(expected + "  ->  " + ground, 1L, Long::sum);
                }
            }
        }

        long comparable = match + mismatch;
        System.out.println("=== CSV (exe-decoded) vs .mapbin ===");
        System.out.printf("CSV rows              : %d%n", total);
        System.out.printf("  no name in exe table: %d%n", csvUnnamed);
        System.out.printf("  empty in mapbin     : %d%n", mapEmpty);
        System.out.printf("Comparable            : %d%n", comparable);
        System.out.printf("  family match        : %d%n", match);
        System.out.printf("  mismatch            : %d%n", mismatch);
        System.out.printf("MATCH RATE            : %.3f%% (%d/%d)%n",
                comparable == 0 ? 0.0 : 100.0 * match / comparable, match, comparable);

        System.out.println("\n-- Top mismatching pairs (csv -> mapbin) --");
        mismatchPairs.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(25)
                .forEach(e -> System.out.printf("  %-56s %d%n", e.getKey(), e.getValue()));
    }

    /** "Ground_Water (1, 2)" -> "Ground_Water". */
    private static String base(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        int paren = name.indexOf('(');
        String result = (paren > 0 ? name.substring(0, paren) : name).trim();
        return result.isEmpty() ? null : result;
    }
}
