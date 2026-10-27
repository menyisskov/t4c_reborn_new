package com.perso.T4C.spell;

import java.util.regex.Pattern;

/**
 * Normalizes a spell's display identity into its content key. A placeholder such as {@code
 * ${spell.renew_armor}} becomes {@code spell.renew_armor}, with any repeated {@code spell.spell_}
 * prefix collapsed. Anything that is not a placeholder is returned unchanged, so saves that store
 * plain display names keep resolving exactly as before.
 */
public final class SpellKeys {
  private static final Pattern PLACEHOLDER = Pattern.compile("^\\$\\{([^{}]+)}$");
  private static final String DOUBLE_PREFIX = "spell.spell_";

  private SpellKeys() {}

  public static String normalize(String value) {
    if (value == null) return null;
    var matcher = PLACEHOLDER.matcher(value.trim());
    if (!matcher.matches()) return value;
    String key = matcher.group(1);
    while (key.startsWith(DOUBLE_PREFIX)) {
      key = "spell." + key.substring(DOUBLE_PREFIX.length());
    }
    return key;
  }
}
