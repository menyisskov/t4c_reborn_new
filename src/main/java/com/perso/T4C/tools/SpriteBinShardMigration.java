package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpriteBinIO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * One-shot migration: converts a monolithic {@code sprites.bin} into shards
 * {@code sprites_0.bin}, {@code sprites_1.bin}, … each under the 100 MB Git/LFS limit.
 *
 * <p>Sprite content and order are strictly preserved: only the spread across several files
 * changes. The original is deleted only after the shards have been read back and the sprite count
 * verified to be identical.
 *
 * <p>Usage: {@code SpriteBinShardMigration [assets/sprites]}
 */
public final class SpriteBinShardMigration {

    private SpriteBinShardMigration() {
    }

    public static void main(String[] args) throws IOException {
        Path dir = Path.of(args.length > 0 ? args[0] : Paths.SPRITE_DIR);
        String base = Paths.SPRITE_BIN_BASE;

        List<Path> existing = SpriteBinIO.resolveShards(dir, base);
        if (existing.isEmpty()) {
            System.err.println("No sprite library found in " + dir);
            System.exit(2);
        }
        if (existing.size() > 1 || !existing.get(0).getFileName().toString()
                .equals(SpriteBinIO.legacyName(base))) {
            System.out.println("Already sharded (" + existing.size() + " shard(s)), nothing to do.");
            return;
        }

        Path legacy = existing.get(0);
        System.out.println("Reading " + legacy + " (" + mb(Files.size(legacy)) + ")");
        List<SpriteBinIO.Packed> sprites = SpriteBinIO.readAllToList(dir, base);
        System.out.println("Sprites read: " + sprites.size());

        // writeSharded deletes the original: keep a copy aside while the shards are validated.
        Path backup = legacy.resolveSibling(SpriteBinIO.legacyName(base) + ".premigration");
        Files.copy(legacy, backup, StandardCopyOption.REPLACE_EXISTING);

        int shardCount = SpriteBinIO.writeSharded(dir, base, sprites);
        System.out.println("Wrote " + shardCount + " shard(s):");
        boolean oversized = false;
        for (Path shard : SpriteBinIO.resolveShards(dir, base)) {
            long size = Files.size(shard);
            oversized |= size >= 100L * 1024 * 1024;
            System.out.println("  " + shard.getFileName() + "  " + mb(size));
        }

        int reread = SpriteBinIO.readAllToList(dir, base).size();
        if (reread != sprites.size() || oversized) {
            // Shards take precedence over the monolith at load time: they must be removed before
            // restoring the original, otherwise the library would be left in an unverified state.
            for (Path shard : SpriteBinIO.resolveShards(dir, base)) {
                Files.deleteIfExists(shard);
            }
            Files.move(backup, legacy, StandardCopyOption.REPLACE_EXISTING);
            System.err.println("Verification failed (read back " + reread + " of " + sprites.size()
                    + ", oversized=" + oversized + "); original restored.");
            System.exit(1);
        }

        Files.delete(backup);
        System.out.println("Verified " + reread + " sprites across " + shardCount
                + " shard(s); original removed.");
    }

    private static String mb(long bytes) {
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
