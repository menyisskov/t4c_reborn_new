package com.perso.T4C.helper;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SpriteNameParser {
  private SpriteNameParser() {}

  private static final ConcurrentHashMap<String, ResolvedSprite> PARSE_CACHE =
      new ConcurrentHashMap<>(4096);

  public static ResolvedSprite parse(String rawName, Map<String, SpriteLoader.Sprite> metaByName) {
    if (rawName == null) return null;
    String trimmed = rawName.trim();
    if (trimmed.isEmpty()) return null;
    if (metaByName != null) {
      ResolvedSprite hit = PARSE_CACHE.get(trimmed);
      if (hit != null) return hit;
    }
    String base = trimmed;
    int openIdx = base.indexOf('[');
    int closeIdx = base.indexOf(']');
    if (openIdx != -1 && closeIdx > openIdx) {
      base = base.substring(0, openIdx).trim();
      trimmed = base;
    }
    boolean mirror = false;
    boolean mirrorSuffix = trimmed.endsWith("M");
    String trimmedNoMirror =
        mirrorSuffix ? trimmed.substring(0, trimmed.length() - 1).trim() : trimmed;
    String trimmedNoInstance = stripTrailingInstance(trimmedNoMirror);
    if (mirrorSuffix) {
      String candidate = base;
      if (candidate.endsWith("M")) {
        candidate = candidate.substring(0, candidate.length() - 1).trim();
      }
      candidate = stripTrailingInstance(candidate);
      if (!candidate.isEmpty()) {
        boolean hasCandidate =
            metaByName == null || metaByName.containsKey(candidate.toLowerCase(Locale.ROOT));
        boolean hasExactName =
            metaByName != null
                && metaByName.containsKey(stripTrailingInstance(base).toLowerCase(Locale.ROOT));
        if (hasCandidate || !hasExactName) {
          base = candidate;
          mirror = true;
        }
      }
    }
    base = stripTrailingInstance(base);
    base = resolveLegacyPaletteAlias(base, metaByName);
    base = resolveLegacySpriteAlias(base, metaByName);
    base = normalizeTmplBase(base);
    ResolvedSprite result = new ResolvedSprite(base, mirror);
    if (metaByName != null) {
      PARSE_CACHE.putIfAbsent(trimmed, result);
    }
    return result;
  }

  private static String normalizeTmplBase(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    if (trimmed.isEmpty()) return trimmed;
    if (!trimmed.regionMatches(true, 0, "tmpl", 0, 4)) {
      return trimmed;
    }
    int idx = 4;
    int start = idx;
    while (idx < trimmed.length() && trimmed.charAt(idx) == ' ') {
      idx++;
    }
    if (idx > start && idx < trimmed.length() && Character.isDigit(trimmed.charAt(idx))) {
      trimmed = "Tmpl" + trimmed.substring(idx);
    }
    return collapseSpaces(trimmed);
  }

  private static String stripTrailingInstance(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    int comma = trimmed.lastIndexOf(',');
    if (comma < 0 || comma == trimmed.length() - 1) {
      return trimmed;
    }
    int at = trimmed.lastIndexOf('@', comma);
    if (at < 0) {
      return trimmed;
    }
    if (isSignedInteger(trimmed, at + 1, comma)
        && isSignedInteger(trimmed, comma + 1, trimmed.length())) {
      return trimmed.substring(0, at).trim();
    }
    return trimmed;
  }

  private static String resolveLegacyPaletteAlias(
      String value, Map<String, SpriteLoader.Sprite> metaByName) {
    if (value == null || metaByName == null || value.isBlank()) {
      return value;
    }
    String trimmed = value.trim();
    if (metaByName.containsKey(trimmed.toLowerCase(Locale.ROOT))) {
      return trimmed;
    }
    int separator = trimmed.lastIndexOf(' ');
    if (separator <= 0 || separator + 3 != trimmed.length()) {
      return trimmed;
    }
    String suffix = trimmed.substring(separator + 1);
    if (!isLegacyPaletteSuffix(suffix)) {
      return trimmed;
    }
    String candidate = trimmed.substring(0, separator).trim();
    return metaByName.containsKey(candidate.toLowerCase(Locale.ROOT)) ? candidate : trimmed;
  }

  private static boolean isLegacyPaletteSuffix(String suffix) {
    return suffix.equalsIgnoreCase("MA")
        || suffix.equalsIgnoreCase("GR")
        || suffix.equalsIgnoreCase("BR")
        || suffix.equalsIgnoreCase("BL");
  }

  private static String resolveLegacySpriteAlias(
      String value, Map<String, SpriteLoader.Sprite> metaByName) {
    if (value == null
        || metaByName == null
        || value.isBlank()
        || metaByName.containsKey(value.toLowerCase(Locale.ROOT))) {
      return value;
    }
    String alias =
        switch (value.toLowerCase(Locale.ROOT)) {
          case "cemetery gates 1" -> "Cemetery Gates /^";
          case "cemetery gates 2" -> "Cemetery Gates /";
          case "cemetery gates 3" -> "Cemetery Gates \\v";
          case "cemetery gates 4" -> "Cemetery Gates X";
          case "cemetery gates 5" -> "Cemetery Gates -";
          case "cemetery gates 6" -> "Cemetery Gates ^";
          case "cemetery gates 7" -> "Cemetery Gates >";
          case "cemetery gates 8" -> "Cemetery Gates v";
          case "cemetery gates 9" -> "Cemetery Gates .|";
          default -> null;
        };
    return alias != null && metaByName.containsKey(alias.toLowerCase(Locale.ROOT)) ? alias : value;
  }

  private static boolean isSignedInteger(String value, int start, int end) {
    if (start >= end) {
      return false;
    }
    if (value.charAt(start) == '-') {
      start++;
    }
    if (start >= end) {
      return false;
    }
    for (int i = start; i < end; i++) {
      if (!Character.isDigit(value.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private static String collapseSpaces(String value) {
    StringBuilder result = null;
    boolean previousSpace = false;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      boolean space = c == ' ';
      if (space && previousSpace) {
        if (result == null) {
          result = new StringBuilder(value.length());
          result.append(value, 0, i);
        }
        continue;
      }
      if (result != null) {
        result.append(c);
      }
      previousSpace = space;
    }
    return result == null ? value : result.toString();
  }
}
