package com.perso.T4C.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.perso.T4C.MyGame;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.objects.GroundItem;
import com.perso.T4C.online.OnlineAccountProfile;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.OnlineLobbyModel;
import com.perso.T4C.online.model.CharacterProgress;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Developer driver for LIVE rendered online runs (T4C-0297, extended by T4C-0306; test scope, not
 * shipped, not a unit test). It is the real launcher game ({@link MyGame}: the lobby, then the real
 * {@link MainGameScreen} over a remote session) against a running server, plus a script that plays
 * a human. Run ONE process per player, each with its own window and its own account profile.
 *
 * <p>What is scripted, and how it reaches the game:
 *
 * <ul>
 *   <li>lobby selection: the Enter key delivered to the lobby screen's own input processor;
 *   <li>walking: held movement keys fed through a wrapper around {@code Gdx.input.isKeyPressed}
 *       (the game polls keys, so the screen's own movement handler runs on scripted key state);
 *   <li>attacking, talking to an NPC, picking up loot: a left {@code touchDown}/{@code touchUp}
 *       delivered to the screen's own input processor at the pixel where the target is drawn, so
 *       the screen's own click handlers ({@code MonsterInputHandler}, the NPC click handler,
 *       {@code GroundItemClickHandler}) run unchanged and send the intent to the server;
 *   <li>after a server restart: the {@code R} key (the screen's own disconnect-to-lobby shortcut),
 *       then Enter in the lobby again;
 *   <li>screenshots are the real back buffer.
 * </ul>
 *
 * Since T4C-0312 the inventory ({@code I}), spell ({@code C}) and potion ({@code U}) lists, the digit
 * keys, {@code Tab} and {@code T} of the shop panel are delivered the same way (key events to the
 * screen's input processor), and the committed state is read from the connection's view. The one
 * thing sent around the screen is the quest offer (topic 0), because the screen has no topic
 * control; it goes through the same connection object and is logged as such.
 *
 * <p>Not scripted (a real mouse or OS key event, the Options button, character creation).
 * Two processes coordinate through marker files in a shared directory; the orchestrating operator
 * creates {@code server-restarted} (or {@code server-restarted-<cycle>}) after restarting the server.
 *
 * <p>Arguments: endpoint, profile file, output directory, tag (for file names), scene
 * ({@code temple}, {@code skeletons}, {@code wares}, {@code quest} or the solo {@code turnin}), sync
 * directory, window x, window y.
 */
public final class OnlineLiveRunDriver extends MyGame {
  private final OnlineLobbyModel lobbyModel;
  private final Path out;
  private final Path sync;
  private final String tag;
  private final String scene;
  private final Set<Integer> held = new HashSet<>();
  private final List<Step> steps = new ArrayList<>();
  private int index;
  private long stepStart = -1;
  private final long t0 = System.currentTimeMillis();
  private final long deadline = t0 + 900_000;

  // Remembered between steps.
  private CharacterProgress before;
  private Vector2 positionBeforeRestart;
  private int playerCountSeen;
  private long lastClick;
  private long lastMessageLog;
  private boolean daylight;
  private long lastDaylight;
  private int attacksSent;
  private int shots;
  private volatile int hoverX = -1, hoverY = -1;
  /** Tile the beta scene walks to for the Skeleton fights (operator argument); MIN_VALUE when unknown. */
  private int goalTx = Integer.MIN_VALUE, goalTy;

  private interface Step {
    /** @return true when the step has finished. */
    boolean tick();
  }

  OnlineLiveRunDriver(
      URI endpoint,
      String key,
      OnlineLobbyModel model,
      Path out,
      String tag,
      String scene,
      Path sync) {
    super(endpoint, key, model);
    this.lobbyModel = model;
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
    config.setTitle("T4C Reborn - live online run " + args[3]);
    config.setWindowedMode(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
    config.setWindowPosition(Integer.parseInt(args[6]), Integer.parseInt(args[7]));
    config.setResizable(false);
    config.useVsync(true);
    config.disableAudio(true);
    var driver = new OnlineLiveRunDriver(endpoint, profile.secret(), profile.lobbyModel(), out, args[3], args[4], sync);
    if (args.length > 9) {
      driver.goalTx = Integer.parseInt(args[8]);
      driver.goalTy = Integer.parseInt(args[9]);
    }
    new Lwjgl3Application(driver, config);
  }

  // ---------------------------------------------------------------- plumbing

  private void log(String text) {
    System.out.println(String.format("[%6.1fs %s] %s", (System.currentTimeMillis() - t0) / 1000.0, tag, text));
  }

  @Override
  public void create() {
    super.create();
    Input real = Gdx.input;
    Gdx.input =
        (Input)
            Proxy.newProxyInstance(
                Input.class.getClassLoader(),
                new Class<?>[] {Input.class},
                (proxy, method, a) -> {
                  if (method.getName().equals("isKeyPressed") && held.contains((Integer) a[0])) return true;
                  // T4C-0316: a pointer position injected for the screen's own hover code (outline and name of another
                  // player); everything else about the pointer stays the real one.
                  if (hoverX >= 0 && method.getName().equals("getX") && (a == null || a.length <= 1)) return hoverX;
                  if (hoverX >= 0 && method.getName().equals("getY") && (a == null || a.length <= 1)) return hoverY;
                  try {
                    return method.invoke(real, a);
                  } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                  }
                });
    script();
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
    PixmapIO.writePNG(new com.badlogic.gdx.files.FileHandle(out.resolve(file).toFile()), pixmap);
    pixmap.dispose();
    log("saved " + file);
  }

  /**
   * A small picture of just the HUD: the message log (bottom left, 450x200) beside the bottom-right block where the
   * online lists and the shop are drawn (440x368). Same real back buffer, a fraction of the file size.
   */
  private void hud(String name) {
    String file = tag + "-" + String.format("%02d", ++shots) + "-" + name + ".png";
    int w = Gdx.graphics.getBackBufferWidth();
    int h = Gdx.graphics.getBackBufferHeight();
    byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
    for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 255;
    Pixmap full = new Pixmap(w, h, Pixmap.Format.RGBA8888);
    BufferUtils.copy(pixels, 0, full.getPixels(), pixels.length);
    Pixmap crop = new Pixmap(890, 368, Pixmap.Format.RGBA8888);
    crop.drawPixmap(full, 0, 168, 0, h - 200, 450, 200);
    crop.drawPixmap(full, 450, 0, w - 440, h - 368, 440, 368);
    PixmapIO.writePNG(new com.badlogic.gdx.files.FileHandle(out.resolve(file).toFile()), crop);
    crop.dispose();
    full.dispose();
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

  private MainGameScreen game() {
    return getScreen() instanceof MainGameScreen s ? s : null;
  }

  private OnlineConnection connection() {
    return (OnlineConnection) field(this, MyGame.class, "onlineConnection");
  }

  private OnlineConnection.View view() {
    var c = connection();
    return c == null ? null : c.view();
  }

  private OrthographicCamera camera(MainGameScreen s) {
    return (OrthographicCamera) field(s, MainGameScreen.class, "camera");
  }

  private com.perso.T4C.player.Player me(MainGameScreen s) {
    return (com.perso.T4C.player.Player) field(s, MainGameScreen.class, "player");
  }

  @SuppressWarnings("unchecked")
  private List<BaseMonster> monsters(MainGameScreen s) {
    var manager = (com.perso.T4C.monster.core.MonsterManager) field(s, MainGameScreen.class, "monsterManager");
    return manager.getMonsters();
  }

  @SuppressWarnings("unchecked")
  private List<BaseNPC> npcs(MainGameScreen s) {
    var manager = field(s, MainGameScreen.class, "npcManager");
    return (List<BaseNPC>) field(manager, manager.getClass(), "npcs");
  }

  @SuppressWarnings("unchecked")
  private List<GroundItem> groundItems(MainGameScreen s) {
    var manager = field(s, MainGameScreen.class, "groundItemManager");
    return (List<GroundItem>) field(manager, manager.getClass(), "items");
  }

  private List<com.perso.T4C.player.Player> others(MainGameScreen s) {
    var session = field(s, MainGameScreen.class, "session");
    var world = ((com.perso.T4C.session.GameSession) session).world();
    return world instanceof com.perso.T4C.session.RemoteWorldDriver r ? r.otherPlayers() : List.of();
  }

  /** Delivers a left click to the screen's own input processor at the screen pixel showing {@code world}. */
  private void click(MainGameScreen s, float worldX, float worldY) {
    var cam = camera(s);
    Vector3 p = cam.project(new Vector3(worldX, worldY, 0));
    int sx = Math.round(p.x);
    int sy = Gdx.graphics.getHeight() - Math.round(p.y) - 1;
    var processor = Gdx.input.getInputProcessor();
    processor.mouseMoved(sx, sy);
    processor.touchDown(sx, sy, 0, Input.Buttons.LEFT);
    processor.touchUp(sx, sy, 0, Input.Buttons.LEFT);
    lastClick = System.currentTimeMillis();
  }

  /** Finds a world point inside the target's click box, trying offsets around its position. */
  private Vector2 aim(Vector2 base, Predicate<Vector2> over) {
    for (int dy = -20; dy >= -120; dy -= 10)
      for (int dx = 0; dx <= 60; dx += 10)
        for (int sign = -1; sign <= 1; sign += 2) {
          Vector2 candidate = new Vector2(base.x + sign * dx, base.y + dy);
          if (over.test(candidate)) return candidate;
        }
    for (int dy = 0; dy <= 30; dy += 10) {
      Vector2 candidate = new Vector2(base.x, base.y + dy);
      if (over.test(candidate)) return candidate;
    }
    return null;
  }

  private static final int TILE_W = GameConstants.GRID_W;
  private static final int TILE_H = GameConstants.GRID_H;
  private List<int[]> route = List.of();
  private long routeAt;
  private int routeGoalX = Integer.MIN_VALUE;
  private int routeGoalY;

  /** Authored portal source tiles of the hosted route: a long walk must not cross one by accident (T4C-0316). */
  private static final Set<Long> PORTAL_TILES =
      Set.of(tileKey(2770, 1094), tileKey(2770, 1095), tileKey(2771, 1094), tileKey(2931, 1056), tileKey(2964, 1038),
          tileKey(306, 381), tileKey(136, 665));
  private static volatile long allowTile = Long.MIN_VALUE;

  private static long tileKey(int x, int y) {
    return ((long) x << 32) | (y & 0xffffffffL);
  }

  private static boolean walkable(int tx, int ty) {
    long key = tileKey(tx, ty);
    if (PORTAL_TILES.contains(key) && key != allowTile) return false;
    return !com.perso.T4C.helper.CollisionManager.getInstance().hasCollision(tx * TILE_W + TILE_W / 2f, ty * TILE_H + TILE_H / 2f);
  }

  /** Breadth-first search over free tiles (4 directions, the client's own collision map). */
  private static List<int[]> search(int sx, int sy, int gx, int gy) {
    int radius = 200;
    int size = radius * 2 + 1;
    int[][] from = new int[size * size][];
    java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
    queue.add(new int[] {sx, sy});
    from[(sy - sy + radius) * size + (sx - sx + radius)] = new int[] {sx, sy};
    int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    while (!queue.isEmpty()) {
      int[] cur = queue.poll();
      if (Math.max(Math.abs(cur[0] - gx), Math.abs(cur[1] - gy)) <= 1 && (cur[0] != gx || cur[1] != gy || walkable(gx, gy))) {
        java.util.LinkedList<int[]> path = new java.util.LinkedList<>();
        int[] at = cur;
        while (!(at[0] == sx && at[1] == sy)) {
          path.addFirst(at);
          at = from[(at[1] - sy + radius) * size + (at[0] - sx + radius)];
        }
        path.addFirst(new int[] {sx, sy});
        return path;
      }
      for (int[] d : dirs) {
        int nx = cur[0] + d[0], ny = cur[1] + d[1];
        int ix = nx - sx + radius, iy = ny - sy + radius;
        if (ix < 0 || iy < 0 || ix >= size || iy >= size || from[iy * size + ix] != null) continue;
        if (!walkable(nx, ny)) continue;
        from[iy * size + ix] = cur;
        queue.add(new int[] {nx, ny});
      }
    }
    return List.of();
  }

  /** Walks along a collision-aware route toward the goal; falls back to a straight line when none exists. */
  private void walkToward(MainGameScreen s, Vector2 goal, float stopDistance) {
    var p = me(s).getCoordinates();
    held.clear();
    if (Math.hypot(goal.x - p.getX(), goal.y - p.getY()) <= stopDistance) return;
    int ptx = (int) (p.getX() / TILE_W), pty = (int) (p.getY() / TILE_H);
    int gx = (int) (goal.x / TILE_W), gy = (int) (goal.y / TILE_H);
    long now = System.currentTimeMillis();
    if (route.isEmpty() || gx != routeGoalX || gy != routeGoalY || now - routeAt > 3000) {
      route = search(ptx, pty, gx, gy);
      routeAt = now;
      routeGoalX = gx;
      routeGoalY = gy;
    }
    Vector2 next = goal;
    int nearest = -1;
    for (int i = 0; i < route.size(); i++) if (route.get(i)[0] == ptx && route.get(i)[1] == pty) nearest = i;
    if (nearest >= 0 && nearest + 1 < route.size()) {
      int[] t = route.get(nearest + 1);
      next = new Vector2(t[0] * TILE_W + TILE_W / 2f, t[1] * TILE_H + TILE_H / 2f);
    } else if (nearest < 0 && !route.isEmpty()) {
      route = List.of(); // off the route: search again next frame
    }
    float dx = next.x - p.getX();
    float dy = next.y - p.getY();
    // One axis at a time (the larger error in tiles first): a diagonal step into a doorway corner is blocked by the
    // wall beside it (T4C-0316: the Lighthaven exit door), an axis-aligned step is not.
    boolean needX = Math.abs(dx) > 5, needY = Math.abs(dy) > 3;
    if (needX && needY) {
      if (Math.abs(dx) / TILE_W >= Math.abs(dy) / TILE_H) needY = false;
      else needX = false;
    }
    if (needX) held.add(dx > 0 ? Input.Keys.D : Input.Keys.A);
    // The world's y axis points down the screen: S increases y, W decreases it.
    if (needY) held.add(dy > 0 ? Input.Keys.S : Input.Keys.W);
  }

  private double distance(MainGameScreen s, Vector2 goal) {
    var p = me(s).getCoordinates();
    return Math.hypot(goal.x - p.getX(), goal.y - p.getY());
  }

  private String inventorySummary(CharacterProgress p) {
    StringBuilder b = new StringBuilder();
    for (var item : p.inventory()) b.append(item.key()).append('x').append(item.quantity()).append(' ');
    return b.toString().trim();
  }

  private int count(CharacterProgress p, String fragment) {
    int n = 0;
    for (var item : p.inventory()) if (item.key().toLowerCase(Locale.ROOT).contains(fragment)) n += item.quantity();
    return n;
  }

  // ---------------------------------------------------------------- step helpers

  private void add(Step step) {
    steps.add(step);
  }

  private void action(Runnable r) {
    add(
        () -> {
          r.run();
          return true;
        });
  }

  private void waitMs(long ms) {
    final long[] start = {-1};
    add(
        () -> {
          long now = System.currentTimeMillis();
          if (start[0] < 0) start[0] = now;
          return now - start[0] >= ms;
        });
  }

  /** Waits for a condition; when it times out the failure is logged and the step moves on. */
  private void until(String what, long timeoutMs, BooleanSupplier condition) {
    final long[] start = {-1};
    add(
        () -> {
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

  /** Repeats {@code body} each frame until it returns true or the time runs out. */
  private void repeat(String what, long timeoutMs, BooleanSupplier body) {
    until(what, timeoutMs, body);
  }

  // ---------------------------------------------------------------- the script

  private void script() {
    // 1. The lobby lists this account's character; Enter selects it.
    until("lobby shown", 60_000, () -> getScreen() instanceof OnlineLobbyScreen);
    if (scene.equals("create")) createInLobby();
    waitMs(4000);
    action(() -> shot("lobby-roster"));
    action(
        () -> {
          Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
          log("lobby: Enter delivered");
        });
    until("real game screen built", 60_000, () -> game() != null);
    waitMs(3000);
    action(
        () -> {
          var v = view();
          log(
              "in world: character "
                  + v.character().name()
                  + " revision "
                  + v.character().revision()
                  + " inventory "
                  + inventorySummary(v.character().progress()));
          before = v.character().progress();
          shot("in-world-default-light");
        });
    // Findings check: the default online light is very dark. Compare a daylight frame.
    action(() -> daylight = true);
    waitMs(1500);
    action(() -> shot("in-world-daylight"));
    // 2. Each rendered client sees the other.
    until(
        "sees the other rendered player",
        solo() ? 1: 45_000,
        () -> {
          var s = game();
          return s != null && !others(s).isEmpty();
        });
    action(
        () -> {
          var s = game();
          playerCountSeen = others(s).size();
          log("other players drawn: " + playerCountSeen);
          shot("sees-other-player");
          signal(tag + ".saw-other");
        });
    until("peer also saw me", solo() ? 1: 60_000, () -> signaled(peer() + ".saw-other"));
    if (scene.equals("temple")) templeScene();
    else if (scene.equals("wares")) waresScene();
    else if (scene.equals("quest")) questScene();
    else if (scene.equals("turnin")) turnInScene();
    else if (scene.equals("beta")) betaScene();
    else if (scene.equals("mortal")) mortalScene();
    else if (scene.equals("m4")) m4Scene();
    else if (scene.equals("accept")) acceptScene();
    else if (scene.equals("m5")) m5Scene();
    else if (scene.equals("create")) createScene();
    else if (scene.equals("reenter")) reenterScene();
    else skeletonScene();
    action(
        () -> {
          signal(tag + ".done");
          log("script finished");
        });
    until("peer finished", solo() ? 1: 120_000, () -> signaled(peer() + ".done"));
    action(
        () -> {
          held.clear();
          Gdx.app.exit();
        });
  }

  private String peer() {
    return tag.endsWith("A") ? tag.substring(0, tag.length() - 1) + "B" : tag.substring(0, tag.length() - 1) + "A";
  }

  private BaseNPC kilhiam(MainGameScreen s) {
    for (BaseNPC npc : npcs(s)) if (npc.getName() != null && npc.getName().toLowerCase(Locale.ROOT).contains("kilhiam")) return npc;
    return null;
  }

  private void talkOnce(String label) {
    action(
        () -> {
          var s = game();
          var npc = kilhiam(s);
          if (npc == null) {
            log("TALK " + label + ": no Kilhiam puppet in the world");
            return;
          }
          Vector2 at = aim(npc.getPosition(), p -> npc.isMouseOver(p.x, p.y));
          if (at == null) {
            log("TALK " + label + ": found no clickable point on Kilhiam");
            return;
          }
          before = view().character().progress();
          log("TALK " + label + ": click on Kilhiam at world " + at + ", distance " + Math.round(distance(s, npc.getPosition())) + " px, inventory before: " + inventorySummary(before));
          click(s, at.x, at.y);
        });
    waitMs(3500);
    action(
        () -> {
          var after = view().character().progress();
          log("TALK " + label + ": inventory after: " + inventorySummary(after) + " (letters before " + count(before, "letter") + ", after " + count(after, "letter") + ")");
          shot("talk-" + label);
        });
  }

  // ---------------------------------------------------------------- M1 creation scenes (T4C-0352)

  /**
   * Scene {@code create}, first part: the account has no character. The lobby's own Create form is driven through the
   * screen's input processor (C opens it, the name is typed as key-typed events, Down picks the class, Enter submits),
   * then the roster is read again and the single new character is selected. Name and class come from
   * {@code -Dcreate.name} and {@code -Dcreate.classIndex} (index into the form's class list).
   */
  private void createInLobby() {
    String name = System.getProperty("create.name", "Live Creator");
    int classIndex = Integer.parseInt(System.getProperty("create.classIndex", "0"));
    until("roster read finished", 60_000, () -> getScreen() instanceof OnlineLobbyScreen l && !l.loading());
    waitMs(1500);
    action(
        () -> {
          log("CREATE: empty roster, entries=" + lobbyModel.entries().size() + ", message [" + ((OnlineLobbyScreen) getScreen()).errorMessage() + "]");
          shot("create-1-empty-lobby");
          Gdx.input.getInputProcessor().keyDown(Input.Keys.C);
          Gdx.input.getInputProcessor().keyUp(Input.Keys.C);
        });
    waitMs(800);
    action(
        () -> {
          for (char c : name.toCharArray()) Gdx.input.getInputProcessor().keyTyped(c);
          for (int i = 0; i < classIndex; i++) {
            Gdx.input.getInputProcessor().keyDown(Input.Keys.DOWN);
            Gdx.input.getInputProcessor().keyUp(Input.Keys.DOWN);
          }
        });
    waitMs(800);
    action(
        () -> {
          var form = ((OnlineLobbyScreen) getScreen()).form();
          log("CREATE: form shows name [" + form.name() + "] class " + form.characterClass() + " gender " + form.gender() + " evil " + form.evil());
          shot("create-2-form");
          Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
          Gdx.input.getInputProcessor().keyUp(Input.Keys.ENTER);
          log("CREATE: Enter delivered");
        });
    until("created and listed", 40_000, () -> getScreen() instanceof OnlineLobbyScreen l && !l.loading() && lobbyModel.entries().size() == 1);
    waitMs(1200);
    action(
        () -> {
          var c = lobbyModel.entries().getFirst();
          log("CREATE: roster lists " + c.name() + " / " + c.characterClass() + " / " + c.availability());
          shot("create-3-listed");
        });
  }

  /** What the committed view says about the character, in one line (class, level, gold, stack and equipment counts). */
  private String characterLine() {
    var v = view();
    var p = v.character().progress();
    return v.character().name() + " class " + p.characterClass() + " level " + p.advancement().level() + " gold " + v.character().gold()
        + " inventoryStacks " + p.inventory().size() + " equipped " + new java.util.TreeSet<>(p.equipment().keySet())
        + " spells " + new java.util.TreeSet<>(p.spells()) + " letters " + count(p, "letter");
  }

  /** Walks to Kilhiam if needed, as the temple scene does. */
  private void walkToKilhiam() {
    repeat(
        "walked within talking distance of Kilhiam",
        60_000,
        () -> {
          var s = game();
          var npc = s == null ? null : kilhiam(s);
          if (npc == null) return false;
          walkToward(s, npc.getPosition(), 70f);
          return distance(s, npc.getPosition()) <= 70f;
        });
    action(() -> held.clear());
    waitMs(800);
  }

  /** Leaves the world through the screen's switch-character callback and enters again from the lobby. */
  private void leaveAndRejoin() {
    action(
        () -> {
          held.clear();
          log("CLOSE: " + characterLine());
          try {
            var m = MainGameScreen.class.getDeclaredMethod("switchCharacter");
            m.setAccessible(true);
            m.invoke(game());
            log("CLOSE: MainGameScreen.switchCharacter() invoked by reflection (the Options button was not clicked)");
          } catch (ReflectiveOperationException e) {
            log("CLOSE: could not invoke switchCharacter: " + e);
          }
        });
    until("back in the lobby", 30_000, () -> getScreen() instanceof OnlineLobbyScreen);
    waitMs(2000);
    action(() -> shot("create-5-lobby-after-close"));
    final long[] rejoinStart = {0};
    repeat(
        "reconnected after closing",
        150_000,
        () -> {
          long now = System.currentTimeMillis();
          if (game() != null && "connected".equals(connection().status())) return true;
          if (getScreen() instanceof OnlineLobbyScreen && now - rejoinStart[0] > 2500) {
            rejoinStart[0] = now;
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
          }
          return false;
        });
    waitMs(4000);
    action(
        () -> {
          log("RECONNECT: " + characterLine());
          shot("create-6-reconnected");
        });
  }

  private void createScene() {
    action(() -> log("ENTERED: " + characterLine()));
    walkToKilhiam();
    talkOnce("first-greeting");
    talkOnce("repeat-greeting");
    leaveAndRejoin();
    walkToKilhiam();
    talkOnce("greeting-after-reconnect");
    action(() -> log("END: " + characterLine()));
  }

  /** Second process for the same account after the first window was closed: the created character is on the roster. */
  private void reenterScene() {
    action(() -> log("REENTERED: " + characterLine()));
    walkToKilhiam();
    talkOnce("greeting-after-window-close");
    action(() -> log("END: " + characterLine()));
  }

  private void templeScene() {
    // 3. Walk to Kilhiam and talk through the screen's own NPC click handler.
    repeat(
        "walked within talking distance of Kilhiam",
        40_000,
        () -> {
          var s = game();
          var npc = s == null ? null : kilhiam(s);
          if (npc == null) return false;
          walkToward(s, npc.getPosition(), 70f);
          return distance(s, npc.getPosition()) <= 70f;
        });
    action(() -> held.clear());
    waitMs(800);
    talkOnce("first-greeting");
    talkOnce("repeat-greeting");
    restartCycle("", null);
    talkOnce("greeting-after-restart");
  }

  /**
   * Server stop and restart (the operator does it between the markers), then the screen's own return to the lobby and
   * the rejoin. {@code afterRejoin} (optional) runs once the world is back, before the final frame is saved.
   */
  private void restartCycle(Runnable afterRejoin) {
    restartCycle("", afterRejoin);
  }

  /** {@code cycle} names the markers when a scene restarts the server more than once ({@code stop-ready-<cycle>}, {@code server-restarted-<cycle>}). */
  private void restartCycle(String cycle, Runnable afterRejoin) {
    restartCycle(cycle, afterRejoin, null);
  }

  /** {@code whileDown} runs every frame until the connection shows disconnected (T4C-0353: the database-crash cycle keeps committing meanwhile). */
  private void restartCycle(String cycle, Runnable afterRejoin, Runnable whileDown) {
    String suffix = cycle.isEmpty() ? "" : "-" + cycle;
    // 4. Server stop and restart (the operator does it between these markers).
    action(
        () -> {
          var s = game();
          // After a hard kill the connection may already be gone (view null): keep the last known values then.
          if (s != null) positionBeforeRestart = new Vector2(me(s).getCoordinates().getX(), me(s).getCoordinates().getY());
          if (view() != null) before = view().character().progress();
          log("BEFORE RESTART: position " + positionBeforeRestart + " inventory " + (before == null ? "?" : inventorySummary(before)) + " status " + connection().status());
          if (s != null) shot("before-server-stop");
          signal(tag + ".stop-ready" + suffix);
        });
    long patience = scene.equals("quest") ? 1_200_000 : 180_000;
    until(
        "operator stopped the server (connection shows disconnected)",
        patience,
        () -> {
          boolean down = "disconnected".equals(connection().status());
          if (down) {
            held.clear();
            if (!signaled(tag + ".down" + suffix)) {
              signal(tag + ".down" + suffix);
              log("M5 DOWN" + suffix + ": connection shows disconnected");
            }
          } else if (whileDown != null) whileDown.run();
          return down;
        });
    waitMs(2000);
    action(() -> shot("server-stopped-disconnected-text"));
    until("operator restarted the server", patience, () -> signaled("server-restarted" + suffix));
    // 5. The screen's own return-to-lobby shortcut, then the lobby again.
    action(
        () -> {
          log("pressing R (return to lobby) with status " + connection().status());
          Gdx.input.getInputProcessor().keyDown(Input.Keys.R);
        });
    until("back in the lobby", 30_000, () -> getScreen() instanceof OnlineLobbyScreen);
    waitMs(2500);
    action(() -> shot("lobby-after-server-restart"));
    final long[] rejoinStart = {0};
    repeat(
        "rejoined the world after the restart",
        120_000,
        () -> {
          long now = System.currentTimeMillis();
          if (game() != null) return true;
          if (getScreen() instanceof OnlineLobbyScreen && now - rejoinStart[0] > 2500) {
            rejoinStart[0] = now;
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
          }
          return false;
        });
    waitMs(4000);
    action(
        () -> {
          var s = game();
          if (s == null) {
            log("AFTER RESTART: no game screen");
            return;
          }
          var p = me(s).getCoordinates();
          var after = view().character().progress();
          log("AFTER RESTART: position (" + p.getX() + "," + p.getY() + ") vs before " + positionBeforeRestart + "; inventory " + inventorySummary(after)
              + " (same as before: " + inventorySummary(after).equals(inventorySummary(before)) + "); other players drawn " + others(s).size());
          daylight = true;
          shot("rejoined-after-restart");
          if (afterRejoin != null) afterRejoin.run();
        });
  }

  private BaseMonster nearestLive(MainGameScreen s) {
    BaseMonster best = null;
    double bestDistance = Double.MAX_VALUE;
    for (BaseMonster m : monsters(s)) {
      if (m.isDead() || m.getHealth() <= 0) continue;
      if (skeletonsOnly && !isSkeleton(m)) continue;
      double d = distance(s, m.getPosition());
      if (d < bestDistance) {
        bestDistance = d;
        best = m;
      }
    }
    return best;
  }

  /** The beta scene fights Skeletons only: a harmless monster near the temple must not hold a respawned player there. */
  private boolean skeletonsOnly;

  private static boolean isSkeleton(BaseMonster m) {
    return m.getName() != null && m.getName().toLowerCase(Locale.ROOT).contains("skeleton");
  }

  private void skeletonScene() {
    final long[] fightStart = {0};
    final int[] kills = {0};
    final BaseMonster[] last = {null};
    final long[] experienceStart = {-1};
    // 3. Fight through the screen's own monster click handler until something drops.
    repeat(
        "a ground drop appeared",
        240_000,
        () -> {
          var s = game();
          if (s == null) return false;
          long now = System.currentTimeMillis();
          var v = view();
          if (experienceStart[0] < 0) {
            experienceStart[0] = v.character().experience();
            fightStart[0] = now;
          }
          if (!v.snapshot().loot().isEmpty()) {
            held.clear();
            return true;
          }
          if (v.character().progress().vitals().hp() <= 0) {
            held.clear();
            return false;
          }
          BaseMonster target = nearestLive(s);
          if (target == null) {
            held.clear();
            return false;
          }
          if (last[0] != target) {
            last[0] = target;
            log("target " + target.getName() + " hp " + target.getHealth() + "/" + target.getMaxHealth() + " at distance " + Math.round(distance(s, target.getPosition())) + " px");
          }
          double d = distance(s, target.getPosition());
          float reach = GameConstants.PLAYER_MELEE_ATTACK_RANGE - 14f;
          if (d > reach) walkToward(s, target.getPosition(), reach);
          else held.clear();
          if (d <= reach + 8 && now - lastClick > 650) {
            Vector2 at = aim(target.getPosition(), p -> target.isMouseOver(p.x, p.y));
            if (at != null) {
              click(s, at.x, at.y);
              attacksSent++;
              if (attacksSent == 1) log("first attack click delivered (monster click handler)");
              if (attacksSent == 6) shot("attacking");
            }
          }
          if (now - lastMessageLog > 15_000) {
            lastMessageLog = now;
            log("fight: hp " + v.character().progress().vitals().hp() + "/" + v.character().progress().vitals().maxHp() + " xp " + (v.character().experience() - experienceStart[0]) + " attacks " + attacksSent);
          }
          return false;
        });
    action(
        () -> {
          held.clear();
          var v = view();
          log("fight over: attacks sent " + attacksSent + ", xp gained " + (v.character().experience() - experienceStart[0]) + ", hp " + v.character().progress().vitals().hp() + ", loot drops in snapshot " + v.snapshot().loot().size());
          shot("after-fight");
        });
    // 4. Loot: walk to the nearest drop, then both clients click it at the same moment (the owner window is
    // still open: the killer may take it, the other client is refused by the server).
    repeat(
        "stood beside the first drop",
        60_000,
        () -> {
          var s = game();
          var v = view();
          if (s == null || v.snapshot().loot().isEmpty()) return false;
          var items = groundItems(s);
          if (items.isEmpty()) return false;
          GroundItem item = items.get(0);
          walkToward(s, item.getPosition(), 80f);
          return distance(s, item.getPosition()) <= 80f;
        });
    action(
        () -> {
          held.clear();
          var v = view();
          for (var loot : v.snapshot().loot())
            log("drop " + loot.key() + " owner " + (loot.ownerPlayerId() == null ? "none" : "set") + " ownerRemainingMs " + loot.ownerRemainingMs());
          shot("beside-drop");
          signal(tag + ".at-loot");
        });
    until("peer beside a drop", 90_000, () -> signaled(peer() + ".at-loot"));
    action(
        () -> {
          var s = game();
          before = view().character().progress();
          var items = groundItems(s);
          if (items.isEmpty()) {
            log("PICKUP: no drop left to click");
            return;
          }
          GroundItem item = items.get(0);
          Vector2 at = aim(item.getPosition(), p -> item.isMouseOver(p.x, p.y));
          log("PICKUP: clicking " + item.getItemName() + " at " + at + " hp " + view().character().progress().vitals().hp() + " ownerPlayerId " + (view().snapshot().loot().isEmpty() ? "-" : String.valueOf(view().snapshot().loot().get(0).ownerPlayerId())) + " inventory before: " + inventorySummary(before));
          if (at != null) click(s, at.x, at.y);
        });
    waitMs(3500);
    action(
        () -> {
          var after = view().character().progress();
          log("PICKUP: inventory after: " + inventorySummary(after) + " loot left in snapshot " + view().snapshot().loot().size());
          shot("after-pickup");
        });
  }

  // ---------------------------------------------------------------- wares scene (T4C-0312)

  private void press(int key) {
    var processor = Gdx.input.getInputProcessor();
    processor.keyDown(key);
    processor.keyUp(key);
  }

  private static int digitKey(int digit) {
    return Input.Keys.NUM_1 + digit - 1;
  }

  private com.perso.T4C.online.OnlineShopPanel shopPanel() {
    return (com.perso.T4C.online.OnlineShopPanel) field(game(), MainGameScreen.class, "remoteShopPanel");
  }

  private com.perso.T4C.online.OnlineInventoryPanel inventoryPanel() {
    return (com.perso.T4C.online.OnlineInventoryPanel) field(game(), MainGameScreen.class, "remoteInventoryPanel");
  }

  private com.perso.T4C.online.OnlineAbilityMenu abilityMenu() {
    return (com.perso.T4C.online.OnlineAbilityMenu) field(game(), MainGameScreen.class, "remoteAbilityMenu");
  }

  /** One line of committed state: revision, gold, vitals, what is worn, what is carried, buffs and cooldowns. */
  private String state() {
    if (view() == null) return "(connection gone: a fault is in progress)";
    var c = view().character();
    var p = c.progress();
    java.util.Map<java.util.UUID, String> keys = new java.util.HashMap<>();
    p.inventory().forEach(i -> keys.put(i.id(), i.key().replace("item.", "")));
    StringBuilder worn = new StringBuilder();
    new java.util.TreeMap<>(p.equipment()).forEach((slot, id) -> worn.append(slot).append('=').append(keys.getOrDefault(id, "?")).append(' '));
    return "rev " + c.revision() + " gold " + c.gold() + " hp " + p.vitals().hp() + "/" + p.vitals().maxHp()
        + " mana " + p.vitals().mana() + "/" + p.vitals().maxMana() + " | worn: " + worn.toString().trim()
        + " | carried: " + inventorySummary(p) + " | buffs " + p.buffs().stream().map(b -> b.spell()).toList()
        + " cooldowns " + p.cooldowns().keySet();
  }

  private BaseNPC npcByName(MainGameScreen s, String fragment) {
    for (BaseNPC npc : npcs(s)) if (npc.getName() != null && npc.getName().toLowerCase(Locale.ROOT).contains(fragment)) return npc;
    return null;
  }

  /** Walks to the shopkeeper and clicks him through the screen's own NPC click handler; the shop panel opens on the listing. */
  private void openShopOf(String fragment, String label) {
    repeat(
        "within talking distance of " + fragment,
        40_000,
        () -> {
          var s = game();
          var npc = s == null ? null : npcByName(s, fragment);
          if (npc == null) return false;
          walkToward(s, npc.getPosition(), 60f);
          return distance(s, npc.getPosition()) <= 60f;
        });
    action(() -> held.clear());
    waitMs(600);
    action(
        () -> {
          var s = game();
          var npc = npcByName(s, fragment);
          Vector2 at = aim(npc.getPosition(), p -> npc.isMouseOver(p.x, p.y));
          log("SHOP " + label + ": clicking " + fragment + " at " + at + ", distance " + Math.round(distance(s, npc.getPosition())) + " px; state " + state());
          if (at != null) click(s, at.x, at.y);
        });
    waitMs(3000);
    action(
        () -> {
          if (shopPanel().isOpen()) return;
          log("SHOP " + label + ": the first click did not open the shop; state " + state());
          hud("shop-first-click-not-open-" + label);
          var s = game();
          var npc = npcByName(s, fragment);
          Vector2 at = aim(npc.getPosition(), p -> npc.isMouseOver(p.x, p.y));
          if (at != null) click(s, at.x, at.y);
        });
    waitMs(3000);
    action(
        () -> {
          log("SHOP " + label + ": panel open " + shopPanel().isOpen() + (shopPanel().isOpen() ? ", server gold shown " + shopPanel().gold() + ", buy rows " + shopPanel().rows().size() : ""));
          hud("shop-open-" + label);
        });
  }

  /** Presses the digit of the row with this item key on the shop panel's current page, then reports committed state. */
  private void shopRow(String itemKey, String label) {
    final String[] before = {null};
    action(
        () -> {
          var panel = shopPanel();
          before[0] = state();
          int digit = -1;
          var visible = panel.visible();
          for (int i = 0; i < visible.size(); i++) if (visible.get(i).key().equals(itemKey)) digit = i + 1;
          log("SHOP " + label + ": " + panel.mode() + " " + itemKey + " digit " + digit + " quantity " + panel.quantity() + "; before: " + before[0]);
          if (digit < 0) {
            log("SHOP " + label + ": row not on this page");
            return;
          }
          press(digitKey(digit));
        });
    waitMs(2500);
    action(
        () -> {
          log("SHOP " + label + ": after:  " + state() + " (changed: " + !state().equals(before[0]) + ")");
          hud("shop-" + label);
        });
  }

  // Frame capture of the other player (appearance and walking), taken from the real back buffer.
  private static final int CROP = 160;
  private long lastCrop;
  private final List<Pixmap> crops = new ArrayList<>();
  private final List<String> cropLog = new ArrayList<>();
  private final int maxCrops = 12;

  private Object animationField(com.perso.T4C.player.Player other, String name) {
    return field(other.getAnimations(), com.perso.T4C.player.PlayerAnimations.class, name);
  }

  private void sampleOther(boolean keepPixels) {
    var s = game();
    if (s == null) return;
    var list = others(s);
    if (list.isEmpty()) {
      cropLog.add("no other player");
      return;
    }
    var other = list.get(0);
    Vector3 p = camera(s).project(new Vector3(other.getPositionVector().x, other.getPositionVector().y, 0));
    int w = Gdx.graphics.getBackBufferWidth(), h = Gdx.graphics.getBackBufferHeight();
    int x0 = Math.max(0, Math.min(w - CROP, Math.round(p.x) - CROP / 2));
    int y0 = Math.max(0, Math.min(h - CROP, Math.round(p.y) - 40));
    boolean moving = (Boolean) field(other.getMovement(), com.perso.T4C.player.PlayerMovement.class, "moving");
    cropLog.add(String.format("t=%.2fs moving=%b animTimer=%.3f pos=(%.0f,%.0f)", (System.currentTimeMillis() - t0) / 1000.0, moving,
        (Float) animationField(other, "animTimer"), other.getPositionVector().x, other.getPositionVector().y));
    if (!keepPixels || crops.size() >= maxCrops) return;
    byte[] pixels = ScreenUtils.getFrameBufferPixels(x0, y0, CROP, CROP, true);
    for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 255;
    Pixmap crop = new Pixmap(CROP, CROP, Pixmap.Format.RGBA8888);
    BufferUtils.copy(pixels, 0, crop.getPixels(), pixels.length);
    crops.add(crop);
  }

  private void saveStrip(String name) {
    if (crops.isEmpty()) return;
    Pixmap strip = new Pixmap(CROP * crops.size(), CROP, Pixmap.Format.RGBA8888);
    for (int i = 0; i < crops.size(); i++) {
      strip.drawPixmap(crops.get(i), i * CROP, 0);
      crops.get(i).dispose();
    }
    crops.clear();
    String file = tag + "-" + name + ".png";
    PixmapIO.writePNG(new com.badlogic.gdx.files.FileHandle(out.resolve(file).toFile()), strip);
    strip.dispose();
    log("saved " + file);
  }

  private void waresScene() {
    if (tag.endsWith("A")) waresHealer();
    else waresWalker();
  }

  /** The walker (B): stands still, then walks a short way out and back while the healer (A) captures frames. */
  private void waresWalker() {
    until("healer is ready to watch", 90_000, () -> signaled("A.watch"));
    waitMs(3500);
    action(() -> signal("B.still-done"));
    final Vector2[] goal = {null};
    final Vector2[] home = {null};
    action(
        () -> {
          var s = game();
          var p = me(s).getCoordinates();
          home[0] = new Vector2(p.getX(), p.getY());
          int ptx = (int) (p.getX() / TILE_W), pty = (int) (p.getY() / TILE_H);
          for (int d = 8; d >= 5 && goal[0] == null; d--)
            for (int dx = -d; dx <= d && goal[0] == null; dx++)
              for (int dy = -d; dy <= d && goal[0] == null; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) != d || !walkable(ptx + dx, pty + dy)) continue;
                var path = search(ptx, pty, ptx + dx, pty + dy);
                if (path.size() >= d) goal[0] = new Vector2((ptx + dx) * TILE_W + TILE_W / 2f, (pty + dy) * TILE_H + TILE_H / 2f);
              }
          log("WALK: home " + home[0] + " goal " + goal[0]);
          signal("B.walking");
        });
    repeat("walked out", 20_000, () -> {
      var s = game();
      if (goal[0] == null) return true;
      walkToward(s, goal[0], 12f);
      return distance(s, goal[0]) <= 12f;
    });
    repeat("walked back", 20_000, () -> {
      var s = game();
      walkToward(s, home[0], 12f);
      return distance(s, home[0]) <= 12f;
    });
    action(
        () -> {
          held.clear();
          signal("B.walk-done");
        });
    // The Warrior carries an over-weight stack: a purchase must be refused by the server.
    openShopOf("sigfried", "walker");
    shopRow("item.rusted_short_sword", "walker-overweight-buy");
    action(() -> signal("B.shop-done"));
    until("healer finished", 240_000, () -> signaled("A.pre-stop"));
    action(() -> shopPanel().close());
    restartCycle(() -> log("AFTER RESTART (walker): " + state()));
  }

  /** The healer (A): watches the walker, then drives the inventory, spell, potion and shop controls, then restarts. */
  private void waresHealer() {
    // 5. First, while the provisioned wounds are still there: Light Heal through C, a healing potion through U.
    action(
        () -> {
          log("SPELL before: " + state());
          shot("wounded-before-spell");
          press(Input.Keys.C);
        });
    waitMs(900);
    action(
        () -> {
          log("SPELL list: " + abilityMenu().lines(view().character().progress()));
          hud("spell-list");
          press(Input.Keys.NUM_1);
        });
    waitMs(3000);
    action(
        () -> {
          log("SPELL after Light Heal: " + state());
          hud("spell-cast");
        });
    // 6. The potion list (U) as the server allows it, then a healing potion; a mana potion should be absent.
    potion("healing");
    // 3. The other player's appearance and the walking animation, from the real frames.
    action(() -> signal("A.watch"));
    until("walker held still", 60_000, () -> signaled("B.still-done"));
    action(
        () -> {
          sampleOther(true);
          sampleOther(false);
          shot("walker-standing");
          log("STILL samples: " + cropLog);
          cropLog.clear();
        });
    until("walker started walking", 30_000, () -> signaled("B.walking"));
    repeat(
        "walker walked out and back",
        30_000,
        () -> {
          long now = System.currentTimeMillis();
          if (now - lastCrop >= 110) {
            lastCrop = now;
            sampleOther(true);
          }
          return signaled("B.walk-done");
        });
    action(
        () -> {
          log("WALK samples (" + cropLog.size() + "): " + cropLog);
          saveStrip("walk-strip-still-then-walking");
          cropLog.clear();
        });
    // 4. Inventory panel: unequip, re-equip, then leave the robe off for the restart check.
    action(
        () -> {
          log("INVENTORY start: " + state());
          press(Input.Keys.I);
        });
    waitMs(900);
    action(
        () -> {
          var rows = inventoryPanel().rows(view().character().progress());
          StringBuilder b = new StringBuilder();
          for (int i = 0; i < rows.size(); i++) b.append(i + 1).append(':').append(rows.get(i).kind()).append(':').append(rows.get(i).item().key()).append(' ');
          log("INVENTORY open=" + inventoryPanel().isOpen() + " rows " + b);
          shot("inventory-open");
        });
    inventoryPick("staff", true, "unequip-staff");
    inventoryPick("staff", false, "re-equip-staff");
    inventoryPick("robe", true, "unequip-robe");
    action(() -> press(Input.Keys.I));
    // 7. Shop: buy, a purchase the character cannot afford, sell.
    openShopOf("sigfried", "healer");
    shopRow("item.rusted_short_sword", "buy-short-sword");
    shopRow("item.rusted_hand_axe", "buy-too-poor");
    action(() -> press(Input.Keys.TAB));
    waitMs(700);
    shopRow("item.torch", "sell-torch");
    action(() -> press(Input.Keys.T));
    waitMs(500);
    action(() -> log("SHOP closed: panel open " + shopPanel().isOpen()));
    until("walker finished its shop attempt", 90_000, () -> signaled("B.shop-done"));
    action(
        () -> {
          before = view().character().progress();
          log("BEFORE STOP (healer): " + state());
          signal("A.pre-stop");
        });
    restartCycle(
        () -> {
          log("AFTER RESTART (healer): " + state());
          log("AFTER RESTART same inventory and equipment as before the stop: "
              + (inventorySummary(view().character().progress()).equals(inventorySummary(before))
                  && view().character().progress().equipment().equals(before.equipment())));
          press(Input.Keys.I);
        });
    waitMs(900);
    action(() -> hud("inventory-after-restart"));
    inventoryPick("robe", false, "re-equip-robe-after-restart");
    action(() -> press(Input.Keys.I));
  }

  /** Presses the digit of the first inventory-panel row (worn or carried) whose item key contains the fragment. */
  private void inventoryPick(String fragment, boolean worn, String label) {
    final String[] before = {null};
    action(
        () -> {
          before[0] = state();
          var rows = inventoryPanel().rows(view().character().progress());
          int digit = -1;
          for (int i = 0; i < rows.size() && i < 9; i++) {
            var r = rows.get(i);
            boolean isWorn = r.kind() == com.perso.T4C.online.OnlineInventoryPanel.Kind.EQUIPPED;
            if (isWorn == worn && r.item().key().contains(fragment)) {
              digit = i + 1;
              break;
            }
          }
          log("INVENTORY " + label + ": digit " + digit + "; before: " + before[0]);
          if (digit > 0) press(digitKey(digit));
        });
    waitMs(2500);
    action(
        () -> {
          log("INVENTORY " + label + ": after:  " + state() + " (changed: " + !state().equals(before[0]) + ")");
          hud("inventory-" + label);
        });
  }

  private void potion(String fragment) {
    final String[] before = {null};
    action(
        () -> {
          before[0] = state();
          press(Input.Keys.U);
        });
    waitMs(900);
    action(
        () -> {
          var lines = abilityMenu().lines(view().character().progress());
          int digit = -1;
          for (int i = 1; i < lines.size() - 1; i++) if (lines.get(i).toLowerCase(Locale.ROOT).contains(fragment)) { digit = i; break; }
          log("POTION " + fragment + ": list " + lines + " digit " + digit + "; mana potion offered: "
              + lines.stream().anyMatch(l -> l.toLowerCase(Locale.ROOT).contains("mana")) + "; before: " + before[0]);
          hud("potion-list");
          if (digit > 0) press(digitKey(digit));
          else press(Input.Keys.U);
        });
    waitMs(2800);
    action(
        () -> {
          log("POTION " + fragment + ": after:  " + state() + " (changed: " + !state().equals(before[0]) + ")");
          hud("potion-used");
        });
  }

  // ---------------------------------------------------------------- quest scene (T4C-0312)

  private static final String QUEST_KILLS = "quest.renewed_wards.kills", QUEST_STATUS = "quest.renewed_wards.status";

  private String questState() {
    var c = view().character();
    var flags = c.progress().questFlags();
    return "status " + flags.getOrDefault(QUEST_STATUS, 0) + " kills " + flags.getOrDefault(QUEST_KILLS, 0) + " gold " + c.gold() + " xp "
        + c.experience() + " level " + c.progress().advancement().level() + " spells " + new java.util.TreeSet<>(c.progress().spells())
        + " rev " + c.revision() + " hp " + c.progress().vitals().hp() + "/" + c.progress().vitals().maxHp();
  }

  /** Clicks the giver through the screen's own NPC click handler, walking up to him first. */
  private void talkTo(String fragment, String label) {
    repeat(
        "within talking distance of " + fragment,
        60_000,
        () -> {
          var s = game();
          var npc = s == null ? null : npcByName(s, fragment);
          if (npc == null) return false;
          walkToward(s, npc.getPosition(), 60f);
          return distance(s, npc.getPosition()) <= 60f;
        });
    action(() -> held.clear());
    waitMs(600);
    action(
        () -> {
          var s = game();
          var npc = npcByName(s, fragment);
          // The screen's click handler takes the first NPC under the cursor (Moonrock stands in front of the
          // Warden), so aim where only this NPC is.
          Vector2 at = aim(npc.getPosition(), p -> npc.isMouseOver(p.x, p.y)
              && npcs(s).stream().noneMatch(o -> o != npc && o.isMouseOver(p.x, p.y)));
          log("TALK " + label + ": click on " + fragment + " at " + at + "; before: " + questState() + "; " + npcDebug(s, at));
          if (at != null) click(s, at.x, at.y);
        });
    waitMs(3500);
    action(
        () -> {
          log("TALK " + label + ": after:  " + questState());
          hud("talk-" + label);
        });
  }

  /** Fights the nearest live monster through the monster click handler until {@code done} holds or the time runs out. */
  private void fightUntil(String what, long timeoutMs, Predicate<OnlineConnection.View> done) {
    final BaseMonster[] last = {null};
    final long[] potionAt = {0};
    final int[] potionStage = {0};
    repeat(
        what,
        timeoutMs,
        () -> {
          var s = game();
          if (s == null) return false;
          long now = System.currentTimeMillis();
          var v = view();
          if (done.test(v)) {
            held.clear();
            return true;
          }
          // A player drinks a healing potion through the U control when badly hurt: U opens the list, 1 uses the first.
          var vit = v.character().progress().vitals();
          if (potionStage[0] == 0 && vit.hp() > 0 && vit.hp() * 100 < vit.maxHp() * 40 && now - potionAt[0] > 5000) {
            press(Input.Keys.U);
            potionStage[0] = 1;
            potionAt[0] = now;
          } else if (potionStage[0] == 1 && now - potionAt[0] > 350) {
            boolean listed = abilityMenu().isOpen() && abilityMenu().lines(v.character().progress()).size() > 2;
            press(listed ? Input.Keys.NUM_1 : Input.Keys.U);
            log("potion at hp " + vit.hp() + "/" + vit.maxHp() + ": " + (listed ? "used the first listed potion" : "no potion left"));
            potionStage[0] = 0;
            potionAt[0] = now;
          }
          if (v.character().progress().vitals().hp() <= 0) {
            held.clear();
            return false;
          }
          BaseMonster target = nearestLive(s);
          if (target == null) {
            held.clear();
            return false;
          }
          if (last[0] != target) last[0] = target;
          double d = distance(s, target.getPosition());
          float reach = GameConstants.PLAYER_MELEE_ATTACK_RANGE - 14f;
          if (d > reach) walkToward(s, target.getPosition(), reach);
          else held.clear();
          if (d <= reach + 8 && now - lastClick > 650) {
            Vector2 at = aim(target.getPosition(), p -> target.isMouseOver(p.x, p.y));
            if (at != null) {
              click(s, at.x, at.y);
              attacksSent++;
            }
          }
          if (now - lastMessageLog > 15_000) {
            lastMessageLog = now;
            log("fight: " + questState() + " attacks " + attacksSent);
          }
          return false;
        });
  }

  /**
   * The quest giver's greeting is the only quest control the rendered screen has: it turns a finished quest in, but
   * offers none (acceptance is topic 0 and the screen sends no topic). So the accept is sent through the same
   * connection object the screen uses, and logged as such. The operator relocates the character between the temple and
   * the Skeleton ground while the server is stopped (the ground is about 160 tiles away behind cave mouths; walking
   * it is the soak's job, not this script's).
   */
  private void questScene() {
    if (!tag.endsWith("A")) {
      // The second window stays in the temple as an observer; it only follows the two server restarts.
      restartCycle("to-skeletons", null);
      restartCycle("to-giver", null);
      until("quester finished", 900_000, () -> signaled(peer() + ".quest-done"));
      return;
    }
    talkTo("aelric", "greeting-before-accept");
    action(
        () -> {
          log("ACCEPT: sending topic 0 through the connection (the screen has no topic control); before: " + questState());
          connection().interact("WardenAelric", java.util.OptionalInt.of(0)).whenComplete((r, f) ->
              log("ACCEPT reply: " + (f != null ? "failure " + f : "accepted=" + (r != null && r.accepted()) + " key=" + (r == null ? null : r.messageKey()))));
        });
    waitMs(3500);
    action(
        () -> {
          log("ACCEPT after: " + questState());
          hud("quest-accepted-via-connection");
        });
    talkTo("aelric", "greeting-after-accept-not-ready");
    action(() -> before = view().character().progress());
    restartCycle("to-skeletons", () -> log("AT SKELETONS: " + questState()));
    action(
        () -> {
          shot("at-skeleton-ground");
          experienceBefore = view().character().experience();
        });
    fightUntil("12 Skeleton kills credited", 600_000, v -> v.character().progress().questFlags().getOrDefault(QUEST_KILLS, 0) >= 12);
    action(
        () -> {
          held.clear();
          log("KILLS DONE: " + questState() + " attacks " + attacksSent);
          shot("kills-done");
        });
    // Let any drop sit; the quest asks for no item.
    restartCycle("to-giver", () -> log("BACK AT THE GIVER: " + questState()));
    talkTo("aelric", "turn-in");
    action(() -> log("TURN-IN result: " + questState()));
    talkTo("aelric", "turn-in-repeat");
    action(
        () -> {
          log("TURN-IN repeat result (no second reward expected): " + questState());
          signal(tag + ".quest-done");
        });
  }

  private long experienceBefore;

  /** Which drawn NPCs are under the click point, and where the server says the player and the giver are. */
  private String npcDebug(MainGameScreen s, Vector2 at) {
    StringBuilder b = new StringBuilder("under the click:");
    for (BaseNPC npc : npcs(s)) if (at != null && npc.isMouseOver(at.x, at.y)) b.append(" ").append(npc.getName());
    var v = view();
    var me = me(s).getCoordinates();
    b.append(" | own (").append(Math.round(me.getX())).append(",").append(Math.round(me.getY())).append(") server actors ");
    for (var p : v.snapshot().players()) b.append("(").append(p.x()).append(",").append(p.y()).append(") ");
    for (var n : v.snapshot().npcs()) if (n.id().equals("WardenAelric")) b.append("| Aelric (").append(n.x()).append(",").append(n.y()).append(") origin (").append(v.map().originX()).append(",").append(v.map().originY()).append(")");
    return b.toString();
  }

  /** Solo scene: only the turn-in at the giver for a quester whose kills are already credited (the previous pass could not reach him). */
  private void turnInScene() {
    talkTo("aelric", "turn-in-first");
    action(() -> log("TURN-IN result: " + questState()));
    talkTo("aelric", "turn-in-repeat");
    action(() -> log("TURN-IN repeat result (no second reward expected): " + questState()));
  }

  // ---------------------------------------------------------------- beta scene (T4C-0316)
  //
  // One long, two-window script on the real screens: the NPC topic panel, equipment seen by the other window, portal
  // trips watched from both sides, a walk to the Skeleton ground, a mixed fight/idle/loot session with claim races, a
  // death and an individual corpse recovery. Everything goes through the screens' own input handlers except where a
  // line says "through the connection" (the screen has no control for it); those are logged as such.

  private final List<Long> messageAt = new ArrayList<>();
  private final List<String> messageText = new ArrayList<>();
  private String lastMessage;

  /** Logs every new on-screen system message (the screen's own SystemMessage, read by reflection). */
  private void pollMessage() {
    var s = game();
    if (s == null) return;
    try {
      // System messages go to the chat log (the screen registers the chat as the message sink): read its entries.
      Object chat = field(s, MainGameScreen.class, "gameChat");
      if (chat == null) return;
      @SuppressWarnings("unchecked")
      List<Object> entries = (List<Object>) field(chat, chat.getClass(), "entries");
      if (entries.size() < chatSeen) chatSeen = 0;
      while (chatSeen < entries.size()) {
        Object entry = entries.get(chatSeen++);
        var accessor = entry.getClass().getDeclaredMethod("segments");
        accessor.setAccessible(true);
        StringBuilder text = new StringBuilder();
        for (Object segment : (List<?>) accessor.invoke(entry)) {
          var t = segment.getClass().getDeclaredMethod("text");
          t.setAccessible(true);
          text.append(t.invoke(segment));
        }
        messageAt.add(System.currentTimeMillis());
        messageText.add(text.toString());
        log("MSG: " + text);
      }
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      // the screen is being rebuilt
    }
  }

  private int chatSeen;

  private List<String> messagesSince(long t) {
    List<String> found = new ArrayList<>();
    for (int i = 0; i < messageAt.size(); i++) if (messageAt.get(i) >= t) found.add(messageText.get(i));
    return found;
  }

  private com.perso.T4C.online.OnlineTalkPanel talkPanel() {
    return (com.perso.T4C.online.OnlineTalkPanel) field(game(), MainGameScreen.class, "remoteTalkPanel");
  }

  private void hoverOver(MainGameScreen s, Vector2 world) {
    Vector3 p = camera(s).project(new Vector3(world.x, world.y, 0));
    hoverX = Math.round(p.x);
    hoverY = Gdx.graphics.getHeight() - Math.round(p.y) - 1;
    Gdx.input.getInputProcessor().mouseMoved(hoverX, hoverY);
  }

  /** A small picture around a world point from the real back buffer. */
  private void cropOf(String name, Vector2 world, int w, int h) {
    var s = game();
    if (s == null) return;
    Vector3 p = camera(s).project(new Vector3(world.x, world.y, 0));
    int bw = Gdx.graphics.getBackBufferWidth(), bh = Gdx.graphics.getBackBufferHeight();
    int x0 = Math.max(0, Math.min(bw - w, Math.round(p.x) - w / 2));
    int y0 = Math.max(0, Math.min(bh - h, Math.round(p.y) - 50));
    byte[] pixels = ScreenUtils.getFrameBufferPixels(x0, y0, w, h, true);
    for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 255;
    Pixmap crop = new Pixmap(w, h, Pixmap.Format.RGBA8888);
    BufferUtils.copy(pixels, 0, crop.getPixels(), pixels.length);
    String file = tag + "-" + String.format("%02d", ++shots) + "-" + name + ".png";
    PixmapIO.writePNG(new com.badlogic.gdx.files.FileHandle(out.resolve(file).toFile()), crop);
    crop.dispose();
    log("saved " + file);
  }

  private OnlineConnection.Actor ownActor() {
    var v = view();
    for (var a : v.snapshot().players()) if (a.id() == v.ownId()) return a;
    return null;
  }

  private OnlineConnection.Actor otherActor() {
    var v = view();
    for (var a : v.snapshot().players()) if (a.id() != v.ownId()) return a;
    return null;
  }

  private static String lookOf(OnlineConnection.Actor a) {
    if (a == null) return "none";
    return "name=" + a.name() + " gender=" + a.gender() + " equipment="
        + new java.util.TreeMap<>(a.equipment() == null ? java.util.Map.<String, String>of() : a.equipment());
  }

  private String drawnOther(MainGameScreen s) {
    var list = others(s);
    if (list.isEmpty()) return "no other player drawn";
    var p = list.get(0);
    return "drawn name=" + p.getName() + " equippedItems=" + new java.util.TreeMap<>(p.getEquippedItems()) + " at " + p.getPositionVector();
  }

  private Vector2 tileCenter(int tx, int ty) {
    return new Vector2(tx * TILE_W + TILE_W / 2f, ty * TILE_H + TILE_H / 2f);
  }

  /** Runs {@code body} each frame for {@code ms}, then logs the end of the segment. */
  private void segment(String what, long ms, Runnable body) {
    final long[] start = {-1};
    add(
        () -> {
          long now = System.currentTimeMillis();
          if (start[0] < 0) {
            start[0] = now;
            log("SEGMENT start: " + what + " | " + state());
          }
          if (now - start[0] >= ms) {
            held.clear();
            log("SEGMENT end: " + what + " after " + (now - start[0]) / 1000 + " s | " + state());
            return true;
          }
          body.run();
          return false;
        });
  }

  // ---- chapter 1: the NPC topic panel

  private void topicChapter() {
    if (tag.endsWith("B")) until("Alpha finished the topic panel", 240_000, () -> signaled(peer() + ".topic-done"));
    talkTo("aelric", "topic-greeting");
    action(
        () -> {
          var panel = talkPanel();
          log("TOPIC PANEL open=" + panel.isOpen() + " lines=" + panel.lines() + "; " + questState());
          shot("topic-panel");
        });
    action(
        () -> {
          if (talkPanel().isOpen()) {
            log("TOPIC: digit 1 (topic 0) pressed on the screen's own key handler");
            press(Input.Keys.NUM_1);
          } else log("TOPIC: the panel did not open after the greeting");
        });
    waitMs(3500);
    // A player who is told "They are busy for a moment. Try again." presses the digit again (T4C-0353: the second window's first
    // press was refused that way in a live run; the game now also retries once by itself, but the packaged build under test does not).
    for (int attempt = 2; attempt <= 4; attempt++) {
      final int n = attempt;
      action(
          () -> {
            if (view().character().progress().questFlags().getOrDefault(QUEST_STATUS, 0) == 0 && talkPanel().isOpen()) {
              log("TOPIC: not accepted yet, digit 1 pressed again (attempt " + n + ")");
              press(Input.Keys.NUM_1);
            }
          });
      waitMs(3500);
    }
    action(
        () -> {
          log("TOPIC after topic 0: " + questState() + " panel open " + talkPanel().isOpen());
          shot("topic-accepted");
          signal(tag + ".topic-done");
        });
    until("the other window finished the topic panel", 240_000, () -> signaled(peer() + ".topic-done"));
    // The panel stays open after a topic; a player closes it with Esc (the screen's own key handler).
    action(() -> press(Input.Keys.ESCAPE));
  }

  // ---- chapter 2: equipment seen by the other window

  /** Presses the digit of the first inventory row matching {@code match}, turning pages first when it is past nine. */
  private void inventoryPickRow(Predicate<com.perso.T4C.online.OnlineInventoryPanel.Row> match, String label, String[] pickedKey) {
    final int[] digit = {-1};
    final String[] before = {null};
    action(
        () -> {
          before[0] = state();
          var rows = inventoryPanel().rows(view().character().progress());
          int idx = -1;
          for (int i = 0; i < rows.size(); i++) if (match.test(rows.get(i))) { idx = i; break; }
          if (idx < 0) {
            log("INVENTORY " + label + ": no matching row");
            return;
          }
          if (pickedKey != null && pickedKey[0] == null) pickedKey[0] = rows.get(idx).item().key();
          for (int p = 0; p < idx / 9; p++) press(Input.Keys.RIGHT);
          digit[0] = idx % 9 + 1;
          log("INVENTORY " + label + ": row " + (idx + 1) + " item " + rows.get(idx).item().key() + " slot " + rows.get(idx).slot());
        });
    waitMs(700);
    action(
        () -> {
          if (digit[0] > 0) press(digitKey(digit[0]));
        });
    waitMs(2500);
    action(
        () -> {
          log("INVENTORY " + label + ": after:  " + state() + " (changed: " + !state().equals(before[0]) + ")");
          hud("inventory-" + label);
        });
  }

  private void equipRound(String actor) {
    boolean acting = tag.endsWith(actor);
    String r = "equip-" + actor;
    final String[] picked = {null};
    if (acting) {
      action(
          () -> {
            log("EQUIP(" + actor + ") start, own public look: " + lookOf(ownActor()) + "; " + state());
            press(Input.Keys.I);
          });
      waitMs(900);
      inventoryPickRowVisible(picked);
      // placeholder: the unequip happens in inventoryPickRowVisible's last step
      action(() -> signal(r + ".ready"));
      until("observer looked at the unchanged look", 60_000, () -> signaled(r + ".before"));
      inventoryPickRow(row -> row.kind() == com.perso.T4C.online.OnlineInventoryPanel.Kind.EQUIPPED
          && row.item().key().equals(picked[0]), "unequip-" + actor, null);
      action(() -> log("EQUIP(" + actor + ") after unequip, own public look: " + lookOf(ownActor())));
      action(() -> signal(r + ".off"));
      until("observer looked at the changed look", 60_000, () -> signaled(r + ".after-off"));
      inventoryPickRow(row -> row.kind() == com.perso.T4C.online.OnlineInventoryPanel.Kind.CARRIED
          && row.item().key().equals(picked[0]), "re-equip-" + actor, null);
      action(() -> log("EQUIP(" + actor + ") after re-equip, own public look: " + lookOf(ownActor())));
      action(() -> signal(r + ".on"));
      until("observer looked at the restored look", 60_000, () -> signaled(r + ".after-on"));
      action(() -> press(Input.Keys.I));
    } else {
      until("the other player is ready", 120_000, () -> signaled(r + ".ready"));
      observeOther(r + "-before");
      action(() -> signal(r + ".before"));
      until("the other player unequipped", 60_000, () -> signaled(r + ".off"));
      waitMs(1500);
      observeOther(r + "-after-unequip");
      action(() -> signal(r + ".after-off"));
      until("the other player re-equipped", 60_000, () -> signaled(r + ".on"));
      waitMs(1500);
      observeOther(r + "-after-reequip");
      action(
          () -> {
            hoverX = -1;
            signal(r + ".after-on");
          });
    }
    until("round finished on both sides", 60_000, () -> signaled(r + ".after-on"));
  }

  /** Unequips nothing yet: only records which visible worn item the acting player will take off (armor before weapon). */
  private void inventoryPickRowVisible(String[] picked) {
    action(
        () -> {
          var own = ownActor();
          var rows = inventoryPanel().rows(view().character().progress());
          String first = null, armor = null;
          for (var r : rows) {
            if (r.kind() != com.perso.T4C.online.OnlineInventoryPanel.Kind.EQUIPPED || r.slot() == null) continue;
            boolean visible = own != null && own.equipment() != null && own.equipment().containsKey(r.slot().name());
            if (!visible) continue;
            if (first == null) first = r.item().key();
            if (armor == null && !r.slot().name().contains("WEAPON") && !r.slot().name().contains("SHIELD")) armor = r.item().key();
          }
          picked[0] = armor != null ? armor : first;
          log("EQUIP: chosen visible worn item " + picked[0] + "; rows " + rows.stream().map(x -> x.kind() + ":" + x.slot() + ":" + x.item().key()).toList());
        });
  }

  private void observeOther(String label) {
    action(
        () -> {
          var s = game();
          var list = others(s);
          if (list.isEmpty()) {
            log("OBSERVE " + label + ": no other player drawn");
            return;
          }
          var p = list.get(0);
          Vector2 at = aim(p.getPositionVector(), q -> p.isMouseOver(q.x, q.y));
          if (at != null) hoverOver(s, at);
          log("OBSERVE " + label + ": pointer on the figure at " + at + "; snapshot actor " + lookOf(otherActor()) + " | " + drawnOther(s));
        });
    waitMs(800);
    action(
        () -> {
          var s = game();
          var list = others(s);
          if (!list.isEmpty()) cropOf("observe-" + label, list.get(0).getPositionVector(), 220, 260);
        });
  }

  // ---- chapter 3: portal hops watched from both windows

  /**
   * One character walks onto an authored portal tile; the other window watches. {@code observerInFrom} says whether the
   * observer stands in the region being left (it should see the walker disappear) or in the destination (it should
   * see the walker arrive).
   */
  private void portalHop(String walker, int tx, int ty, String fromKey, String toKey, boolean observerInFrom, String label) {
    boolean walking = tag.endsWith(walker);
    String go = "hop-" + label + ".go", arrived = "hop-" + label + ".arrived", seen = "hop-" + label + ".seen";
    if (walking) {
      action(
          () -> {
            var v = view();
            log("HOP " + label + " (walker): region " + v.map().key() + " epoch " + v.map().locationEpoch() + " others drawn " + others(game()).size());
            signal(go);
          });
      repeat(
          "walked onto the portal " + label,
          150_000,
          () -> {
            var s = game();
            if (s == null) return false;
            if (!view().map().key().equals(fromKey)) return true;
            allowTile = tileKey(tx, ty);
            walkToward(s, tileCenter(tx, ty), 3f);
            long now = System.currentTimeMillis();
            if (now - lastMessageLog > 4000) {
              lastMessageLog = now;
              var p = me(s).getCoordinates();
              log("HOP " + label + " walking: at (" + Math.round(p.getX()) + "," + Math.round(p.getY()) + ") tile (" + (int) (p.getX() / TILE_W) + "," + (int) (p.getY() / TILE_H)
                  + ") goal tile (" + tx + "," + ty + ") keys " + held + " route " + route.size());
            }
            return false;
          });
      action(() -> held.clear());
      until("the character is in region " + toKey + " with the new map applied", 40_000, () -> {
        var v = view();
        return game() != null && v.map().key().equals(toKey) && v.snapshot() != null && v.snapshot().region() != null
            && v.snapshot().region().key().equals(toKey);
      });
      waitMs(2500);
      action(
          () -> {
            var v = view();
            var p = me(game()).getCoordinates();
            log("HOP " + label + " (walker) arrived: region " + v.map().key() + " epoch " + v.map().locationEpoch() + " position (" + Math.round(p.getX())
                + "," + Math.round(p.getY()) + ") tile (" + (int) (p.getX() / TILE_W) + "," + (int) (p.getY() / TILE_H) + ") others drawn " + others(game()).size());
            shot("walker-arrived-" + label);
            signal(arrived);
          });
      until("observer saw it", 90_000, () -> signaled(seen));
    } else {
      until("walker started " + label, 150_000, () -> signaled(go));
      final long[] began = {0};
      action(() -> began[0] = System.currentTimeMillis());
      if (observerInFrom) {
        repeat(
            "the walker disappeared from my region",
            150_000,
            () -> {
              var s = game();
              return s != null && others(s).isEmpty() && view().snapshot().players().stream().noneMatch(a -> a.id() != view().ownId());
            });
        action(
            () -> {
              log("OBSERVE " + label + ": the walker left my region " + view().map().key() + " after " + (System.currentTimeMillis() - began[0]) / 1000.0
                  + " s; drawn others " + others(game()).size() + ", snapshot players " + view().snapshot().players().size());
              shot("observer-sees-leave-" + label);
            });
        until("walker arrived", 90_000, () -> signaled(arrived));
      } else {
        until("walker arrived", 150_000, () -> signaled(arrived));
        repeat("the walker is drawn in my region", 60_000, () -> game() != null && !others(game()).isEmpty());
        waitMs(1500);
        action(
            () -> {
              var s = game();
              log("OBSERVE " + label + ": the walker is drawn in my region " + view().map().key() + "; " + lookOf(otherActor()) + " | " + drawnOther(s));
              shot("observer-sees-arrive-" + label);
              cropOf("observer-crop-arrive-" + label, others(s).get(0).getPositionVector(), 220, 260);
            });
      }
      action(() -> signal(seen));
    }
  }

  // ---- chapter 4: walk to the hunting ground

  private void walkToGround() {
    repeat(
        "reached the Skeleton ground",
        420_000,
        () -> {
          var s = game();
          if (s == null || goalTx == Integer.MIN_VALUE) return true;
          for (BaseMonster m : monsters(s))
            if (!m.isDead() && m.getHealth() > 0 && m.getName() != null && m.getName().toLowerCase(Locale.ROOT).contains("skeleton")
                && distance(s, m.getPosition()) < 10 * TILE_W) return true;
          walkToward(s, tileCenter(goalTx, goalTy), 40f);
          if (System.currentTimeMillis() - lastMessageLog > 20_000) {
            lastMessageLog = System.currentTimeMillis();
            var p = me(s).getCoordinates();
            log("WALK to the ground: at tile (" + (int) (p.getX() / TILE_W) + "," + (int) (p.getY() / TILE_H) + "), goal (" + goalTx + "," + goalTy + "), live monsters drawn " + monsters(s).size());
          }
          return false;
        });
    action(
        () -> {
          held.clear();
          var p = me(game()).getCoordinates();
          log("WALK done at tile (" + (int) (p.getX() / TILE_W) + "," + (int) (p.getY() / TILE_H) + "); " + state());
          shot("at-skeleton-ground");
        });
  }

  // ---- chapter 5: the mixed session with loot races

  private final long[] potionAt = {0};
  private int potionStage;
  private boolean noPotions;
  private int minHp = Integer.MAX_VALUE;
  private long lastSoakLog;
  private long lastDropLog;
  private final Set<UUID> raced = new HashSet<>();
  private final Set<UUID> dropsSeen = new HashSet<>();

  private static final class Race {
    UUID id;
    int number;
    long at;
    String type;
    String key;
    boolean clicked;
    long clickedAt;
    int carriedBefore;
    String ownerBefore;
    long remainingBefore;
    boolean reported;
    Vector2 position;
  }

  private Race race;
  private int racesDone;
  private long lastRaceEnd;

  private BaseMonster nearestLiveTo(MainGameScreen s, Vector2 point) {
    BaseMonster best = null;
    double bestDistance = Double.MAX_VALUE;
    for (BaseMonster m : monsters(s)) {
      if (m.isDead() || m.getHealth() <= 0) continue;
      if (skeletonsOnly && !isSkeleton(m)) continue;
      double d = Math.hypot(point.x - m.getPosition().x, point.y - m.getPosition().y);
      if (d < bestDistance) {
        bestDistance = d;
        best = m;
      }
    }
    return best;
  }

  private void drinkIfHurt(OnlineConnection.View v, long now) {
    var vit = v.character().progress().vitals();
    minHp = Math.min(minHp, vit.hp());
    if (noPotions) return;
    if (potionStage == 0 && vit.hp() > 0 && vit.hp() * 100 < vit.maxHp() * 60 && now - potionAt[0] > 3000) {
      press(Input.Keys.U);
      potionStage = 1;
      potionAt[0] = now;
    } else if (potionStage == 1 && now - potionAt[0] > 350) {
      boolean listed = abilityMenu().isOpen() && abilityMenu().lines(v.character().progress()).size() > 2;
      press(listed ? Input.Keys.NUM_1 : Input.Keys.U);
      if (!listed) log("potion at hp " + vit.hp() + "/" + vit.maxHp() + ": no potion left");
      potionStage = 0;
      potionAt[0] = now;
    }
  }

  /** One frame of fighting through the monster click handler; with {@code focus} the nearest monster to that point. */
  private void combatFrame(MainGameScreen s, boolean attack, Vector2 focus) {
    long now = System.currentTimeMillis();
    var v = view();
    if (v == null) { // the connection is gone (a hard kill or an outage is in progress)
      held.clear();
      return;
    }
    drinkIfHurt(v, now);
    if (v.character().progress().vitals().hp() <= 0 || !attack) {
      held.clear();
      return;
    }
    BaseMonster target = focus == null ? nearestLive(s) : nearestLiveTo(s, focus);
    if (target == null || distance(s, target.getPosition()) > 25 * TILE_W) {
      if (goalTx != Integer.MIN_VALUE) walkToward(s, tileCenter(goalTx, goalTy), 40f);
      else held.clear();
      return;
    }
    double d = distance(s, target.getPosition());
    float reach = GameConstants.PLAYER_MELEE_ATTACK_RANGE - 14f;
    if (d > reach) walkToward(s, target.getPosition(), reach);
    else held.clear();
    if (d <= reach + 8 && now - lastClick > 650) {
      Vector2 at = aim(target.getPosition(), p -> target.isMouseOver(p.x, p.y));
      if (at != null) {
        click(s, at.x, at.y);
        attacksSent++;
      }
    }
  }

  private GroundItem groundItemOf(MainGameScreen s, UUID id) {
    var world = ((com.perso.T4C.session.GameSession) field(s, MainGameScreen.class, "session")).world();
    if (!(world instanceof com.perso.T4C.session.RemoteWorldDriver r)) return null;
    for (GroundItem item : groundItems(s)) if (id.equals(r.lootId(item))) return item;
    return null;
  }

  private com.perso.T4C.online.model.GroundLootView lootView(UUID id) {
    for (var loot : view().snapshot().loot()) if (loot.id().equals(id)) return loot;
    return null;
  }

  private String ownIdText() {
    return String.valueOf(view().ownId());
  }

  /** Leader (A): plan the next claim race once the previous one is over; both windows then follow the marker file. */
  private void planRace(long now) {
    if (race != null || !tag.endsWith("A") || now - lastRaceEnd < 15_000) return;
    int next = racesDone + 1;
    if (signaled("race-" + next)) return;
    var v = view();
    var other = otherActor();
    var mine = ownActor();
    if (other == null || mine == null) return;
    for (var loot : v.snapshot().loot()) {
      if (raced.contains(loot.id())) continue;
      if (Math.max(Math.abs(loot.x() - other.x()), Math.abs(loot.y() - other.y())) > 40
          || Math.max(Math.abs(loot.x() - mine.x()), Math.abs(loot.y() - mine.y())) > 40) continue;
      boolean inWindow = (next == 1 || next == 3) && loot.ownerRemainingMs() > 20_000;
      long at = inWindow ? now + 7_000 : now + Math.min(70_000, loot.ownerRemainingMs() + 2_000) + (loot.ownerRemainingMs() > 0 ? 0 : 5_000);
      String body = loot.id() + "|" + at + "|" + (inWindow ? "IN_WINDOW" : "AFTER_WINDOW") + "|" + loot.key();
      try {
        Files.writeString(sync.resolve("race-" + next), body);
      } catch (java.io.IOException e) {
        throw new IllegalStateException(e);
      }
      raced.add(loot.id());
      log("RACE " + next + " planned by the leader: " + body + " owner " + loot.ownerPlayerId() + " remaining " + loot.ownerRemainingMs());
      return;
    }
  }

  private void followRace(MainGameScreen s, long now) {
    if (race == null) {
      int next = racesDone + 1;
      Path marker = sync.resolve("race-" + next);
      if (!Files.exists(marker)) return;
      try {
        String[] part = Files.readString(marker).split("\\|");
        if (part.length < 4) return;
        race = new Race();
        race.number = next;
        race.id = UUID.fromString(part[0]);
        race.at = Long.parseLong(part[1]);
        race.type = part[2];
        race.key = part[3];
        raced.add(race.id);
      } catch (java.io.IOException | RuntimeException e) {
        race = null;
        return;
      }
    }
    var item = groundItemOf(s, race.id);
    if (item != null) race.position = new Vector2(item.getPosition());
    if (!race.clicked && now >= race.at) {
      race.clicked = true;
      race.clickedAt = now;
      var view = lootView(race.id);
      race.carriedBefore = count(view().character().progress(), race.key.replace("item.", ""));
      race.ownerBefore = view == null ? "gone" : String.valueOf(view.ownerPlayerId());
      race.remainingBefore = view == null ? -1 : view.ownerRemainingMs();
      Vector2 at = item == null ? null : aim(item.getPosition(), p -> item.isMouseOver(p.x, p.y));
      log("RACE " + race.number + " " + race.type + " CLICK at " + now + ": " + race.key + " own id " + ownIdText() + " owner " + race.ownerBefore
          + " remainingMs " + race.remainingBefore + " distance " + (item == null ? "n/a" : Math.round(distance(s, item.getPosition())) + " px")
          + " point " + at + " | " + state());
      if (at != null) click(s, at.x, at.y);
      if (m5KillRace == race.number) {
        m5PkDrop = race.id;
        m5PkKey = race.key;
        m5PkCarried = race.carriedBefore;
        if (tag.endsWith("A")) write("kill-now-pk", race.id + " " + System.currentTimeMillis());
        log("M5 PK: the claim click was delivered at " + System.currentTimeMillis());
      }
    }
    if (race.clicked && !race.reported && now - race.clickedAt > 3500) {
      race.reported = true;
      int after = count(view().character().progress(), race.key.replace("item.", ""));
      log("RACE " + race.number + " " + race.type + " RESULT: I now carry " + after + " of " + race.key + " (was " + race.carriedBefore + "); messages "
          + messagesSince(race.clickedAt) + "; drop still on the ground: " + (lootView(race.id) != null) + " | " + state());
      hud("race-" + race.number + "-" + tag.charAt(tag.length() - 1));
      signal("race-" + race.number + ".done-" + tag.charAt(tag.length() - 1));
    }
    if (race.reported && signaled("race-" + race.number + ".done-A") && signaled("race-" + race.number + ".done-B")) {
      racesDone++;
      lastRaceEnd = now;
      race = null;
    }
  }

  private void watchDrops(long now) {
    var v = view();
    for (var loot : v.snapshot().loot()) {
      if (dropsSeen.add(loot.id()))
        log("DROP seen: " + loot.key() + " id " + loot.id().toString().substring(0, 8) + " owner " + loot.ownerPlayerId() + " (own id " + ownIdText()
            + ") ownerRemainingMs " + loot.ownerRemainingMs());
    }
    if (now - lastDropLog > 10_000 && dropsSeen.size() <= 4) {
      lastDropLog = now;
      for (var loot : v.snapshot().loot())
        log("DROP countdown: " + loot.id().toString().substring(0, 8) + " " + loot.key() + " owner " + loot.ownerPlayerId() + " ownerRemainingMs " + loot.ownerRemainingMs());
    }
  }

  private long sessionStart;
  private long xpAtStart;

  private void soakLog(long now) {
    if (now - lastSoakLog < 60_000 || view() == null) return;
    lastSoakLog = now;
    var v = view();
    var rt = Runtime.getRuntime();
    log("SOAK t=" + (now - sessionStart) / 1000 + "s hp " + v.character().progress().vitals().hp() + "/" + v.character().progress().vitals().maxHp()
        + " minHp " + minHp + " xp+" + (v.character().experience() - xpAtStart) + " gold " + v.character().gold() + " attacks " + attacksSent
        + " drops " + dropsSeen.size() + " races " + racesDone + " connection " + connection().status() + " others " + others(game()).size()
        + " heapMB " + (rt.totalMemory() - rt.freeMemory()) / 1_000_000 + " revision " + v.character().revision());
  }

  /** Fight, idle and loot for {@code ms}; idle for 40 s of every 300 s, claim races whenever the leader plans one. */
  private void mixedSegment(String what, long ms) {
    // -Dbeta.scale shortens the session for a smoke test of the other chapters; the evidence run uses 1.
    double scale = Double.parseDouble(System.getProperty("beta.scale", "1"));
    segment(what, Math.round(ms * scale), this::mixedFrame);
  }

  private void mixedFrame() {
    var s = game();
    if (s == null || view() == null || view().snapshot() == null) return; // the connection is gone (a hard kill is in progress)
    long now = System.currentTimeMillis();
    if (sessionStart == 0) {
      sessionStart = now;
      xpAtStart = view().character().experience();
    }
    watchDrops(now);
    soakLog(now);
    planRace(now);
    followRace(s, now);
    var vit = view().character().progress().vitals();
    boolean idle = race == null && (now - sessionStart) % 300_000 >= 260_000 && vit.hp() * 100 >= vit.maxHp() * 50;
    if (race != null && race.position != null && !race.clicked && now >= race.at - 15_000 && distance(s, race.position) > 50f) {
      // Stand beside the drop before the agreed instant (drinking when hurt, no swings in the last seconds).
      drinkIfHurt(view(), now);
      walkToward(s, race.position, 40f);
    } else if (race != null && race.position != null && !race.clicked && now >= race.at - 15_000) {
      held.clear();
      drinkIfHurt(view(), now);
    } else {
      combatFrame(s, !idle, race == null ? null : race.position);
    }
  }

  // ---- chapter 6: death and individual corpse recovery

  private void deathChapter() {
    deathChapter(false);
  }

  /** {@code killRecover}: the operator hard-kills the server right after the first recovery request (T4C-0353); both windows then rejoin and Bravo retries. */
  private void deathChapter(boolean killRecover) {
    if (tag.endsWith("A")) {
      // Alpha keeps fighting near the corpse spot so the recovering player can reach it.
      final long[] began = {0};
      add(
          () -> {
            long now = System.currentTimeMillis();
            if (began[0] == 0) {
              began[0] = now;
              log("SEGMENT start: Alpha guards while Bravo dies and recovers | " + state());
            }
            var s = game();
            if (signaled(killRecover ? "B.rk-ready" : "B.recovered") || now - began[0] > 900_000) {
              held.clear();
              log("SEGMENT end: Alpha guard after " + (now - began[0]) / 1000 + " s | " + state());
              return true;
            }
            if (s != null) {
              soakLog(now);
              combatFrame(s, true, null);
            }
            return false;
          });
      if (killRecover) {
        // The operator hard-kills the server after Bravo's first recovery request: Alpha rejoins too and keeps guarding.
        restartCycle("rk", () -> log("M5 rk rejoined (Alpha): " + state()));
        repeat(
            "Bravo recovered after the restart",
            600_000,
            () -> {
              var s = game();
              if (s != null) {
                soakLog(System.currentTimeMillis());
                combatFrame(s, true, null);
              }
              return signaled("B.recovered");
            });
      }
      return;
    }
    final UUID[] corpse = {null};
    final Vector2[] corpsePoint = {null};
    final long[] diedAt = {0};
    action(
        () -> {
          noPotions = true;
          log("DEATH: Bravo stops fighting and drinking and waits among the Skeletons; " + state());
        });
    final int[] corpsesBefore = {0};
    action(() -> corpsesBefore[0] = view().corpses().size());
    repeat(
        "Bravo died (a new corpse is listed; the server respawns the character at once)",
        600_000,
        () -> {
          var s = game();
          held.clear();
          if (killRecover) {
            // T4C-0353: standing beside idle Skeletons for ten minutes did not kill a 278-hit-point character (they only retaliate),
            // so the M5 run keeps swinging, with no potions, until the death happens.
            if (s != null) combatFrame(s, true, null);
          } else if (s != null) {
            // Stand beside the nearest live monster and do nothing (no swings, no potions).
            BaseMonster m = nearestLive(s);
            if (m != null && distance(s, m.getPosition()) > 40f) walkToward(s, m.getPosition(), 30f);
          }
          return view().character().progress().vitals().hp() <= 0 || view().corpses().size() > corpsesBefore[0];
        });
    waitMs(2500);
    action(
        () -> {
          diedAt[0] = System.currentTimeMillis();
          var v = view();
          log("DEATH happened: hp " + v.character().progress().vitals().hp() + " corpses in view " + v.corpses() + "; " + state());
          shot("after-death");
          for (var c : v.corpses()) if (c.world() == v.map().world()) { corpse[0] = c.id(); corpsePoint[0] = new Vector2((float) ((c.x() - 0.5) * TILE_W), (float) ((c.y() - 0.5) * TILE_H)); log("CORPSE " + c); }
        });
    repeat(
        "respawned alive",
        60_000,
        () -> view().character().progress().vitals().hp() > 0 && game() != null);
    waitMs(3000);
    action(
        () -> {
          var v = view();
          var p = me(game()).getCoordinates();
          noPotions = false;
          potionStage = 0;
          // A player closes any panel left open (here the potion list) before walking on.
          press(Input.Keys.ESCAPE);
          for (var c : v.corpses()) if (c.world() == v.map().world() && corpse[0] == null) { corpse[0] = c.id(); corpsePoint[0] = new Vector2((float) ((c.x() - 0.5) * TILE_W), (float) ((c.y() - 0.5) * TILE_H)); }
          log("RESPAWNED: region " + v.map().key() + " at tile (" + (int) (p.getX() / TILE_W) + "," + (int) (p.getY() / TILE_H) + ") corpses " + v.corpses() + "; " + state());
          shot("respawned");
        });
    final long[] leaving = {0};
    final boolean[] recoverAccepted = {false};
    final int[] recoverTries = {0};
    final long[] recoverAt = {0};
    repeat(
        "walked back near the corpse (fighting on the way)",
        420_000,
        () -> {
          var s = game();
          if (s == null || corpsePoint[0] == null) return true;
          // The respawn spot in the temple: the route search oscillates there (A, S, A, S for minutes, seen in two runs), while
          // the keys on their own move freely (mortal diagnostic run), so a player first walks south out of the room.
          if (leaving[0] == 0) leaving[0] = System.currentTimeMillis();
          if (System.currentTimeMillis() - leaving[0] < 4000) {
            held.clear();
            held.add(Input.Keys.S);
            return false;
          }
          long now = System.currentTimeMillis();
          if (distance(s, corpsePoint[0]) < 44f) {
            // Within the server's recovery reach (two tiles of walking range): ask, and ask again if it says out of range.
            held.clear();
            if (killRecover && recoverTries[0] >= 1) return now - recoverAt[0] > 1200; // the kill step follows
            if (recoverAccepted[0] || recoverTries[0] >= 6) return true;
            if (now - recoverAt[0] > 3500 && corpse[0] != null) {
              recoverAt[0] = now;
              recoverTries[0]++;
              before = view().character().progress();
              if (killRecover) {
                write("kill-now-rk", corpse[0] + " " + now);
                log("M5 RK: the recovery request is handed to the connection now; kill marker written; carried " + inventorySummary(before));
              }
              log("RECOVER try " + recoverTries[0] + ": /recover through the connection (the screen has no corpse control) for " + corpse[0] + "; distance "
                  + Math.round(distance(s, corpsePoint[0])) + " px; " + state());
              connection().recover(corpse[0]).whenComplete((r, f) -> {
                if (r != null && r.accepted()) recoverAccepted[0] = true;
                log("RECOVER reply: " + (f != null ? "failure " + f : "accepted=" + (r != null && r.accepted()) + " key=" + (r == null ? null : r.messageKey())));
              });
            }
            return false;
          }
          BaseMonster m = nearestLive(s);
          boolean threatened = m != null && distance(s, m.getPosition()) < 3 * TILE_W;
          if (threatened) combatFrame(s, true, null);
          else {
            drinkIfHurt(view(), now);
            // Far away: walk to the known walkable hunting-ground tile; close by: straight to the corpse.
            if (distance(s, corpsePoint[0]) > 14 * TILE_W && goalTx != Integer.MIN_VALUE) walkToward(s, tileCenter(goalTx, goalTy), 40f);
            else walkToward(s, corpsePoint[0], 20f);
          }
          if (now - lastMessageLog > 20_000) {
            lastMessageLog = now;
            log("RETURN to the corpse: distance " + Math.round(distance(s, corpsePoint[0])) + " px; hp " + view().character().progress().vitals().hp()
                + "; threatened " + threatened + " nearest monster " + (m == null ? "none" : m.getName() + " at " + Math.round(distance(s, m.getPosition())) + " px")
                + "; route " + route.size() + "; " + movementDiagnostics());
          }
          return false;
        });
    action(
        () -> {
          held.clear();
          log("RECOVER finished after " + recoverTries[0] + " tries, accepted " + recoverAccepted[0]);
        });
    if (killRecover) {
      action(() -> signal("B.rk-ready"));
      restartCycle("rk", () -> log("M5 rk rejoined (Bravo): " + state() + "; corpses listed " + view().corpses()));
      final UUID[] retryId = {null};
      final long[] retryAt = {0};
      final boolean[] done = {false};
      repeat(
          "recovery retried after the restart (or the corpse was already recovered)",
          300_000,
          () -> {
            var s = game();
            if (s == null) return false;
            long now = System.currentTimeMillis();
            var v = view();
            boolean listed = false;
            for (var c : v.corpses()) if (c.id().equals(corpse[0])) listed = true;
            if (!listed) {
              log("M5 RK RETRY: the corpse is no longer listed (the kill landed after the commit, or it was recovered): " + state());
              return true;
            }
            if (now - retryAt[0] > 3500) {
              retryAt[0] = now;
              held.clear();
              if (distance(s, corpsePoint[0]) > 40f) {
                walkToward(s, corpsePoint[0], 20f);
                retryAt[0] = now - 3000;
                return false;
              }
              log("M5 RK RETRY: /recover again after the restart; " + state());
              connection().recover(corpse[0]).whenComplete((r, f) -> {
                if (r != null && r.accepted()) done[0] = true;
                log("M5 RK RETRY reply: " + (f != null ? "failure " + f : "accepted=" + (r != null && r.accepted()) + " key=" + (r == null ? null : r.messageKey())));
              });
            }
            return done[0];
          });
    }
    waitMs(4000);
    action(
        () -> {
          var v = view();
          log("RECOVER after: " + state() + "; corpses still listed " + v.corpses() + "; carried before " + inventorySummary(before));
          shot("after-recover");
          signal("B.recovered");
        });
  }

  private boolean solo() {
    return scene.equals("turnin") || scene.equals("mortal");
  }

  /** What the screen knows about its own movement after a respawn (diagnostics for the death chapter). */
  private String movementDiagnostics() {
    var s = game();
    if (s == null) return "no screen";
    var v = view();
    var p = me(s).getCoordinates();
    long confirmed = -1;
    try {
      var f = MainGameScreen.class.getDeclaredField("confirmedRemoteEpoch");
      f.setAccessible(true);
      confirmed = f.getLong(s);
    } catch (ReflectiveOperationException ignored) {
    }
    var own = ownActor();
    return "tile (" + (int) (p.getX() / TILE_W) + "," + (int) (p.getY() / TILE_H) + ") keys " + held + " map epoch " + v.map().locationEpoch()
        + " snapshot epoch " + (v.snapshot().region() == null ? "none" : v.snapshot().region().locationEpoch()) + " confirmed epoch " + confirmed
        + " status " + connection().status() + " server actor " + (own == null ? "none" : own.x() + "," + own.y()) + " hp " + v.character().progress().vitals().hp();
  }

  /** Solo diagnostic scene: die, then try to walk after the respawn. */
  private void mortalScene() {
    final int[] corpsesBefore = {0};
    action(() -> {
      corpsesBefore[0] = view().corpses().size();
      log("MORTAL start: " + movementDiagnostics());
    });
    repeat("died", 120_000, () -> view().corpses().size() > corpsesBefore[0] || view().character().progress().vitals().hp() <= 0 && view().corpses().size() > 0);
    waitMs(3000);
    action(() -> {
      log("MORTAL after death: " + movementDiagnostics() + " corpses " + view().corpses());
      shot("mortal-after-death");
    });
    final long[] began = {0};
    final int[] keys = {Input.Keys.S, Input.Keys.A, Input.Keys.W, Input.Keys.D, Input.Keys.S};
    repeat("walk attempt after the respawn (each direction for 5 s)", 26_000, () -> {
      var s = game();
      if (s == null) return false;
      long now = System.currentTimeMillis();
      if (began[0] == 0) began[0] = now;
      int phase = (int) Math.min(keys.length - 1, (now - began[0]) / 5200);
      held.clear();
      held.add(keys[phase]);
      if (now - lastMessageLog > 1000) {
        lastMessageLog = now;
        log("MORTAL walking key " + Input.Keys.toString(keys[phase]) + ": " + movementDiagnostics());
      }
      return false;
    });
    action(() -> {
      held.clear();
      log("MORTAL end: " + movementDiagnostics());
    });
  }

  // ---- the scene

  private void betaScene() {
    final long[] t = {0};
    skeletonsOnly = true;
    action(
        () -> {
          var s = game();
          t[0] = System.currentTimeMillis();
          log("CENSUS: monsters drawn " + monsters(s).size() + ", npcs " + npcs(s).size() + ", ground items " + groundItems(s).size() + ", own look " + lookOf(ownActor())
              + ", other look " + lookOf(otherActor()) + " | " + drawnOther(s));
        });
    // -Dbeta.from=ground skips the first three chapters (a smoke test of the later ones only).
    if (!"ground".equals(System.getProperty("beta.from", "")) && !"death".equals(System.getProperty("beta.from", ""))) {
      topicChapter();
      equipRound("A");
      equipRound("B");
      if ("equip".equals(System.getProperty("beta.until", ""))) return; // a short run of the first two chapters only
      // Portals: the Lighthaven door pair, each character in turn, the other window watching.
      portalHop("A", 2931, 1056, "start", "lighthaven-interior", true, "1-A-into-Lighthaven");
      portalHop("B", 2931, 1056, "start", "lighthaven-interior", false, "2-B-into-Lighthaven");
      action(
          () -> {
            log("BOTH in Lighthaven: region " + view().map().key() + " others drawn " + others(game()).size() + " " + drawnOther(game()));
            shot("both-in-lighthaven");
          });
      waitMs(4000);
      portalHop("A", 306, 381, "lighthaven-interior", "start", true, "3-A-out-of-Lighthaven");
      portalHop("B", 306, 381, "lighthaven-interior", "start", false, "4-B-out-of-Lighthaven");
    }
    walkToGround();
    action(() -> signal(tag + ".at-ground"));
    until("peer at the ground", 240_000, () -> signaled(peer() + ".at-ground"));
    if (!"death".equals(System.getProperty("beta.from", ""))) {
      mixedSegment("mixed session part 1 (fight, idle, loot races)", 540_000);
      // Cave mouth: both walk to the mouth, Alpha goes in first.
      portalHop("A", 2770, 1095, "start", "skeleton-cave", true, "5-A-into-the-cave");
      portalHop("B", 2770, 1095, "start", "skeleton-cave", false, "6-B-into-the-cave");
      action(
          () -> {
            log("BOTH in the cave: region " + view().map().key() + " others drawn " + others(game()).size() + " " + drawnOther(game()));
            shot("both-in-cave");
          });
      waitMs(6000);
      portalHop("A", 136, 665, "skeleton-cave", "start", true, "7-A-out-of-the-cave");
      portalHop("B", 136, 665, "skeleton-cave", "start", false, "8-B-out-of-the-cave");
      mixedSegment("mixed session part 2 (fight, idle, loot races)", 600_000);
    }
    deathChapter();
    action(
        () -> {
          log("BETA SESSION SUMMARY: " + state() + " minHp " + minHp + " attacks " + attacksSent + " drops seen " + dropsSeen.size() + " races " + racesDone
              + " connection " + connection().status() + " elapsed " + (System.currentTimeMillis() - t0) / 1000 + " s");
          shot("final");
        });
  }

  // ---------------------------------------------------------------- m4 scene (T4C-0331)
  //
  // Two DIFFERENT roles (tag ending A: the melee role, tag ending B: the caster) fight the Skeleton ground together through the
  // screens' own handlers: both contend for one creature life at a time (damage and experience attribution, loot ownership),
  // the caster casts a creature-target spell through C / digit after a click selected the creature, heals itself, lays a
  // timed buff, the melee role is refused with a bow, a provoked creature is left behind through a portal, the server
  // is restarted (buff persistence), one character leaves while its target lives on, and one dies in front of the other.
  // Everything is a key or a click on the screen's input processor; the few logged exceptions say so.

  private long m4Target = -1;
  private int m4TargetHp = -2;
  private boolean m4Seen;
  private long m4LifeXp0;
  private long m4LifeStart;
  private long m4LastCast;
  private long m4LastSelect;
  private long m4LastHeal;
  private long healPendingAt;
  private int healHp0, healMana0;
  private int castStage;
  private long castAt;
  private String castFragment;
  private int monHp = -1;
  private int hitsTaken;
  private long lastHitAt;
  private long damageTaken;
  private int castsPressed;
  private final Set<Long> m4Done = new HashSet<>();
  private final Set<Integer> m4ShotLives = new HashSet<>();
  private boolean retaliationCropTaken;

  private int creatureHp(long id) {
    var v = view();
    if (v == null || v.snapshot() == null) return -1;
    for (var c : v.snapshot().creatures()) if (c.id() == id) return c.health();
    return -1;
  }

  private com.perso.T4C.session.RemoteWorldDriver remoteWorld(MainGameScreen s) {
    var world = ((com.perso.T4C.session.GameSession) field(s, MainGameScreen.class, "session")).world();
    return world instanceof com.perso.T4C.session.RemoteWorldDriver r ? r : null;
  }

  private BaseMonster monsterById(MainGameScreen s, long id) {
    var r = remoteWorld(s);
    if (r == null) return null;
    for (BaseMonster m : monsters(s)) if (r.creatureId(m) == id) return m;
    return null;
  }

  private String buffDetail() {
    var p = view().character().progress();
    long now = System.currentTimeMillis();
    StringBuilder b = new StringBuilder();
    for (var buff : p.buffs()) b.append(buff.spell()).append(" expiresIn=").append(buff.expiresAt() - now).append("ms ");
    p.cooldowns().forEach((k, v) -> b.append(k).append('=').append(v - now).append("ms "));
    return b.length() == 0 ? "(no buffs, no cooldowns)" : b.toString().trim();
  }

  /** Non-blocking cast through the screen's own C list: opens it, then presses the digit of the row containing the fragment. */
  private boolean castStep(String fragment, long now) {
    if (castStage == 0) {
      if (abilityMenu().isOpen()) {
        press(Input.Keys.ESCAPE);
        return false;
      }
      press(Input.Keys.C);
      castStage = 1;
      castAt = now;
      castFragment = fragment;
      return false;
    }
    if (castStage == 1 && now - castAt > 450) {
      var lines = abilityMenu().lines(view().character().progress());
      int digit = -1;
      for (int i = 1; i < lines.size() - 1; i++)
        if (lines.get(i).toLowerCase(Locale.ROOT).contains(castFragment)) {
          digit = i;
          break;
        }
      castStage = 0;
      castAt = now;
      if (digit > 0 && abilityMenu().isOpen()) {
        press(digitKey(digit));
        castsPressed++;
        return true;
      }
      log("CAST " + castFragment + ": not offered by the list " + lines);
      press(Input.Keys.ESCAPE);
    }
    return false;
  }

  /** Per-frame monitor of the committed hit points: every drop (a creature's retaliation) and rise is logged. */
  private void m4Monitor() {
    var v = view();
    if (v == null || v.character() == null || game() == null) return;
    int hp = v.character().progress().vitals().hp();
    int max = v.character().progress().vitals().maxHp();
    if (monHp >= 0 && hp < monHp) {
      hitsTaken++;
      lastHitAt = System.currentTimeMillis();
      damageTaken += monHp - hp;
      var s = game();
      BaseMonster near = nearestLive(s);
      String who = near == null ? "none" : near.getName() + " at " + Math.round(distance(s, near.getPosition())) + " px";
      log("HP DOWN " + monHp + " -> " + hp + "/" + max + " (committed); nearest creature " + who + "; hits so far " + hitsTaken + " total " + damageTaken);
      if (!retaliationCropTaken && near != null && distance(s, near.getPosition()) < 90 && hp > 0) {
        retaliationCropTaken = true;
        cropOf("retaliation", me(s).getPositionVector(), 360, 280);
      }
    } else if (monHp >= 0 && hp > monHp && hp - monHp >= Math.max(3, max / 25)) {
      log("HP UP " + monHp + " -> " + hp + "/" + max + " (committed)");
    }
    monHp = hp;
    if (watchId > 0 && System.currentTimeMillis() - watchAt > 2000) {
      watchAt = System.currentTimeMillis();
      var s = game();
      var t = monsterById(s, watchId);
      log("WATCH creature " + watchId + " hp " + creatureHp(watchId) + (t == null ? " not drawn" : " at (" + Math.round(t.getPosition().x) + "," + Math.round(t.getPosition().y)
          + ") distance to me " + Math.round(distance(s, t.getPosition()))) + " region " + v.map().key());
    }
  }

  private long watchId = -1;
  private long watchAt;

  private void m4Census() {
    action(
        () -> {
          var p = view().character().progress();
          log("M4 ROLE " + p.characterClass() + " level " + p.advancement().level() + " attributes " + p.attributes() + " spells " + p.spells()
              + " | " + state());
          press(Input.Keys.C);
        });
    waitMs(800);
    action(
        () -> {
          log("M4 SPELL LIST " + abilityMenu().lines(view().character().progress()));
          hud("spell-list-" + tag.charAt(tag.length() - 1));
          press(Input.Keys.U);
        });
    waitMs(800);
    action(
        () -> {
          log("M4 POTION LIST " + abilityMenu().lines(view().character().progress()));
          press(Input.Keys.ESCAPE);
        });
  }

  /** The caster lays a timed buff on itself (a different role from the melee role, which has no spell). */
  private void m4Buff() {
    if (!tag.endsWith("B")) {
      until("the caster laid its buff", 120_000, () -> signaled("m4.buff-done"));
      return;
    }
    final String[] before = {null};
    // Heal on self first: the provisioned Healer starts wounded, so the heal visibly restores health and spends mana.
    action(
        () -> {
          var vit = view().character().progress().vitals();
          healHp0 = vit.hp();
          healMana0 = vit.mana();
          log("HEAL (explicit): before hp " + vit.hp() + "/" + vit.maxHp() + " mana " + vit.mana() + "/" + vit.maxMana() + " | " + buffDetail());
          press(Input.Keys.C);
        });
    waitMs(800);
    action(
        () -> {
          var lines = abilityMenu().lines(view().character().progress());
          int digit = -1;
          for (int i = 1; i < lines.size() - 1; i++) if (lines.get(i).toLowerCase(Locale.ROOT).contains("heal")) digit = i;
          log("HEAL: spell list " + lines + " -> digit " + digit);
          if (digit > 0) press(digitKey(digit));
        });
    waitMs(3000);
    action(
        () -> {
          var vit = view().character().progress().vitals();
          log("HEAL (explicit): after hp " + healHp0 + " -> " + vit.hp() + "/" + vit.maxHp() + ", mana " + healMana0 + " -> " + vit.mana() + " | " + buffDetail());
          hud("heal-cast");
        });
    action(
        () -> {
          before[0] = state();
          log("BUFF before: " + buffDetail() + " | " + before[0]);
          press(Input.Keys.C);
        });
    waitMs(800);
    action(
        () -> {
          var lines = abilityMenu().lines(view().character().progress());
          int digit = -1;
          for (int i = 1; i < lines.size() - 1; i++) if (lines.get(i).toLowerCase(Locale.ROOT).contains("protect")) digit = i;
          log("BUFF: spell list " + lines + " -> digit " + digit);
          if (digit > 0) press(digitKey(digit));
        });
    waitMs(3000);
    action(
        () -> {
          log("BUFF after:  " + buffDetail() + " | " + state());
          hud("buff-cast");
          signal("m4.buff-done");
        });
  }

  private void m4Pick(int life) {
    repeat(
        "announce target " + life,
        90_000,
        () -> {
          if (!tag.endsWith("A")) return Files.exists(sync.resolve("m4-target-" + life));
          var s = game();
          var r = s == null ? null : remoteWorld(s);
          if (r == null) return false;
          held.clear();
          BaseMonster best = null;
          double bestDistance = Double.MAX_VALUE;
          for (BaseMonster m : monsters(s)) {
            if (m.isDead() || m.getHealth() <= 0 || !isSkeleton(m)) continue;
            long id = r.creatureId(m);
            if (id <= 0 || m4Done.contains(id) || creatureHp(id) <= 0) continue;
            double d = distance(s, m.getPosition());
            if (d < bestDistance) {
              bestDistance = d;
              best = m;
            }
          }
          if (best == null) return false;
          try {
            Files.writeString(sync.resolve("m4-target-" + life), String.valueOf(r.creatureId(best)));
          } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
          }
          return true;
        });
    action(
        () -> {
          try {
            m4Target = Long.parseLong(Files.readString(sync.resolve("m4-target-" + life)).trim());
          } catch (java.io.IOException | RuntimeException e) {
            m4Target = -1;
          }
          m4Seen = false;
          m4TargetHp = -2;
          m4LifeStart = System.currentTimeMillis();
          m4LifeXp0 = view().character().experience();
          log("LIFE " + life + " START: creature " + m4Target + " hp " + creatureHp(m4Target) + " | my xp " + m4LifeXp0 + " hp "
              + view().character().progress().vitals().hp() + " | " + state());
        });
  }

  /** One frame of the caster against the shared target: stay at spell distance, select, cast, heal itself when hurt, drink when low. */
  private void casterFrame(MainGameScreen s, BaseMonster t, long now) {
    var v = view();
    var vit = v.character().progress().vitals();
    if (healPendingAt > 0 && now - healPendingAt > 2800) {
      log("HEAL result: hp " + healHp0 + " -> " + vit.hp() + "/" + vit.maxHp() + ", mana " + healMana0 + " -> " + vit.mana() + "/" + vit.maxMana() + " | " + buffDetail());
      healPendingAt = 0;
    }
    if (castStage != 0) {
      castStep(castFragment, now);
      return;
    }
    if (potionStage != 0) {
      drinkIfHurt(v, now);
      return;
    }
    if (vit.hp() > 0 && vit.hp() * 100 < vit.maxHp() * 35 && now - m4LastHeal > 2500) {
      drinkIfHurt(v, now);
      return;
    }
    double d = distance(s, t.getPosition());
    float stand = 3.5f * TILE_W;
    if (d > stand) walkToward(s, t.getPosition(), stand);
    else held.clear();
    if (vit.hp() > 0 && vit.hp() * 100 < vit.maxHp() * 65 && now - m4LastHeal > 5000 && vit.mana() >= 2) {
      m4LastHeal = now;
      healPendingAt = now;
      healHp0 = vit.hp();
      healMana0 = vit.mana();
      log("HEAL: casting the self heal through C / digit at hp " + vit.hp() + "/" + vit.maxHp() + " mana " + vit.mana());
      castStep("heal", now);
      return;
    }
    Object selected = field(s, MainGameScreen.class, "selectedMonster");
    if (selected != t && now - m4LastSelect > 3500 && d <= 8 * TILE_W) {
      m4LastSelect = now;
      Vector2 at = aim(t.getPosition(), p -> t.isMouseOver(p.x, p.y));
      if (at != null) {
        click(s, at.x, at.y);
        log("SELECT: clicked the creature (monster click handler: selects it and sends the melee attack, refused out of reach)");
      }
      return;
    }
    if (selected == t && d <= 8 * TILE_W && now - m4LastCast > 3300 && vit.mana() >= 2) {
      m4LastCast = now;
      log("CAST: attack spell on the selected creature at " + Math.round(d) + " px, creature hp " + creatureHp(m4Target) + " | mana " + vit.mana());
      castStep(System.getProperty("m4.spell", "stone"), now);
    }
  }

  /** The caster (a Mage with Fireball written into its spells by the operator) casts an area attack at a creature with others beside it. */
  private void m4Area() {
    if (!tag.endsWith("B")) {
      repeat("the caster finished the area check", 240_000, () -> {
        if (game() != null) drinkIfHurt(view(), System.currentTimeMillis());
        return signaled("m4.area-done");
      });
      return;
    }
    final int[] casts = {0};
    final long[] began = {0};
    final long[] castTime = {0};
    final java.util.Map<Long, Integer> hpBefore = new java.util.HashMap<>();
    repeat(
        "area spell cast twice",
        200_000,
        () -> {
          var s = game();
          if (s == null) return false;
          long now = System.currentTimeMillis();
          if (began[0] == 0) began[0] = now;
          if (castStage != 0) {
            castStep(castFragment, now);
            return false;
          }
          if (castTime[0] > 0 && now - castTime[0] > 3500) {
            StringBuilder sb = new StringBuilder();
            for (var c : view().snapshot().creatures()) {
              Integer before = hpBefore.get(c.id());
              if (before != null) sb.append(c.id()).append(':').append(before).append("->").append(c.health()).append("/").append(c.maxHealth()).append(' ');
            }
            log("AREA result " + casts[0] + ": creatures near the target (id:before->after) " + sb + "| " + state());
            hud("area-cast-" + casts[0]);
            castTime[0] = 0;
            hpBefore.clear();
            if (casts[0] >= 2) return true;
          }
          if (castTime[0] > 0) return false;
          var vit = view().character().progress().vitals();
          if (vit.hp() * 100 < vit.maxHp() * 40) {
            drinkIfHurt(view(), now);
            return false;
          }
          BaseMonster t = nearestLive(s);
          if (t == null) return now - began[0] > 150_000;
          double d = distance(s, t.getPosition());
          if (d > 4.5f * TILE_W) {
            walkToward(s, t.getPosition(), 4f * TILE_W);
            return false;
          }
          held.clear();
          Object selected = field(s, MainGameScreen.class, "selectedMonster");
          if (selected != t) {
            Vector2 at = aim(t.getPosition(), p -> t.isMouseOver(p.x, p.y));
            if (at != null) click(s, at.x, at.y);
            return false;
          }
          if (now - m4LastCast < 3500 || vit.mana() < 10) return false;
          m4LastCast = now;
          long id = remoteWorld(s).creatureId(t);
          double tx = 0, ty = 0;
          for (var c : view().snapshot().creatures()) if (c.id() == id) { tx = c.x(); ty = c.y(); }
          hpBefore.clear();
          int near = 0;
          for (var c : view().snapshot().creatures())
            if (Math.max(Math.abs(c.x() - tx), Math.abs(c.y() - ty)) <= 7 && c.health() > 0) { hpBefore.put(c.id(), c.health()); near++; }
          casts[0]++;
          castTime[0] = now;
          log("AREA cast " + casts[0] + ": fireball with the creature " + id + " selected, " + near + " live creatures within 7 tiles of it");
          castStep("fireball", now);
          return false;
        });
    action(() -> {
      held.clear();
      signal("m4.area-done");
    });
  }

  /** A melee role that knows a heal (the Paladin) heals itself through the C list when hurt; true when the frame was used for that. */
  private boolean meleeHealStep(long now) {
    var p = view().character().progress();
    if (p.spells().isEmpty()) return false;
    var vit = p.vitals();
    if (healPendingAt > 0 && now - healPendingAt > 2800) {
      log("HEAL result: hp " + healHp0 + " -> " + vit.hp() + "/" + vit.maxHp() + ", mana " + healMana0 + " -> " + vit.mana() + "/" + vit.maxMana() + " | " + buffDetail());
      healPendingAt = 0;
    }
    if (castStage != 0) {
      castStep(castFragment, now);
      return true;
    }
    if (vit.hp() > 0 && vit.hp() * 100 < vit.maxHp() * 65 && now - m4LastHeal > 5000 && vit.mana() >= 2) {
      m4LastHeal = now;
      healPendingAt = now;
      healHp0 = vit.hp();
      healMana0 = vit.mana();
      held.clear();
      log("HEAL: the melee role casts its self heal through C / digit at hp " + vit.hp() + "/" + vit.maxHp() + " mana " + vit.mana());
      castStep("heal", now);
      return true;
    }
    return false;
  }

  private long resetCreature = -1;

  private void m4Contention(int lives) {
    for (int n = 1; n <= lives; n++) {
      final int life = n;
      m4Pick(life);
      repeat(
          "life " + life + " ended",
          240_000,
          () -> {
            var s = game();
            if (s == null) return false;
            long now = System.currentTimeMillis();
            int hp = creatureHp(m4Target);
            if (hp > 0) m4Seen = true;
            if (hp != m4TargetHp) {
              log("LIFE " + life + ": creature " + m4Target + " hp " + m4TargetHp + " -> " + hp + " | my xp +" + (view().character().experience() - m4LifeXp0)
                  + " my hp " + view().character().progress().vitals().hp());
              m4TargetHp = hp;
            }
            if ((m4Seen && hp <= 0) || (!m4Seen && now - m4LifeStart > 6000)) {
              held.clear();
              return true;
            }
            BaseMonster t = monsterById(s, m4Target);
            if (t == null) {
              held.clear();
              return false;
            }
            if (tag.endsWith("A")) {
              if (!meleeHealStep(now)) combatFrame(s, true, t.getPosition());
            } else casterFrame(s, t, now);
            return false;
          });
      action(
          () -> {
            held.clear();
            m4Done.add(m4Target);
            var v = view();
            StringBuilder loot = new StringBuilder();
            for (var l : v.snapshot().loot())
              loot.append(l.key()).append(" owner=").append(l.ownerPlayerId() == null ? "null" : l.ownerPlayerId().equals(v.ownId()) ? "ME" : "other")
                  .append(" remainingMs=").append(l.ownerRemainingMs()).append("; ");
            log("LIFE " + life + " END: creature " + m4Target + " | my xp gained this life +" + (v.character().experience() - m4LifeXp0) + " gold "
                + v.character().gold() + " | drops in view: " + (loot.length() == 0 ? "none" : loot) + "| hits taken so far " + hitsTaken + " dmg " + damageTaken
                + " | " + state());
            signal(tag + ".life-" + life);
          });
      until("peer finished life " + life, 60_000, () -> signaled(peer() + ".life-" + life));
      waitMs(2500);
      action(() -> log("LIFE " + life + " AFTER: my xp +" + (view().character().experience() - m4LifeXp0) + " | drops in view " + view().snapshot().loot().size()));
    }
  }

  /** The melee role equips the bow it carries (it has no quiver) and swings at a creature: the server must refuse, clearly. */
  private void m4Refusal() {
    boolean archer = System.getProperty("m4.pair", "").startsWith("archer");
    if (tag.endsWith("A")) {
      action(() -> press(Input.Keys.I));
      waitMs(900);
      if (archer) {
        // The Archer wears its bow and quiver; taking the quiver off leaves a bow with no arrows holder: not a supported attack.
        inventoryPickRow(row -> row.kind() == com.perso.T4C.online.OnlineInventoryPanel.Kind.EQUIPPED && row.item().key().contains("quiver"), "unequip-quiver", null);
      } else {
        // The Warrior cannot wear the Archer's bow (agility requirement): the equip itself is refused with its own text.
        inventoryPickRow(row -> row.kind() == com.perso.T4C.online.OnlineInventoryPanel.Kind.CARRIED && row.item().key().contains("bow"), "equip-bow", null);
      }
      action(() -> press(Input.Keys.ESCAPE));
      waitMs(600);
      final long[] began = {0};
      repeat(
          "attacked with the bow equipped",
          40_000,
          () -> {
            var s = game();
            if (s == null) return false;
            long now = System.currentTimeMillis();
            if (began[0] == 0) {
              began[0] = now;
              log("REFUSAL: bow equipped, no quiver | " + state());
            }
            BaseMonster t = nearestLive(s);
            if (t == null) return now - began[0] > 30_000;
            double d = distance(s, t.getPosition());
            float reach = GameConstants.PLAYER_MELEE_ATTACK_RANGE - 14f;
            if (d > reach) walkToward(s, t.getPosition(), reach);
            else held.clear();
            if (d <= reach + 8 && now - lastClick > 900) {
              Vector2 at = aim(t.getPosition(), p -> t.isMouseOver(p.x, p.y));
              if (at != null) {
                click(s, at.x, at.y);
                log("REFUSAL: clicked " + t.getName() + " at " + Math.round(d) + " px with the bow equipped");
              }
            }
            return messagesSince(began[0]).stream().filter(m -> m.toLowerCase(Locale.ROOT).contains("cannot") || m.toLowerCase(Locale.ROOT).contains("unsupported")
                || m.toLowerCase(Locale.ROOT).contains("not supported")).count() >= 2 || now - began[0] > 30_000;
          });
      action(
          () -> {
            held.clear();
            log("REFUSAL messages: " + messagesSince(0).stream().filter(m -> !m.startsWith("HP")).toList());
            hud("refusal-bow");
            press(Input.Keys.I);
          });
      waitMs(900);
      if (archer) {
        inventoryPickRow(row -> row.kind() == com.perso.T4C.online.OnlineInventoryPanel.Kind.CARRIED && row.item().key().contains("quiver"), "re-equip-quiver", null);
        action(() -> press(Input.Keys.ESCAPE));
        waitMs(800);
        // With the quiver back the same swing is a supported bow attack: the creature loses health and the Archer gains experience.
        final long[] again = {0};
        final long[] xp0 = {0};
        final int[] hpSeen = {0};
        repeat(
            "bow attack with the quiver back on",
            30_000,
            () -> {
              var s = game();
              if (s == null) return false;
              long now = System.currentTimeMillis();
              if (again[0] == 0) {
                again[0] = now;
                xp0[0] = view().character().experience();
                log("REFUSAL: quiver worn again | " + state());
              }
              BaseMonster t = nearestLive(s);
              if (t == null) return now - again[0] > 25_000;
              double d = distance(s, t.getPosition());
              if (d > 6 * TILE_W) walkToward(s, t.getPosition(), 5 * TILE_W);
              else held.clear();
              if (d <= 8 * TILE_W && now - lastClick > 900) {
                Vector2 at = aim(t.getPosition(), p -> t.isMouseOver(p.x, p.y));
                if (at != null) click(s, at.x, at.y);
              }
              return view().character().experience() > xp0[0] + 5 || now - again[0] > 25_000;
            });
        action(
            () -> {
              held.clear();
              log("REFUSAL with the quiver: experience gained " + (view().character().experience()) + " | messages " + messagesSince(0).stream().filter(m -> !m.startsWith("HP")).toList() + " | " + state());
            });
      } else {
        inventoryPickRow(row -> row.kind() == com.perso.T4C.online.OnlineInventoryPanel.Kind.CARRIED && row.item().key().contains("greatsword"), "re-equip-greatsword", null);
      }
      action(
          () -> {
            press(Input.Keys.ESCAPE);
            signal("m4.refusal-done");
          });
    } else if (archer) {
      // The Healer carries a flag of a quest the online game does not run: its attack and its cast at a creature must be refused.
      final long[] began = {0};
      final boolean[] sent = {false};
      repeat(
          "attacked and cast with an unsupported quest flag",
          60_000,
          () -> {
            var s = game();
            if (s == null) return false;
            long now = System.currentTimeMillis();
            if (began[0] == 0) {
              began[0] = now;
              log("FLAG REFUSAL: quest flags " + view().character().progress().questFlags());
            }
            BaseMonster t = nearestLive(s);
            if (t == null) return now - began[0] > 50_000;
            double d = distance(s, t.getPosition());
            if (d > 3.5f * TILE_W) {
              walkToward(s, t.getPosition(), 3f * TILE_W);
              return false;
            }
            held.clear();
            Object selected = field(s, MainGameScreen.class, "selectedMonster");
            if (selected != t && now - m4LastSelect > 2500) {
              m4LastSelect = now;
              Vector2 at = aim(t.getPosition(), p -> t.isMouseOver(p.x, p.y));
              if (at != null) {
                click(s, at.x, at.y);
                log("FLAG REFUSAL: clicked the creature (attack) at " + Math.round(d) + " px");
              }
              return false;
            }
            if (castStage != 0) {
              castStep(castFragment, now);
              return false;
            }
            if (!sent[0] && selected == t) {
              sent[0] = true;
              log("FLAG REFUSAL: casting the attack spell at the selected creature");
              castStep("stone", now);
            }
            return sent[0] && castStage == 0 && now - m4LastSelect > 2500 && now - began[0] > 10_000 || now - began[0] > 50_000;
          });
      action(
          () -> {
            log("FLAG REFUSAL messages: " + messagesSince(0).stream().filter(m -> !m.startsWith("HP")).toList());
            hud("refusal-flag");
          });
      until("the melee role finished the refusal check", 180_000, () -> signaled("m4.refusal-done"));
    } else {
      repeat("the melee role finished the refusal check", 180_000, () -> {
        var s = game();
        if (s != null) drinkIfHurt(view(), System.currentTimeMillis());
        return signaled("m4.refusal-done");
      });
    }
  }

  /** The melee role provokes a creature, walks into the cave and back: the creature must reset (no heal, no new life). */
  private void m4Reset() {
    final long[] provoked = {-1};
    final int[] hpAfterHit = {0};
    final Vector2[] posAfterHit = {null};
    if (tag.endsWith("A")) {
      final long[] began = {0};
      repeat(
          "provoked and wounded a creature",
          60_000,
          () -> {
            var s = game();
            if (s == null) return false;
            long now = System.currentTimeMillis();
            if (began[0] == 0) began[0] = now;
            if (provoked[0] < 0) {
              var r = remoteWorld(s);
              BaseMonster best = null;
              double bd = Double.MAX_VALUE;
              for (BaseMonster m : monsters(s)) {
                if (m.isDead() || m.getHealth() <= 0 || !isSkeleton(m)) continue;
                long id = r.creatureId(m);
                if (id <= 0 || creatureHp(id) <= 0) continue;
                double d = distance(s, m.getPosition());
                if (d < bd) { bd = d; best = m; }
              }
              if (best == null) return now - began[0] > 50_000;
              provoked[0] = r.creatureId(best);
              watchId = provoked[0];
              resetCreature = provoked[0];
              log("RESET: provoking creature " + provoked[0] + " hp " + creatureHp(provoked[0]));
            }
            BaseMonster t = monsterById(s, provoked[0]);
            int hp = creatureHp(provoked[0]);
            if (t == null || hp <= 0) return true;
            int max = 0;
            for (var c : view().snapshot().creatures()) if (c.id() == provoked[0]) max = c.maxHealth();
            if (hp < max && hp * 100 < max * 85) {
              hpAfterHit[0] = hp;
              posAfterHit[0] = new Vector2(t.getPosition());
              return true;
            }
            combatFrame(s, true, t.getPosition());
            return false;
          });
      action(
          () -> {
            held.clear();
            log("RESET: creature " + provoked[0] + " wounded to hp " + hpAfterHit[0] + " at " + posAfterHit[0] + "; leaving through the cave mouth | " + state());
            signal("m4.reset-provoked");
          });
      portalHop("A", 2770, 1095, "start", "skeleton-cave", true, "m4-A-into-cave");
      waitMs(8000);
      portalHop("A", 136, 665, "skeleton-cave", "start", true, "m4-A-out-of-cave");
      repeat(
          "observed the wounded creature after returning",
          30_000,
          () -> {
            var s = game();
            if (s == null) return false;
            long now = System.currentTimeMillis();
            if (now - lastMessageLog > 5000) {
              lastMessageLog = now;
              var t = monsterById(s, provoked[0]);
              log("RESET: after returning, creature " + provoked[0] + " hp " + creatureHp(provoked[0]) + " (was " + hpAfterHit[0] + ") at "
                  + (t == null ? "not drawn" : t.getPosition().toString()) + " (was " + posAfterHit[0] + "); distance to me "
                  + (t == null ? "n/a" : String.valueOf(Math.round(distance(s, t.getPosition())))));
            }
            return false;
          });
      action(() -> {
        watchId = -1;
        signal("m4.reset-done");
      });
    } else {
      portalHop("A", 2770, 1095, "start", "skeleton-cave", true, "m4-A-into-cave");
      portalHop("A", 136, 665, "skeleton-cave", "start", true, "m4-A-out-of-cave");
      repeat("the other window finished the reset check", 120_000, () -> {
        if (game() != null) drinkIfHurt(view(), System.currentTimeMillis());
        return signaled("m4.reset-done");
      });
    }
  }

  /** Restart cycle for both windows, with the buff, cooldowns and mana compared across the rejoin. */
  private void m4Restart() {
    action(() -> log("RESTART: before, committed state: " + buffDetail() + " | " + state() + " | wounded creature " + resetCreature + " hp " + creatureHp(resetCreature)));
    restartCycle("m4", () -> log("RESTART: after the rejoin, committed state: " + buffDetail() + " | " + state()));
    waitMs(2500);
    action(() -> log("RESTART: 2.5 s later: " + buffDetail() + " | " + state() + " | wounded creature " + resetCreature + " hp " + creatureHp(resetCreature)));
  }

  /** The melee role leaves (return to lobby) while a wounded creature targets it; the caster must be able to kill that creature. */
  private void m4Disconnect() {
    final long[] victim = {-1};
    if (tag.endsWith("A")) {
      final long[] began = {0};
      repeat(
          "wounded a creature before leaving",
          60_000,
          () -> {
            var s = game();
            if (s == null) return false;
            long now = System.currentTimeMillis();
            if (began[0] == 0) began[0] = now;
            if (victim[0] < 0) {
              var r = remoteWorld(s);
              BaseMonster best = null;
              double bd = Double.MAX_VALUE;
              for (BaseMonster m : monsters(s)) {
                if (m.isDead() || m.getHealth() <= 0 || !isSkeleton(m)) continue;
                long id = r.creatureId(m);
                if (id <= 0 || creatureHp(id) <= 0 || m4Done.contains(id)) continue;
                double d = distance(s, m.getPosition());
                if (d < bd) { bd = d; best = m; }
              }
              if (best == null) return now - began[0] > 50_000;
              victim[0] = r.creatureId(best);
            }
            BaseMonster t = monsterById(s, victim[0]);
            int hp = creatureHp(victim[0]);
            int max = 0;
            for (var c : view().snapshot().creatures()) if (c.id() == victim[0]) max = c.maxHealth();
            if (t == null || hp <= 0) return true;
            if (hp < max) return now - began[0] > 3000;
            combatFrame(s, true, t.getPosition());
            return false;
          });
      action(
          () -> {
            held.clear();
            try {
              Files.writeString(sync.resolve("m4-victim"), String.valueOf(victim[0]));
            } catch (java.io.IOException e) {
              throw new IllegalStateException(e);
            }
            log("DISCONNECT: creature " + victim[0] + " hp " + creatureHp(victim[0]) + " targets me; leaving the world | " + state());
            // The Options screen's "Switch character" button calls MainGameScreen.switchCharacter(); the Options button itself is not
            // clicked here (no GUI click driver), the same callback is invoked by reflection. Logged as scripted around the screen.
            try {
              var m = MainGameScreen.class.getDeclaredMethod("switchCharacter");
              m.setAccessible(true);
              m.invoke(game());
              log("DISCONNECT: MainGameScreen.switchCharacter() invoked by reflection (the Options button was not clicked)");
            } catch (ReflectiveOperationException e) {
              log("DISCONNECT: could not invoke switchCharacter: " + e);
            }
          });
      until("back in the lobby", 30_000, () -> getScreen() instanceof OnlineLobbyScreen);
      until("the other window killed the creature", 240_000, () -> signaled("m4.victim-dead"));
      final long[] rejoinStart = {0};
      repeat(
          "rejoined after leaving",
          180_000,
          () -> {
            long now = System.currentTimeMillis();
            if (game() != null) return true;
            if (getScreen() instanceof OnlineLobbyScreen && now - rejoinStart[0] > 2500) {
              rejoinStart[0] = now;
              Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
            }
            return false;
          });
      waitMs(3000);
      action(() -> log("DISCONNECT: rejoined | " + state()));
    } else {
      until("the other window left", 120_000, () -> signaled("m4-victim"));
      action(
          () -> {
            try {
              m4Target = Long.parseLong(Files.readString(sync.resolve("m4-victim")).trim());
            } catch (java.io.IOException | RuntimeException e) {
              m4Target = -1;
            }
            m4Seen = false;
            m4TargetHp = -2;
            m4LifeStart = System.currentTimeMillis();
            m4LifeXp0 = view().character().experience();
            log("DISCONNECT: the other player left; I attack its target creature " + m4Target + " hp " + creatureHp(m4Target));
          });
      repeat(
          "killed the abandoned creature",
          240_000,
          () -> {
            var s = game();
            if (s == null) return false;
            long now = System.currentTimeMillis();
            int hp = creatureHp(m4Target);
            if (hp > 0) m4Seen = true;
            if (hp != m4TargetHp) {
              log("DISCONNECT: creature " + m4Target + " hp " + m4TargetHp + " -> " + hp + " | my xp +" + (view().character().experience() - m4LifeXp0));
              m4TargetHp = hp;
            }
            if ((m4Seen && hp <= 0) || (!m4Seen && now - m4LifeStart > 8000)) {
              held.clear();
              return true;
            }
            BaseMonster t = monsterById(s, m4Target);
            if (t == null) return false;
            casterFrame(s, t, now);
            return false;
          });
      action(
          () -> {
            held.clear();
            log("DISCONNECT: the abandoned creature is dead; my xp +" + (view().character().experience() - m4LifeXp0) + " | " + state());
            signal("m4.victim-dead");
          });
    }
  }

  /** The melee role stands among creatures until it dies; the caster watches from its own window (public hit points, then the figure vanishes). */
  private void m4Death() {
    final long[] diedAt = {0};
    final int[] corpsesBefore = {0};
    final boolean[] lowShot = {false};
    final String[] carriedBefore = {null};
    if (tag.endsWith("A")) {
      action(
          () -> {
            noPotions = true;
            corpsesBefore[0] = view().corpses().size();
            carriedBefore[0] = inventorySummary(view().character().progress());
            log("DEATH: the melee role stops fighting and drinking and stands among the creatures | " + state());
            signal("m4.death-begin");
          });
      repeat(
          "died (a corpse is listed)",
          600_000,
          () -> {
            var s = game();
            held.clear();
            if (s != null) {
              BaseMonster m = nearestLive(s);
              if (m != null && distance(s, m.getPosition()) > 40f) walkToward(s, m.getPosition(), 30f);
              long t = System.currentTimeMillis();
              // A Skeleton does not attack an unprovoked player: one swing makes it retaliate, then the character does nothing.
              if (m != null && !provokedForDeath && distance(s, m.getPosition()) <= 60f && t - lastClick > 1500) {
                Vector2 at = aim(m.getPosition(), p -> m.isMouseOver(p.x, p.y));
                if (at != null) {
                  click(s, at.x, at.y);
                  provokedForDeath = true;
                  log("DEATH: one swing at " + m.getName() + " to provoke its retaliation; no further attack, no potion");
                }
              }
              if (t - lastMessageLog > 5000) {
                lastMessageLog = t;
                log("DEATH waiting: nearest creature " + (m == null ? "none drawn (" + monsters(s).size() + " creatures drawn)" : m.getName() + " at " + Math.round(distance(s, m.getPosition())) + " px")
                    + "; keys " + held + "; hp " + view().character().progress().vitals().hp());
              }
            }
            return view().corpses().size() > corpsesBefore[0] || view().character().progress().vitals().hp() <= 0;
          });
      waitMs(2500);
      action(
          () -> {
            diedAt[0] = System.currentTimeMillis();
            var v = view();
            log("DEATH happened: hp " + v.character().progress().vitals().hp() + " corpses " + v.corpses() + " | " + state());
            log("DEATH: carried before: " + carriedBefore[0] + " | after: " + inventorySummary(v.character().progress()));
            shot("after-death");
            signal("m4.A-dead");
          });
      repeat("respawned alive", 60_000, () -> view().character().progress().vitals().hp() > 0 && game() != null);
      waitMs(3000);
      action(
          () -> {
            noPotions = false;
            potionStage = 0;
            press(Input.Keys.ESCAPE);
            var p = me(game()).getCoordinates();
            log("RESPAWNED: region " + view().map().key() + " tile (" + (int) (p.getX() / TILE_W) + "," + (int) (p.getY() / TILE_H) + ") | " + state());
          });
    } else {
      action(() -> log("DEATH witness: the other player's public state: " + otherActor()));
      repeat(
          "the other player's hit points fell and it died",
          700_000,
          () -> {
            var s = game();
            if (s == null) return false;
            long now = System.currentTimeMillis();
            var o = otherActor();
            if (now - lastMessageLog > 3000) {
              lastMessageLog = now;
              log("WITNESS: other actor " + (o == null ? "not in my snapshot" : "hp " + o.hp() + "/" + o.maxHp() + " at (" + Math.round(o.x()) + "," + Math.round(o.y()) + ")")
                  + "; drawn others " + others(s).size() + "; my hp " + view().character().progress().vitals().hp());
            }
            if (o != null && o.maxHp() > 0 && o.hp() * 100 < o.maxHp() * 40 && !lowShot[0] && !others(s).isEmpty()) {
              lowShot[0] = true;
              cropOf("witness-other-low-hp", others(s).get(0).getPositionVector(), 300, 280);
            }
            drinkIfHurtOrHeal(s, now);
            return signaled("m4.A-dead");
          });
      waitMs(3000);
      action(
          () -> {
            var s = game();
            var o = otherActor();
            log("WITNESS after the death: other actor " + (o == null ? "no longer in my snapshot" : "hp " + o.hp() + "/" + o.maxHp() + " at (" + Math.round(o.x()) + "," + Math.round(o.y()) + ")")
                + "; drawn others " + (s == null ? -1 : others(s).size()) + "; my corpses " + view().corpses().size());
            shot("witness-after-death");
          });
    }
  }

  private boolean provokedForDeath;
  private int sipStage;
  private long sipAt;

  /** The watcher keeps itself alive: heal spell first, then a Healing Potion picked by name (never the Potion of Mana). */
  private void drinkIfHurtOrHeal(MainGameScreen s, long now) {
    var v = view();
    if (castStage != 0) {
      castStep(castFragment, now);
      return;
    }
    var vit = v.character().progress().vitals();
    if (sipStage == 1 && now - sipAt > 450) {
      var lines = abilityMenu().lines(v.character().progress());
      int digit = -1;
      for (int i = 1; i < lines.size() - 1; i++) {
        String line = lines.get(i).toLowerCase(Locale.ROOT);
        if (line.contains("healing") && !line.contains("light")) digit = i; // the strongest named Healing Potion
      }
      if (digit < 0)
        for (int i = 1; i < lines.size() - 1; i++) if (lines.get(i).toLowerCase(Locale.ROOT).contains("healing")) digit = i;
      if (digit > 0 && abilityMenu().isOpen()) press(digitKey(digit));
      else press(Input.Keys.ESCAPE);
      sipStage = 0;
      sipAt = now;
      return;
    }
    if (sipStage != 0) return;
    if (vit.hp() > 0 && vit.hp() * 100 < vit.maxHp() * 45 && now - sipAt > 2500) {
      if (abilityMenu().isOpen()) {
        press(Input.Keys.ESCAPE);
        return;
      }
      press(Input.Keys.U);
      sipStage = 1;
      sipAt = now;
      return;
    }
    if (vit.hp() > 0 && vit.hp() * 100 < vit.maxHp() * 70 && now - m4LastHeal > 4000 && vit.mana() >= 2 && tag.endsWith("B")) {
      m4LastHeal = now;
      castStep("heal", now);
    }
  }

  /** The dead role walks back and clicks its own corpse on the screen; the carried items must return exactly once. */
  private void m4Corpse() {
    if (!tag.endsWith("A")) {
      repeat("the other window recovered its corpse", 480_000, () -> {
        if (game() != null) drinkIfHurtOrHeal(game(), System.currentTimeMillis());
        return signaled("m4.A-recovered");
      });
      return;
    }
    final Vector2[] corpsePoint = {null};
    final UUID[] corpse = {null};
    final long[] leaving = {0};
    final boolean[] done = {false};
    final int[] tries = {0};
    final long[] clickAt = {0};
    action(
        () -> {
          for (var c : view().corpses())
            if (c.world() == view().map().world()) {
              corpse[0] = c.id();
              corpsePoint[0] = new Vector2((float) ((c.x() - 0.5) * TILE_W), (float) ((c.y() - 0.5) * TILE_H));
              log("CORPSE: " + c);
            }
          before = view().character().progress();
        });
    repeat(
        "walked back to the corpse and clicked it on the screen",
        480_000,
        () -> {
          var s = game();
          if (s == null || corpsePoint[0] == null) return true;
          long now = System.currentTimeMillis();
          if (leaving[0] == 0) leaving[0] = now;
          if (now - leaving[0] < 4000) {
            held.clear();
            held.add(Input.Keys.S);
            return false;
          }
          if (distance(s, corpsePoint[0]) < 44f) {
            held.clear();
            if (done[0] || tries[0] >= 6) return true;
            if (now - clickAt[0] > 3500) {
              clickAt[0] = now;
              tries[0]++;
              log("CORPSE: click " + tries[0] + " on the drawn corpse (GroundItemClickHandler), distance " + Math.round(distance(s, corpsePoint[0])) + " px");
              click(s, corpsePoint[0].x, corpsePoint[0].y);
            }
            if (view().corpses().stream().noneMatch(c -> c.id().equals(corpse[0]) && (c.itemCount() > 0 || c.gold() > 0))) done[0] = true;
            return false;
          }
          BaseMonster m = nearestLive(s);
          boolean threatened = m != null && distance(s, m.getPosition()) < 3 * TILE_W && System.currentTimeMillis() - lastHitAt < 4000;
          if (threatened) combatFrame(s, true, null);
          else {
            drinkIfHurt(view(), now);
            if (distance(s, corpsePoint[0]) > 14 * TILE_W && goalTx != Integer.MIN_VALUE) walkToward(s, tileCenter(goalTx, goalTy), 40f);
            else walkToward(s, corpsePoint[0], 20f);
          }
          if (now - lastMessageLog > 20_000) {
            lastMessageLog = now;
            log("CORPSE: returning, distance " + Math.round(distance(s, corpsePoint[0])) + " px; hp " + view().character().progress().vitals().hp());
          }
          return false;
        });
    waitMs(3500);
    action(
        () -> {
          held.clear();
          var v = view();
          log("CORPSE: after the click(s): corpses " + v.corpses() + " | carried before death " + inventorySummary(before) + " | now " + inventorySummary(v.character().progress()));
          shot("after-corpse-click");
          signal("m4.A-recovered");
        });
  }

  private void m4Scene() {
    skeletonsOnly = true;
    String only = System.getProperty("m4.chapters", "census,buff,contention,refusal,reset,disconnect,restart,death,corpse");
    java.util.List<String> chapters = java.util.Arrays.asList(only.split(","));
    if (chapters.contains("census")) m4Census();
    if (chapters.contains("buff")) m4Buff();
    if (chapters.contains("contention")) m4Contention(Integer.getInteger("m4.lives", 3));
    if (chapters.contains("area")) m4Area();
    if (chapters.contains("refusal")) m4Refusal();
    if (chapters.contains("reset")) m4Reset();
    if (chapters.contains("disconnect")) m4Disconnect();
    if (chapters.contains("restart")) m4Restart();
    if (chapters.contains("death")) m4Death();
    if (chapters.contains("corpse")) m4Corpse();
    action(() -> log("M4 SUMMARY: hits taken " + hitsTaken + " damage " + damageTaken + " casts pressed " + castsPressed + " | " + state()));
  }

  // ---------------------------------------------------------------- accept scene (T4C-0342, milestones M2 and M3)
  //
  // Two level-30 warriors beside WardenAelric run the whole authored adventure on the real screens: the own-screen inventory panel and
  // character sheet around an equip, unequip and re-equip (stat and armor class numbers read from the screen's own Player object), the
  // topic accept, the Lighthaven door pair, the walk to the Skeleton ground, kills until each character's own counter reaches the goal,
  // a cave trip of one window while the other fights outside (cross-region commands attempted through the connection, logged as such),
  // the walk back, the greeting turn-in twice per character, and a server restart. Chapters: -Daccept.chapters=equip,quest,boundary,turnin,restart.

  private static final int ACCEPT_KILLS = 12;

  private int questKills() {
    return view().character().progress().questFlags().getOrDefault(QUEST_KILLS, 0);
  }

  private String sheet() {
    var s = game();
    var p = me(s);
    return String.format("OWN SHEET strength %d, dexterity %d, armor class %.2f, endurance %d, worn on this screen %s", p.getEffectiveStrength(),
        p.getEffectiveDexterity(), com.perso.T4C.combat.ArmorClassRules.effectiveArmorClass(p), p.getEffectiveEndurance(),
        new java.util.TreeSet<>(p.getEquippedItems().keySet().stream().map(Object::toString).toList()));
  }

  /** Clicks a chat-bar button of the HUD (the screen's own touch handler) at a pixel inside it, found through the button's own bounds. */
  private boolean clickHudButton(String logName) {
    try {
      var s = game();
      Object hud = field(s, MainGameScreen.class, "hud");
      @SuppressWarnings("unchecked")
      List<Object> items = (List<Object>) field(hud, hud.getClass(), "chatBarButtons");
      for (Object item : items) {
        if (!logName.equals(field(item, item.getClass(), "logName"))) continue;
        Object button = field(item, item.getClass(), "button");
        var contains = button.getClass().getMethod("contains", float.class, float.class);
        long sx = 0, sy = 0, n = 0;
        for (int y = 0; y < Gdx.graphics.getHeight(); y += 3)
          for (int x = 0; x < Gdx.graphics.getWidth(); x += 3)
            if ((Boolean) contains.invoke(button, (float) x, (float) y)) {
              sx += x;
              sy += y;
              n++;
            }
        if (n == 0) return false;
        int x = (int) (sx / n), y = (int) (sy / n);
        var processor = Gdx.input.getInputProcessor();
        processor.mouseMoved(x, y);
        boolean down = processor.touchDown(x, y, 0, Input.Buttons.LEFT);
        boolean up = processor.touchUp(x, y, 0, Input.Buttons.LEFT);
        log("HUD button " + logName + ": clicked at (" + x + "," + y + ") over " + n + " sampled pixels, touchDown handled " + down + ", touchUp handled " + up);
        return true;
      }
    } catch (ReflectiveOperationException | RuntimeException e) {
      log("HUD button " + logName + ": " + e);
    }
    return false;
  }

  /** Waits for the committed state to change (an equip commit can take a few seconds when the first try is answered busy), then a moment more. */
  private void settled(String label, String[] before) {
    until("committed change after " + label, 15_000, () -> !state().equals(before[0]));
    waitMs(1800);
  }

  private void pick(Predicate<com.perso.T4C.online.OnlineInventoryPanel.Row> match, String label) {
    final String[] before = {null};
    action(() -> before[0] = state());
    inventoryPickRow(match, label, null);
    settled(label, before);
  }

  /** A picture of the inventory window itself (600 x 470 around its centre), real back buffer. */
  private void panelCrop(String name) {
    int w = 600, h = 470, bw = Gdx.graphics.getBackBufferWidth(), bh = Gdx.graphics.getBackBufferHeight();
    byte[] pixels = ScreenUtils.getFrameBufferPixels(340, bh - (150 + h), w, h, true);
    for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 255;
    Pixmap crop = new Pixmap(w, h, Pixmap.Format.RGBA8888);
    BufferUtils.copy(pixels, 0, crop.getPixels(), pixels.length);
    String file = tag + "-" + String.format("%02d", ++shots) + "-" + name + ".png";
    PixmapIO.writePNG(new com.badlogic.gdx.files.FileHandle(out.resolve(file).toFile()), crop);
    crop.dispose();
    log("saved " + file);
  }

  /** The character sheet through the HUD's own button, a picture, and closed again (the button toggles). */
  private void sheetShot(String name) {
    action(
        () -> {
          boolean clicked = clickHudButton("Character");
          log("CHARACTER SHEET click on the HUD button: " + clicked + " | " + sheet());
        });
    waitMs(800);
    action(
        () -> {
          // The online screen's input chain does not route clicks to the HUD buttons (finding), so a click opens nothing: the HUD's own
          // callback for that button is run instead (the same Runnable the button would run), and logged as scripted around the screen.
          if (!GuiManager.isOpen()) {
            try {
              Object hud = field(game(), MainGameScreen.class, "hud");
              ((Runnable) field(hud, hud.getClass(), "characterAction")).run();
              log("CHARACTER SHEET: the click opened nothing; ran the HUD's characterAction callback instead (scripted around the screen)");
            } catch (RuntimeException e) {
              log("CHARACTER SHEET: could not run the callback: " + e);
            }
          }
        });
    waitMs(900);
    action(
        () -> {
          log("CHARACTER SHEET window open: " + GuiManager.isOpen());
          shot(name);
          if (GuiManager.isOpen()) GuiManager.close();
        });
    waitMs(400);
  }

  private void acceptEquip() {
    final String side = tag.substring(tag.length() - 1);
    final var carried = com.perso.T4C.online.OnlineInventoryPanel.Kind.CARRIED;
    final var worn = com.perso.T4C.online.OnlineInventoryPanel.Kind.EQUIPPED;
    final String[] weapon = {null};
    action(
        () -> {
          for (var r : inventoryPanel().rows(view().character().progress()))
            if (r.kind() == worn && r.slot() != null && r.slot().name().equals("WEAPON")) weapon[0] = r.item().key();
          log("EQUIP baseline | " + sheet() + " | own public look " + lookOf(ownActor()) + " | " + state());
          press(Input.Keys.I);
        });
    waitMs(900);
    action(
        () -> {
          log("INVENTORY PANEL page 1 rows " + inventoryPanel().rows(view().character().progress()).stream()
              .map(r -> r.kind() + ":" + r.slot() + ":" + r.item().key() + ":" + r.item().quantity() + ":charges " + r.item().charges()).toList());
          panelCrop("inventory-window-page1-" + side);
          press(Input.Keys.RIGHT);
        });
    waitMs(700);
    action(
        () -> {
          panelCrop("inventory-window-page2-" + side);
          press(Input.Keys.ESCAPE);
        });
    waitMs(500);
    sheetShot("character-sheet-before-" + side);
    action(() -> cropOf("own-before-" + side, me(game()).getPositionVector(), 200, 240));
    // 1. Body armour off: armor class falls, the drawn model loses the piece.
    action(() -> press(Input.Keys.I));
    waitMs(700);
    pick(row -> row.kind() == worn && row.item().key().equals("item.leather_armor"), "unequip-body-" + side);
    action(
        () -> {
          log("EQUIP body armour off | " + sheet() + " | own public look " + lookOf(ownActor()));
          press(Input.Keys.ESCAPE);
        });
    waitMs(500);
    action(() -> cropOf("own-body-off-" + side, me(game()).getPositionVector(), 200, 240));
    sheetShot("character-sheet-body-off-" + side);
    // 2. Weapon off: strength falls by the weapon's boost.
    action(() -> press(Input.Keys.I));
    waitMs(700);
    pick(row -> row.kind() == worn && row.slot() != null && row.slot().name().equals("WEAPON"), "unequip-weapon-" + side);
    action(
        () -> {
          log("EQUIP weapon off | " + sheet() + " | own public look " + lookOf(ownActor()));
          press(Input.Keys.ESCAPE);
        });
    waitMs(500);
    action(() -> cropOf("own-weapon-off-" + side, me(game()).getPositionVector(), 200, 240));
    sheetShot("character-sheet-weapon-off-" + side);
    // 3. Both back on.
    action(() -> press(Input.Keys.I));
    waitMs(700);
    pick(row -> row.kind() == carried && row.item().key().equals("item.leather_armor"), "re-equip-body-" + side);
    pick(row -> row.kind() == carried && row.item().key().equals(weapon[0]), "re-equip-weapon-" + side);
    action(
        () -> {
          log("EQUIP everything back on | " + sheet() + " | own public look " + lookOf(ownActor()) + " | " + state());
          press(Input.Keys.ESCAPE);
        });
    waitMs(500);
    action(
        () -> {
          cropOf("own-restored-" + side, me(game()).getPositionVector(), 200, 240);
          signal(tag + ".equip-done");
        });
    sheetShot("character-sheet-restored-" + side);
    until("the other window finished the equip chapter", 240_000, () -> signaled(peer() + ".equip-done"));
  }

  /** Kills until this character's own committed kill counter reaches {@code goal}; both keep fighting until both have, so nobody idles among Skeletons. */
  private void acceptKills(int goal, String label) {
    final String marker = "kills-" + goal + "-";
    repeat(
        label,
        1_500_000,
        () -> {
          var s = game();
          if (s == null) return false;
          long now = System.currentTimeMillis();
          watchDrops(now);
          soakLog(now);
          int k = questKills();
          if (k >= goal && !signaled(marker + tag)) {
            signal(marker + tag);
            log("KILLS " + label + ": my counter reached " + k + " | " + questState());
          }
          if (signaled(marker + tag) && signaled(marker + peer())) {
            held.clear();
            return true;
          }
          if (now - lastMessageLog > 30_000) {
            lastMessageLog = now;
            log("KILLS " + label + ": " + questState());
          }
          combatFrame(s, true, null);
          return false;
        });
  }

  private long numberIn(String markerFile) {
    try {
      Path f = sync.resolve(markerFile);
      return Files.exists(f) ? Long.parseLong(Files.readString(f).trim()) : -1;
    } catch (java.io.IOException | RuntimeException e) {
      return -1;
    }
  }

  private void write(String name, String text) {
    try {
      Files.writeString(sync.resolve(name), text);
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private void logCommand(String what, java.util.concurrent.CompletionStage<OnlineConnection.CommandResult> stage) {
    stage.whenComplete((r, f) -> log("CROSS-REGION " + what + " -> " + (f != null ? "failure " + f : "accepted=" + r.accepted() + " key=" + r.messageKey())));
  }

  /** The live Skeleton with the highest creature id in this window's region, or null. */
  private BaseMonster highestIdSkeleton(MainGameScreen s) {
    var r = remoteWorld(s);
    BaseMonster best = null;
    long bestId = -1;
    for (BaseMonster m : monsters(s)) {
      if (m.isDead() || m.getHealth() <= 0 || !isSkeleton(m)) continue;
      long id = r.creatureId(m);
      if (id > bestId && creatureHp(id) > 0) {
        bestId = id;
        best = m;
      }
    }
    return best;
  }

  private long highestCreatureId() {
    long max = -1;
    for (var c : view().snapshot().creatures()) max = Math.max(max, c.id());
    return max;
  }

  /**
   * One window walks into the cave while the other stays out; each then tries the other's targets through the connection (the screen has no
   * way to aim at another region, so these are logged as sent around the screen). Creature ids are region-local: the id of the other
   * window's target either does not exist in this window's region (the server answers that there is no such target) or names a different
   * creature here (out of reach); the other window's creature is never touched.
   */
  private void acceptBoundary() {
    final boolean outside = tag.endsWith("A");
    // Fight on until a ground drop exists, so the cave window can try to pick up an item it cannot see.
    repeat(
        "a ground drop exists in the outside window",
        420_000,
        () -> {
          var s = game();
          if (s == null) return false;
          long now = System.currentTimeMillis();
          watchDrops(now);
          if (outside && !view().snapshot().loot().isEmpty() && !signaled("bnd.have-loot")) {
            write("bnd.loot", view().snapshot().loot().get(0).id().toString());
            signal("bnd.have-loot");
          }
          if (signaled("bnd.have-loot")) {
            held.clear();
            return true;
          }
          combatFrame(s, true, null);
          return false;
        });
    if (outside) {
      action(
          () -> {
            var s = game();
            var r = remoteWorld(s);
            BaseMonster m = highestIdSkeleton(s);
            if (m != null) write("bnd.outside", String.valueOf(r.creatureId(m)));
            var loot = view().snapshot().loot();
            log("BOUNDARY outside window: target creature " + (m == null ? "none" : r.creatureId(m)) + " hp " + (m == null ? -1 : creatureHp(r.creatureId(m)))
                + ", loot in view " + loot.size() + (loot.isEmpty() ? "" : " first " + loot.get(0).id() + " " + loot.get(0).key()) + " | " + state());
          });
    }
    portalHop("B", 2770, 1095, "start", "skeleton-cave", true, "boundary-B-into-the-cave");
    final long[] x = {-1}, y = {-1};
    final int[] hpX = {-1}, hpY = {-1};
    if (outside) {
      repeat("the cave window published its report", 60_000, () -> numberIn("bnd.cave") > 0);
      action(
          () -> {
            x[0] = numberIn("bnd.outside");
            y[0] = numberIn("bnd.cave");
            hpX[0] = creatureHp(x[0]);
            log("BOUNDARY outside window: B is in the cave (others drawn " + others(game()).size() + ", snapshot players " + view().snapshot().players().size()
                + "); the cave window's target id " + y[0] + (creatureHp(y[0]) >= 0 ? " also exists here as another creature (ids are region-local)" : " does not exist here")
                + "; my target " + x[0] + " hp " + hpX[0]);
            logCommand("attack on the cave window's creature id " + y[0] + " from the outside window", connection().attack(y[0]));
            signal("bnd.outside-ready");
          });
    } else {
      action(
          () -> {
            var v = view();
            y[0] = highestCreatureId();
            hpY[0] = creatureHp(y[0]);
            long outsideId = numberIn("bnd.outside");
            log("BOUNDARY cave window: region " + v.map().key() + ", creatures in my snapshot " + v.snapshot().creatures().size() + ", my highest id " + y[0] + " hp " + hpY[0]
                + ", the outside window's target " + outsideId + (creatureHp(outsideId) >= 0 ? " also exists here as another creature (ids are region-local)" : " does not exist here")
                + ", loot in my snapshot " + v.snapshot().loot().size());
            write("bnd.cave", String.valueOf(y[0]));
          });
      until("the outside window is ready", 60_000, () -> signaled("bnd.outside-ready"));
      action(
          () -> {
            long outsideId = numberIn("bnd.outside");
            if (outsideId > 0) logCommand("attack on the outside window's creature id " + outsideId + " from the cave window", connection().attack(outsideId));
          });
      waitMs(2500);
      action(
          () -> {
            try {
              Path f = sync.resolve("bnd.loot");
              if (Files.exists(f)) logCommand("pickup of the outside window's ground drop from the cave window", connection().pickUp(UUID.fromString(Files.readString(f).trim())));
              else log("BOUNDARY cave window: no outside loot id to try");
            } catch (java.io.IOException ignored) {
              log("BOUNDARY cave window: no outside loot id to try");
            }
          });
    }
    waitMs(5000);
    action(
        () -> {
          if (outside) log("BOUNDARY outside window: my target " + x[0] + " hp " + hpX[0] + " -> " + creatureHp(x[0]) + " after the cave window's attack on it; drop still on the ground: "
              + (!view().snapshot().loot().isEmpty()));
          else {
            log("BOUNDARY cave window: my creature " + y[0] + " hp " + hpY[0] + " -> " + creatureHp(y[0]) + " after the outside window's attack on that id | " + state());
            signal("bnd.cave-done");
          }
        });
    until("both windows finished the boundary attempts", 90_000, () -> signaled("bnd.cave-done"));
    portalHop("B", 136, 665, "skeleton-cave", "start", false, "boundary-B-out-of-the-cave");
  }

  private void acceptTurnIn() {
    action(() -> log("TURN-IN start: " + questState()));
    // Aelric's tile (2948,1044): the walk from the Skeleton ground is longer than talkTo's own patience.
    repeat(
        "walked back to Warden Aelric",
        360_000,
        () -> {
          var s = game();
          if (s == null) return false;
          long now = System.currentTimeMillis();
          drinkIfHurt(view(), now);
          Vector2 goal = tileCenter(2948, 1044);
          walkToward(s, goal, 60f);
          return distance(s, goal) <= 70f;
        });
    action(() -> held.clear());
    talkTo("aelric", "turn-in-1");
    action(() -> press(Input.Keys.ESCAPE));
    waitMs(2500);
    talkTo("aelric", "turn-in-2-repeat");
    action(() -> press(Input.Keys.ESCAPE));
    waitMs(2500);
    action(
        () -> {
          log("TURN-IN end: " + questState() + " | " + state());
          shot("turned-in");
          signal(tag + ".turnin-done");
        });
    until("the other window finished the turn-in", 240_000, () -> signaled(peer() + ".turnin-done"));
  }

  private void acceptScene() {
    acceptScene(java.util.Arrays.asList(System.getProperty("accept.chapters", "equip,topic,hops,ground,kills,boundary,turnin,restart").split(",")));
  }

  private void acceptScene(List<String> chapters) {
    skeletonsOnly = true;
    if (chapters.contains("equip")) acceptEquip();
    if (chapters.contains("topic")) topicChapter();
    if (chapters.contains("hops")) {
      portalHop("A", 2931, 1056, "start", "lighthaven-interior", true, "1-A-into-Lighthaven");
      portalHop("B", 2931, 1056, "start", "lighthaven-interior", false, "2-B-into-Lighthaven");
      waitMs(3000);
      portalHop("A", 306, 381, "lighthaven-interior", "start", true, "3-A-out-of-Lighthaven");
      portalHop("B", 306, 381, "lighthaven-interior", "start", false, "4-B-out-of-Lighthaven");
    }
    if (chapters.contains("ground")) {
      // A window that starts inside the cave (an earlier chapter left it there) takes the cave exit first.
      repeat(
          "left the cave if the character started in it",
          150_000,
          () -> {
            var s = game();
            if (s == null) return false;
            if (!view().map().key().equals("skeleton-cave")) return true;
            allowTile = tileKey(136, 665);
            walkToward(s, tileCenter(136, 665), 3f);
            return false;
          });
      action(() -> held.clear());
      // Healthy before the walk (a window that starts hurt would die among the Skeletons), and south out of the temple room first
      // (the route search oscillates at the respawn tile, 3.17 finding 6).
      repeat(
          "healthy enough for the walk",
          180_000,
          () -> {
            var s = game();
            if (s == null) return false;
            var vit = view().character().progress().vitals();
            drinkIfHurt(view(), System.currentTimeMillis());
            return vit.hp() * 100 >= vit.maxHp() * 75;
          });
      final long[] leaving = {0};
      repeat(
          "walked south out of the temple room if it started there",
          6_000,
          () -> {
            var s = game();
            if (s == null) return true;
            var p = me(s).getCoordinates();
            int tx = (int) (p.getX() / TILE_W), ty = (int) (p.getY() / TILE_H);
            if (Math.abs(tx - 2950) > 14 || Math.abs(ty - 1040) > 10) return true;
            long now = System.currentTimeMillis();
            if (leaving[0] == 0) leaving[0] = now;
            held.clear();
            held.add(Input.Keys.S);
            return now - leaving[0] > 4000;
          });
      action(() -> held.clear());
      waitMs(1500);
      walkToGround();
      action(() -> signal(tag + ".at-ground"));
      until("peer at the ground", 240_000, () -> signaled(peer() + ".at-ground"));
    }
    if (chapters.contains("kills")) acceptKills(6, "first half of the kills");
    if (chapters.contains("boundary")) acceptBoundary();
    if (chapters.contains("kills")) acceptKills(ACCEPT_KILLS, "kills to the goal");
    if (chapters.contains("turnin")) acceptTurnIn();
    if (chapters.contains("reaccept")) {
      // After completion: the greeting, then topic 0 (the quest offer) again. The completed quest must stay completed.
      talkTo("aelric", "reaccept-greeting");
      action(
          () -> {
            log("REACCEPT: panel open " + talkPanel().isOpen() + " lines " + talkPanel().lines() + " | " + questState());
            if (talkPanel().isOpen()) press(Input.Keys.NUM_1);
          });
      waitMs(3500);
      action(
          () -> {
            log("REACCEPT: after topic 0 on a completed quest: " + questState());
            press(Input.Keys.ESCAPE);
          });
    }
    if (chapters.contains("restart")) {
      action(() -> log("RESTART: before: " + questState() + " | " + state()));
      restartCycle("acc", () -> log("RESTART: after the rejoin: " + questState() + " | " + state()));
      waitMs(3000);
      talkTo("aelric", "after-restart-repeat");
      action(() -> log("RESTART: after the repeat greeting: " + questState()));
    }
  }

  // ---------------------------------------------------------------- m5 scene (T4C-0353)
  //
  // The M5 mixed session: the whole quest (accept, Lighthaven door pair, kills, turn-in) for both windows, then a graceful server
  // restart, a forced server kill and a database crash with an exact before/after comparison of the committed state, a cave trip,
  // a fight and loot segment in which the operator hard-kills the server right after a pickup claim (-Dm5.killRace=N), and a
  // death with a corpse recovery request that is hard-killed too (-Dm5.killRecover=true). The operator (a script, not this class)
  // performs every fault between the marker files: see multiplayer/tools/NATIVE_FAULT_DRILL.md "Rendered mixed session".

  private final int m5KillRace = Integer.getInteger("m5.killRace", 0);
  private final boolean m5KillRecover = Boolean.getBoolean("m5.killRecover");
  private UUID m5PkDrop;
  private String m5PkKey;
  private int m5PkCarried;
  private long m5TouchAt;
  private boolean m5TouchRight;
  private long m5SampleAt;

  private String m5Snapshot() {
    var c = view().character();
    var p = c.progress();
    java.util.TreeMap<String, Integer> carried = new java.util.TreeMap<>();
    java.util.Map<UUID, String> keys = new java.util.HashMap<>();
    p.inventory().forEach(i -> {
      carried.merge(i.key(), i.quantity(), Integer::sum);
      keys.put(i.id(), i.key());
    });
    java.util.TreeMap<String, String> worn = new java.util.TreeMap<>();
    p.equipment().forEach((slot, id) -> worn.put(String.valueOf(slot), keys.getOrDefault(id, "?")));
    return "gold=" + c.gold() + " xp=" + c.experience() + " level=" + p.advancement().level() + " quest=" + new java.util.TreeMap<>(p.questFlags())
        + " spells=" + new java.util.TreeSet<>(p.spells()) + " carried=" + carried + " worn=" + worn;
  }

  /** Keeps the character committing (a short step left and right) while the realm is about to lose its database. */
  private void m5Touch() {
    long now = System.currentTimeMillis();
    if (now - m5TouchAt > 1500) {
      m5TouchAt = now;
      m5TouchRight = !m5TouchRight;
      held.clear();
      held.add(m5TouchRight ? Input.Keys.D : Input.Keys.A);
    }
  }

  /** One fault cycle: committed state before, the operator's fault between the markers, rejoin, exact comparison. */
  private void m5Fault(String label, String what, boolean touch) {
    final String[] snap = {null};
    action(
        () -> {
          snap[0] = m5Snapshot();
          log("M5 FAULT " + label + " (" + what + ") BEFORE: " + snap[0]);
        });
    restartCycle(
        label,
        () -> {
          String after = m5Snapshot();
          double moved = -1;
          if (game() != null && positionBeforeRestart != null) {
            var p = me(game()).getCoordinates();
            moved = Math.hypot(p.getX() - positionBeforeRestart.x, p.getY() - positionBeforeRestart.y);
          }
          log("M5 COMPARE " + label + " (" + what + ") " + (snap[0].equals(after) ? "IDENTICAL" : "DIFF") + "; moved " + Math.round(moved) + " px"
              + (touch ? " (the window walked left and right while waiting for the fault to show)" : "") + "\n  before " + snap[0] + "\n  after  " + after);
        },
        touch ? this::m5Touch : null);
  }

  private void m5Monitor(long now) {
    if (now - m5SampleAt < 60_000 || view() == null) return;
    m5SampleAt = now;
    var rt = Runtime.getRuntime();
    log("M5 SAMPLE t=" + (now - t0) / 1000 + "s heapMB " + (rt.totalMemory() - rt.freeMemory()) / 1_000_000 + " connection " + connection().status()
        + " screen " + (game() != null ? "game" : getScreen() == null ? "none" : getScreen().getClass().getSimpleName()));
  }

  private void m5Scene() {
    skeletonsOnly = true;
    // The quest and the travel: topic panel, Lighthaven door pair, walk to the ground, kills to the goal, walk back, turn-in.
    // -Dm5.skipQuest=true is for a smoke test of the fault machinery only; the evidence run does the quest.
    if (!Boolean.getBoolean("m5.skipQuest")) acceptScene(List.of("topic", "hops", "ground", "kills", "turnin"));
    action(() -> log("M5 QUEST DONE: " + questState()));
    waitMs(3000);
    // -Dm5.from=death is for a smoke test of the death and recovery-kill part only.
    if ("death".equals(System.getProperty("m5.from"))) acceptScene(List.of("ground"));
    else m5Faults();
    deathChapter(m5KillRecover);
    action(
        () -> {
          log("M5 SESSION SUMMARY: " + state() + " minHp " + minHp + " attacks " + attacksSent + " drops seen " + dropsSeen.size() + " races " + racesDone
              + " connection " + connection().status() + " elapsed " + (System.currentTimeMillis() - t0) / 1000 + " s");
          shot("final");
        });
  }

  private void m5Faults() {
    m5Fault("g", "graceful server restart", false);
    waitMs(3000);
    m5Fault("k", "forced server kill", false);
    waitMs(3000);
    m5Fault("d", "database crash", true);
    waitMs(3000);
    // Cave trip (a portal pair to a second region and back), each character in turn with the other watching.
    acceptScene(List.of("ground"));
    portalHop("A", 2770, 1095, "start", "skeleton-cave", true, "5-A-into-the-cave");
    portalHop("B", 2770, 1095, "start", "skeleton-cave", false, "6-B-into-the-cave");
    waitMs(4000);
    portalHop("A", 136, 665, "skeleton-cave", "start", true, "7-A-out-of-the-cave");
    portalHop("B", 136, 665, "skeleton-cave", "start", false, "8-B-out-of-the-cave");
    // Fight and loot until the planned claim race has been clicked; the operator hard-kills the server right after it.
    repeat(
        "fight and loot until claim race " + m5KillRace + " was clicked",
        1_500_000,
        () -> {
          mixedFrame();
          return race != null && race.number == m5KillRace && race.clicked && System.currentTimeMillis() - race.clickedAt > 1200;
        });
    action(() -> held.clear());
    restartCycle("pk", () -> log("M5 pk rejoined: " + state()));
    waitMs(3000);
    final int[] before = {0};
    action(
        () -> {
          var s = game();
          before[0] = count(view().character().progress(), m5PkKey.replace("item.", ""));
          var item = s == null ? null : groundItemOf(s, m5PkDrop);
          var loot = lootView(m5PkDrop);
          log("M5 PK after the restart: the drop " + (item != null ? "is still on the ground" : "is not on the ground") + " (view " + (loot != null) + "); carried "
              + before[0] + " of " + m5PkKey + " (before the claim " + m5PkCarried + ")");
          race = null;
          racesDone++;
          lastRaceEnd = System.currentTimeMillis();
          if (item != null) {
            Vector2 at = aim(item.getPosition(), p -> item.isMouseOver(p.x, p.y));
            log("M5 PK retry: click on the drop at " + at + ", distance " + Math.round(distance(s, item.getPosition())) + " px");
            if (at != null) click(s, at.x, at.y);
          }
        });
    waitMs(5000);
    action(
        () -> {
          int after = count(view().character().progress(), m5PkKey.replace("item.", ""));
          log("M5 PK final: carried " + after + " of " + m5PkKey + " (before the claim " + m5PkCarried + "); messages " + messagesSince(System.currentTimeMillis() - 6000)
              + "; drop still listed " + (lootView(m5PkDrop) != null) + " | " + state());
        });
    mixedSegment("m5 fight and loot after the pickup kill", Long.getLong("m5.segmentMs", 420_000L));
  }

  // ---------------------------------------------------------------- frame loop

  @Override
  public void render() {
    super.render();
    long now = System.currentTimeMillis();
    if (scene.equals("beta") || scene.equals("m4") || scene.equals("accept") || scene.equals("m5")) pollMessage();
    if (scene.equals("m5")) m5Monitor(now);
    if (scene.equals("m4")) m4Monitor();
    if (now > (scene.equals("quest") ? t0 + 1_500_000 : scene.equals("beta") ? t0 + 4_800_000 : scene.equals("m4") ? t0 + 3_000_000 : scene.equals("accept") ? t0 + 5_400_000 : scene.equals("m5") ? t0 + 7_200_000 : deadline)) {
      log("DRIVER TIMEOUT at step " + index);
      Gdx.app.exit();
      return;
    }
    if (daylight && now - lastDaylight > 3000 && game() != null) {
      lastDaylight = now;
      try {
        var cycle = field(game(), MainGameScreen.class, "dayNightCycle");
        cycle.getClass().getMethod("setHour", float.class).invoke(cycle, 12f);
      } catch (ReflectiveOperationException e) {
        log("could not set daylight: " + e);
      }
    }
    if (index < steps.size() && steps.get(index).tick()) index++;
  }
}
