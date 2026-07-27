package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.player.BodyPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NPCAttackPoseTest {

    @Test
    void compositeNpcUsesPlayerFinalAttackPose() throws GameException {
        NPCAnimations animations = new NPCAnimations(
                BodyPart.BODY, "PupNakedBody",
                BodyPart.HEAD, "PupNakedHead",
                BodyPart.LEGS, "PupNakedLegs",
                BodyPart.FEET, "PupNakedFoot",
                BodyPart.LEFT_ARM, "PupNakedArmL",
                BodyPart.RIGHT_ARM, "PupNakedArmR",
                BodyPart.LEFT_HAND, "PupNakedHandL",
                BodyPart.RIGHT_HAND, "PupNakedHandR"
        );

        animations.startAttack("000");
        for (int i = 0; i < 200 && animations.isAttacking(); i++) {
            animations.update(1f / 60f, false);
        }

        assertFalse(animations.isAttacking());
        assertTrue(animations.isHoldingAttackPose());

        animations.update(1f / 60f, true);
        assertFalse(animations.isHoldingAttackPose());
    }
}
