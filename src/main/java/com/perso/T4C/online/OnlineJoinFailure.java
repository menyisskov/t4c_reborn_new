package com.perso.T4C.online;

import com.perso.T4C.i18n.I18n;

/**
 * Player-facing text for a refused or failed attempt to enter the game from the lobby. Pure and
 * render-free. The server deliberately answers an unknown key, another account's character, a
 * character that does not exist and a character already in use with the same refusal, so the text for
 * that answer lists the possibilities rather than naming one. Never includes the account key, a
 * character id or raw exception text.
 */
public final class OnlineJoinFailure {
  private OnlineJoinFailure() {}

  /** The client and the server speak different protocol versions. */
  public static final class Incompatible extends IllegalStateException {
    public Incompatible(String message) {
      super(message);
    }
  }

  /** Message for a server answer to the join request or to the map read before it. */
  public static String forRefusal(int status, String reason) {
    if (status == 409) return I18n.key("online.lobby.error.join_refused");
    if (status == 426) return I18n.key("online.lobby.error.incompatible");
    if (status == 401 || status == 403) return I18n.key("online.lobby.error.rejected_auth");
    if (status == 429 || status == 503) return I18n.key("online.lobby.error.rejected_busy");
    return I18n.message("online.lobby.error.rejected", status);
  }

  /** Message for a failure that has no server answer: an incompatible version or a transport problem. */
  public static String forFailure(Throwable failure) {
    if (failure instanceof Incompatible) return I18n.key("online.lobby.error.incompatible");
    return I18n.key("online.lobby.error.unreachable");
  }
}
