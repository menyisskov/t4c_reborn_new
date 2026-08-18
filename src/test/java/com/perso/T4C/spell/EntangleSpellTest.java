package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class EntangleSpellTest {
  @Test
  void catalogueContainsGonMovementExhaustEffect() {
    SpellData effect =
        SpellRegistry.load().stream()
            .filter(spell -> spell.getSpellId() == 10349)
            .findFirst()
            .orElse(null);
    assertNotNull(effect);
    assertEquals("1750", effect.getPhysicalExhaustion());
  }
}
