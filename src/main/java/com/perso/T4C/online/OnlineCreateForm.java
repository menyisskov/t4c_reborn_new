package com.perso.T4C.online;

import com.perso.T4C.helper.CharacterCreationRules;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.online.model.OnlineCharacterCreation;
import java.util.List;
import java.util.UUID;

/**
 * Pure, render-free state of the online character-creation form. Name rules come from {@link
 * CharacterCreationRules}; class and gender choices come from {@link OnlineCharacterCreation}, the
 * same lists the server enforces. The request id stays stable while the fields are unchanged so a
 * retry after an uncertain result is idempotent, and changes as soon as any field changes.
 */
public final class OnlineCreateForm {
  /** Longest name the shared rule can accept (punctuated classic names). */
  public static final int HARD_MAX_NAME_LENGTH = 20;

  private String name = "";
  private int classIndex;
  private String gender = OnlineCharacterCreation.GENDERS.getFirst();
  private boolean evil;
  private UUID requestId = UUID.randomUUID();

  public List<String> classes() {
    return OnlineCharacterCreation.CLASSES;
  }

  public String name() {
    return name;
  }

  public int classIndex() {
    return classIndex;
  }

  public String characterClass() {
    return OnlineCharacterCreation.CLASSES.get(classIndex);
  }

  public String gender() {
    return gender;
  }

  public boolean evil() {
    return evil;
  }

  /** Appends a typed character if the name rules could still accept it; returns whether it was taken. */
  public boolean type(char c) {
    if (!(Character.isLetter(c) || c == ' ' || c == '-' || c == '\'')) return false;
    String next = name + c;
    boolean punctuated = next.indexOf('\'') >= 0;
    int limit = punctuated ? HARD_MAX_NAME_LENGTH : CharacterCreationRules.MAX_NAME_LENGTH;
    if (next.length() > limit) return false;
    name = next;
    changed();
    return true;
  }

  public void backspace() {
    if (name.isEmpty()) return;
    name = name.substring(0, name.length() - 1);
    changed();
  }

  public void selectClass(int index) {
    if (index < 0 || index >= OnlineCharacterCreation.CLASSES.size() || index == classIndex) return;
    classIndex = index;
    changed();
  }

  public void moveClass(int delta) {
    int size = OnlineCharacterCreation.CLASSES.size();
    selectClass(Math.max(0, Math.min(size - 1, classIndex + delta)));
  }

  public void setGender(String value) {
    if (!OnlineCharacterCreation.GENDERS.contains(value) || value.equals(gender)) return;
    gender = value;
    changed();
  }

  public void setEvil(boolean value) {
    if (value == evil) return;
    evil = value;
    changed();
  }

  /** The name as the server will see it (trimmed, collapsed, first letter capitalised). */
  public String normalizedName() {
    return CharacterCreationRules.normalizeName(name);
  }

  public boolean nameValid() {
    return CharacterCreationRules.isValidName(normalizedName());
  }

  /** Plain-English message for an unacceptable name, or null when the name is acceptable. */
  public String nameProblem() {
    if (nameValid()) return null;
    return I18n.message(
        "online.create.error.name_invalid",
        CharacterCreationRules.MIN_NAME_LENGTH,
        CharacterCreationRules.MAX_NAME_LENGTH);
  }

  public boolean canSubmit() {
    return nameValid();
  }

  /** Request for the current fields, or null when the name is not acceptable. */
  public OnlineCharacterCreation.Request request() {
    if (!canSubmit()) return null;
    return new OnlineCharacterCreation.Request(requestId, normalizedName(), characterClass(), gender, evil);
  }

  /** Forces a new request id; used when the server reports the old id conflicts with other fields. */
  public void freshRequest() {
    requestId = UUID.randomUUID();
  }

  private void changed() {
    freshRequest();
  }

  /** Plain-English text for a creation failure. Never includes the account key or raw exception text. */
  public static String describe(Throwable failure) {
    if (failure instanceof OnlineAccountClient.Rejected rejected) {
      String code = rejected.code() == null ? "" : rejected.code();
      int status = rejected.statusCode();
      switch (code) {
        case "name_taken":
          return I18n.key("online.create.error.name_taken");
        case "roster_full":
          return I18n.key("online.create.error.roster_full");
        case "creation_conflict":
          return I18n.key("online.create.error.conflict");
        case "unsupported_class":
          return I18n.key("online.create.error.unsupported_class");
        case "creation_input":
          return I18n.key("online.create.error.input");
        default:
          break;
      }
      if (status == 401 || status == 403) return I18n.key("online.lobby.error.rejected_auth");
      if (status == 429 || status == 503) return I18n.key("online.create.error.busy");
      return I18n.message("online.create.error.rejected", status);
    }
    if (failure instanceof InterruptedException) return I18n.key("online.create.error.timeout");
    return I18n.key("online.create.error.unreachable");
  }

  /** True when the old request id can never succeed with these fields, so the next try needs a new one. */
  public static boolean needsFreshRequest(Throwable failure) {
    return failure instanceof OnlineAccountClient.Rejected rejected
        && "creation_conflict".equals(rejected.code());
  }
}
