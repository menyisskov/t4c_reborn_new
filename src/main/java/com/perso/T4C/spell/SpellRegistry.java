package com.perso.T4C.spell;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.i18n.I18n;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SpellRegistry {
    private static List<SpellData> cache;
    private static Map<String, SpellData> byName;

    private SpellRegistry() {
    }

    public static synchronized List<SpellData> load() {
        if (cache != null) {
            return cache;
        }
        File file = new File(Paths.SPELLS_BIN);
        if (!file.exists()) {
            rebuild(List.of());
            return cache;
        }
        try {
            rebuild(SpellBinaryIO.read(file));
        } catch (Exception ignored) {
            rebuild(List.of());
        }
        return cache;
    }

    public static synchronized void save(List<SpellData> spells) throws IOException {
        SpellBinaryIO.write(new File(Paths.SPELLS_BIN), spells);
        rebuild(spells);
    }

    /**
     * Looks a spell up by its stored identity ({@code ${spell.x}}), by the bare
     * catalogue key, or by the text a player sees. Spell definitions carry a
     * placeholder rather than prose, so callers hold whichever form their own
     * source of truth recorded.
     */
    public static synchronized SpellData findByName(String name) {
        load();
        if (name == null || name.isBlank()) return null;
        SpellData direct = byName.get(name);
        if (direct != null) return direct;
        return byName.get(identityOf(name));
    }

    /** Reduces any of the accepted spell designations to the registry's key. */
    static String identityOf(String value) {
        String key = I18n.keyOf(value);
        if (key != null) return key;
        String candidate = "spell." + I18n.normalizedKey(value);
        return I18n.has(candidate) ? candidate : value;
    }

    public static synchronized SpellData findById(int spellId) {
        if (spellId <= 0) return null;
        for (SpellData spell : load()) {
            if (spell != null && spell.getSpellId() == spellId) return spell;
        }
        return null;
    }

    public static synchronized Map<String, SpellData> allByName() {
        load();
        return byName;
    }

    public static synchronized void invalidate() {
        cache = null;
        byName = null;
    }

    private static void rebuild(List<SpellData> spells) {
        cache = List.copyOf(spells == null ? List.of() : spells);
        Map<String, SpellData> map = new LinkedHashMap<>();
        for (SpellData spell : cache) {
            if (spell != null && spell.getName() != null) {
                map.put(spell.getName(), spell);
                String key = spell.getKey();
                if (key != null) {
                    map.putIfAbsent(key, spell);
                }
            }
        }
        byName = Map.copyOf(map);
    }
}
