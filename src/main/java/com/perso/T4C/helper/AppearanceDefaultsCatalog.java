package com.perso.T4C.helper;

import com.perso.T4C.config.Paths;
import com.perso.T4C.player.BodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Naked body-part sprites and concealment rules, loaded from
 * {@link Paths#APPEARANCE_DEFAULTS_BIN}.
 *
 * <p>Definitions are cached after the first read; call {@link #invalidate()} after rewriting the
 * asset (T4C Content Studio does this on save).
 */
public final class AppearanceDefaultsCatalog {
    private static final Logger log = LoggerFactory.getLogger(AppearanceDefaultsCatalog.class);

    /** Gender discriminator used by the {@code gender} column of the asset. */
    public static final String MALE = "MALE";
    public static final String FEMALE = "FEMALE";

    private static Map<String, Map<BodyPart, String>> cachedNakedParts;
    private static Map<String, List<AppearanceDefaultsBinaryIO.ConcealmentRule>> cachedRules;
    private static Map<String, EquippedAppearance> cachedEquippedOverrides;

    public record EquippedAppearance(BodyPart bodyPart, String sprite) {
    }

    private AppearanceDefaultsCatalog() {
    }

    /**
     * Naked fallback sprites for the given gender, or an empty map when the asset does not
     * describe it. Keys are the body parts the puppet renders when nothing is equipped.
     */
    public static Map<BodyPart, String> nakedParts(String gender) {
        load();
        return cachedNakedParts.getOrDefault(key(gender), Map.of());
    }

    /** True when the given head appearance replaces the naked head sprite. */
    public static boolean hidesHead(String appearance) {
        return rulesFor(BodyPart.HEAD, appearance).stream()
                .anyMatch(rule -> rule.hiddenParts().stream().anyMatch("HEAD"::equalsIgnoreCase));
    }

    /** True when the given head appearance covers the hair underneath it. */
    public static boolean hidesHair(String appearance) {
        return rulesFor(BodyPart.HEAD, appearance).stream()
                .anyMatch(rule -> rule.hiddenParts().stream().anyMatch("HAIR"::equalsIgnoreCase));
    }

    public static List<AppearanceDefaultsBinaryIO.ConcealmentRule> rulesFor(
            BodyPart triggerSlot, String appearance) {
        load();
        if (triggerSlot == null || appearance == null || appearance.isBlank()) return List.of();
        String rawKey = ruleKey(triggerSlot.name(), appearance);
        List<AppearanceDefaultsBinaryIO.ConcealmentRule> exact = cachedRules.get(rawKey);
        if (exact != null) return exact;
        return cachedRules.getOrDefault(ruleKey(triggerSlot.name(), normalizeAppearance(appearance)), List.of());
    }

    public static EquippedAppearance equippedAppearance(
            String gender, BodyPart slot, String defaultAppearance) {
        load();
        if (slot == null || defaultAppearance == null || defaultAppearance.isBlank()) {
            return new EquippedAppearance(slot, defaultAppearance);
        }
        EquippedAppearance override = cachedEquippedOverrides.get(
                overrideKey(gender, slot.name(), defaultAppearance));
        if (override == null) return new EquippedAppearance(slot, defaultAppearance);
        return new EquippedAppearance(override.bodyPart(),
                preservePalette(defaultAppearance, override.sprite()));
    }

    /** Reloads the tables from disk on the next lookup. */
    public static synchronized void invalidate() {
        cachedNakedParts = null;
        cachedRules = null;
        cachedEquippedOverrides = null;
    }

    private static synchronized void load() {
        if (cachedNakedParts != null) {
            return;
        }
        AppearanceDefaultsBinaryIO.Defaults defaults =
                new AppearanceDefaultsBinaryIO.Defaults(java.util.List.of(), java.util.List.of());
        File file = new File(Paths.APPEARANCE_DEFAULTS_BIN);
        if (file.exists()) {
            try {
                defaults = AppearanceDefaultsBinaryIO.read(file);
            } catch (Exception e) {
                log.warn("Could not load appearance defaults from {}: {}", file.getPath(), e.getMessage());
            }
        } else {
            log.warn("Missing appearance defaults catalog: {}", file.getPath());
        }

        Map<String, Map<BodyPart, String>> byGender = new LinkedHashMap<>();
        for (AppearanceDefaultsBinaryIO.NakedPart part : defaults.nakedParts()) {
            BodyPart bodyPart = bodyPart(part.bodyPart());
            if (bodyPart == null) {
                // The table is editable from Content Studio, so an unknown slot must not take the
                // whole puppet down: drop just that row and keep the rest usable.
                log.warn("Skipping naked part {}: unknown body part '{}'", part.sprite(), part.bodyPart());
                continue;
            }
            byGender.computeIfAbsent(key(part.gender()), ignored -> new EnumMap<>(BodyPart.class))
                    .put(bodyPart, part.sprite());
        }
        byGender.replaceAll((ignored, value) -> Collections.unmodifiableMap(value));

        Map<String, List<AppearanceDefaultsBinaryIO.ConcealmentRule>> rules = new LinkedHashMap<>();
        for (AppearanceDefaultsBinaryIO.ConcealmentRule rule : defaults.concealmentRules()) {
            if (bodyPart(rule.triggerSlot()) == null || rule.appearance() == null
                    || rule.appearance().isBlank()) continue;
            rules.computeIfAbsent(ruleKey(rule.triggerSlot(), rule.appearance()),
                    ignored -> new ArrayList<>()).add(rule);
        }
        rules.replaceAll((ignored, value) -> List.copyOf(value));
        Map<String, EquippedAppearance> overrides = new LinkedHashMap<>();
        for (AppearanceDefaultsBinaryIO.EquippedOverride override : defaults.equippedOverrides()) {
            BodyPart sourceSlot = bodyPart(override.sourceSlot());
            BodyPart targetSlot = bodyPart(override.targetSlot());
            if (sourceSlot == null || targetSlot == null || override.sourceAppearance().isBlank()
                    || override.targetAppearance().isBlank()) continue;
            overrides.put(overrideKey(override.gender(), sourceSlot.name(), override.sourceAppearance()),
                    new EquippedAppearance(targetSlot, override.targetAppearance()));
        }
        cachedRules = Collections.unmodifiableMap(rules);
        cachedNakedParts = Collections.unmodifiableMap(byGender);
        cachedEquippedOverrides = Collections.unmodifiableMap(overrides);
    }

    private static BodyPart bodyPart(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return BodyPart.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String key(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeAppearance(String sprite) {
        String value = sprite == null ? "" : sprite.trim();
        int palette = value.indexOf("__");
        return palette >= 0 ? value.substring(0, palette) : value;
    }

    private static String ruleKey(String triggerSlot, String appearance) {
        return key(triggerSlot) + "|" + key(appearance);
    }

    private static String preservePalette(String source, String target) {
        if (source == null || target == null || target.contains("__")) return target;
        int palette = source.indexOf("__");
        return palette >= 0 ? target + source.substring(palette) : target;
    }

    private static String overrideKey(String gender, String slot, String appearance) {
        return key(gender) + "|" + key(slot) + "|" + key(normalizeAppearance(appearance));
    }
}
