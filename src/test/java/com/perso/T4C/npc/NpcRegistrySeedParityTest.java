package com.perso.T4C.npc;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.i18n.I18n;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcRegistrySeedParityTest {
    @Test
    void catalogueContainsTheOriginalSourceMigration() throws Exception {
        List<NpcDef> defs = NpcDefBinaryIO.read(new File(Paths.NPCS_BIN));
        List<SpawnBinaryIO.Entry> spawns = SpawnBinaryIO.read(new File(Paths.NPC_SPAWNS_BIN));
        assertTrue(defs.size() >= 400, "the original DLL projects contain hundreds of interactive NPCs");
        assertEquals(defs.size(), spawns.size());
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
    }
}
