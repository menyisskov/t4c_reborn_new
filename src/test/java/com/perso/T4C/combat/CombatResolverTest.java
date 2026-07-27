package com.perso.T4C.combat;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatResolverTest {
    private static final RandomGenerator MINIMUM_ROLL = new RandomGenerator() {
        @Override
        public long nextLong() {
            return 0L;
        }
    };

    @Test
    void equalOpposedRollsMissAnActiveTarget() {
        CombatProfile attacker = profile(false, 0d);
        CombatProfile target = profile(false, 0d);

        CombatResult result = CombatResolver.resolve(
                new PhysicalAttackRequest(attacker, target, 10, 0, false), MINIMUM_ROLL);

        assertFalse(result.hit());
        assertEquals(0, result.damage());
        assertEquals(0, result.precision());
    }

    @Test
    void stunnedTargetIsHitAndTakesOriginalDamageMultiplierAfterArmor() {
        CombatProfile attacker = profile(false, 0d);
        CombatProfile target = profile(true, 2d);

        CombatResult result = CombatResolver.resolve(
                new PhysicalAttackRequest(attacker, target, 10, 0, false), MINIMUM_ROLL);

        assertTrue(result.hit());
        assertEquals(13, result.damage());
    }

    private static CombatProfile profile(boolean stunned, double armor) {
        return new CombatProfile(1, 10, 10, 10, 10, 10, 10, armor,
                stunned, false, false, Map.of());
    }
}
