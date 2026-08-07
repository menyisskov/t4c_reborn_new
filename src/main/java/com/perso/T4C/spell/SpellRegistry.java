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
        SpellData identified = byName.get(identityOf(name));
        if (identified != null) return identified;
        String requested = canonicalKey(name);
        for (SpellData spell : cache) {
            if (spell != null && requested.equals(canonicalKey(spell.getKey()))) return spell;
        }
        return null;
    }

    /** Reduces any of the accepted spell designations to the registry's key. */
    static String identityOf(String value) {
        String key = I18n.keyOf(value);
        if (key != null) return key;
        if (value.startsWith("spell.")) return value;
        String candidate = "spell." + I18n.normalizedKey(value);
        return I18n.has(candidate) ? candidate : value;
    }

    private static String canonicalKey(String value) {
        if (value == null) return "";
        String key = I18n.keyOf(value);
        if (key != null) value = key;
        while (value.startsWith("spell.spell_")) {
            value = "spell." + value.substring("spell.spell_".length());
        }
        return value;
    }

    public static synchronized SpellData findById(int spellId) {
        if (spellId <= 0) return null;
        for (SpellData spell : load()) {
            if (spell != null && spell.getSpellId() == spellId) return spell;
        }
        return null;
    }

    /**
     * Returns the visible, player-castable spell catalogue in data-file order.
     * Item procs, monster abilities, test entries and secondary effect records
     * share the same binary registry but must never appear in a player's book.
     */
    public static synchronized List<SpellData> playerCastableSpells() {
        Map<String, SpellData> result = new LinkedHashMap<>();
        for (SpellData spell : load()) {
            if (!isPlayerCastable(spell)) continue;
            result.putIfAbsent(canonicalKey(spell.getKey()), spell);
        }
        return List.copyOf(result.values());
    }

    private static boolean isPlayerCastable(SpellData spell) {
        if (spell == null || spell.getName() == null || spell.getName().isBlank()) return false;
        if (spell.getIconId() == null || spell.getIconId().isBlank()
                || "0".equals(spell.getIconId())) return false;
        String identity = spell.getName().toLowerCase();
        if (!identity.startsWith("${spell.")) return false;
        if (identity.startsWith("${spell.item_")
                || identity.startsWith("${spell.mob_")
                || identity.startsWith("${spell.test_")
                || identity.startsWith("${spell.npc_")) return false;
        if (identity.endsWith("_effect}")) return false;
        return !spell.getT4cEffects().isEmpty() || identity.equals("${spell.tame_beast}");
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
