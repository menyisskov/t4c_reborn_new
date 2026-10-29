package com.perso.T4C.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.player.Player;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Characterizes the single-player click pickup (T4C-0292) before the online hook exists: it must
 * pass unchanged afterwards. A pickup hits the topmost item under the click, refuses beyond the
 * reach it is given, moves the item into the inventory and pays a corpse's gold once.
 */
class GroundItemPickupCharacterizationTest {
  private static final String DAGGER = "item.dagger";

  @BeforeAll
  static void items() {
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister();
  }

  private static Player strong() {
    Player player = new Player();
    player.setStrength(500);
    return player;
  }

  @SuppressWarnings("unchecked")
  private static List<GroundItem> items(GroundItemManager ground) throws Exception {
    Field field = GroundItemManager.class.getDeclaredField("items");
    field.setAccessible(true);
    return (List<GroundItem>) field.get(ground);
  }

  @Test
  void nothingOnTheGroundOrNoPlayerIsNone() {
    GroundItemManager ground = new GroundItemManager();
    assertEquals(
        GroundItemManager.PickupStatus.NONE,
        ground.pickUpAt(10, 10, new Player(), 0, 3).status);
    ground.addRemoteItem(GroundItem.ofItem(DAGGER, 10, 10));
    assertEquals(GroundItemManager.PickupStatus.NONE, ground.pickUpAt(10, 10, null, 0, 3).status);
  }

  @Test
  void aClickAwayFromEveryItemIsNone() throws Exception {
    GroundItemManager ground = new GroundItemManager();
    ground.addRemoteItem(GroundItem.ofItem(DAGGER, 100, 100));
    assertEquals(
        GroundItemManager.PickupStatus.NONE,
        ground.pickUpAt(400, 400, new Player(), 0, 3).status);
    assertEquals(1, items(ground).size());
  }

  @Test
  void beyondTheGivenReachTheItemStaysAndTheLabelIsReturned() throws Exception {
    GroundItemManager ground = new GroundItemManager();
    GroundItem dagger = GroundItem.ofItem(DAGGER, 100, 100);
    ground.addRemoteItem(dagger);
    Player player = strong();
    GroundItemManager.PickupResult result = ground.pickUpAt(100, 100, player, 4, 3);
    assertEquals(GroundItemManager.PickupStatus.TOO_FAR, result.status);
    assertEquals(dagger.describe(), result.label);
    assertEquals(List.of(dagger), items(ground));
    assertEquals(0, InventoryService.count(player, DAGGER));
  }

  @Test
  void withinReachTheItemMovesIntoTheInventory() throws Exception {
    GroundItemManager ground = new GroundItemManager();
    GroundItem dagger = GroundItem.ofItem(DAGGER, 100, 100);
    ground.addRemoteItem(dagger);
    Player player = strong();
    GroundItemManager.PickupResult result = ground.pickUpAt(100, 100, player, 3, 3);
    assertEquals(GroundItemManager.PickupStatus.PICKED_UP, result.status);
    assertEquals(dagger.describe(), result.label);
    assertTrue(items(ground).isEmpty());
    assertEquals(1, InventoryService.count(player, DAGGER));
  }

  @Test
  void theTopmostItemUnderTheClickIsTakenFirst() throws Exception {
    GroundItemManager ground = new GroundItemManager();
    GroundItem below = GroundItem.ofItem(DAGGER, 100, 100);
    GroundItem above = GroundItem.ofItem(DAGGER, 102, 100);
    ground.addRemoteItem(below);
    ground.addRemoteItem(above);
    ground.pickUpAt(101, 100, strong(), 0, 3);
    assertEquals(List.of(below), items(ground));
    assertSame(below, items(ground).get(0));
  }

  @Test
  void aCorpsesGoldIsPaidOnceWhateverNumberOfItemsAreTaken() throws Exception {
    GroundItemManager ground = new GroundItemManager();
    ground.spawnCorpse(List.of(DAGGER, DAGGER), 40, 500, 500);
    Player player = strong();
    int before = player.getGold();
    // The items scatter around the corpse; take them wherever they lie.
    for (GroundItem item : List.copyOf(items(ground))) {
      ground.pickUpAt(item.getPosition().x, item.getPosition().y, player, 0, 3);
    }
    assertTrue(items(ground).isEmpty());
    assertEquals(2, InventoryService.count(player, DAGGER));
    assertEquals(before + 40, player.getGold());
  }

  @Test
  void theDefaultManagerHasNoRemoteHookAndStaysLocal() {
    assertNull(new GroundItemManager().pickUpAt(0, 0, new Player(), 0, 3).label);
  }
}
