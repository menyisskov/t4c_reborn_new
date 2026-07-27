package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonsterAnimationsAttackPoseTest {

    @Test
    void holdsLastAttackPoseUntilMovementOrNextAttack() throws GameException {
        MonsterAnimations animations = new MonsterAnimations(null, null, null, null, null);

        animations.startAttack();
        animations.update(0.5f, false);

        assertFalse(animations.isAttacking());
        assertTrue(animations.isHoldingAttackPose());

        animations.startAttack();
        assertTrue(animations.isAttacking());
        assertFalse(animations.isHoldingAttackPose());

        animations.update(0.5f, false);
        animations.update(1f / 60f, true);
        assertFalse(animations.isHoldingAttackPose());
    }
}
