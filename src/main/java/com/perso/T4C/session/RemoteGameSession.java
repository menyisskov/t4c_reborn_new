package com.perso.T4C.session;

import com.perso.T4C.objects.GroundItemManager;
import com.perso.T4C.online.OnlineConnection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * Online session: the character comes from the server's committed state (R1), the world is the
 * server's latest snapshot applied to puppet monsters, NPCs and loot (R2), and the commands are
 * intents sent over the persistent connection (R3); nothing is ever saved locally.
 *
 * <p>The commands cover attack, own movement, pickup and NPC interaction. Every other player
 * action (equipment, item use, spells, shops, corpse recovery, chat, trade) has no {@link
 * PlayerCommands} member yet; the connection can already carry some of them, but the screens that
 * start them are not routed to the session until their own steps.
 */
public final class RemoteGameSession implements GameSession {
  private final RemoteCharacterSource characters;
  private final CharacterPersistence persistence = new RemoteCharacterPersistence();
  private final RemoteWorldDriver world;
  private final RemotePlayerCommands commands;

  /** Reads the character and position from whatever view the connection currently holds. */
  public RemoteGameSession(Supplier<OnlineConnection.View> views) {
    this(views, null);
  }

  /**
   * A session with no connection behind it (headless character and world tests): its commands
   * fail like a link that is down.
   *
   * @param ground the screen's ground-item registry that server loot is drawn into, or null
   */
  public RemoteGameSession(Supplier<OnlineConnection.View> views, GroundItemManager ground) {
    this(new DetachedLink(views), ground);
  }

  /** The online session over a live connection. */
  public RemoteGameSession(OnlineConnection connection, GroundItemManager ground) {
    this(new OnlineConnectionLink(Objects.requireNonNull(connection, "connection")), ground);
  }

  /** The online session over any link (tests use a fake). */
  public RemoteGameSession(RemoteLink link, GroundItemManager ground) {
    this.characters = new RemoteCharacterSource(link::view);
    this.world = new RemoteWorldDriver(link::view, ground);
    this.commands = new RemotePlayerCommands(link, world::creatureId);
    this.world.bindIntents(commands);
  }

  RemoteGameSession(RemoteCharacterSource characters) {
    this.characters = characters;
    DetachedLink link = new DetachedLink(() -> null);
    this.world = new RemoteWorldDriver(link::view, null);
    this.commands = new RemotePlayerCommands(link, world::creatureId);
  }

  @Override
  public SessionKind kind() {
    return SessionKind.REMOTE;
  }

  @Override
  public CharacterSource characters() {
    return characters;
  }

  @Override
  public CharacterPersistence persistence() {
    return persistence;
  }

  @Override
  public WorldDriver world() {
    return world;
  }

  @Override
  public RemotePlayerCommands commands() {
    return commands;
  }

  /** The remote world driver (also exposes the other players the screen draws). */
  public RemoteWorldDriver remoteWorld() {
    return world;
  }

  /** The last mirrored character with its server item identities, or {@code null} before load. */
  public ProgressMirror.Mirrored mirrored() {
    return characters.mirrored();
  }

  /** A link that reads views but cannot send anything: every send fails as a down link does. */
  private static final class DetachedLink implements RemoteLink {
    private final Supplier<OnlineConnection.View> views;

    DetachedLink(Supplier<OnlineConnection.View> views) {
      this.views = views;
    }

    @Override
    public OnlineConnection.View view() {
      return views.get();
    }

    private static IllegalStateException down() {
      return new IllegalStateException("Persistent session is not connected");
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> attack(long creature) {
      throw down();
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> pickUp(UUID loot) {
      throw down();
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> interact(String npc) {
      throw down();
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> shopOffers(String npc) {
      throw down();
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> shopBuy(String npc, String itemKey, int quantity) {
      throw down();
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> shopSell(String npc, String itemKey, int quantity) {
      throw down();
    }

    @Override
    public void direction(int dx, int dy, long frame) {}

    @Override
    public long acknowledgedFrame(long sequence) {
      return -1;
    }
  }
}
