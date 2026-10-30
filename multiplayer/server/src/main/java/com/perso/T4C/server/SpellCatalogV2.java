package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.SpellContentV2;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * The engine's spell catalog (spells-v2) with a stage gate. A spell is castable only when its
 * authored engine stage is at or below {@link #ENABLED_STAGE}; everything else stays refused as
 * {@code unsupported_spell}, exactly like a key the server has never heard of. Raising the stage is
 * the only change a later engine stage makes here. The live spells-v1 allowlist is not replaced:
 * {@link PersistentSessions#cast} keeps serving the v1 self-cast spells unchanged.
 */
public final class SpellCatalogV2 {
  /** Highest engine stage this server resolves (SPELL_ENGINE_DESIGN section 6). */
  public static final int ENABLED_STAGE = 3;
  private static SpellCatalogV2 bundled;
  private final SpellContentV2 content;
  private final int enabledStage;

  public SpellCatalogV2(SpellContentV2 content) {
    this(content, ENABLED_STAGE);
  }

  public SpellCatalogV2(SpellContentV2 content, int enabledStage) {
    this.content = Objects.requireNonNull(content);
    this.enabledStage = enabledStage;
  }

  /** The named spell when this server may resolve it; any other key or stage is refused. */
  public SpellContentV2.Spell castable(String key) {
    var spell = content.spells().get(key);
    if (spell == null || (spell.stage() > enabledStage && !isRecall(spell))) throw new IllegalArgumentException("unsupported_spell");
    return spell;
  }

  /** True when {@link #castable} would accept the key. */
  public boolean castableKey(String key) {
    var spell = content.spells().get(key);
    return spell != null && (spell.stage() <= enabledStage || isRecall(spell));
  }

  private static boolean isRecall(SpellContentV2.Spell spell) {
    return spell.id() == 10029 && spell.key().equals("spell.word_of_recall");
  }

  public Map<String, SpellContentV2.Spell> spells() {
    return content.spells();
  }

  public long castableCount() {
    return content.spells().values().stream().filter(spell -> spell.stage() <= enabledStage || isRecall(spell)).count();
  }

  public static synchronized SpellCatalogV2 bundled() {
    if (bundled == null) {
      try {
        bundled = loadBundled();
      } catch (IOException unavailable) {
        throw new java.io.UncheckedIOException(unavailable);
      }
    }
    return bundled;
  }

  public static SpellCatalogV2 loadBundled() throws IOException {
    try (var input = SpellCatalogV2.class.getResourceAsStream("/content/spells-v2.json")) {
      if (input == null) throw new IOException("Missing bundled spells-v2 content");
      return read(input);
    }
  }

  static SpellCatalogV2 read(InputStream input) throws IOException {
    int limit = 1024 * 1024;
    byte[] bytes = input.readNBytes(limit + 1);
    if (bytes.length > limit) throw new IOException("Spell content exceeds size limit");
    try {
      var content = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), SpellContentV2.class);
      if (content == null) throw new IllegalArgumentException("Empty content");
      return new SpellCatalogV2(content);
    } catch (RuntimeException error) {
      throw new IOException("Invalid or unsupported spell content", error);
    }
  }
}
