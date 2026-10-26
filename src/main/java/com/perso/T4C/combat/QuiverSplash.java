package com.perso.T4C.combat;

/** Damage-only arrow burst; the primary target occupies one of the target slots. */
public record QuiverSplash(double radius, int targets, int damagePercent) {
  public QuiverSplash {
    if (!Double.isFinite(radius)
        || radius <= 0
        || targets < 2
        || targets > 8
        || damagePercent <= 0
        || damagePercent > 100) throw new IllegalArgumentException("Invalid quiver splash profile");
  }

  public <T> java.util.List<T> select(
      boolean hit,
      int primaryDamage,
      java.util.Collection<T> candidates,
      T primary,
      java.util.function.Predicate<T> eligible,
      java.util.function.ToDoubleFunction<T> distance) {
    if (!hit || primaryDamage <= 0) return java.util.List.of();
    return com
        .perso
        .T4C
        .spell
        .SpellAreaTargeting
        .nearest(
            candidates,
            target ->
                target != primary
                    && eligible.test(target)
                    && distance.applyAsDouble(target) <= radius,
            distance,
            1)
        .stream()
        .limit(targets - 1)
        .toList();
  }

  public int damage(int primaryDamage) {
    return primaryDamage <= 0 ? 0 : Math.max(1, (int) ((long) primaryDamage * damagePercent / 100));
  }
}
