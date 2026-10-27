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

  @Test void multiChargeItemsFailClosedBecauseChargesAreNotPerInstance() throws Exception {
    var base = ItemCatalog.loadBundled().items().get(POTION);
    var multi = new ItemContent(ItemContent.VERSION, Map.of(POTION, withCharges(base, 2)));
    var charge = new CharacterProgress.Item(UUID.randomUUID(), POTION, 1, 2);
    var p = progress(new CharacterProgress.Vitals(10, 100, 20, 30), List.of(charge), Map.of(), Map.of());
    assertEquals("unsupported_item", assertThrows(PreviewWorld.Rejected.class, () ->
        OnlineItemUse.prepareUse(state(p), charge.id(), NOW, new Random(5), multi, SpellCatalog.loadBundled())).getMessage());
  }

  private static OnlineStore.CharacterState state(CharacterProgress p) {
    return new OnlineStore.CharacterState(UUID.randomUUID(), UUID.randomUUID(), "Drinker", 0, 3, 3, 0, 0, 0, p);
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
