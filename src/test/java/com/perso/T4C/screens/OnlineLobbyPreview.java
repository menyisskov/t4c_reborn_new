package com.perso.T4C.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.online.OnlineConnection.Credentials;
import com.perso.T4C.online.OnlineLobbyModel;
import com.perso.T4C.online.model.OnlineRoster;
import com.perso.T4C.online.model.OnlineRoster.CharacterSummary;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Developer preview, not shipped: opens a LibGDX window with a fake in-memory roster (no network, no
 * server, no ports), renders {@link OnlineLobbyScreen} and saves PNG screenshots.
 *
 * <p>Run from the repository root: {@code java -cp ... com.perso.T4C.screens.OnlineLobbyPreview
 * <output-directory>}.
 */
public final class OnlineLobbyPreview extends ApplicationAdapter {
  private static final String KEY = "a".repeat(64);
  private static final long NOW = 1_000_000L;
  private static final int WIDTH = 1280;
  private static final int HEIGHT = 720;

  private final Path outputDir;
  private SpriteBatch batch;
  private OnlineLobbyScreen screen;
  private OnlineLobbyModel model;
  private int frame;
  private int stage;

  OnlineLobbyPreview(Path outputDir) {
    this.outputDir = outputDir;
  }

  public static void main(String[] args) {
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Online lobby preview");
    config.setWindowedMode(WIDTH, HEIGHT);
    config.setResizable(false);
    config.disableAudio(true);
    config.useVsync(true);
    new Lwjgl3Application(new OnlineLobbyPreview(Path.of(args.length > 0 ? args[0] : ".")), config);
  }

  private static UUID id(long n) {
    return new UUID(0L, n);
  }

  private static OnlineLobbyModel.PageSource fakeRoster() {
    var roster =
        new OnlineRoster(
            List.of(
                new CharacterSummary(id(1), "Aldric", "Warrior", "male", 5, 0, "ready"),
                new CharacterSummary(id(2), "Seraphine", "Paladin", "female", 3, 0, "in_use"),
                new CharacterSummary(id(3), "Merrow", "Mage", "male", 1, 0, "ready")),
            null);
    return after -> roster;
  }

  @Override
  public void create() {
    try {
      SpriteLoader.getInstance().loadSpriteBin(Paths.SPRITE_BIN);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    batch = new SpriteBatch();
    model = new OnlineLobbyModel(KEY, id(1));
    screen = open(fakeRoster());
  }

  private OnlineLobbyScreen open(OnlineLobbyModel.PageSource source) {
    var created =
        new OnlineLobbyScreen(
            batch,
            model,
            source,
            null,
            new OnlineLobbyScreen.Listener() {
              @Override
              public void play(Credentials credentials) {}

              @Override
              public void back() {}
            },
            () -> NOW,
            null);
    created.show();
    created.resize(WIDTH, HEIGHT);
    created.pinPointerToPanel(-200f, -200f);
    return created;
  }

  @Override
  public void render() {
    screen.render(Gdx.graphics.getDeltaTime());
    frame++;
    if (frame < 20 || !screen.loadedOnce() && !screen.loading() && stage != 3) {
      if (frame > 200) fail("roster never loaded: " + screen.errorMessage());
      return;
    }
    if (frame % 10 != 0) return;
    switch (stage) {
      case 0 -> {
        save("online-lobby-preview.png");
        screen.pinPointerToPanel(120f, OnlineLobbyLayout.ROW_Y + OnlineLobbyLayout.ROW_PITCH + 6f);
        model.finishReturnFromGame(NOW + 7_000L);
        stage = 1;
      }
      case 1 -> {
        save("online-lobby-preview-lease-hold.png");
        model.beginReturnFromGame();
        screen.pinPointerToPanel(
            OnlineLobbyLayout.BUTTON_X + 10f, OnlineLobbyLayout.REFRESH_Y + 6f);
        stage = 2;
      }
      case 2 -> {
        save("online-lobby-preview-leave-pending.png");
        screen.dispose();
        screen = open(after -> { throw new IOException("preview: no server"); });
        stage = 3;
      }
      case 3 -> {
        if (screen.errorMessage() == null) return;
        save("online-lobby-preview-error.png");
        Gdx.app.exit();
      }
      default -> {}
    }
  }

  private void fail(String message) {
    System.err.println("PREVIEW FAILED: " + message);
    Gdx.app.exit();
  }

  private void save(String name) {
    int w = Gdx.graphics.getBackBufferWidth();
    int h = Gdx.graphics.getBackBufferHeight();
    byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
    for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 255;
    Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
    ByteBuffer target = pixmap.getPixels();
    BufferUtils.copy(pixels, 0, target, pixels.length);
    PixmapIO.writePNG(new com.badlogic.gdx.files.FileHandle(outputDir.resolve(name).toFile()), pixmap);
    pixmap.dispose();
    System.out.println("saved " + outputDir.resolve(name));
  }

  @Override
  public void dispose() {
    if (screen != null) screen.dispose();
    if (batch != null) batch.dispose();
  }
}
