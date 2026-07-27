package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.SpawnGroup;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Binary serialization for {@link SpawnGroup} lists.
 *
 * <p>Version history:
 * <ul>
 *   <li>v1 — initial: name, tmin, tmax, distance, spawnCount, creatures[], positions[]</li>
 * </ul>
 */
public final class SpawnGroupBinaryIO {
    private static final byte[] MAGIC = "T4CGRP".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 1;
    private static final int MAX_STRING_BYTES = 4096;

    private SpawnGroupBinaryIO() {
    }

    public static List<SpawnGroup> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid spawn group file: wrong magic header");
            }
            short version = BinaryIOUtils.readShortLE(in);
            if (version < 1 || version > VERSION) {
                throw new GameException("Unsupported spawn group version: " + version);
            }
            int count = BinaryIOUtils.readIntLE(in);
            if (count < 0) throw new GameException("Invalid spawn group count: " + count);
            List<SpawnGroup> groups = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                groups.add(readGroup(in));
            }
            return groups;
        }
    }

    public static void write(File file, List<SpawnGroup> groups) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
            out.write(MAGIC);
            BinaryIOUtils.writeShortLE(out, VERSION);
            BinaryIOUtils.writeIntLE(out, groups == null ? 0 : groups.size());
            if (groups == null) return;
            for (SpawnGroup g : groups) writeGroup(out, g);
        }
    }

    private static SpawnGroup readGroup(DataInputStream in) throws IOException {
        String name = BinaryIOUtils.readString(in, MAX_STRING_BYTES);
        int tmin = BinaryIOUtils.readIntLE(in);
        int tmax = BinaryIOUtils.readIntLE(in);
        int distance = BinaryIOUtils.readIntLE(in);
        int spawnCount = BinaryIOUtils.readIntLE(in);

        int creatureCount = BinaryIOUtils.readIntLE(in);
        List<String> creatures = new ArrayList<>(creatureCount);
        for (int i = 0; i < creatureCount; i++) {
            creatures.add(BinaryIOUtils.readString(in, MAX_STRING_BYTES));
        }

        int posCount = BinaryIOUtils.readIntLE(in);
        List<SpawnGroup.SpawnPoint> positions = new ArrayList<>(posCount);
        for (int i = 0; i < posCount; i++) {
            int x = BinaryIOUtils.readIntLE(in);
            int y = BinaryIOUtils.readIntLE(in);
            int z = BinaryIOUtils.readIntLE(in);
            positions.add(new SpawnGroup.SpawnPoint(x, y, z));
        }

        return new SpawnGroup(name, tmin, tmax, distance, spawnCount, creatures, positions);
    }

    private static void writeGroup(DataOutputStream out, SpawnGroup g) throws IOException {
        BinaryIOUtils.writeString(out, g == null ? "" : g.getName());
        BinaryIOUtils.writeIntLE(out, g == null ? 0 : g.getTmin());
        BinaryIOUtils.writeIntLE(out, g == null ? 0 : g.getTmax());
        BinaryIOUtils.writeIntLE(out, g == null ? 0 : g.getDistance());
        BinaryIOUtils.writeIntLE(out, g == null ? 0 : g.getSpawnCount());

        List<String> creatures = g == null ? null : g.getCreatures();
        BinaryIOUtils.writeIntLE(out, creatures == null ? 0 : creatures.size());
        if (creatures != null) {
            for (String c : creatures) BinaryIOUtils.writeString(out, c == null ? "" : c);
        }

        List<SpawnGroup.SpawnPoint> positions = g == null ? null : g.getPositions();
        BinaryIOUtils.writeIntLE(out, positions == null ? 0 : positions.size());
        if (positions != null) {
            for (SpawnGroup.SpawnPoint p : positions) {
                BinaryIOUtils.writeIntLE(out, p == null ? 0 : p.getX());
                BinaryIOUtils.writeIntLE(out, p == null ? 0 : p.getY());
                BinaryIOUtils.writeIntLE(out, p == null ? 0 : p.getZ());
            }
        }
    }
}
