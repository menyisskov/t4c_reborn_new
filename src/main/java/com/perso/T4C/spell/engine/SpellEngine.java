package com.perso.T4C.spell.engine;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.helper.RandomSource;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.spell.SpellCastRules;
import com.perso.T4C.spell.SpellFormulaContexts;
import com.perso.T4C.spell.SpellHealRules;
import com.perso.T4C.spell.SpellSplash;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Pure spell resolution for engine stage 1: instant single-target attack spells with their residual
 * splash, drains, and single heals on the caster. It reads snapshots and returns <em>data</em> (costs
 * and an ordered event list); the caller applies the events to its own state. It owns no clock, no
 * persistence and no game types, so the server's domain module compiles it and single-player's code
 * path can be compared with it value for value.
 *
 * <p>Randomness: every draw comes from the injected generator, in the order single-player consumes
 * {@link RandomSource}: (1) the cast success roll inside {@link SpellCastRules}; (2) the primary
 * target's damage formulas, dice left to right, then the min/max fallback only if every formula
 * rolled zero; (3) the caster's drain heal (no draw); (4) each residual-splash target in selection
 * order, again formulas then fallback. The engine scopes the generator over {@link RandomSource}
 * for the duration of the call, so {@link DiceFormula} draws from it.
 *
 * <p>Line of sight is whatever {@link WorldView#lineOfSight} answers. Spells must be given the
 * shared sampled rule (single-player's), never the server's stricter attack ray.
 */
public final class SpellEngine {
  private SpellEngine() {}

  /** Who or what the cast is aimed at. */
  public sealed interface Target {
    record Self() implements Target {}

    record Unit(long id) implements Target {}

    record Position(double x, double y) implements Target {}
  }

  /** A hostile creature as the engine sees it. Positions are tiles; resistances are normalised. */
  public record UnitView(
      long id,
      double x,
      double y,
      int health,
      boolean attackable,
      SpellFormulaContexts.Elements resistance) {
    boolean alive() {
      return health > 0;
    }
  }

  /** What the world answers for one cast. Implementations must be deterministic for a cast. */
  public interface WorldView {
    /** The unit with this id, or null when it does not exist. */
    UnitView unit(long id);

    /** Every candidate for splash, in a stable order (ascending id); ties then resolve by id. */
    List<UnitView> units();

    /** True when neither tile lies in a safe haven (no combat there). */
    boolean combatSafe(double fromX, double fromY, double toX, double toY);

    /** The shared sampled spell sight rule between two tile positions. */
    boolean lineOfSight(double fromX, double fromY, double toX, double toY);
  }

  /** The caster's committed facts. Cooldown and exhaustion are resolved by the caller against its clock. */
  public record CastRequest(
      SpellContentV2.Spell spell,
      SpellFormulaContexts.Caster caster,
      double casterX,
      double casterY,
      Target target,
      boolean known,
      int mana,
      boolean unlimitedResources,
      boolean spellOnCooldown,
      boolean exhausted) {}

  /** How an event damages: the cast's own hit or a quarter-strength splash hit. */
  public enum DamageKind {
    DIRECT,
    RESIDUAL
  }

  /** An outcome the caller applies, in list order. Amounts are rolled, not clamped to remaining health. */
  public sealed interface Event {
    record Damage(long target, int amount, DamageKind kind) implements Event {}

    /** Health the caster regains from a drain: the full rolled damage of the direct hit. */
    record DrainHeal(int amount) implements Event {}

    /** Health a single-target heal restores to the caster (positive only). */
    record SelfHeal(int amount) implements Event {}
  }

  /**
   * {@code verdict} is the shared cast verdict; when it is {@link SpellCastRules.Verdict#NONE}
   * {@code costs} and {@code events} are set. {@code refusal} is non-null only for a structural
   * target refusal decided before any rule or draw ({@code creature_unavailable},
   * {@code protected_target}).
   */
  public record CastResolution(
      SpellCastRules.Verdict verdict,
      String refusal,
      SpellCastRules.Outcome costs,
      List<Event> events) {
    public boolean success() {
      return refusal == null && verdict == SpellCastRules.Verdict.NONE;
    }

    static CastResolution refused(SpellCastRules.Verdict verdict) {
      return new CastResolution(verdict, null, null, List.of());
    }

    static CastResolution refused(String refusal) {
      return new CastResolution(SpellCastRules.Verdict.NONE, refusal, null, List.of());
    }
  }

  public static CastResolution cast(CastRequest request, WorldView world, RandomGenerator random) {
    var spell = request.spell();
    if (spell.stage() > 1 || !isStageOneKind(spell.kind()) || !onlyEffects(spell, spell.kind()))
      throw new IllegalArgumentException("unsupported_spell");
    UnitView primary = null;
    SpellCastRules.TargetKind kind;
    switch (request.target()) {
      case Target.Self ignored -> kind = SpellCastRules.TargetKind.SELF;
      case Target.Position ignored -> kind = SpellCastRules.TargetKind.POSITION;
      case Target.Unit unit -> {
        kind = SpellCastRules.TargetKind.HOSTILE_UNIT;
        primary = world.unit(unit.id());
        if (primary == null || !primary.alive()) return CastResolution.refused("creature_unavailable");
        if (!primary.attackable()) return CastResolution.refused("protected_target");
      }
    }
    float distance = 0f;
    boolean sightClear = true;
    boolean safeFromCombat = false;
    if (primary != null) {
      // Single-player: distance in pixels over the larger tile side, then the spell's own sight flag.
      distance =
          (float)
              (Math.hypot(
                      (primary.x() - request.casterX()) * GameConstants.GRID_W,
                      (primary.y() - request.casterY()) * GameConstants.GRID_H)
                  / Math.max(GameConstants.GRID_W, GameConstants.GRID_H));
      sightClear =
          !spell.lineOfSight()
              || world.lineOfSight(request.casterX(), request.casterY(), primary.x(), primary.y());
      safeFromCombat =
          !world.combatSafe(request.casterX(), request.casterY(), primary.x(), primary.y());
    }
    var ruleSpell =
        new SpellCastRules.Spell(
            spell.targetType(),
            spell.attack(),
            false,
            spell.pvp(),
            spell.lineOfSight(),
            spell.manaCost(),
            spell.successRate(),
            spell.mentalExhaustion(),
            spell.physicalExhaustion(),
            spell.attackExhaustion(),
            spell.cooldownSeconds());
    try (var ignored = RandomSource.use(random)) {
      var input =
          new SpellCastRules.CastInput(
              ruleSpell,
              true,
              request.known(),
              kind,
              false,
              distance,
              sightClear,
              safeFromCombat,
              false,
              request.spellOnCooldown(),
              request.exhausted(),
              request.unlimitedResources(),
              request.mana(),
              SpellFormulaContexts.forCastRules(request.caster()),
              random::nextInt);
      var outcome = SpellCastRules.evaluate(input);
      if (!outcome.success()) return CastResolution.refused(outcome.verdict());
      var events = new ArrayList<Event>();
      if (spell.kind() == SpellContentV2.Kind.HEAL) {
        int heal = healAmount(spell, request.caster(), random);
        if (heal > 0) events.add(new Event.SelfHeal(heal));
      } else {
        impact(request, spell, primary, world, events);
      }
      return new CastResolution(SpellCastRules.Verdict.NONE, null, outcome, List.copyOf(events));
    }
  }

  private static boolean isStageOneKind(SpellContentV2.Kind kind) {
    return kind == SpellContentV2.Kind.NUKE
        || kind == SpellContentV2.Kind.DRAIN
        || kind == SpellContentV2.Kind.HEAL;
  }

  /** Stage 1 spells carry only direct damage/heal (effect 1) or drain (effect 10) effects. */
  private static boolean onlyEffects(SpellContentV2.Spell spell, SpellContentV2.Kind kind) {
    int allowed = kind == SpellContentV2.Kind.DRAIN ? 10 : 1;
    return !spell.effects().isEmpty()
        && spell.effects().stream().allMatch(effect -> effect.type() == allowed);
  }

  /** Mirror of {@code SpellEffectManager.resolvePlayerHealthDelta} for the caster's own heal. */
  private static int healAmount(
      SpellContentV2.Spell spell, SpellFormulaContexts.Caster caster, RandomGenerator random) {
    var formulas = new ArrayList<String>();
    for (var effect : spell.effects()) {
      if (effect.type() != 1) continue;
      String formula = parameter(effect, 1);
      if (formula != null) formulas.add(formula);
    }
    return SpellHealRules.healthDelta(
        formulas, spell.minDamage(), spell.maxDamage(), SpellFormulaContexts.forSelf(caster), random);
  }

  /** Primary hit, drain heal, then the quarter-strength splash, as {@code applySpellImpact} does. */
  private static void impact(
      CastRequest request,
      SpellContentV2.Spell spell,
      UnitView primary,
      WorldView world,
      List<Event> events) {
    if (primary == null) throw new IllegalArgumentException("wrong_target");
    // Splash targets are chosen before any damage: nearest first, the primary holding one slot.
    float cx = (float) (primary.x() * GameConstants.GRID_W);
    float cy = (float) (primary.y() * GameConstants.GRID_H);
    List<UnitView> secondary =
        SpellSplash.nearest(
            world.units(),
            candidate ->
                candidate.id() != primary.id()
                    && candidate.alive()
                    && candidate.attackable()
                    && world.combatSafe(request.casterX(), request.casterY(), candidate.x(), candidate.y())
                    && SpellSplash.isAdjacent(
                        cx,
                        cy,
                        (float) (candidate.x() * GameConstants.GRID_W),
                        (float) (candidate.y() * GameConstants.GRID_H))
                    && world.lineOfSight(primary.x(), primary.y(), candidate.x(), candidate.y()),
            candidate -> {
              double dx = (candidate.x() - primary.x()) * GameConstants.GRID_W;
              double dy = (candidate.y() - primary.y()) * GameConstants.GRID_H;
              return dx * dx + dy * dy;
            },
            1);
    int delta = healthDelta(spell, request.caster(), primary);
    boolean drain = spell.effects().stream().anyMatch(effect -> effect.type() == 10);
    if (delta < 0) events.add(new Event.Damage(primary.id(), -delta, DamageKind.DIRECT));
    // Stage 1 formulas are damage-only. A positive roll would heal the monster offline; refuse to
    // guess here so a content change cannot silently diverge.
    if (delta > 0) throw new IllegalStateException("Stage 1 attack spell rolled a heal: " + spell.key());
    int drained = drain ? Math.max(0, -delta) : 0;
    if (drained > 0) events.add(new Event.DrainHeal(drained));
    for (UnitView target : secondary) {
      int splash = SpellSplash.residualDamage(healthDelta(spell, request.caster(), target));
      if (splash > 0) events.add(new Event.Damage(target.id(), splash, DamageKind.RESIDUAL));
    }
  }

  /** Mirror of {@code SpellEffectManager.resolveHealthDelta} at range 0 (the cast's centre). */
  private static int healthDelta(
      SpellContentV2.Spell spell, SpellFormulaContexts.Caster caster, UnitView target) {
    for (var effect : spell.effects()) {
      if (effect.type() == 1 || effect.type() == 10) {
        int evaluated = evaluate(parameter(effect, 1), caster, target);
        if (evaluated != 0) return evaluated;
      }
    }
    int low = Math.min(spell.minDamage(), spell.maxDamage());
    int high = Math.max(spell.minDamage(), spell.maxDamage());
    return low == high ? low : RandomSource.current().nextInt(low, high + 1);
  }

  private static int evaluate(String formula, SpellFormulaContexts.Caster caster, UnitView target) {
    if (formula == null || formula.isBlank()) return 0;
    DiceFormula.Context context = SpellFormulaContexts.against(caster, target.resistance());
    String withRange = formula.replaceAll("(?<![A-Za-z_.])r(?![A-Za-z_])", Double.toString(0d));
    return DiceFormula.of(withRange).evaluate(context);
  }

  private static String parameter(SpellContent.Effect effect, int id) {
    return effect.params().stream()
        .filter(param -> param.id() == id)
        .map(SpellContent.Param::expression)
        .findFirst()
        .orElse(null);
  }
}
