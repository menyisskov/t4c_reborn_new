package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroundMosaicBinaryIOTest {

    @Test
    void writeThenReadKeepsExplicitAndTemplatePatterns(@TempDir Path dir) throws Exception {
        List<GroundMosaicBinaryIO.Definition> definitions = List.of(
                new GroundMosaicBinaryIO.Definition("0x1", 13, 15, List.of("64kNormalGrass (&x, &y)")),
                new GroundMosaicBinaryIO.Definition("0xd", 2, 2, List.of(
                        "RockFloor (1, 1)", "RockFloor (1, 2)", "RockFloor (2, 1)", "RockFloor (2, 2)")));
        File file = dir.resolve("ground_mosaics.bin").toFile();

        GroundMosaicBinaryIO.write(file, definitions);

        assertEquals(definitions, GroundMosaicBinaryIO.read(file));
    }

    @Test
    void frameOrderIsPreserved(@TempDir Path dir) throws Exception {
        List<String> frames = List.of("A (1, 1)", "A (1, 2)", "A (2, 1)", "A (2, 2)");
        File file = dir.resolve("ground_mosaics.bin").toFile();

        GroundMosaicBinaryIO.write(file, List.of(new GroundMosaicBinaryIO.Definition("0x2", 2, 2, frames)));

        assertEquals(frames, GroundMosaicBinaryIO.read(file).get(0).frames());
    }

    @Test
    void writeCreatesMissingParentDirectories(@TempDir Path dir) throws Exception {
        File file = dir.resolve("nested/mappings/ground_mosaics.bin").toFile();

        GroundMosaicBinaryIO.write(file, List.of(
                new GroundMosaicBinaryIO.Definition("0x1", 1, 1, List.of("Grass (1, 1)"))));

        assertTrue(file.isFile());
        assertEquals(1, GroundMosaicBinaryIO.read(file).size());
    }

    @Test
    void nullListWritesAnEmptyCatalog(@TempDir Path dir) throws Exception {
        File file = dir.resolve("ground_mosaics.bin").toFile();

        GroundMosaicBinaryIO.write(file, null);

        assertEquals(List.of(), GroundMosaicBinaryIO.read(file));
    }

    @Test
    void wrongMagicIsRejected(@TempDir Path dir) throws Exception {
        File file = dir.resolve("bogus.bin").toFile();
        Files.write(file.toPath(), "NOTT4CGMO-payload".getBytes(StandardCharsets.US_ASCII));

        assertThrows(GameException.class, () -> GroundMosaicBinaryIO.read(file));
    }

    @Test
    void shippedCatalogStillHolds91PatternsAnd803Frames() throws Exception {
        File file = new File(com.perso.T4C.config.Paths.GROUND_MOSAICS_BIN);
        if (!file.exists()) {
            return; // asset not present in this checkout
        }
        List<GroundMosaicBinaryIO.Definition> definitions = GroundMosaicBinaryIO.read(file);

        assertEquals(91, definitions.size());
        assertEquals(803, definitions.stream().mapToInt(d -> d.frames().size()).sum());
    }
}
