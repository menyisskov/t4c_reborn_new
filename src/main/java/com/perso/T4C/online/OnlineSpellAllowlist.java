package com.perso.T4C.online;

import com.google.gson.Gson;
import com.perso.T4C.online.model.SpellContent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The online spell allowlist as a client resource. It is a byte-for-byte copy of the server's
 * {@code content/spells-v1.json}; {@code OnlineSpellAllowlistFreshnessTest} fails when the two drift,
 * so the control never offers a spell the server refuses and loading never touches the item registry.
 */
final class OnlineSpellAllowlist {
  static final String RESOURCE = "/online/spells-v1.json";
  private static final int LIMIT = 1024 * 1024;
  private static SpellContent cached;

  private OnlineSpellAllowlist() {}

  /** Returns null when the resource is missing or invalid; the controls then simply list nothing. */
  static synchronized SpellContent bundled() {
    if (cached == null) {
      try (var input = OnlineSpellAllowlist.class.getResourceAsStream(RESOURCE)) {
        if (input == null) return null;
        byte[] bytes = input.readNBytes(LIMIT + 1);
        if (bytes.length > LIMIT) return null;
        cached = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), SpellContent.class);
      } catch (IOException | RuntimeException failure) {
        return null;
      }
    }
    return cached;
  }
}
