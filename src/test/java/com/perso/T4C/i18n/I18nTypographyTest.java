package com.perso.T4C.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class I18nTypographyTest {
  @Test
  void replacesCurlyApostrophesUnsupportedByBitmapFont() {
    String resolved = I18n.resolve("It’s a trap");
    assertFalse(resolved.contains("’"));
    assertTrue(resolved.contains("It's a trap"));
  }

  @Test
  void resolvesIraltokNamePlaceholder() {
    assertEquals("Iraltok", I18n.resolve("${npc.iraltok}"));
  }

  @Test
  void returnsPlainTextUnchanged() {
    assertEquals("not a placeholder", I18n.resolve("not a placeholder"));
  }

  @Test
  void keepsUnknownPlaceholderVisibleInsteadOfSilentlyBlanking() {
    assertEquals("${npc.dialog.does_not_exist}", I18n.resolve("${npc.dialog.does_not_exist}"));
  }
}
