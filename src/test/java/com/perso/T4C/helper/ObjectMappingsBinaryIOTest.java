package com.perso.T4C.helper;

import com.perso.T4C.render.ObjectMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ObjectMappingsBinaryIOTest {
    @Test
    void objectMappingRoundTrips(@TempDir Path dir) throws Exception {
        ObjectMapping mapping = new ObjectMapping(42, "64kChest", true, false,
                "open.wav", "close.wav", true, "Chest", 3);
        ObjectMappingsBinaryIO.Entry source = new ObjectMappingsBinaryIO.Entry("chest_wood", mapping);
        File file = dir.resolve("objects.bin").toFile();

        ObjectMappingsBinaryIO.write(file, List.of(source));
        ObjectMappingsBinaryIO.Entry read = ObjectMappingsBinaryIO.read(file).get(0);

        assertEquals(source.logicalName, read.logicalName);
        assertEquals(mapping.id, read.mapping.id);
        assertEquals(mapping.sprite, read.mapping.sprite);
        assertEquals(mapping.clickAnimate, read.mapping.clickAnimate);
        assertEquals(mapping.mirror, read.mapping.mirror);
        assertEquals(mapping.animateSound, read.mapping.animateSound);
        assertEquals(mapping.reverseAnimateSound, read.mapping.reverseAnimateSound);
        assertEquals(mapping.alwaysBehindEntities, read.mapping.alwaysBehindEntities);
        assertEquals(mapping.depthTileOffsetY, read.mapping.depthTileOffsetY);
    }
}
