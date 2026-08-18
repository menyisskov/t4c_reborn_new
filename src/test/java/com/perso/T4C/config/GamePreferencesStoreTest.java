package com.perso.T4C.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GamePreferencesStoreTest {
  @TempDir Path tempDir;

  @Test
  void missingAndInvalidFilesUseDefaults() throws Exception {
    GamePreferences missing = GamePreferencesStore.load(tempDir.resolve("missing.json"));
    assertEquals(0.7f, missing.getMusicVolume(), 0.001f);
    Path invalid = tempDir.resolve("invalid.json");
    Files.writeString(invalid, "not-json");
    GamePreferences recovered = GamePreferencesStore.load(invalid);
    assertEquals(1f, recovered.getBrightness(), 0.001f);
  }

  @Test
  void valuesAreClampedAndRoundTrip() {
    Path file = tempDir.resolve("settings.json");
    GamePreferences preferences = new GamePreferences();
    preferences.setMusicVolume(3f);
    preferences.setEffectsVolume(-2f);
    preferences.setBrightness(9f);
    preferences.setFullscreen(true);
    preferences.setChatLogging(true);
    preferences.setChatLogFilename("session.log");
    GamePreferencesStore.save(file, preferences);
    GamePreferences loaded = GamePreferencesStore.load(file);
    assertEquals(1f, loaded.getMusicVolume(), 0.001f);
    assertEquals(0f, loaded.getEffectsVolume(), 0.001f);
    assertEquals(1.25f, loaded.getBrightness(), 0.001f);
    assertTrue(loaded.isFullscreen());
    assertTrue(loaded.isChatLogging());
    assertEquals("session.log", loaded.getChatLogFilename());
  }

  @Test
  void missingLogFilenameUsesOriginalCompatibleDefault() throws Exception {
    Path file = tempDir.resolve("settings.json");
    Files.writeString(file, "{\"chatLogFilename\":\"  \"}");
    assertEquals("t4c-chat.log", GamePreferencesStore.load(file).getChatLogFilename());
  }
}
