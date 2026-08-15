package com.perso.T4C.helper;

import com.perso.T4C.config.Paths;
import com.perso.T4C.exception.GameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppearanceDefaultsBinaryIOTest {

    @Test
    void writeThenReadKeepsEveryTable(@TempDir Path dir) throws Exception {
        AppearanceDefaultsBinaryIO.Defaults defaults = new AppearanceDefaultsBinaryIO.Defaults(
                List.of(new AppearanceDefaultsBinaryIO.NakedPart("MALE", "BODY", "PupNakedBody"),
                        new AppearanceDefaultsBinaryIO.NakedPart("FEMALE", "BODY", "WoNakedBody")),
                List.of(new AppearanceDefaultsBinaryIO.ConcealmentRule(
                        "HEAD", "PupPlateHelm", List.of("HEAD", "HAIR"), false),
                        new AppearanceDefaultsBinaryIO.ConcealmentRule(
                                "BODY", "PupWhiteRobe", List.of("BOOT"), true)),
                List.of(new AppearanceDefaultsBinaryIO.EquippedOverride(
                        "FEMALE", "BODY", "PupBodyClothSet1", "BODY", "WoClothBody")));
        File file = dir.resolve("appearance_defaults.bin").toFile();

        AppearanceDefaultsBinaryIO.write(file, defaults);

        assertEquals(defaults, AppearanceDefaultsBinaryIO.read(file));
    }

    @Test
    void concealmentFieldsSurviveIndependently(@TempDir Path dir) throws Exception {
        File file = dir.resolve("appearance_defaults.bin").toFile();
        AppearanceDefaultsBinaryIO.write(file, new AppearanceDefaultsBinaryIO.Defaults(List.of(),
                List.of(new AppearanceDefaultsBinaryIO.ConcealmentRule(
                        "HEAD", "OnlyHair", List.of("HAIR"), true))));

        AppearanceDefaultsBinaryIO.ConcealmentRule rule =
                AppearanceDefaultsBinaryIO.read(file).concealmentRules().get(0);

        assertEquals(List.of("HAIR"), rule.hiddenParts());
        assertTrue(rule.hidesExplicit());
    }

    @Test
    void writeCreatesMissingParentDirectories(@TempDir Path dir) throws Exception {
        File file = dir.resolve("nested/mappings/appearance_defaults.bin").toFile();

        AppearanceDefaultsBinaryIO.write(file, new AppearanceDefaultsBinaryIO.Defaults(
                List.of(new AppearanceDefaultsBinaryIO.NakedPart("MALE", "HEAD", "PupNakedHead")),
                List.of()));

        assertTrue(file.isFile());
        assertEquals(1, AppearanceDefaultsBinaryIO.read(file).nakedParts().size());
    }

    @Test
    void nullDefaultsWriteAnEmptyCatalog(@TempDir Path dir) throws Exception {
        File file = dir.resolve("appearance_defaults.bin").toFile();

        AppearanceDefaultsBinaryIO.write(file, null);

        AppearanceDefaultsBinaryIO.Defaults defaults = AppearanceDefaultsBinaryIO.read(file);
        assertEquals(List.of(), defaults.nakedParts());
        assertEquals(List.of(), defaults.concealmentRules());
        assertEquals(List.of(), defaults.equippedOverrides());
    }

    @Test
    void wrongMagicIsRejected(@TempDir Path dir) throws Exception {
        File file = dir.resolve("bogus.bin").toFile();
        Files.write(file.toPath(), "NOTT4CAPD-payload".getBytes(StandardCharsets.US_ASCII));

        assertThrows(GameException.class, () -> AppearanceDefaultsBinaryIO.read(file));
    }

    @Test
    void shippedCatalogHoldsBothGendersAndEveryHelmet() throws Exception {
        File file = new File(Paths.APPEARANCE_DEFAULTS_BIN);
        if (!file.exists()) {
            return; // asset not present in this checkout
        }
        AppearanceDefaultsBinaryIO.Defaults defaults = AppearanceDefaultsBinaryIO.read(file);

        assertEquals(18, defaults.nakedParts().size());
        assertEquals(70, defaults.concealmentRules().size());
        assertEquals(2, defaults.equippedOverrides().size());
        assertEquals(4, defaults.concealmentRules().stream()
                .filter(rule -> rule.hiddenParts().contains("HEAD")).count());
    }

    @Test
    void v1FileUpconvertsToConcealmentRules(@TempDir Path dir) throws Exception {
        File file = dir.resolve("v1.bin").toFile();
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
            out.write("T4CAPD".getBytes(StandardCharsets.US_ASCII));
            BinaryIOUtils.writeShortLE(out, (short) 1);
            BinaryIOUtils.writeIntLE(out, 0);
            BinaryIOUtils.writeIntLE(out, 1);
            BinaryIOUtils.writeString(out, "LegacyHelm");
            BinaryIOUtils.writeIntLE(out, 1);
            BinaryIOUtils.writeIntLE(out, 1);
        }

        assertEquals(new AppearanceDefaultsBinaryIO.ConcealmentRule(
                        "HEAD", "LegacyHelm", List.of("HEAD", "HAIR"), false),
                AppearanceDefaultsBinaryIO.read(file).concealmentRules().get(0));
    }
}
