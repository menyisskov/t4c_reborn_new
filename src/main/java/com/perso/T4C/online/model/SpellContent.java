package com.perso.T4C.online.model;

import java.util.List;
import java.util.Map;

/**
 * Authored spell data for the authoritative server. Formulas stay unevaluated text; the server
 * decides which spells it may run, so buffs, summons and teleports fail closed at load.
 */
public record SpellContent(int version, Map<String, Spell> spells) {
  public static final int VERSION = 1;

  /**
   * CAST: castable self-target spell. ITEM: applied only through a consumable. GRANT_ONLY: learned
   * from a quest reward and never cast online.
   */
  public enum Use { CAST, ITEM, GRANT_ONLY }

  public SpellContent {
    if (version != VERSION) throw new IllegalArgumentException("Unsupported spell content version");
    spells = Map.copyOf(spells);
    if (spells.isEmpty()) throw new IllegalArgumentException("Empty spell content");
    spells.forEach((key, spell) -> {
      if (!key.equals(spell.key())) throw new IllegalArgumentException("Spell key mismatch");
    });
  }

  public record Spell(String key, int id, Use use, int targetType, boolean attack,
      boolean selfCenteredAttack, boolean pvp, boolean lineOfSight, String manaCost,
      String successRate, String mentalExhaustion, String physicalExhaustion,
      String attackExhaustion, int cooldownSeconds, List<Effect> effects) {
    public Spell {
      if (key == null || !key.startsWith("spell.") || key.length() <= 6 || key.length() > 256
          || id <= 0 || use == null || targetType < 0 || cooldownSeconds < 0
          || manaCost == null || mentalExhaustion == null || physicalExhaustion == null
          || attackExhaustion == null || effects == null)
        throw new IllegalArgumentException("Invalid spell definition");
      effects = List.copyOf(effects);
    }
  }

  public record Effect(int type, List<Param> params) {
    public Effect {
      params = List.copyOf(params);
    }
  }

  public record Param(int id, String expression) {}
}
