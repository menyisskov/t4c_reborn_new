package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.CharacterCreationRules;
import com.perso.T4C.helper.CharacterStartingKit;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OnlineCharacterCreatorTest {
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
