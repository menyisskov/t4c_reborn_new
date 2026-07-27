package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.monster.MonsterRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/**
 * Clears the per-spawn "aggressive" override in monster_spawns.bin whenever it
 * contradicts the monster definition's own aggro value (MonsterDef.aggro <= 0,
 * mirroring the original server's dwAggressivness/agressive field). Every spawn
 * placement in this binary currently carries an explicit boolean (there is no
 * "inherit from definition" state), so passive monster types such as Olin Haad
 * Guard were incorrectly baked in as aggressive at every spawn point.
 */
public final class MonsterSpawnAggressionMigration {

    private MonsterSpawnAggressionMigration() {
    }

    public static void main(String[] args) throws Exception {
        boolean apply = args.length > 0 && "--apply".equals(args[0]);

        List<SpawnBinaryIO.Entry> entries = SpawnBinaryIO.read(new File(Paths.MONSTER_SPAWNS_BIN));
        List<SpawnBinaryIO.Entry> updated = new ArrayList<>(entries.size());

        TreeMap<String, Integer> changedByType = new TreeMap<>();
        List<String> unresolvedTypes = new ArrayList<>();
        int changed = 0;

        for (SpawnBinaryIO.Entry entry : entries) {
            MonsterDef def = MonsterRegistry.findByName(entry.type);
            if (def == null) {
                if (!unresolvedTypes.contains(entry.type)) unresolvedTypes.add(entry.type);
                updated.add(entry);
                continue;
            }

            if (entry.aggressive && !def.isDefaultAggressive()) {
                entry.aggressive = false;
                changed++;
                changedByType.merge(entry.type, 1, Integer::sum);
            }
            updated.add(entry);
        }

        System.out.printf(Locale.ROOT, "Spawn aggression: %d entries corrected, %d total%n", changed, entries.size());
        changedByType.forEach((type, count) -> System.out.printf(Locale.ROOT, "  %s: %d spawn(s)%n", type, count));
        if (!unresolvedTypes.isEmpty()) {
            System.out.println("Unresolved spawn types (no matching MonsterDef, left untouched):");
            unresolvedTypes.forEach(t -> System.out.println("  " + t));
        }

        if (apply) {
            SpawnBinaryIO.write(new File(Paths.MONSTER_SPAWNS_BIN), updated);
            System.out.println("Applied: " + Paths.MONSTER_SPAWNS_BIN + " rewritten.");
        } else {
            System.out.println("Dry run (pass --apply to write changes).");
        }
    }
}
