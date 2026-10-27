package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.CharacterProgress.Item;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.tools.OnlineSpellContentExporter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class OnlineAbilityMenuTest {
  private static final SpellContent ALLOWLIST = OnlineSpellContentExporter.snapshot();

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
    return ALLOWLIST.spells().values().stream().filter(s -> s.use() == SpellContent.Use.CAST)
        .map(SpellContent.Spell::key).findFirst().orElseThrow();
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
    assertTrue(menu.choose(1, alive, ALLOWLIST, false).isEmpty());
    assertFalse(menu.isOpen(), "An unavailable character closes the list instead of casting");
  }

  @Test
  void digitCastsTheChosenSpellRowAndClosesTheList() {
    var key = castable();
    var state = progress(10, Set.of(key), List.of());
    var menu = new OnlineAbilityMenu();
    assertTrue(menu.choose(1, state, ALLOWLIST, true).isEmpty(), "A closed list ignores digits");

    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);
    assertTrue(menu.choose(2, state, ALLOWLIST, true).isEmpty());
    assertTrue(menu.isOpen(), "A digit with no row leaves the list open");
    var action = menu.choose(1, state, ALLOWLIST, true).orElseThrow();
    assertEquals(OnlineAbilityMenu.Kind.SPELLS, action.kind());
    assertEquals(key, action.spellKey());
    assertNull(action.item());
    assertFalse(menu.isOpen());
  }

  @Test
  void digitUsesTheChosenPotionInstance() {
    var potion = potionItem();
    var state = progress(10, Set.of(), List.of(potion));
    var menu = new OnlineAbilityMenu();
    menu.toggle(OnlineAbilityMenu.Kind.POTIONS, true);
    var action = menu.choose(1, state, ALLOWLIST, true).orElseThrow();
    assertEquals(OnlineAbilityMenu.Kind.POTIONS, action.kind());
    assertEquals(potion.id(), action.item());
    assertNull(action.spellKey());
  }

  @Test
  void linesShowRowsWithTheManaCostFormulaAndAnEmptyNote() {
    var key = castable();
    var menu = new OnlineAbilityMenu();
    menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);
    var lines = menu.lines(progress(10, Set.of(key), List.of()), ALLOWLIST);
    assertEquals(3, lines.size(), lines.toString());
    assertTrue(lines.get(1).startsWith("1. "), lines.get(1));
    assertTrue(lines.get(1).endsWith(" " + ALLOWLIST.spells().get(key).manaCost().trim() + ")"), lines.get(1));
    assertTrue(lines.getLast().contains("Esc"));

    var empty = menu.lines(progress(10, Set.of(), List.of()), ALLOWLIST);
    assertEquals(3, empty.size());
    assertEquals("You have no spells you can cast online.", empty.get(1));

    menu.toggle(OnlineAbilityMenu.Kind.POTIONS, true);
    var potions = menu.lines(progress(10, Set.of(), List.of(potionItem())), ALLOWLIST);
    assertTrue(potions.get(1).startsWith("1. ") && potions.get(1).endsWith(" x2"), potions.get(1));
  }

  @Test
  void closedListHasNoLines() {
    assertTrue(new OnlineAbilityMenu().lines(null, ALLOWLIST).isEmpty());
  }
}
