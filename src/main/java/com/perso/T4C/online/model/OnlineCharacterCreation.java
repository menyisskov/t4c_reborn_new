package com.perso.T4C.online.model;

import java.util.Objects;
import java.util.UUID;

/** Account-key authenticated creation. Neither request nor result carries a gameplay session. */
public final class OnlineCharacterCreation {
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
