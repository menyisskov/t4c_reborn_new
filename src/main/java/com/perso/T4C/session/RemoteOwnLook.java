package com.perso.T4C.session;

import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps the own {@link Player} of an online screen on the server's committed worn items (T4C-0337). The player is built once
 * at join; before this, an equip or unequip changed the committed character (and what other windows saw) while the own
 * screen kept the join-time gear: the character sheet's strength and armor class, the drawn own model and every bonus read
 * from the worn items stayed those of the first frame (found live: the weapon's strength bonus and the body armour's armor
 * class never changed on the screen that had just taken them off).
 *
 * <p>Only the worn items are mirrored: they are the one input of the equipment bonuses and of the drawn look that the screen
 * does not otherwise refresh. Vitals have their own synchronisation; nothing here touches the position or the server.
 */
public final class RemoteOwnLook {
  private RemoteOwnLook() {}

  /** The worn items of the committed progress as the player model holds them (two-slot items fill both slots). */
  public static Map<BodyPart, String> worn(CharacterProgress progress) {
    Map<UUID, String> keys = new HashMap<>();
    for (CharacterProgress.Item item : progress.inventory()) keys.put(item.id(), item.key());
    Map<String, String> bySlot = new HashMap<>();
    for (Map.Entry<String, UUID> entry : progress.equipment().entrySet()) {
      String key = keys.get(entry.getValue());
      if (key != null) bySlot.put(entry.getKey(), key);
    }
    return RemotePlayerLook.equipped(bySlot);
  }

  /**
   * Puts the committed worn items on the player and rebuilds its drawn look when they differ.
   *
   * @return true when the worn items changed
   */
  public static boolean refresh(Player player, CharacterProgress progress) {
    if (player == null || progress == null) return false;
    Map<BodyPart, String> worn = worn(progress);
    if (worn.equals(player.getEquippedItems())) return false;
    player.setEquippedItems(worn);
    PlayerAppearanceDefaults.applyDefaults(player);
    return true;
  }
}
