package com.perso.T4C.helper;

import com.perso.T4C.harvest.HerbDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HerbDefinitionBinaryIOTest {
    @TempDir Path tempDir;

    @Test
    void roundTripsEveryEditableField() throws Exception {
        File file = tempDir.resolve("herbs.bin").toFile();
        HerbDefinitionBinaryIO.write(file, List.of(new HerbDefinition(
                "test-herb", "item.test_herb", "64kTestHerb", 37)));
        HerbDefinition loaded = HerbDefinitionBinaryIO.read(file).get(0);
        assertEquals("test-herb", loaded.getId());
        assertEquals("item.test_herb", loaded.getItemKey());
        assertEquals("64kTestHerb", loaded.getWorldSprite());
        assertEquals(37, loaded.getSpawnWeight());
    }
}
