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
 * Not scripted (a real mouse or OS key event, the Options button, character creation, spell
 * casting, equipping). Two processes coordinate through marker files in a shared directory;
 * the orchestrating operator creates {@code server-restarted} after restarting the server.
 *
 * <p>Arguments: endpoint, profile file, output directory, tag (for file names), scene
 * ({@code temple} or {@code skeletons}), sync directory, window x, window y.
 */
public final class OnlineLiveRunDriver extends MyGame {
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
    new Lwjgl3Application(
        new OnlineLiveRunDriver(endpoint, profile.secret(), profile.lobbyModel(), out, args[3], args[4], sync),
        config);
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

  private static boolean walkable(int tx, int ty) {
    return !com.perso.T4C.helper.CollisionManager.getInstance().hasCollision(tx * TILE_W + TILE_W / 2f, ty * TILE_H + TILE_H / 2f);
  }

  /** Breadth-first search over free tiles (4 directions, the client's own collision map). */
  private static List<int[]> search(int sx, int sy, int gx, int gy) {
    int radius = 70;
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
    if (dx > 5) held.add(Input.Keys.D);
    if (dx < -5) held.add(Input.Keys.A);
    // The world's y axis points down the screen: S increases y, W decreases it.
    if (dy > 3) held.add(Input.Keys.S);
    if (dy < -3) held.add(Input.Keys.W);
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
        45_000,
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
    until("peer also saw me", 60_000, () -> signaled(peer() + ".saw-other"));
    if (scene.equals("temple")) templeScene();
    else skeletonScene();
    action(
        () -> {
          signal(tag + ".done");
          log("script finished");
        });
    until("peer finished", 120_000, () -> signaled(peer() + ".done"));
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
    // 4. Server stop and restart (the operator does it between these markers).
    action(
        () -> {
          var s = game();
          positionBeforeRestart = new Vector2(me(s).getCoordinates().getX(), me(s).getCoordinates().getY());
          before = view().character().progress();
          log("BEFORE RESTART: position " + positionBeforeRestart + " inventory " + inventorySummary(before) + " status " + connection().status());
          shot("before-server-stop");
          signal(tag + ".stop-ready");
        });
    until("operator stopped the server (connection shows disconnected)", 180_000, () -> "disconnected".equals(connection().status()));
    waitMs(2000);
    action(() -> shot("server-stopped-disconnected-text"));
    until("operator restarted the server", 240_000, () -> signaled("server-restarted"));
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
        });
    talkOnce("greeting-after-restart");
  }

  private BaseMonster nearestLive(MainGameScreen s) {
    BaseMonster best = null;
    double bestDistance = Double.MAX_VALUE;
    for (BaseMonster m : monsters(s)) {
      if (m.isDead() || m.getHealth() <= 0) continue;
      double d = distance(s, m.getPosition());
      if (d < bestDistance) {
        bestDistance = d;
        best = m;
      }
    }
    return best;
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

  // ---------------------------------------------------------------- frame loop

  @Override
  public void render() {
    super.render();
    long now = System.currentTimeMillis();
    if (now > deadline) {
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
