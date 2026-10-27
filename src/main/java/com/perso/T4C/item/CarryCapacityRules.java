package com.perso.T4C.item;

import java.util.Collection;
import java.util.function.ToLongFunction;

/** Pure carry-weight rules shared by offline inventory and online pickup. */
public final class CarryCapacityRules {
  private CarryCapacityRules() {}

  /** Asymptotic capacity from effective strength; negative strength carries nothing. */
  public static long maximumWeight(long effectiveStrength) {
    long strength = Math.max(0L, effectiveStrength);
    return strength * 2000L / (550L + strength);
  }

  /** Sum of non-negative weights. Callers return 0 for unknown keys, which are skipped. */
  public static long weightOf(Collection<String> keys, ToLongFunction<String> weightLookup) {
    long total = 0L;
    for (String key : keys) total += Math.max(0L, weightLookup.applyAsLong(key));
    return total;
  }

  /** Inclusive: a total exactly at the maximum is allowed, one unit more is refused. */
  public static boolean canAdd(long currentWeight, long itemWeight, long maximumWeight) {
    return currentWeight + Math.max(0L, itemWeight) <= maximumWeight;
  }
}
