package com.perso.T4C.npc.core;

import static com.perso.T4C.config.GameConstants.NPC_PATROL_PAUSE_MAX;
import static com.perso.T4C.config.GameConstants.NPC_PATROL_PAUSE_MIN;
import static com.perso.T4C.config.GameConstants.NPC_SPEED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.npc.LighthavenSamaritan;
import com.perso.T4C.npc.core.NpcContext;
import java.util.Random;
import org.junit.jupiter.api.Test;

class NpcLocomotionParityTest {
  /** Frozen pre-extraction patrol reference, including call and random-draw order. */
  private static void originalTick(Fixture s, float delta) {
    if (s.paused) {
      s.pause -= delta;
      if (s.pause <= 0) {
        s.paused = false;
        originalPick(s);
      }
      s.stop();
      return;
    }
    if (s.target != null) {
      float dx = s.target.x() - s.x;
      float dy = s.target.y() - s.y;
      float distance = (float) Math.sqrt(dx * dx + dy * dy);
      if (distance < 2f) {
        s.position(s.target.x(), s.target.y());
        s.paused = true;
        s.pause = s.nextFloat() * (NPC_PATROL_PAUSE_MAX - NPC_PATROL_PAUSE_MIN)
            + NPC_PATROL_PAUSE_MIN;
        s.stop();
        s.target = null;
        s.clearPath();
      } else {
        NpcLocomotion.Point moveTarget = s.nextPathWaypoint(s.target);
        if (moveTarget == null) {
          originalPick(s);
          s.stop();
          return;
        }
        dx = moveTarget.x() - s.x;
        dy = moveTarget.y() - s.y;
        distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance <= 0.001f) {
          s.stop();
          return;
        }
        float ndx = dx / distance;
        float ndy = dy / distance;
        float step = Math.min(NPC_SPEED * delta, distance);
        float newX = s.x + ndx * step;
        float newY = s.y + ndy * step;
        if (s.collisionInitialized()) {
          if (s.collides(newX, newY)) {
            originalPick(s);
            s.stop();
            return;
          }
        }
        s.x = newX;
        s.y = newY;
        s.direction(ndx, ndy);
      }
    } else {
      originalPick(s);
    }
  }

  private static void originalPick(Fixture s) {
    s.clearPath();
    float patrolRadius = s.radius();
    for (int attempt = 0; attempt < 12; attempt++) {
      float angle = s.nextFloat() * 2f * (float) Math.PI;
      float radius = s.nextFloat() * patrolRadius;
      float targetX = s.homeX() + (float) Math.cos(angle) * radius;
      float targetY = s.homeY() + (float) Math.sin(angle) * radius;
      if (!s.collisionInitialized() || !s.collides(targetX, targetY)) {
        s.target = new NpcLocomotion.Point(targetX, targetY);
        return;
      }
    }
    s.target = null;
  }

  @Test
  void seededOriginalTraceMatchesPausePathArrivalAndUnreachableRetarget() {
    for (int mode = 0; mode < 3; mode++) {
      Fixture original = new Fixture(mode);
      Fixture extracted = new Fixture(mode);
      float[] deltas = {0.1f, 0.2f, 0.6f, 0.016f, 0.3f, 0.8f, 0.02f, 0.5f};
      for (float delta : deltas) {
        originalTick(original, delta);
        NpcLocomotion.updatePatrol(extracted, delta);
        assertEquals(original.snapshot(), extracted.snapshot(), "mode " + mode);
      }
    }
  }

  @Test
  void twelveRejectedTargetsKeepRandomDrawCount() {
    Fixture original = new Fixture(3);
    Fixture extracted = new Fixture(3);
    originalTick(original, 1f);
    NpcLocomotion.updatePatrol(extracted, 1f);
    assertEquals(original.snapshot(), extracted.snapshot());
    assertEquals(24, extracted.draws);
  }

  @Test
  void offlineNpcDelegatesAndConversationRestoresPausedPatrol() throws Exception {
    BaseNPC npc = new LighthavenSamaritan(new NpcContext(null));
    npc.setSpawnPosition(10f, 20f);
    npc.random = new Random(98123);
    npc.pauseTimer = 0.25f;
    npc.isPaused = true;
    npc.update(0.3f, null);

    Fixture reference = new Fixture(0);
    reference.radius = npc.getPatrolRadius();
    originalTick(reference, 0.3f);
    assertEquals(reference.target.x(), npc.patrolTarget.x);
    assertEquals(reference.target.y(), npc.patrolTarget.y);
    assertEquals(reference.pause, npc.pauseTimer);
    assertEquals(reference.paused, npc.isPaused);

    Vector2 selected = npc.patrolTarget;
    npc.savedPatrolTarget = selected;
    npc.savedPauseTimer = npc.pauseTimer;
    npc.wasPaused = npc.isPaused;
    npc.isInteracting = true;
    npc.patrolTarget = null;
    npc.update(0.4f, new Vector2(10f, 20f));
    assertEquals(null, npc.patrolTarget);
    npc.endInteraction();
    assertEquals(selected, npc.patrolTarget);
    npc.update(0.1f, null);
    originalTick(reference, 0.1f);
    assertEquals(reference.x, npc.position.x);
    assertEquals(reference.y, npc.position.y);
  }

  private static final class Fixture implements NpcLocomotion.State {
    private final Random random = new Random(98123);
    private final int mode;
    private float x = 10f;
    private float y = 20f;
    private NpcLocomotion.Point target;
    private boolean paused = true;
    private float pause = 0.25f;
    private int draws;
    private int stops;
    private int clears;
    private int paths;
    private float dirX;
    private float dirY;
    private float radius = 30f;

    private Fixture(int mode) {
      this.mode = mode;
      if (mode == 1) {
        paused = false;
        target = new NpcLocomotion.Point(11.9f, 20f);
      } else if (mode == 2) {
        paused = false;
        target = new NpcLocomotion.Point(50f, 50f);
      }
    }

    String snapshot() {
      return Float.floatToIntBits(x) + ":" + Float.floatToIntBits(y) + ":" + target
          + ":" + paused + ":" + Float.floatToIntBits(pause) + ":" + draws + ":"
          + stops + ":" + clears + ":" + paths + ":" + Float.floatToIntBits(dirX)
          + ":" + Float.floatToIntBits(dirY);
    }

    public float x() { return x; }
    public float y() { return y; }
    public void position(float x, float y) { this.x = x; this.y = y; }
    public float homeX() { return 10f; }
    public float homeY() { return 20f; }
    public NpcLocomotion.Point target() { return target; }
    public void target(NpcLocomotion.Point target) { this.target = target; }
    public boolean paused() { return paused; }
    public void paused(boolean value) { paused = value; }
    public float pauseTimer() { return pause; }
    public void pauseTimer(float value) { pause = value; }
    public float radius() { return radius; }
    public float nextFloat() { draws++; return random.nextFloat(); }
    public boolean collisionInitialized() { return mode == 2 || mode == 3; }
    public boolean collides(float x, float y) { return mode == 3; }
    public NpcLocomotion.Point nextPathWaypoint(NpcLocomotion.Point target) {
      paths++;
      return mode == 2 ? null : target;
    }
    public void clearPath() { clears++; }
    public void stop() { stops++; }
    public void direction(float x, float y) { dirX = x; dirY = y; }
  }
}
