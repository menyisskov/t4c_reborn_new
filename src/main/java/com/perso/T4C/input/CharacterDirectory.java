package com.perso.T4C.input;

import com.perso.T4C.player.GmRank;
import java.util.Map;

/**
 * Looks up characters other than the one issuing a GM command, by name. Today that means the
 * local save slots; on a server it becomes the account/character database, and the GM commands
 * built on it stay the same.
 */
public interface CharacterDirectory {
  /** The character's canonical name, or {@code null} if no character has that name. */
  String findName(String name);

  /** The rank saved on the character, or {@code null} if no character has that name. */
  GmRank storedRank(String name);

  /** Saves a new rank on the character; {@code false} if no character has that name. */
  boolean setStoredRank(String name, GmRank rank);

  /** Every known character with its saved rank, keyed by canonical name. */
  Map<String, GmRank> storedRanks();
}
