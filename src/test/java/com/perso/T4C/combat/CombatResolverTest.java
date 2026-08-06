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

    /** Creatures.cpp:174 - AC >= 100000 marks an invulnerable unit. */
    @Test
    void sentinelArmorClassMakesTheTargetImmuneWhileStillBeingHit() {
        CombatProfile attacker = profile(false, 0d);
        CombatProfile target = profile(true, 1_000_000d);

        CombatResult result = CombatResolver.resolve(
                new PhysicalAttackRequest(attacker, target, 50, 0, false), MINIMUM_ROLL);

        assertTrue(result.hit());
        assertEquals(0, result.damage());
    }

    /**
     * ArmorPenetration.cpp rebuilds Strike as TrueStrike + dNewAC + dBoost, so the
     * restored AC cancels the later subtraction instead of lowering it.
     */
    @Test
    void armorPenetrationCancelsTheArmorSubtractionInsteadOfReducingIt() {
        CombatProfile attacker = new CombatProfile(1, 10, 10, 10, 10, 10, 10, 0d,
                false, false, false, Map.of("armor_penetration", 100));
        CombatProfile target = profile(true, 8d);

        CombatResult result = CombatResolver.resolve(
                new PhysicalAttackRequest(attacker, target, 20, 0, false), MINIMUM_ROLL);

        assertTrue(result.hit());
        assertTrue(result.armorPenetration());
        // trueStrike=20, restored=100/113.2*8=7.06, boost=100/200*(20/3.05)=3.27
        // strike = 20 + 7.06 + 3.27 - 8 = 22.33 -> 22
        assertEquals(22, result.damage());
    }

    private static CombatProfile profile(boolean stunned, double armor) {
        return new CombatProfile(1, 10, 10, 10, 10, 10, 10, armor,
                stunned, false, false, Map.of());
    }
}
