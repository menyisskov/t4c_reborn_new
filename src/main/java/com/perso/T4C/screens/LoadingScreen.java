package com.perso.T4C.screens;

import static com.perso.T4C.config.GameConstants.WINDOW_HEIGHT;
import static com.perso.T4C.config.GameConstants.WINDOW_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.perso.T4C.MyGame;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.config.Paths;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.ui.FontManager;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LoadingScreen implements Screen {
  private final MyGame game;
  private final OrthographicCamera camera;
  private final FillViewport viewport;
  private final SpriteBatch batch;
  private static final String SOUNDS_DIR = Paths.SOUNDS_DIR;
  private final TextureRegion loadingImage;
  private final List<String> failedSounds = new ArrayList<>();
  private boolean retryingFailedAssets = false;
  private boolean cursorApplied = false;

  public LoadingScreen(MyGame game) throws GameException {
    this.game = game;
    this.batch = game.batch;
    this.camera = new OrthographicCamera();
    camera.setToOrtho(false, WINDOW_WIDTH, WINDOW_HEIGHT);
    this.viewport = new FillViewport(WINDOW_WIDTH, WINDOW_HEIGHT, camera);
    SpriteLoader.getInstance().loadSpriteBin(Paths.SPRITE_BIN);
    initGameCursor();
    this.loadingImage = SpriteLoader.getInstance().getRegionFromSpriteName("Loading");
    game.customFont = FontManager.getInstance().getT4CBeaulieuFont(22, Color.WHITE);
    loadAllAssetsRecursive(SOUNDS_DIR);
    SoundManager.init(game.assetManager);
    CompletableFuture.runAsync(
        () -> {
          com.perso.T4C.teleport.TeleportRegistry.load();
          com.perso.T4C.spawn.SpawnRegistry.monsters();
          com.perso.T4C.spawn.SpawnRegistry.npcs();
        });
  }

  private void initGameCursor() {
    if (cursorApplied) return;
    try {
      if (game.cursorManager != null) {
        game.cursorManager.ensureDefaultCursor(game);
        game.cursorManager.applyDefaultCursor(game);
        cursorApplied = true;
      }
    } catch (Throwable t) {
      Gdx.app.error("LoadingScreen", "Failed to apply custom cursor", t);
    }
  }

  @Override
  public void render(float delta) {
    if (!cursorApplied) {
      initGameCursor();
    }
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    if (loadingImage != null) {
      batch.draw(loadingImage, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    batch.end();
    boolean assetsLoaded = false;
    try {
      assetsLoaded = game.assetManager.update();
    } catch (Exception e) {
      handleLoadingError(e);
    }
    if (assetsLoaded) {
      if (!failedSounds.isEmpty() && !retryingFailedAssets) {
        retryingFailedAssets = true;
        System.out.println("Retrying " + failedSounds.size() + " sound file(s) as Music...");
        for (String soundPath : failedSounds) {
          try {
            game.assetManager.load(soundPath, Music.class);
          } catch (Exception e) {
            System.err.println("Failed to load " + soundPath + " even as Music: " + e.getMessage());
          }
        }
        failedSounds.clear();
      } else {
        game.setScreen(new CharacterSelectionScreen(game));
      }
    }
  }

  private void handleLoadingError(Exception e) {
    String message = e.getMessage();
    if (message != null && message.contains("asset:")) {
      int assetIndex = message.indexOf("asset:");
      if (assetIndex != -1) {
        String assetPath = message.substring(assetIndex + 7).trim();
        if (assetPath.contains("\n")) {
          assetPath = assetPath.substring(0, assetPath.indexOf("\n")).trim();
        }
        System.err.println("Asset loading error: " + assetPath);
        try {
          game.assetManager.unload(assetPath);
        } catch (Exception ignored) {
        }
        if (!failedSounds.contains(assetPath)) {
          failedSounds.add(assetPath);
        }
      }
    } else {
      System.err.println("Asset loading error: " + e.getMessage());
    }
  }

  private void loadAllAssetsRecursive(String folderPath) {
    FileHandle folder = Gdx.files.internal(folderPath);
    if (!folder.exists() || !folder.isDirectory()) return;
    for (FileHandle file : folder.list()) {
      if (file.isDirectory()) {
        loadAllAssetsRecursive(file.path());
      } else {
        String name = file.name().toLowerCase();
        if (name.endsWith(".wav")) {
          if (isValidWav(file)) {
            game.assetManager.load(file.path(), Sound.class);
          } else {
            game.assetManager.load(file.path(), Music.class);
          }
        } else if (name.endsWith(".mp3") || name.endsWith(".ogg")) {
          game.assetManager.load(file.path(), Music.class);
        }
      }
    }
  }

  private boolean isValidWav(FileHandle file) {
    try (InputStream is = file.read()) {
      byte[] header = new byte[44];
      int read = is.read(header);
      if (read < 12) return false;
      boolean hasRiffWave =
          header[0] == 'R'
              && header[1] == 'I'
              && header[2] == 'F'
              && header[3] == 'F'
              && header[8] == 'W'
              && header[9] == 'A'
              && header[10] == 'V'
              && header[11] == 'E';
      if (!hasRiffWave) return false;
      long declaredSize =
          ((long) (header[7] & 0xFF) << 24)
              | ((long) (header[6] & 0xFF) << 16)
              | ((long) (header[5] & 0xFF) << 8)
              | (header[4] & 0xFF);
      long actualSize = file.length();
      if (declaredSize > 0 && Math.abs((declaredSize + 8) - actualSize) > 100) {
        System.out.println(
            "Suspicious WAV file (size mismatch): "
                + file.name()
                + " (declared="
                + declaredSize
                + ", actual="
                + actualSize
                + ")");
        return false;
      }
      if (actualSize < 44) {
        System.out.println("WAV file too small: " + file.name());
        return false;
      }
      return true;
    } catch (Throwable t) {
      System.err.println("Error while validating WAV file " + file.name() + ": " + t.getMessage());
      return false;
    }
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }

  @Override
  public void show() {
    if (!cursorApplied) {
      initGameCursor();
    }
  }

  @Override
  public void hide() {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void dispose() {}
}
