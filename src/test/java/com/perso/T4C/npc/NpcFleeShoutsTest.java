package com.perso.T4C.npc;

import com.perso.T4C.helper.NpcDefBinaryIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the flee-shout field added in binary version 5. Shouts must survive a
 * write/read cycle as {@code ${npc.flee_shout...}} placeholders: the binary never
 * stores prose, and NPCManager resolves the line only when it is spoken.
 */
class NpcFleeShoutsTest {

    private static final List<String> SHOUTS =
            List.of("${npc.flee_shout.moonrock.0}", "${npc.flee_shout.moonrock.1}");

    private static NpcDef moonrock(List<String> shouts) {
        return new NpcDef("Moonrock", "Moonrock", List.of(), "moonrock",
                0, shouts, "Hello", List.of());
    }

    @Test
    void fleeShoutsSurviveTheBinaryRoundTrip(@TempDir Path dir) throws Exception {
        File file = dir.resolve("npcs.bin").toFile();
        NpcDefBinaryIO.write(file, List.of(moonrock(SHOUTS)));

        NpcDef read = NpcDefBinaryIO.read(file).get(0);
        assertEquals(SHOUTS, read.getFleeShouts(),
                "flee shouts must stay ${npc.flee_shout...} placeholders through the binary");
    }

    @Test
    void npcsWithoutFleeShoutsRoundTripToAnEmptyList(@TempDir Path dir) throws Exception {
        File file = dir.resolve("npcs.bin").toFile();
        NpcDefBinaryIO.write(file, List.of(moonrock(List.of())));

        assertTrue(NpcDefBinaryIO.read(file).get(0).getFleeShouts().isEmpty());
    }

    @Test
    void emptyTopicsRepresentAWelcomeOnlyConversation() {
        NpcDef def = new NpcDef("Rolph", "Rolph", List.of(), "rolph",
                0, List.of(), "Hello", List.of());
        assertTrue(def.getFleeShouts().isEmpty());
        assertTrue(def.getTopics().isEmpty());
    }
}
