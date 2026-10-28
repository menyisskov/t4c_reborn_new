package com.perso.T4C.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

/**
 * Developer preview, not shipped: renders the online lobby's empty-roster state and the native
 * creation form against a fake in-memory adapter (no network, no server, no ports) and saves PNGs.
 *
 * <p>Run from the repository root: {@code java -cp ... com.perso.T4C.screens.OnlineCreatePreview
 * <output-directory>}.
 */
public final class OnlineCreatePreview extends ApplicationAdapter {
  private static final String KEY = "a".repeat(64);
  private static final int WIDTH = 1280;
  private static final int HEIGHT = 720;

  private final Path outputDir;
  private SpriteBatch batch;
  private OnlineLobbyScreen screen;
  private int frame;
  private int stage;
  private int stageFrame;

  OnlineCreatePreview(Path outputDir) {
    this.outputDir = outputDir;
  }

  public static void main(String[] args) {
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Online create-character preview");
    config.setWindowedMode(WIDTH, HEIGHT);
    config.setResizable(false);
    config.disableAudio(true);
    config.useVsync(true);
    new Lwjgl3Application(new OnlineCreatePreview(Path.of(args.length > 0 ? args[0] : ".")), config);
  }

  @Override
  public void create() {
    try {
      SpriteLoader.getInstance().loadSpriteBin(Paths.SPRITE_BIN);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    batch = new SpriteBatch();
    screen =
        new OnlineLobbyScreen(
            batch,
            new OnlineLobbyModel(KEY, null),
            after -> new OnlineRoster(List.of(), null),
            request -> {
              throw new IOException("preview: no server");
            },
            new OnlineLobbyScreen.Listener() {
              @Override
              public void play(Credentials credentials) {}

              @Override
              public void back() {}
            },
            () -> 1_000_000L,
            null);
    screen.show();
    screen.resize(WIDTH, HEIGHT);
    screen.pinPointerToPanel(-200f, -200f);
  }

  @Override
  public void render() {
    screen.render(Gdx.graphics.getDeltaTime());
    frame++;
    stageFrame++;
    if (frame > 600) {
      System.err.println("PREVIEW FAILED: stage " + stage);
      Gdx.app.exit();
      return;
    }
    if (stageFrame < 15) return;
    switch (stage) {
      case 0 -> {
        if (!screen.loadedOnce()) return;
        save("online-create-1-empty-lobby.png");
        screen.keyDown(Input.Keys.C);
        for (char c : "Brynn".toCharArray()) screen.keyTyped(c);
        screen.keyDown(Input.Keys.DOWN);
        screen.keyDown(Input.Keys.RIGHT);
        screen.pinPointerToPanel(-200f, -200f);
        next();
      }
      case 1 -> {
        save("online-create-2-form.png");
        screen.keyDown(Input.Keys.ENTER);
        next();
      }
      case 2 -> {
        if (screen.createError() == null) return;
        save("online-create-3-error-unreachable.png");
        for (int i = 0; i < 5; i++) screen.keyDown(Input.Keys.BACKSPACE);
        screen.keyTyped('X');
        screen.keyDown(Input.Keys.ENTER);
        next();
      }
      case 3 -> {
        save("online-create-4-error-name.png");
        Gdx.app.exit();
      }
      default -> {}
    }
  }

  private void next() {
    stage++;
    stageFrame = 0;
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
