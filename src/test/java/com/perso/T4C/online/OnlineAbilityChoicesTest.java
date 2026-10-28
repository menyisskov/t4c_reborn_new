package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.CharacterProgress.Item;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.spell.engine.SpellEngine;
import com.perso.T4C.tools.OnlineSpellContentExporter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineAbilityChoicesTest {
  private static final SpellContent ALLOWLIST = OnlineSpellContentExporter.snapshot();
  private static final SpellContentV2 ENGINE_CATALOG = OnlineSpellAllowlistV2.bundled();

  /** The exporter registers JSON items globally; leave the shared registry as other tests expect it. */
  @org.junit.jupiter.api.AfterAll
  static void resetSharedItemRegistry() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  private static CharacterProgress progress(Set<String> spells, List<Item> inventory, Map<String, UUID> equipment) {
    return new CharacterProgress(
        1, "warrior", "male", new CharacterProgress.Attributes(10, 10, 10, 10, 10),
        new CharacterProgress.Vitals(10, 20, 5, 10), new CharacterProgress.Advancement(1, 0, 0, 0, 0),
        inventory, equipment, Map.of(), spells, Map.of(), List.of(), 0, new CharacterProgress.Respawn(0, 1, 1),
        List.of(), Map.of(), 0, null);
  }

  private static String supportedKey() {
    return ENGINE_CATALOG.spells().values().stream()
        .filter(s -> s.stage() <= OnlineSpellAllowlistV2.ENABLED_STAGE && SpellEngine.supported(s))
        .map(SpellContentV2.Spell::key).sorted().findFirst().orElseThrow();
  }

  private static String targetedKey() {
    return ENGINE_CATALOG.spells().values().stream()
        .filter(s -> s.stage() <= OnlineSpellAllowlistV2.ENABLED_STAGE && SpellEngine.supported(s))
        .filter(s -> s.kind() == SpellContentV2.Kind.NUKE || s.kind() == SpellContentV2.Kind.DRAIN
            || s.kind() == SpellContentV2.Kind.DEBUFF)
        .map(SpellContentV2.Spell::key).sorted().findFirst().orElseThrow();
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
  void listsOnlyLearnedEngineSpellsTheServerSupportsWithTheAuthoredManaCost() {
    var castable = supportedKey();
    var spell = ENGINE_CATALOG.spells().get(castable);
    var rows = OnlineAbilityChoices.castableSpells(
        progress(Set.of(castable, "spell.not_in_catalogue"), List.of(), Map.of()), ENGINE_CATALOG);

    assertEquals(1, rows.size());
    assertEquals(castable, rows.getFirst().key());
    assertTrue(rows.getFirst().manaCostLabel().endsWith(" " + spell.manaCost()), rows.getFirst().manaCostLabel());
    assertFalse(rows.getFirst().name().isBlank());
  }

  @Test
  void unlearnedSpellsAreNotOffered() {
    assertTrue(OnlineAbilityChoices.castableSpells(progress(Set.of(), List.of(), Map.of()), ENGINE_CATALOG).isEmpty());
    assertTrue(OnlineAbilityChoices.castableSpells(null, ENGINE_CATALOG).isEmpty());
  }

  @Test
  void stageGateExcludesAnOtherwiseSupportedSpellAboveTheCurrentServerStage() {
    var spell = ENGINE_CATALOG.spells().get(supportedKey());
    var tooNew = new SpellContentV2.Spell(spell.key() + "_stage4", spell.id(), spell.kind(), 4, "stage-4",
        spell.targetType(), spell.attack(), spell.selfCenteredAttack(), spell.pvp(), spell.lineOfSight(),
        spell.manaCost(), spell.successRate(), spell.mentalExhaustion(), spell.physicalExhaustion(),
        spell.attackExhaustion(), spell.cooldownSeconds(), spell.radius(), spell.element(), spell.attackType(),
        spell.minDamage(), spell.maxDamage(), spell.duration(), spell.frequency(), spell.learning(), spell.visual(),
        spell.effects(), spell.dispelTargets());
    var catalog = new SpellContentV2(SpellContentV2.VERSION, Map.of(spell.key(), spell, tooNew.key(), tooNew));
    var rows = OnlineAbilityChoices.castableSpells(
        progress(Set.of(spell.key(), tooNew.key()), List.of(), Map.of()), catalog);

    assertEquals(List.of(spell.key()), rows.stream().map(OnlineAbilityChoices.SpellChoice::key).toList());
  }

  @Test
  void creatureAttackSpellsAreMarkedForMonsterTargeting() {
    var rows = OnlineAbilityChoices.castableSpells(
        progress(Set.of(targetedKey()), List.of(), Map.of()), ENGINE_CATALOG);
    assertEquals(1, rows.size());
    assertTrue(rows.getFirst().requiresCreatureTarget());
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
