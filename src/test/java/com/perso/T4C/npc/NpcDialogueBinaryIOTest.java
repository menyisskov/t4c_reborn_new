package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.helper.NpcDefBinaryIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NpcDialogueBinaryIOTest {
    @Test
    void simpleDialogueRoundTrips(@TempDir Path dir) throws Exception {
        NpcDef source = new NpcDef("Test", "Test", List.of(), "sprite", 2, List.of(),
                "${npc.welcome.test}",
                List.of(new NpcDef.DialogTopic(
                        List.of("${npc.topic_keyword.test.0.0}"), null,
                        List.of(
                                new NpcDef.Action(ActionType.OPEN_SPELL_LEARNING, List.of("spell.flaming_arrow")),
                                new NpcDef.Action(ActionType.HEAL),
                                new NpcDef.Action(ActionType.END_CONVERSATION)))));
        File file = dir.resolve("npcs.bin").toFile();
        NpcDefBinaryIO.write(file, List.of(source));

        NpcDef read = NpcDefBinaryIO.read(file).get(0);
        assertEquals(source.getWelcomeText(), read.getWelcomeText());
        assertEquals(source.getTopics().get(0).getKeywords(), read.getTopics().get(0).getKeywords());
        assertEquals(List.of(ActionType.OPEN_SPELL_LEARNING, ActionType.HEAL, ActionType.END_CONVERSATION),
                read.getTopics().get(0).getActions().stream().map(NpcDef.Action::getType).toList());
        assertEquals(List.of("spell.flaming_arrow"),
                read.getTopics().get(0).getActions().get(0).getTargets());
    }

    @Test
    void oldVersionsAreRejected(@TempDir Path dir) throws Exception {
        File file = dir.resolve("old.bin").toFile();
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1024))) {
            out.write("T4CNPC".getBytes(StandardCharsets.US_ASCII));
            BinaryIOUtils.writeShortLE(out, (short) 9);
            BinaryIOUtils.writeIntLE(out, 0);
        }
        assertThrows(GameException.class, () -> NpcDefBinaryIO.read(file));
    }
}
