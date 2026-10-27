package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.CharacterCreationRules;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.online.model.OnlineCharacterCreation;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class OnlineCreateFormTest {
  private static void typeAll(OnlineCreateForm form, String text) {
    for (char c : text.toCharArray()) form.type(c);
  }

  @Test
  void everyOfferedClassIsARealGameClassAndHasEnglishText() {
    for (String id : OnlineCharacterCreation.CLASSES) {
      CharacterClass match = null;
      for (CharacterClass value : CharacterClass.values()) if (value.id().equals(id)) match = value;
      assertNotNull(match, id);
      assertTrue(I18n.has(match.nameKey()), id);
    }
  }

  @Test
  void nameRulesComeFromTheSharedRule() {
    var form = new OnlineCreateForm();
    assertFalse(form.canSubmit());
    assertNull(form.request());
    assertNotNull(form.nameProblem());
    typeAll(form, "a");
    assertFalse(form.nameValid());
    typeAll(form, "ldric");
    assertTrue(form.nameValid());
    assertEquals("Aldric", form.normalizedName());
    assertTrue(CharacterCreationRules.isValidName(form.normalizedName()));
    assertNull(form.nameProblem());
    assertEquals("Aldric", form.request().name());
  }

  @Test
  void typingRefusesWhatTheRuleCouldNeverAccept() {
    var form = new OnlineCreateForm();
    assertFalse(form.type('7'));
    assertFalse(form.type('\n'));
    typeAll(form, "Abcdefgh");
    assertEquals(CharacterCreationRules.MAX_NAME_LENGTH, form.name().length());
    assertFalse(form.type('i'));
    assertTrue(form.type('\''));
    assertTrue(form.type('s'));
    var long20 = new OnlineCreateForm();
    typeAll(long20, "O'Abcdefghijklmnopqrstuvwx");
    assertEquals(OnlineCreateForm.HARD_MAX_NAME_LENGTH, long20.name().length());
    form.backspace();
    form.backspace();
    assertEquals("Abcdefgh", form.name());
    new OnlineCreateForm().backspace();
  }

  @Test
  void requestIdIsStableUntilAFieldChanges() {
    var form = new OnlineCreateForm();
    typeAll(form, "Brynn");
    var first = form.request();
    assertEquals(first.requestId(), form.request().requestId());
    form.selectClass(form.classes().size() - 1);
    var second = form.request();
    assertNotEquals(first.requestId(), second.requestId());
    assertEquals(OnlineCharacterCreation.CLASSES.getLast(), second.characterClass());
    form.setGender("female");
    form.setEvil(true);
    var third = form.request();
    assertNotEquals(second.requestId(), third.requestId());
    assertEquals("female", third.gender());
    assertTrue(third.evil());
    form.setGender("female"); // unchanged: same id
    assertEquals(third.requestId(), form.request().requestId());
    form.setGender("robot");
    assertEquals("female", form.gender());
    form.selectClass(99);
    form.moveClass(-99);
    assertEquals(0, form.classIndex());
  }

  @Test
  void adapterRefusalsMapToSpecificEnglishMessages() {
    assertEquals(I18n.key("online.create.error.name_taken"), describe(409, "name_taken"));
    assertEquals(I18n.key("online.create.error.roster_full"), describe(409, "roster_full"));
    assertEquals(I18n.key("online.create.error.conflict"), describe(409, "creation_conflict"));
    assertEquals(I18n.key("online.create.error.unsupported_class"), describe(400, "unsupported_class"));
    assertEquals(I18n.key("online.create.error.input"), describe(400, "creation_input"));
    assertEquals(I18n.key("online.lobby.error.rejected_auth"), describe(401, null));
    assertEquals(I18n.key("online.create.error.busy"), describe(503, "account_busy"));
    assertEquals(I18n.key("online.create.error.busy"), describe(429, "account_rate"));
    assertEquals(I18n.message("online.create.error.rejected", 418), describe(418, null));
    assertTrue(OnlineCreateForm.needsFreshRequest(new OnlineAccountClient.Rejected(409, "creation_conflict")));
    assertFalse(OnlineCreateForm.needsFreshRequest(new OnlineAccountClient.Rejected(409, "name_taken")));
  }

  @Test
  void transportFailuresNeverLeakRawText() {
    String unreachable = OnlineCreateForm.describe(new IOException("secret-internal-detail"));
    assertEquals(I18n.key("online.create.error.unreachable"), unreachable);
    assertFalse(unreachable.contains("secret-internal-detail"));
    assertEquals(
        I18n.key("online.create.error.timeout"), OnlineCreateForm.describe(new InterruptedException()));
  }

  @Test
  void everyMessageKeyExistsInEnglish() {
    for (String key :
        new String[] {
          "online.create.title", "online.create.name", "online.create.class", "online.create.gender",
          "online.create.alignment", "online.create.submit", "online.create.back",
          "online.create.working", "online.create.hints", "online.create.error.name_invalid",
          "online.create.error.name_taken", "online.create.error.roster_full",
          "online.create.error.conflict", "online.create.error.unsupported_class",
          "online.create.error.input", "online.create.error.busy", "online.create.error.rejected",
          "online.create.error.unreachable", "online.create.error.timeout", "online.lobby.create"
        }) assertTrue(I18n.has(key), key);
  }

  private static String describe(int status, String code) {
    return OnlineCreateForm.describe(new OnlineAccountClient.Rejected(status, code));
  }
}
