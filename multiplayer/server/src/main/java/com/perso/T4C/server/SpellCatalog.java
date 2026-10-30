package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.spell.BuffAttributes;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Strict allowlist of spells the online server may cast, apply from a consumable, or grant. Item
 * entries are restorative effects plus explicitly supported buff potions routed through v2's
 * persistent buff engine; other unsupported effects are refused at load.
 */
public final class SpellCatalog {
  private static final Set<Integer> CAST_EFFECTS = Set.of(1, 2);
  private static final Set<Integer> ITEM_EFFECTS = Set.of(1, 2);
  private static final Set<String> ITEM_BUFF_KEYS = Set.of(
      "spell.item_potion_of_fury", "spell.item_potion_of_nimbleness");
  private final SpellContent content;

  public SpellCatalog(SpellContent content) {
    this.content = Objects.requireNonNull(content);
    content.spells().values().forEach(SpellCatalog::validate);
  }

  private static void validate(SpellContent.Spell spell) {
    switch (spell.use()) {
      case GRANT_ONLY -> { }
      case CAST -> requirePlain(spell, CAST_EFFECTS);
      case ITEM -> {
        requirePlain(spell, ITEM_EFFECTS);
        if (!isManaRestore(spell) && !isSupportedItemBuff(spell))
          throw new IllegalArgumentException("unsupported_spell");
      }
    }
  }

  private static void requirePlain(SpellContent.Spell spell, Set<Integer> allowedEffects) {
    if (spell.attack() || spell.pvp() || spell.effects().isEmpty()
        || !spell.effects().stream().allMatch(effect -> allowedEffects.contains(effect.type())))
      throw new IllegalArgumentException("unsupported_spell");
  }

  /** A consumable mana effect must be a restore on MANA; any other type-2 effect is a buff the server cannot run. */
  private static boolean isManaRestore(SpellContent.Spell spell) {
    return spell.effects().stream().filter(effect -> effect.type() == 2)
        .allMatch(effect -> effect.params().stream()
            .anyMatch(param -> param.id() == 2 && "MANA".equalsIgnoreCase(param.expression())));
  }

  /** Explicit item buffs routed through v2's authoritative persistent buff application. */
  private static boolean isSupportedItemBuff(SpellContent.Spell spell) {
    return ITEM_BUFF_KEYS.contains(spell.key()) && !spell.effects().isEmpty()
        && spell.effects().stream().allMatch(effect -> effect.type() == 2
            && effect.params().stream().filter(param -> param.id() == 2).count() == 1
            && effect.params().stream().filter(param -> param.id() == 2)
                .map(param -> BuffAttributes.normalize(param.expression()))
                .allMatch(attribute -> BuffAttributes.MODELLED.contains(attribute)));
  }

  /** The named spell, whatever its use. Unknown keys are refused. */
  public SpellContent.Spell require(String key) {
    var spell = content.spells().get(key);
    if (spell == null) throw new IllegalArgumentException("unsupported_spell");
    return spell;
  }

  /** The named spell only when it may be cast online; grant-only spells are refused. */
  public SpellContent.Spell castable(String key) {
    var spell = require(key);
    if (spell.use() != SpellContent.Use.CAST) throw new IllegalArgumentException("unsupported_spell");
    return spell;
  }

  public Map<String, SpellContent.Spell> spells() {
    return content.spells();
  }

  public static SpellCatalog loadBundled() throws IOException {
    try (var input = SpellCatalog.class.getResourceAsStream("/content/spells-v1.json")) {
      if (input == null) throw new IOException("Missing bundled spell content");
      return read(input);
    }
  }

  static SpellCatalog read(InputStream input) throws IOException {
    int limit = 1024 * 1024;
    byte[] bytes = input.readNBytes(limit + 1);
    if (bytes.length > limit) throw new IOException("Spell content exceeds size limit");
    try {
      var content = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), SpellContent.class);
      if (content == null) throw new IllegalArgumentException("Empty content");
      return new SpellCatalog(content);
    } catch (RuntimeException error) {
      throw new IOException("Invalid or unsupported spell content", error);
    }
  }
}
