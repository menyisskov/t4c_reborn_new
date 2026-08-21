package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MakrshTeleportSpellVisualTest {

  @Test
  void originalMakrshTeleportSpellHasFlakVisualLikeWordOfRecall() {
    SpellData teleport = SpellRegistry.findById(10596);
    assertNotNull(teleport);
    assertEquals("Flak1-", teleport.getProjectileSpell());
    assertEquals("Flak1-", teleport.getImpactSpell());
    assertEquals("Explosion.wav", teleport.getSound());
    assertEquals(30012, teleport.getVisualEffect());
  }
}
