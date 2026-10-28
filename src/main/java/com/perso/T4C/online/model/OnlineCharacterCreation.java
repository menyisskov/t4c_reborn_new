package com.perso.T4C.online.model;

import java.util.Objects;
import java.util.UUID;

/** Account-key authenticated creation. Neither request nor result carries a gameplay session. */
public final class OnlineCharacterCreation {
  /** Classes the server accepts for online creation; the server and the native form both read this list. */
  public static final java.util.List<String> CLASSES =
      java.util.List.of("warrior", "archer", "paladin", "cleric", "healer", "mage", "battlemage");
  /** Genders the server accepts. */
  public static final java.util.List<String> GENDERS = java.util.List.of("male", "female");

  private OnlineCharacterCreation() {}

  public record Request(UUID requestId, String name, String characterClass, String gender, boolean evil) {
    public Request {
      Objects.requireNonNull(requestId);
      Objects.requireNonNull(name);
      Objects.requireNonNull(characterClass);
      Objects.requireNonNull(gender);
    }
  }

  public record Result(UUID requestId, OnlineRoster.CharacterSummary character) {
    public Result {
      Objects.requireNonNull(requestId);
      Objects.requireNonNull(character);
    }
  }
}
