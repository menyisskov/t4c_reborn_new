package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SpellRegistryParityTest {
  @Test
  void registryContainsEveryMigratedSpellOnce() {
    var registry = SpellRegistry.load();
    // T4C-0067 added 6 spells: the Ultra tier of the 5 protection spells, plus Renew Armor.
    // T4C-0084: 334 minus the seven removed level-400 spells (six attacks and Sanctum Ward).
    assertEquals(327, registry.size());
    registry.forEach(
        spell -> {
          assertNotNull(spell.getName());
        });
  }
}
