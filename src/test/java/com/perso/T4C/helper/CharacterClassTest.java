package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Guards the starting kits (T4C-0059). A class hands the player gear and spells straight into
 * their save file, bypassing the shop and the trainer, so nothing else would catch a kit item the
 * class can't actually wear or a spell it couldn't have learned - it would just silently sit
 * unusable in a brand-new character's slots.
 */
class CharacterClassTest {
  @BeforeEach
  void loadJsonItems() {
    // Starter kits can reference JSON-authored gear, which only the game bootstrap registers.
    ItemJsonLoader.loadAndRegister("assets/items");
  }

  @AfterEach
  void unloadJsonItems() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  @Test
  void everyClassCanWearItsOwnStartingKit() {
    for (CharacterClass characterClass : CharacterClass.values()) {
      CharacterCreationRules.Stats stats =
          CharacterCreationRules.roll(characterClass, new Random(1));
      for (Map.Entry<BodyPart, String> entry : characterClass.startingEquipment().entrySet()) {
        ItemDefinition item = ItemDefinition.get(entry.getValue());
        assertNotNull(item, characterClass + " starts with unknown item " + entry.getValue());
        assertEquals(
            entry.getKey(),
            item.getBodyPart(),
            entry.getValue() + " is not worn in the slot " + characterClass + " puts it in");
        assertRequirementMet(characterClass, entry.getValue(), "endurance",
            stats.endurance(), item.getMinEnd());
        assertRequirementMet(characterClass, entry.getValue(), "strength",
            stats.strength(), item.getReqStr());
        assertRequirementMet(characterClass, entry.getValue(), "agility",
            stats.dexterity(), item.getReqAgi());
        assertRequirementMet(characterClass, entry.getValue(), "intelligence",
            stats.intelligence(), item.getMinInt());
        assertRequirementMet(characterClass, entry.getValue(), "wisdom",
            stats.wisdom(), item.getMinWis());
      }
      for (String stacked : characterClass.startingStacks()) {
        assertNotNull(
            ItemDefinition.get(stacked), characterClass + " stacks unknown item " + stacked);
      }
    }
  }

  @Test
  void everyStartingSpellExistsAndIsWithinTheClassesReach() {
    for (CharacterClass characterClass : CharacterClass.values()) {
      CharacterCreationRules.Stats stats =
          CharacterCreationRules.roll(characterClass, new Random(1));
      for (String key : characterClass.startingSpellKeys()) {
        SpellData spell = SpellRegistry.findByName(key);
        assertNotNull(spell, characterClass + " starts with unknown spell " + key);
        assertTrue(
            stats.intelligence() >= spell.getMinInt(),
            characterClass + " cannot learn " + key + ": needs " + spell.getMinInt() + " int");
        assertTrue(
            stats.wisdom() >= spell.getMinWis(),
            characterClass + " cannot learn " + key + ": needs " + spell.getMinWis() + " wis");
      }
    }
  }

  @Test
  void classesHaveDistinctIdsAndStatSpreads() {
    Set<String> ids = new HashSet<>();
    Set<String> spreads = new HashSet<>();
    for (CharacterClass characterClass : CharacterClass.values()) {
      assertTrue(ids.add(characterClass.id()), "Duplicate class id " + characterClass.id());
      String spread =
          characterClass.strengthBonus()
              + "/"
              + characterClass.enduranceBonus()
              + "/"
              + characterClass.dexterityBonus()
              + "/"
              + characterClass.wisdomBonus()
              + "/"
              + characterClass.intelligenceBonus();
      assertTrue(spreads.add(spread), characterClass + " duplicates another class's spread");
    }
  }

  private static void assertRequirementMet(
      CharacterClass characterClass, String item, String stat, int has, long needs) {
    assertTrue(
        has >= needs,
        characterClass + " cannot equip " + item + ": needs " + needs + " " + stat + ", has " + has);
  }
}
