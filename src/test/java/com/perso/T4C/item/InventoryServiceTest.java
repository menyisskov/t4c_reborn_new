package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import java.util.List;
import org.junit.jupiter.api.Test;

class InventoryServiceTest {
  @Test
  void warriorWeaponStylesExchangeBothHandsAndPreserveTheShieldOnSave() throws Exception {
    var tier = WarriorEquipment.TIERS.getFirst();
    Player player = new Player();
    player.setLevel(1);
    player.setStrength(tier.strength());
    player.setEndurance(tier.endurance());
    String prefix = "item.warrior_" + tier.key() + "_";
    String blade = prefix + "warblade",
        shield = prefix + "shield",
        greatsword = prefix + "greatsword";
    player.getInventory().addAll(List.of(blade, shield, greatsword));
    var main = com.perso.T4C.player.BodyPart.WEAPON;
    var off = com.perso.T4C.player.BodyPart.WEAPON2;
    var shieldSlot = com.perso.T4C.player.BodyPart.SHIELD;
    assertTrue(InventoryService.equip(player, main, blade).success());
    assertTrue(InventoryService.equip(player, shieldSlot, shield).success());
    assertEquals(blade, player.getEquippedItems().get(main));
    assertEquals(shield, player.getEquippedItems().get(shieldSlot));
    assertTrue(InventoryService.equip(player, main, greatsword).success());
    assertEquals(greatsword, player.getEquippedItems().get(main));
    assertEquals(greatsword, player.getEquippedItems().get(off));
    assertFalse(player.getEquippedItems().containsKey(shieldSlot));
    assertEquals(1, java.util.Collections.frequency(player.getInventory(), shield));
    assertTrue(InventoryService.equip(player, shieldSlot, shield).success());
    assertFalse(player.getEquippedItems().containsKey(main));
    assertFalse(player.getEquippedItems().containsKey(off));
    assertEquals(1, java.util.Collections.frequency(player.getInventory(), greatsword));
    assertTrue(InventoryService.equip(player, main, blade).success());
    var saved = com.perso.T4C.helper.PlayerStateMapper.fromPlayer(player);
    Player restored = new Player();
    com.perso.T4C.helper.PlayerStateMapper.applyToPlayer(saved, restored);
    assertEquals(blade, restored.getEquippedItems().get(main));
    assertEquals(shield, restored.getEquippedItems().get(shieldSlot));
    assertFalse(restored.getEquippedItems().containsKey(off));
  }

  @Test
  void maximumWeightUsesEffectiveStrength() {
    Player player = new Player();
    player.setStrength(50);
    assertEquals(166L, InventoryService.maximumWeight(player));
    player.applyBuff(
        "Strength buff",
        "",
        "",
        null,
        true,
        List.of(new SpellData.SpellEffect("ATTRIBUTE", "strength", "50", "")));
    assertEquals(100, player.getEffectiveStrength());
    // T4C-0054: curve recalibrated (higher asymptote) so strength keeps mattering well past 1000;
    // this value moved from 250 to 307.
    assertEquals(307L, InventoryService.maximumWeight(player));
  }

  @Test
  void maximumWeightKeepsScalingAtHighStrength() {
    // T4C-0054: gear alone can hand out +600 strength per item, so a heavily-equipped or
    // rebirthed character routinely exceeds 1000 strength - the old 500/100 curve had already
    // saturated to ~91% of its ceiling by then, making further strength invisible. Assert the
    // recalibrated curve still grows meaningfully between 1000 and 3000.
    Player player = new Player();
    player.setStrength(1000);
    long atThousand = InventoryService.maximumWeight(player);
    player.setStrength(3000);
    long atThreeThousand = InventoryService.maximumWeight(player);
    assertEquals(1290L, atThousand);
    assertEquals(1690L, atThreeThousand);
    assertTrue(atThreeThousand - atThousand > 300L);
  }

  @Test
  void uniqueDropsAllowAdditionalCopiesEvenWhenOneIsEquippedButRespectCarryWeight() {
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister("assets/items");
    try {
      Player player = new Player();
      player.setStrength(500);
      String key = "witness_earth_signet";
      assertTrue(ItemRegistry.findByKey(key).isUnique());
      player.getEquippedItems().put(com.perso.T4C.player.BodyPart.RING1, key);
      assertTrue(InventoryService.canAdd(player, key));
      assertTrue(InventoryService.add(player, key).success());
      assertTrue(InventoryService.add(player, key).success());
      assertEquals(3, InventoryService.count(player, key));
      player.setStrength(0);
      org.junit.jupiter.api.Assertions.assertFalse(InventoryService.canAdd(player, key));
      assertEquals(InventoryService.Failure.TOO_HEAVY, InventoryService.add(player, key).failure());
      assertEquals(3, InventoryService.count(player, key));
    } finally {
      ItemRegistry.resetAdditionalDefinitions();
    }
  }

  @Test
  void moonsteelUsesAttributesAtLevelOneAndSurvivesSaveLoading() throws Exception {
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister("assets/items");
    try {
      Player player = new Player();
      player.setLevel(1);
      player.setStrength(749);
      player.setEndurance(1000);
      String key = ItemRegistry.findByKey("moonsteel_halberd").getKey();
      var slot = com.perso.T4C.player.BodyPart.WEAPON;
      player.getInventory().add(key);
      assertEquals(
          InventoryService.Failure.REQUIREMENTS_NOT_MET,
          InventoryService.validateEquip(player, slot, key).failure());
      player.setStrength(750);
      assertTrue(InventoryService.equip(player, slot, key).success());
      assertEquals(key, player.getEquippedItems().get(com.perso.T4C.player.BodyPart.WEAPON2));
      var saved = com.perso.T4C.helper.PlayerStateMapper.fromPlayer(player);
      saved.level = 1;
      Player restored = new Player();
      com.perso.T4C.helper.PlayerStateMapper.applyToPlayer(saved, restored);
      assertEquals(key, restored.getEquippedItems().get(slot));
      assertEquals(key, restored.getEquippedItems().get(com.perso.T4C.player.BodyPart.WEAPON2));
      assertFalse(restored.getInventory().contains(key));
    } finally {
      ItemRegistry.resetAdditionalDefinitions();
    }
  }
}
