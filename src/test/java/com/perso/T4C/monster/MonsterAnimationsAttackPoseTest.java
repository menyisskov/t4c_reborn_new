package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterAnimations;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MonsterAnimationsAttackPoseTest {
  @Test
  void castingPlaysAGestureAndReturnsToIdleWithoutMovement() throws GameException {
    MonsterAnimations animations = new MonsterAnimations(null, null, null, null, null);
    animations.startCast();
    assertTrue(animations.isAttacking());
    animations.update(animations.getAttackDuration(), false);
    assertFalse(animations.isAttacking());
    assertFalse(animations.isHoldingAttackPose());
    animations.startAttack();
    animations.update(animations.getAttackDuration(), false);
    assertTrue(animations.isHoldingAttackPose(), "Melee keeps its existing final-pose behavior");
  }

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

  @Test void sourceFrameEvidenceUsesTheRenderedPatternGrammarWithoutTextures() {
    assertTrue(MonsterAnimations.hasFirstAttackFrameName("GoblinA#i",Set.of("goblina000-a")));
    assertTrue(MonsterAnimations.hasFirstAttackFrameName("HorseA@03",Set.of("horsea01")));
    assertFalse(MonsterAnimations.hasFirstAttackFrameName("GoblinA#i",Set.of("goblinb-a")));
    assertFalse(MonsterAnimations.hasFirstAttackFrameName(null,Set.of("goblina000-a")));
  }
}
