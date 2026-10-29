package com.perso.T4C.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.perso.T4C.MyGame;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.online.OnlineAccountProfile;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.OnlineLobbyModel;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Developer driver for the first LIVE rendered online run (T4C-0297; not shipped, not a test).
 * It is the real launcher game ({@link MyGame}: the lobby, then the real {@link MainGameScreen}
 * over a remote session) against a running server, plus a script that plays a human:
 *
 * <ul>
 *   <li>the lobby's Enter key is delivered to the lobby's own input processor;
 *   <li>held movement keys are fed to the game through a wrapper around {@code Gdx.input}
 *       ({@code isKeyPressed}), because the game polls keys; the screen's own handlers run
 *       unchanged;
 *   <li>a second player is a headless {@link OnlineConnection} adapter (player B) that walks a few
 *       steps next to the rendered player;
 *   <li>screenshots are the real back buffer.
 * </ul>
 *
 * Arguments: endpoint, profile A (rendered), profile B (adapter), output directory.
 */
public final class OnlineLiveRunDriver extends MyGame {
  private final Path out;
  private final OnlineConnection.Credentials adapterCredentials;
  private final URI endpoint;
  private final Set<Integer> held = new HashSet<>();
  private OnlineConnection adapter;
  private long t0;
  private long lobbySeen;
  private int step;
  private long stepAt;
  private final long deadline = System.currentTimeMillis() + 240_000;

  OnlineLiveRunDriver(
      URI endpoint,
      String key,
      OnlineLobbyModel model,
      OnlineConnection.Credentials adapter,
      Path out) {
    super(endpoint, key, model);
    this.endpoint = endpoint;
    this.adapterCredentials = adapter;
    this.out = out;
  }

  public static void main(String[] args) {
    try {
      run(args);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static void run(String[] args) throws Exception {
    var configure = Class.forName("com.perso.T4C.online.OnlineNativeLibrary").getDeclaredMethod("configure");
    configure.setAccessible(true);
    configure.invoke(null);
    URI endpoint = URI.create(args[0]);
    var a = OnlineAccountProfile.load(Path.of(args[1]));
    var b = OnlineAccountProfile.load(Path.of(args[2]));
    Path out = Path.of(args[3]).toAbsolutePath();
    Files.createDirectories(out);
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("T4C Reborn - live online run");
    config.setWindowedMode(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
    config.setResizable(false);
    config.useVsync(true);
    config.disableAudio(true);
    new Lwjgl3Application(
        new OnlineLiveRunDriver(endpoint, a.secret(), a.lobbyModel(), b.directCredentials(), out),
        config);
  }

  @Override
  public void create() {
    super.create();
    t0 = System.currentTimeMillis();
    Input real = Gdx.input;
    Gdx.input =
        (Input)
            Proxy.newProxyInstance(
                Input.class.getClassLoader(),
                new Class<?>[] {Input.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("isKeyPressed") && held.contains((Integer) args[0]))
                    return true;
                  try {
                    return method.invoke(real, args);
                  } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                  }
                });
  }

  private void shot(String name) {
    int w = Gdx.graphics.getBackBufferWidth();
    int h = Gdx.graphics.getBackBufferHeight();
    byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
    for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 255;
    Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
    ByteBuffer target = pixmap.getPixels();
    BufferUtils.copy(pixels, 0, target, pixels.length);
    PixmapIO.writePNG(new com.badlogic.gdx.files.FileHandle(out.resolve(name).toFile()), pixmap);
    pixmap.dispose();
    System.out.println("saved " + out.resolve(name));
  }

  private void next() {
    step++;
    stepAt = System.currentTimeMillis();
  }

  private long since() {
    return System.currentTimeMillis() - stepAt;
  }

  @Override
  public void render() {
    super.render();
    long now = System.currentTimeMillis();
    if (now > deadline) {
      System.out.println("DRIVER TIMEOUT at step " + step);
      Gdx.app.exit();
      return;
    }
    var screen = getScreen();
    switch (step) {
      case 0 -> { // wait for the lobby roster, then play
        if (screen instanceof OnlineLobbyScreen) {
          if (lobbySeen == 0) lobbySeen = now;
          if (now - lobbySeen > 4000) {
            shot("live-1-lobby.png");
            adapter = new OnlineConnection(endpoint, adapterCredentials);
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
            System.out.println("lobby: Enter delivered after " + (now - t0) + " ms");
            next();
          }
        }
      }
      case 1 -> { // the real game screen has been built
        if (screen instanceof MainGameScreen) {
          System.out.println("game screen up after " + (now - t0) + " ms");
          next();
        } else if (since() > 60_000) {
          System.out.println("never reached the game screen: " + screen);
          Gdx.app.exit();
        }
      }
      case 2 -> {
        if (since() > 4000) {
          shot("live-2-temple-hud.png");
          held.add(Input.Keys.D);
          adapter.direction(-1, 0);
          next();
        }
      }
      case 3 -> {
        if (since() > 900) {
          shot("live-3-own-character-moving.png");
          next();
        }
      }
      case 4 -> {
        if (since() > 1100) {
          held.clear();
          adapter.stopInput();
          next();
        }
      }
      case 5 -> {
        if (since() > 2500) {
          shot("live-4-after-move-other-player.png");
          System.out.println("adapter status=" + adapter.status() + " view=" + (adapter.view() != null));
          try {
            Method m = MainGameScreen.class.getDeclaredMethod("switchCharacter");
            m.setAccessible(true);
            m.invoke(screen);
          } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
          }
          next();
        }
      }
      case 6 -> {
        if (screen instanceof OnlineLobbyScreen && since() > 3000) {
          shot("live-5-back-in-lobby.png");
          next();
        } else if (since() > 20000) {
          System.out.println("did not return to the lobby: " + screen);
          Gdx.app.exit();
        }
      }
      default -> {
        if (adapter != null) adapter.close();
        Gdx.app.exit();
      }
    }
  }

  @Override
  public void dispose() {
    if (adapter != null) adapter.close();
    super.dispose();
  }
}
