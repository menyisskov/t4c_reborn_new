package com.perso.T4C.npc;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseNpcDialogFormattingTest {
    @Test
    void removesAllQuotationMarksFromDisplayedNpcDialogue() {
        assertEquals("Parlez de magie, oracle et portail.",
                BaseNPC.removeNpcDialogueQuotes(
                        "Parlez de \"magie\", “oracle” et «portail»."));
    }

    @Test
    void removesQuotesOnlyAroundInteractiveKeywords() {
        assertEquals("Parlez-moi de Dark One, mais gardez \"cette citation\".",
                BaseNPC.stripDialogKeywordQuotes(
                        "Parlez-moi de \"Dark One\", mais gardez \"cette citation\".",
                        List.of("dark one")));
    }

    @Test
    void supportsFrenchAndCurlyQuotationMarks() {
        assertEquals("Cherchez magie puis oracle.",
                BaseNPC.stripDialogKeywordQuotes("Cherchez «magie» puis “oracle”.",
                        List.of("magie", "oracle")));
    }
}
