package com.perso.T4C.session;

import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.online.OnlineConnection;
import java.util.function.Supplier;

/**
 * Online session: the character comes from the server's committed state (R1) and the world is the
 * server's latest snapshot applied to puppet monsters, NPCs and loot (R2); nothing is ever saved
 * locally. The commands are placeholders that say so; they arrive with step R3 (intents over the
 * connection).
 */
public final class RemoteGameSession implements GameSession {
  private final RemoteCharacterSource characters;
  private final CharacterPersistence persistence = new RemoteCharacterPersistence();
  private final WorldDriver world;
  private final PlayerCommands commands = new UnavailableCommands();

  /** Reads the character and position from whatever view the connection currently holds. */
  public RemoteGameSession(Supplier<OnlineConnection.View> views) {
    this(views, null);
  }

  /**
   * @param ground the screen's ground-item registry that server loot is drawn into, or null
   */
  public RemoteGameSession(
      Supplier<OnlineConnection.View> views, com.perso.T4C.objects.GroundItemManager ground) {
    this.characters = new RemoteCharacterSource(views);
    this.world = new RemoteWorldDriver(views, ground);
  }

  RemoteGameSession(RemoteCharacterSource characters) {
    this.characters = characters;
    this.world = new RemoteWorldDriver(() -> null, null);
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
  public PlayerCommands commands() {
    return commands;
  }

  /** The last mirrored character with its server item identities, or {@code null} before load. */
  public ProgressMirror.Mirrored mirrored() {
    return characters.mirrored();
  }

  private static UnsupportedOperationException unavailable(String member) {
    return new UnsupportedOperationException("not available online yet: " + member);
  }

  private static final class UnavailableCommands implements PlayerCommands {
    @Override
    public void attack(BaseMonster target) {
      throw unavailable("commands().attack");
    }

    @Override
    public void move(float dx, float dy, float delta) {
      throw unavailable("commands().move");
    }
  }
}
