package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OnlineNativeLibraryTest {
  @Test
  void bundledWindowsLibraryMatchesItsReleaseChecksum() throws Exception {
    byte[] library = OnlineNativeLibrary.verifiedLibrary();
    assertTrue(library.length > 2);
    assertEquals('M', library[0]);
    assertEquals('Z', library[1]);
  }
}
