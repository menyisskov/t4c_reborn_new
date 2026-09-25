package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The account vault is a shared, on-disk singleton, so every test runs against a scratch working
 * directory and resets {@link AccountStorage} before and after, to avoid polluting the repo's own
 * {@code account_storage.json} or leaking state between tests.
 */
class AccountStorageServiceTest {
  private static final String DAGGER = "item.dagger";
  private String originalUserDir;

  @TempDir Path tempDir;

  private static Player strongPlayer() throws Exception {
    Player player = new Player();
    player.setStrength(400);
    return player;
  }

  @BeforeEach
  void redirectToScratchDir() throws Exception {
    originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", tempDir.toString());
    resetSingleton();
  }

  @AfterEach
  void restoreUserDir() throws Exception {
    System.setProperty("user.dir", originalUserDir);
    resetSingleton();
  }

  private static void resetSingleton() throws Exception {
    Method reset = AccountStorage.class.getDeclaredMethod("reset");
    reset.setAccessible(true);
    reset.invoke(null);
  }

  @Test
  void depositAndWithdrawMoveTheItemBetweenBackpackAndVault() throws Exception {
    Player player = strongPlayer();
    player.setInventory(List.of(DAGGER));
    player.setInventoryDurability(List.of(50d));

    assertTrue(AccountStorageService.deposit(player, 0, DAGGER).success());
    assertTrue(player.getInventory().isEmpty());
    assertEquals(List.of(DAGGER), AccountStorage.get().storage());

    assertTrue(AccountStorageService.withdraw(player, 0, DAGGER).success());
    assertEquals(List.of(DAGGER), player.getInventory());
    assertTrue(AccountStorage.get().storage().isEmpty());
  }

  @Test
  void goldMovesBetweenCharacterAndVault() throws Exception {
    Player player = strongPlayer();
    player.setGold(500);

    AccountStorageService.depositGold(player, 300);
    assertEquals(200, player.getGold());
    assertEquals(300, AccountStorage.get().storageGold());

    AccountStorageService.withdrawGold(player, 300);
    assertEquals(500, player.getGold());
    assertEquals(0, AccountStorage.get().storageGold());
  }

  @Test
  void vaultContentsSurviveAReloadFromDisk() throws Exception {
    Player player = strongPlayer();
    player.setInventory(List.of(DAGGER));
    player.setInventoryDurability(List.of(12d));
    player.setGold(750);
    AccountStorageService.deposit(player, 0, DAGGER);
    AccountStorageService.depositGold(player, 750);
    AccountStorage.get().save();

    resetSingleton();

    assertEquals(List.of(DAGGER), AccountStorage.get().storage());
    assertEquals(750, AccountStorage.get().storageGold());
    assertEquals(12d, AccountStorage.get().storageDurability().get(0));
  }

  @Test
  void isSharedAcrossDifferentCharactersOnTheSameRoster() throws Exception {
    Player alice = strongPlayer();
    alice.setInventory(List.of(DAGGER));
    alice.setInventoryDurability(List.of(100d));
    AccountStorageService.deposit(alice, 0, DAGGER);

    Player bob = strongPlayer();
    assertTrue(AccountStorageService.withdraw(bob, 0, DAGGER).success());
    assertEquals(List.of(DAGGER), bob.getInventory());
  }
}
