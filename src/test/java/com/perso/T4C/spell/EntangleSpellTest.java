package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.perso.T4C.helper.SpellBinaryIO;
import java.io.File;
import org.junit.jupiter.api.Test;

class EntangleSpellTest {
  @Test
  void catalogueContainsGonMovementExhaustEffect() throws Exception {
    SpellData effect =
        SpellBinaryIO.read(new File("assets/spells/spells.bin")).stream()
            .filter(spell -> spell.getSpellId() == 10349)
            .findFirst()
            .orElse(null);
    assertNotNull(effect);
    assertEquals("1750", effect.getPhysicalExhaustion());
  }
}
