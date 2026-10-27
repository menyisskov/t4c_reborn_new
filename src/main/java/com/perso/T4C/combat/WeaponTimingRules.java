package com.perso.T4C.combat;

import com.perso.T4C.helper.DiceFormula;

/** Shared weapon timing over trusted content; clocks and scheduling belong to each runtime. */
public final class WeaponTimingRules {
  private WeaponTimingRules() {}

  public record Speed(double attacksPerSecond, boolean invalidDelay) {}

  public static Speed resolve(String delayFormula, double configuredSpeed) {
    boolean invalidDelay = false;
    if (delayFormula != null && !delayFormula.isBlank()) {
      int delayMillis = DiceFormula.of(delayFormula).roll();
      if (delayMillis > 0) return new Speed(1000d / delayMillis, false);
      invalidDelay = true;
    }
    return new Speed(configuredSpeed <= 0 ? 1.0 : configuredSpeed, invalidDelay);
  }

  public static long cooldownMillis(double attacksPerSecond) {
    double speed = attacksPerSecond <= 0 ? 1.0 : attacksPerSecond;
    return (long) (1000.0 / speed);
  }
}
