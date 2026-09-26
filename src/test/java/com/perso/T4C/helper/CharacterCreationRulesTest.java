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
  void everyClassStartsFromTheSameBaseAndSpendsTheSameBonus() {
    for (CharacterClass characterClass : CharacterClass.values()) {
      CharacterCreationRules.Stats stats =
          CharacterCreationRules.roll(characterClass, new Random(1234));
      int total =
          stats.strength()
              + stats.endurance()
              + stats.dexterity()
              + stats.wisdom()
              + stats.intelligence();
      assertEquals(
          5 * CharacterCreationRules.BASE_ATTRIBUTE + CharacterCreationRules.CLASS_BONUS_POINTS,
          total,
          characterClass + " spends a different number of points than the others");
      assertEquals(
          CharacterCreationRules.CLASS_BONUS_POINTS,
          characterClass.totalBonus(),
          characterClass + "'s bonus spread does not add up");
      assertTrue(
          stats.strength() >= CharacterCreationRules.BASE_ATTRIBUTE
              && stats.endurance() >= CharacterCreationRules.BASE_ATTRIBUTE
              && stats.dexterity() >= CharacterCreationRules.BASE_ATTRIBUTE
              && stats.wisdom() >= CharacterCreationRules.BASE_ATTRIBUTE
              && stats.intelligence() >= CharacterCreationRules.BASE_ATTRIBUTE,
          characterClass + " drops an attribute below the base");
      assertTrue(stats.maxHp() > 0 && stats.maxMana() > 0);
    }
  }

  @Test
  void aClassRollIsDeterministicApartFromHealthAndMana() {
    CharacterCreationRules.Stats first =
        CharacterCreationRules.roll(CharacterClass.HEALER, new Random(1));
    CharacterCreationRules.Stats second =
        CharacterCreationRules.roll(CharacterClass.HEALER, new Random(99));
    assertEquals(first.strength(), second.strength());
    assertEquals(first.endurance(), second.endurance());
    assertEquals(first.dexterity(), second.dexterity());
    assertEquals(first.wisdom(), second.wisdom());
    assertEquals(first.intelligence(), second.intelligence());
  }

  @Test
  void rerollingChangesOnlyHealthAndMana() {
    CharacterCreationRules.Stats rolled =
        CharacterCreationRules.roll(CharacterClass.WARRIOR, new Random(7));
    boolean vitalsMoved = false;
    for (int seed = 0; seed < 50; seed++) {
      CharacterCreationRules.Stats rerolled =
          CharacterCreationRules.rerollVitals(rolled, new Random(seed));
      assertEquals(rolled.strength(), rerolled.strength());
      assertEquals(rolled.endurance(), rerolled.endurance());
      assertEquals(rolled.dexterity(), rerolled.dexterity());
      assertEquals(rolled.wisdom(), rerolled.wisdom());
      assertEquals(rolled.intelligence(), rerolled.intelligence());
      vitalsMoved |= rerolled.maxHp() != rolled.maxHp() || rerolled.maxMana() != rolled.maxMana();
    }
    assertTrue(vitalsMoved, "Reroll never changed health or mana");
  }

  @Test
  void classIdsRoundTrip() {
    for (CharacterClass characterClass : CharacterClass.values()) {
      assertEquals(characterClass, CharacterClass.byId(characterClass.id()));
    }
  }
}
