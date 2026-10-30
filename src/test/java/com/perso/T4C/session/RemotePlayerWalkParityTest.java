package com.perso.T4C.session;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.PLAYER_SPEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.audio.SoundSpy;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Another player must walk like the own player does in single-player: continuous legs, no restart of
 * the cycle, the speed the server moves it at and a facing that follows the path. The harness feeds
 * the driver what a real client sees: 34 Hz server ticks at {@code PLAYER_SPEED}, delivered by a
 * poller that gets the latest tick every 50 to 110 ms (the real loop sleeps 75 ms between two HTTP
 * calls), drawn at 60 frames per second.
 */
@ResourceLock("sound.manager")
class RemotePlayerWalkParityTest {
  private static final double TICK_HZ = 34.0;
  private static final float FRAME = 1f / 60f;

  private OnlineConnection.View view;
  private RemoteWorldDriver driver;
  private WorldEntities entities;

  @BeforeEach
  void build() {
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister("assets/items");
    driver = new RemoteWorldDriver(() -> view, new com.perso.T4C.objects.GroundItemManager());
    entities = driver.loadEntities(new WorldEntityInputs(null, null, null, () -> null, new Player()));
  }

  @AfterEach
  void cleanUp() {
    com.perso.T4C.item.ItemRegistry.resetAdditionalDefinitions();
    SoundSpy.remove();
  }

  /** One frame of the walker as the observing client draws it. */
  private record Frame(
      double time, boolean moving, float animTimer, float x, float y, String angle, boolean flip) {}

  /** A path leg: direction in tile steps per axis (-1, 0 or 1) held for {@code seconds}. */
  private record Leg(int dx, int dy, double seconds) {}

  private static final List<Leg> STRAIGHT = List.of(new Leg(1, 0, 6.0));
  private static final List<Leg> ZIG_ZAG =
      List.of(
          new Leg(1, 0, 1.5),
          new Leg(1, 1, 1.5),
          new Leg(1, 0, 1.0),
          new Leg(1, -1, 1.5),
          new Leg(0, -1, 1.0),
          new Leg(-1, -1, 1.5),
          new Leg(-1, 0, 1.0));

  /** Server position at a tick: the walker moves {@code PLAYER_SPEED / 34} pixels per axis per tick. */
  private static double[] serverPosition(List<Leg> legs, long tick) {
    double stepTiles = PLAYER_SPEED / 34.0 / GRID_W; // same tiles per tick on both axes
    double x = 40.5, y = 40.5;
    double remaining = tick / TICK_HZ;
    for (Leg leg : legs) {
      double t = Math.min(remaining, leg.seconds());
      long ticks = Math.round(t * TICK_HZ);
      x += leg.dx() * stepTiles * ticks;
      y += leg.dy() * stepTiles * ticks;
      remaining -= leg.seconds();
      if (remaining <= 0) break;
    }
    return new double[] {x, y};
  }

  private List<Frame> run(List<Leg> legs, long seed, boolean withStall, double minGap, double spread) {
    double total = 0;
    for (Leg leg : legs) total += leg.seconds();
    long lastMovingTick = Math.round(total * TICK_HZ);
    Random random = new Random(seed);
    double nextPoll = 0.0;
    List<Frame> frames = new ArrayList<>();
    Player other = null;
    int polls = 0;
    // 1 s of standing, then the route, then 2 s of standing.
    double standing = 1.0;
    for (double now = 0; now < standing + total + 2.0; now += FRAME) {
      if (now >= nextPoll) {
        double serverTime = Math.max(0, now - standing);
        long tick = 10_000 + Math.round(serverTime * TICK_HZ);
        double[] p = serverPosition(legs, Math.min(Math.max(0, tick - 10_000), lastMovingTick));
        view =
            RemoteWorldDriverTest.view(
                tick,
                RemoteWorldDriverTest.MAP.region(),
                List.of(
                    RemoteWorldDriverTest.actor(RemoteWorldDriverTest.OWN, 3.5, 5.5, 100, 100),
                    RemoteWorldDriverTest.actor(9, p[0], p[1], 80, 80)),
                List.of(),
                List.of(),
                List.of());
        polls++;
        double gap = minGap + random.nextDouble() * spread;
        if (withStall && polls == 40) gap = 0.18;
        nextPoll += gap;
      }
      driver.tickEntities(
          new EntityTick(
              entities.npcs(), entities.monsters(), new Player(), new Vector2(), FRAME,
              0, 1000, 0, 1000, 5));
      if (other == null && !driver.otherPlayers().isEmpty()) other = driver.otherPlayers().get(0);
      if (other != null) {
        var pos = other.getPositionVector();
        frames.add(
            new Frame(
                now,
                other.getMovement().isMoving(),
                other.getAnimations().getAnimTimer(),
                pos.x,
                pos.y,
                other.getMovement().getCurrentAngle(),
                other.getMovement().isFlipX()));
      }
    }
    return frames;
  }

  /** The frames in which the server is certainly still moving the player (after the pipeline's lag). */
  private static List<Frame> steadyWalking(List<Frame> frames, List<Leg> legs) {
    double total = 0;
    for (Leg leg : legs) total += leg.seconds();
    List<Frame> out = new ArrayList<>();
    for (Frame f : frames) if (f.time() > 1.0 + 0.6 && f.time() < 1.0 + total - 0.1) out.add(f);
    return out;
  }

  private void assertWalksLikeSinglePlayer(
      List<Leg> legs, long seed, boolean stall, double minGap, double spread) {
    List<Frame> frames = run(legs, seed, stall, minGap, spread);
    List<Frame> walking = steadyWalking(frames, legs);
    assertTrue(walking.size() > 100);

    int notMoving = 0;
    int restarts = 0;
    for (int i = 0; i < walking.size(); i++) {
      Frame f = walking.get(i);
      if (!f.moving()) notMoving++;
      if (i > 0 && f.animTimer() < walking.get(i - 1).animTimer()) restarts++;
    }
    System.out.println(
        "walk harness seed " + seed + ": frames " + walking.size() + ", standing-pose frames " + notMoving
            + ", cycle restarts " + restarts);
    assertEquals(0, notMoving, "walking pose on every frame while the server moves the player");
    assertEquals(0, restarts, "the walk cycle never restarts (seed " + seed + ")");

    // Visual speed over 0.25 s windows equals the server's pace per axis (x px/s, y px/s / 2).
    int window = 15;
    double worst = 0;
    for (int i = 0; i + window < walking.size(); i++) {
      Frame a = walking.get(i), b = walking.get(i + window);
      double dt = b.time() - a.time();
      double speedX = Math.abs(b.x() - a.x()) / dt;
      double speedY = Math.abs(b.y() - a.y()) / dt * (GRID_W / (double) GRID_H);
      double speed = Math.max(speedX, speedY);
      worst = Math.max(worst, Math.abs(speed - PLAYER_SPEED) / PLAYER_SPEED);
    }
    System.out.println("  worst 0.25 s speed error " + worst);
    assertTrue(worst < 0.2, "visual speed within 20% of the server pace over 0.25 s, worst " + worst);

    // Per-frame step never bursts (no snap-then-ease): at most 1.6x the nominal 60 fps step.
    double nominal = PLAYER_SPEED * FRAME;
    double maxStep = 0;
    for (int i = 1; i < walking.size(); i++) {
      Frame a = walking.get(i - 1), b = walking.get(i);
      maxStep =
          Math.max(
              maxStep,
              Math.max(
                  Math.abs(b.x() - a.x()), Math.abs(b.y() - a.y()) * (GRID_W / (double) GRID_H)));
    }
    System.out.println("  max per-frame step " + maxStep + " nominal " + nominal);
    assertTrue(maxStep < nominal * 1.6, "no bursts: max per-frame step " + maxStep + " vs " + nominal);

    // The pose is the same one a single-player walk shows: the facing only changes at path turns.
    int facingChanges = 0;
    for (int i = 1; i < walking.size(); i++) {
      Frame a = walking.get(i - 1), b = walking.get(i);
      if (!a.angle().equals(b.angle()) || a.flip() != b.flip()) facingChanges++;
    }
    System.out.println("  facing changes " + facingChanges + " for " + legs.size() + " legs");
    assertTrue(
        facingChanges <= legs.size(), "facing changes " + facingChanges + " for " + legs.size() + " legs");

    // Once the server stops, the walker rests.
    Frame last = frames.get(frames.size() - 1);
    assertTrue(!last.moving(), "rests at the end");
  }

  @Test
  void straightWalkIsContinuousAtPlayerSpeed() {
    for (long seed = 1; seed <= 5; seed++) assertWalksLikeSinglePlayer(STRAIGHT, seed, false, 0.05, 0.06);
  }

  @Test
  void zigZagWalkNeverRestartsTheLegsAndKeepsTheServerSpeed() {
    for (long seed = 1; seed <= 5; seed++) assertWalksLikeSinglePlayer(ZIG_ZAG, seed, false, 0.05, 0.06);
  }

  @Test
  void aSlowBusyClientWithPollsEvery100To300MsStillWalksContinuously() {
    for (long seed = 1; seed <= 5; seed++) assertWalksLikeSinglePlayer(ZIG_ZAG, seed, false, 0.1, 0.2);
  }

  @Test
  void aSingleLateSnapshotDoesNotStopTheLegs() {
    assertWalksLikeSinglePlayer(ZIG_ZAG, 11, true, 0.05, 0.06);
  }
}
