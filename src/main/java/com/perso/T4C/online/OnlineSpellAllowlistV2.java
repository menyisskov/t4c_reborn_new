package com.perso.T4C.online;

import com.google.gson.Gson;
import com.perso.T4C.online.model.SpellContentV2;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Client copy of the server's spells-v2 engine catalog. */
final class OnlineSpellAllowlistV2 {
  static final String RESOURCE = "/online/spells-v2.json";
  /** Mirrors {@code SpellCatalogV2.ENABLED_STAGE}; the server currently enables stages 1 to 3. */
  static final int ENABLED_STAGE = 3;
  private static final int LIMIT = 1024 * 1024;
  private static SpellContentV2 cached;

  private OnlineSpellAllowlistV2() {}

  /** Returns null for a missing or invalid resource, leaving the native menu empty. */
  static synchronized SpellContentV2 bundled() {
    if (cached == null) {
      try (var input = OnlineSpellAllowlistV2.class.getResourceAsStream(RESOURCE)) {
        if (input == null) return null;
        byte[] bytes = input.readNBytes(LIMIT + 1);
        if (bytes.length > LIMIT) return null;
        cached = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), SpellContentV2.class);
      } catch (IOException | RuntimeException failure) {
        return null;
      }
    }
    return cached;
  }
}
