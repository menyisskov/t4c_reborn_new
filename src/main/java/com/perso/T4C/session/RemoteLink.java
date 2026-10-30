package com.perso.T4C.session;

import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.player.BodyPart;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * What the online commands need from the connection, and nothing more: the latest view, the
 * identity-only intents and the held direction with its prediction frame. {@link
 * OnlineConnectionLink} is the real one; tests supply a fake. Every intent is single-flight and is
 * never replayed (see {@link OnlineConnection}); one that cannot be sent fails with {@link
 * IllegalStateException}.
 */
public interface RemoteLink {
  /** The newest view, or {@code null} before the first snapshot. */
  OnlineConnection.View view();

  CompletionStage<OnlineConnection.CommandResult> attack(long creature);

  CompletionStage<OnlineConnection.CommandResult> pickUp(UUID loot);

  CompletionStage<OnlineConnection.CommandResult> interact(String npc);

  /** Recovers the character's own corpse by server id; range, ownership and contents are the server's. */
  default CompletionStage<OnlineConnection.CommandResult> recover(UUID corpse) {
    throw new IllegalStateException("Corpse recovery is not available on this link");
  }

  /** As {@link #interact(String)}, asking the NPC about one authored topic (0-99). */
  default CompletionStage<OnlineConnection.CommandResult> interact(String npc, int topic) {
    throw new IllegalStateException("Topics are not available on this link");
  }

  CompletionStage<OnlineConnection.CommandResult> equip(UUID instance, BodyPart slot);

  CompletionStage<OnlineConnection.CommandResult> unequip(BodyPart slot);

  CompletionStage<OnlineConnection.CommandResult> shopOffers(String npc);

  CompletionStage<OnlineConnection.CommandResult> shopBuy(String npc, String itemKey, int quantity);

  CompletionStage<OnlineConnection.CommandResult> shopSell(String npc, String itemKey, int quantity);

  CompletionStage<OnlineConnection.CommandResult> cast(String spellKey);

  CompletionStage<OnlineConnection.CommandResult> cast(String spellKey, long creature);

  default CompletionStage<OnlineConnection.CommandResult> castAtPosition(
      String spellKey, int tileX, int tileY) {
    throw new IllegalStateException("Position-target spells are not available on this link");
  }

  CompletionStage<OnlineConnection.CommandResult> castToPlayer(String spellKey,long actor);

  CompletionStage<OnlineConnection.CommandResult> useItem(UUID itemInstance);

  default CompletionStage<OnlineConnection.CommandResult> allocateStats(Map<String, Integer> allocation) {
    throw new IllegalStateException("Stat allocation is not available on this link");
  }

  /** Holds a direction (each component -1..1); {@code frame} names the predicted client input. */
  void direction(int dx, int dy, long frame);

  /** The newest client frame the server had when it applied input {@code sequence}; -1 if unknown. */
  long acknowledgedFrame(long sequence);
}
