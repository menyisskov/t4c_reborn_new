package com.perso.T4C.monster;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.MonsterDefBinaryIO;
import com.perso.T4C.i18n.I18n;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Central access to monster definitions stored in {@link Paths#MONSTERS_BIN}.
 * Mirrors {@code SpellRegistry} but keeps a small cache so per-spawn lookups by
 * name are O(1).
 */
public final class MonsterRegistry {
    private static List<MonsterDef> cache;
    private static Map<String, MonsterDef> byName;
    private static Map<String, MonsterDef> byNormalizedName;
    private static Map<String, MonsterDef> byAlias;

    private MonsterRegistry() {
    }

    /** Load all definitions (cached). Returns an empty list if the file is missing/invalid. */
    public static synchronized List<MonsterDef> load() {
        if (cache != null) {
            return cache;
        }
        File file = new File(Paths.MONSTERS_BIN);
        if (!file.exists()) {
            rebuild(List.of());
            return cache;
        }
        try {
            rebuild(MonsterDefBinaryIO.read(file));
        } catch (Exception ignored) {
            rebuild(List.of());
        }
        return cache;
    }

    /** Persist the given definitions and refresh the cache. */
    public static synchronized void save(List<MonsterDef> defs) throws IOException {
        MonsterDefBinaryIO.write(new File(Paths.MONSTERS_BIN), defs);
        rebuild(defs);
    }

    /** Lookup a definition by name, or null if absent. */
    public static synchronized MonsterDef findByName(String name) {
        load();
        if (name == null) return null;
        MonsterDef exact = byName.get(name);
        if (exact != null) return exact;
        MonsterDef normalized = byNormalizedName.get(normalize(name));
        if (normalized != null) return normalized;
        return byAlias.get(name);
    }

    /** Drop the in-memory cache so the next {@link #load()} re-reads from disk. */
    public static synchronized void invalidate() {
        cache = null;
        byName = null;
        byNormalizedName = null;
    }

    private static void rebuild(List<MonsterDef> defs) {
        cache = List.copyOf(defs);
        Map<String, MonsterDef> map = new LinkedHashMap<>();
        Map<String, MonsterDef> normalized = new LinkedHashMap<>();
        Map<String, MonsterDef> aliases = new LinkedHashMap<>();
        for (MonsterDef def : cache) {
            if (def != null && def.getName() != null) {
                map.put(def.getName(), def);
                normalized.putIfAbsent(normalize(def.getName()), def);
                // displayName is a ${monster.x} placeholder, so index the key it
                // points at rather than the placeholder text itself.
                String displayKey = I18n.keyOf(def.getDisplayName());
                if (displayKey != null) {
                    normalized.putIfAbsent(normalize(displayKey.substring(displayKey.indexOf('.') + 1)), def);
                } else if (def.getDisplayName() != null) {
                    normalized.putIfAbsent(normalize(def.getDisplayName()), def);
                }
                if (def.getSpawnAliases() != null) {
                    for (String alias : def.getSpawnAliases()) {
                        if (alias != null && !alias.isBlank()) aliases.putIfAbsent(alias, def);
                    }
                }
            }
        }
        byName = map;
        byNormalizedName = normalized;
        byAlias = aliases;
    }

    private static String normalize(String value) {
        return value.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }
}
