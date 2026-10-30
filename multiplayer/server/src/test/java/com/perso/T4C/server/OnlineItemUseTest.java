package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineItemUseTest {
  private static final long NOW = 10_000L;
  private static final String POTION = "item.critical_healing_potion";

  @Test void potionHealsByItsSpellAndRemovesItsOnlyCharge() throws Exception {
    var charge = new CharacterProgress.Item(UUID.randomUUID(), POTION, 1, 1);
    var p = progress(new CharacterProgress.Vitals(10, 100, 20, 30), List.of(charge), Map.of(), Map.of());
    var used = OnlineItemUse.prepareUse(state(p), charge.id(), NOW, new Random(5),
        ItemCatalog.loadBundled(), SpellCatalog.loadBundled());
    // The critical potion's spell restores at least 38 and spends its 7 mana.
    assertTrue(used.healthGained() >= 38);
    assertEquals(10 + used.healthGained(), used.progress().vitals().hp());
    assertEquals(7, used.manaSpent());
    assertTrue(used.progress().inventory().isEmpty(), "A single-charge potion leaves the inventory");
  }

  @Test void foreignAndWornInstancesAreRefusedWithoutAnyDraft() throws Exception {
    var items = ItemCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    var charge = new CharacterProgress.Item(UUID.randomUUID(), POTION, 1, 1);
    var p = progress(new CharacterProgress.Vitals(10, 100, 20, 30), List.of(charge), Map.of(), Map.of());
    assertEquals("item_not_owned", assertThrows(PreviewWorld.Rejected.class, () ->
        OnlineItemUse.prepareUse(state(p), UUID.randomUUID(), NOW, new Random(5), items, spells)).getMessage());

    var worn = progress(new CharacterProgress.Vitals(10, 100, 20, 30), List.of(charge),
        Map.of("WEAPON", charge.id()), Map.of());
    assertEquals("item_equipped", assertThrows(PreviewWorld.Rejected.class, () ->
        OnlineItemUse.prepareUse(state(worn), charge.id(), NOW, new Random(5), items, spells)).getMessage());
  }

  @Test void fullHealthAndMentalExhaustionRefuseWithoutSpendingTheCharge() throws Exception {
    var items = ItemCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    var charge = new CharacterProgress.Item(UUID.randomUUID(), POTION, 1, 1);
    var full = progress(new CharacterProgress.Vitals(100, 100, 20, 30), List.of(charge), Map.of(), Map.of());
    assertEquals("no_healing_needed", assertThrows(PreviewWorld.Rejected.class, () ->
        OnlineItemUse.prepareUse(state(full), charge.id(), NOW, new Random(5), items, spells)).getMessage());

    var exhausted = progress(new CharacterProgress.Vitals(10, 100, 20, 30), List.of(charge),
        Map.of(), Map.of(OnlineCasting.MENTAL_EXHAUSTION, NOW + 1_000));
    assertEquals("exhausted", assertThrows(PreviewWorld.Rejected.class, () ->
        OnlineItemUse.prepareUse(state(exhausted), charge.id(), NOW, new Random(5), items, spells)).getMessage());
  }

  @Test void finiteMultiChargeItemsSpendChargesBeforeConsumingStackUnits() throws Exception {
    var base = ItemCatalog.loadBundled().items().get(POTION);
    var multi = new ItemContent(ItemContent.VERSION, Map.of(POTION, withCharges(base, 2)));
    var charge = new CharacterProgress.Item(UUID.randomUUID(), POTION, 2, 2);
    var p = progress(new CharacterProgress.Vitals(1, 100, 20, 30), List.of(charge), Map.of(), Map.of());
    var spells = SpellCatalog.loadBundled();

    var first = OnlineItemUse.prepareUse(state(p), charge.id(), NOW, new Random(5), multi, spells).progress();
    assertEquals(List.of(new CharacterProgress.Item(charge.id(), POTION, 2, 1)), first.inventory());
    var second = OnlineItemUse.prepareUse(state(withLowHealth(first)), charge.id(), NOW + 60_000,
        new Random(5), multi, spells).progress();
    assertEquals(List.of(new CharacterProgress.Item(charge.id(), POTION, 1, 2)), second.inventory());
    var third = OnlineItemUse.prepareUse(state(withLowHealth(second)), charge.id(), NOW + 120_000,
        new Random(5), multi, spells).progress();
    assertEquals(List.of(new CharacterProgress.Item(charge.id(), POTION, 1, 1)), third.inventory());
    var fourth = OnlineItemUse.prepareUse(state(withLowHealth(third)), charge.id(), NOW + 180_000,
        new Random(5), multi, spells).progress();
    assertTrue(fourth.inventory().isEmpty());
  }

  @Test void depletedOrOverchargedMultiChargeInstancesAreRefused() throws Exception {
    var base = ItemCatalog.loadBundled().items().get(POTION);
    var multi = new ItemContent(ItemContent.VERSION, Map.of(POTION, withCharges(base, 2)));
    var spells = SpellCatalog.loadBundled();
    for (int invalidCharges : List.of(0, 3)) {
      var invalid = new CharacterProgress.Item(UUID.randomUUID(), POTION, 2, invalidCharges);
      var p = progress(new CharacterProgress.Vitals(1, 100, 20, 30), List.of(invalid), Map.of(), Map.of());
      assertEquals("unsupported_item", assertThrows(PreviewWorld.Rejected.class, () ->
          OnlineItemUse.prepareUse(state(p), invalid.id(), NOW, new Random(5), multi, spells)).getMessage());
    }
  }

  @Test void manaPotionRestoresItsAuthoredManaConsumesOneAndRefusesAtFullMana() throws Exception {
    var items = ItemCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    var stack = new CharacterProgress.Item(UUID.randomUUID(), "item.potion_of_mana", 2, 1);
    var low = progress(new CharacterProgress.Vitals(10, 100, 5, 40), List.of(stack), Map.of(), Map.of());
    var used = OnlineItemUse.prepareUse(state(low), stack.id(), NOW, new Random(5), items, spells);
    assertEquals(25, used.manaGained());
    assertEquals(30, used.progress().vitals().mana());
    assertEquals(0, used.healthGained());
    assertEquals(1, used.progress().inventory().get(0).quantity(), "exactly one unit leaves the stack");
    // The clamp is the offline one: a pool two short of full gains two.
    var nearly = progress(new CharacterProgress.Vitals(10, 100, 38, 40), List.of(stack), Map.of(), Map.of());
    assertEquals(2, OnlineItemUse.prepareUse(state(nearly), stack.id(), NOW, new Random(5), items, spells).manaGained());
    // Offline refuses a potion at full mana; so does the server, keeping the stack.
    var full = progress(new CharacterProgress.Vitals(10, 100, 40, 40), List.of(stack), Map.of(), Map.of());
    assertEquals("no_mana_needed", assertThrows(PreviewWorld.Rejected.class, () ->
        OnlineItemUse.prepareUse(state(full), stack.id(), NOW, new Random(5), items, spells)).getMessage());
  }

  @Test void healingPotionsAddNoExhaustionSoTheNextPotionWorksAtOnce() throws Exception {
    var items = ItemCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    var heal = new CharacterProgress.Item(UUID.randomUUID(), "item.healing_potion", 2, 1);
    var mana = new CharacterProgress.Item(UUID.randomUUID(), "item.potion_of_mana", 1, 1);
    var p = progress(new CharacterProgress.Vitals(10, 100, 5, 40), List.of(heal, mana), Map.of(), Map.of());
    var first = OnlineItemUse.prepareUse(state(p), heal.id(), NOW, new Random(5), items, spells);
    assertEquals(50, first.healthGained());
    assertEquals(0, first.progress().cooldowns().getOrDefault(OnlineCasting.MENTAL_EXHAUSTION, 0L));
    var second = OnlineItemUse.prepareUse(state(first.progress()), mana.id(), NOW, new Random(5), items, spells);
    assertEquals(25, second.manaGained());
  }

  @Test void buffPotionsAreRefusedWithAClearCodeAndNothingChanges() throws Exception {
    var items = ItemCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    var buff = new CharacterProgress.Item(UUID.randomUUID(), "item.potion_of_partial_air_resistance", 1, 1);
    var p = progress(new CharacterProgress.Vitals(10, 100, 5, 40), List.of(buff), Map.of(), Map.of());
    var code = assertThrows(PreviewWorld.Rejected.class, () ->
        OnlineItemUse.prepareUse(state(p), buff.id(), NOW, new Random(5), items, spells)).getMessage();
    assertTrue(code.equals("no_effect") || code.equals("unsupported_spell"), code);
  }

  @Test void potionOfFuryPersistsItsAuthoredStrengthBuffAndSpendsItsChargeAtomically() throws Exception {
    var items = ItemCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    var stack = new CharacterProgress.Item(UUID.randomUUID(), "item.potion_of_fury", 1, 1);
    var p = progress(new CharacterProgress.Vitals(10, 100, 5, 40), List.of(stack), Map.of(), Map.of());

    var used = OnlineItemUse.prepareUse(state(p), stack.id(), NOW, new Random(5), items, spells,
        SpellCatalogV2.loadBundled());

    assertTrue(used.progress().inventory().isEmpty());
    assertEquals(1, used.progress().buffs().size());
    var buff = used.progress().buffs().getFirst();
    assertEquals("spell.item_potion_of_fury", buff.spell());
    assertEquals(NOW + 300_000, buff.expiresAt());
    assertEquals(10, buff.modifiers().get("str"));
    assertEquals(NOW, used.progress().cooldowns().get(OnlineBuffs.ANCHOR));
    assertEquals(used.progress(), OnlineBuffs.expire(used.progress(), NOW + 299_999));
    assertTrue(OnlineBuffs.expire(used.progress(), NOW + 300_000).buffs().isEmpty());
    assertEquals(0, used.manaSpent());
    assertEquals(0, used.healthGained());
  }

  @Test void potionOfNimblenessPersistsItsAuthoredAgilityBuffAndSpendsItsChargeAtomically() throws Exception {
    var items = ItemCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    var stack = new CharacterProgress.Item(UUID.randomUUID(), "item.potion_of_nimbleness", 1, 1);
    var p = progress(new CharacterProgress.Vitals(10, 100, 5, 40), List.of(stack), Map.of(), Map.of());

    var used = OnlineItemUse.prepareUse(state(p), stack.id(), NOW, new Random(5), items, spells,
        SpellCatalogV2.loadBundled());

    assertTrue(used.progress().inventory().isEmpty());
    assertEquals(1, used.progress().buffs().size());
    var buff = used.progress().buffs().getFirst();
    assertEquals("spell.item_potion_of_nimbleness", buff.spell());
    assertEquals(NOW + 300_000, buff.expiresAt());
    assertEquals(10, buff.modifiers().get("dex"));
    assertEquals(NOW, used.progress().cooldowns().get(OnlineBuffs.ANCHOR));
    assertEquals(used.progress(), OnlineBuffs.expire(used.progress(), NOW + 299_999));
    assertTrue(OnlineBuffs.expire(used.progress(), NOW + 300_000).buffs().isEmpty());
    assertEquals(0, used.manaSpent());
    assertEquals(0, used.healthGained());
  }

  @Test void ordinaryStatPotionsPersistTheirAuthoredBuffsAndSpendTheirCharges() throws Exception {
    var items = ItemCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    var spellsV2 = SpellCatalogV2.loadBundled();
    var potions = List.of(
        new StatPotion("item.potion_of_clear_thought", "spell.item_potion_of_clear_thought", "int"),
        new StatPotion("item.potion_of_fortitude", "spell.item_potion_of_fortitude", "end"),
        new StatPotion("item.potion_of_tranquility", "spell.item_potion_of_tranquility", "wis"));

    for (var potion : potions) {
      var stack = new CharacterProgress.Item(UUID.randomUUID(), potion.itemKey(), 1, 1);
      var p = progress(new CharacterProgress.Vitals(10, 100, 5, 40), List.of(stack), Map.of(), Map.of());
      var used = OnlineItemUse.prepareUse(state(p), stack.id(), NOW, new Random(5), items, spells, spellsV2);

      assertTrue(used.progress().inventory().isEmpty(), potion.itemKey());
      assertEquals(1, used.progress().buffs().size(), potion.itemKey());
      var buff = used.progress().buffs().getFirst();
      assertEquals(potion.spellKey(), buff.spell());
      assertEquals(NOW + 300_000, buff.expiresAt(), potion.itemKey());
      assertEquals(Map.of(potion.attribute(), 10), buff.modifiers(), potion.itemKey());
      assertEquals(used.progress(), OnlineBuffs.expire(used.progress(), NOW + 299_999), potion.itemKey());
      assertTrue(OnlineBuffs.expire(used.progress(), NOW + 300_000).buffs().isEmpty(), potion.itemKey());
      assertEquals(NOW, used.progress().cooldowns().get(OnlineBuffs.ANCHOR), potion.itemKey());
      assertEquals(0, used.manaSpent(), potion.itemKey());
      assertEquals(0, used.healthGained(), potion.itemKey());
    }
  }

  private record StatPotion(String itemKey, String spellKey, String attribute) {}

  @Test void hyperPotionOfNimblenessUsesItsMinimumBonusBelowTheThreshold() throws Exception {
    var used = useHyperPotion(120, 0, 0);

    assertHyperPotionUse(used, 50);
  }

  @Test void hyperPotionScalesFromEquippedTrueAgilityButNotActiveDexterityBuffs() throws Exception {
    // Base 180 plus 40 from equipped gear gives true_agility 220; the active +200 dex buff is excluded.
    var used = useHyperPotion(180, 40, 200);

    assertHyperPotionUse(used, 55);
  }

  private static OnlineCasting.Result useHyperPotion(int dexterity, int gearDexterity, int activeDexterityBuff)
      throws Exception {
    var items = ItemCatalog.loadBundled();
    var itemDefinitions = new java.util.HashMap<>(items.items());
    var inventory = new java.util.ArrayList<CharacterProgress.Item>();
    var equipment = new java.util.HashMap<String, UUID>();
    if (gearDexterity > 0) {
      String gearKey = "item.test_hyper_potion_agility_gear";
      itemDefinitions.put(gearKey, new ItemContent.Definition(gearKey, "Test Agility Gear",
          com.perso.T4C.player.BodyPart.HEAD, null, null, null, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          false, false, false, 900_001, 0, null, null, 0, false,
          List.of(new ItemContent.Boost(1, 6, Integer.toString(gearDexterity), 0, 0)), List.of(), false, null));
      var gear = new CharacterProgress.Item(UUID.randomUUID(), gearKey, 1, 0);
      inventory.add(gear);
      equipment.put("HEAD", gear.id());
    }
    var stack = new CharacterProgress.Item(UUID.randomUUID(), "item.hyper_potion_of_nimbleness", 1, 1);
    inventory.add(stack);
    var base = progress(new CharacterProgress.Vitals(10, 100, 5, 40), inventory, equipment, Map.of());
    var attributes = new CharacterProgress.Attributes(base.attributes().strength(), base.attributes().endurance(),
        dexterity, base.attributes().wisdom(), base.attributes().intelligence());
    var buffs = activeDexterityBuff == 0 ? List.<CharacterProgress.Buff>of()
        : List.of(new CharacterProgress.Buff("spell.item_potion_of_nimbleness", NOW + 600_000,
            0, 0, Map.of("dex", activeDexterityBuff), NOW));
    var state = new CharacterProgress(base.version(), base.characterClass(), base.gender(), attributes, base.vitals(),
        base.advancement(), inventory, equipment, base.skills(), base.spells(), base.questFlags(), base.storage(),
        base.storageGold(), base.respawn(), buffs, base.cooldowns(), base.hiddenUntil(), base.companion());
    return OnlineItemUse.prepareUse(state(state), stack.id(), NOW, new Random(5),
        new ItemContent(ItemContent.VERSION, itemDefinitions), SpellCatalog.loadBundled(), SpellCatalogV2.loadBundled());
  }

  private static void assertHyperPotionUse(OnlineCasting.Result used, int expectedDexterity) {
    assertTrue(used.progress().inventory().stream()
        .noneMatch(item -> item.key().equals("item.hyper_potion_of_nimbleness")),
        "accepted use atomically consumes the single charge");
    if (used.progress().equipment().containsKey("HEAD")) {
      UUID gear = used.progress().equipment().get("HEAD");
      assertTrue(used.progress().inventory().stream().anyMatch(item -> item.id().equals(gear)
          && item.key().equals("item.test_hyper_potion_agility_gear")),
          "using the potion retains its separate equipped gear instance");
    }
    var buff = used.progress().buffs().stream()
        .filter(candidate -> candidate.spell().equals("spell.item_hyper_potion_of_nimbleness"))
        .findFirst().orElseThrow();
    assertEquals(NOW + 300_000, buff.expiresAt());
    assertEquals(expectedDexterity, buff.modifiers().get("dex"));
    assertEquals(NOW, used.progress().cooldowns().get(OnlineBuffs.ANCHOR));
    assertEquals(used.progress(), OnlineBuffs.expire(used.progress(), NOW + 299_999));
    var expired = OnlineBuffs.expire(used.progress(), NOW + 300_000);
    assertFalse(expired.buffs().stream().anyMatch(candidate ->
        candidate.spell().equals("spell.item_hyper_potion_of_nimbleness")));
    assertEquals(used.progress().buffs().size() - 1, expired.buffs().size());
    assertEquals(0, used.manaSpent());
    assertEquals(0, used.healthGained());
  }

  private static OnlineStore.CharacterState state(CharacterProgress p) {
    return new OnlineStore.CharacterState(UUID.randomUUID(), UUID.randomUUID(), "Drinker", 0, 3, 3, 0, 0, 0, p);
  }

  private static CharacterProgress withLowHealth(CharacterProgress progress) {
    return ProgressEdits.withVitals(progress, new CharacterProgress.Vitals(1, 100,
        progress.vitals().maxMana(), progress.vitals().maxMana()));
  }

  private static ItemContent.Definition withCharges(ItemContent.Definition d, int nbCharges) {
    return new ItemContent.Definition(d.key(), d.name(), d.bodyPart(), d.secondaryBodyPart(),
        d.appearanceEquippedPrimary(), d.appearanceEquippedSecondary(), d.appearanceInventory(), d.price(),
        d.weight(), d.armorClass(), d.dodgeLost(), d.minEnd(), d.reqAttack(), d.reqStr(), d.reqAgi(),
        d.minInt(), d.minWis(), d.attackSpeed(), d.unique(), d.isBow(), d.unlimitedUse(), d.numId(),
        d.structure(), d.dmgFormula(), d.atkDelay(), nbCharges, d.undroppable(), d.boosts(), d.spells(),
        d.rarityPriced(), d.quiverSplash());
  }

  /** A valid warrior with the given vitals, inventory, equipment and cooldowns. */
  private static CharacterProgress progress(CharacterProgress.Vitals vitals, List<CharacterProgress.Item> inventory,
      Map<String, UUID> equipment, Map<String, Long> cooldowns) {
    var base = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3)).progress();
    return new CharacterProgress(base.version(), base.characterClass(), base.gender(), base.attributes(),
        vitals, base.advancement(), inventory, equipment, base.skills(), Set.of(), Map.of(), List.of(), 0,
        base.respawn(), List.of(), cooldowns, 0, base.companion());
  }
}
