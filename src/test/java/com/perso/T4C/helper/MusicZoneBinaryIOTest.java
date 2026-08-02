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

class MusicZoneBinaryIOTest {
    @Test
    void musicZoneRoundTrips(@TempDir Path dir) throws Exception {
        MusicZoneBinaryIO.Entry source = new MusicZoneBinaryIO.Entry();
        source.name = "Lighthaven";
        source.x1 = 0;
        source.y1 = 0;
        source.x2 = 100;
        source.y2 = 100;
        source.music = "town_theme.ogg";
        File file = dir.resolve("zones.bin").toFile();

        MusicZoneBinaryIO.write(file, List.of(source));
        MusicZoneBinaryIO.Entry read = MusicZoneBinaryIO.read(file).get(0);

        assertEquals(source.name, read.name);
        assertEquals(source.x1, read.x1);
        assertEquals(source.y1, read.y1);
        assertEquals(source.x2, read.x2);
        assertEquals(source.y2, read.y2);
        assertEquals(source.music, read.music);
    }

    @Test
    void unsupportedVersionIsRejected(@TempDir Path dir) throws Exception {
        File file = dir.resolve("old-zones.bin").toFile();
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1024))) {
            out.write("T4CMUZ".getBytes(StandardCharsets.US_ASCII));
            BinaryIOUtils.writeShortLE(out, (short) 0);
            BinaryIOUtils.writeIntLE(out, 0);
        }

        assertThrows(GameException.class, () -> MusicZoneBinaryIO.read(file));
    }
}
