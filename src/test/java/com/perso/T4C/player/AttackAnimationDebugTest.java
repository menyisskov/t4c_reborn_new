package com.perso.T4C.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttackAnimationDebugTest {

    @Test
    void debugAttackCycle() throws Exception {
        Player player = new Player(
                BodyPart.BODY, "PupNakedBody",
                BodyPart.HEAD, "PupNakedHead",
                BodyPart.HAIR, "PupNormalHair",
                BodyPart.LEGS, "PupNakedLegs",
                BodyPart.FEET, "PupNakedFoot",
                BodyPart.LEFT_ARM, "PupNakedArmL",
                BodyPart.RIGHT_ARM, "PupNakedArmR",
                BodyPart.LEFT_HAND, "PupNakedHandL",
                BodyPart.RIGHT_HAND, "PupNakedHandR"
        );
        player.setMapBounds(1_000, 1_000);
        PlayerAnimations anim = player.getAnimations();

        java.lang.reflect.Field partMapField = PlayerAnimations.class.getDeclaredField("partMap");
        partMapField.setAccessible(true);
        System.out.println("partMap: " + partMapField.get(anim));

        java.lang.reflect.Field attackAnimField = PlayerAnimations.class.getDeclaredField("attackAnimations");
        attackAnimField.setAccessible(true);
        java.util.Map<?, ?> attackAnimations = (java.util.Map<?, ?>) attackAnimField.get(anim);
        for (Object key : attackAnimations.keySet()) {
            java.util.Map<?, ?> byAngle = (java.util.Map<?, ?>) attackAnimations.get(key);
            for (Object angle : byAngle.keySet()) {
                java.util.List<?> frames = (java.util.List<?>) byAngle.get(angle);
                if (!frames.isEmpty()) {
                    System.out.println("base=" + key + " angle=" + angle + " frameCount=" + frames.size());
                }
            }
        }

        player.attack(player.getMovement(), false);
        System.out.println("After startAttack: isAttacking=" + anim.isAttacking());

        for (int i = 0; i < 200; i++) {
            anim.update(1f / 60f, false);
            System.out.println("tick " + i + " isAttacking=" + anim.isAttacking());
            if (!anim.isAttacking()) {
                System.out.println("Attack ended at tick " + i);
                break;
            }
        }

        assertFalse(anim.isAttacking());
        assertTrue(anim.isHoldingMeleeAttackPose());

        anim.update(1f / 60f, true);
        assertFalse(anim.isHoldingMeleeAttackPose());

        player.attack(player.getMovement(), false);
        assertFalse(anim.isHoldingMeleeAttackPose());
        assertTrue(anim.isAttacking());
    }
}
