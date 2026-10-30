package com.perso.T4C.session;

import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.player.BodyPart;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Remote-only actions not exposed by {@link PlayerCommands} yet. Screen controls route equipment
 * and NPC/loot interactions here by the server identity already present in the committed view.
 * Each returns whether the request could be taken by the online command path.
 */
public interface RemoteIntents {
  boolean pickUp(UUID loot);

  boolean interact(String npc);

  /** Asks the server to recover the player's own corpse; the server decides range and contents. */
  default boolean recover(UUID corpse) {
    return false;
  }

  /** Recovers one selected stack from the player's own corpse; the server decides if it still fits. */
  default boolean recover(UUID corpse, UUID item) { return false; }

  /** Asks the NPC about one authored topic (the server validates it); the quest offer is topic 0. */
  default boolean interact(String npc, int topic) {
    return false;
  }

  boolean equip(UUID instance, BodyPart slot);

  boolean unequip(BodyPart slot);

  boolean shopOffers(String npc);

  boolean shopBuy(String npc, String itemKey, int quantity);

  boolean shopSell(String npc, String itemKey, int quantity);

  boolean cast(String spellKey);

  boolean cast(String spellKey, BaseMonster target);

  /** Asks the server to cast at a region-local tile; the server validates range and line of sight. */
  default boolean castAtPosition(String spellKey, int tileX, int tileY) {
    return false;
  }

  /** Casts a learned single-target heal at a live online player id. */
  default boolean castToPlayer(String spellKey,long actor) { return false; }

  boolean useItem(UUID itemInstance);

  /** Requests a server-validated allocation and delivers its committed reply on the render thread. */
  default boolean allocateStats(
      Map<String, Integer> allocation, Consumer<OnlineConnection.CommandResult> completion) {
    return false;
  }

  /** Cancels pending listing UI replies, for example when the player closes the shop panel. */
  void closeShop();
}
