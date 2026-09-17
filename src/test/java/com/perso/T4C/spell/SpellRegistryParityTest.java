package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SpellRegistryParityTest {
  @Test
  void registryContainsEveryMigratedSpellOnce() {
    var registry = SpellRegistry.load();
    assertEquals(291, registry.size());
    registry.forEach(
        spell -> {
          assertNotNull(spell.getName());
        });
  }
}
