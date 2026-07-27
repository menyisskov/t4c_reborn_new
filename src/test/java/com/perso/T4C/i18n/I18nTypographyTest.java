package com.perso.T4C.i18n;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class I18nTypographyTest {

    @Test
    void replacesCurlyApostrophesUnsupportedByBitmapFont() {
        Lang previous = Lang.current();
        try {
            Lang.set(Lang.FR);
            String translated = I18n.t("item.sign.sign_8", "fallback");
            assertFalse(translated.contains("’"));
            assertTrue(translated.contains("L'enseigne"));
        } finally {
            Lang.set(previous);
        }
    }

    @Test
    void resolvesIraltokDialogPlaceholderInFrench() {
        Lang previous = Lang.current();
        try {
            Lang.set(Lang.FR);

            assertEquals(
                    "Moi, le chercheur de *connaissances*, vous salue, mon ami. "
                            + "Je suis aussi un grand scribe arcanique et je peux vous enseigner plusieurs *sorts*.",
                    I18n.npcDialog("Iraltok", "${npc.dialog.iraltok}"));
        } finally {
            Lang.set(previous);
        }
    }
}
