package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.CharacterProgress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OnlineHudTextTest {
  private static CharacterProgress progress(int mana, int maxMana) {
    return new CharacterProgress(
        1, "mage", "male", new CharacterProgress.Attributes(10, 10, 10, 10, 10),
        new CharacterProgress.Vitals(27, 40, mana, maxMana), new CharacterProgress.Advancement(3, 0, 0, 0, 0),
        List.of(), Map.of(), Map.of(), Set.of(), Map.of(), List.of(), 0, new CharacterProgress.Respawn(0, 1, 1),
        List.of(), Map.of(), 0, null);
  }

  @Test
  void lineShowsCurrentAndMaximumMana() {
    String line = OnlineHudText.progressLine(progress(8, 20), 1500, 200);
    assertTrue(line.contains("Mana 8 / 20"), line);
    assertTrue(line.contains("HP 27 / 40") && line.contains("Level 3") && line.contains("Gold 200"), line);
    assertTrue(line.indexOf("HP") < line.indexOf("Mana") && line.indexOf("Mana") < line.indexOf("Level"), line);
  }

  @Test
  void characterWithoutManaShowsZeroOfZero() {
    assertTrue(OnlineHudText.progressLine(progress(0, 0), 0, 0).contains("Mana 0 / 0"));
  }

  @Test
  void lineStaysWellInsideTheWindowForLargeNumbers() {
    String line = OnlineHudText.progressLine(progress(9999, 9999), 2_000_000_000L, 2_000_000_000L);
    assertTrue(line.length() <= 90, line.length() + ": " + line);
  }
}
