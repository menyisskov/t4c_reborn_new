package com.perso.T4C.spell;

import com.perso.T4C.config.GameConstants;
import java.util.List;
import java.util.Map;

/**
 * One shared power curve for the high-tier (level 150-350) attack spells, so every school gets a
 * spell at the same levels with the same requirements and the same damage at those requirements
 * (T4C-0025). Each spell class in {@code spell.definition} just picks its element, tier and shape
 * and delegates here; all balancing lives in this one file.
 *
 * <p><b>Stat requirements</b> are sized against what a character can actually have at that level:
 * 5 stat points per level plus the rebirth base ({@link GameConstants#REBIRTH_BASE_ATTRIBUTE} +
 * {@link GameConstants#REBIRTH_ATTRIBUTE_PER_REMORT} per rebirth, in all five attributes). A
 * spell of tier {@code L} needs {@code 2.5L} in its school's main stat plus {@code 0.6L} in the
 * other casting stat - i.e. {@code 3.1L} of the {@code ~5L} points earned by level {@code L},
 * which even a first-life (never reborn) character can reach while still putting the rest into
 * endurance. Air, the hybrid school, splits the same total evenly ({@code 1.55L} each). At the
 * top tier (350) that is 875 Intelligence (or Wisdom). The ladder stops at 350 although the level
 * cap is 400: the level-400 rung was removed (T4C-0084).
 *
 * <p><b>Damage</b> is {@code (1d(L/5) + L/2 + stat/4) * power/resist}, times 6 for a single-target
 * bolt or 5 for an area spell (which hits everything in its radius). At exactly the requirements
 * that is ~7.35L per bolt (1100 at 150, ~2570 at 350) - roughly a dozen casts for a same-level
 * monster - and it keeps climbing as the caster's stat outgrows the requirement. Air uses
 * {@code (int+wis)/5} for its stat term so a hybrid caster at requirements lands on the same
 * number.
 */
public final class HighTierSpellCurve {
  public static final int FIRE = 1;
  public static final int EARTH = 2;
  public static final int AIR = 3;
  public static final int WATER = 4;
  public static final int LIGHT = 5;
  public static final int DARK = 6;

  /** The tiers every school has exactly one attack spell at. */
  public static final List<Integer> TIERS = List.of(150, 200, 250, 300, 350);

  /** Single-target bolt or ground-targeted area spell. */
  public enum Shape {
    BOLT,
    AREA
  }

  private static final int BOLT_MULTIPLIER = 6;
  private static final int AREA_MULTIPLIER = 5;
  private static final int AREA_RADIUS = 4;

  // T4C-0054: cast speed decays with level like every other spell (the idiom described in the
  // spell-creator skill), converging on the same 1000/750/750ms floor everything else uses by
  // GameConstants.MAX_PLAYER_LEVEL (400). Written as (400-self.level) rather than
  // (self.level-tier) so it's one formula shared by every tier: at a tier's own minLevel it
  // reproduces that tier's starting exhaustion, and a level-400 spell (minLevel==400, the level
  // cap) is *already* at the floor the moment it's learned, with no decay window at all.
  private static final String BOLT_MENTAL_EXHAUSTION =
      "1000+if((400-self.level)>=0?(600*(400-self.level)/250):0)";
  private static final String BOLT_PHYSICAL_EXHAUSTION =
      "750+if((400-self.level)>=0?(450*(400-self.level)/250):0)";
  private static final String AREA_MENTAL_EXHAUSTION =
      "1000+if((400-self.level)>=0?(900*(400-self.level)/250):0)";
  private static final String AREA_PHYSICAL_EXHAUSTION =
      "750+if((400-self.level)>=0?(650*(400-self.level)/250):0)";

  private HighTierSpellCurve() {}

  /** Main casting stat requirement at a tier: Intelligence for fire/water/dark, Wisdom for
   * earth/light. */
  public static int primaryRequirement(int tier) {
    return tier * 5 / 2;
  }

  /** The other casting stat's requirement at a tier. */
  public static int secondaryRequirement(int tier) {
    return tier * 3 / 5;
  }

  /** Air's Intelligence and Wisdom requirement (each) at a tier. */
  public static int hybridRequirement(int tier) {
    return tier * 31 / 20;
  }

  public static boolean usesWisdom(int element) {
    return element == EARTH || element == LIGHT;
  }

  public static int minIntelligence(int element, int tier) {
    if (element == AIR) return hybridRequirement(tier);
    return usesWisdom(element) ? secondaryRequirement(tier) : primaryRequirement(tier);
  }

  public static int minWisdom(int element, int tier) {
    if (element == AIR) return hybridRequirement(tier);
    return usesWisdom(element) ? primaryRequirement(tier) : secondaryRequirement(tier);
  }

  /** Gold a trainer charges to teach a spell of this tier and shape. */
  public static int price(int tier, Shape shape) {
    int base = tier * tier * 20;
    return shape == Shape.AREA ? base * 5 / 4 : base;
  }

  public static String manaCost(int tier, Shape shape) {
    return String.valueOf(shape == Shape.AREA ? tier * 3 / 10 : tier / 5);
  }

  /** The (negative = damage) formula a spell of this element/tier/shape deals. */
  public static String damageFormula(int element, int tier, Shape shape) {
    String stat =
        element == AIR ? "(self.int+self.wis)/5" : usesWisdom(element) ? "self.wis/4" : "self.int/4";
    String school = schoolVariable(element);
    int multiplier = shape == Shape.AREA ? AREA_MULTIPLIER : BOLT_MULTIPLIER;
    return "-(((1d" + tier / 5 + "+" + tier / 2 + "+" + stat + ")*self." + school + "/target.r_"
        + school + ")*" + multiplier + ")";
  }

  /**
   * The impact animation of every ladder spell, by element then tier (T4C-0085, owner's call:
   * "each spell its own animation"). No two ladder spells share one, and none reuses the impact of
   * a lower-level spell - {@code HighTierSpellLadderTest} enforces both. By tier:
   *
   * <ul>
   *   <li>150 (bolt) - {@code Strike*}: a new SpriteCook strike per element;
   *   <li>200 (area) - {@code Eruption*}: the pixel-art eruptions first made for T4C-0082;
   *   <li>250 (bolt) - the "-Ascended" recolor of the element's legacy bolt impact (T4C-0077);
   *   <li>300 (bolt) - the original game's own top-tier animation for the element: {@code Grand*}
   *       are alpha-blended copies of the additive "NM" originals ({@code tools.GrandVfxGenerator});
   *       BoulderFire and iceTree are the originals themselves;
   *   <li>350 (area) - {@code Mythic*}: SpriteCook upgrades drawn from that same original.
   * </ul>
   *
   * Values are sprite base names without the trailing "-". Every family is checked as packed by
   * {@code MythicVfxAssetTest} / {@code GrandVfxAssetTest}.
   */
  public static final Map<Integer, Map<Integer, String>> IMPACTS =
      Map.of(
          FIRE,
          Map.of(
              150, "StrikeFire",
              200, "EruptionFire",
              250, "64kSpellFireCircle-Ascended",
              300, "GrandFire",
              350, "MythicFire"),
          EARTH,
          Map.of(
              150, "StrikeEarth",
              200, "EruptionEarth",
              250, "Flak1-Ascended",
              300, "BoulderFire",
              350, "MythicEarth"),
          AIR,
          Map.of(
              150, "StrikeAir",
              200, "EruptionAir",
              250, "ElectricShield-Ascended",
              300, "GrandAir",
              350, "MythicAir"),
          WATER,
          Map.of(
              150, "StrikeWater",
              200, "EruptionWater",
              250, "IceCloud-Ascended",
              300, "iceTree",
              350, "MythicWater"),
          LIGHT,
          Map.of(
              150, "StrikeLight",
              200, "EruptionLight",
              250, "HealingSpell-Ascended",
              300, "GrandLight",
              350, "MythicLight"),
          DARK,
          Map.of(
              150, "StrikeDark",
              200, "EruptionDark",
              250, "Curse-Ascended",
              300, "GrandDark",
              350, "MythicDark"));

  public static SpellData attack(String key, int spellId, int element, int tier, Shape shape) {
    Visuals v = Visuals.of(element, shape);
    String impact = IMPACTS.getOrDefault(element, Map.of()).get(tier);
    if (impact != null) {
      v = v.withImpact(impact + "-");
    }
    boolean area = shape == Shape.AREA;
    String damage = damageFormula(element, tier, shape);
    return new SpellData(
        "${spell." + key + "}",
        "${spell.description." + key + "}",
        manaCost(tier, shape),
        area ? AREA_RADIUS : 0,
        minIntelligence(element, tier),
        minWisdom(element, tier),
        tier,
        true,
        true,
        v.icon,
        v.projectile,
        v.impact,
        0,
        0,
        v.sound,
        v.soundImpact,
        0,
        "0",
        "0",
        price(tier, shape),
        null,
        spellId,
        element,
        area ? 19 : 11,
        area ? SpellData.ATTACK_MENTAL : SpellData.ATTACK_PHYSICAL,
        "100",
        area ? AREA_MENTAL_EXHAUSTION : BOLT_MENTAL_EXHAUSTION,
        area ? AREA_PHYSICAL_EXHAUSTION : BOLT_PHYSICAL_EXHAUSTION,
        area ? AREA_PHYSICAL_EXHAUSTION : BOLT_PHYSICAL_EXHAUSTION,
        v.visualEffect,
        area ? v.visualEffectTarget : 0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, damage),
                    new SpellData.T4cEffect.EffectParam(2, area ? damage : null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }

  private static String schoolVariable(int element) {
    return switch (element) {
      case FIRE -> "fire";
      case EARTH -> "earth";
      case AIR -> "air";
      case WATER -> "water";
      case LIGHT -> "light";
      case DARK -> "dark";
      default -> throw new IllegalArgumentException("Unknown spell element " + element);
    };
  }

  /**
   * Legacy spell art per school: icon, projectile, sounds and palette ids. The impact given here
   * is the plain legacy one; {@link #attack} swaps it for the spell's own entry in
   * {@link #IMPACTS}.
   */
  private record Visuals(
      String icon,
      String projectile,
      String impact,
      String sound,
      String soundImpact,
      int visualEffect,
      int visualEffectTarget) {

    Visuals withImpact(String newImpact) {
      return new Visuals(
          icon, projectile, newImpact, sound, soundImpact, visualEffect, visualEffectTarget);
    }

    static Visuals of(int element, Shape shape) {
      boolean area = shape == Shape.AREA;
      return switch (element) {
        case FIRE ->
            area
                ? new Visuals("64kSpellIconFireAttackArea", "64kSpellFireBall",
                    "GreatExplosion-", "Healing.wav", "Explosion.wav",
                    30124, 30014)
                : new Visuals("64kSpellIconFireAttackSingle", "64kSpellEnergyBall-",
                    "64kSpellFireCircle-", "Healing.wav",
                    "Fire circle.wav", 30121, 0);
        case EARTH ->
            area
                ? new Visuals("64kSpellIconEarthAttackArea", "64kSpellEnergyBallGreen-",
                    "64kSpellBoulders-", "Healing.wav", "Boulders.wav",
                    30056, 30056)
                : new Visuals("64kSpellIconEarthAttackSingle", "64kSpellEnergyBallGreen-",
                    "Flak1-", "Healing.wav", "Explosion.wav", 30073, 0);
        case AIR ->
            area
                ? new Visuals("64kSpellIconAirAttackArea", "64kSpellEnergyBallYellow-",
                    "GreatBolt-", "Healing.wav", "Spark.wav", 30087,
                    30087)
                : new Visuals("64kSpellIconAirAttackSingle", "Lightning",
                    "ElectricShield-", "Lightning.wav",
                    "Electric Shield.wav", 30002, 0);
        case WATER ->
            area
                ? new Visuals("64kSpellIconWaterAttackArea", "64kSpellEnergyBallBlue-",
                    "64kSpellGlacier-", "Healing.wav", "Glacier.wav",
                    30085, 30020)
                : new Visuals("64kSpellIconWaterAttackSingle", "IceShard",
                    "IceCloud-", "Small Projectile.wav",
                    "Ice Cloud.wav", 30023, 0);
        case LIGHT ->
            area
                ? new Visuals("64kSpellIconLightAttackArea", "64kSpellEnergyBallWhite-",
                    "HealingSpell-", "Healing.wav", "Healing.wav",
                    30096, 30096)
                : new Visuals("64kSpellIconLightAttackSingle", "64kSpellEnergyBallWhite-",
                    "HealingSpell-", "Healing.wav", "Healing.wav",
                    30096, 0);
        case DARK ->
            area
                ? new Visuals("64kSpellIconDarkDrainArea", "64kSpellEnergyBallBlack-",
                    "Curse-", "Healing.wav", "Curse.wav", 30062, 30062)
                : new Visuals("64kSpellIconDarkAttackSingle", "64kSpellEnergyBallBlack-",
                    "Curse-", "Healing.wav", "Curse.wav", 30062, 0);
        default -> throw new IllegalArgumentException("Unknown spell element " + element);
      };
    }
  }
}
