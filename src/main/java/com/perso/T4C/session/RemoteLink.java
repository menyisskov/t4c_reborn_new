package com.perso.T4C.session;

import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.player.BodyPart;
import java.util.UUID;
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

  CompletionStage<OnlineConnection.CommandResult> equip(UUID instance, BodyPart slot);

  CompletionStage<OnlineConnection.CommandResult> unequip(BodyPart slot);

  CompletionStage<OnlineConnection.CommandResult> shopOffers(String npc);

  CompletionStage<OnlineConnection.CommandResult> shopBuy(String npc, String itemKey, int quantity);

  CompletionStage<OnlineConnection.CommandResult> shopSell(String npc, String itemKey, int quantity);

  CompletionStage<OnlineConnection.CommandResult> cast(String spellKey);

  CompletionStage<OnlineConnection.CommandResult> cast(String spellKey, long creature);

  CompletionStage<OnlineConnection.CommandResult> useItem(UUID itemInstance);

  /** Holds a direction (each component -1..1); {@code frame} names the predicted client input. */
  void direction(int dx, int dy, long frame);

  /** The newest client frame the server had when it applied input {@code sequence}; -1 if unknown. */
  long acknowledgedFrame(long sequence);
}
