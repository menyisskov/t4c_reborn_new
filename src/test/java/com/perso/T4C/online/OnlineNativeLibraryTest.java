package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OnlineNativeLibraryTest {
  @Test
  void bundledWindowsLibraryMatchesItsReleaseChecksum() throws Exception {
    byte[] library = OnlineNativeLibrary.verifiedLibrary();
    assertTrue(library.length > 2);
    assertEquals('M', library[0]);
    assertEquals('Z', library[1]);
  }

  @TempDir java.nio.file.Path temp;

  @Test
  void repeatedLaunchesReuseOneFolderAndNeverLoadADifferentFile() throws Exception {
    byte[] library = OnlineNativeLibrary.verifiedLibrary();
    var first = OnlineNativeLibrary.prepare(temp, library);
    var second = OnlineNativeLibrary.prepare(temp, library);
    assertEquals(first, second);
    try (var folders = java.nio.file.Files.list(temp)) {
      assertEquals(1, folders.count());
    }
    java.nio.file.Files.write(first, new byte[] {1, 2, 3});
    var repaired = OnlineNativeLibrary.prepare(temp, library);
    org.junit.jupiter.api.Assertions.assertArrayEquals(library, java.nio.file.Files.readAllBytes(repaired));
  }
}
