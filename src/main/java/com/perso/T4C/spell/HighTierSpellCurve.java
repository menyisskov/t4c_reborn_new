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

  /** Tiers at or above this get the palette-shifted "Ascended" impact (T4C-0077) instead of the
   * plain impact every lower-level spell of the same element/shape already uses. */
  private static final int ASCENDED_TIER_THRESHOLD = 250;

  /** Tiers at or above this play the original game's own top-tier spell animation for their
   * element (T4C-0083, see {@link #GRAND_IMPACTS}), instead of the Ascended recolor. */
  public static final int GRAND_TIER_THRESHOLD = 300;

  /**
   * Element to the original top-tier animation its level-300 spells play (T4C-0083). The "Grand"
   * names are alpha-blended copies of the original additive "NM" animations made by
   * {@code tools.GrandVfxGenerator}; BoulderFire (earth) and iceTree (water) are the originals
   * themselves, which already draw correctly. {@code GrandVfxAssetTest} checks every one is packed.
   */
  public static final Map<Integer, String> GRAND_IMPACTS =
      Map.of(
          FIRE, "GrandFire",
          EARTH, "BoulderFire",
          AIR, "GrandAir",
          WATER, "iceTree",
          LIGHT, "GrandLight",
          DARK, "GrandDark");

  /** Tiers at or above this get a "Mythic" impact (T4C-0082, redrawn in T4C-0083 as an upgrade of
   * the element's {@link #GRAND_IMPACTS} original) for every element listed in
   * {@link #MYTHIC_IMPACTS}; an element without one falls back to its Grand impact. */
  public static final int MYTHIC_TIER_THRESHOLD = 350;

  /**
   * Element to the base name of its newly drawn Mythic impact, packed by
   * {@code tools.MythicVfxPacker} (T4C-0082). One animation per element serves both shapes - the
   * area version already reads differently through {@link GrandImpactShower}. Only list an element
   * once its frames are actually packed: {@code MythicVfxAssetTest} fails on a listed element
   * with missing or broken frames.
   */
  public static final Map<Integer, String> MYTHIC_IMPACTS =
      Map.of(
          FIRE, "MythicFire",
          EARTH, "MythicEarth",
          AIR, "MythicAir",
          WATER, "MythicWater",
          LIGHT, "MythicLight",
          DARK, "MythicDark");

  public static SpellData attack(String key, int spellId, int element, int tier, Shape shape) {
    Visuals v = Visuals.of(element, shape, tier >= ASCENDED_TIER_THRESHOLD);
    String special = tier >= MYTHIC_TIER_THRESHOLD ? MYTHIC_IMPACTS.get(element) : null;
    if (special == null && tier >= GRAND_TIER_THRESHOLD) {
      special = GRAND_IMPACTS.get(element);
    }
    if (special != null) {
      v = v.withImpact(special + "-");
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
   * Legacy spell art per school, reused as-is for the projectile - but for {@code ascended}
   * (tier 250+, T4C-0077) the impact is swapped for a palette-shifted "-Ascended" variant of the
   * same animation (generated by {@code tools.AscendedVfxGenerator}), so the strongest spells
   * finally look like a different, more intense effect instead of the exact same burst every
   * lower-tier spell of that element/shape already uses.
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

    static Visuals of(int element, Shape shape, boolean ascended) {
      boolean area = shape == Shape.AREA;
      return switch (element) {
        case FIRE ->
            area
                ? new Visuals("64kSpellIconFireAttackArea", "64kSpellFireBall",
                    ascendedImpact("GreatExplosion-", ascended), "Healing.wav", "Explosion.wav",
                    30124, 30014)
                : new Visuals("64kSpellIconFireAttackSingle", "64kSpellEnergyBall-",
                    ascendedImpact("64kSpellFireCircle-", ascended), "Healing.wav",
                    "Fire circle.wav", 30121, 0);
        case EARTH ->
            area
                ? new Visuals("64kSpellIconEarthAttackArea", "64kSpellEnergyBallGreen-",
                    ascendedImpact("64kSpellBoulders-", ascended), "Healing.wav", "Boulders.wav",
                    30056, 30056)
                : new Visuals("64kSpellIconEarthAttackSingle", "64kSpellEnergyBallGreen-",
                    ascendedImpact("Flak1-", ascended), "Healing.wav", "Explosion.wav", 30073, 0);
        case AIR ->
            area
                ? new Visuals("64kSpellIconAirAttackArea", "64kSpellEnergyBallYellow-",
                    ascendedImpact("GreatBolt-", ascended), "Healing.wav", "Spark.wav", 30087,
                    30087)
                : new Visuals("64kSpellIconAirAttackSingle", "Lightning",
                    ascendedImpact("ElectricShield-", ascended), "Lightning.wav",
                    "Electric Shield.wav", 30002, 0);
        case WATER ->
            area
                ? new Visuals("64kSpellIconWaterAttackArea", "64kSpellEnergyBallBlue-",
                    ascendedImpact("64kSpellGlacier-", ascended), "Healing.wav", "Glacier.wav",
                    30085, 30020)
                : new Visuals("64kSpellIconWaterAttackSingle", "IceShard",
                    ascendedImpact("IceCloud-", ascended), "Small Projectile.wav",
                    "Ice Cloud.wav", 30023, 0);
        case LIGHT ->
            area
                ? new Visuals("64kSpellIconLightAttackArea", "64kSpellEnergyBallWhite-",
                    ascendedImpact("HealingSpell-", ascended), "Healing.wav", "Healing.wav",
                    30096, 30096)
                : new Visuals("64kSpellIconLightAttackSingle", "64kSpellEnergyBallWhite-",
                    ascendedImpact("HealingSpell-", ascended), "Healing.wav", "Healing.wav",
                    30096, 0);
        case DARK ->
            area
                ? new Visuals("64kSpellIconDarkDrainArea", "64kSpellEnergyBallBlack-",
                    ascendedImpact("Curse-", ascended), "Healing.wav", "Curse.wav", 30062, 30062)
                : new Visuals("64kSpellIconDarkAttackSingle", "64kSpellEnergyBallBlack-",
                    ascendedImpact("Curse-", ascended), "Healing.wav", "Curse.wav", 30062, 0);
        default -> throw new IllegalArgumentException("Unknown spell element " + element);
      };
    }

    private static String ascendedImpact(String baseImpact, boolean ascended) {
      if (!ascended) {
        return baseImpact;
      }
      // baseImpact always ends in "-" (see spell-creator skill's Graphics section); insert the
      // "Ascended" tag before that trailing dash so it still resolves as "<name>-Ascended-<frame>".
      return baseImpact.substring(0, baseImpact.length() - 1) + "-Ascended-";
    }
  }
}
