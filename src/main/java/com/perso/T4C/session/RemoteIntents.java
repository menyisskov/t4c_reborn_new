package com.perso.T4C.session;

import java.util.UUID;

/**
 * The click intents that are not {@link PlayerCommands} members yet: they reach the session from
 * the ground-item and NPC click hooks by server identity. Each returns whether the intent was
 * taken (and so the click consumed).
 */
public interface RemoteIntents {
  boolean pickUp(UUID loot);

  boolean interact(String npc);

  boolean shopOffers(String npc);

  boolean shopBuy(String npc, String itemKey, int quantity);

  boolean shopSell(String npc, String itemKey, int quantity);

  /** Cancels pending listing UI replies, for example when the player closes the shop panel. */
  void closeShop();
}
