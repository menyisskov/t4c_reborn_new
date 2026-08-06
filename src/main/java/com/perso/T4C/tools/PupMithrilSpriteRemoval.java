package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpriteBinIO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One-shot cleanup: drops the {@code PupMithril*} equipped-appearance sprites from the sprite pack.
 *
 * <p>These frames back the mithril plate armour paper-doll (body, helm, legs, feet and both
 * gloves/arms, over 8 angles and 13 frames each). Removing them leaves the matching
 * {@code item.mithril_plate_*} definitions in place: an item whose appearance sprite is absent
 * simply renders nothing on the equipped character, it does not break loading.
 *
 * <p>Sprite order is otherwise preserved, so every remaining sprite keeps its relative position;
 * ids are sequential over the pack, so they shift down for entries after the first removal. That is
 * safe because ids are never persisted — lookups always go through the name index built at load
 * time by {@link com.perso.T4C.helper.SpriteLoader}.
 *
 * <p>Usage: {@code PupMithrilSpriteRemoval [assets/sprites] [--dry-run]}
 */
public final class PupMithrilSpriteRemoval {

    /** Matched case-insensitively against the sprite name. */
    private static final String PREFIX = "pupmithril";

    private PupMithrilSpriteRemoval() {
    }

    public static void main(String[] args) throws IOException {
        Path dir = Path.of(args.length > 0 && !args[0].startsWith("--") ? args[0] : Paths.SPRITE_DIR);
        boolean dryRun = List.of(args).contains("--dry-run");

        List<SpriteBinIO.Packed> kept = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        long removedBytes = 0;
        long keptBytes = 0;
        for (SpriteBinIO.Packed sprite : SpriteBinIO.readAllToList(dir, Paths.SPRITE_BIN_BASE)) {
            if (matches(sprite.name())) {
                removed.add(sprite.name());
                removedBytes += sprite.png().length;
            } else {
                kept.add(sprite);
                keptBytes += sprite.png().length;
            }
        }

        System.out.printf("Sprites: %d kept (%.1f MB), %d removed (%.1f MB)%n",
                kept.size(), keptBytes / 1048576.0, removed.size(), removedBytes / 1048576.0);
        if (removed.isEmpty()) {
            System.out.println("Nothing to remove.");
            return;
        }
        System.out.println("First removed: " + removed.get(0)
                + " … last: " + removed.get(removed.size() - 1));

        if (dryRun) {
            System.out.println("Dry run: sprite pack left untouched.");
            return;
        }

        int shards = SpriteBinIO.writeSharded(dir, Paths.SPRITE_BIN_BASE, kept);
        int reread = SpriteBinIO.readAllToList(dir, Paths.SPRITE_BIN_BASE).size();
        if (reread != kept.size()) {
            throw new IOException("Verification failed: read back " + reread + " of " + kept.size());
        }
        System.out.println("Wrote " + shards + " shard(s); verified " + reread + " sprites.");
    }

    private static boolean matches(String name) {
        return name.toLowerCase(Locale.ROOT).startsWith(PREFIX);
    }
}
