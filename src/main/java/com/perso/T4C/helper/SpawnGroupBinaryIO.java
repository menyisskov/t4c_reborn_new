package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.SpawnGroup;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        return BinaryCatalogueIO.read(file, MAGIC, "spawn group",
                version -> {
                    if (version < 1 || version > VERSION) {
                        throw new GameException("Unsupported spawn group version: " + version);
                    }
                },
                (in, version) -> readGroup(in));
    }

    public static void write(File file, List<SpawnGroup> groups) throws IOException {
        BinaryCatalogueIO.write(file, MAGIC, VERSION, groups, SpawnGroupBinaryIO::writeGroup);
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
