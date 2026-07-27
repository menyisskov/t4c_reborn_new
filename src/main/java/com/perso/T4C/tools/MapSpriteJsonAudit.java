package com.perso.T4C.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.perso.T4C.helper.MapReader;

/**
 * Compares an extracted "*.sprites.json" reference dump (produced from the original
 * T4C .Map files) against what the ported .mapbin/.decorbin actually contains.
 *
 * The JSON is streamed entry by entry (the WorldMap dump is ~2 GB), so memory stays flat.
 *
 * Usage:
 *   MapSpriteJsonAudit &lt;sprites.json&gt; [mapbin] [--report=file.txt] [--samples=N] [--strict-decor]
 *
 * Matching rule: a JSON entry matches when its {@code nomSprite} equals the ground sprite
 * OR the decor sprite of the same tile (the original format has a single layer per cell,
 * the port splits it into ground + decor). {@code --strict-decor} compares against ground only.
 */
public final class MapSpriteJsonAudit {

    private static final int DEFAULT_SAMPLES = 40;

    private MapSpriteJsonAudit() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: MapSpriteJsonAudit <sprites.json> [mapbin] "
                    + "[--report=file.txt] [--samples=N] [--strict-decor]");
            System.exit(2);
        }

        File jsonFile = new File(args[0]);
        File mapFile = new File("assets/maps/worldmap/worldmap.mapbin");
        Path report = null;
        int samples = DEFAULT_SAMPLES;
        boolean strictDecor = false;

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--report=")) {
                report = Path.of(arg.substring("--report=".length()));
            } else if (arg.startsWith("--samples=")) {
                samples = Integer.parseInt(arg.substring("--samples=".length()));
            } else if (arg.equals("--strict-decor")) {
                strictDecor = true;
            } else if (!arg.startsWith("--")) {
                mapFile = new File(arg);
            } else {
                System.err.println("Unknown option: " + arg);
                System.exit(2);
            }
        }

        if (!jsonFile.isFile()) {
            System.err.println("JSON not found: " + jsonFile.getAbsolutePath());
            System.exit(1);
        }

        try (MapReader map = new MapReader(mapFile)) {
            Result result = audit(jsonFile, map, samples, strictDecor);
            print(result, map, System.out);
            if (report != null) {
                try (Writer out = Files.newBufferedWriter(report, StandardCharsets.UTF_8)) {
                    print(result, map, out);
                }
                System.out.println("Report written to " + report.toAbsolutePath());
            }
        }
    }

    static Result audit(File jsonFile, MapReader map, int maxSamples, boolean strictDecor) throws Exception {
        Result result = new Result();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8), 1 << 20);
                JsonReader reader = new JsonReader(br)) {
            reader.setLenient(true);
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if ("tiles".equals(key) || "sprites".equals(key)) {
                    readTiles(reader, map, result, maxSamples, strictDecor);
                } else {
                    reader.skipValue();
                }
            }
        }
        return result;
    }

    private static void readTiles(JsonReader reader, MapReader map, Result result,
            int maxSamples, boolean strictDecor) throws Exception {
        reader.beginArray();
        while (reader.hasNext()) {
            Entry entry = readEntry(reader);
            classify(entry, map, result, maxSamples, strictDecor);
        }
        reader.endArray();
    }

    private static Entry readEntry(JsonReader reader) throws Exception {
        Entry entry = new Entry();
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "x" -> entry.x = reader.nextInt();
                case "y" -> entry.y = reader.nextInt();
                case "z" -> entry.z = reader.nextInt();
                case "id" -> entry.id = reader.nextInt();
                case "nomSprite" -> entry.nomSprite = nextStringOrNull(reader);
                case "family" -> entry.family = nextStringOrNull(reader);
                case "resolution" -> entry.resolution = nextStringOrNull(reader);
                default -> reader.skipValue();
            }
        }
        reader.endObject();
        return entry;
    }

    private static String nextStringOrNull(JsonReader reader) throws Exception {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return reader.nextString();
    }

    private static void classify(Entry entry, MapReader map, Result result,
            int maxSamples, boolean strictDecor) {
        result.total++;

        if (entry.z != 0) {
            result.skippedNonZeroZ++;
            return;
        }
        if (map.isInside(entry.x, entry.y)) { // isInside() returns true when OUT of bounds
            result.outOfBounds++;
            addSample(result.outOfBoundsSamples, entry, null, null, maxSamples);
            return;
        }

        String ground = blankToNull(map.getGroundSpriteName(entry.x, entry.y));
        String decor = blankToNull(map.getDecorSpriteName(entry.x, entry.y));
        String expected = blankToNull(entry.nomSprite);

        if (expected == null) {
            if (ground == null && decor == null) {
                result.bothEmpty++;
            } else {
                result.emptyInJsonFilledInMap++;
                addSample(result.emptyInJsonSamples, entry, ground, decor, maxSamples);
            }
            return;
        }

        if (expected.equals(ground)) {
            result.matchGround++;
            result.matchedByFamily.merge(familyOf(entry), 1L, Long::sum);
            return;
        }
        if (!strictDecor && expected.equals(decor)) {
            result.matchDecor++;
            result.matchedByFamily.merge(familyOf(entry), 1L, Long::sum);
            return;
        }

        if (ground == null && decor == null) {
            result.missingInMap++;
            addSample(result.missingSamples, entry, ground, decor, maxSamples);
        } else if (sameFamily(expected, ground) || (!strictDecor && sameFamily(expected, decor))) {
            result.mismatchSameFamily++;
            addSample(result.sameFamilySamples, entry, ground, decor, maxSamples);
        } else {
            result.mismatchDifferentSprite++;
            addSample(result.mismatchSamples, entry, ground, decor, maxSamples);
        }
        result.mismatchByFamily.merge(familyOf(entry), 1L, Long::sum);
    }

    /** "GreenGrass (4, 8)" -> "GreenGrass". */
    static String baseName(String spriteName) {
        if (spriteName == null) {
            return null;
        }
        int paren = spriteName.indexOf('(');
        return (paren > 0 ? spriteName.substring(0, paren) : spriteName).trim();
    }

    private static boolean sameFamily(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        String a = baseName(expected);
        String b = baseName(actual);
        return a != null && a.equalsIgnoreCase(b);
    }

    private static String familyOf(Entry entry) {
        if (entry.family != null && !entry.family.isBlank()) {
            return entry.family;
        }
        String base = baseName(entry.nomSprite);
        return base == null || base.isBlank() ? "<unknown>" : base;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static void addSample(List<Sample> samples, Entry entry, String ground, String decor, int max) {
        if (samples.size() < max) {
            samples.add(new Sample(entry.x, entry.y, entry.id, entry.nomSprite, entry.resolution, ground, decor));
        }
    }

    static void print(Result r, MapReader map, Appendable out) throws java.io.IOException {
        long comparable = r.matchGround + r.matchDecor + r.missingInMap
                + r.mismatchSameFamily + r.mismatchDifferentSprite;
        long matched = r.matchGround + r.matchDecor;

        out.append("=== Map sprite audit ===\n");
        out.append(String.format("Map size                 : %dx%d (%d tiles)%n",
                map.getWidth(), map.getHeight(), (long) map.getWidth() * map.getHeight()));
        out.append(String.format("JSON entries read        : %d%n", r.total));
        out.append(String.format("  skipped (z != 0)       : %d%n", r.skippedNonZeroZ));
        out.append(String.format("  out of map bounds      : %d%n", r.outOfBounds));
        out.append(String.format("  empty in JSON & map    : %d%n", r.bothEmpty));
        out.append(String.format("  empty in JSON, set map : %d%n", r.emptyInJsonFilledInMap));
        out.append("\n-- Comparable tiles (JSON has a sprite name) --\n");
        out.append(String.format("Comparable               : %d%n", comparable));
        out.append(String.format("  exact match (ground)   : %d%n", r.matchGround));
        out.append(String.format("  exact match (decor)    : %d%n", r.matchDecor));
        out.append(String.format("  same family, other var : %d%n", r.mismatchSameFamily));
        out.append(String.format("  different sprite       : %d%n", r.mismatchDifferentSprite));
        out.append(String.format("  empty in map           : %d%n", r.missingInMap));
        out.append(String.format("MATCH RATE               : %s%n", pct(matched, comparable)));
        out.append(String.format("MATCH RATE (family-level): %s%n",
                pct(matched + r.mismatchSameFamily, comparable)));

        printTopFamilies(out, "\n-- Top families by mismatch --", r.mismatchByFamily, 25);
        printSamples(out, "\n-- Sample: different sprite --", r.mismatchSamples);
        printSamples(out, "\n-- Sample: same family, different variant --", r.sameFamilySamples);
        printSamples(out, "\n-- Sample: present in JSON, empty in map --", r.missingSamples);
        printSamples(out, "\n-- Sample: empty in JSON, present in map --", r.emptyInJsonSamples);
        printSamples(out, "\n-- Sample: out of bounds --", r.outOfBoundsSamples);
    }

    private static void printTopFamilies(Appendable out, String title, Map<String, Long> counts, int limit)
            throws java.io.IOException {
        if (counts.isEmpty()) {
            return;
        }
        out.append(title).append('\n');
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .forEach(e -> {
                    try {
                        out.append(String.format("  %-32s %d%n", e.getKey(), e.getValue()));
                    } catch (java.io.IOException ignored) {
                        // Appendable to a StringBuilder/PrintStream does not throw in practice.
                    }
                });
    }

    private static void printSamples(Appendable out, String title, List<Sample> samples) throws java.io.IOException {
        if (samples.isEmpty()) {
            return;
        }
        out.append(title).append('\n');
        for (Sample s : samples) {
            out.append(String.format("  (%4d,%4d) id=%-6d json=%-28s | ground=%-28s decor=%-28s [%s]%n",
                    s.x, s.y, s.id, String.valueOf(s.json), String.valueOf(s.ground),
                    String.valueOf(s.decor), String.valueOf(s.resolution)));
        }
    }

    private static String pct(long part, long total) {
        if (total == 0) {
            return "n/a";
        }
        return String.format("%.3f%% (%d/%d)", 100.0 * part / total, part, total);
    }

    private static final class Entry {
        int x;
        int y;
        int z;
        int id;
        String nomSprite;
        String family;
        String resolution;
    }

    record Sample(int x, int y, int id, String json, String resolution, String ground, String decor) {
    }

    static final class Result {
        long total;
        long skippedNonZeroZ;
        long outOfBounds;
        long bothEmpty;
        long emptyInJsonFilledInMap;
        long matchGround;
        long matchDecor;
        long missingInMap;
        long mismatchSameFamily;
        long mismatchDifferentSprite;
        final Map<String, Long> matchedByFamily = new HashMap<>();
        final Map<String, Long> mismatchByFamily = new HashMap<>();
        final List<Sample> mismatchSamples = new ArrayList<>();
        final List<Sample> sameFamilySamples = new ArrayList<>();
        final List<Sample> missingSamples = new ArrayList<>();
        final List<Sample> emptyInJsonSamples = new ArrayList<>();
        final List<Sample> outOfBoundsSamples = new ArrayList<>();
    }
}
