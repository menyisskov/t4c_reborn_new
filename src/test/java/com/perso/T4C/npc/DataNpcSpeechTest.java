package com.perso.T4C.npc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataNpcSpeechTest {

    @Test
    void recognizesMoonrockHealingCommandsInEnglishAndFrench() {
        assertTrue(DataNpc.isHealCommand("heal"));
        assertTrue(DataNpc.isHealCommand("soin"));
        assertTrue(DataNpc.isHealCommand("Pouvez-vous me soigner ?"));
        assertTrue(DataNpc.isHealCommand("Je voudrais guérir."));
    }

    @Test
    void matchesOnlyCompleteHealingWords() {
        assertFalse(DataNpc.isHealCommand("healer"));
        assertFalse(DataNpc.isHealCommand("soignant"));
    }
}
