package com.perso.T4C.online.model;

import java.util.List;
import java.util.Map;

/**
 * Authored data for every player spell the online engine will eventually run (spells-v2). It sits
 * beside {@link SpellContent} (spells-v1), which stays the live allowlist byte for byte: v2 adds the
 * per-spell classification and the numbers the engine reads (radius, element, damage range,
 * duration, frequency, learning requirements, visuals) and says in which engine stage each spell
 * becomes castable. Formulas stay unevaluated text.
 */
public record SpellContentV2(int version, Map<String, Spell> spells) {
  public static final int VERSION = 2;

  /** What a spell does, in the engine's terms (SPELL_ENGINE_DESIGN section 1.2). */
  public enum Kind {
    /** Instant single-target damage with residual splash. */
    NUKE,
    AREA_DAMAGE,
    /** Damage the target and heal the caster. */
    DRAIN,
    HEAL,
    AREA_HEAL,
    /** Timed beneficial modifiers on self or a friendly unit. */
    BUFF,
    /** Hostile modifiers on a monster (resolves to nothing on monsters offline). */
    DEBUFF,
    /** Damage over time through a linked tick spell. */
    DOT,
    /** Damage shield (on-hit hook); no effect offline. */
    SHIELD,
    /** Delayed teleport hooks on self; no effect offline. */
    GATEWAY,
    PORTAL,
    RECALL,
    /** Invisibility and detection flags. */
    DETECTION,
    DISPEL,
    /** Imperative spells with logic outside the effect list. */
    SPECIAL
  }

  public SpellContentV2 {
    if (version != VERSION) throw new IllegalArgumentException("Unsupported spell content version");
    spells = Map.copyOf(spells);
    if (spells.isEmpty()) throw new IllegalArgumentException("Empty spell content");
    spells.forEach((key, spell) -> {
      if (!key.equals(spell.key())) throw new IllegalArgumentException("Spell key mismatch");
    });
  }

  /**
   * {@code id} is the authored T4C spell id; imperative spells such as tame_beast have none (0) and
   * are identified by key only. {@code stage} is the engine stage that first enables the spell (1 to 4); {@code support} is the
   * same as the {@code stage-N} label the server compares with its enabled stage.
   */
  public record Spell(String key, int id, Kind kind, int stage, String support, int targetType,
      boolean attack, boolean selfCenteredAttack, boolean pvp, boolean lineOfSight, String manaCost,
      String successRate, String mentalExhaustion, String physicalExhaustion, String attackExhaustion,
      int cooldownSeconds, int radius, int element, int attackType, int minDamage, int maxDamage,
      String duration, String frequency, Learning learning, Visual visual, List<SpellContent.Effect> effects,
      Map<Integer, String> dispelTargets, boolean helperOnly) {
    public Spell(String key, int id, Kind kind, int stage, String support, int targetType,
        boolean attack, boolean selfCenteredAttack, boolean pvp, boolean lineOfSight, String manaCost,
        String successRate, String mentalExhaustion, String physicalExhaustion, String attackExhaustion,
        int cooldownSeconds, int radius, int element, int attackType, int minDamage, int maxDamage,
        String duration, String frequency, Learning learning, Visual visual, List<SpellContent.Effect> effects,
        Map<Integer, String> dispelTargets) {
      this(key, id, kind, stage, support, targetType, attack, selfCenteredAttack, pvp, lineOfSight, manaCost,
          successRate, mentalExhaustion, physicalExhaustion, attackExhaustion, cooldownSeconds, radius, element,
          attackType, minDamage, maxDamage, duration, frequency, learning, visual, effects, dispelTargets, false);
    }

    public Spell {
      if (key == null || !key.startsWith("spell.") || key.length() <= 6 || key.length() > 256
          || id < 0 || kind == null || stage < 1 || stage > 4 || support == null || targetType < 0
          || cooldownSeconds < 0 || radius < 0 || manaCost == null || mentalExhaustion == null
          || physicalExhaustion == null || attackExhaustion == null || learning == null || visual == null
          || effects == null)
        throw new IllegalArgumentException("Invalid spell definition " + key + " id=" + id
            + " target=" + targetType + " cooldown=" + cooldownSeconds + " radius=" + radius);
      effects = List.copyOf(effects);
      dispelTargets = dispelTargets == null ? Map.of() : Map.copyOf(dispelTargets);
    }
  }

  public record Learning(int minLevel, int minInt, int minWis) {}

  /** Presentation inputs the server needs to report (projectile name for flight time, impact). */
  public record Visual(String projectile, String impact, String sound, String impactSound,
      int effect, int targetEffect) {}
}
