package com.perso.T4C.npc;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.i18n.I18n;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NpcRegistrySeedParityTest {
    @Test
    void catalogueContainsOnlyTheIraltokExample() throws Exception {
        List<NpcDef> defs = NpcDefBinaryIO.read(new File(Paths.NPCS_BIN));
        assertEquals(3, defs.size());
        NpcDef iraltok = defs.get(0);
        assertEquals("Iraltok", iraltok.getName());
        assertEquals("Moi Iraltok, le chercheur de connaissances, vous salue, mon ami. "
                        + "Je suis aussi un grand scribe arcanique et je peux vous enseigner plusieurs sorts.",
                I18n.resolve(iraltok.getWelcomeText()));
        assertEquals(1, iraltok.getTopics().size());
        NpcDef.DialogTopic topic = iraltok.getTopics().get(0);
        assertEquals(List.of("sorts"), topic.getKeywords().stream().map(I18n::resolve).toList());
        assertNull(topic.getResponse());
        assertEquals(1, topic.getActions().size());
        assertEquals(ActionType.OPEN_SPELL_LEARNING, topic.getActions().get(0).getType());
        assertEquals(List.of("spell.flaming_arrow"), topic.getActions().get(0).getTargets());

        NpcDef moonrock = defs.stream().filter(def -> "Moonrock".equals(def.getName())).findFirst().orElseThrow();
        assertEquals("Je suis Moonrock, prêtresse de ce temple. Si vos blessures vous accablent, je peux invoquer la grâce des dieux pour vous soigner.",
                I18n.resolve(moonrock.getWelcomeText()));
        assertEquals(List.of("soigner", "heal"),
                moonrock.getTopics().get(0).getKeywords().stream().map(I18n::resolve).toList());
        assertEquals(ActionType.HEAL, moonrock.getTopics().get(0).getActions().get(0).getType());

        NpcDef jagarKar = defs.stream().filter(def -> "JagarKar".equals(def.getName())).findFirst().orElseThrow();
        assertEquals(List.of("attack", "archery", "dodge"),
                jagarKar.getTopics().get(0).getActions().get(0).getTargets());
        assertEquals(ActionType.OPEN_SKILL_LEARNING,
                jagarKar.getTopics().get(0).getActions().get(0).getType());
    }
}
