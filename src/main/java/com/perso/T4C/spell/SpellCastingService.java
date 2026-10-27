package com.perso.T4C.spell;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.helper.SanctuaryCombatRules;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class SpellCastingService {
  public static final int MAXIMUM_CAST_RANGE_TILES = SpellCastRules.MAXIMUM_CAST_RANGE_TILES;

  public enum TargetKind {
    SELF,
    HOSTILE_UNIT,
    FRIENDLY_UNIT,
    POSITION
  }

  public enum Failure {
    NONE,
    NOT_LEARNED,
    WRONG_TARGET,
    PVP_FORBIDDEN,
    SAFE_HAVEN,
    OUT_OF_RANGE,
    NO_LINE_OF_SIGHT,
    COOLDOWN,
    EXHAUSTED,
    NOT_ENOUGH_MANA,
    ACTIVATION_FAILED
  }

  public record Request(
      SpellData spell,
      Player caster,
      TargetKind targetKind,
      float distanceInTiles,
      boolean lineOfSightClear,
      boolean pvpTarget,
      boolean knownSpellRequired) {}

  public record Result(boolean success, Failure failure, int manaSpent) {
    private static Result failure(Failure failure) {
      return new Result(false, failure, 0);
    }

    private static Result success(int mana) {
      return new Result(true, Failure.NONE, mana);
    }
  }

  private SpellCastingService() {}

  public static Result begin(Request request) {
    return begin(request, true);
  }

  public static Result beginItemUse(Request request) {
    return begin(request, false);
  }

  private static Result begin(Request request, boolean interruptActions) {
    if (request == null || request.spell() == null || request.caster() == null) {
      return Result.failure(Failure.ACTIVATION_FAILED);
    }
    SpellData spell = request.spell();
    Player caster = request.caster();
    var position = caster.getPositionVector();
    var input =
        new SpellCastRules.CastInput(
            new SpellCastRules.Spell(
                spell.getTargetType(),
                spell.isAttack(),
                SpellAreaTargeting.isSelfCenteredAttack(spell),
                spell.isPvp(),
                spell.isLineOfSight(),
                spell.getManaCost(),
                spell.getSuccessRate(),
                spell.getMentalExhaustion(),
                spell.getPhysicalExhaustion(),
                spell.getAttackExhaustion(),
                spell.getCooldownSeconds()),
            request.knownSpellRequired(),
            hasLearnedSpell(caster, spell),
            request.targetKind() == null
                ? null
                : SpellCastRules.TargetKind.valueOf(request.targetKind().name()),
            request.pvpTarget(),
            request.distanceInTiles(),
            request.lineOfSightClear(),
            SanctuaryCombatRules.isProtected(position),
            SanctuaryCombatRules.isPvpProtected(position),
            caster.isSpellOnCooldown(spell.getName()),
            caster.isMentallyExhausted() || caster.isStunned(),
            caster.hasUnlimitedResources(),
            caster.getMana(),
            context(caster),
            ThreadLocalRandom.current()::nextInt);
    var outcome = SpellCastRules.evaluate(input);
    if (outcome.verdict() == SpellCastRules.Verdict.ACTIVATION_FAILED) {
      if (interruptActions) caster.interruptActions();
      return Result.failure(Failure.ACTIVATION_FAILED);
    }
    if (!outcome.success()) return Result.failure(Failure.valueOf(outcome.verdict().name()));
    if (interruptActions) caster.disturb();
    caster.setMana(caster.getMana() - outcome.manaCost());
    caster.applyExhaustion(
        outcome.mentalMillis(), outcome.physicalMillis(), outcome.attackMillis());
    caster.triggerSpellCooldown(spell.getName(), outcome.cooldownSeconds());
    return Result.success(outcome.manaCost());
  }

  public static boolean hasLearnedSpell(Player player, SpellData spell) {
    if (player.getSpells() == null) return false;
    for (String learned : player.getSpells()) {
      if (learned != null
          && (learned.equals(spell.getKey())
              || (SpellRegistry.findByName(learned) != null
                  && SpellRegistry.findByName(learned).getKey().equals(spell.getKey())))) {
        return true;
      }
    }
    return false;
  }

  public static String message(Failure failure) {
    return switch (failure) {
      case NOT_LEARNED -> I18n.message("message.spell_not_learned");
      case WRONG_TARGET -> I18n.message("message.spell_wrong_target");
      case SAFE_HAVEN -> I18n.message("message.combat_safe_haven");
      case PVP_FORBIDDEN -> I18n.message("message.spell_pvp_forbidden");
      case OUT_OF_RANGE -> I18n.message("message.target_too_far");
      case NO_LINE_OF_SIGHT -> I18n.message("message.target_no_line_of_sight");
      case COOLDOWN -> I18n.message("message.spell_cooldown");
      case EXHAUSTED -> I18n.message("message.spell_exhausted");
      case NOT_ENOUGH_MANA -> I18n.message("message.not_enough_mana");
      case ACTIVATION_FAILED -> I18n.message("message.spell_failed");
      case NONE -> "";
    };
  }

  static DiceFormula.Context context(Player caster) {
    return new DiceFormula.Context(
            caster.getEffectiveStrength(),
            caster.getEffectiveEndurance(),
            caster.getEffectiveDexterity(),
            caster.getEffectiveIntelligence(),
            0,
            caster.getEffectiveWisdom(),
            0,
            caster.getLevel())
        .withSelfTrue(selfTrue(caster))
        .withEquippedItems(equippedItems(caster));
  }

  static Map<Integer, Integer> equippedItems(Player caster) {
    Map<Integer, Integer> counts = new LinkedHashMap<>();
    for (String key : caster.getEquippedItems().values()) {
      var item = com.perso.T4C.item.ItemRegistry.findByKey(key);
      if (item != null && item.getNumId() > 0) counts.merge(item.getNumId(), 1, Integer::sum);
    }
    return counts;
  }

  /** The caster's own true readings, for the {@code self.true_*} formula variables. */
  static Map<String, Integer> selfTrue(Player caster) {
    Map<String, Integer> readings = new LinkedHashMap<>();
    readings.put("str", caster.getTrueStrength());
    readings.put("end", caster.getTrueEndurance());
    readings.put("agi", caster.getTrueDexterity());
    readings.put("int", caster.getTrueIntelligence());
    readings.put("wis", caster.getTrueWisdom());
    readings.put("level", caster.getLevel());
    readings.put("attack", caster.getTrueSkillLevel("attack"));
    readings.put("archery", caster.getTrueSkillLevel("archery"));
    readings.put("dodge", caster.getTrueSkillLevel("dodge"));
    for (String element : List.of("fire", "earth", "air", "water", "light", "dark")) {
      readings.put(element, caster.getTrueElementPower(element));
      readings.put("r_" + element, caster.getTrueElementResistance(element));
    }
    return readings;
  }

}
