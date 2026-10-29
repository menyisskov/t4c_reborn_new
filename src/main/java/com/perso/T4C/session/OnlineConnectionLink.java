package com.perso.T4C.session;

import com.perso.T4C.online.OnlineConnection;
import java.util.Objects;
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
  public void direction(int dx, int dy, long frame) {
    connection.direction(dx, dy, frame);
  }

  @Override
  public long acknowledgedFrame(long sequence) {
    return connection.acknowledgedFrame(sequence);
  }
}
