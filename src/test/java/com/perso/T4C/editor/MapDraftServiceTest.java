package com.perso.T4C.editor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MapDraftServiceTest {
  @TempDir Path tempDir;

  @Test
  void duplicatesTheMapAndEveryPresentFileBackedLayer() throws IOException {
    Path sourceDirectory = Files.createDirectories(tempDir.resolve("worldmap"));
    Path source = sourceDirectory.resolve("worldmap.mapbin");
    Files.write(source, new byte[] {1, 2, 3});
    Files.write(sourceDirectory.resolve("worldmap.decorbin"), new byte[] {4});
    Files.write(sourceDirectory.resolve("worldmap.colbin"), new byte[] {5});
    Files.write(sourceDirectory.resolve("worldmap.musiczones.bin"), new byte[] {6});
    Files.write(sourceDirectory.resolve("worldmap.musiczones.json"), new byte[] {7});
    Files.write(sourceDirectory.resolve("worldmap.monsters.json"), new byte[] {8});
    Files.write(sourceDirectory.resolve("worldmap.npcs.json"), new byte[] {9});

    Path draft = MapDraftService.duplicate(source, tempDir, "MyMap");

    assertEquals(tempDir.resolve("drafts/MyMap/MyMap.mapbin"), draft);
    assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(draft));
    assertArrayEquals(new byte[] {4}, Files.readAllBytes(draft.resolveSibling("MyMap.decorbin")));
    assertArrayEquals(new byte[] {5}, Files.readAllBytes(draft.resolveSibling("MyMap.colbin")));
    assertArrayEquals(
        new byte[] {6}, Files.readAllBytes(draft.resolveSibling("MyMap.musiczones.bin")));
    assertArrayEquals(
        new byte[] {7}, Files.readAllBytes(draft.resolveSibling("MyMap.musiczones.json")));
    assertArrayEquals(
        new byte[] {8}, Files.readAllBytes(draft.resolveSibling("MyMap.monsters.json")));
    assertArrayEquals(new byte[] {9}, Files.readAllBytes(draft.resolveSibling("MyMap.npcs.json")));
    assertThrows(IOException.class, () -> MapDraftService.duplicate(source, tempDir, "MyMap"));
  }

  @Test
  void rejectsNamesThatWouldEscapeTheDraftDirectory() throws IOException {
    Path source = tempDir.resolve("source.mapbin");
    Files.write(source, new byte[] {1});

    assertThrows(
        IllegalArgumentException.class,
        () -> MapDraftService.duplicate(source, tempDir, "../outside"));
  }
}
