package com.perso.T4C.session;

import com.perso.T4C.player.BodyPart;
import java.util.UUID;

/**
 * Remote-only actions not exposed by {@link PlayerCommands} yet. Screen controls route equipment
 * and NPC/loot interactions here by the server identity already present in the committed view.
 * Each returns whether the request could be taken by the online command path.
 */
public interface RemoteIntents {
  boolean pickUp(UUID loot);

  boolean interact(String npc);

  boolean equip(UUID instance, BodyPart slot);

  boolean unequip(BodyPart slot);

  boolean shopOffers(String npc);

  boolean shopBuy(String npc, String itemKey, int quantity);

  boolean shopSell(String npc, String itemKey, int quantity);

  /** Cancels pending listing UI replies, for example when the player closes the shop panel. */
  void closeShop();
}
