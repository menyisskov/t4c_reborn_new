package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.online.model.CharacterProgress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProgressEditsTest {
  private static CharacterProgress base() {
    return new CharacterProgress(CharacterProgress.CURRENT_VERSION, "paladin", "male",
        new CharacterProgress.Attributes(60, 60, 60, 60, 60),
        new CharacterProgress.Vitals(40, 50, 20, 30),
        new CharacterProgress.Advancement(3, 0, 0, 0, 0), List.of(), Map.of(), Map.of(),
        Set.of("spell.heal_light"), Map.of("quest.a.status", 1), List.of(), 5,
        new CharacterProgress.Respawn(0, 10, 10), List.of(), Map.of("attack", 100L), 0, null);
  }

  @Test
  void eachEditReplacesOnlyItsSection() {
    CharacterProgress original = base();
    var vitals = new CharacterProgress.Vitals(50, 50, 30, 30);
    CharacterProgress edited = ProgressEdits.withVitals(original, vitals);
    assertEquals(vitals, edited.vitals());
    assertEquals(original.spells(), edited.spells());
    assertEquals(original.questFlags(), edited.questFlags());
    assertEquals(original.cooldowns(), edited.cooldowns());

    assertEquals(Map.of("quest.a.status", 2),
        ProgressEdits.withFlags(original, Map.of("quest.a.status", 2)).questFlags());
    assertEquals(Set.of(), ProgressEdits.withSpells(original, Set.of()).spells());
    assertEquals(Map.of(), ProgressEdits.withCooldowns(original, Map.of()).cooldowns());
  }
}
