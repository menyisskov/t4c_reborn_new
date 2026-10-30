package com.perso.T4C.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class DayNightCycleTest {
  @Test
  void defaultCycleStartsAtSevenAm() {
    DayNightCycle cycle = new DayNightCycle();
    assertEquals(7f, cycle.getHour(), 0.0001f);
    assertFalse(cycle.isNight());
  }

  @Test
  void constructorArgumentIsHoursNotSeconds() {
    assertEquals(12f, new DayNightCycle(12f).getHour(), 0.0001f);
    assertEquals(13.5f, new DayNightCycle(13.5f).getHour(), 0.0001f);
    // A saved midnight stays midnight; only the no-saved-hour default is 7 am.
    assertEquals(0f, new DayNightCycle(0f).getHour(), 0.0001f);
  }

  @Test
  void outOfRangeHoursWrapIntoOneDay() {
    assertEquals(1f, new DayNightCycle(25f).getHour(), 0.0001f);
    assertEquals(23f, new DayNightCycle(-1f).getHour(), 0.0001f);
  }
}
