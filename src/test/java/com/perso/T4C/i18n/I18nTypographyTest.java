package com.perso.T4C.i18n;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class I18nTypographyTest {

    @Test
    void replacesCurlyApostrophesUnsupportedByBitmapFont() {
        String translated = I18n.key("item.sign.sign_8");
        assertFalse(translated.contains("’"));
        assertTrue(translated.contains("L'enseigne"));
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
