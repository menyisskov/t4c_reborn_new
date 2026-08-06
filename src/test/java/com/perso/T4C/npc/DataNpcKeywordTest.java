package com.perso.T4C.npc;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataNpcKeywordTest {
    private final NpcDef.DialogTopic topic =
            new NpcDef.DialogTopic(List.of("sorts"), null, List.of());

    @Test
    void keywordMatchingIgnoresCaseAndPunctuation() {
        assertTrue(DataNpc.matches(topic, "SORTS !"));
        assertTrue(DataNpc.matches(topic, "Montrez-moi vos sorts, Iraltok."));
    }

    @Test
    void keywordMatchingUsesWholeWords() {
        assertFalse(DataNpc.matches(topic, "ressorts"));
    }

    @Test
    void namingOneCmdAndWordSaysTheWholeSentence() {
        String script = """
                CmdAND(INTL(1, "READY"), INTL(2, "REBORN"))
                    SetYesNo(REBIRTH)
                Command2(INTL(2, "REBORN"), INTL(3, "REBIRTH"))
                    INTL(4, "Once you are reborn...")
                """;
        // Typed on its own, either word must still reach the CmdAND section.
        assertEquals("READY REBORN", DataNpc.sentenceForKeyword(script, "ready"));
        assertEquals("READY REBORN", DataNpc.sentenceForKeyword(script, "reborn"));
        // An already complete sentence is left untouched.
        assertEquals("ready reborn", DataNpc.sentenceForKeyword(script, "ready reborn"));
        // Words the script does not know are passed through verbatim.
        assertEquals("bonjour", DataNpc.sentenceForKeyword(script, "bonjour"));
    }

    @Test
    void keywordMatchingIgnoresAccents() {
        NpcDef.DialogTopic accented =
                new NpcDef.DialogTopic(List.of("guérison"), null, List.of());
        assertTrue(DataNpc.matches(accented, "GUERISON."));
    }
}
