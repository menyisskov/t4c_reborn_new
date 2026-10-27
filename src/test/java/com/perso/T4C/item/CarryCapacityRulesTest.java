package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CarryCapacityRulesTest {
  @Test
  void capacityIsInclusiveAtTheLimitAndNeverNegative() {
    long maximum = CarryCapacityRules.maximumWeight(550);
    assertEquals(1000L, maximum);
    assertEquals(0L, CarryCapacityRules.maximumWeight(-5));
    assertTrue(CarryCapacityRules.canAdd(maximum - 1, 1, maximum), "exactly at the limit is allowed");
    assertFalse(CarryCapacityRules.canAdd(maximum, 1, maximum), "one unit over is refused");
  }

  @Test
  void weightSumsKnownKeysAndSkipsUnknownOnes() {
    long total = CarryCapacityRules.weightOf(List.of("a", "missing", "b"),
        key -> switch (key) { case "a" -> 3L; case "b" -> -4L; default -> 0L; });
    assertEquals(3L, total);
  }
}
