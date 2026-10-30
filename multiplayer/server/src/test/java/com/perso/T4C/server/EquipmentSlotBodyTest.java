package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.BodyPart;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * A live run (T4C-0331) took an Archer's quiver off: the slot name WEAPON2 has a digit and the /unequip body pattern allowed
 * only letters and underscores, so the server answered 400 and the client dropped the connection. Every real body part
 * must be a valid slot in both bodies, and malformed bodies must still be refused.
 */
class EquipmentSlotBodyTest {
  @Test
  void everyBodyPartNameIsAValidSlotInBothEquipmentBodies() {
    UUID item = UUID.fromString("00000000-0000-0000-0000-0000000000ab");
    for (BodyPart slot : BodyPart.values()) {
      assertTrue(PreviewServer.equipmentBodyValid(false, "12 3 " + slot.name()), "unequip " + slot);
      assertTrue(PreviewServer.equipmentBodyValid(true, "12 3 " + item + " " + slot.name()), "equip " + slot);
    }
    assertTrue(PreviewServer.equipmentBodyValid(false, "12 3 WEAPON2"));
  }

  @Test
  void malformedEquipmentBodiesAreStillRefused() {
    UUID item = UUID.fromString("00000000-0000-0000-0000-0000000000ab");
    assertFalse(PreviewServer.equipmentBodyValid(false, null));
    assertFalse(PreviewServer.equipmentBodyValid(false, "12 3 weapon"));
    assertFalse(PreviewServer.equipmentBodyValid(false, "12 3 2WEAPON"));
    assertFalse(PreviewServer.equipmentBodyValid(false, "12 3 WEAPON2 EXTRA"));
    assertFalse(PreviewServer.equipmentBodyValid(false, "12 3 THIS_SLOT_NAME_IS_TOO_LONG"));
    assertFalse(PreviewServer.equipmentBodyValid(true, "12 3 WEAPON2"));
    assertFalse(PreviewServer.equipmentBodyValid(true, "12 3 " + item));
    assertFalse(PreviewServer.equipmentBodyValid(true, "12 3 not-a-uuid WEAPON"));
  }
}
