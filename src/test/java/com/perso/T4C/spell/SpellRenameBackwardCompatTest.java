package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * T4C-0018 renamed five spells (Sentinel/Divine Veil/Clemancy/Undead Annihilation/Omega
 * Planetoids) that collided with real t4cfantasy.com spell names. Characters that learned them
 * under the old name still have the old "${spell.<old>}" string in their persisted
 * {@code player.spells} list (see e.g. {@code characters/627442f4-4f9d-4606-aa43-7c85edb47ad8.json}) -
 * {@link SpellRegistry#findByName} must keep resolving that old string to the renamed spell (same
 * {@code spellId}, new key) via its {@code canonicalAlias} table, or those characters silently
 * lose the spell from their spellbook and can no longer cast it.
 */
class SpellRenameBackwardCompatTest {

  @Test
  void oldSentinelKeyResolvesToLeywardBastion() {
    assertRenamedAlias("${spell.sentinel}", "${spell.leyward_bastion}", 99914);
  }

  @Test
  void oldDivineVeilKeyResolvesToVeilstoneAegis() {
    assertRenamedAlias("${spell.divine_veil}", "${spell.veilstone_aegis}", 99903);
  }

  @Test
  void oldClemancyKeyResolvesToWellspringMercy() {
    assertRenamedAlias("${spell.clemancy}", "${spell.wellspring_mercy}", 99902);
  }

  @Test
  void oldUndeadAnnihilationKeyResolvesToSunscour() {
    assertRenamedAlias("${spell.undead_annihilation}", "${spell.sunscour}", 99904);
  }

  @Test
  void oldOmegaPlanetoidsKeyResolvesToGravebreaker() {
    assertRenamedAlias("${spell.omega_planetoids}", "${spell.gravebreaker}", 99905);
  }

  private static void assertRenamedAlias(String oldKey, String newKey, int expectedSpellId) {
    SpellData byOldKey = SpellRegistry.findByName(oldKey);
    assertNotNull(byOldKey, "Old key " + oldKey + " should still resolve for existing characters");
    assertEquals(expectedSpellId, byOldKey.getSpellId());
    assertEquals(newKey, byOldKey.getName());
  }
}
