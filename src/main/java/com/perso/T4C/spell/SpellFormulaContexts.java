package com.perso.T4C.spell;

import com.perso.T4C.helper.DiceFormula;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * The one place that turns a caster's numbers into a {@link DiceFormula.Context}. Single-player
 * ({@code SpellCastingService}, {@code SpellEffectManager}) and the online server both fill a
 * {@link Caster} from their own state and call these builders, so a spell formula sees the same
 * inputs on both sides. The three builders reproduce, value for value, the three contexts
 * single-player used before this class existed:
 *
 * <ul>
 *   <li>{@link #forCastRules} - mana cost, exhaustion and success formulas (powers neutral).
 *   <li>{@link #forSelf} - self-target and caster-side effects (real powers, resistances 100).
 *   <li>{@link #against} - effects landing on a target with the given resistances.
 * </ul>
 */
public final class SpellFormulaContexts {
  private SpellFormulaContexts() {}

  /** Strength, endurance, dexterity, intelligence and wisdom, in formula order. */
  public record Stats(int strength, int endurance, int dexterity, int intelligence, int wisdom) {}

  /** One value per element in {@link ElementRules#ELEMENTS} order. */
  public record Elements(int fire, int earth, int air, int water, int light, int dark) {
    public static Elements uniform(int value) {
      return new Elements(value, value, value, value, value, value);
    }

    public int of(String element) {
      return switch (element) {
        case "fire" -> fire;
        case "earth" -> earth;
        case "air" -> air;
        case "water" -> water;
        case "light" -> light;
        case "dark" -> dark;
        default -> throw new IllegalArgumentException("Unknown element " + element);
      };
    }
  }

  /**
   * Everything a spell formula may read about its caster. "Effective" values include active spell
   * buffs; "true" values (gear and permanent bonuses only) feed {@code self.true_*}. A caster with
   * no buffs has equal effective and true values. {@code equippedItems} counts equipped items by
   * numeric item id, a mirrored two-slot item counting once per slot as it does in single-player.
   */
  public record Caster(
      int level,
      Stats effective,
      Stats trueStats,
      int trueAttack,
      int trueArchery,
      int trueDodge,
      Elements power,
      Elements truePower,
      Elements trueResistance,
      Map<Integer, Integer> equippedItems) {
    public Caster {
      if (effective == null || trueStats == null || power == null || truePower == null
          || trueResistance == null) throw new IllegalArgumentException("Incomplete caster");
      equippedItems = equippedItems == null ? Map.of() : Map.copyOf(new TreeMap<>(equippedItems));
    }
  }

  /** Cast-rule formulas: effective stats, neutral element powers, no real target. */
  public static DiceFormula.Context forCastRules(Caster caster) {
    Stats stats = caster.effective();
    return new DiceFormula.Context(
            stats.strength(),
            stats.endurance(),
            stats.dexterity(),
            stats.intelligence(),
            0,
            stats.wisdom(),
            0,
            caster.level())
        .withSelfTrue(selfTrue(caster))
        .withEquippedItems(caster.equippedItems());
  }

  /** Caster-side effects: real element powers, every target resistance at the neutral 100. */
  public static DiceFormula.Context forSelf(Caster caster) {
    return against(caster, Elements.uniform(100));
  }

  /**
   * Effects landing on a target. {@code targetResistance} must already be normalised with {@link
   * ElementRules#targetResistance}.
   */
  public static DiceFormula.Context against(Caster caster, Elements targetResistance) {
    Stats stats = caster.effective();
    Elements power = caster.power();
    return new DiceFormula.Context(
            stats.strength(),
            stats.endurance(),
            stats.dexterity(),
            stats.intelligence(),
            0,
            stats.wisdom(),
            0,
            caster.level(),
            0,
            targetResistance.fire(),
            targetResistance.earth(),
            targetResistance.air(),
            targetResistance.water(),
            targetResistance.light(),
            targetResistance.dark(),
            power.fire(),
            power.earth(),
            power.air(),
            power.water(),
            power.light(),
            power.dark())
        .withSelfTrue(selfTrue(caster))
        .withEquippedItems(caster.equippedItems());
  }

  /** The caster's own true readings for the {@code self.true_*} formula variables. */
  public static Map<String, Integer> selfTrue(Caster caster) {
    Map<String, Integer> readings = new LinkedHashMap<>();
    Stats stats = caster.trueStats();
    readings.put("str", stats.strength());
    readings.put("end", stats.endurance());
    readings.put("agi", stats.dexterity());
    readings.put("int", stats.intelligence());
    readings.put("wis", stats.wisdom());
    readings.put("level", caster.level());
    readings.put("attack", caster.trueAttack());
    readings.put("archery", caster.trueArchery());
    readings.put("dodge", caster.trueDodge());
    for (String element : ElementRules.ELEMENTS) {
      readings.put(element, caster.truePower().of(element));
      readings.put("r_" + element, caster.trueResistance().of(element));
    }
    return readings;
  }

  /** Every field a formula can read, in a stable text form, for equality tests and diagnostics. */
  public static String describe(DiceFormula.Context context) {
    return "str=" + context.str + " end=" + context.end + " agi=" + context.agi + " int=" + context.intel
        + " wil=" + context.wil + " wis=" + context.wis + " luck=" + context.luck
        + " level=" + context.level + " arrow=" + context.arrowDmg
        + " resist[fire=" + context.targetResistFire + " earth=" + context.targetResistEarth
        + " air=" + context.targetResistAir + " water=" + context.targetResistWater
        + " light=" + context.targetResistLight + " dark=" + context.targetResistDark + "]"
        + " power[fire=" + context.selfFire + " earth=" + context.selfEarth + " air=" + context.selfAir
        + " water=" + context.selfWater + " light=" + context.selfLight + " dark=" + context.selfDark + "]"
        + " true=" + new TreeMap<>(context.selfTrue) + " equipped=" + new TreeMap<>(context.equippedItems);
  }
}
