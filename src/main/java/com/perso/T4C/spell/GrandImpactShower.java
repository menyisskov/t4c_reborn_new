package com.perso.T4C.spell;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Turns a single area-attack spell's impact into a multi-burst "shower" scattered across its
 * radius, scaled up by the spell's own {@code minLevel}. This is how a level-400 ultra nuke reads
 * as visibly grander than the level-27 spell it otherwise shares its impact sprite with (T4C-0064)
 * — no new art, just more of the existing burst landing in more places over a short cascade.
 *
 * <p>Purely cosmetic and engine-agnostic: callers convert the fractional offsets/angles into world
 * pixels using the spell's actual radius, and the delays into a staggered trigger schedule.
 */
public final class GrandImpactShower {
  /**
   * One burst in the shower. {@code radiusFraction} (0-1) and {@code angleRadians} locate it
   * relative to the cast point and the spell's radius; {@code delaySeconds} staggers it after the
   * cast lands. The first burst is always {@code (0, 0, 0)} — dead center, no delay — matching the
   * single-burst behavior every area spell had before this existed.
   */
  public record Burst(float radiusFraction, float angleRadians, float delaySeconds) {}

  /** Below this level an area spell renders exactly as it always has: one centered burst. */
  private static final int GRAND_TIER_THRESHOLD = 150;

  private static final int MAX_BURSTS = 7;
  private static final float MIN_SPREAD_FRACTION = 0.3f;
  private static final float MAX_SPREAD_FRACTION = 0.95f;
  private static final float BASE_STAGGER_SECONDS = 0.09f;
  private static final float STAGGER_JITTER_SECONDS = 0.05f;

  private GrandImpactShower() {}

  public static List<Burst> bursts(SpellData spell) {
    return bursts(spell, new Random());
  }

  static List<Burst> bursts(SpellData spell, Random random) {
    List<Burst> single = List.of(new Burst(0f, 0f, 0f));
    if (spell == null || !spell.isAttack() || spell.getRadius() <= 0) {
      return single;
    }
    int level = spell.getMinLevel();
    if (level < GRAND_TIER_THRESHOLD) {
      return single;
    }
    int extraBursts =
        Math.min(MAX_BURSTS - 1, 1 + (level - GRAND_TIER_THRESHOLD) / 50);
    List<Burst> bursts = new ArrayList<>(extraBursts + 1);
    bursts.add(new Burst(0f, 0f, 0f));
    for (int i = 0; i < extraBursts; i++) {
      float radiusFraction =
          MIN_SPREAD_FRACTION + random.nextFloat() * (MAX_SPREAD_FRACTION - MIN_SPREAD_FRACTION);
      float angle = random.nextFloat() * (float) (2 * Math.PI);
      float delay =
          BASE_STAGGER_SECONDS * (i + 1) + random.nextFloat() * STAGGER_JITTER_SECONDS;
      bursts.add(new Burst(radiusFraction, angle, delay));
    }
    return bursts;
  }
}
