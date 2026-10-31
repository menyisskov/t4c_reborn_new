package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

  @Test
  void journalRecordsOriginalChargeExhaustionAsOneUnderlyingItemRemoval() {
    Player player = new Player();
    player.setStrength(1000);
    String key = finiteMultiChargeItem().getKey();
    InventoryMutationJournal journal = player.beginInventoryMutationJournal();
    assertTrue(InventoryService.add(player, key).success());
    int charges = ItemRegistry.findByKey(key).getNbCharges();
    assertTrue(charges > 1);
    for (int index = 0; index < charges; index++) {
      assertTrue(InventoryService.useCharge(player, key).success());
    }
    var prepared = journal.seal();
    assertTrue(prepared.finalState().inventory().isEmpty());
    assertEquals(1, prepared.mutations().stream()
        .filter(m -> m.container() == JournaledInventoryState.Container.INVENTORY
            && m.change() == JournaledInventoryState.Change.DELETE).count());
    var outer = prepared.operations().stream()
        .filter(o -> o.name().equals("useCharge"))
        .reduce((first, last) -> last).orElseThrow();
    var destroy = prepared.operations().stream()
        .filter(o -> o.name().equals("destroyOne")).findFirst().orElseThrow();
    var remove = prepared.operations().stream()
        .filter(o -> o.name().equals("remove")).findFirst().orElseThrow();
    assertEquals(outer.id(), destroy.parentId());
    assertEquals(destroy.id(), remove.parentId());
    assertEquals(0, prepared.mutations().stream()
        .filter(m -> m.change() == JournaledInventoryState.Change.DELETE).findFirst()
        .orElseThrow().key());
  }

  @Test
  void journalTracksSettersIteratorsAndMapViewsThenSealsExactProjection() {
    Player player = new Player();
    String key = "item.breuvage_de_soin";
    player.setInventory(List.of(key, key));
    player.getEquippedItems().put(com.perso.T4C.player.BodyPart.RING1, key);
    player.getItemCharges().put(key, 7);
    InventoryMutationJournal journal = player.beginInventoryMutationJournal();
    var items = player.getInventory().listIterator();
    assertEquals(key, items.next());
    items.set("changed");
    items.add(key);
    items.next();
    items.remove();
    player.getEquippedItems().values().removeIf(key::equals);
    player.getEquippedItems().put(com.perso.T4C.player.BodyPart.RING2, key);
    player.getEquippedItems().entrySet().iterator().next().setValue("changed");
    player.getItemCharges().keySet().removeIf(key::equals);
    player.setInventory(List.of(key));
    player.setEquippedItems(java.util.Map.of(com.perso.T4C.player.BodyPart.RING1, key));
    player.setItemCharges(java.util.Map.of(key, 2));
    InventoryService.removeExtractedCharges(player, key, 1);
    var prepared = journal.seal();
    assertEquals(List.of(key), prepared.finalState().inventory());
    assertEquals(java.util.Map.of(com.perso.T4C.player.BodyPart.RING1, key),
        prepared.finalState().equipment());
    assertEquals(java.util.Map.of(key, 1), prepared.finalState().charges());
    assertTrue(prepared.mutations().stream().anyMatch(m -> m.operationId() == 0));
    assertEquals(1, prepared.operations().stream()
        .filter(o -> o.name().equals("removeExtractedCharges")).count());
  }

  @Test
  void exceptionInvalidatesAndRollbackRestoresOriginalInventoryProjection() {
    Player player = new Player();
    player.setInventory(List.of("first"));
    InventoryMutationJournal journal = player.beginInventoryMutationJournal();
    assertThrows(IllegalStateException.class, () -> journal.capture("failed", () -> {
      player.getInventory().add("second");
      throw new IllegalStateException("injected failure");
    }));
    assertTrue(journal.isInvalid());
    assertThrows(IllegalStateException.class, journal::seal);
    journal.rollback();
    assertEquals(List.of("first"), player.getInventory());
    assertEquals(null, player.getInventoryMutationJournal());
  }

  @Test
  void originalAggregateChargesCannotIdentifyTwoDistinctPartialInstances() {
    Player player = new Player();
    ItemDefinition item = finiteMultiChargeItem();
    String key = item.getKey();
    player.setInventory(List.of(key, key));
    player.setItemCharges(java.util.Map.of(key, item.getNbCharges() * 2 - 1));
    // The original aggregate selects one partial copy but cannot name either owned UUID.
    assertEquals(item.getNbCharges() - 1, InventoryService.chargesForNextInstance(player, key));
  }
  @Test
  void journalKeepsOriginalEquipDisplacementAndRefusalWithoutExtraWrites() {
    var tier = WarriorEquipment.TIERS.getFirst();
    Player player = new Player();
    player.setStrength(tier.strength());
    player.setEndurance(tier.endurance());
    String prefix = "item.warrior_" + tier.key() + "_";
    String blade = prefix + "warblade";
    String shield = prefix + "shield";
    String greatsword = prefix + "greatsword";
    player.setInventory(List.of(blade, shield, greatsword));
    var main = com.perso.T4C.player.BodyPart.WEAPON;
    var shieldSlot = com.perso.T4C.player.BodyPart.SHIELD;
    InventoryMutationJournal journal = player.beginInventoryMutationJournal();
    assertEquals(InventoryService.Failure.UNKNOWN_ITEM,
        InventoryService.equip(player, main, "missing_item").failure());
    assertTrue(InventoryService.equip(player, main, blade).success());
    assertTrue(InventoryService.equip(player, shieldSlot, shield).success());
    assertTrue(InventoryService.equip(player, main, greatsword).success());
    var prepared = journal.seal();
    assertEquals(greatsword, prepared.finalState().equipment().get(main));
    assertEquals(greatsword,
        prepared.finalState().equipment().get(com.perso.T4C.player.BodyPart.WEAPON2));
    assertFalse(prepared.finalState().equipment().containsKey(shieldSlot));
    assertEquals(1, java.util.Collections.frequency(prepared.finalState().inventory(), shield));
    var refused = prepared.operations().stream()
        .filter(o -> o.name().equals("equip") &&
            "missing_item".equals(o.arguments().get(1))).findFirst().orElseThrow();
    assertFalse(((InventoryService.Result) refused.result()).success());
    assertTrue(prepared.mutations().stream().noneMatch(m -> m.operationId() == refused.id()));
  }

  private static ItemDefinition finiteMultiChargeItem() {
    return ItemRegistry.load().stream()
        .filter(item -> item.getNbCharges() > 1 && !item.isUnlimitedUse()
            && item.getBodyPart() == null)
        .findFirst().orElseThrow();
  }
}
