package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.CharacterProgress.Item;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.spell.engine.SpellEngine;
import com.perso.T4C.tools.OnlineSpellContentExporter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class OnlineAbilityMenuTest {
  private static final SpellContent ALLOWLIST = OnlineSpellContentExporter.snapshot();
  private static final SpellContentV2 ENGINE_CATALOG = OnlineSpellAllowlistV2.bundled();

  @AfterAll
  static void resetSharedItemRegistry() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  private static CharacterProgress progress(int hp, Set<String> spells, List<Item> inventory) {
    return new CharacterProgress(
        1, "warrior", "male", new CharacterProgress.Attributes(10, 10, 10, 10, 10),
        new CharacterProgress.Vitals(hp, 20, 5, 10), new CharacterProgress.Advancement(1, 0, 0, 0, 0),
        inventory, Map.of(), Map.of(), spells, Map.of(), List.of(), 0, new CharacterProgress.Respawn(0, 1, 1),
        List.of(), Map.of(), 0, null);
  }

  private static String castable() {
    return ENGINE_CATALOG.spells().values().stream()
        .filter(s -> s.stage() <= OnlineSpellAllowlistV2.ENABLED_STAGE && SpellEngine.supported(s))
        .map(SpellContentV2.Spell::key).sorted().findFirst().orElseThrow();
  }

  private static List<String> castables() {
    return ENGINE_CATALOG.spells().values().stream()
        .filter(s -> s.stage() <= OnlineSpellAllowlistV2.ENABLED_STAGE && SpellEngine.supported(s))
        .map(SpellContentV2.Spell::key).sorted().toList();
  }

  private static String targetedCastable() {
    return ENGINE_CATALOG.spells().values().stream()
        .filter(s -> s.stage() <= OnlineSpellAllowlistV2.ENABLED_STAGE && SpellEngine.supported(s))
        .filter(s -> s.kind() == SpellContentV2.Kind.NUKE || s.kind() == SpellContentV2.Kind.DRAIN
            || s.kind() == SpellContentV2.Kind.DEBUFF)
        .map(SpellContentV2.Spell::key).sorted().findFirst().orElseThrow();
  }

  private static String positionTargetCastable() {
    return ENGINE_CATALOG.spells().values().stream()
        .filter(s -> s.kind() == SpellContentV2.Kind.AREA_DAMAGE && s.targetType() == 19
            && !s.selfCenteredAttack() && s.stage() <= OnlineSpellAllowlistV2.ENABLED_STAGE
            && SpellEngine.supported(s))
        .map(SpellContentV2.Spell::key).sorted().findFirst().orElseThrow();
  }

  private static Item potionItem() {
    var ids = ALLOWLIST.spells().values().stream().filter(s -> s.use() == SpellContent.Use.ITEM)
        .map(SpellContent.Spell::id).toList();
    var definition = ItemRegistry.allByKey().values().stream()
        .filter(i -> i.getBodyPart() == null && i.getNbCharges() == 1 && !i.isUnlimitedUse())
        .filter(i -> i.getSpells().stream().anyMatch(s -> ids.contains(s.getSpellId())))
        .findFirst().orElseThrow();
    String key = definition.getKey().startsWith("item.") ? definition.getKey() : "item." + definition.getKey();
    return new Item(UUID.randomUUID(), key, 2, 1);
  }

  @Test
  void keysOpenSwitchAndCloseTheLists() {
    var menu = new OnlineAbilityMenu();
    assertFalse(menu.isOpen());
    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);
    assertEquals(OnlineAbilityMenu.Kind.SPELLS, menu.kind());
    menu.toggle(OnlineAbilityMenu.Kind.POTIONS, true);
    assertEquals(OnlineAbilityMenu.Kind.POTIONS, menu.kind());
    menu.toggle(OnlineAbilityMenu.Kind.POTIONS, true);
    assertFalse(menu.isOpen());
    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);
    menu.close();
    assertFalse(menu.isOpen());
  }

  @Test
  void deadOrDisconnectedCharactersCannotOpenOrUseTheLists() {
    var alive = progress(10, Set.of(castable()), List.of());
    assertTrue(OnlineAbilityMenu.available("connected", alive));
    assertFalse(OnlineAbilityMenu.available("disconnected", alive));
    assertFalse(OnlineAbilityMenu.available("connected", progress(0, Set.of(), List.of())));
    assertFalse(OnlineAbilityMenu.available("connected", null));

    var menu = new OnlineAbilityMenu();
    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, false);
    assertFalse(menu.isOpen());

    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);
    assertTrue(menu.choose(1, alive, ENGINE_CATALOG, ALLOWLIST, false).isEmpty());
    assertFalse(menu.isOpen(), "An unavailable character closes the list instead of casting");
  }

  @Test
  void digitCastsTheChosenSpellRowAndClosesTheList() {
    var key = castable();
    var state = progress(10, Set.of(key), List.of());
    var menu = new OnlineAbilityMenu();
    assertTrue(menu.choose(1, state, ENGINE_CATALOG, ALLOWLIST, true).isEmpty(), "A closed list ignores digits");

    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);
    assertTrue(menu.choose(2, state, ENGINE_CATALOG, ALLOWLIST, true).isEmpty());
    assertTrue(menu.isOpen(), "A digit with no row leaves the list open");
    var action = menu.choose(1, state, ENGINE_CATALOG, ALLOWLIST, true).orElseThrow();
    assertEquals(OnlineAbilityMenu.Kind.SPELLS, action.kind());
    assertEquals(key, action.spellKey());
    assertNull(action.item());
    assertEquals(ENGINE_CATALOG.spells().get(key).kind() == SpellContentV2.Kind.NUKE
        || ENGINE_CATALOG.spells().get(key).kind() == SpellContentV2.Kind.DRAIN
        || ENGINE_CATALOG.spells().get(key).kind() == SpellContentV2.Kind.DEBUFF, action.requiresCreatureTarget());
    assertFalse(menu.isOpen());
  }

  @Test
  void targetedSpellSelectionCarriesTheMonsterTargetRequirement() {
    var key = targetedCastable();
    var menu = new OnlineAbilityMenu();
    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);

    var action = menu.choose(1, progress(10, Set.of(key), List.of()), ENGINE_CATALOG, ALLOWLIST, true).orElseThrow();

    assertEquals(key, action.spellKey());
    assertTrue(action.requiresCreatureTarget());
  }

  @Test
  void tileAimedAreaSelectionCarriesPositionTargetRequirement() {
    var key = positionTargetCastable();
    var menu = new OnlineAbilityMenu();
    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);

    var action = menu.choose(1, progress(10, Set.of(key), List.of()), ENGINE_CATALOG, ALLOWLIST, true).orElseThrow();

    assertEquals(key, action.spellKey());
    assertTrue(action.requiresPositionTarget());
    assertFalse(action.requiresCreatureTarget());
  }

  @Test
  void learnedWordOfRecallIsSelectableWithoutACreatureTarget() {
    var menu = new OnlineAbilityMenu();
    var state = progress(10, Set.of("spell.word_of_recall"), List.of());
    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);

    var action = menu.choose(1, state, ENGINE_CATALOG, ALLOWLIST, true).orElseThrow();

    assertEquals("spell.word_of_recall", action.spellKey());
    assertFalse(action.requiresCreatureTarget());
  }

  @Test
  void targetedDispatchCanResolveOnlyTheSelectedLiveCreatureFromTheLatestSnapshot() {
    var live = new CreatureView(42, "Goblin", 2, 3, 5, 5, "walk", "attack", "death");
    var dead = new CreatureView(43, "Goblin", 2, 3, 0, 5, "walk", "attack", "death");

    assertEquals(42, OnlineAbilityMenu.liveCreatureTarget(42, List.of(live, dead)).orElseThrow());
    assertTrue(OnlineAbilityMenu.liveCreatureTarget(43, List.of(live, dead)).isEmpty());
    assertTrue(OnlineAbilityMenu.liveCreatureTarget(99, List.of(live, dead)).isEmpty());
    assertTrue(OnlineAbilityMenu.liveCreatureTarget(42, null).isEmpty());
  }

  @Test
  void digitUsesTheChosenPotionInstance() {
    var potion = potionItem();
    var state = progress(10, Set.of(), List.of(potion));
    var menu = new OnlineAbilityMenu();
    menu.toggle(OnlineAbilityMenu.Kind.POTIONS, true);
    var action = menu.choose(1, state, ENGINE_CATALOG, ALLOWLIST, true).orElseThrow();
    assertEquals(OnlineAbilityMenu.Kind.POTIONS, action.kind());
    assertEquals(potion.id(), action.item());
    assertNull(action.spellKey());
    assertFalse(action.requiresCreatureTarget());
  }

  @Test
  void learnedSpellsArePagedAndDigitsSelectWithinTheCurrentPage() {
    var keys = castables();
    assertTrue(keys.size() > OnlineAbilityMenu.MAX_CHOICES, "Fixture must exercise paging");
    var state = progress(10, Set.copyOf(keys), List.of());
    var menu = new OnlineAbilityMenu();
    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);
    menu.turnPage(1, state);

    var lines = menu.lines(state, ENGINE_CATALOG, ALLOWLIST);
    assertTrue(lines.get(1).startsWith("1. "), lines.get(1));
    assertTrue(lines.stream().anyMatch(line -> line.contains("Page 2 of")));
    assertEquals(keys.get(OnlineAbilityMenu.MAX_CHOICES),
        menu.choose(1, state, ENGINE_CATALOG, ALLOWLIST, true).orElseThrow().spellKey());
  }

  @Test
  void usablePotionsArePagedAndPageClampsWhenInventoryShrinks() {
    var potion = potionItem();
    var items = java.util.stream.IntStream.range(0, OnlineAbilityMenu.MAX_CHOICES + 1)
        .mapToObj(i -> new Item(UUID.randomUUID(), potion.key(), 1, 1)).toList();
    var state = progress(10, Set.of(), items);
    var menu = new OnlineAbilityMenu();
    menu.toggle(OnlineAbilityMenu.Kind.POTIONS, true);
    menu.turnPage(1, state);

    assertEquals(1, menu.page());
    assertTrue(menu.lines(state, ENGINE_CATALOG, ALLOWLIST).stream().anyMatch(line -> line.contains("Page 2 of 2")));
    assertEquals(items.getLast().id(),
        menu.choose(1, state, ENGINE_CATALOG, ALLOWLIST, true).orElseThrow().item());
    menu.toggle(OnlineAbilityMenu.Kind.POTIONS, true);
    menu.turnPage(1, state);
    var afterConsumption = menu.lines(progress(10, Set.of(), List.of()), ENGINE_CATALOG, ALLOWLIST);
    assertEquals(0, menu.page());
    assertFalse(afterConsumption.stream().anyMatch(line -> line.contains("Page ")));
    assertEquals("You carry nothing you can use online.", afterConsumption.get(1));
  }

  @Test
  void linesShowRowsWithTheManaCostFormulaAndAnEmptyNote() {
    var key = castable();
    var menu = new OnlineAbilityMenu();
    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);
    var lines = menu.lines(progress(10, Set.of(key), List.of()), ENGINE_CATALOG, ALLOWLIST);
    assertEquals(3, lines.size(), lines.toString());
    assertTrue(lines.get(1).startsWith("1. "), lines.get(1));
    assertTrue(lines.get(1).endsWith(" " + ENGINE_CATALOG.spells().get(key).manaCost().trim() + ")"), lines.get(1));
    assertTrue(lines.getLast().contains("Esc"));

    var empty = menu.lines(progress(10, Set.of(), List.of()), ENGINE_CATALOG, ALLOWLIST);
    assertEquals(3, empty.size());
    assertEquals("You have no spells you can cast online.", empty.get(1));

    menu.toggle(OnlineAbilityMenu.Kind.POTIONS, true);
    var potions = menu.lines(progress(10, Set.of(), List.of(potionItem())), ENGINE_CATALOG, ALLOWLIST);
    assertTrue(potions.get(1).startsWith("1. ") && potions.get(1).endsWith(" x2"), potions.get(1));
  }

  @Test
  void closedListHasNoLines() {
    assertTrue(new OnlineAbilityMenu().lines(null, ENGINE_CATALOG, ALLOWLIST).isEmpty());
  }
}
