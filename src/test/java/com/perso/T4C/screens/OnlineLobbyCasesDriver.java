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
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;

/**
 * Developer driver for the rendered LOBBY error cases of milestone M1 (T4C-0324; test scope, not
 * shipped, not a unit test). It is the real launcher game ({@link MyGame}) and the real
 * {@link OnlineLobbyScreen} against a running {@code ServerMain --online}; keys are delivered to the
 * screen's own input processor, nothing is injected into the model. One process per case.
 *
 * <p>Scenes: {@code plain} (load the lobby and look: invalid key, empty roster, incompatible
 * version), {@code roster} (pagination, availability labels, selection alone, unavailable
 * characters, then enter the game with the ready character and hold), {@code inuse} (a second
 * window of the same account once {@code a.in-game} exists), {@code stale} (the roster is read, the
 * operator moves the character to another account and creates {@code stale.moved}, then Enter).
 *
 * <p>Arguments: endpoint, profile file, output directory, tag, scene, sync directory, window x, y.
 */
public final class OnlineLobbyCasesDriver extends MyGame {
  private final Path out;
  private final Path sync;
  private final String tag;
  private final String scene;
  private final List<BooleanSupplier> steps = new ArrayList<>();
  private int index;
  private int shots;
  private final long t0 = System.currentTimeMillis();
  private OnlineLobbyScreen before;

  OnlineLobbyCasesDriver(URI endpoint, String key, OnlineLobbyModel model, Path out, String tag, String scene, Path sync) {
    super(endpoint, key, model);
    this.out = out;
    this.tag = tag;
    this.scene = scene;
    this.sync = sync;
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
    var profile = OnlineAccountProfile.load(Path.of(args[1]));
    Path out = Path.of(args[2]).toAbsolutePath();
    Files.createDirectories(out);
    Path sync = Path.of(args[5]).toAbsolutePath();
    Files.createDirectories(sync);
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("T4C Reborn - lobby cases " + args[3]);
    config.setWindowedMode(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
    config.setWindowPosition(Integer.parseInt(args[6]), Integer.parseInt(args[7]));
    config.setResizable(false);
    config.useVsync(true);
    config.disableAudio(true);
    new Lwjgl3Application(
        new OnlineLobbyCasesDriver(endpoint, profile.secret(), profile.lobbyModel(), out, args[3], args[4], sync), config);
  }

  private void log(String text) {
    System.out.println(String.format("[%6.1fs %s] %s", (System.currentTimeMillis() - t0) / 1000.0, tag, text));
  }

  private void shot(String name) {
    String file = tag + "-" + String.format("%02d", ++shots) + "-" + name + ".png";
    int w = Gdx.graphics.getBackBufferWidth();
    int h = Gdx.graphics.getBackBufferHeight();
    byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
    for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 255;
    Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
    ByteBuffer target = pixmap.getPixels();
    BufferUtils.copy(pixels, 0, target, pixels.length);
    // Half size keeps the evidence small; the lobby text stays readable.
    Pixmap half = new Pixmap(w / 2, h / 2, Pixmap.Format.RGBA8888);
    half.drawPixmap(pixmap, 0, 0, w, h, 0, 0, w / 2, h / 2);
    PixmapIO.writePNG(new com.badlogic.gdx.files.FileHandle(out.resolve(file).toFile()), half);
    half.dispose();
    pixmap.dispose();
    log("saved " + file);
  }

  private void signal(String name) {
    try {
      Files.writeString(sync.resolve(name), String.valueOf(System.currentTimeMillis()));
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private boolean signaled(String name) {
    return Files.exists(sync.resolve(name));
  }

  private static Object field(Object target, Class<?> owner, String name) {
    try {
      Field f = owner.getDeclaredField(name);
      f.setAccessible(true);
      return f.get(target);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  private OnlineLobbyModel model() {
    return (OnlineLobbyModel) field(this, MyGame.class, "onlineLobbyModel");
  }

  private OnlineConnection connection() {
    return (OnlineConnection) field(this, MyGame.class, "onlineConnection");
  }

  private OnlineLobbyScreen lobby() {
    return getScreen() instanceof OnlineLobbyScreen s ? s : null;
  }

  private void key(int code) {
    // Enter hands the window to another screen, which replaces the input processor: release only if one is left.
    Gdx.input.getInputProcessor().keyDown(code);
    var processor = Gdx.input.getInputProcessor();
    if (processor != null) processor.keyUp(code);
  }

  private void action(Runnable r) {
    steps.add(() -> {
      r.run();
      return true;
    });
  }

  private void waitMs(long ms) {
    final long[] start = {-1};
    steps.add(() -> {
      long now = System.currentTimeMillis();
      if (start[0] < 0) start[0] = now;
      return now - start[0] >= ms;
    });
  }

  private void until(String what, long timeoutMs, BooleanSupplier condition) {
    final long[] start = {-1};
    steps.add(() -> {
      long now = System.currentTimeMillis();
      if (start[0] < 0) start[0] = now;
      if (condition.getAsBoolean()) {
        log("OK: " + what + " after " + (now - start[0]) + " ms");
        return true;
      }
      if (now - start[0] > timeoutMs) {
        log("TIMEOUT: " + what + " after " + timeoutMs + " ms");
        return true;
      }
      return false;
    });
  }

  /** The lobby is up and its roster read has finished (successfully or not). */
  private void lobbyLoaded() {
    until("lobby shown", 60_000, () -> lobby() != null);
    until("roster read finished", 60_000, () -> lobby() != null && !lobby().loading());
    waitMs(1500);
  }

  private void report(String what) {
    action(() -> {
      var m = model();
      var counts = new TreeMap<String, Integer>();
      m.entries().forEach(c -> counts.merge(String.valueOf(c.availability()), 1, Integer::sum));
      log(what + ": entries=" + m.entries().size() + " truncated=" + m.truncated() + " availability=" + counts
          + " selected=" + m.selected().map(c -> c.name() + "/" + c.availability()).orElse("none")
          + " errorMessage=[" + lobby().errorMessage() + "] connectionCreated=" + (connection() != null));
    });
  }

  /** Presses Enter on the lobby and waits for either the game or a fresh lobby (the handoff returned). */
  private void enterAndExpectRefusal(String what) {
    action(() -> {
      before = lobby();
      key(Input.Keys.ENTER);
      log(what + ": Enter delivered");
    });
    until(what + ": back in a fresh lobby with a message", 60_000,
        () -> lobby() != null && lobby() != before && !lobby().loading() && lobby().errorMessage() != null);
    waitMs(800);
    report(what);
    action(() -> shot(what.replace(' ', '-')));
  }

  private void script() {
    switch (scene) {
      case "plain" -> {
        lobbyLoaded();
        report("lobby");
        action(() -> shot("lobby"));
        // Enter must do nothing when there is nothing to enter, and no connection may appear.
        action(() -> key(Input.Keys.ENTER));
        waitMs(4000);
        report("after Enter");
        action(() -> shot("after-enter"));
      }
      case "roster" -> {
        lobbyLoaded();
        report("lobby");
        action(() -> shot("page-1"));
        // Scroll to the end with the screen's own Down key (more than one page of rows).
        action(() -> {
          for (int i = 0; i < model().entries().size() + 2; i++) key(Input.Keys.DOWN);
        });
        waitMs(800);
        action(() -> shot("page-last"));
        action(() -> {
          for (int i = 0; i < model().entries().size() + 2; i++) key(Input.Keys.UP);
        });
        // Selection alone: sit on the roster for 8 s; the operator checks the database for any lease.
        waitMs(8000);
        action(() -> signal(tag + ".selection-only"));
        until("operator checked the database", 120_000, () -> signaled("proceed"));
        // Unavailable characters: select each by name and press Enter.
        for (String name : new String[] {"Lobby Remote", "Lobby NoRespawn"}) {
          action(() -> {
            var target = model().entries().stream().filter(c -> c.name().equals(name)).findFirst().orElseThrow();
            model().select(target.id());
            log("selected " + name + " availability " + target.availability());
          });
          enterAndExpectRefusal(name.toLowerCase().replace(' ', '-'));
        }
        // The ready character enters the game and holds it for the second window.
        action(() -> {
          var target = model().entries().stream().filter(c -> c.name().equals("Lobby Alpha")).findFirst().orElseThrow();
          model().select(target.id());
          before = lobby();
          key(Input.Keys.ENTER);
          log("Lobby Alpha: Enter delivered");
        });
        until("in the game", 60_000, () -> getScreen() instanceof MainGameScreen
            && connection() != null && "connected".equals(connection().status()));
        waitMs(3000);
        action(() -> {
          shot("in-game");
          signal("a.in-game");
        });
        until("second window finished", 180_000, () -> signaled("b.done"));
      }
      case "inuse" -> {
        until("first window in the game", 180_000, () -> signaled("a.in-game"));
        lobbyLoaded();
        report("lobby while in use");
        action(() -> shot("lobby-in-use"));
        enterAndExpectRefusal("in use");
        action(() -> signal("b.done"));
      }
      case "stale" -> {
        lobbyLoaded();
        report("lobby before the move");
        action(() -> {
          shot("lobby-before");
          signal("stale.ready");
        });
        until("operator moved the character", 120_000, () -> signaled("stale.moved"));
        enterAndExpectRefusal("foreign");
      }
      case "tls" -> {
        // The roster is read and the join made over the endpoint the operator passed (https with a trusted certificate).
        lobbyLoaded();
        report("lobby over the endpoint");
        action(() -> shot("lobby"));
        action(() -> key(Input.Keys.ENTER));
        until("in the game", 60_000, () -> getScreen() instanceof MainGameScreen
            && connection() != null && "connected".equals(connection().status()));
        waitMs(2500);
        action(() -> {
          var v = connection().view();
          log("in game: " + v.character().name() + " status " + connection().status());
          shot("in-game");
        });
      }
      case "create" -> {
        lobbyLoaded();
        report("empty account");
        action(() -> key(Input.Keys.C));
        waitMs(800);
        action(() -> {
          for (char c : "Lobby Cr".toCharArray()) Gdx.input.getInputProcessor().keyTyped(c);
          key(Input.Keys.DOWN);
        });
        waitMs(800);
        action(() -> shot("create-form"));
        action(() -> {
          before = lobby();
          key(Input.Keys.ENTER);
          log("create: Enter delivered");
        });
        until("created and listed", 30_000, () -> lobby() != null && !lobby().loading() && model().entries().size() == 1);
        waitMs(1200);
        report("after creation");
        action(() -> {
          shot("created");
          signal("created.listed");
        });
        until("operator checked the stored kit", 60_000, () -> signaled("created.checked"));
        action(() -> key(Input.Keys.ENTER));
        until("in the game", 60_000, () -> getScreen() instanceof MainGameScreen
            && connection() != null && "connected".equals(connection().status()));
        waitMs(2500);
        action(() -> {
          var v = connection().view();
          log("in game: " + v.character().name() + " class " + v.character().progress().characterClass() + " inventory stacks "
              + v.character().progress().inventory().size() + " gold " + v.character().gold());
          shot("created-in-game");
        });
      }
      default -> throw new IllegalArgumentException("unknown scene " + scene);
    }
    waitMs(1500);
    action(() -> {
      log("script finished");
      Gdx.app.exit();
    });
  }

  @Override
  public void create() {
    super.create();
    script();
  }

  @Override
  public void render() {
    super.render();
    if (System.currentTimeMillis() > t0 + 600_000) {
      log("DRIVER TIMEOUT at step " + index);
      Gdx.app.exit();
      return;
    }
    if (index < steps.size() && steps.get(index).getAsBoolean()) index++;
  }
}
