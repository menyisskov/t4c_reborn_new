package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OnlineCreatureMotionTest {
  @Test
  void authoritativeMovementFacesWalksAndStopsWithoutMovingTheStoredPosition() {
    var motion = new OnlineCreatureMotion();
    motion.snapshot(100, 100);
    assertFalse(motion.moving());
    assertEquals("000", motion.angle());

    motion.snapshot(116, 116);
    assertTrue(motion.moving());
    assertEquals("045", motion.angle());
    assertTrue(motion.flipX());
    assertEquals(116, motion.position().x);
    assertEquals(116, motion.position().y);

    motion.snapshot(116, 116);
    assertFalse(motion.moving());
    assertEquals("045", motion.angle());
    assertTrue(motion.flipX());

    motion.snapshot(100, 100);
    assertTrue(motion.moving());
    assertFalse(motion.flipX());
    motion.stop(); // Connection loss freezes walking but keeps the last facing.
    assertFalse(motion.moving());
    assertEquals("135", motion.angle());

    var nextRegion = new OnlineCreatureMotion();
    nextRegion.snapshot(100, 100);
    assertFalse(nextRegion.moving());
    assertEquals("000", nextRegion.angle());
  }
}
