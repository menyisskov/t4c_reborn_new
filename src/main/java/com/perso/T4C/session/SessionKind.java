package com.perso.T4C.session;

/** Where a played character lives. Chosen when the game screen is built, never switched. */
public enum SessionKind {
  /** Single-player: this process owns the character and its save file. */
  LOCAL,
  /** Online: a server owns the character; this process only shows it. */
  REMOTE
}
