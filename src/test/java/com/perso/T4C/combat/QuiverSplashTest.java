package com.perso.T4C.combat;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuiverSplashTest {
  @Test
  void capsTotalTargetsAndSnapshotsNearestEligibleNeighboursBeforeDeaths() {
    var candidates = new ArrayList<>(List.of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0));
    var profile = new QuiverSplash(8, 8, 45);
    var selected = profile.select(true, 100, candidates, 0, n -> n != 2, Integer::doubleValue);
    assertEquals(List.of(1, 3, 4, 5, 6, 7, 8), selected);
    candidates.clear();
    assertEquals(7, selected.size());
    assertEquals(
        List.of(1, 3),
        new QuiverSplash(8, 3, 15).select(true, 100, selected, 0, n -> true, Integer::doubleValue));
  }

  @Test
  void requiresAHitAndPositiveDamageAndScalesWithoutRecursion() {
    var profile = new QuiverSplash(1, 3, 15);
    assertTrue(
        profile.select(false, 100, List.of(1), 0, n -> true, Integer::doubleValue).isEmpty());
    assertTrue(profile.select(true, 0, List.of(1), 0, n -> true, Integer::doubleValue).isEmpty());
    assertTrue(profile.select(true, 100, List.of(2), 0, n -> true, Integer::doubleValue).isEmpty());
    assertEquals(15, profile.damage(100));
    assertEquals(1, profile.damage(1));
    assertEquals(0, profile.damage(0));
    assertThrows(IllegalArgumentException.class, () -> new QuiverSplash(1, 9, 15));
  }
}
