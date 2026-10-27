package com.perso.T4C.spell;

import com.perso.T4C.helper.DiceFormula;
import java.util.function.IntUnaryOperator;

/**
 * Pure cast validation and cost rules. The caller resolves every world, time and caster fact into
 * a {@link CastInput} snapshot, and applies the returned {@link Outcome} itself. Checks run in the
 * same order as offline casting, and the success roll is drawn only when the success rate is above
 * zero, so the random sequence matches the original code exactly.
 */
public final class SpellCastRules {
  public static final int MAXIMUM_CAST_RANGE_TILES = 20;

  /** Mirrors {@code SpellCastingService.TargetKind}; names must stay identical. */
  public enum TargetKind {
    SELF,
    HOSTILE_UNIT,
    FRIENDLY_UNIT,
    POSITION
  }

  /** Mirrors {@code SpellCastingService.Failure}; names must stay identical. */
  public enum Verdict {
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

  /** The spell fields a cast reads, with formulas still unevaluated. */
  public record Spell(
      int targetType,
      boolean attack,
      boolean selfCenteredAttack,
      boolean pvp,
      boolean lineOfSight,
      String manaCost,
      String successRate,
      String mentalExhaustion,
      String physicalExhaustion,
      String attackExhaustion,
      int cooldownSeconds) {}

  /**
   * Everything the caster and world contribute. {@code spellOnCooldown} and {@code exhausted} are
   * resolved by the caller (exhausted covers mental exhaustion or stun), so this rule never reads a
   * clock. {@code roll} draws a uniform integer in {@code [0, bound)}.
   */
  public record CastInput(
      Spell spell,
      boolean knownRequired,
      boolean known,
      TargetKind target,
      boolean pvpTarget,
      float distanceTiles,
      boolean lineOfSightClear,
      boolean safeFromCombat,
      boolean safeFromPvp,
      boolean spellOnCooldown,
      boolean exhausted,
      boolean unlimitedResources,
      int mana,
      DiceFormula.Context context,
      IntUnaryOperator roll) {}

  /** Verdict plus, on success, what the caller must spend and start. */
  public record Outcome(
      Verdict verdict,
      int manaCost,
      long mentalMillis,
      long physicalMillis,
      long attackMillis,
      int cooldownSeconds) {
    public boolean success() {
      return verdict == Verdict.NONE;
    }

    private static Outcome fail(Verdict verdict) {
      return new Outcome(verdict, 0, 0L, 0L, 0L, 0);
    }
  }

  private SpellCastRules() {}

  /** An expiry is still running while it lies strictly after {@code now}; {@code now == until} is ready. */
  public static boolean isActive(long until, long now) {
    return until > now;
  }

  public static Outcome evaluate(CastInput input) {
    Spell spell = input.spell();
    if ((input.target() == TargetKind.HOSTILE_UNIT
            || (input.target() == TargetKind.POSITION && spell.attack())
            || (input.target() == TargetKind.SELF && spell.selfCenteredAttack()))
        && input.safeFromCombat()) {
      return Outcome.fail(Verdict.SAFE_HAVEN);
    }
    if (input.pvpTarget() && input.safeFromPvp()) return Outcome.fail(Verdict.SAFE_HAVEN);
    if (input.knownRequired() && !input.known()) return Outcome.fail(Verdict.NOT_LEARNED);
    if (!targetAccepted(spell.targetType(), input.target())) {
      return Outcome.fail(Verdict.WRONG_TARGET);
    }
    if (input.pvpTarget() && !spell.pvp()) return Outcome.fail(Verdict.PVP_FORBIDDEN);
    if (input.target() != TargetKind.SELF && input.distanceTiles() > MAXIMUM_CAST_RANGE_TILES) {
      return Outcome.fail(Verdict.OUT_OF_RANGE);
    }
    if (spell.lineOfSight()
        && input.target() != TargetKind.SELF
        && !input.lineOfSightClear()) {
      return Outcome.fail(Verdict.NO_LINE_OF_SIGHT);
    }
    if (input.spellOnCooldown()) return Outcome.fail(Verdict.COOLDOWN);
    if (input.exhausted()) return Outcome.fail(Verdict.EXHAUSTED);
    DiceFormula.Context context = input.context();
    int manaCost =
        input.unlimitedResources()
            ? 0
            : Math.max(0, DiceFormula.of(spell.manaCost()).evaluate(context));
    if (input.mana() < manaCost) return Outcome.fail(Verdict.NOT_ENOUGH_MANA);
    int successRate =
        spell.successRate() == null
            ? 100
            : Math.max(0, Math.min(100, DiceFormula.of(spell.successRate()).evaluate(context)));
    if (successRate == 0 || input.roll().applyAsInt(101) > successRate) {
      return Outcome.fail(Verdict.ACTIVATION_FAILED);
    }
    return new Outcome(
        Verdict.NONE,
        manaCost,
        evaluateMillis(spell.mentalExhaustion(), context),
        evaluateMillis(spell.physicalExhaustion(), context),
        evaluateMillis(spell.attackExhaustion(), context),
        spell.cooldownSeconds());
  }

  static boolean targetAccepted(int type, TargetKind kind) {
    if (type <= 0) return true;
    return switch (kind) {
      case SELF ->
          type == 0
              || type == 1
              || type == 3
              || type == 4
              || type == 5
              || type == 12
              || type == 13
              || type == 14
              || type == 15
              || type == 18;
      case HOSTILE_UNIT ->
          type == 0
              || type == 2
              || type == 4
              || type == 8
              || type == 9
              || type == 10
              || type == 11
              || type == 17
              || type == 19;
      case FRIENDLY_UNIT ->
          type == 0
              || type == 3
              || type == 4
              || type == 10
              || type == 12
              || type == 13
              || type == 14
              || type == 17;
      case POSITION -> type == 6 || type == 16 || type == 19;
    };
  }

  private static long evaluateMillis(String formula, DiceFormula.Context context) {
    return Math.max(0L, DiceFormula.of(formula).evaluate(context));
  }
}
