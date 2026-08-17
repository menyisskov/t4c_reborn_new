package com.perso.T4C.npc;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.i18n.I18n;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcRegistrySeedParityTest {
    @Test
    void catalogueContainsTheOriginalSourceMigration() throws Exception {
        List<NpcDef> defs = NpcDefBinaryIO.read(new File(Paths.NPCS_BIN));
        List<SpawnBinaryIO.Entry> spawns = SpawnBinaryIO.read(new File(Paths.NPC_SPAWNS_BIN));
        assertEquals(440, defs.size(), "every unique NPC from the original DLL projects must be persisted");
        assertEquals(defs.size(), spawns.size());
        assertEquals(defs.size(), defs.stream().map(NpcDef::getName).collect(Collectors.toSet()).size(),
                "NPC names must stay unique after merging the original projects");
        Set<String> names = defs.stream().map(NpcDef::getName).collect(Collectors.toSet());
        assertTrue(spawns.stream().allMatch(spawn -> names.contains(spawn.type)),
                "every persisted NPC spawn must reference an imported definition");
        assertTrue(defs.stream().allMatch(def -> def.getSourceScript() != null
                        && def.getSourceScript().contains("InitTalk")
                        && def.getSourceScript().contains("EndTalk")),
                "the bin must contain each complete original OnTalk script, not a shortened Java substitute");
        assertTrue(spawns.stream().anyMatch(spawn -> !spawn.stationary),
                "ordinary original NPCs must retain their default wandering behavior");
        assertTrue(!spawns.stream().filter(spawn -> "NissusHaloseeker".equals(spawn.type))
                .findFirst().orElseThrow().stationary);
        assertTrue(spawns.stream().filter(spawn -> "KingTheodore".equals(spawn.type))
                .findFirst().orElseThrow().stationary);

        NpcDef iraltok = defs.stream().filter(def -> "Iraltok".equals(def.getName()))
                .findFirst().orElseThrow();
        assertEquals("Iraltok", I18n.resolve(iraltok.getDisplayName()));
        assertEquals("Mage", iraltok.getSourceTemplate());
        assertNotNull(iraltok.getSourceScript());
        assertTrue(iraltok.getSourceScript().contains("::") || iraltok.getSourceScript().contains("Command"));
        assertTrue(iraltok.getTopics().size() >= 10);

        NpcDef samaritan = defs.stream().filter(def -> "LighthavenSamaritan".equals(def.getName()))
                .findFirst().orElseThrow();
        assertEquals("Lighthaven Samaritan", I18n.resolve(samaritan.getDisplayName()));
        assertEquals("Samaritan", samaritan.getSourceTemplate());
        assertTrue(samaritan.getSourceScript().contains("GiveFlag"));

        NpcDef nissus = defs.stream().filter(def -> "NissusHaloseeker".equals(def.getName()))
                .findFirst().orElseThrow();
        assertEquals(List.of(
                        "BODY:PupWhiteRobe",
                        "LEGS:PupLegsClothSet1",
                        "WEAPON:PupMorningStar"),
                nissus.getParts().stream()
                        .map(part -> part.getBodyPart() + ":" + part.getSpriteBase()).toList());

        NpcDef amandra = defs.stream().filter(def -> "Amandra".equals(def.getName()))
                .findFirst().orElseThrow();
        assertEquals(List.of(
                        "BODY:WoPlateBody", "BOOT:WoPlateBoots", "LEGS:WoPlateLegs",
                        "HEAD:WoPlateHelm", "WEAPON:PupBattleSword", "SHIELD:PupRomanShield",
                        "RIGHT_HAND:WoPlateGloveR", "LEFT_HAND:WoPlateGloveL"),
                amandra.getParts().stream()
                        .map(part -> part.getBodyPart() + ":" + part.getSpriteBase()).toList());

        NpcDef remort = defs.stream().filter(def -> "RemortNPC1".equals(def.getName()))
                .findFirst().orElseThrow();
        assertEquals("TELEPORT(2939,1066,0)\nGiveFlag(__FLAG_REMORT_PROCESS,0)",
                remort.getSourceEvents().get("@spell.spell.remort_teleport_spell"),
                "spell side effects specific to an NPC flow belong in npcs.bin, not Java conditionals");

        NpcDef clerk = defs.stream().filter(def -> "ColosseumClerk".equals(def.getName()))
                .findFirst().orElseThrow();
        assertEquals("true", clerk.getSourceEvents().get(DataNpc.TALK_THROUGH_WALLS_EVENT),
                "the clerk's across-wall interaction rule belongs in npcs.bin");
        assertEquals("true", clerk.getSourceEvents().get(DataNpc.SCRIPT_WELCOME_EVENT),
                "the clerk's state-dependent greeting rule belongs in npcs.bin");
    }
}
