package com.perso.T4C.gui.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import com.perso.T4C.config.MacroBinding;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.teleport.NamedLocation;
import com.perso.T4C.teleport.NamedLocations;
import org.junit.jupiter.api.Test;

class TravelAndSpellScreensTest {
  @Test
  void everyUnlockableDestinationHasADescription() {
    for (NamedLocation location : NamedLocations.all()) {
      if (location.unlockZoneId() == null) continue;
      String key = "location." + LocationsScreen.slug(location) + ".desc";
      assertTrue(I18n.has(key), location.displayName() + " has no fast-travel description");
    }
  }

  @Test
  void spellDurationsReadAsTimes() {
    assertEquals(I18n.key("ui.spell_duration.instant"), SpellBook.formatSeconds(0));
    assertEquals("45 s", SpellBook.formatSeconds(45));
    assertEquals("5 min", SpellBook.formatSeconds(300));
    assertEquals("2 min 30 s", SpellBook.formatSeconds(150));
    assertEquals("1 h 30 min", SpellBook.formatSeconds(5400));
  }

  @Test
  void macroKeysShowTheirModifiers() {
    assertEquals("F1", MacrosScreen.keyName(Input.Keys.F1, 0));
    assertEquals(
        "Ctrl+Shift+A",
        MacrosScreen.keyName(Input.Keys.A, MacroBinding.MOD_CTRL | MacroBinding.MOD_SHIFT));
  }
}
