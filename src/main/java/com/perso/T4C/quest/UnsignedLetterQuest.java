package com.perso.T4C.quest;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.player.Player;

/**
 * T4C-0050, "The Unsigned Letter": a short story beat that starts the moment a character is
 * reborn for the first time - someone already knew it would happen, and left a letter waiting.
 * Not a {@link QuestDef} (there's no kill-count objective here, just an item and a
 * conversation), so this is its own small piece of state, the same shape {@code MirrorTrials}
 * and {@code HourglassTrials} use: plain quest flags, no save-format change.
 *
 * <p>{@link #onRebirth} is called from {@code RebirthBehavior.perform}'s two callers
 * ({@code npc/Oracle.java}, {@code npc/AnchoriteRowan.java}) right after a rebirth succeeds. The
 * story resolves at {@code npc/MirrorwardenYsmera.java} - she's the one who left it (see that
 * class's new "letter" topic).
 */
public final class UnsignedLetterQuest {
  public static final String LETTER_ITEM_KEY = "item.unsigned_letter";

  /** 0 = never rebirthed (or rebirthed before this feature shipped and never noticed); 1 =
   * holding the letter, unresolved; 2 = resolved with Ysmera. */
  public static final String FLAG_STAGE = "letter.stage";

  public static final int STAGE_NONE = 0;
  public static final int STAGE_RECEIVED = 1;
  public static final int STAGE_RESOLVED = 2;

  private UnsignedLetterQuest() {}

  public static int stage(Player player) {
    return player == null ? STAGE_NONE : Math.max(STAGE_NONE, player.getQuestFlag(FLAG_STAGE));
  }

  /**
   * Call right after a successful {@code RebirthBehavior.perform(player)}. Grants the letter and
   * returns the reveal message to show the player, or {@code null} if this isn't their first
   * rebirth, or they've somehow already been through this (a save from before this feature
   * shipped, replaying a rebirth flow twice in one session, etc).
   */
  public static String onRebirth(Player player) {
    if (player == null || player.getRebirthCount() != 1 || stage(player) != STAGE_NONE) {
      return null;
    }
    player.setQuestFlag(FLAG_STAGE, STAGE_RECEIVED);
    if (InventoryService.canAdd(player, LETTER_ITEM_KEY)) {
      InventoryService.add(player, LETTER_ITEM_KEY);
    }
    return I18n.resolve("${message.unsigned_letter.received}");
  }

  /**
   * Call from Ysmera's "letter" topic. Resolves the story if the player is actually carrying the
   * letter and hasn't already resolved it - consumes it, advances the stage, and returns the
   * reveal to show. Returns {@code null} when there's nothing to resolve (never received one,
   * already resolved, or the letter itself is gone without the stage having advanced).
   */
  public static boolean resolve(Player player) {
    if (player == null || stage(player) != STAGE_RECEIVED) return false;
    if (!player.getInventory().contains(LETTER_ITEM_KEY)) return false;
    InventoryService.remove(player, -1, LETTER_ITEM_KEY);
    player.setQuestFlag(FLAG_STAGE, STAGE_RESOLVED);
    return true;
  }
}
