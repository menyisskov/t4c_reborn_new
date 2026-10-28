package com.perso.T4C.session;

/**
 * One per played character, chosen when the game screen is built. Members arrive with the
 * responsibility that moves behind them (see multiplayer/CLIENT_UNIFICATION_DESIGN.md); the
 * character source, world driver, commands, events and capabilities are declared but still empty.
 */
public interface GameSession {
  SessionKind kind();

  CharacterPersistence persistence();
}
