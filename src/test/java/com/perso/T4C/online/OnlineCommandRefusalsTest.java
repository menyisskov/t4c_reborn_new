package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.i18n.I18n;
import java.util.List;
import org.junit.jupiter.api.Test;

class OnlineCommandRefusalsTest {
  @Test
  void mapsEquipAndUnequipReasonsToReadableKeysAndFallsBackToOneGenericRefusal() {
    assertEquals("online.equip.requirements", OnlineCommandRefusals.messageKey("/equip", "requirements"));
    assertEquals("online.equip.conflict", OnlineCommandRefusals.messageKey("/equip", "equipment_conflict"));
    assertEquals("online.unequip.not_equipped", OnlineCommandRefusals.messageKey("/unequip", "item_not_equipped"));
    assertEquals("online.equip.refused", OnlineCommandRefusals.messageKey("/equip", "revision"));
    assertEquals("online.equip.refused", OnlineCommandRefusals.messageKey("/unequip", null));
  }

  @Test
  void keepsPickupRefusalsAndMapsInteractionRefusals() {
    assertEquals("online.loot.out_of_range", OnlineCommandRefusals.messageKey("/pickup", "out_of_range"));
    assertEquals("online.loot.refused", OnlineCommandRefusals.messageKey("/pickup", null));
    assertEquals("online.npc.unavailable", OnlineCommandRefusals.messageKey("/interact", "npc_unavailable"));
    assertEquals("online.npc.refused", OnlineCommandRefusals.messageKey("/interact", "busy"));
  }

  @Test
  void commandsWithoutPlayerFacingRefusalsReturnNothing() {
    assertNull(OnlineCommandRefusals.messageKey("/attack", "busy"));
  }

  @Test
  void everyMappedKeyExistsInTheEnglishCatalogue() {
    for (var path : List.of("/equip", "/unequip", "/interact", "/pickup")) {
      for (var reason : List.of("requirements", "equipment_conflict", "wrong_slot", "item_not_equipped",
          "npc_unavailable", "out_of_range", "loot_unavailable", "anything-unknown")) {
        var key = OnlineCommandRefusals.messageKey(path, reason);
        assertTrue(I18n.has(key), key);
      }
    }
  }
}
