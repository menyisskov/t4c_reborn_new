package com.perso.T4C.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GamePreferencesStoreTest {
    @TempDir Path tempDir;

    @Test
    void missingAndInvalidFilesUseDefaults() throws Exception {
        GamePreferences missing = GamePreferencesStore.load(tempDir.resolve("missing.json"));
        assertEquals(0.7f, missing.getMusicVolume(), 0.001f);
        assertEquals(GameConstants.OVERLAY_PERCENTAGE_DEFAULT, missing.getOverlayPercentage(), 0.001f);

        Path invalid = tempDir.resolve("invalid.json");
        Files.writeString(invalid, "not-json");
        GamePreferences recovered = GamePreferencesStore.load(invalid);
        assertEquals(1f, recovered.getBrightness(), 0.001f);
        assertEquals(GameConstants.OVERLAY_PERCENTAGE_DEFAULT, recovered.getOverlayPercentage(), 0.001f);

        Path legacy = tempDir.resolve("legacy.json");
        Files.writeString(legacy, "{\"brightness\":0.8}");
        assertEquals(GameConstants.OVERLAY_PERCENTAGE_DEFAULT,
                GamePreferencesStore.load(legacy).getOverlayPercentage(), 0.001f);
    }

    @Test
    void valuesAreClampedAndRoundTrip() {
        Path file = tempDir.resolve("settings.json");
        GamePreferences preferences = new GamePreferences();
        preferences.setMusicVolume(3f);
        preferences.setEffectsVolume(-2f);
        preferences.setBrightness(9f);
        preferences.setOverlayPercentage(125f);
        preferences.setFullscreen(true);

        GamePreferencesStore.save(file, preferences);
        GamePreferences loaded = GamePreferencesStore.load(file);

        assertEquals(1f, loaded.getMusicVolume(), 0.001f);
        assertEquals(0f, loaded.getEffectsVolume(), 0.001f);
        assertEquals(1.25f, loaded.getBrightness(), 0.001f);
        assertEquals(100f, loaded.getOverlayPercentage(), 0.001f);
        assertTrue(loaded.isFullscreen());
    }

    @Test
    void overlayPercentageIsClampedAndPersisted() {
        GamePreferences preferences = new GamePreferences();
        preferences.setOverlayPercentage(-5f);
        assertEquals(0f, GamePreferencesStore.sanitize(preferences).getOverlayPercentage(), 0.001f);

        preferences.setOverlayPercentage(Float.NaN);
        assertEquals(GameConstants.OVERLAY_PERCENTAGE_DEFAULT,
                GamePreferencesStore.sanitize(preferences).getOverlayPercentage(), 0.001f);

        preferences.setOverlayPercentage(42f);
        Path file = tempDir.resolve("overlay-settings.json");
        GamePreferencesStore.save(file, preferences);
        assertEquals(42f, GamePreferencesStore.load(file).getOverlayPercentage(), 0.001f);
    }
}
