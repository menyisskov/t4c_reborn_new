package com.perso.T4C.npc;

import org.junit.jupiter.api.Test;

import java.util.List;

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
    void keywordMatchingIgnoresAccents() {
        NpcDef.DialogTopic accented =
                new NpcDef.DialogTopic(List.of("guérison"), null, List.of());
        assertTrue(DataNpc.matches(accented, "GUERISON."));
    }
}
