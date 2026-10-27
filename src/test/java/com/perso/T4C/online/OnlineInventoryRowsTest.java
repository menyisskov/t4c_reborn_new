package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.OnlineInventoryRows.Kind;
import com.perso.T4C.online.OnlineInventoryRows.Row;
import com.perso.T4C.online.model.CharacterProgress.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineInventoryRowsTest {
  private static final UUID SWORD = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID SHIELD = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID BAG = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @Test
  void equippedRowsComeFirstInSlotOrderThenCarriedItemsInInventoryOrder() {
    var inventory = List.of(
        new Item(BAG, "item.bag", 1, 0), new Item(SWORD, "item.sword", 1, 0), new Item(SHIELD, "item.shield", 1, 0));

    var rows = OnlineInventoryRows.of(inventory, Map.of("WEAPON", SWORD));

    assertEquals(List.of(
        new Row(Kind.EQUIPPED, SWORD, "WEAPON", "item.sword"),
        new Row(Kind.CARRIED, BAG, null, "item.bag"),
        new Row(Kind.CARRIED, SHIELD, null, "item.shield")), rows);
  }

  @Test
  void unknownSlotsAndDanglingEquipmentAreDroppedAndTheItemStaysCarried() {
    var inventory = List.of(new Item(SWORD, "item.sword", 1, 0));

    var rows = OnlineInventoryRows.of(inventory, Map.of("NOT_A_SLOT", SWORD, "WEAPON", UUID.randomUUID()));

    assertEquals(List.of(new Row(Kind.CARRIED, SWORD, null, "item.sword")), rows);
  }

  @Test
  void numberKeysAddressOnlyTheFirstNineRows() {
    var inventory = new ArrayList<Item>();
    for (int i = 0; i < 10; i++) inventory.add(new Item(UUID.randomUUID(), "item.coin" + i, 1, 0));
    var rows = OnlineInventoryRows.of(inventory, Map.of());

    assertEquals(inventory.get(0).id(), OnlineInventoryRows.choose(rows, 1).orElseThrow().instance());
    assertEquals(inventory.get(8).id(), OnlineInventoryRows.choose(rows, 9).orElseThrow().instance());
    assertTrue(OnlineInventoryRows.choose(rows, 10).isEmpty());
    assertTrue(OnlineInventoryRows.choose(rows, 0).isEmpty());
  }
}
