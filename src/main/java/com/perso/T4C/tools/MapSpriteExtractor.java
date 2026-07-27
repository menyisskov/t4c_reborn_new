package com.perso.T4C.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Standalone extractor based exclusively on the OpenMMO wiki:
 *   https://github.com/OpenMMO/openMMO/wiki/MAP
 *   https://github.com/OpenMMO/openMMO/wiki/ID
 *
 * For every coordinate of a T4C .Map file (e.g. WorldMap.Map) it extracts the
 * tile ID and the sprite name(s) associated with that ID.
 *
 * MAP wiki:
 *  - A map is a 3072x3072 grid split into 24x24 blocks of 128x128 tiles.
 *  - Header = 576 block offsets (4-byte unsigned LE each), left-to-right,
 *    top-to-bottom. Block data is RLE-compressed.
 *  - Pseudo-RLE: read 16-bit LE values. value < TRIGGER => one tile ID.
 *    value >= TRIGGER => repeat count = value - TRIGGER, next 16-bit value is
 *    the tile ID repeated that many times.
 *  - TRIGGER = 0x1000 for versions <= 1.50, 0x2000 for versions >= 1.60.
 *  - Final tile ID = decompressed value + 0x354 (852), the starting tile ID.
 *
 * ID wiki:
 *  - The game exe (t4c.exe) initialises a table of function pointers indexed
 *    by ID: entry written at [ebx + 0x364 + ID*4]. So ID = (disp - 0x364)/4.
 *  - The init code is a long sequence of either
 *      mov reg32, imm32            (opcodes 0xB8..0xBF)
 *      mov [ebx+disp32], reg32     (opcode 0x89, modrm mod=10 rm=ebx)
 *    or
 *      mov [ebx+disp32], imm32     (opcode 0xC7, modrm 0x83)
 *  - Each pointed function loads the sprite name(s): the string address shows
 *    up as "mov edi, imm32" (0xBF) or "push imm32" (0x68). Parse the function
 *    until "ret" (0xC3), following "call imm32" (0xE8) and "jmp imm32" (0xE9)
 *    to also catch composite/smoothing tiles that reference several sprites.
 *
 * Usage:
 *   java MapSpriteExtractor [mapFile] [exeFile] [outputCsv]
 * Defaults:
 *   map = C:\Program Files (x86)\La Quatrième Prophétie\Game Files\WorldMap.Map
 *   exe = C:\Program Files (x86)\La Quatrième Prophétie\t4c.exe
 *   out = worldmap_tiles.csv  (one line per coordinate: x;y;id;sprites)
 */
public final class MapSpriteExtractor {

    // ---- MAP wiki constants ----
    private static final int MAP_SIZE = 3072;          // tiles per side
    private static final int BLOCKS_PER_SIDE = 24;
    private static final int BLOCK_SIZE = 128;         // tiles per block side
    private static final int BLOCK_COUNT = BLOCKS_PER_SIDE * BLOCKS_PER_SIDE; // 576
    private static final int TILES_PER_BLOCK = BLOCK_SIZE * BLOCK_SIZE;       // 16384
    private static final int RLE_TRIGGER_V160 = 0x2000;
    private static final int RLE_TRIGGER_V150 = 0x1000;
    private static final int FIRST_TILE_ID = 0x354;    // 852: starting tile ID offset

    // ---- ID wiki constants ----
    // The exe writes function pointers to [ebx + disp]. The MAP wiki says the
    // starting tile ID is 0x354: that is the table offset of map raw value 0,
    // so disp = 0x354 + rawValue*4. (The ID wiki anchors its own numbering at
    // 0x364, which is just raw value 4 — same table, different anchor.)
    private static final int TABLE_BASE_DISP = 0x354;  // [ebx + 0x354 + raw*4]
    private static final int MAX_ID = 15000;           // wiki: ~15000 IDs

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("--ids")) {
            int from = args.length > 1 ? Integer.parseInt(args[1]) : 850;
            int to = args.length > 2 ? Integer.parseInt(args[2]) : 905;
            debugDumpIds(Paths.get("C:\\Program Files (x86)\\La Quatrième Prophétie\\t4c.exe"), from, to);
            return;
        }
        Path mapFile = Paths.get(args.length > 0 ? args[0]
                : "C:\\Program Files (x86)\\La Quatrième Prophétie\\Game Files\\WorldMap.Map");
        Path exeFile = Paths.get(args.length > 1 ? args[1]
                : "C:\\Program Files (x86)\\La Quatrième Prophétie\\t4c.exe");
        Path outFile = Paths.get(args.length > 2 ? args[2] : "worldmap_tiles.csv");

        System.out.println("Map : " + mapFile);
        System.out.println("Exe : " + exeFile);

        // Phase A: ID -> sprite name(s) from the exe.
        Map<Integer, List<String>> idToSprites = extractIdTable(exeFile);
        System.out.println("IDs resolved from exe: " + idToSprites.size());

        // Phase B: decompress the map grid.
        int[] grid = readMapGrid(mapFile);

        // Phase C: dump every coordinate.
        long named = 0;
        try (BufferedWriter w = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            w.write("x;y;id;sprites\n");
            for (int y = 0; y < MAP_SIZE; y++) {
                for (int x = 0; x < MAP_SIZE; x++) {
                    int id = grid[y * MAP_SIZE + x];
                    List<String> sprites = idToSprites.get(id);
                    if (sprites != null) named++;
                    // Variant families (e.g. "DesertTile 1..10") keep several
                    // candidates internally; the CSV only shows the first one.
                    w.write(x + ";" + y + ";" + id + ";"
                            + (sprites == null ? "" : sprites.get(0)) + "\n");
                }
            }
        }
        System.out.println("Wrote " + outFile + " (" + (long) MAP_SIZE * MAP_SIZE
                + " coordinates, " + named + " with a sprite name)");
    }

    // =====================================================================
    // Phase B — MAP file (wiki MAP)
    // =====================================================================

    /** Returns a MAP_SIZE*MAP_SIZE array of final tile IDs (raw + 0x354). */
    public static int[] readMapGrid(Path mapFile) throws IOException {
        byte[] raw = Files.readAllBytes(mapFile);
        ByteBuffer buf = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);

        long[] blockOffsets = new long[BLOCK_COUNT];
        for (int i = 0; i < BLOCK_COUNT; i++) {
            blockOffsets[i] = buf.getInt(i * 4) & 0xFFFFFFFFL;
        }

        // Pick the RLE trigger (0x1000 for <=1.50, 0x2000 for >=1.60) by
        // trying both on the first valid block and keeping the one that
        // decompresses exactly 16384 tiles without overflowing the file.
        int trigger = detectTrigger(buf, raw.length, blockOffsets);
        System.out.printf("RLE trigger detected: 0x%04X%n", trigger);
        int[] grid = new int[MAP_SIZE * MAP_SIZE];

        for (int block = 0; block < BLOCK_COUNT; block++) {
            long off = blockOffsets[block];
            if (off == 0 || off == 0xFFFFFFFFL || off >= raw.length) {
                continue; // empty / absent block
            }
            int[] tiles = decompressBlock(buf, (int) off, raw.length, trigger);
            if (tiles == null) {
                System.err.println("Block " + block + " failed to decompress, skipped");
                continue;
            }
            // Place the 128x128 block into the 3072x3072 grid. The wiki line
            // address formula: (block%24)*128*2 + (block/24)*3072*2*128 + line*3072*2
            int baseX = (block % BLOCKS_PER_SIDE) * BLOCK_SIZE;
            int baseY = (block / BLOCKS_PER_SIDE) * BLOCK_SIZE;
            for (int line = 0; line < BLOCK_SIZE; line++) {
                int dst = (baseY + line) * MAP_SIZE + baseX;
                System.arraycopy(tiles, line * BLOCK_SIZE, grid, dst, BLOCK_SIZE);
            }
        }
        return grid;
    }

    /**
     * Picks the RLE trigger (0x1000 for <=1.50, 0x2000 for >=1.60). A block
     * can decode "successfully" with the wrong trigger, so the real test is
     * that decoding consumes exactly the bytes up to the next block's offset:
     * the trigger that fits the most blocks wins.
     */
    private static int detectTrigger(ByteBuffer buf, int fileLen, long[] blockOffsets) {
        long[] sorted = Arrays.stream(blockOffsets)
                .filter(o -> o > 0 && o != 0xFFFFFFFFL && o < fileLen)
                .sorted().toArray();
        int best = RLE_TRIGGER_V160;
        int bestFits = -1;
        for (int trigger : new int[]{RLE_TRIGGER_V150, RLE_TRIGGER_V160}) {
            int fits = 0;
            for (int i = 0; i + 1 < sorted.length && i < 64; i++) {
                int end = decompressedEnd(buf, (int) sorted[i], fileLen, trigger);
                if (end == sorted[i + 1]) {
                    fits++;
                }
            }
            if (fits > bestFits) {
                bestFits = fits;
                best = trigger;
            }
        }
        return best;
    }

    /** Returns the file position right after one decompressed block, or -1. */
    private static int decompressedEnd(ByteBuffer buf, int offset, int fileLen, int trigger) {
        int pos = offset;
        int n = 0;
        while (n < TILES_PER_BLOCK) {
            if (pos + 2 > fileLen) return -1;
            int v = buf.getShort(pos) & 0xFFFF;
            pos += 2;
            if (v < trigger) {
                n++;
            } else {
                if (pos + 2 > fileLen) return -1;
                pos += 2;
                n += v - trigger;
                if (n > TILES_PER_BLOCK) return -1;
            }
        }
        return pos;
    }

    /**
     * Vircom pseudo-RLE (wiki MAP): decompress one block of 16384 tile IDs.
     * Returns null if the data does not decode cleanly with this trigger.
     */
    private static int[] decompressBlock(ByteBuffer buf, int offset, int fileLen, int trigger) {
        int[] tiles = new int[TILES_PER_BLOCK];
        int pos = offset;
        int n = 0;
        while (n < TILES_PER_BLOCK) {
            if (pos + 2 > fileLen) return null;
            int v = buf.getShort(pos) & 0xFFFF;
            pos += 2;
            if (v < trigger) {
                tiles[n++] = v + FIRST_TILE_ID;
            } else {
                int count = v - trigger;
                if (count == 0 || n + count > TILES_PER_BLOCK || pos + 2 > fileLen) return null;
                int id = (buf.getShort(pos) & 0xFFFF) + FIRST_TILE_ID;
                pos += 2;
                for (int i = 0; i < count; i++) {
                    tiles[n++] = id;
                }
            }
        }
        return tiles;
    }

    // =====================================================================
    // Phase A — ID table from t4c.exe (wiki ID)
    // =====================================================================

    private static void debugDumpIds(Path exeFile, int from, int to) throws IOException {
        byte[] exe = Files.readAllBytes(exeFile);
        Pe pe = new Pe(exe);
        Map<Integer, Long> table = buildIdToFuncTable(exe, pe);
        Set<Long> distinct = new HashSet<>(table.values());
        System.out.println("entries=" + table.size() + " distinctFuncs=" + distinct.size());
        Map<Integer, List<String>> resolved = extractIdTable(exeFile);
        int shown = 0;
        for (Map.Entry<Integer, List<String>> e : resolved.entrySet()) {
            if (e.getKey() >= from && e.getKey() <= to || shown < 10) {
                System.out.printf("ID %d -> 0x%08X : %s%n", e.getKey(), table.get(e.getKey()), e.getValue());
                shown++;
            }
        }
    }

    public static Map<Integer, List<String>> extractIdTable(Path exeFile) throws IOException {
        byte[] exe = Files.readAllBytes(exeFile);
        Pe pe = new Pe(exe);
        Map<Integer, Long> idToFuncVa = buildIdToFuncTable(exe, pe);

        // --- Phase 2 (wiki): parse each loading function for string addresses.
        // Several consecutive IDs share one loading function: smoothing groups.
        // The shared function pushes the 32 template masks ("tmplX 1..32")
        // plus the base tile name(s); the actual mask used by a given ID is
        // its position inside the group (wiki output: "ID 305: tmpl1 1 T1:
        // NormalGrass T2: EarthTile"). So: cache strings per function, group
        // IDs per function, and resolve each ID to "its" mask + base tiles.
        Map<Long, List<String>> funcStrings = new TreeMap<>();
        Map<Long, Integer> funcMinId = new TreeMap<>();
        Map<Long, Integer> funcGroupSize = new TreeMap<>();
        for (Map.Entry<Integer, Long> e : idToFuncVa.entrySet()) {
            funcStrings.computeIfAbsent(e.getValue(), va -> extractSpriteNames(exe, pe, va));
            funcMinId.merge(e.getValue(), e.getKey(), Math::min);
            funcGroupSize.merge(e.getValue(), 1, Integer::sum);
        }

        Map<Integer, List<String>> result = new TreeMap<>();
        List<Integer> unresolved = new ArrayList<>();
        for (Map.Entry<Integer, Long> e : idToFuncVa.entrySet()) {
            List<String> strings = funcStrings.get(e.getValue());
            if (strings.isEmpty()) {
                // Keep this visible: an id with a table entry but no recoverable
                // string means the walker failed, not that the id is unused.
                unresolved.add(e.getKey());
                continue;
            }
            result.put(e.getKey(), resolveForId(e.getKey(), funcMinId.get(e.getValue()),
                    funcGroupSize.get(e.getValue()), strings));
        }
        if (!unresolved.isEmpty()) {
            System.err.println("WARNING: " + unresolved.size()
                    + " ids have a table entry but no recoverable sprite name: "
                    + unresolved.subList(0, Math.min(20, unresolved.size()))
                    + (unresolved.size() > 20 ? " ..." : ""));
        }
        return result;
    }

    /**
     * Several consecutive IDs can share one loading function (a "family":
     * smoothing templates, wall pieces...).
     *
     * <p>For smoothing groups the function pushes 32 "tmplX n" masks in order, and
     * the mask for an id is its position inside the group — that mapping holds.
     *
     * <p>For everything else it does NOT. The stucco walls (ids 4611-4655) all store
     * the very same pointer 0x004C8C40 in the id table, and that function is a
     * constructor that initialises ~42 objects at a fixed 0x20 stride; which object
     * an id uses is decided at run time from the id held in a register. Checked
     * against the shipped map, push order and id order disagree in both directions
     * (id 4613 is "StuccoMainSmallWallB", push index 12, not index 2). So the exe's
     * static data cannot disambiguate these ids: return every candidate and let the
     * caller decide, rather than pick by position and be confidently wrong.
     */
    private static List<String> resolveForId(int id, int groupMinId, int groupSize, List<String> strings) {
        int idx = id - groupMinId;
        List<String> masks = new ArrayList<>();
        for (String s : strings) {
            if (s.matches("tmpl\\d+ \\d+")) {
                masks.add(s);
            }
        }
        if (masks.size() >= 2 && idx >= 0 && idx < masks.size()) {
            return List.of(masks.get(idx));
        }
        return strings;
    }

    private static Map<Integer, Long> buildIdToFuncTable(byte[] exe, Pe pe) {
        // --- Phase 1 (wiki): rebuild the [ebx+0x364+ID*4] = functionVA table.
        // Linear scan of the code section, tracking "mov reg32, imm32" then
        // catching the stores into [ebx+disp32].
        Map<Integer, Long> idToFuncVa = new TreeMap<>();
        long[] regs = new long[8];
        boolean[] regKnown = new boolean[8];

        int start = pe.codeStart, end = pe.codeEnd;
        for (int p = start; p < end - 6; p++) {
            int op = exe[p] & 0xFF;

            if (op >= 0xB8 && op <= 0xBF) {            // mov reg32, imm32
                int reg = op - 0xB8;
                regs[reg] = readU32(exe, p + 1);
                regKnown[reg] = true;
                p += 4;
            } else if (op == 0x89) {                   // mov [ebx+disp32], reg32
                int modrm = exe[p + 1] & 0xFF;
                if ((modrm & 0xC7) == 0x83) {          // mod=10, rm=ebx
                    int reg = (modrm >> 3) & 7;
                    long disp = readU32(exe, p + 2);
                    if (regKnown[reg]) {
                        recordEntry(idToFuncVa, disp, regs[reg], pe);
                    }
                    p += 5;
                }
            } else if (op == 0xC7 && (exe[p + 1] & 0xFF) == 0x83) { // mov [ebx+disp32], imm32
                long disp = readU32(exe, p + 2);
                long imm = readU32(exe, p + 6);
                recordEntry(idToFuncVa, disp, imm, pe);
                p += 9;
            }
        }
        return idToFuncVa;
    }

    private static void recordEntry(Map<Integer, Long> table, long disp, long value, Pe pe) {
        if (disp < TABLE_BASE_DISP || (disp - TABLE_BASE_DISP) % 4 != 0) return;
        long raw = (disp - TABLE_BASE_DISP) / 4;       // raw map value
        if (raw > MAX_ID) return;
        if (!pe.isCodeVa(value)) return;               // must point into code
        // Key by the final T4C tile ID (raw + 0x354), same numbering as the
        // decompressed map grid.
        table.put((int) (raw + FIRST_TILE_ID), value);
    }

    /**
     * Walks one loading function: collects strings referenced by
     * "mov edi, imm32" (0xBF) and "push imm32" (0x68), follows call/jmp imm32,
     * stops at ret (0xC3). Depth-limited with a visited set (wiki: composite
     * and smoothing tiles chain several functions).
     */
    private static List<String> extractSpriteNames(byte[] exe, Pe pe, long funcVa) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        Set<Long> visited = new HashSet<>();
        Deque<Long> frontier = new ArrayDeque<>();
        frontier.push(funcVa);

        // Breadth-first by call depth: if the function body itself references
        // strings, those ARE the sprites for this ID. Only when the body has
        // no string at all do we descend into its call/jmp targets (wiki:
        // some IDs reach their loader through a chain of call/jmp). This
        // avoids vacuuming shared helper routines (e.g. the template loader
        // that registers all 32 "tmplX n" masks) into every smoothing tile.
        while (!frontier.isEmpty() && out.isEmpty() && visited.size() < 64) {
            Deque<Long> next = new ArrayDeque<>();
            while (!frontier.isEmpty()) {
                long va = frontier.pop();
                if (!visited.add(va)) continue;
                int p = pe.vaToFileOffset(va);
                if (p < 0) continue;

                int limit = Math.min(p + 4096, exe.length - 5);
                while (p < limit) {
                    int op = exe[p] & 0xFF;
                    if (op == 0xC3) break;                      // ret
                    if (op == 0xBF || op == 0x68) {             // mov edi,imm32 / push imm32
                        String s = readStringAt(exe, pe, readU32(exe, p + 1));
                        if (s != null) out.add(s);
                        p += 5;
                    } else if (op == 0xE8 || op == 0xE9) {      // call/jmp rel32 (signed)
                        int rel = (int) readU32(exe, p + 1);
                        long target = pe.fileOffsetToVa(p) + 5 + rel;
                        if (pe.isCodeVa(target)) next.push(target);
                        if (op == 0xE9) break;                  // unconditional jmp ends flow
                        p += 5;
                    } else {
                        p++;
                    }
                }
            }
            frontier = next;
        }
        return new ArrayList<>(out);
    }

    /** Reads a plausible null-terminated ASCII sprite name at the given VA. */
    private static String readStringAt(byte[] exe, Pe pe, long va) {
        int p = pe.vaToFileOffset(va);
        if (p < 0) return null;
        int s = p;
        while (p < exe.length && exe[p] != 0) {
            int c = exe[p] & 0xFF;
            if (c < 0x20 || c > 0x7E) return null;     // printable ASCII only
            p++;
            if (p - s > 96) return null;
        }
        int len = p - s;
        if (len < 2) return null;
        String str = new String(exe, s, len, StandardCharsets.US_ASCII);
        // Sprite names are identifier-like ("Object_LightHaven_Crypt") but some
        // families use punctuation: "Floor: Wooden 4", "Tmpl3 1/2"... Rejecting
        // those silently dropped whole ids (866 = "Floor: Wooden", 87696 tiles).
        return str.matches("[A-Za-z0-9_\\-:.,'/() ]+") ? str : null;
    }

    private static long readU32(byte[] b, int p) {
        return (b[p] & 0xFFL) | (b[p + 1] & 0xFFL) << 8
                | (b[p + 2] & 0xFFL) << 16 | (b[p + 3] & 0xFFL) << 24;
    }

    // =====================================================================
    // Minimal PE reader: image base + sections, VA <-> file offset.
    // =====================================================================
    private static final class Pe {
        final long imageBase;
        final long[] secVa, secVSize, secRaw, secRawSize;
        final boolean[] secCode;
        final int codeStart, codeEnd;     // file offsets of the first code section

        Pe(byte[] exe) {
            ByteBuffer b = ByteBuffer.wrap(exe).order(ByteOrder.LITTLE_ENDIAN);
            int peOff = b.getInt(0x3C);
            int numSections = b.getShort(peOff + 6) & 0xFFFF;
            int optSize = b.getShort(peOff + 20) & 0xFFFF;
            imageBase = b.getInt(peOff + 24 + 28) & 0xFFFFFFFFL;
            int secTable = peOff + 24 + optSize;

            secVa = new long[numSections];
            secVSize = new long[numSections];
            secRaw = new long[numSections];
            secRawSize = new long[numSections];
            secCode = new boolean[numSections];
            int cs = -1, ce = -1;
            for (int i = 0; i < numSections; i++) {
                int s = secTable + i * 40;
                secVSize[i] = b.getInt(s + 8) & 0xFFFFFFFFL;
                secVa[i] = b.getInt(s + 12) & 0xFFFFFFFFL;
                secRawSize[i] = b.getInt(s + 16) & 0xFFFFFFFFL;
                secRaw[i] = b.getInt(s + 20) & 0xFFFFFFFFL;
                long flags = b.getInt(s + 36) & 0xFFFFFFFFL;
                secCode[i] = (flags & 0x20) != 0 || (flags & 0x20000000) != 0; // CNT_CODE | MEM_EXECUTE
                if (secCode[i] && cs < 0) {
                    cs = (int) secRaw[i];
                    ce = (int) Math.min(secRaw[i] + secRawSize[i], exe.length);
                }
            }
            codeStart = cs;
            codeEnd = ce;
        }

        int vaToFileOffset(long va) {
            long rva = va - imageBase;
            for (int i = 0; i < secVa.length; i++) {
                if (rva >= secVa[i] && rva < secVa[i] + Math.max(secVSize[i], secRawSize[i])) {
                    long off = secRaw[i] + (rva - secVa[i]);
                    return off < secRaw[i] + secRawSize[i] ? (int) off : -1;
                }
            }
            return -1;
        }

        long fileOffsetToVa(long fileOff) {
            for (int i = 0; i < secVa.length; i++) {
                if (fileOff >= secRaw[i] && fileOff < secRaw[i] + secRawSize[i]) {
                    return imageBase + secVa[i] + (fileOff - secRaw[i]);
                }
            }
            return -1;
        }

        boolean isCodeVa(long va) {
            long rva = va - imageBase;
            for (int i = 0; i < secVa.length; i++) {
                if (secCode[i] && rva >= secVa[i] && rva < secVa[i] + Math.max(secVSize[i], secRawSize[i])) {
                    return true;
                }
            }
            return false;
        }
    }

    private MapSpriteExtractor() {
    }
}
