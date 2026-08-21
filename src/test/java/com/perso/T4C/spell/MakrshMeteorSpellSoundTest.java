package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MakrshMeteorSpellSoundTest {

  @Test
  void originalMakrshMeteorSpellUsesEnergyBallThenMeteorSounds() {
    SpellData meteor = SpellRegistry.findById(10710);
    assertNotNull(meteor);
    assertEquals("64kSpellEnergyBall-", meteor.getProjectileSpell());
    assertEquals("64kSpellMeteor-", meteor.getImpactSpell());
    assertEquals("Healing.wav", meteor.getSound());
    assertEquals("Meteor.wav", meteor.getSoundImpact());
    assertEquals(30070, meteor.getVisualEffect());
    assertEquals(30102, meteor.getVisualEffectTarget());
  }
}
