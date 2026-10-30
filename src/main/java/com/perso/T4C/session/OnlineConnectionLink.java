package com.perso.T4C.session;

import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.player.BodyPart;
import java.util.Objects;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/** The real {@link RemoteLink}: forwards to the persistent connection, adding nothing. */
public final class OnlineConnectionLink implements RemoteLink {
  private final OnlineConnection connection;

  public OnlineConnectionLink(OnlineConnection connection) {
    this.connection = Objects.requireNonNull(connection, "connection");
  }

  @Override
  public OnlineConnection.View view() {
    return connection.view();
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> attack(long creature) {
    return connection.attack(creature);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> pickUp(UUID loot) {
    return connection.pickUp(loot);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> interact(String npc) {
    return connection.interact(npc);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> interact(String npc, int topic) {
    return connection.interact(npc, java.util.OptionalInt.of(topic));
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> equip(UUID instance, BodyPart slot) {
    return connection.equip(instance, slot);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> unequip(BodyPart slot) {
    return connection.unequip(slot);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> shopOffers(String npc) {
    return connection.shopOffers(npc);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> shopBuy(String npc, String itemKey, int quantity) {
    return connection.shopBuy(npc, itemKey, quantity);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> shopSell(String npc, String itemKey, int quantity) {
    return connection.shopSell(npc, itemKey, quantity);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> cast(String spellKey) {
    return connection.cast(spellKey);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> cast(String spellKey, long creature) {
    return connection.cast(spellKey, creature);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> castAtPosition(
      String spellKey, int tileX, int tileY) {
    return connection.castAtPosition(spellKey, tileX, tileY);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> castToPlayer(String spellKey,long actor) {
    return connection.castToPlayer(spellKey,actor);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> recover(UUID corpse) {
    return connection.recover(corpse);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> recover(UUID corpse, UUID item) {
    return item == null ? connection.recover(corpse) : connection.recover(corpse, item);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> useItem(UUID itemInstance) {
    return connection.useItem(itemInstance);
  }

  @Override
  public CompletionStage<OnlineConnection.CommandResult> allocateStats(Map<String, Integer> allocation) {
    return connection.allocateStats(allocation);
  }

  @Override
  public void direction(int dx, int dy, long frame) {
    connection.direction(dx, dy, frame);
  }

  @Override
  public long acknowledgedFrame(long sequence) {
    return connection.acknowledgedFrame(sequence);
  }
}
