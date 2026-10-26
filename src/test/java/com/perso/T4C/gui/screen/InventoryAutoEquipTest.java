package com.perso.T4C.gui.screen;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.List;
import org.junit.jupiter.api.Test;

class InventoryAutoEquipTest {
  @Test
  void usableInventoryItemsToggleOneCanonicalMacroWhilePassiveEquipmentDoesNot() {
    var player = new Player();
    player.getInventory().add("item.gem_of_unstable_protection");
    player.getInventory().add("item.belt_of_unstable_protection");
    assertTrue(Inventory.toggleItemMacro(player, "gem_of_unstable_protection"));
    assertEquals(1, player.getMacros().size());
    assertTrue(player.getMacros().getFirst().isItem());
    assertEquals("item.gem_of_unstable_protection", player.getMacros().getFirst().getItemKey());
    assertTrue(Inventory.toggleItemMacro(player, "item.gem_of_unstable_protection"));
    assertTrue(player.getMacros().isEmpty());
    assertFalse(Inventory.toggleItemMacro(player, "belt_of_unstable_protection"));
    assertFalse(
        Inventory.toggleItemMacro(player, "healing_potion"), "Unowned items cannot be bound");
  }

  private static Player equippedForAnything() {
    Player player = new Player();
    player.setStrength(5000);
    player.setEndurance(5000);
    player.setDexterity(5000);
    player.setIntelligence(5000);
    player.setWisdom(5000);
    player.setSkillLevel("attack", 5000);
    return player;
  }

  @Test
  void routesArmorToItsOwnSlotAndReturnsThePreviousPieceToTheBackpack() {
    Player player = equippedForAnything();
    var armor =
        ItemRegistry.load().stream()
            .filter(item -> item.getBodyPart() == BodyPart.BODY)
            .limit(2)
            .toList();
    String previous = armor.getFirst().getKey(), replacement = armor.getLast().getKey();
    player.getEquippedItems().put(BodyPart.BODY, previous);
    player.getInventory().add(replacement);

    assertTrue(Inventory.equipAutomatically(player, replacement).success());
    assertEquals(replacement, player.getEquippedItems().get(BodyPart.BODY));
    assertEquals(List.of(previous), player.getInventory());
  }

  @Test
  void failedRequirementsAndNonEquipmentLeaveTheBackpackUnchanged() {
    Player player = equippedForAnything();
    var armor =
        ItemRegistry.load().stream()
            .filter(item -> item.getBodyPart() == BodyPart.BODY && item.getMinEnd() > 0)
            .findFirst()
            .orElseThrow();
    var nonEquipment =
        ItemRegistry.load().stream()
            .filter(item -> item.getBodyPart() == null)
            .findFirst()
            .orElseThrow();
    player.setEndurance((int) armor.getMinEnd() - 1);
    player.setInventory(List.of(armor.getKey(), nonEquipment.getKey()));

    assertEquals(
        InventoryService.Failure.REQUIREMENTS_NOT_MET,
        Inventory.equipAutomatically(player, armor.getKey()).failure());
    assertEquals(
        InventoryService.Failure.WRONG_SLOT,
        Inventory.equipAutomatically(player, nonEquipment.getKey()).failure());
    assertTrue(player.getEquippedItems().isEmpty());
    assertEquals(List.of(armor.getKey(), nonEquipment.getKey()), player.getInventory());
  }

  @Test
  void ringsPreferTheEmptySecondRingSlot() {
    Player player = equippedForAnything();
    var ring =
        ItemRegistry.load().stream()
            .filter(item -> item.getBodyPart() == BodyPart.RING1)
            .findFirst()
            .orElseThrow();
    player.getEquippedItems().put(BodyPart.RING1, ring.getKey());
    player.getInventory().add(ring.getKey());

    assertTrue(Inventory.equipAutomatically(player, ring.getKey()).success());
    assertEquals(ring.getKey(), player.getEquippedItems().get(BodyPart.RING1));
    assertEquals(ring.getKey(), player.getEquippedItems().get(BodyPart.RING2));
    assertTrue(player.getInventory().isEmpty());
  }
}
