package com.perso.T4C.online.model;

import java.util.Map;
import java.util.Objects;

/** Minimal authored monster-spell facts needed by the online retaliation allowlist. */
public record MonsterSpellContent(int version, Map<String, Spell> spells) {
  public static final int VERSION = 1;

  public enum Behavior {
    ELEMENTAL_DAMAGE,
    ARMOR_DAMAGE,
    SELF_HEAL,
    TELEPORT,
    AFFLICTION,
    UNSUPPORTED
  }

  public MonsterSpellContent {
    if (version != VERSION) throw new IllegalArgumentException("Unsupported monster spell content version");
    spells = Map.copyOf(spells);
    if (spells.isEmpty()) throw new IllegalArgumentException("Empty monster spell content");
    spells.forEach((id, spell) -> {
      if (!id.equals(Integer.toString(spell.id()))) throw new IllegalArgumentException("Monster spell id key mismatch");
    });
  }

  public record Spell(int id, String key, String element, Behavior behavior) {
    public Spell {
      if (id <= 0) throw new IllegalArgumentException("Invalid monster spell id");
      Objects.requireNonNull(behavior);
      if (behavior == Behavior.ELEMENTAL_DAMAGE && (element == null || element.isBlank()))
        throw new IllegalArgumentException("Elemental monster spell needs an element");
    }
  }
}
