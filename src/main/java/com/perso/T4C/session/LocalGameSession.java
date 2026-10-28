package com.perso.T4C.session;

/** Single-player session: today's code, moved behind the seam without changing behaviour. */
public final class LocalGameSession implements GameSession {
  private final CharacterPersistence persistence = new LocalCharacterPersistence();
  private final CharacterSource characters = new LocalCharacterSource();
  private final WorldDriver world = new LocalWorldDriver();
  private final LocalPlayerCommands commands = new LocalPlayerCommands();

  @Override
  public SessionKind kind() {
    return SessionKind.LOCAL;
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
  public LocalPlayerCommands commands() {
    return commands;
  }
}
