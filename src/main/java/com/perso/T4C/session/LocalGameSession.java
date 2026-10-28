package com.perso.T4C.session;

/** Single-player session: today's code, moved behind the seam without changing behaviour. */
public final class LocalGameSession implements GameSession {
  private final CharacterPersistence persistence = new LocalCharacterPersistence();

  @Override
  public SessionKind kind() {
    return SessionKind.LOCAL;
  }

  @Override
  public CharacterPersistence persistence() {
    return persistence;
  }
}
