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
  void anAreaAroundACreatureNeedsACreatureTargetAndASelfCentredAreaDoesNot() {
    var around = ENGINE_CATALOG.spells().values().stream()
        .filter(s -> s.kind() == SpellContentV2.Kind.AREA_DAMAGE && s.targetType() == 17
            && s.stage() <= OnlineSpellAllowlistV2.ENABLED_STAGE && SpellEngine.supported(s))
        .map(SpellContentV2.Spell::key).sorted().findFirst().orElseThrow();
    var centred = ENGINE_CATALOG.spells().values().stream()
        .filter(s -> s.kind() == SpellContentV2.Kind.AREA_DAMAGE && s.selfCenteredAttack()
            && s.stage() <= OnlineSpellAllowlistV2.ENABLED_STAGE && SpellEngine.supported(s))
        .map(SpellContentV2.Spell::key).sorted().findFirst().orElseThrow();
    var rows = OnlineAbilityChoices.castableSpells(progress(Set.of(around, centred), List.of(), Map.of()), ENGINE_CATALOG);
    assertEquals(2, rows.size());
    for (var row : rows) {
      if (row.key().equals(around)) assertTrue(row.requiresCreatureTarget(), "an area around a creature is aimed at one: " + row.key());
      else assertFalse(row.requiresCreatureTarget(), "a self-centred area is cast with no target: " + row.key());
    }
  }

  @Test
  void tileAimedAreaDamageNeedsPositionTargetWithoutBeingTreatedAsSelfCast() {
    var aimed = ENGINE_CATALOG.spells().values().stream()
        .filter(s -> s.kind() == SpellContentV2.Kind.AREA_DAMAGE && s.targetType() == 19
            && !s.selfCenteredAttack() && s.stage() <= OnlineSpellAllowlistV2.ENABLED_STAGE
            && SpellEngine.supported(s))
        .map(SpellContentV2.Spell::key).sorted().findFirst().orElseThrow();
    var rows = OnlineAbilityChoices.castableSpells(progress(Set.of(aimed), List.of(), Map.of()), ENGINE_CATALOG);

    assertEquals(1, rows.size());
    assertTrue(rows.getFirst().requiresPositionTarget());
    assertFalse(rows.getFirst().requiresCreatureTarget());
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
  void exposesOnlyTheAuthoredLearnedWordOfRecallAboveTheCurrentServerStage() {
    var stageFourKeys = ENGINE_CATALOG.spells().values().stream()
        .filter(spell -> spell.stage() > OnlineSpellAllowlistV2.ENABLED_STAGE)
        .map(SpellContentV2.Spell::key)
        .collect(java.util.stream.Collectors.toSet());
    var rows = OnlineAbilityChoices.castableSpells(progress(stageFourKeys, List.of(), Map.of()), ENGINE_CATALOG);

    assertEquals(List.of("spell.word_of_recall"), rows.stream().map(OnlineAbilityChoices.SpellChoice::key).toList());
    assertFalse(rows.getFirst().requiresCreatureTarget());
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
  void offersFiniteMultiChargeRestorativesOnlyWhileTheirCurrentItemHasCharges() {
    var base = potion();
    var multi = withCharges(base, 3, false);
    var key = inventoryKey(base);
    var charged = new Item(UUID.randomUUID(), key, 2, 2);
    var lastCharge = new Item(UUID.randomUUID(), key, 1, 1);
    var depleted = new Item(UUID.randomUUID(), key, 2, 0);
    var overcharged = new Item(UUID.randomUUID(), key, 2, 4);
    var definitions = Map.of(key, multi);

    var rows = OnlineAbilityChoices.usablePotions(
        progress(Set.of(), List.of(charged, lastCharge, depleted, overcharged), Map.of()), ALLOWLIST,
        itemKey -> definitions.get(itemKey));

    assertEquals(List.of(charged.id(), lastCharge.id()),
        rows.stream().map(OnlineAbilityChoices.PotionChoice::instance).toList());
  }

  private static ItemDefinition withCharges(ItemDefinition item, int charges, boolean unlimited) {
    return new ItemDefinition(item.getKey(), item.getName(), item.getBodyPart(),
        item.getAppearanceEquippedPrimary(), item.getSecondaryBodyPart(), item.getAppearanceEquippedSecondary(),
        item.getAppearanceInventory(), item.getPrice(), item.getWeight(), item.getArmorClass(), item.getDodgeLost(),
        item.getMinEnd(), item.getReqAttack(), item.getReqStr(), item.getReqAgi(), item.getMinInt(), item.getMinWis(),
        item.getAttackSpeed(), item.isUnique(), item.isBow(), unlimited, item.getNumId(), item.getStructure(),
        item.getAppearanceId(), item.getDmgFormula(), item.getAtkDelay(), item.getRadiance(), charges,
        item.isCanSummon(), item.getLockName(), item.getLockDiff(), item.getSignText(), item.getContainerGold(),
        item.getGlobalRespawn(), item.getLocalRespawn(), item.getSpells(), item.getBoosts(),
        item.getContainerLootGroups(), item.isUndroppable());
  }

  /** What the offline game does for a drink: the spell resolves and only restores health or mana. */
  private static boolean offlineRestoreOnly(ItemDefinition item) {
    if (item.getBodyPart() != null || item.getNbCharges() != 1 || item.isUnlimitedUse() || item.getSpells().isEmpty())
      return false;
    return item.getSpells().stream().allMatch(itemSpell -> {
      var spell = com.perso.T4C.spell.SpellRegistry.findById(itemSpell.getSpellId());
      var effects = spell == null ? null : spell.getT4cEffects();
      return effects != null && !effects.isEmpty() && effects.stream().allMatch(effect -> effect.getEffectType() == 1
          || (effect.getEffectType() == 2 && effect.getParameters().stream()
              .anyMatch(param -> param.getParamId() == 2 && "MANA".equalsIgnoreCase(param.getExpression()))));
    });
  }

  @Test
  void everyOfflineRestorePotionIsOfferedAndNothingElseIs() {
    var inventory = new ArrayList<Item>();
    var expected = new java.util.TreeSet<String>();
    for (var definition : ItemRegistry.allByKey().values()) {
      if (definition.getBodyPart() != null || definition.getSpells().isEmpty()) continue;
      var key = inventoryKey(definition);
      inventory.add(new Item(UUID.randomUUID(), key, 2, 1));
      if (offlineRestoreOnly(definition)) expected.add(key);
    }
    assertTrue(expected.containsAll(List.of("item.light_healing_potion", "item.healing_potion",
        "item.potion_of_healing", "item.critical_healing_potion", "item.potion_of_mana")), expected.toString());
    var offered = new java.util.TreeSet<String>();
    OnlineAbilityChoices.usablePotions(progress(Set.of(), inventory, Map.of()), ALLOWLIST)
        .forEach(row -> offered.add(row.itemKey()));
    assertEquals(expected, offered);
    // The bundled client copy is what the screen really reads; it must agree with the exporter.
    var bundled = new java.util.TreeSet<String>();
    OnlineAbilityChoices.usablePotions(progress(Set.of(), inventory, Map.of()), OnlineSpellAllowlist.bundled())
        .forEach(row -> bundled.add(row.itemKey()));
    assertEquals(expected, bundled);
  }

  @Test
  void aDroppedPotionStackIsOfferedWithTheChargeTheServerGivesDrops() {
    // The server's loot draft gives a one-charge consumable one charge (OnlineLootDrafts.charges), so
    // a Skeleton's Healing Potion is offered; an instance provisioned with zero charges is not usable.
    var dropped = new Item(UUID.randomUUID(), "item.healing_potion", 19, 1);
    var zeroCharge = new Item(UUID.randomUUID(), "item.healing_potion", 19, 0);
    assertEquals(1, OnlineAbilityChoices.usablePotions(progress(Set.of(), List.of(dropped), Map.of()),
        OnlineSpellAllowlist.bundled()).size());
    assertTrue(OnlineAbilityChoices.usablePotions(progress(Set.of(), List.of(zeroCharge), Map.of()),
        OnlineSpellAllowlist.bundled()).isEmpty());
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
