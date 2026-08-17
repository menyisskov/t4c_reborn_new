package com.perso.T4C.npc;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Central access to NPC definitions stored in {@link Paths#NPCS_BIN}.
 * Mirrors {@code SpellRegistry} but keeps a small cache so per-spawn lookups by
 * name are O(1).
 */
public final class NpcRegistry {
    private static List<NpcDef> cache;
    private static Map<String, NpcDef> byName;

    private NpcRegistry() {
    }

    /** Load all definitions (cached). Returns an empty list if the file is missing/invalid. */
    public static synchronized List<NpcDef> load() {
        if (cache != null) {
            return cache;
        }
        File file = new File(Paths.NPCS_BIN);
        List<NpcDef> defs = List.of();
        if (file.exists()) {
            try {
                defs = NpcDefBinaryIO.read(file);
            } catch (Exception ignored) {
            }
        }
        rebuild(defs);
        return cache;
    }

    /** Persist the given definitions and refresh the cache. */
    public static synchronized void save(List<NpcDef> defs) throws IOException {
        NpcDefBinaryIO.write(new File(Paths.NPCS_BIN), defs);
        rebuild(defs);
    }

    /** Lookup a definition by name, or null if absent. */
    public static synchronized NpcDef findByName(String name) {
        load();
        return name == null ? null : byName.get(normalize(name));
    }

    /** Drop the in-memory cache so the next {@link #load()} re-reads from disk. */
    public static synchronized void invalidate() {
        cache = null;
        byName = null;
    }

    private static void rebuild(List<NpcDef> defs) {
        cache = List.copyOf(defs);
        Map<String, NpcDef> map = new LinkedHashMap<>();
        for (NpcDef def : cache) {
            if (def != null && def.getName() != null) {
                map.put(normalize(def.getName()), def);
            }
        }
        byName = map;
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
