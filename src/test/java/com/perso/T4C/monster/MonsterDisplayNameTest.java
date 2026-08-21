package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.i18n.I18n;
import org.junit.jupiter.api.Test;

class MonsterDisplayNameTest {

  @Test
  void celestialCobraUsesOriginalReadableName() {
    assertEquals("Celestial Cobra", I18n.key("monster.mobcelestialcobra"));
    assertEquals("Celestial Cobra", I18n.key("monster.celestialcobra"));
  }

  @Test
  void concatenatedMobIdsExpandToT4cNames() {
    assertEquals("Centaur Avenger", I18n.key("monster.centauravenger"));
    assertEquals("Crazed Nurse", I18n.key("monster.crazednurse"));
    assertEquals("Anthor the Mad", I18n.key("monster.anthorthemad"));
  }
}
