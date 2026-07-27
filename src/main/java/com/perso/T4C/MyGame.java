package com.perso.T4C;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.perso.T4C.helper.GameCursorManager;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.screens.LoadingScreen;
import com.perso.T4C.screens.MainGameScreen;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.exception.GameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Game application entry point that holds shared resources (SpriteBatch, AssetManager, fonts).
 */
public class MyGame extends Game {
    private static final Logger log = LoggerFactory.getLogger(MyGame.class);

    public SpriteBatch batch;
    public AssetManager assetManager;
    public BitmapFont customFont;
    public Cursor customCursor;
    public GameCursorManager cursorManager;
    private final Map<MapDefinition, MapReader> preloadedMaps = new ConcurrentHashMap<>();
    private ExecutorService mapPreloadExecutor;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("T4C Reborn");
        if (com.perso.T4C.config.GameConstants.FULLSCREEN) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        } else {
            config.setWindowedMode(
                    com.perso.T4C.config.GameConstants.WINDOW_WIDTH,
                    com.perso.T4C.config.GameConstants.WINDOW_HEIGHT
            );
        }
        config.setResizable(false);
        config.useVsync(true);
        config.setWindowIcon(
                Files.FileType.Absolute,
                Paths.get("App_icon.png").toAbsolutePath().toString()
        );
        new Lwjgl3Application(new MyGame(), config);
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        cursorManager = new GameCursorManager();
        setScreen(new LoadingScreen(this));
    }

    public synchronized void startMapPreloadAsync() {
        if (mapPreloadExecutor != null) {
            return;
        }
        // Single thread: parallel preloads cause massive concurrent String allocation
        // from readUtf8, triggering G1 Humongous Allocation spikes (up to 2s pauses).
        mapPreloadExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "map-preloader");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        for (MapDefinition mapDefinition : MapDefinition.values()) {
            mapPreloadExecutor.submit(() -> {
                try {
                    getOrLoadMapReader(mapDefinition);
                } catch (Exception e) {
                    log.warn("Failed to preload map {}", mapDefinition.name(), e);
                }
            });
        }
        mapPreloadExecutor.shutdown();
    }

    public MapReader getOrLoadMapReader(MapDefinition mapDefinition) throws GameException {
        return preloadedMaps.computeIfAbsent(mapDefinition, def -> {
            try {
                String mapFile = com.badlogic.gdx.Gdx.files.internal(def.getMapPath()).file().getAbsolutePath();
                return new MapReader(new File(mapFile));
            } catch (Exception e) {
                throw new RuntimeException("Failed to load map " + def.name(), e);
            }
        });
    }

    @Override
    public void dispose() {
        // Best-effort: persist player position even if screen dispose isn't called.
        try {
            if (getScreen() instanceof MainGameScreen mgs) {
                var p = mgs.getPlayer();
                if (p != null) {
                    PlayerStateStore.save(p);
                }
            }
        } catch (Throwable t) {
            log.warn("Failed to persist player state on dispose", t);
        }

        if (batch != null) batch.dispose();
        if (assetManager != null) assetManager.dispose();
        if (customFont != null) customFont.dispose();
        if (cursorManager != null) cursorManager.dispose(this);
        if (mapPreloadExecutor != null) {
            mapPreloadExecutor.shutdownNow();
        }
        for (MapReader mapReader : preloadedMaps.values()) {
            try {
                mapReader.close();
            } catch (Exception ignored) {}
        }
        preloadedMaps.clear();
    }
}
