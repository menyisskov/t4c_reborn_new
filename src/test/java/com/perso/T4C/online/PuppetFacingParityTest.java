package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterRegistry;
import org.junit.jupiter.api.Test;

/**
 * The puppet monster turns toward each authoritative move with the same rule as the text client's
 * {@link OnlineCreatureMotion}: feeding both the same positions gives the same facing and moving
 * state at every step.
 */
class PuppetFacingParityTest {
  @Test
  void aPuppetFacesLikeTheOnlineCreatureMotionAtEveryStep() throws Exception {
    BaseMonster puppet = MonsterRegistry.create(MonsterRegistry.findByName("Skeleton"), 0f, 0f);
    puppet.setPuppet(true);
    OnlineCreatureMotion motion = new OnlineCreatureMotion();
    float[][] path = {
      {320, 160}, {352, 160}, {352, 192}, {320, 224}, {320, 224}, {288, 192}, {288, 160}, {320, 160}
    };
    for (float[] point : path) {
      motion.snapshot(point[0], point[1]);
      puppet.puppetPlace(point[0], point[1]);
      assertEquals(motion.angle(), puppet.getMovement().getCurrentAngle());
      assertEquals(motion.flipX(), puppet.getMovement().isFlipX());
      assertEquals(motion.moving(), puppet.getMovement().isMoving());
    }
  }
}
