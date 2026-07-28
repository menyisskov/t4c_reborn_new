package com.perso.T4C.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Single source of truth for runtime settings and their JSON persistence.
 */
public final class GamePreferencesStore {
    public static final String FILE_NAME = Paths.GAME_PREFERENCES_FILE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static GamePreferences current = load(Path.of(FILE_NAME));

    private GamePreferencesStore() {
    }

    public static GamePreferences get() {
        return current;
    }

    public static void save() {
        save(Path.of(FILE_NAME), current);
    }

    public static GamePreferences load(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return sanitize(new GamePreferences());
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            GamePreferences parsed = GSON.fromJson(reader, GamePreferences.class);
            return sanitize(parsed == null ? new GamePreferences() : parsed);
        } catch (Exception ignored) {
            return sanitize(new GamePreferences());
        }
    }

    public static void save(Path path, GamePreferences preferences) {
        if (path == null) return;
        GamePreferences safe = sanitize(preferences == null ? new GamePreferences() : preferences);
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(safe, writer);
        } catch (Exception ignored) {
            // Settings must never prevent the game from running.
        }
    }

    static GamePreferences sanitize(GamePreferences value) {
        value.setMusicVolume(clamp01(value.getMusicVolume()));
        value.setEffectsVolume(clamp01(value.getEffectsVolume()));
        value.setBrightness(clamp(value.getBrightness(), 0.5f, 1.25f));
        return value;
    }

    private static float clamp01(float value) {
        return clamp(value, 0f, 1f);
    }

    private static float clamp(float value, float min, float max) {
        if (!Float.isFinite(value)) return min;
        return Math.max(min, Math.min(max, value));
    }

    /** Applies settings supported by LibGDX at runtime. */
    public static void applyDisplaySettings() {
        if (Gdx.graphics == null) return;
        GamePreferences settings = get();
        Gdx.graphics.setVSync(settings.isVSync());
        boolean currentlyFullscreen = Gdx.graphics.isFullscreen();
        if (settings.isFullscreen() && !currentlyFullscreen) {
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(mode);
        } else if (!settings.isFullscreen() && currentlyFullscreen) {
            Gdx.graphics.setWindowedMode(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        }
    }
}
