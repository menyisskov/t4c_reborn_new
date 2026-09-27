package com.perso.T4C.player;

import java.util.Locale;

/**
 * A character's game-master rank. Stored on the character (not the client) so the same model
 * works once characters live on a server: a {@link #GM} can use the GM commands, a {@link
 * #SUPER_GM} can also grant or take away other characters' ranks.
 */
public enum GmRank {
  PLAYER,
  GM,
  SUPER_GM;

  public boolean atLeast(GmRank required) {
    return compareTo(required) >= 0;
  }

  public String label() {
    return switch (this) {
      case PLAYER -> "player";
      case GM -> "GM";
      case SUPER_GM -> "Super GM";
    };
  }

  /** Parses a command/save value; {@code null} if it names no rank. */
  public static GmRank parse(String value) {
    if (value == null) return null;
    return switch (value.trim().toLowerCase(Locale.ROOT).replace("-", "_")) {
      case "player", "none", "normal_player" -> PLAYER;
      case "gm" -> GM;
      case "super", "supergm", "super_gm", "sgm" -> SUPER_GM;
      default -> null;
    };
  }

  /** Save-file values that are missing or unknown load as a plain player. */
  public static GmRank fromSave(String value) {
    GmRank rank = parse(value);
    return rank == null ? PLAYER : rank;
  }
}
