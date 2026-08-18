package com.perso.T4C.death;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.CollisionType;
import org.junit.jupiter.api.Test;

class DeathPenaltyServiceTest {
  @Test
  void outdoorSafeHavenCancelsEveryPenalty() {
    assertTrue(DeathPenaltyService.isSafeHaven(CollisionType.SAFE_HAVEN.getValue()));
  }

  @Test
  void indoorSafeHavenCancelsEveryPenalty() {
    assertTrue(DeathPenaltyService.isSafeHaven(CollisionType.INDOOR_SAFE_HAVEN.getValue()));
  }

  @Test
  void ordinaryGroundStillAppliesPenalties() {
    assertFalse(DeathPenaltyService.isSafeHaven(CollisionType.NONE.getValue()));
    assertFalse(DeathPenaltyService.isSafeHaven(CollisionType.ABSOLUTE.getValue()));
  }
}
