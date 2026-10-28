package com.perso.T4C.session;

/** Single-player session: today's code, moved behind the seam without changing behaviour. */
public final class LocalGameSession implements GameSession {
  private final CharacterPersistence persistence = new LocalCharacterPersistence();
  private final CharacterSource characters = new LocalCharacterSource();

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
}
