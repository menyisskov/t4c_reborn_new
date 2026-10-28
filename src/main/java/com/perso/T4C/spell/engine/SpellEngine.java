package com.perso.T4C.spell.engine;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.helper.RandomSource;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.spell.BuffAttributes;
import com.perso.T4C.spell.SpellCastRules;
import com.perso.T4C.spell.SpellFormulaContexts;
import com.perso.T4C.spell.SpellHealRules;
import com.perso.T4C.spell.SpellSplash;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * Pure spell resolution for engine stages 1 and 2: instant single-target attack spells with their
 * residual splash, drains, single heals on the caster (stage 1), and timed buffs on the caster or a
 * friendly unit, the inert curse, invisibility and detection flags, dispels and Renew Armor
 * (stage 2). It reads snapshots and returns <em>data</em> (costs and an ordered event list); the
 * caller applies the events to its own state. It owns no clock, no persistence and no game types,
 * so the server's domain module compiles it and single-player's code path can be compared with it
 * value for value.
 *
 * <p>Randomness: every draw comes from the injected generator, in the order single-player consumes
 * {@link RandomSource}: (1) the cast success roll inside {@link SpellCastRules}; (2) for an attack,
 * the primary target's damage formulas, dice left to right, then the min/max fallback only if every
 * formula rolled zero; (3) the caster's drain heal (no draw); (4) each residual-splash target in
 * selection order. For a buff: the heal roll (a no-op for buffs), then the duration and the effect
 * amounts in authored order, then the duration again; for flags and dispels one chance roll per
 * effect, in authored order. The engine scopes the generator over {@link RandomSource} for the
 * duration of the call, so {@link DiceFormula} draws from it.
 *
 * <p>Line of sight is whatever {@link WorldView#lineOfSight} answers. Spells must be given the
 * shared sampled rule (single-player's), never the server's stricter attack ray.
 */
public final class SpellEngine {
  /** The highest engine stage this class can resolve. */
  public static final int MAX_STAGE = 3;

  /** Renew Armor re-lays these, in this order, when the caster knows them (single-player's list). */
  public static final List<String> RENEW_ARMOR_COMPONENTS =
      List.of(
          "spell.barrier",
          "spell.ultra_barrier",
          "spell.protection",
          "spell.ultra_protection",
          "spell.stone_skin",
          "spell.ultra_stone_skin",
          "spell.mana_shield",
          "spell.ultra_mana_shield",
          "spell.mana_surge",
          "spell.ultra_mana_surge");

  private static final String RENEW_ARMOR = "spell.renew_armor";

  private SpellEngine() {}

  /** Who or what the cast is aimed at. */
  public sealed interface Target {
    record Self() implements Target {}

    record Unit(long id) implements Target {}

    record Position(double x, double y) implements Target {}

    /** Another player the caster is allowed to help (the shared rules decide which spells may). */
    record Friend(long id) implements Target {}
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

  /** A friendly player as the engine sees it: where it stands and its own spell numbers. */
  public record FriendView(
      long id, double x, double y, boolean alive, SpellFormulaContexts.Caster caster) {}

  /** What the world answers for one cast. Implementations must be deterministic for a cast. */
  public interface WorldView {
    /** The unit with this id, or null when it does not exist. */
    UnitView unit(long id);

    /** Every candidate for splash, in a stable order (ascending id); ties then resolve by id. */
    List<UnitView> units();

    /**
     * Every candidate that can lie within the box around a tile centre ({@code tilesX} tiles either
     * side in x, {@code tilesY} in y), in ascending id. The default is every unit; a server returns
     * just the creatures it needs to read, so an area spell never loads the whole region.
     */
    default List<UnitView> unitsNear(double centerX, double centerY, double tilesX, double tilesY) {
      return units();
    }

    /** True when neither tile lies in a safe haven (no combat there). */
    boolean combatSafe(double fromX, double fromY, double toX, double toY);

    /** The shared sampled spell sight rule between two tile positions. */
    boolean lineOfSight(double fromX, double fromY, double toX, double toY);

    /** The friendly player with this id, or null when it does not exist or may not be helped. */
    default FriendView friend(long id) {
      return null;
    }
  }

  /**
   * The caster's committed facts. Cooldown and exhaustion are resolved by the caller against its
   * clock. {@code related} holds the other spells a cast may need (the learned Renew Armor
   * components, by key) and {@code activeModifiers} the buff totals currently running on the caster by
   * spell key (used only so Renew Armor can refresh them one after another, as single-player does).
   */
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
      boolean exhausted,
      Map<String, SpellContentV2.Spell> related,
      Map<String, Map<String, Integer>> activeModifiers) {
    public CastRequest {
      related = related == null ? Map.of() : Map.copyOf(related);
      activeModifiers = activeModifiers == null ? Map.of() : Map.copyOf(activeModifiers);
    }

    public CastRequest(
        SpellContentV2.Spell spell,
        SpellFormulaContexts.Caster caster,
        double casterX,
        double casterY,
        Target target,
        boolean known,
        int mana,
        boolean unlimitedResources,
        boolean spellOnCooldown,
        boolean exhausted) {
      this(spell, caster, casterX, casterY, target, known, mana, unlimitedResources,
          spellOnCooldown, exhausted, Map.of(), Map.of());
    }
  }

  /** How an event damages: the cast's own hit or a quarter-strength splash hit. */
  public enum DamageKind {
    DIRECT,
    RESIDUAL,
    /** A hit of an area spell on a creature other than the one aimed at. */
    AREA
  }

  /** One resolved buff amount, keyed by the normalised attribute ({@link BuffAttributes}). */
  public record Modifier(String attribute, int amount) {}

  /** An outcome the caller applies, in list order. Amounts are rolled, not clamped to remaining health. */
  public sealed interface Event {
    record Damage(long target, int amount, DamageKind kind) implements Event {}

    /** Health the caster regains from a drain: the full rolled damage of the direct hit. */
    record DrainHeal(int amount) implements Event {}

    /** Health a single-target heal restores to the caster (positive only). */
    record SelfHeal(int amount) implements Event {}

    /**
     * A buff to lay on the caster ({@code recipient} null) or on the friend with that id.
     * {@code durationSeconds} 0 means it never expires. Laying the same spell again refreshes it.
     */
    record ApplyBuff(Long recipient, String spellKey, int durationSeconds, List<Modifier> modifiers)
        implements Event {
      public ApplyBuff {
        modifiers = List.copyOf(modifiers);
      }
    }

    /** The caster turns invisible for this long. */
    record Hide(long durationMillis) implements Event {}

    /** The caster can see invisible units for this long. */
    record DetectInvisible(long durationMillis) implements Event {}

    /** The caster can see hidden units for this long. */
    record DetectHidden(long durationMillis) implements Event {}

    /** A dispel roll succeeded: remove the caster's buff of this spell if one is running. */
    record Dispel(String spellKey) implements Event {}
  }

  /**
   * {@code verdict} is the shared cast verdict; when it is {@link SpellCastRules.Verdict#NONE}
   * {@code costs} and {@code events} are set. {@code refusal} is non-null only for a structural
   * target refusal decided before any rule or draw ({@code creature_unavailable},
   * {@code protected_target}, {@code friend_unavailable}).
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
    if (spell.stage() > MAX_STAGE || !supported(spell))
      throw new IllegalArgumentException("unsupported_spell");
    UnitView primary = null;
    FriendView friend = null;
    SpellCastRules.TargetKind kind;
    Target.Position position = null;
    switch (request.target()) {
      case Target.Self ignored -> kind = SpellCastRules.TargetKind.SELF;
      case Target.Position aimed -> {
        kind = SpellCastRules.TargetKind.POSITION;
        position = aimed;
      }
      case Target.Unit unit -> {
        kind = SpellCastRules.TargetKind.HOSTILE_UNIT;
        primary = world.unit(unit.id());
        if (primary == null || !primary.alive()) return CastResolution.refused("creature_unavailable");
        if (!primary.attackable()) return CastResolution.refused("protected_target");
      }
      case Target.Friend other -> {
        kind = SpellCastRules.TargetKind.FRIENDLY_UNIT;
        friend = world.friend(other.id());
        if (friend == null || !friend.alive()) return CastResolution.refused("friend_unavailable");
      }
    }
    // Only a timed buff is ever laid on someone else; flags, dispels and heals always affect the caster.
    if (friend != null && spell.kind() != SpellContentV2.Kind.BUFF)
      return CastResolution.refused(SpellCastRules.Verdict.WRONG_TARGET);
    float distance = 0f;
    boolean sightClear = true;
    boolean safeFromCombat = false;
    if (primary != null) {
      // Single-player: distance in pixels over the larger tile side, then the spell's own sight flag.
      distance = distance(primary.x(), primary.y(), request);
      sightClear =
          !spell.lineOfSight()
              || world.lineOfSight(request.casterX(), request.casterY(), primary.x(), primary.y());
      safeFromCombat =
          !world.combatSafe(request.casterX(), request.casterY(), primary.x(), primary.y());
    } else if (position != null) {
      distance = distance(position.x(), position.y(), request);
      sightClear =
          !spell.lineOfSight()
              || world.lineOfSight(request.casterX(), request.casterY(), position.x(), position.y());
      // Single-player refuses an aimed attack at a protected tile before it rolls anything.
      safeFromCombat =
          spell.attack()
              && !world.combatSafe(request.casterX(), request.casterY(), position.x(), position.y());
    } else if (kind == SpellCastRules.TargetKind.SELF && spell.selfCenteredAttack()) {
      safeFromCombat =
          !world.combatSafe(
              request.casterX(), request.casterY(), request.casterX(), request.casterY());
    } else if (friend != null) {
      distance = distance(friend.x(), friend.y(), request);
      sightClear =
          !spell.lineOfSight()
              || world.lineOfSight(request.casterX(), request.casterY(), friend.x(), friend.y());
    }
    var ruleSpell =
        new SpellCastRules.Spell(
            spell.targetType(),
            spell.attack(),
            spell.selfCenteredAttack(),
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
      switch (spell.kind()) {
        // An area heal heals the caster only: single-player routes it through the self-cast path.
        case HEAL, AREA_HEAL -> {
          int heal = healAmount(spell, request.caster(), random);
          if (heal > 0) events.add(new Event.SelfHeal(heal));
        }
        case NUKE, DRAIN, DEBUFF, DOT -> impact(request, spell, primary, world, events);
        case AREA_DAMAGE -> area(request, spell, primary, position, world, events);
        case SHIELD, GATEWAY -> {
          // Single-player installs no hook for a self cast: only the cost, the exhaustion and the visual.
          int heal = healAmount(spell, request.caster(), random);
          if (heal > 0) events.add(new Event.SelfHeal(heal));
        }
        case BUFF -> buff(request, spell, friend, random, events);
        case DETECTION -> detection(spell, request.caster(), random, events);
        case DISPEL -> dispel(spell, request.caster(), random, events);
        case SPECIAL -> renewArmor(request, events);
        default -> throw new IllegalArgumentException("unsupported_spell");
      }
      return new CastResolution(SpellCastRules.Verdict.NONE, null, outcome, List.copyOf(events));
    }
  }

  private static float distance(double x, double y, CastRequest request) {
    return (float)
        (Math.hypot(
                (x - request.casterX()) * GameConstants.GRID_W,
                (y - request.casterY()) * GameConstants.GRID_H)
            / Math.max(GameConstants.GRID_W, GameConstants.GRID_H));
  }

  /** True for the spell shapes this engine resolves; anything else must be refused by the caller. */
  public static boolean supported(SpellContentV2.Spell spell) {
    return switch (spell.kind()) {
      case NUKE, HEAL, AREA_HEAL, AREA_DAMAGE -> onlyEffects(spell, 1);
      // Tick-less damage over time: one hit, plus hooks single-player never installs (see the exporter).
      case DOT -> allEffects(spell, 1, 2, 9);
      case SHIELD, GATEWAY -> onlyEffects(spell, 9);
      case DRAIN -> onlyEffects(spell, 10);
      case DEBUFF, BUFF -> onlyEffects(spell, 2);
      case DETECTION -> allEffects(spell, 15, 16, 17);
      case DISPEL -> onlyEffects(spell, 13);
      case SPECIAL -> spell.key().equals(RENEW_ARMOR);
      default -> false;
    };
  }

  private static boolean onlyEffects(SpellContentV2.Spell spell, int type) {
    return allEffects(spell, type);
  }

  private static boolean allEffects(SpellContentV2.Spell spell, int... types) {
    return !spell.effects().isEmpty()
        && spell.effects().stream()
            .allMatch(
                effect -> {
                  for (int type : types) if (effect.type() == type) return true;
                  return false;
                });
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
    // Residual splash is for single-target attacks only (single-player: attack with no radius).
    boolean residual = spell.attack() && spell.radius() <= 0;
    List<UnitView> secondary =
        !residual
            ? List.of()
            : SpellSplash.nearest(
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
    int delta = healthDelta(spell, request.caster(), primary, 0d);
    boolean drain = spell.effects().stream().anyMatch(effect -> effect.type() == 10);
    if (delta < 0) events.add(new Event.Damage(primary.id(), -delta, DamageKind.DIRECT));
    // Formulas are damage-only. A positive roll would heal the monster offline; refuse to guess
    // here so a content change cannot silently diverge.
    if (delta > 0) throw new IllegalStateException("Attack spell rolled a heal: " + spell.key());
    int drained = drain ? Math.max(0, -delta) : 0;
    if (drained > 0) events.add(new Event.DrainHeal(drained));
    for (UnitView target : secondary) {
      int splash = SpellSplash.residualDamage(healthDelta(spell, request.caster(), target, 0d));
      if (splash > 0) events.add(new Event.Damage(target.id(), splash, DamageKind.RESIDUAL));
    }
  }

  /**
   * Mirror of {@code SpellEffectManager.resolveHealthDelta}: the centre formula at range 0, the ranged
   * formula (with {@code r} the distance in tiles) when the target is away from the centre.
   */
  private static int healthDelta(
      SpellContentV2.Spell spell, SpellFormulaContexts.Caster caster, UnitView target, double range) {
    for (var effect : spell.effects()) {
      if (effect.type() == 1 || effect.type() == 10) {
        String center = parameter(effect, 1);
        String ranged = parameter(effect, 2);
        String formula = range > 0d && ranged != null && !ranged.isBlank() ? ranged : center;
        int evaluated = evaluate(formula, caster, target, range);
        if (evaluated != 0) return evaluated;
      }
    }
    int low = Math.min(spell.minDamage(), spell.maxDamage());
    int high = Math.max(spell.minDamage(), spell.maxDamage());
    return low == high ? low : RandomSource.current().nextInt(low, high + 1);
  }

  private static int evaluate(
      String formula, SpellFormulaContexts.Caster caster, UnitView target, double range) {
    if (formula == null || formula.isBlank()) return 0;
    DiceFormula.Context context = SpellFormulaContexts.against(caster, target.resistance());
    String withRange =
        formula.replaceAll(
            "(?<![A-Za-z_.])r(?![A-Za-z_])", Double.toString(Math.max(0d, range)));
    return DiceFormula.of(withRange).evaluate(context);
  }

  // ---- stage 3: area damage ----------------------------------------------------------------

  /**
   * Mirror of single-player's three area routes. Self-centred (target type 18): the caster is the
   * centre, a creature is hit when its tile distance is at most the radius, nearest eight. Aimed at a
   * creature (17, 19): that creature is hit first at range 0 and holds one of the eight slots, the rest
   * lie strictly inside the radius measured in pixels over the larger tile side. Aimed at a position
   * (19): the same radius rule around the tile, no primary. Targets are all chosen before any damage;
   * each is rolled in turn, nearest first (ties by id), with {@code r} the distance from the centre.
   * Pixel arithmetic is done in floats, as {@code Vector2} does, so the boundaries agree exactly.
   */
  private static void area(
      CastRequest request,
      SpellContentV2.Spell spell,
      UnitView primary,
      Target.Position position,
      WorldView world,
      List<Event> events) {
    boolean selfCentred = request.target() instanceof Target.Self;
    if (!selfCentred && primary == null && position == null)
      throw new IllegalArgumentException("wrong_target");
    float cx;
    float cy;
    if (selfCentred) {
      cx = (float) (request.casterX() * GameConstants.GRID_W);
      cy = (float) (request.casterY() * GameConstants.GRID_H);
    } else if (primary != null) {
      cx = (float) (primary.x() * GameConstants.GRID_W);
      cy = (float) (primary.y() * GameConstants.GRID_H);
    } else {
      cx = (float) (position.x() * GameConstants.GRID_W);
      cy = (float) (position.y() * GameConstants.GRID_H);
    }
    float gridMax = Math.max(GameConstants.GRID_W, GameConstants.GRID_H);
    int radius = spell.radius();
    var near =
        world.unitsNear(
            cx / (double) GameConstants.GRID_W,
            cy / (double) GameConstants.GRID_H,
            radius + 1d,
            2d * radius + 1d);
    List<UnitView> secondary =
        SpellSplash.nearest(
            near,
            candidate -> {
              if (primary != null && candidate.id() == primary.id()) return false;
              if (!candidate.alive() || !candidate.attackable()) return false;
              if (!world.combatSafe(
                  request.casterX(), request.casterY(), candidate.x(), candidate.y())) return false;
              float tx = (float) (candidate.x() * GameConstants.GRID_W);
              float ty = (float) (candidate.y() * GameConstants.GRID_H);
              boolean inRange =
                  selfCentred
                      ? tileDistance(cx, cy, tx, ty) <= radius
                      : pixelDistance(cx, cy, tx, ty) / gridMax < radius;
              return inRange
                  && (!spell.lineOfSight()
                      || world.lineOfSight(
                          cx / (double) GameConstants.GRID_W,
                          cy / (double) GameConstants.GRID_H,
                          candidate.x(),
                          candidate.y()));
            },
            candidate -> {
              float dx = (float) (candidate.x() * GameConstants.GRID_W) - cx;
              float dy = (float) (candidate.y() * GameConstants.GRID_H) - cy;
              return dx * dx + dy * dy;
            },
            primary == null ? 0 : 1);
    if (primary != null) {
      int delta = healthDelta(spell, request.caster(), primary, 0d);
      if (delta > 0) throw new IllegalStateException("Attack spell rolled a heal: " + spell.key());
      if (delta < 0) events.add(new Event.Damage(primary.id(), -delta, DamageKind.DIRECT));
    }
    for (UnitView target : secondary) {
      float tx = (float) (target.x() * GameConstants.GRID_W);
      float ty = (float) (target.y() * GameConstants.GRID_H);
      double range = selfCentred ? tileDistance(cx, cy, tx, ty) : pixelDistance(cx, cy, tx, ty) / gridMax;
      int delta = healthDelta(spell, request.caster(), target, range);
      if (delta > 0) throw new IllegalStateException("Attack spell rolled a heal: " + spell.key());
      if (delta < 0) events.add(new Event.Damage(target.id(), -delta, DamageKind.AREA));
    }
  }

  /** {@code SpellAreaTargeting.distanceInTiles}: each axis in its own tile size. */
  private static double tileDistance(float cx, float cy, float tx, float ty) {
    double x = (tx - cx) / GameConstants.GRID_W;
    double y = (ty - cy) / GameConstants.GRID_H;
    return Math.hypot(x, y);
  }

  /** {@code Vector2.dst}, in float arithmetic. */
  private static float pixelDistance(float cx, float cy, float tx, float ty) {
    float dx = tx - cx;
    float dy = ty - cy;
    return (float) Math.sqrt(dx * dx + dy * dy);
  }

  // ---- stage 2: buffs ----------------------------------------------------------------------

  /**
   * Mirror of {@code MainGameScreen.castDefensiveSpell} for a buff: the heal roll (nothing for a
   * buff), then {@code resolvePlayerBuffEffects} (duration first, then every effect in authored
   * order), then the duration once more for the buff's own length.
   */
  private static void buff(
      CastRequest request,
      SpellContentV2.Spell spell,
      FriendView friend,
      RandomGenerator random,
      List<Event> events) {
    int heal = healAmount(spell, request.caster(), random);
    if (heal > 0) events.add(new Event.SelfHeal(heal));
    else if (heal < 0) throw new IllegalStateException("Buff rolled damage: " + spell.key());
    DiceFormula.Context context =
        friend == null
            ? SpellFormulaContexts.forSelf(request.caster())
            : friendContext(request.caster(), friend.caster());
    var resolved = resolveBuff(spell, context);
    if (!resolved.modifiers().isEmpty())
      events.add(
          new Event.ApplyBuff(
              friend == null ? null : friend.id(),
              spell.key(),
              resolved.durationSeconds(),
              resolved.modifiers()));
  }

  /**
   * Formulas for a buff laid on another player: the caster's own level, stats and skills, but the
   * recipient's element powers and resistances, because {@code target.true_*} names the recipient.
   * This mirrors how a temple blessing builds its context; single-player has no player-to-player
   * buff, so there is no offline value to match.
   */
  public static DiceFormula.Context friendContext(
      SpellFormulaContexts.Caster caster, SpellFormulaContexts.Caster recipient) {
    var blended =
        new SpellFormulaContexts.Caster(
            caster.level(),
            caster.effective(),
            caster.trueStats(),
            caster.trueAttack(),
            caster.trueArchery(),
            caster.trueDodge(),
            recipient.truePower(),
            recipient.truePower(),
            recipient.trueResistance(),
            caster.equippedItems());
    return SpellFormulaContexts.against(blended, recipient.trueResistance());
  }

  /** A buff's resolved duration (seconds, 0 = never expires) and amounts. */
  public record BuffResolution(int durationSeconds, List<Modifier> modifiers) {}

  /** Mirror of {@code SpellEffectManager.resolvePlayerBuffEffects} plus {@code resolveDurationSeconds}. */
  public static BuffResolution resolveBuff(SpellContentV2.Spell spell, DiceFormula.Context context) {
    boolean hasDuration = durationSeconds(spell, context) > 0;
    var modifiers = new ArrayList<Modifier>();
    for (var effect : spell.effects()) {
      if (effect.type() != 2) continue;
      String attribute = BuffAttributes.normalize(parameter(effect, 2));
      if (attribute == null) continue;
      if ("mana".equals(attribute) && !hasDuration) continue;
      modifiers.add(new Modifier(attribute, evaluate(parameter(effect, 3), context)));
    }
    return new BuffResolution(durationSeconds(spell, context), List.copyOf(modifiers));
  }

  private static int durationSeconds(SpellContentV2.Spell spell, DiceFormula.Context context) {
    long millis = Math.max(0, evaluate(spell.duration(), context));
    return millis <= 0
        ? 0
        : (int) Math.min(Integer.MAX_VALUE, Math.max(1L, (millis + 999L) / 1_000L));
  }

  /**
   * Mirror of {@code applyRenewArmorRecast}: every learned component is re-laid in the fixed order,
   * each one resolved against the caster as the previous ones left it.
   */
  private static void renewArmor(CastRequest request, List<Event> events) {
    int heal = healAmount(request.spell(), request.caster(), RandomSource.current());
    if (heal > 0) events.add(new Event.SelfHeal(heal));
    var running = new LinkedHashMap<String, Map<String, Integer>>(request.activeModifiers());
    var caster = request.caster();
    for (String key : RENEW_ARMOR_COMPONENTS) {
      var component = request.related().get(key);
      if (component == null) continue;
      var resolved = resolveBuff(component, SpellFormulaContexts.forSelf(caster));
      if (resolved.modifiers().isEmpty()) continue;
      events.add(new Event.ApplyBuff(null, key, resolved.durationSeconds(), resolved.modifiers()));
      var next = new LinkedHashMap<String, Integer>();
      for (Modifier modifier : resolved.modifiers()) next.merge(modifier.attribute(), modifier.amount(), Integer::sum);
      var delta = new LinkedHashMap<String, Integer>(next);
      running.getOrDefault(key, Map.of()).forEach((attribute, amount) -> delta.merge(attribute, -amount, Integer::sum));
      running.put(key, next);
      caster = adjust(caster, delta);
    }
  }

  /** The caster with buff amounts added to its effective stats and element powers (true readings stay). */
  public static SpellFormulaContexts.Caster adjust(
      SpellFormulaContexts.Caster caster, Map<String, Integer> delta) {
    var stats = caster.effective();
    var power = caster.power();
    return new SpellFormulaContexts.Caster(
        caster.level(),
        new SpellFormulaContexts.Stats(
            stats.strength() + delta.getOrDefault("str", 0),
            stats.endurance() + delta.getOrDefault("end", 0),
            stats.dexterity() + delta.getOrDefault("dex", 0),
            stats.intelligence() + delta.getOrDefault("int", 0),
            stats.wisdom() + delta.getOrDefault("wis", 0)),
        caster.trueStats(),
        caster.trueAttack(),
        caster.trueArchery(),
        caster.trueDodge(),
        new SpellFormulaContexts.Elements(
            power.fire() + delta.getOrDefault("power:fire", 0),
            power.earth() + delta.getOrDefault("power:earth", 0),
            power.air() + delta.getOrDefault("power:air", 0),
            power.water() + delta.getOrDefault("power:water", 0),
            power.light() + delta.getOrDefault("power:light", 0),
            power.dark() + delta.getOrDefault("power:dark", 0)),
        caster.truePower(),
        caster.trueResistance(),
        caster.equippedItems());
  }

  // ---- stage 2: flags and dispels ----------------------------------------------------------

  /** Mirror of the effect 15/16/17 branches of {@code applyPlayerUtilityEffects}. */
  private static void detection(
      SpellContentV2.Spell spell,
      SpellFormulaContexts.Caster caster,
      RandomGenerator random,
      List<Event> events) {
    DiceFormula.Context context = SpellFormulaContexts.forSelf(caster);
    for (var effect : spell.effects()) {
      int chance = Math.max(0, Math.min(100, evaluate(parameter(effect, 1), context)));
      if (chance <= 0 || random.nextInt(101) > chance) continue;
      long duration = Math.max(1_000L, evaluate(spell.duration(), context));
      events.add(
          switch (effect.type()) {
            case 15 -> new Event.Hide(duration);
            case 16 -> new Event.DetectInvisible(duration);
            default -> new Event.DetectHidden(duration);
          });
    }
  }

  /** Mirror of the effect 13 branch of {@code applyPlayerUtilityEffects}: one roll per resolvable id. */
  private static void dispel(
      SpellContentV2.Spell spell,
      SpellFormulaContexts.Caster caster,
      RandomGenerator random,
      List<Event> events) {
    DiceFormula.Context context = SpellFormulaContexts.forSelf(caster);
    for (var effect : spell.effects()) {
      int id = evaluate(parameter(effect, 1), context);
      int chance = Math.max(0, Math.min(100, evaluate(parameter(effect, 2), context)));
      String target = spell.dispelTargets().get(id);
      if (target != null && chance > 0 && random.nextInt(101) <= chance)
        events.add(new Event.Dispel(target));
    }
  }

  private static int evaluate(String formula, DiceFormula.Context context) {
    if (formula == null || formula.isBlank()) return 0;
    return DiceFormula.of(formula).evaluate(context);
  }

  private static String parameter(SpellContent.Effect effect, int id) {
    for (var param : effect.params()) if (param.id() == id) return param.expression();
    return null;
  }
}
