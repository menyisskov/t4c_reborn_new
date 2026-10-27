package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SpellKeysTest {
  @Test
  void placeholderBecomesItsContentKey() {
    assertEquals("spell.renew_armor", SpellKeys.normalize("${spell.renew_armor}"));
  }

  @Test
  void repeatedSpellPrefixCollapses() {
    assertEquals("spell.x", SpellKeys.normalize("${spell.spell_spell_x}"));
  }

  @Test
  void plainNamesAndNormalizedKeysPassThroughUnchanged() {
    assertEquals("Barrier", SpellKeys.normalize("Barrier"));
    assertEquals("spell.heal_light", SpellKeys.normalize("spell.heal_light"));
    assertNull(SpellKeys.normalize(null));
  }
}
