package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.objects.ObjectPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObjectPositionBinaryIOTest {
    @Test
    void objectPositionRoundTrips(@TempDir Path dir) throws Exception {
        ObjectPos source = new ObjectPos("chest_wood", 10, 20, 0, true);
        File file = dir.resolve("positions.bin").toFile();

        ObjectPositionBinaryIO.write(file, List.of(source));
        ObjectPos read = ObjectPositionBinaryIO.read(file).get(0);

        assertEquals(source.name(), read.name());
        assertEquals(source.x(), read.x());
        assertEquals(source.y(), read.y());
        assertEquals(source.z(), read.z());
        assertEquals(source.mirror(), read.mirror());
    }

    @Test
    void unsupportedVersionIsRejected(@TempDir Path dir) throws Exception {
        File file = dir.resolve("old-positions.bin").toFile();
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1024))) {
            out.write("T4COBJ".getBytes(StandardCharsets.US_ASCII));
            BinaryIOUtils.writeShortLE(out, (short) 99);
            BinaryIOUtils.writeIntLE(out, 0);
        }

        assertThrows(GameException.class, () -> ObjectPositionBinaryIO.read(file));
    }
}
