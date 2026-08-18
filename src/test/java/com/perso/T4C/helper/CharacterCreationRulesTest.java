package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class CharacterCreationRulesTest {
  @Test
  void validatesAndNormalizesClassicCharacterNames() {
    assertEquals("Jean Luc", CharacterCreationRules.normalizeName("  jean   Luc "));
    assertTrue(CharacterCreationRules.isValidName("Jean Luc"));
    assertTrue(CharacterCreationRules.isValidName("D'Artagnan"));
    assertFalse(CharacterCreationRules.isValidName("A"));
    assertFalse(CharacterCreationRules.isValidName("Jean  Luc"));
    assertFalse(CharacterCreationRules.isValidName("Jean42"));
    assertTrue(CharacterCreationRules.isValidName("Abcdefgh"));
    assertFalse(CharacterCreationRules.isValidName("Abcdefghi"));
  }

  @Test
  void questionnaireAffinityBiasesTheMatchingAttribute() {
    CharacterCreationRules.Stats neutral =
        CharacterCreationRules.roll(new int[] {0, 0, 0, 0, 0}, new Random(1234));
    CharacterCreationRules.Stats strengthBiased =
        CharacterCreationRules.roll(new int[] {4, 0, 0, 0, 0}, new Random(1234));
    assertEquals(neutral.strength() + 8, strengthBiased.strength());
    assertEquals(neutral.endurance(), strengthBiased.endurance());
    assertTrue(neutral.maxHp() > 0);
    assertTrue(neutral.maxMana() > 0);
  }
}
