package com.perso.T4C.session;

/**
 * One per played character, chosen when the game screen is built. Members arrive with the
 * responsibility that moves behind them (see multiplayer/CLIENT_UNIFICATION_DESIGN.md); the
 * events and capabilities are declared but still empty and the commands carry one intent so far.
 */
public interface GameSession {
  SessionKind kind();

  CharacterSource characters();

  CharacterPersistence persistence();

  WorldDriver world();

  PlayerCommands commands();
}
