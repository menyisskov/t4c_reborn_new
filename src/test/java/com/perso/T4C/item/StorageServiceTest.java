package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.player.Player;
import java.util.List;
import org.junit.jupiter.api.Test;

class StorageServiceTest {
  private static final String DAGGER = "item.dagger";
  private static final String BOOK = "item.book_of_psalms"; // 12 charges

  private static Player strongPlayer() throws Exception {
    Player player = new Player();
    player.setStrength(400);
    return player;
  }

  @Test
  void storedItemsAndGoldSurviveASaveAndReload() throws Exception {
    Player source = strongPlayer();
    source.setInventory(List.of(DAGGER, DAGGER));
    source.setGold(500);
    StorageService.depositMany(source, DAGGER, 2);
    StorageService.depositGold(source, 300);

    PlayerStateDto state = PlayerStateMapper.fromPlayer(source);
    Player restored = strongPlayer();
    PlayerStateMapper.applyToPlayer(state, restored);

    assertEquals(List.of(DAGGER, DAGGER), restored.getStorage());
    assertEquals(300, restored.getStorageGold());
    assertEquals(200, restored.getGold());
  }

  @Test
  void depositingAndWithdrawingKeepsDurability() throws Exception {
    Player player = strongPlayer();
    player.setInventory(List.of(DAGGER));
    player.setInventoryDurability(List.of(37d));

    assertTrue(StorageService.deposit(player, 0, DAGGER).success());
    assertEquals(37d, StorageService.durability(player, 0));

    assertTrue(StorageService.withdraw(player, 0, DAGGER).success());
    assertEquals(37d, ItemDurabilityService.inventory(player, 0), "storage must not repair items");
    assertTrue(player.getStorage().isEmpty());
  }

  @Test
  void storedDurabilitySurvivesASaveAndReload() throws Exception {
    Player source = strongPlayer();
    source.setInventory(List.of(DAGGER));
    source.setInventoryDurability(List.of(12d));
    StorageService.deposit(source, 0, DAGGER);

    Player restored = strongPlayer();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(source), restored);
    assertEquals(12d, StorageService.durability(restored, 0));
  }

  @Test
  void depositingAndWithdrawingKeepsRemainingCharges() throws Exception {
    Player player = strongPlayer();
    assertTrue(InventoryService.add(player, BOOK, 5).success());
    assertEquals(5, player.getItemCharges().get(BOOK));

    assertTrue(StorageService.deposit(player, 0, BOOK).success());
    assertFalse(player.getItemCharges().containsKey(BOOK));

    assertTrue(StorageService.withdraw(player, 0, BOOK).success());
    assertEquals(5, player.getItemCharges().get(BOOK), "storage must not recharge items");
  }

  @Test
  void legacySavesWithoutStorageMetadataLoadAtFullDurabilityAndCharges() throws Exception {
    PlayerStateDto state = PlayerStateMapper.fromPlayer(strongPlayer());
    state.storage = List.of(DAGGER, BOOK);
    state.storageDurability = null;
    state.storageCharges = null;
    Player restored = strongPlayer();
    PlayerStateMapper.applyToPlayer(state, restored);

    assertEquals(ItemDurabilityService.MAX, StorageService.durability(restored, 0));
    assertTrue(StorageService.withdraw(restored, 1, BOOK).success());
    assertEquals(12, restored.getItemCharges().get(BOOK));
  }

  @Test
  void withdrawingStopsAndReportsWhenTheItemIsTooHeavy() throws Exception {
    Player player = new Player();
    player.setStrength(0);
    player.getStorage().add(DAGGER);
    player.getStorage().add(DAGGER);

    StorageService.Transfer transfer = StorageService.withdrawMany(player, DAGGER, 2);
    assertEquals(0, transfer.moved());
    assertEquals(InventoryService.Failure.TOO_HEAVY, transfer.lastResult().failure());
    assertEquals(2, player.getStorage().size(), "nothing is lost when a withdrawal fails");
  }

  @Test
  void depositAllOnlyMovesTheRequestedCategory() throws Exception {
    Player player = strongPlayer();
    player.setInventory(List.of(DAGGER, BOOK, DAGGER));

    int moved = StorageService.depositAll(player, StorageService.Category.WEAPONS);
    assertEquals(2, moved);
    assertEquals(List.of(BOOK), player.getInventory());
    assertEquals(List.of(DAGGER, DAGGER), player.getStorage());
  }
}
