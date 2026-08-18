package com.perso.T4C.helper;

import com.perso.T4C.mapping.definition.AppearanceDefaultsDefinitions;
import com.perso.T4C.player.BodyPart;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppearanceDefaultsCatalog {
  private static final Logger log = LoggerFactory.getLogger(AppearanceDefaultsCatalog.class);
  public static final String MALE = "MALE";
  public static final String FEMALE = "FEMALE";
  private static Map<String, Map<BodyPart, String>> cachedNakedParts;
  private static Map<String, List<AppearanceDefaultsDefinitions.ConcealmentRule>> cachedRules;
  private static Map<String, EquippedAppearance> cachedEquippedOverrides;

  public record EquippedAppearance(BodyPart bodyPart, String sprite) {}

  private AppearanceDefaultsCatalog() {}

  public static Map<BodyPart, String> nakedParts(String gender) {
    load();
    return cachedNakedParts.getOrDefault(key(gender), Map.of());
  }

  public static boolean hidesHead(String appearance) {
    return rulesFor(BodyPart.HEAD, appearance).stream()
        .anyMatch(rule -> rule.hiddenParts().stream().anyMatch("HEAD"::equalsIgnoreCase));
  }

  public static boolean hidesHair(String appearance) {
    return rulesFor(BodyPart.HEAD, appearance).stream()
        .anyMatch(rule -> rule.hiddenParts().stream().anyMatch("HAIR"::equalsIgnoreCase));
  }

  public static List<AppearanceDefaultsDefinitions.ConcealmentRule> rulesFor(
      BodyPart triggerSlot, String appearance) {
    load();
    if (triggerSlot == null || appearance == null || appearance.isBlank()) return List.of();
    String rawKey = ruleKey(triggerSlot.name(), appearance);
    List<AppearanceDefaultsDefinitions.ConcealmentRule> exact = cachedRules.get(rawKey);
    if (exact != null) return exact;
    return cachedRules.getOrDefault(
        ruleKey(triggerSlot.name(), normalizeAppearance(appearance)), List.of());
  }

  public static EquippedAppearance equippedAppearance(
      String gender, BodyPart slot, String defaultAppearance) {
    load();
    if (slot == null || defaultAppearance == null || defaultAppearance.isBlank()) {
      return new EquippedAppearance(slot, defaultAppearance);
    }
    EquippedAppearance override =
        cachedEquippedOverrides.get(overrideKey(gender, slot.name(), defaultAppearance));
    if (override == null) return new EquippedAppearance(slot, defaultAppearance);
    return new EquippedAppearance(
        override.bodyPart(), preservePalette(defaultAppearance, override.sprite()));
  }

  public static synchronized void invalidate() {
    cachedNakedParts = null;
    cachedRules = null;
    cachedEquippedOverrides = null;
  }

  private static synchronized void load() {
    if (cachedNakedParts != null) {
      return;
    }
    AppearanceDefaultsDefinitions.Defaults defaults = AppearanceDefaultsDefinitions.defaults();
    Map<String, Map<BodyPart, String>> byGender = new LinkedHashMap<>();
    for (AppearanceDefaultsDefinitions.NakedPart part : defaults.nakedParts()) {
      BodyPart bodyPart = bodyPart(part.bodyPart());
      if (bodyPart == null) {
        log.warn("Skipping naked part {}: unknown body part '{}'", part.sprite(), part.bodyPart());
        continue;
      }
      byGender
          .computeIfAbsent(key(part.gender()), ignored -> new EnumMap<>(BodyPart.class))
          .put(bodyPart, part.sprite());
    }
    byGender.replaceAll((ignored, value) -> Collections.unmodifiableMap(value));
    Map<String, List<AppearanceDefaultsDefinitions.ConcealmentRule>> rules = new LinkedHashMap<>();
    for (AppearanceDefaultsDefinitions.ConcealmentRule rule : defaults.concealmentRules()) {
      if (bodyPart(rule.triggerSlot()) == null
          || rule.appearance() == null
          || rule.appearance().isBlank()) continue;
      rules
          .computeIfAbsent(
              ruleKey(rule.triggerSlot(), rule.appearance()), ignored -> new ArrayList<>())
          .add(rule);
    }
    rules.replaceAll((ignored, value) -> List.copyOf(value));
    Map<String, EquippedAppearance> overrides = new LinkedHashMap<>();
    for (AppearanceDefaultsDefinitions.EquippedOverride override : defaults.equippedOverrides()) {
      BodyPart sourceSlot = bodyPart(override.sourceSlot());
      BodyPart targetSlot = bodyPart(override.targetSlot());
      if (sourceSlot == null
          || targetSlot == null
          || override.sourceAppearance().isBlank()
          || override.targetAppearance().isBlank()) continue;
      overrides.put(
          overrideKey(override.gender(), sourceSlot.name(), override.sourceAppearance()),
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
