package com.perso.T4C;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.config.Paths;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.GameCursorManager;
import com.perso.T4C.screens.MapEditorScreen;
import java.io.File;

/**
 * Entry point for the standalone map editor
 */
public class MapEditor {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("T4C Map Editor");
        config.setWindowedMode(
                com.perso.T4C.config.GameConstants.WINDOW_WIDTH,
                com.perso.T4C.config.GameConstants.WINDOW_HEIGHT
        );
        config.setResizable(true);
        config.useVsync(true);
        config.setWindowIcon(
                Files.FileType.Absolute,
                java.nio.file.Paths.get("App_icon.png").toAbsolutePath().toString()
        );
        final MapEditorScreen[] editorScreenRef = new MapEditorScreen[1];
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public boolean closeRequested() {
                MapEditorScreen screen = editorScreenRef[0];
                if (screen != null) {
                    if (screen.isExitSaveCompleted()) {
                        return true;
                    }
                    screen.requestExitWithSaveScreen();
                    return false;
                }
                return true;
            }
        });

        final MyGame editorGame = new MyGame() {
            private MapEditorScreen editorScreen;

            @Override
            public void create() {
                Gdx.app.setLogLevel(Application.LOG_DEBUG);
                batch = new SpriteBatch();
                assetManager = new AssetManager();
                cursorManager = new GameCursorManager();
                try {
                    File defaultMap = new File(Paths.MAP);
                    if (!defaultMap.exists()) {
                        throw new RuntimeException("Missing v2 map file: " + defaultMap.getPath());
                    }

                    editorScreen = new MapEditorScreen(this);
                    editorScreenRef[0] = editorScreen;
                    setScreen(editorScreen);
                } catch (GameException e) {
                    throw new RuntimeException("Failed to start map editor", e);
                }
            }

            @Override
            public void dispose() {
                if (editorScreen != null) {
                    editorScreen.saveAllState();
                }
                super.dispose();
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (editorGame.getScreen() instanceof MapEditorScreen screen) {
                    screen.saveAllState();
                }
            } catch (Throwable ignored) {
            }
        }, "map-editor-shutdown-save"));

        new Lwjgl3Application(editorGame, config);
    }
}
