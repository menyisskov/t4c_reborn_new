package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.skill.SkillDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkillDefBinaryIOTest {
    @Test
    void skillDefinitionRoundTrips(@TempDir Path dir) throws Exception {
        SkillDefinition source = new SkillDefinition(
                "power_strike",
                10,
                20,
                15,
                12,
                5,
                5,
                250,
                Map.of("basic_strike", 5),
                3000L
        );
        File file = dir.resolve("skills.bin").toFile();

        SkillDefBinaryIO.write(file, List.of(source));
        SkillDefinition read = SkillDefBinaryIO.read(file).get(0);

        assertEquals(source.id(), read.id());
        assertEquals(source.minimumLevel(), read.minimumLevel());
        assertEquals(source.minimumStrength(), read.minimumStrength());
        assertEquals(source.minimumEndurance(), read.minimumEndurance());
        assertEquals(source.minimumAgility(), read.minimumAgility());
        assertEquals(source.minimumIntelligence(), read.minimumIntelligence());
        assertEquals(source.minimumWisdom(), read.minimumWisdom());
        assertEquals(source.learningCost(), read.learningCost());
        assertEquals(source.prerequisites(), read.prerequisites());
        assertEquals(source.useCooldownMillis(), read.useCooldownMillis());
    }

    @Test
    void unsupportedVersionIsRejected(@TempDir Path dir) throws Exception {
        File file = dir.resolve("old-skills.bin").toFile();
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1024))) {
            out.write("T4CSKL".getBytes(StandardCharsets.US_ASCII));
            BinaryIOUtils.writeShortLE(out, (short) 0);
            BinaryIOUtils.writeIntLE(out, 0);
        }

        assertThrows(GameException.class, () -> SkillDefBinaryIO.read(file));
    }
}
