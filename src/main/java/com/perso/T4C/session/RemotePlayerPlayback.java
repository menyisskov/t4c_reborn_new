package com.perso.T4C.session;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Plays back another player's recorded route at the pace the server moved it.
 *
 * <p>A client only sees the server's position when its poll returns (every 75 to 350 ms, in bursts),
 * while the server moves a player a pixel or two per 34 Hz tick. Chasing the latest position (what
 * this class replaces) alternates "jump, then ease" and the walking flag follows how recently a
 * jump happened, so a remote player looked as if it glided and its leg animation restarted whenever
 * a poll was late. Here every received position is a sample on the <em>server's</em> time line
 * (tick / 34 Hz), and the puppet is drawn a little behind the newest sample, moving along the
 * straight lines between samples. The motion is then constant-speed, the walking flag is exactly
 * "the drawn position is moving right now", and the facing is the direction of the drawn segment.
 *
 * <p>The distance the picture trails the newest sample is controlled from how much the buffer ran
 * down before each new sample arrived: it is kept just above the recent longest gap between samples,
 * so a late poll is absorbed instead of stopping the puppet. No extrapolation is done, so a player
 * that stops is never drawn past where it stopped.
 */
final class RemotePlayerPlayback {
  /** Server ticks per second (the server's fixed rate). */
  static final double TICK_HZ = 34.0;

  /** Smallest and largest distance the picture may trail the newest sample, in seconds. */
  private static final double MIN_LAG = 0.1;

  private static final double MAX_LAG = 0.4;

  /** A jump this long at this speed is a teleport (portal, respawn), not a walk. */
  private static final float TELEPORT_DISTANCE = 4f * 32f;

  private static final float TELEPORT_SPEED = 900f;

  /** Walking stays on this long after the drawn position stopped (hides single-frame flicker). */
  private static final double MOVING_HOLD = 0.05;

  /** Playback speed may differ from real time by at most this fraction while the lag converges. */
  private static final double MAX_RATE_ERROR = 0.12;

  private record Sample(double time, float x, float y) {}

  private final Deque<Sample> samples = new ArrayDeque<>();
  private double renderTime;
  private double rate = 1.0;
  private double recentMaxGap = 0.1;
  private double sinceMoved = Double.MAX_VALUE;
  private long lastTick = Long.MIN_VALUE;
  private float x;
  private float y;
  private float directionX;
  private float directionY;
  private boolean primed;

  float x() {
    return x;
  }

  float y() {
    return y;
  }

  /** True while the drawn position is moving (or only just stopped). */
  boolean moving() {
    return sinceMoved < MOVING_HOLD;
  }

  /** The drawn direction of travel in world pixels (y grows downward); zero before any motion. */
  float directionX() {
    return directionX;
  }

  float directionY() {
    return directionY;
  }

  /** Takes over another playback's route and state (the model was rebuilt, the walk goes on). */
  void copyFrom(RemotePlayerPlayback other) {
    samples.clear();
    samples.addAll(other.samples);
    renderTime = other.renderTime;
    rate = other.rate;
    recentMaxGap = other.recentMaxGap;
    sinceMoved = other.sinceMoved;
    lastTick = other.lastTick;
    x = other.x;
    y = other.y;
    directionX = other.directionX;
    directionY = other.directionY;
    primed = other.primed;
  }

  /** Puts the puppet at a place at once and forgets the route so far. */
  void place(float px, float py) {
    samples.clear();
    primed = false;
    lastTick = Long.MIN_VALUE;
    x = px;
    y = py;
    sinceMoved = Double.MAX_VALUE;
  }

  /** A position the server reported at {@code tick}. A repeated or older tick is ignored. */
  void accept(long tick, float px, float py) {
    if (lastTick != Long.MIN_VALUE && tick == lastTick) return;
    if (!primed || tick < lastTick) {
      place(px, py);
      lastTick = tick;
      primed = true;
      double t = tick / TICK_HZ;
      samples.add(new Sample(t, px, py));
      renderTime = t - MIN_LAG;
      return;
    }
    double time = tick / TICK_HZ;
    Sample newest = samples.getLast();
    double gap = time - newest.time();
    float distance = (float) Math.hypot(px - newest.x(), py - newest.y());
    if (distance > TELEPORT_DISTANCE && distance / gap > TELEPORT_SPEED) {
      place(px, py);
      lastTick = tick;
      primed = true;
      samples.add(new Sample(time, px, py));
      renderTime = time - MIN_LAG;
      return;
    }
    // How far the picture trailed the newest sample just before this one arrived.
    double lagBefore = Math.max(0, newest.time() - renderTime);
    recentMaxGap = Math.max(gap, recentMaxGap * 0.97);
    double wanted = Math.min(MAX_LAG, Math.max(MIN_LAG, 0.6 * recentMaxGap));
    rate = 1.0 + Math.max(-MAX_RATE_ERROR, Math.min(MAX_RATE_ERROR, 3.0 * (lagBefore - wanted)));
    samples.add(new Sample(time, px, py));
    lastTick = tick;
  }

  /** Moves the picture on by one frame. */
  void advance(float delta) {
    if (samples.isEmpty()) return;
    double step = Math.max(0f, delta);
    renderTime += step * rate;
    Sample newest = samples.getLast();
    if (renderTime > newest.time()) renderTime = newest.time();
    while (samples.size() >= 2) {
      Sample[] pair = firstTwo();
      if (pair[1].time() > renderTime) break;
      samples.removeFirst();
    }
    Sample[] pair = firstTwo();
    Sample from = pair[0];
    Sample to = pair[1];
    float previousX = x;
    float previousY = y;
    if (to == null || renderTime <= from.time()) {
      x = from.x();
      y = from.y();
    } else {
      float f = (float) ((renderTime - from.time()) / (to.time() - from.time()));
      x = from.x() + (to.x() - from.x()) * f;
      y = from.y() + (to.y() - from.y()) * f;
      directionX = to.x() - from.x();
      directionY = to.y() - from.y();
    }
    boolean displaced = Math.abs(x - previousX) + Math.abs(y - previousY) > 0.001f;
    sinceMoved = displaced ? 0 : (sinceMoved >= MOVING_HOLD ? sinceMoved : sinceMoved + step);
  }

  private Sample[] firstTwo() {
    var iterator = samples.iterator();
    Sample first = iterator.next();
    return new Sample[] {first, iterator.hasNext() ? iterator.next() : null};
  }
}
