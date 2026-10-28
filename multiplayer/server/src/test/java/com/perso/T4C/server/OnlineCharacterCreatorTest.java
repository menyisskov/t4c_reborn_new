package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.CharacterCreationRules;
import com.perso.T4C.helper.CharacterStartingKit;
import com.perso.T4C.online.model.OnlineCharacterCreation;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OnlineCharacterCreatorTest {
  @Test void archerStartsOnlineWithItsSharedBowQuiverAndArrowKit() {
    var type = CharacterClass.ARCHER;
    assertTrue(OnlineCharacterCreation.CLASSES.contains(type.id()));
    var prepared = OnlineCharacterCreator.prepare(type, "male", false, new Random(7));
    var progress = prepared.progress();
    assertEquals(type.id(), progress.characterClass());
    assertEquals(type.startingSpellKeys().size(), progress.spells().size());
    type.startingEquipment().forEach((slot, key) -> {
      var id = progress.equipment().get(slot.name());
      assertTrue(progress.inventory().stream().anyMatch(item -> item.id().equals(id) && item.key().equals(key)),
          slot.name());
    });
    assertEquals(CharacterClass.STACKED_KIT_ITEM_COUNT, progress.inventory().stream()
        .filter(item -> item.key().equals("item.wooden_arrow")).count());
  }

  @Test void casterClassesReceiveTheirSharedStartingKitsAndPlayableSpells() {
    Map<String, CharacterClass> classes = Map.of(
        "cleric", CharacterClass.CLERIC,
        "healer", CharacterClass.HEALER,
        "mage", CharacterClass.MAGE,
        "battlemage", CharacterClass.BATTLE_MAGE);
    assertTrue(OnlineCharacterCreation.CLASSES.containsAll(classes.keySet()));
    for (var entry : classes.entrySet()) {
      var type = entry.getValue();
      var prepared = OnlineCharacterCreator.prepare(type, "male", false, new Random(7));
      var progress = prepared.progress();
      var expected = CharacterCreationRules.roll(type, new Random(7));
      assertEquals(expected.strength(), progress.attributes().strength(), entry.getKey());
      assertEquals(expected.intelligence(), progress.attributes().intelligence(), entry.getKey());
      assertEquals(expected.wisdom(), progress.attributes().wisdom(), entry.getKey());
      assertEquals(Set.copyOf(type.startingSpellKeys()), progress.spells(), entry.getKey());
      assertFalse(progress.spells().isEmpty(), entry.getKey());
      assertTrue(progress.spells().stream().allMatch(SpellCatalogV2.bundled()::castableKey), entry.getKey());
      type.startingEquipment().forEach((slot, key) -> {
        var id = progress.equipment().get(slot.name());
        assertTrue(progress.inventory().stream().anyMatch(item -> item.id().equals(id) && item.key().equals(key)),
            entry.getKey() + " " + slot);
      });
    }
  }

  @Test void preparesTheSharedClassKitAndRollWithoutDesktopStorage() {
    var type=CharacterClass.PALADIN;
    var expected=CharacterCreationRules.roll(type,new Random(7));
    var prepared=OnlineCharacterCreator.prepare(type,"female",true,new Random(7));
    var progress=prepared.progress();
    assertEquals(expected.strength(),progress.attributes().strength());
    assertEquals(expected.maxHp(),progress.vitals().maxHp());
    assertEquals(expected.maxMana(),progress.vitals().mana());
    assertEquals(CharacterStartingKit.EVIL_KARMA,progress.advancement().karma());
    assertEquals(CharacterStartingKit.SKILLS,progress.skills());
    assertEquals(Set.copyOf(type.startingSpellKeys()),progress.spells());
    type.startingEquipment().forEach((slot,key) -> {
      var id=progress.equipment().get(slot.name());
      assertTrue(progress.inventory().stream().anyMatch(item -> item.id().equals(id) && item.key().equals(key)));
    });
    assertEquals(CharacterStartingKit.TORCH_COUNT,progress.inventory().stream()
        .filter(item -> item.key().equals(CharacterStartingKit.TORCH)).count());
    assertTrue(prepared.gold()>=CharacterStartingKit.MIN_GOLD && prepared.gold()<=CharacterStartingKit.MAX_GOLD);
  }
}
