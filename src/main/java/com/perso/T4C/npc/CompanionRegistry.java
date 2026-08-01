package com.perso.T4C.npc;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.CompanionDefBinaryIO;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central access to companion definitions stored in {@link Paths#COMPANIONS_BIN},
 * mirroring {@link NpcRegistry}.
 */
@Slf4j
public final class CompanionRegistry {
    private static List<CompanionDef> cache;
    private static Map<String, CompanionDef> byId;

    private CompanionRegistry() {
    }

    /** Load all definitions (cached). Returns an empty list if the file is missing/invalid. */
    public static synchronized List<CompanionDef> load() {
        if (cache != null) {
            return cache;
        }
        File file = new File(Paths.COMPANIONS_BIN);
        List<CompanionDef> defs = List.of();
        if (file.exists()) {
            try {
                defs = CompanionDefBinaryIO.read(file);
            } catch (Exception e) {
                // Unlike NPC spawns, a broken companion file silently disables
                // every summon, so make the cause visible.
                log.warn("Failed to read companion definitions from {}", Paths.COMPANIONS_BIN, e);
            }
        }
        rebuild(defs);
        return cache;
    }

    /** Persist the given definitions and refresh the cache. */
    public static synchronized void save(List<CompanionDef> defs) throws IOException {
        CompanionDefBinaryIO.write(new File(Paths.COMPANIONS_BIN), defs);
        rebuild(defs);
    }

    /** Lookup a definition by its id, or null if absent. */
    public static synchronized CompanionDef findById(String id) {
        load();
        return id == null ? null : byId.get(id);
    }

    /** Drop the in-memory cache so the next {@link #load()} re-reads from disk. */
    public static synchronized void invalidate() {
        cache = null;
        byId = null;
    }

    private static void rebuild(List<CompanionDef> defs) {
        cache = List.copyOf(defs);
        Map<String, CompanionDef> map = new LinkedHashMap<>();
        for (CompanionDef def : cache) {
            if (def != null && def.getId() != null) {
                map.put(def.getId(), def);
            }
        }
        byId = map;
    }
}
