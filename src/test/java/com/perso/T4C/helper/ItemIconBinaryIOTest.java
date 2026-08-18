package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.exception.GameException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ItemIconBinaryIOTest {
  @Test
  void writeThenReadKeepsEveryBinding(@TempDir Path dir) throws Exception {
    Map<Integer, String> icons = new LinkedHashMap<>();
    icons.put(1, "64kIconSword");
    icons.put(2, "64kIconSword");
    icons.put(42, "64kIconShield");
    icons.put(9999, "64kIconPotion");
    File file = dir.resolve("item_icons.bin").toFile();
    ItemIconBinaryIO.write(file, icons);
    assertEquals(icons, ItemIconBinaryIO.read(file));
  }

  @Test
  void writeCreatesMissingParentDirectories(@TempDir Path dir) throws Exception {
    File file = dir.resolve("nested/mappings/item_icons.bin").toFile();
    ItemIconBinaryIO.write(file, Map.of(7, "64kIconRing"));
    assertTrue(file.isFile());
    assertEquals(Map.of(7, "64kIconRing"), ItemIconBinaryIO.read(file));
  }

  @Test
  void nullMapWritesAnEmptyTable(@TempDir Path dir) throws Exception {
    File file = dir.resolve("item_icons.bin").toFile();
    ItemIconBinaryIO.write(file, null);
    assertEquals(Map.of(), ItemIconBinaryIO.read(file));
  }

  @Test
  void invalidAppearanceIdsAndBlankSpritesAreDropped(@TempDir Path dir) throws Exception {
    Map<Integer, String> icons = new LinkedHashMap<>();
    icons.put(0, "64kIconSword");
    icons.put(-3, "64kIconSword");
    icons.put(5, "   ");
    icons.put(6, "64kIconGem");
    File file = dir.resolve("item_icons.bin").toFile();
    ItemIconBinaryIO.write(file, icons);
    assertEquals(Map.of(6, "64kIconGem"), ItemIconBinaryIO.read(file));
  }

  @Test
  void wrongMagicIsRejected(@TempDir Path dir) throws Exception {
    File file = dir.resolve("bogus.bin").toFile();
    Files.write(
        file.toPath(), "NOTT4CICO-payload".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    assertThrows(GameException.class, () -> ItemIconBinaryIO.read(file));
  }
}
