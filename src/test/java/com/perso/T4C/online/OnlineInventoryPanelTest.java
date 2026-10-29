package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.CharacterProgress.Item;
import com.perso.T4C.player.BodyPart;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineInventoryPanelTest {
  private static final UUID SWORD = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID POTION = UUID.fromString("00000000-0000-0000-0000-000000000002");

  private static CharacterProgress progress(List<Item> inventory, Map<String, UUID> equipment) {
    return new CharacterProgress(
        2, "warrior", "male",
        new CharacterProgress.Attributes(10, 10, 10, 10, 10),
        new CharacterProgress.Vitals(10, 10, 0, 0),
        new CharacterProgress.Advancement(1, 0, 0, 0, 0), inventory, equipment,
        Map.of(), Set.of(), Map.of(), List.of(), 0,
        new CharacterProgress.Respawn(0, 0, 0), List.of(), Map.of(), 0, null);
  }

  @Test
  void committedItemsShowQuantityChargesAndEquippedSlotAndSelectionUsesIdentity() {
    var sword = new Item(SWORD, "item.sword", 1, 0);
    var potion = new Item(POTION, "item.potion", 4, 3);
    var state = progress(List.of(sword, potion), Map.of("WEAPON", SWORD));
    var panel = new OnlineInventoryPanel();
    panel.open();

    var lines = panel.lines(state, key -> key);

    assertTrue(lines.stream().anyMatch(line -> line.contains("Unequip item.sword (WEAPON)")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("Equip item.potion x4 (3)")));
    assertEquals(new OnlineInventoryPanel.Action(SWORD, BodyPart.WEAPON), panel.choose(1, state).orElseThrow());
    assertEquals(new OnlineInventoryPanel.Action(POTION, null), panel.choose(2, state).orElseThrow());
    assertTrue(panel.choose(3, state).isEmpty());
  }

  @Test
  void selectionsAreRejectedAfterServerInventoryChangesAndPagesClamp() {
    var items = new ArrayList<Item>();
    for (int i = 0; i < 10; i++)
      items.add(new Item(UUID.nameUUIDFromBytes(("item" + i).getBytes()), "item.coin", 1, 0));
    var panel = new OnlineInventoryPanel();
    var state = progress(items, Map.of());
    panel.open();
    panel.lines(state, key -> key);
    assertTrue(panel.choose(9, state).isPresent());

    panel.turnPage(1, state);
    panel.lines(state, key -> key);
    assertEquals(items.get(9).id(), panel.choose(1, state).orElseThrow().item());

    var reduced = progress(items.subList(0, 2), Map.of());
    assertTrue(panel.choose(1, reduced).isEmpty());
    panel.lines(reduced, key -> key);
    assertEquals(items.get(0).id(), panel.choose(1, reduced).orElseThrow().item());
    assertFalse(panel.choose(3, reduced).isPresent());
  }
}
