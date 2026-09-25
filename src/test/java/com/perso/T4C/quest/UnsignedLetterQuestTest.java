package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** T4C-0050: "The Unsigned Letter" - granted on a first rebirth, resolved with Ysmera. */
class UnsignedLetterQuestTest {
  @BeforeEach
  void loadItems() {
    ItemJsonLoader.loadAndRegister("assets/items");
  }

  /** A fresh {@code new Player()} has near-zero carry weight, which would make
   * {@code InventoryService.canAdd} silently refuse the letter - not what these tests are
   * about, so give every player enough strength to actually carry a one-weight token. */
  private static Player playerAbleToCarryTheLetter() {
    Player player = new Player();
    player.setStrength(400);
    return player;
  }

  @Test
  void firstRebirthGrantsTheLetterAndTheRevealMessage() throws Exception {
    Player player = playerAbleToCarryTheLetter();
    player.setRebirthCount(1);

    String reveal = UnsignedLetterQuest.onRebirth(player);

    assertTrue(
        reveal != null && !reveal.isBlank() && !reveal.startsWith("${"),
        "resolves through I18n, not a raw placeholder: " + reveal);
    assertEquals(UnsignedLetterQuest.STAGE_RECEIVED, UnsignedLetterQuest.stage(player));
    assertTrue(player.getInventory().contains(UnsignedLetterQuest.LETTER_ITEM_KEY));
  }

  @Test
  void laterRebirthsGrantNothing() throws Exception {
    Player player = new Player();
    player.setRebirthCount(2);

    assertNull(UnsignedLetterQuest.onRebirth(player));
    assertEquals(UnsignedLetterQuest.STAGE_NONE, UnsignedLetterQuest.stage(player));
    assertFalse(player.getInventory().contains(UnsignedLetterQuest.LETTER_ITEM_KEY));
  }

  @Test
  void onRebirthIsIdempotentEvenIfCalledTwiceForTheSameFirstRebirth() throws Exception {
    Player player = playerAbleToCarryTheLetter();
    player.setRebirthCount(1);
    UnsignedLetterQuest.onRebirth(player);

    // Simulates a second call site running (or a replayed event) after the flag already
    // advanced - must not hand out a second letter.
    assertNull(UnsignedLetterQuest.onRebirth(player));
    assertEquals(1, player.getInventory().size());
  }

  @Test
  void resolveRequiresBothTheStageAndTheLetterInHand() throws Exception {
    Player player = playerAbleToCarryTheLetter();

    assertFalse(UnsignedLetterQuest.resolve(player), "never received one");

    player.setRebirthCount(1);
    UnsignedLetterQuest.onRebirth(player);
    player.getInventory().remove(UnsignedLetterQuest.LETTER_ITEM_KEY);

    assertFalse(UnsignedLetterQuest.resolve(player), "letter is gone");
  }

  @Test
  void resolveConsumesTheLetterAndAdvancesTheStageExactlyOnce() throws Exception {
    Player player = playerAbleToCarryTheLetter();
    player.setRebirthCount(1);
    UnsignedLetterQuest.onRebirth(player);

    assertTrue(UnsignedLetterQuest.resolve(player));
    assertEquals(UnsignedLetterQuest.STAGE_RESOLVED, UnsignedLetterQuest.stage(player));
    assertFalse(player.getInventory().contains(UnsignedLetterQuest.LETTER_ITEM_KEY));

    assertFalse(UnsignedLetterQuest.resolve(player), "already resolved");
  }
}
