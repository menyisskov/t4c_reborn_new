package com.perso.T4C.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CombatVitalsTest {
  @Test
  void manaDrainsClockwiseAndTheFullHorseshoeKeepsItsBottomOpening() {
    float[] mesh = new float[4096];
    assertEquals(0, CombatVitals.writeManaArc(mesh, 0, 0, 0, 1));
    int half = CombatVitals.writeManaArc(mesh, 0, 0, .5f, 1);
    assertTrue(half > 0);
    // Half of the horseshoe reaches the top; screen Y increases downward.
    assertEquals(CombatVitals.SIZE / 2f, mesh[half - 5], .1f);
    assertEquals(0, mesh[half - 4], .1f);
    int full = CombatVitals.writeManaArc(mesh, 0, 0, 1, 1);
    assertTrue(full > half);
    // The full track begins below-left and ends below-right, leaving the number gap open.
    assertTrue(mesh[0] < CombatVitals.SIZE / 2f && mesh[1] > CombatVitals.SIZE / 2f);
    assertTrue(mesh[full - 5] > CombatVitals.SIZE / 2f && mesh[full - 4] > CombatVitals.SIZE / 2f);
    for (int i = 0; i < full; i += 5) {
      assertTrue(mesh[i + 3] >= 0 && mesh[i + 3] <= 1, "Arc samples outside the texture");
      assertTrue(mesh[i + 4] >= 0 && mesh[i + 4] <= 1, "Arc samples outside the texture");
    }
  }
}
