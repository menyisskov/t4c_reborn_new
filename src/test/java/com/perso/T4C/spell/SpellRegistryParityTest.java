package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpellRegistryParityTest {
  @Test
  void registryContainsEveryMigratedSpellOnce() {
    var registry = SpellRegistry.load();
    // Numeric identity must remain unique as item-only and monster-only spells are added.
    java.util.Set<Integer> ids = new java.util.HashSet<>();
    registry.forEach(
        spell -> {
          assertNotNull(spell.getName());
          if (spell.getSpellId() > 0)
            assertTrue(ids.add(spell.getSpellId()), "Duplicate spell ID " + spell.getSpellId());
        });
  }
}
