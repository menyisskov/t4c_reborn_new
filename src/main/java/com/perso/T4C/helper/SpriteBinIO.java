package com.perso.T4C.helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Single source of truth for the {@code sprites.bin} format (T4C1 v1 payload).
 *
 * <p>The sprite library is split into shards {@code sprites_0.bin}, {@code sprites_1.bin}, … each
 * kept under the 100 MB limit imposed by Git/LFS. Every shard is a complete T4C1 v1 payload, valid
 * on its own: the entry format is unchanged, only the spread across several files is new. The
 * global logical order is the concatenation of the shards by increasing numeric index, which
 * preserves sprite ids (sequential indices) and the first-wins rule on lowercased names.
 *
 * <p>A monolithic {@code sprites.bin} is still read when no shard is present.
 */
public final class SpriteBinIO {

    private static final String MAGIC = "T4C1";
    private static final int VERSION = 1;

    /** Base name of the shards, without index or extension. */
    public static final String DEFAULT_BASE_NAME = "sprites";

    /**
     * Uncompressed payload size beyond which a new shard is started. The margin under 100 MB covers
     * the fact that an entry is never split: a shard may exceed this threshold by the size of its
     * last entry.
     */
    static final long SHARD_MAX_BYTES = 90L * 1024 * 1024;

    /** Fixed-size integer fields of an entry: 8 int32 (dims, offsets, type, pngLen). */
    private static final int ENTRY_FIXED_BYTES = 8 * 4;

    /** A serialized sprite: metadata plus the raw PNG payload. */
    public record Packed(String name, int width, int height, int off1X, int off1Y,
                         int off2X, int off2Y, int type, byte[] png) {

        /** Size this entry will occupy in the uncompressed payload. */
        long serializedSize() {
            return 4L + name.getBytes(StandardCharsets.UTF_8).length + ENTRY_FIXED_BYTES + png.length;
        }
    }

    private SpriteBinIO() {
    }

    /** A 32x16 sprite is a ground tile, everything else is a free sprite. */
    public static int spriteType(int width, int height) {
        return width == 32 && height == 16 ? 0 : 1;
    }

    /** Lookup key: sprite names are case-insensitive. */
    public static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /** File name of the shard at the given index. */
    public static String shardName(String baseName, int index) {
        return baseName + "_" + index + ".bin";
    }

    /** Historical monolithic file name. */
    public static String legacyName(String baseName) {
        return baseName + ".bin";
    }

    /**
     * Resolves the ordered list of files to read for {@code baseName} in {@code dir}.
     *
     * <p>Shards are discovered by listing the directory, filtered on a fully numeric suffix and
     * sorted numerically (so that {@code sprites_10.bin} follows {@code sprites_9.bin} rather than
     * {@code sprites_1.bin}). When no shard exists, falls back to the historical monolithic file.
     * Returns an empty list when nothing is found.
     */
    public static List<Path> resolveShards(Path dir, String baseName) throws IOException {
        List<Path> shards = new ArrayList<>();
        if (Files.isDirectory(dir)) {
            String prefix = baseName + "_";
            try (Stream<Path> entries = Files.list(dir)) {
                entries.filter(Files::isRegularFile)
                        .filter(p -> shardIndex(p.getFileName().toString(), prefix) >= 0)
                        .sorted(Comparator.comparingInt(p -> shardIndex(p.getFileName().toString(), prefix)))
                        .forEach(shards::add);
            }
        }
        if (!shards.isEmpty()) {
            return shards;
        }
        Path legacy = dir.resolve(legacyName(baseName));
        return Files.exists(legacy) ? List.of(legacy) : List.of();
    }

    /**
     * Shard index for a file name, or -1 when it is not a shard of {@code prefix}.
     */
    private static int shardIndex(String fileName, String prefix) {
        if (!fileName.startsWith(prefix) || !fileName.endsWith(".bin")) {
            return -1;
        }
        String digits = fileName.substring(prefix.length(), fileName.length() - ".bin".length());
        if (digits.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < digits.length(); i++) {
            if (!Character.isDigit(digits.charAt(i))) {
                return -1;
            }
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Reads every sprite in {@code dir} in the global logical order and pushes them into
     * {@code sink}. Nothing is accumulated here: the caller decides what to retain, which lets
     * tools that only need metadata drop the PNG payloads as they stream past.
     */
    public static void readAll(Path dir, String baseName, Consumer<Packed> sink) throws IOException {
        for (Path shard : resolveShards(dir, baseName)) {
            readShard(shard, sink);
        }
    }

    /** Reads a single T4C1 file and pushes its entries into {@code sink}. */
    public static void readShard(Path file, Consumer<Packed> sink) throws IOException {
        try (InputStream raw = Files.newInputStream(file);
                DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(raw))) {
            readPayload(in, file.toString(), sink);
        }
    }

    /**
     * Parses an already decompressed T4C1 payload. {@code source} is only used for error messages.
     */
    public static void readPayload(DataInputStream in, String source, Consumer<Packed> sink)
            throws IOException {
        byte[] magic = new byte[4];
        in.readFully(magic);
        String magicStr = new String(magic, StandardCharsets.US_ASCII);
        if (!MAGIC.equals(magicStr)) {
            throw new IOException("Unsupported sprite binary magic: " + magicStr + " in " + source);
        }
        int version = in.readInt();
        if (version != VERSION) {
            throw new IOException("Unsupported sprite binary version: " + version + " in " + source);
        }
        int count = in.readInt();
        if (count < 0) {
            throw new IOException("Invalid sprite count: " + count + " in " + source);
        }

        for (int i = 0; i < count; i++) {
            int nameLen = in.readInt();
            if (nameLen <= 0) {
                throw new IOException("Invalid sprite name length: " + nameLen + " in " + source);
            }
            byte[] nameBytes = new byte[nameLen];
            in.readFully(nameBytes);
            String name = new String(nameBytes, StandardCharsets.UTF_8);

            int width = in.readInt();
            int height = in.readInt();
            int off1X = in.readInt();
            int off1Y = in.readInt();
            int off2X = in.readInt();
            int off2Y = in.readInt();
            int type = in.readInt();

            int pngLen = in.readInt();
            if (pngLen <= 0) {
                throw new IOException("Invalid PNG payload length: " + pngLen + " for sprite " + name
                        + " in " + source);
            }
            byte[] png = new byte[pngLen];
            in.readFully(png);

            sink.accept(new Packed(name, width, height, off1X, off1Y, off2X, off2Y, type, png));
        }
    }

    /** Reads every sprite into a list, in the global logical order. */
    public static List<Packed> readAllToList(Path dir, String baseName) throws IOException {
        List<Packed> sprites = new ArrayList<>();
        readAll(dir, baseName, sprites::add);
        return sprites;
    }

    /**
     * Writes {@code sprites} into shards smaller than {@link #SHARD_MAX_BYTES}, preserving order.
     *
     * <p>Writing goes through temporary files moved into place. Stale shards with an index above
     * the last one written are deleted — otherwise a split producing fewer shards than before would
     * leave ghost sprites to be read back on the next load. The historical monolithic file is
     * removed as well once the shards are in place.
     *
     * @return the number of shards written
     */
    public static int writeSharded(Path dir, String baseName, List<Packed> sprites)
            throws IOException {
        Files.createDirectories(dir);

        List<List<Packed>> groups = groupIntoShards(sprites);
        List<Path> temps = new ArrayList<>();
        try {
            for (int i = 0; i < groups.size(); i++) {
                Path tmp = dir.resolve(shardName(baseName, i) + ".tmp");
                writePayload(tmp, groups.get(i));
                temps.add(tmp);
            }
            // Every payload is written: swap them into place, then clean up the leftovers.
            for (int i = 0; i < temps.size(); i++) {
                Files.move(temps.get(i), dir.resolve(shardName(baseName, i)),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            temps.clear();
        } finally {
            for (Path tmp : temps) {
                Files.deleteIfExists(tmp);
            }
        }

        for (int i = groups.size(); ; i++) {
            Path stale = dir.resolve(shardName(baseName, i));
            if (!Files.deleteIfExists(stale)) {
                break;
            }
        }
        Files.deleteIfExists(dir.resolve(legacyName(baseName)));

        return groups.size();
    }

    /**
     * Splits the sprites into sequential groups under the size threshold, never cutting an entry.
     * A sprite larger than the threshold gets a shard of its own.
     */
    private static List<List<Packed>> groupIntoShards(List<Packed> sprites) {
        List<List<Packed>> groups = new ArrayList<>();
        List<Packed> current = new ArrayList<>();
        long currentBytes = 0;
        for (Packed sprite : sprites) {
            long size = sprite.serializedSize();
            if (!current.isEmpty() && currentBytes + size > SHARD_MAX_BYTES) {
                groups.add(current);
                current = new ArrayList<>();
                currentBytes = 0;
            }
            current.add(sprite);
            currentBytes += size;
        }
        // Always at least one shard, even empty: no file at all would mean "no pack".
        if (!current.isEmpty() || groups.isEmpty()) {
            groups.add(current);
        }
        return groups;
    }

    /** Writes a complete T4C1 payload into {@code file}. */
    public static void writePayload(Path file, List<Packed> sprites) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                BinaryIOUtils.openOutputStream(file.toFile(), 1 << 20))) {
            out.write(MAGIC.getBytes(StandardCharsets.US_ASCII));
            out.writeInt(VERSION);
            out.writeInt(sprites.size());
            for (Packed sprite : sprites) {
                byte[] nameBytes = sprite.name().getBytes(StandardCharsets.UTF_8);
                out.writeInt(nameBytes.length);
                out.write(nameBytes);
                out.writeInt(sprite.width());
                out.writeInt(sprite.height());
                out.writeInt(sprite.off1X());
                out.writeInt(sprite.off1Y());
                out.writeInt(sprite.off2X());
                out.writeInt(sprite.off2Y());
                out.writeInt(sprite.type());
                out.writeInt(sprite.png().length);
                out.write(sprite.png());
            }
        }
    }
}
