package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SpellProjectilePaletteTest {
  @Test
  void spellDefinitionsKeepOriginalGonEnergyBallPalettes() {
    for (SpellData spell : SpellRegistry.load()) {
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
