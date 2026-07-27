package com.perso.T4C.tools;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WdaCollisionImporterTest {
    private static final File SOURCE =
            new File("C:/Users/nicon/Desktop/VSF 1.50/T4C Worlds.WDA");

    @Test
    void readsProvidedVsf150WorldsWdaWhenAvailable() throws Exception {
        if (!SOURCE.isFile()) {
            return;
        }

        List<WdaCollisionImporter.ImportedWorld> worlds =
                WdaCollisionImporter.importFile(SOURCE, false);

        assertEquals(4, worlds.size());
        assertEquals(List.of(1, 2, 3, 0),
                worlds.stream().map(WdaCollisionImporter.ImportedWorld::getId).toList());
        for (WdaCollisionImporter.ImportedWorld world : worlds) {
            assertEquals(3072, world.getWidth());
            assertEquals(3072, world.getHeight());
            assertEquals(3072 * 3072, world.getData().length);
            assertTrue(world.getName() != null && !world.getName().isBlank());
        }
    }
}
