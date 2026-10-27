package com.perso.T4C.item;

import java.util.Locale;

/** Pure normalization of item keys, shared by offline registries and the server. */
public final class ItemKeys {
  private ItemKeys() {}

  /** Returns the canonical {@code item.<name>} form; values that are null or blank pass through. */
  public static String normalize(String value) {
    if (value == null || value.isBlank()) return value;
    String raw = value.startsWith("item.") ? value.substring(5) : value;
    String normalized =
        raw.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9_]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "");
    return "item." + normalized;
  }
}
