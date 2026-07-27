package com.perso.T4C.i18n;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Loads all user-facing translations from assets/i18n/*.json. */
public final class I18n {
    private static final Type CATALOGUE_TYPE = new TypeToken<Map<String, String>>() { }.getType();
    private static volatile Lang loadedLanguage;
    private static volatile Map<String, String> catalogue = Collections.emptyMap();
    private static final Pattern PLACEHOLDER = Pattern.compile("^\\$\\{([^{}]+)}$");
    private static volatile Map<String, String> englishCatalogue;

    private I18n() { }

    public static String t(String key, String english) {
        String placeholderKey = placeholderKey(english);
        if (placeholderKey != null) return value(placeholderKey, english(english));
        return value(key, english);
    }

    public static String message(String key, String english, Object... args) {
        String template = value(key, english);
        return args.length == 0 ? template : String.format(java.util.Locale.ROOT, template, args);
    }

    public static String tooltipLabel(String value) {
        return value(value == null ? "" : "tooltip." + value, value);
    }

    public static String tooltip(String key, String english) { return value(key, english); }
    public static String object(String name) { return value("object." + normalized(name), name); }
    public static String item(String name) { return translatedValue("item", name); }
    public static String spellName(String name) { return translatedValue("spell", name); }
    public static String monster(String name) { return translatedValue("monster", name); }
    public static String monster(String identity, String fallback) {
        if (identity == null || identity.isBlank()) return translatedValue("monster", fallback);
        return value("monster." + normalized(identity), fallback == null || fallback.isBlank() ? identity : fallback);
    }
    public static String npc(String name) { return translatedValue("npc", name); }
    public static String npcDialog(String npcName, String dialog) {
        String placeholderKey = placeholderKey(dialog);
        if (placeholderKey != null) return value(placeholderKey, english(dialog));
        return value("npc.dialog." + normalized(npcName), dialog);
    }
    public static String npcCastDialog(String npcName, String fallback) {
        return value("npc.cast_dialog." + normalized(npcName), fallback);
    }
    public static String npcKeyword(String npcName, String keyword) {
        String placeholderKey = placeholderKey(keyword);
        if (placeholderKey != null) return value(placeholderKey, english(keyword));
        return value("npc.keyword." + normalized(npcName), keyword);
    }
    public static String spellDescription(String spellName, String description) {
        String placeholderKey = placeholderKey(description);
        if (placeholderKey != null) return value(placeholderKey, english(description));
        return value("spell.description." + normalized(spellName), description);
    }

    /** Normalizes a name into the slug used inside catalogue keys (e.g. {@code Moonrock} -> {@code moonrock}). */
    public static String normalizedKey(String value) {
        return normalized(value);
    }

    /** Returns the on-disk representation used for player-facing strings in binary assets. */
    public static String placeholder(String namespace, String identity) {
        return placeholderFor(namespace, identity, identity);
    }

    public static String placeholderFor(String namespace, String identity, String value) {
        if (value == null || value.isBlank() || identity == null || identity.isBlank()) return value;
        if (placeholderKey(value) != null) return value;
        String key = namespace + "." + normalized(identity);
        return englishValues().containsKey(key) ? "${" + key + "}" : value;
    }

    public static String placeholderForKey(String key, String value) {
        if (value == null || value.isBlank() || key == null || key.isBlank()) return value;
        if (placeholderKey(value) != null) return value;
        return englishValues().containsKey(key) ? "${" + key + "}" : value;
    }

    public static synchronized void reloadEnglishCatalogue() {
        englishCatalogue = loadCatalogue(new File("assets/i18n/en.json"));
    }

    /** Resolves a placeholder through en.json for stable technical values used by registries. */
    public static String english(String value) {
        String key = placeholderKey(value);
        if (key == null) return value;
        return englishValues().getOrDefault(key, value);
    }

    private static Map<String, String> englishValues() {
        Map<String, String> english = englishCatalogue;
        if (english != null) return english;
        synchronized (I18n.class) {
            if (englishCatalogue == null) reloadEnglishCatalogue();
            return englishCatalogue;
        }
    }

    private static String value(String key, String fallback) {
        ensureLoaded();
        return displaySafe(catalogue.getOrDefault(key, fallback));
    }

    private static synchronized void ensureLoaded() {
        if (loadedLanguage == Lang.current()) return;
        File file = new File("assets/i18n/" + Lang.current().name().toLowerCase(java.util.Locale.ROOT) + ".json");
        Map<String, String> loaded = loadCatalogue(file);
        catalogue = Collections.unmodifiableMap(loaded);
        loadedLanguage = Lang.current();
    }

    private static Map<String, String> loadCatalogue(File file) {
        Map<String, String> loaded = new HashMap<>();
        if (file.isFile()) {
            try (FileReader reader = new FileReader(file)) {
                Map<String, String> parsed = new Gson().fromJson(reader, CATALOGUE_TYPE);
                if (parsed != null) loaded.putAll(parsed);
            } catch (Exception ignored) { }
        }
        return loaded;
    }

    private static String translatedValue(String namespace, String original) {
        String key = placeholderKey(original);
        return key == null ? translateWords(namespace, original) : value(key, english(original));
    }

    private static String placeholderKey(String value) {
        if (value == null) return null;
        Matcher matcher = PLACEHOLDER.matcher(value.trim());
        return matcher.matches() ? matcher.group(1) : null;
    }

    private static String normalized(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(java.util.Locale.ROOT)
                .replaceFirst("^\\s*\\[[^]]+]\\s*", "")
                .replaceFirst("^a\\s+", "")
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
    }

    private static String translateWords(String namespace, String original) {
        if (original == null) return null;
        ensureLoaded();
        String exact = catalogue.get(namespace + "." + normalized(original));
        if (exact != null) return displaySafe(exact);
        if (Lang.current() != Lang.FR) return displaySafe(original);
        StringBuilder result = new StringBuilder();
        for (String token : original.replaceFirst("^\\s*\\[[^]]+]\\s*", "").split("(?=[^A-Za-z']+)|(?<=[^A-Za-z']+)")) {
            String word = catalogue.get(namespace + ".word." + token.toLowerCase(java.util.Locale.ROOT));
            result.append(word == null ? token : word);
        }
        return displaySafe(result.toString());
    }

    /** Replaces typographic punctuation missing from the legacy bitmap font. */
    private static String displaySafe(String value) {
        if (value == null) return null;
        return value.replace('\u2018', '\'')
                .replace('\u2019', '\'')
                .replace('\u201C', '"')
                .replace('\u201D', '"')
                .replace('\u00A0', ' ');
    }
}
