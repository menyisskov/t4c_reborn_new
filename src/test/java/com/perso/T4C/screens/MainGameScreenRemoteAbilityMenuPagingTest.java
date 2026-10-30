package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.OnlineAbilityMenu;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.tools.OnlineSpellContentExporter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MainGameScreenRemoteAbilityMenuPagingTest {
  @Test
  void leftAndRightKeysPageAnOpenPotionMenuWithoutClaimingOtherKeys() {
    var allowlist = OnlineSpellContentExporter.snapshot();
    var potionSpellIds = allowlist.spells().values().stream()
        .filter(spell -> spell.use() == SpellContent.Use.ITEM)
        .map(SpellContent.Spell::id).toList();
    var definition = ItemRegistry.allByKey().values().stream()
        .filter(item -> item.getBodyPart() == null && item.getNbCharges() == 1 && !item.isUnlimitedUse())
        .filter(item -> item.getSpells().stream().anyMatch(spell -> potionSpellIds.contains(spell.getSpellId())))
        .findFirst().orElseThrow();
    String itemKey = definition.getKey().startsWith("item.") ? definition.getKey() : "item." + definition.getKey();
    var inventory = java.util.stream.IntStream.range(0, OnlineAbilityMenu.MAX_CHOICES + 1)
        .mapToObj(i -> new CharacterProgress.Item(UUID.randomUUID(), itemKey, 1, 1)).toList();
    var progress = new CharacterProgress(
        1, "warrior", "male", new CharacterProgress.Attributes(10, 10, 10, 10, 10),
        new CharacterProgress.Vitals(10, 20, 5, 10), new CharacterProgress.Advancement(1, 0, 0, 0, 0),
        inventory, Map.of(), Map.of(), Set.of(), Map.of(), List.of(), 0,
        new CharacterProgress.Respawn(0, 1, 1), List.of(), Map.of(), 0, null);
    var menu = new OnlineAbilityMenu();
    menu.toggle(OnlineAbilityMenu.Kind.POTIONS, true);

    assertFalse(MainGameScreen.turnRemoteAbilityMenuPage(menu, Input.Keys.NUM_1, progress));
    assertTrue(MainGameScreen.turnRemoteAbilityMenuPage(menu, Input.Keys.RIGHT, progress));
    assertEquals(1, menu.page());
    assertTrue(MainGameScreen.turnRemoteAbilityMenuPage(menu, Input.Keys.LEFT, progress));
    assertEquals(0, menu.page());
  }
}
