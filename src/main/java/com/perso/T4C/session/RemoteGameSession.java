package com.perso.T4C.session;

import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.online.OnlineConnection;
import java.util.function.Supplier;

/**
 * Online session, step R1: the character comes from the server's committed state and nothing is
 * ever saved locally. The world driver and the commands are placeholders that say so; they arrive
 * with the later remote steps (snapshot-driven world, intents over the connection).
 */
public final class RemoteGameSession implements GameSession {
  private final RemoteCharacterSource characters;
  private final CharacterPersistence persistence = new RemoteCharacterPersistence();
  private final WorldDriver world = new UnavailableWorldDriver();
  private final PlayerCommands commands = new UnavailableCommands();

  /** Reads the character and position from whatever view the connection currently holds. */
  public RemoteGameSession(Supplier<OnlineConnection.View> views) {
    this.characters = new RemoteCharacterSource(views);
  }

  RemoteGameSession(RemoteCharacterSource characters) {
    this.characters = characters;
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

  private static final class UnavailableWorldDriver implements WorldDriver {
    @Override
    public WorldEntities loadEntities(WorldEntityInputs inputs) {
      throw unavailable("world().loadEntities");
    }

    @Override
    public void tickEntities(EntityTick tick) {
      throw unavailable("world().tickEntities");
    }
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
