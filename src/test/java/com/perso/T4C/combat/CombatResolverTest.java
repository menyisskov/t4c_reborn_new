package com.perso.T4C.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class CombatResolverTest {
  private static final RandomGenerator MINIMUM_ROLL =
      new RandomGenerator() {
        @Override
        public long nextLong() {
          return 0L;
        }
      };

  @Test
  void equalOpposedRollsMissAnActiveTarget() {
    CombatProfile attacker = profile(false, 0d);
    CombatProfile target = profile(false, 0d);
    CombatResult result =
        CombatResolver.resolve(
            new PhysicalAttackRequest(attacker, target, 10, 0, false), MINIMUM_ROLL);
    assertFalse(result.hit());
    assertEquals(0, result.damage());
    assertEquals(0, result.precision());
  }

  @Test
  void stunnedTargetIsHitAndTakesOriginalDamageMultiplierAfterArmor() {
    CombatProfile attacker = profile(false, 0d);
    CombatProfile target = profile(true, 2d);
    CombatResult result =
        CombatResolver.resolve(
            new PhysicalAttackRequest(attacker, target, 10, 0, false), MINIMUM_ROLL);
    assertTrue(result.hit());
    assertEquals(13, result.damage());
  }

  @Test
  void sentinelArmorClassMakesTheTargetImmuneWhileStillBeingHit() {
    CombatProfile attacker = profile(false, 0d);
    CombatProfile target = profile(true, 1_000_000d);
    CombatResult result =
        CombatResolver.resolve(
            new PhysicalAttackRequest(attacker, target, 50, 0, false), MINIMUM_ROLL);
    assertTrue(result.hit());
    assertEquals(0, result.damage());
  }

  @Test
  void armorPenetrationCancelsTheArmorSubtractionInsteadOfReducingIt() {
    CombatProfile attacker =
        new CombatProfile(
            1, 10, 10, 10, 10, 10, 10, 0d, false, false, false, Map.of("armor_penetration", 100));
    CombatProfile target = profile(true, 8d);
    CombatResult result =
        CombatResolver.resolve(
            new PhysicalAttackRequest(attacker, target, 20, 0, false), MINIMUM_ROLL);
    assertTrue(result.hit());
    assertTrue(result.armorPenetration());
    assertEquals(22, result.damage());
  }

  private static CombatProfile profile(boolean stunned, double armor) {
    return new CombatProfile(1, 10, 10, 10, 10, 10, 10, armor, stunned, false, false, Map.of());
  }
}
