package com.perso.T4C.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XpCurveBinaryIOTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsEntries() throws Exception {
        List<XpCurve.Entry> expected = List.of(
                new XpCurve.Entry(1, 100, 0),
                new XpCurve.Entry(2, 360, 100),
                new XpCurve.Entry(200, 0, 125_889_551));
        Path file = tempDir.resolve("xp_curve.bin");

        XpCurveBinaryIO.write(file.toFile(), expected);
        List<XpCurve.Entry> actual = XpCurveBinaryIO.read(file.toFile());

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getLevel(), actual.get(i).getLevel());
            assertEquals(expected.get(i).getXpToNextLevel(), actual.get(i).getXpToNextLevel());
            assertEquals(expected.get(i).getTotalXp(), actual.get(i).getTotalXp());
        }
    }
}
