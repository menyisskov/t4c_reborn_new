package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.CharacterProgress.Item;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.tools.OnlineSpellContentExporter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineAbilityChoicesTest {
  private static final SpellContent ALLOWLIST = OnlineSpellContentExporter.snapshot();

  private static CharacterProgress progress(Set<String> spells, List<Item> inventory, Map<String, UUID> equipment) {
    return new CharacterProgress(
        1, "warrior", "male", new CharacterProgress.Attributes(10, 10, 10, 10, 10),
        new CharacterProgress.Vitals(10, 20, 5, 10), new CharacterProgress.Advancement(1, 0, 0, 0, 0),
        inventory, equipment, Map.of(), spells, Map.of(), List.of(), 0, new CharacterProgress.Respawn(0, 1, 1),
        List.of(), Map.of(), 0, null);
  }

  private static String castableKey() {
    return ALLOWLIST.spells().values().stream().filter(s -> s.use() == SpellContent.Use.CAST)
        .map(SpellContent.Spell::key).findFirst().orElseThrow();
  }

  private static String grantOnlyKey() {
    return ALLOWLIST.spells().values().stream().filter(s -> s.use() == SpellContent.Use.GRANT_ONLY)
        .map(SpellContent.Spell::key).findFirst().orElseThrow();
  }

  private static ItemDefinition potion() {
    var itemSpellIds = ALLOWLIST.spells().values().stream().filter(s -> s.use() == SpellContent.Use.ITEM)
        .map(SpellContent.Spell::id).toList();
    return ItemRegistry.allByKey().values().stream()
        .filter(i -> i.getBodyPart() == null && i.getNbCharges() == 1 && !i.isUnlimitedUse())
        .filter(i -> i.getSpells().stream().anyMatch(s -> itemSpellIds.contains(s.getSpellId())))
        .findFirst().orElseThrow();
  }

  private static String inventoryKey(ItemDefinition definition) {
    return definition.getKey().startsWith("item.") ? definition.getKey() : "item." + definition.getKey();
  }

  @Test
  void listsOnlyLearnedSpellsTheServerCastsWithTheAuthoredManaCost() {
    var castable = castableKey();
    var spell = ALLOWLIST.spells().get(castable);
    var rows = OnlineAbilityChoices.castableSpells(
        progress(Set.of(castable, grantOnlyKey(), "spell.not_in_catalogue"), List.of(), Map.of()), ALLOWLIST);

    assertEquals(1, rows.size());
    assertEquals(castable, rows.getFirst().key());
    assertTrue(rows.getFirst().manaCostLabel().endsWith(" " + spell.manaCost()), rows.getFirst().manaCostLabel());
    assertFalse(rows.getFirst().name().isBlank());
  }

  @Test
  void unlearnedSpellsAreNotOffered() {
    assertTrue(OnlineAbilityChoices.castableSpells(progress(Set.of(), List.of(), Map.of()), ALLOWLIST).isEmpty());
    assertTrue(OnlineAbilityChoices.castableSpells(null, ALLOWLIST).isEmpty());
  }

  @Test
  void listsCarriedSingleChargePotionsButNotEquippedOrUnsupportedItems() {
    var definition = potion();
    var key = inventoryKey(definition);
    var carried = new Item(UUID.randomUUID(), key, 3, 1);
    var depleted = new Item(UUID.randomUUID(), key, 1, 0);
    var unknown = new Item(UUID.randomUUID(), "item.no_such_item_anywhere", 1, 1);
    var rows = OnlineAbilityChoices.usablePotions(
        progress(Set.of(), List.of(carried, depleted, unknown), Map.of()), ALLOWLIST);

    assertEquals(1, rows.size());
    assertEquals(carried.id(), rows.getFirst().instance());
    assertEquals(3, rows.getFirst().quantity());

    var worn = OnlineAbilityChoices.usablePotions(
        progress(Set.of(), List.of(carried), Map.of("WEAPON", carried.id())), ALLOWLIST);
    assertTrue(worn.isEmpty(), "An equipped instance is refused by the server");
  }

  @Test
  void gearIsNotAPotion() {
    var gear = ItemRegistry.allByKey().values().stream().filter(i -> i.getBodyPart() != null).findFirst().orElseThrow();
    var rows = OnlineAbilityChoices.usablePotions(
        progress(Set.of(), List.of(new Item(UUID.randomUUID(), inventoryKey(gear), 1, 1)), Map.of()), ALLOWLIST);
    assertTrue(rows.isEmpty());
  }

  @Test
  void numberKeysAddressOnlyTheFirstNineRows() {
    var rows = new ArrayList<Integer>();
    for (int i = 0; i < 10; i++) rows.add(i);
    assertEquals(0, OnlineAbilityChoices.choose(rows, 1).orElseThrow());
    assertEquals(8, OnlineAbilityChoices.choose(rows, 9).orElseThrow());
    assertTrue(OnlineAbilityChoices.choose(rows, 10).isEmpty());
    assertTrue(OnlineAbilityChoices.choose(rows, 0).isEmpty());
  }
}
