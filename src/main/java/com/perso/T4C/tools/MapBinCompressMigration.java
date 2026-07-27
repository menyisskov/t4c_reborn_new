package com.perso.T4C.tools;

import com.perso.T4C.helper.BinaryIOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * One-shot migration: wraps raw {@code .mapbin} / {@code .decorbin} / {@code .colbin} files in the
 * {@code T4CBIN} Deflate container so no asset exceeds the 90 MB Git/LFS budget.
 *
 * <p>These files are highly repetitive — run-length encoded sprite ids plus per-tile metadata that
 * is overwhelmingly default — and shrink to roughly a fifth of their size. The payload itself is
 * untouched: only the container is added, and {@code MapReader} reads compressed and raw files
 * alike, so the migration is reversible and safe to run repeatedly.
 *
 * <p>Each file is verified by inflating it back and comparing byte-for-byte with the original
 * before the original is replaced.
 *
 * <p>Usage: {@code MapBinCompressMigration [assets/maps] [--apply]}
 */
public final class MapBinCompressMigration {

    private static final byte[] COMPRESSED_MAGIC = "T4CBIN".getBytes(StandardCharsets.US_ASCII);
    private static final String[] EXTENSIONS = { ".mapbin", ".decorbin", ".colbin" };

    private MapBinCompressMigration() {
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of("assets/maps");
        boolean apply = false;
        for (String arg : args) {
            if ("--apply".equals(arg)) {
                apply = true;
            } else {
                root = Path.of(arg);
            }
        }
        if (!Files.isDirectory(root)) {
            System.err.println("Not a directory: " + root);
            System.exit(2);
        }

        List<Path> targets = new ArrayList<>();
        try (var walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile)
                    .filter(MapBinCompressMigration::hasTargetExtension)
                    .sorted()
                    .forEach(targets::add);
        }

        long before = 0;
        long after = 0;
        int compressed = 0;
        int skipped = 0;
        for (Path file : targets) {
            long size = Files.size(file);
            before += size;
            if (isCompressed(file)) {
                after += size;
                skipped++;
                System.out.printf("  skip     %-46s %8.2f MB (already compressed)%n",
                        file.getFileName(), mb(size));
                continue;
            }

            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            byte[] original = Files.readAllBytes(file);
            try (var out = BinaryIOUtils.openOutputStream(tmp.toFile(), 1 << 20)) {
                out.write(original);
            }

            // Verify before replacing: inflate back and compare byte-for-byte.
            byte[] roundTrip;
            try (InputStream in = BinaryIOUtils.openInputStream(tmp.toFile(), 1 << 20)) {
                roundTrip = in.readAllBytes();
            }
            if (!Arrays.equals(original, roundTrip)) {
                Files.deleteIfExists(tmp);
                System.err.println("VERIFY FAILED, left untouched: " + file);
                System.exit(1);
            }

            long newSize = Files.size(tmp);
            after += newSize;
            compressed++;
            System.out.printf("  %-8s %-46s %8.2f MB -> %8.2f MB  (%.1f%%)%n",
                    apply ? "compress" : "would", file.getFileName(), mb(size), mb(newSize),
                    100.0 * newSize / size);

            if (apply) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.deleteIfExists(tmp);
            }
        }

        System.out.printf("%n%d file(s): %d compressed, %d already done.%n",
                targets.size(), compressed, skipped);
        System.out.printf("Total %.2f MB -> %.2f MB (%.1f%%)%n", mb(before), mb(after),
                before == 0 ? 100.0 : 100.0 * after / before);
        if (!apply) {
            System.out.println("Dry run (pass --apply to write changes).");
        }
    }

    private static boolean hasTargetExtension(Path file) {
        String name = file.getFileName().toString();
        for (String ext : EXTENSIONS) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCompressed(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            return Arrays.equals(in.readNBytes(COMPRESSED_MAGIC.length), COMPRESSED_MAGIC);
        }
    }

    private static double mb(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
}
