package com.perso.T4C.npc;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.i18n.I18n;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcRegistrySeedParityTest {
    @Test
    void catalogueContainsTheCoreExamplesAndQuestEnabledSamaritan() throws Exception {
        List<NpcDef> defs = NpcDefBinaryIO.read(new File(Paths.NPCS_BIN));
        assertTrue(defs.size() >= 4);
        NpcDef iraltok = defs.stream()
                .filter(def -> "Iraltok".equals(def.getName())).findFirst().orElseThrow();
        assertEquals("Iraltok", iraltok.getName());
        assertEquals("Moi Iraltok, le chercheur de connaissances, vous salue, mon ami. "
                        + "Je suis aussi un grand scribe arcanique et je peux vous enseigner plusieurs sorts.",
                I18n.resolve(iraltok.getWelcomeText()));
        assertEquals(1, iraltok.getTopics().size());
        NpcDef.DialogTopic iraltokTopic = iraltok.getTopics().get(0);
        assertEquals(List.of("sorts"), iraltokTopic.getKeywords().stream().map(I18n::resolve).toList());
        assertNull(iraltokTopic.getResponse());
        assertEquals(1, iraltokTopic.getActions().size());
        assertEquals(ActionType.OPEN_SPELL_LEARNING, iraltokTopic.getActions().get(0).getType());
        assertEquals(List.of("spell.flaming_arrow"), iraltokTopic.getActions().get(0).getTargets());

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

        NpcDef samaritan = defs.stream()
                .filter(def -> "LighthavenSamaritan".equals(def.getName())).findFirst().orElseThrow();
        assertEquals("Samaritain de Lighthaven", I18n.resolve(samaritan.getDisplayName()));
        assertEquals("PaysanModel1", samaritan.getSpriteBase());
        assertTrue(samaritan.getParts().isEmpty());
        assertEquals(0, samaritan.getPatrolRadiusTiles());
        assertEquals(15, samaritan.getTopics().size());
        NpcDef.DialogTopic work = samaritan.getTopics().stream()
                .filter(topic -> topic.getKeywords().stream().map(I18n::resolve)
                        .anyMatch("travail"::equals))
                .findFirst()
                .orElseThrow();
        assertEquals(List.of("travail", "que faites-vous", "occupation"),
                work.getKeywords().stream().map(I18n::resolve).toList());
        assertTrue(work.getKeywords().stream().map(I18n::resolve)
                .noneMatch(value -> value.contains("${")));
        assertNull(work.getResponse());
        assertEquals(1, work.getActions().size());
        assertEquals(ActionType.GIVE_QUEST, work.getActions().get(0).getType());
        assertEquals(List.of("lighthaven_samaritan_rats"),
                work.getActions().get(0).getTargets());
        assertEquals("Vous commencez avec des objets dans votre sac à dos.",
                I18n.resolve(samaritan.getTopics().get(13).getResponse()));
        assertFalse(I18n.resolve(samaritan.getTopics().get(13).getKeywords().get(0))
                .contains("${"));

        SpawnBinaryIO.Entry spawn = SpawnBinaryIO.read(new File(Paths.NPC_SPAWNS_BIN)).stream()
                .filter(entry -> "LighthavenSamaritan".equals(entry.type))
                .findFirst()
                .orElseThrow();
        assertEquals(2931, spawn.x);
        assertEquals(1072, spawn.y);
        assertEquals(0, spawn.z);
        assertFalse(spawn.stationary);
        assertFalse(spawn.aggressive);
    }
}
