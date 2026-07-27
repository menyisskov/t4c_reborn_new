package com.perso.T4C.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.MusicZoneBinaryIO;

public final class LegacyMusicZoneImporter {
    static final String[] MUSIC_NAMES = {
            "Boss Music",
            "Outdoors Music",
            "Forest Music",
            "Dungeons Music",
            "Caverns Music",
            "Sadness Music",
            null,
            "Noises Music"
    };

    private LegacyMusicZoneImporter() {
    }

    public static void main(String[] args) throws Exception {
        for (MapDefinition definition : MapDefinition.values()) {
            File mapFile = new File(definition.getMapPath());
            int width;
            int height;
            try (MapReader reader = new MapReader(mapFile)) {
                width = reader.getWidth();
                height = reader.getHeight();
            }

            List<MusicZoneBinaryIO.Entry> entries = buildEntries(definition.getZ(), width, height);
            File output = sidecarFile(mapFile);
            MusicZoneBinaryIO.write(output, entries);
            System.out.printf("World %d: %s (%dx%d, %d zones)%n",
                    definition.getZ(), output.getPath(), width, height, entries.size());
        }
    }

    static List<MusicZoneBinaryIO.Entry> buildEntries(int world, int width, int height) {
        List<MusicZoneBinaryIO.Entry> completed = new ArrayList<>();
        Map<RunKey, MusicZoneBinaryIO.Entry> previousRow = new HashMap<>();
        int[] counters = new int[MUSIC_NAMES.length];

        for (int y = 0; y < height; y++) {
            Map<RunKey, MusicZoneBinaryIO.Entry> currentRow = new HashMap<>();
            int x = 0;
            while (x < width) {
                int musicId = resolveMusicId(world, x, y);
                int x1 = x++;
                while (x < width && resolveMusicId(world, x, y) == musicId) {
                    x++;
                }

                String music = musicId >= 0 && musicId < MUSIC_NAMES.length ? MUSIC_NAMES[musicId] : null;
                if (music == null) {
                    continue;
                }

                RunKey key = new RunKey(musicId, x1, x - 1);
                MusicZoneBinaryIO.Entry entry = previousRow.remove(key);
                if (entry == null) {
                    entry = new MusicZoneBinaryIO.Entry();
                    entry.name = music + " " + ++counters[musicId];
                    entry.x1 = x1;
                    entry.y1 = y;
                    entry.x2 = x - 1;
                    entry.music = music;
                }
                entry.y2 = y;
                currentRow.put(key, entry);
            }
            completed.addAll(previousRow.values());
            previousRow = currentRow;
        }
        completed.addAll(previousRow.values());
        completed.sort((a, b) -> {
            int result = Integer.compare(a.y1, b.y1);
            if (result == 0) result = Integer.compare(a.x1, b.x1);
            if (result == 0) result = Integer.compare(a.y2, b.y2);
            if (result == 0) result = Integer.compare(a.x2, b.x2);
            return result;
        });
        return completed;
    }

    /**
     * Exact transcription of the zone precedence in t4c.exe 1.25.
     */
    public static int resolveMusicId(int world, int x, int y) {
        int music = switch (world) {
            case 1 -> 3;
            case 2 -> 4;
            default -> 2;
        };

        if (world == 3) {
            if (x >= 0 && x <= 0x600 && y > 0x5fe && y < 0xc00) {
                music = 3;
            }
            if (x > 0x5ff && x < 0x601 && y > 0xbfe && y < 0xc00) {
                music = 4;
            }
        }

        if (world == 1) {
            if (insideExclusive(x, y, 0x86, 0x11e, 0x199, 0x231)
                    || insideExclusive(x, y, 0x3bf, 0x4a2, 0x4a, 0x12d)) {
                music = 0;
            }
            return music;
        }
        if (world != 0) {
            return music;
        }

        if (diamond(x, y, 0x337, 0x197, 0x46f, 0x2d3)
                || diamond(x, y, 0x470, 0x196, 0x4b8, 0x298)
                || diamond(x, y, 0x238, 0xf8, 0x2c8, 0x138)
                || insideExclusive(x, y, 0x3e0, 0x400, 0x3b0, 0x3b8)
                || insideExclusive(x, y, 0x3b7, 0x406, 0x3b7, 0x3be)
                || insideExclusive(x, y, 0x3aa, 0x40b, 0x3bd, 0x3c4)
                || insideExclusive(x, y, 0x387, 0x411, 0x3c3, 0x3ca)
                || insideExclusive(x, y, 0x374, 0x428, 0x3c9, 0x3d3)
                || insideExclusive(x, y, 0x368, 0x42f, 0x3d2, 0x3dc)
                || insideExclusive(x, y, 0x360, 0x446, 0x3db, 0x3ea)
                || insideExclusive(x, y, 0x354, 0x4d9, 0x3e9, 0x440)
                || insideExclusive(x, y, 0x34b, 0x4e8, 0x43f, 0x529)) {
            music = 1;
        }
        if (diamond(x, y, 0x22b, -0x73, 0x271, -0x2d)) {
            music = 0;
        }
        if (diamond(x, y, 0x623, 0x327, 0x6a3, 0x3a5)) {
            music = 5;
        }
        if (diamond(x, y, 0x36f, -0x309, 0x3f9, -0x223)
                || diamond(x, y, 0x3fa, -0x2e8, 0x430, -0x25a)
                || diamond(x, y, 0x6b8, -0x270, 0x71a, -0x264)
                || diamond(x, y, 0x59d, -0x2b7, 0x75b, -0x271)
                || diamond(x, y, 0x5b4, -0x2dc, 0x762, -0x2b8)
                || diamond(x, y, 0x5f1, -0x2f9, 0x763, -0x2dd)
                || diamond(x, y, 0x60a, -0x322, 0x774, -0x2fa)
                || diamond(x, y, 0x627, -0x341, 0x77b, -0x323)) {
            music = 4;
        }
        if (diamond(x, y, 0x4c6, -0x86, 0x572, 0x24)
                || insideExclusive(x, y, 0x9f7, 0xba0, 0x751, 0x85b)) {
            music = 7;
        }
        if (insideExclusive(x, y, 0x655, 0x717, 0x6d0, 0x793)
                || insideExclusive(x, y, 0xa87, 0xb20, 0x890, 0xa30)) {
            music = 3;
        }
        if (insideExclusive(x, y, 0xb6f, 0xbcf, 0x38b, 0x3c7)
                || insideExclusive(x, y, 0xb4f, 0xbcf, 0x3c6, 0x3fd)
                || insideExclusive(x, y, 0xae0, 0xbcf, 0x3fc, 0x403)
                || insideExclusive(x, y, 0xada, 0xbcf, 0x402, 0x407)
                || insideExclusive(x, y, 0xac8, 0xbcf, 0x406, 0x417)
                || insideExclusive(x, y, 0xab7, 0xbcf, 0x416, 0x425)
                || insideExclusive(x, y, 0xaa9, 0xbcf, 0x424, 0x434)
                || insideExclusive(x, y, 0xaa4, 0xbcf, 0x433, 0x4fc)) {
            music = 1;
        }
        if (diamond(x, y, 0xacf, -0x237, 0xb83, -0x13d)
                || diamond(x, y, 0xb84, -0x236, 0xc28, -0x12e)) {
            music = 1;
        }
        if (diamond(x, y, 0xf08, -0x6ba, 0xf3e, -0x67e)
                || diamond(x, y, 0xf3f, -0x6a5, 0xf43, -0x697)) {
            music = 4;
        }
        if (diamond(x, y, 0xe59, 0x349, 0xecf, 0x44f)
                || diamond(x, y, 0xed0, 0x30a, 0xf12, 0x450)
                || diamond(x, y, 0xf13, 0x2f7, 0xf6f, 0x44f)
                || diamond(x, y, 0xf70, 0x31c, 0xfc4, 0x450)
                || diamond(x, y, 0xfc5, 0x335, 0x10d7, 0x451)) {
            music = 1;
        }
        if (diamond(x, y, 0x79e, 0x582, 0x7b2, 0x596)
                || diamond(x, y, 0x79e, 0x4ec, 0x7b2, 0x500)
                || diamond(x, y, 0x82a, 0x4ec, 0x83e, 0x500)
                || diamond(x, y, 0x82a, 0x582, 0x83e, 0x596)
                || diamond(x, y, 0x7a9, 0x501, 0x833, 0x581)) {
            music = 5;
        }
        if (diamond(x, y, 0x9e7, 0x66d, 0xa2d, 0x6b3)) {
            music = 0;
        }
        return music;
    }

    private static boolean insideExclusive(int x, int y, int minX, int maxX, int minY, int maxY) {
        return x > minX && x < maxX && y > minY && y < maxY;
    }

    private static boolean diamond(int x, int y, int lowerSum, int lowerDifference,
            int upperSum, int upperDifference) {
        return lowerSum - x <= y
                && x + lowerDifference <= y
                && y <= upperSum - x
                && y <= x + upperDifference;
    }

    private static File sidecarFile(File mapFile) {
        String name = mapFile.getName()
                .replace(".mapbin", "")
                .replace(".map", "")
                .replace(".json.gz", "");
        File parent = mapFile.getParentFile();
        return new File(parent != null ? parent : new File("."), name + ".musiczones.bin");
    }

    private record RunKey(int musicId, int x1, int x2) {
    }
}
