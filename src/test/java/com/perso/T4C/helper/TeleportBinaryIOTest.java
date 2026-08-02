package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeleportBinaryIOTest {
    @Test
    void teleportEntryRoundTrips(@TempDir Path dir) throws Exception {
        TeleportBinaryIO.Entry source = new TeleportBinaryIO.Entry();
        source.id = 1;
        source.sourceZ = 0;
        source.sourceX = 10;
        source.sourceY = 20;
        source.targetZ = 1;
        source.targetX = 30;
        source.targetY = 40;
        File file = dir.resolve("teleports.bin").toFile();

        TeleportBinaryIO.write(file, List.of(source));
        TeleportBinaryIO.Entry read = TeleportBinaryIO.read(file).get(0);

        assertEquals(source.id, read.id);
        assertEquals(source.sourceZ, read.sourceZ);
        assertEquals(source.sourceX, read.sourceX);
        assertEquals(source.sourceY, read.sourceY);
        assertEquals(source.targetZ, read.targetZ);
        assertEquals(source.targetX, read.targetX);
        assertEquals(source.targetY, read.targetY);
    }

    @Test
    void unsupportedVersionIsRejected(@TempDir Path dir) throws Exception {
        File file = dir.resolve("old-teleports.bin").toFile();
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1024))) {
            out.write("T4CTLP".getBytes(StandardCharsets.US_ASCII));
            BinaryIOUtils.writeShortLE(out, (short) 0);
            BinaryIOUtils.writeIntLE(out, 0);
        }

        assertThrows(GameException.class, () -> TeleportBinaryIO.read(file));
    }
}
