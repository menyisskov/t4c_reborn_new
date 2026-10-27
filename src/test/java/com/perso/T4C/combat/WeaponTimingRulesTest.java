package com.perso.T4C.combat;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class WeaponTimingRulesTest {
  @Test void authoredDelayTakesPriorityAndFallbackPreservesLegacyCooldownConversion() {
    var authored = WeaponTimingRules.resolve("250", 2.0);
    assertEquals(250, WeaponTimingRules.cooldownMillis(authored.attacksPerSecond()));
    assertFalse(authored.invalidDelay());
    var invalid = WeaponTimingRules.resolve("0", 3.0);
    assertTrue(invalid.invalidDelay());
    assertEquals(333, WeaponTimingRules.cooldownMillis(invalid.attacksPerSecond()));
    assertEquals(1000, WeaponTimingRules.cooldownMillis(WeaponTimingRules.resolve(null, 0).attacksPerSecond()));
    assertEquals(1000, WeaponTimingRules.cooldownMillis(-1));
  }
}
