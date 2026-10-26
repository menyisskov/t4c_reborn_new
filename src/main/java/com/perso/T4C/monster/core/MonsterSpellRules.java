package com.perso.T4C.monster.core;

import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;

/** Shared monster spell damage, targeting and elemental rules. */
public final class MonsterSpellRules {
  private static final int ELEMENTAL_DAMAGE_DIVISOR = 4;
  private static final double MAXIMUM_ELEMENTAL_DAMAGE_PER_LEVEL = 0.5;

  private MonsterSpellRules() {}

  public static int maximumElementalDamage(int level) {
    return (int)
        Math.min(
            Integer.MAX_VALUE, Math.max(1, Math.round(level * MAXIMUM_ELEMENTAL_DAMAGE_PER_LEVEL)));
  }

  public static int elementalDamageLimit(int spellId, int level) {
    return elementalDamageLimit(spellId, level, null);
  }

  public static int elementalDamageLimit(int spellId, int level, String casterName) {
    var god = com.perso.T4C.divine.DivinePantheon.findJudgment(spellId);
    if (god != null) return god.judgmentDamageMax();
    if (spellId == 10095 && "Arch Drake".equals(casterName)) return 1800;
    return maximumElementalDamage(level);
  }

  public static boolean usesArmor(int spellId) {
    var spell = SpellRegistry.findById(spellId);
    return spell != null
        && spell.getElement() == 0
        && spell.getAttackType() == SpellData.ATTACK_PHYSICAL;
  }

  public static boolean isElementalDamageSpell(int spellId) {
    return spellId > 0
        && spellId != 10596
        && !healsCaster(spellId)
        && !usesArmor(spellId)
        && !com.perso.T4C.divine.DivineAfflictions.isAffliction(spellId);
  }

  /** Applied once before resistance; divine judgments retain their separate boss budget. */
  public static int balanceRawDamage(int spellId, int level, int rawDamage) {
    return balanceRawDamage(spellId, level, rawDamage, null);
  }

  public static int balanceRawDamage(int spellId, int level, int rawDamage, String casterName) {
    if (!isElementalDamageSpell(spellId)) return rawDamage;
    var god = com.perso.T4C.divine.DivinePantheon.findJudgment(spellId);
    if (god != null) return Math.max(0, Math.min(rawDamage, god.judgmentDamageMax()));
    if (spellId == 10095 && "Arch Drake".equals(casterName))
      return Math.max(0, Math.min(rawDamage, elementalDamageLimit(spellId, level, casterName)));
    long scaled =
        (Math.max(0L, rawDamage) + ELEMENTAL_DAMAGE_DIVISOR - 1) / ELEMENTAL_DAMAGE_DIVISOR;
    return (int) Math.min(scaled, maximumElementalDamage(level));
  }

  public static int damageAfterResistance(int rawDamage, int resistance) {
    return (int)
        Math.min(Integer.MAX_VALUE, Math.max(0L, rawDamage) * 100 / Math.max(1, resistance));
  }

  /** Resolve a landed elemental hit with the same on-hit reactions as physical attacks. */
  public static int applyElementalHit(
      com.perso.T4C.player.Player target, int rawDamage, int resistance, Runnable onHit) {
    if (com.perso.T4C.helper.SanctuaryCombatRules.isProtected(target.getPositionVector())) return 0;
    onHit.run();
    target.takeCombatDamage(damageAfterResistance(rawDamage, resistance));
    return target.getLastDamageTaken();
  }

  /** Legacy monster-only spell IDs borrow visuals, never player spell damage or costs. */
  public static SpellData visualSpell(int spellId) {
    SpellData spell = SpellRegistry.findById(spellId);
    if (spell != null) return spell;
    return switch (spellId) {
      case 10095 -> SpellRegistry.findById(10018); // Fireball
      case 10616 -> SpellRegistry.findById(10033); // Heal Serious
      default -> null;
    };
  }

  public static boolean healsCaster(int spellId) {
    // __SPELL_MOB_ARENA_HEAL_SERIOUS_SPELL
    return spellId == 10616;
  }

  public static String fallbackElement(int spellId) {
    return switch (spellId) {
      case 10095 -> "fire"; // __SPELL_MOB_FIREBALL_SPELL
      case 10120 -> "air";
      default -> "water";
    };
  }
}
