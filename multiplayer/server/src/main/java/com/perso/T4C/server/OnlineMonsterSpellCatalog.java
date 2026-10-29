package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.MonsterSpellContent;
import com.perso.T4C.online.model.MonsterSpellContent.Behavior;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Bundled allowlist for the exact monster-only spell IDs used by authored attacks. */
final class OnlineMonsterSpellCatalog {
  private static OnlineMonsterSpellCatalog bundled;
  private final MonsterSpellContent content;

  OnlineMonsterSpellCatalog(MonsterSpellContent content) {
    this.content = Objects.requireNonNull(content);
  }

  MonsterSpellContent.Spell damageSpell(int id) {
    var spell = content.spells().get(Integer.toString(id));
    if (spell == null) return null;
    return spell.behavior() == Behavior.ELEMENTAL_DAMAGE || spell.behavior() == Behavior.ARMOR_DAMAGE
        ? spell : null;
  }

  MonsterSpellContent.Spell selfHealSpell(int id) {
    var spell = content.spells().get(Integer.toString(id));
    return id == 10616 && spell != null && spell.behavior() == Behavior.SELF_HEAL ? spell : null;
  }

  static synchronized OnlineMonsterSpellCatalog bundled() {
    if (bundled == null) {
      try {
        bundled = loadBundled();
      } catch (IOException missing) {
        throw new java.io.UncheckedIOException(missing);
      }
    }
    return bundled;
  }

  static OnlineMonsterSpellCatalog loadBundled() throws IOException {
    try (InputStream input = OnlineMonsterSpellCatalog.class.getResourceAsStream("/content/monster-spells-v1.json")) {
      if (input == null) throw new IOException("Missing bundled monster spell content");
      byte[] bytes = input.readNBytes(1024 * 1024 + 1);
      if (bytes.length > 1024 * 1024) throw new IOException("Monster spell content exceeds size limit");
      try {
        var content = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), MonsterSpellContent.class);
        if (content == null) throw new IllegalArgumentException("Empty monster spell content");
        return new OnlineMonsterSpellCatalog(content);
      } catch (RuntimeException invalid) {
        throw new IOException("Invalid monster spell content", invalid);
      }
    }
  }
}
