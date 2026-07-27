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
 * Covers the flee-shout field added in binary version 5: it must survive a
 * write/read cycle, and the i18n placeholders written to disk must resolve back
 * to the English text rather than leaking a raw {@code ${...}} into the bubble.
 */
class NpcFleeShoutsTest {

    private static final List<String> SHOUTS = List.of("AHHHHHHHHHHHHHH!", "HELP!");

    private static NpcDef moonrock(List<String> shouts) {
        return new NpcDef("Moonrock", "Moonrock", List.of(), "moonrock",
                null, null, KeywordActionType.NONE, 0, 0, 0,
                List.of(), List.of(), List.of(), shouts);
    }

    @Test
    void fleeShoutsSurviveTheBinaryRoundTrip(@TempDir Path dir) throws Exception {
        File file = dir.resolve("npcs.bin").toFile();
        NpcDefBinaryIO.write(file, List.of(moonrock(SHOUTS)));

        NpcDef read = NpcDefBinaryIO.read(file).get(0);
        assertEquals(SHOUTS, read.getFleeShouts(),
                "flee shouts must come back resolved, not as ${npc.flee_shout...} placeholders");
    }

    @Test
    void npcsWithoutFleeShoutsRoundTripToAnEmptyList(@TempDir Path dir) throws Exception {
        File file = dir.resolve("npcs.bin").toFile();
        NpcDefBinaryIO.write(file, List.of(moonrock(List.of())));

        assertTrue(NpcDefBinaryIO.read(file).get(0).getFleeShouts().isEmpty());
    }

    /** The 13-arg constructor is still used by the editors and migrations. */
    @Test
    void legacyConstructorDefaultsToNoFleeShouts() {
        NpcDef def = new NpcDef("Rolph", "Rolph", List.of(), "rolph",
                null, null, KeywordActionType.SHOP, 0, 0, 0,
                List.of(), List.of(), List.of());
        assertTrue(def.getFleeShouts().isEmpty());
    }
}
