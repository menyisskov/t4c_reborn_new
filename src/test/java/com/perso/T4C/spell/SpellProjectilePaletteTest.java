package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.helper.SpellBinaryIO;
import java.io.File;
import org.junit.jupiter.api.Test;

class SpellProjectilePaletteTest {
  @Test
  void spellBinaryKeepsOriginalGonEnergyBallPalettes() throws Exception {
    for (SpellData spell : SpellBinaryIO.read(new File("assets/spells/spells.bin"))) {
      assertEquals(
          SpellProjectilePalette.projectileFor(spell.getVisualEffect(), spell.getProjectileSpell()),
          spell.getProjectileSpell(),
          () ->
              "Wrong projectile palette for "
                  + spell.getName()
                  + " (visual "
                  + spell.getVisualEffect()
                  + ")");
    }
  }
}
