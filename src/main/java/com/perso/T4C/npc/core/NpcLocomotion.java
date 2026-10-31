package com.perso.T4C.npc.core;

import static com.perso.T4C.config.GameConstants.NPC_PATROL_PAUSE_MAX;
import static com.perso.T4C.config.GameConstants.NPC_PATROL_PAUSE_MIN;
import static com.perso.T4C.config.GameConstants.NPC_SPEED;

/** The original single-player patrol execution, with world and presentation behind ports. */
public final class NpcLocomotion {
  private NpcLocomotion() {}

  public record Point(float x, float y) {}

  public interface State {
    float x();

    float y();

    void position(float x, float y);

    float homeX();

    float homeY();

    Point target();

    void target(Point target);

    boolean paused();

    void paused(boolean value);

    float pauseTimer();

    void pauseTimer(float value);

    float radius();

    float nextFloat();

    boolean collisionInitialized();

    boolean collides(float x, float y);

    Point nextPathWaypoint(Point target);

    void clearPath();

    void stop();

    void direction(float x, float y);
  }

  public static void updatePatrol(State state, float delta) {
    if (state.paused()) {
      state.pauseTimer(state.pauseTimer() - delta);
      if (state.pauseTimer() <= 0) {
        state.paused(false);
        pickNewPatrolTarget(state);
      }
      state.stop();
      return;
    }

    Point target = state.target();
    if (target != null) {
      float dx = target.x - state.x();
      float dy = target.y - state.y();
      float distance = (float) Math.sqrt(dx * dx + dy * dy);
      if (distance < 2f) {
        state.position(target.x, target.y);
        state.paused(true);
        state.pauseTimer(
            state.nextFloat() * (NPC_PATROL_PAUSE_MAX - NPC_PATROL_PAUSE_MIN)
                + NPC_PATROL_PAUSE_MIN);
        state.stop();
        state.target(null);
        state.clearPath();
      } else {
        Point moveTarget = state.nextPathWaypoint(target);
        if (moveTarget == null) {
          pickNewPatrolTarget(state);
          state.stop();
          return;
        }
        dx = moveTarget.x - state.x();
        dy = moveTarget.y - state.y();
        distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance <= 0.001f) {
          state.stop();
          return;
        }
        float ndx = dx / distance;
        float ndy = dy / distance;
        float step = Math.min(NPC_SPEED * delta, distance);
        float newX = state.x() + ndx * step;
        float newY = state.y() + ndy * step;
        if (state.collisionInitialized()) {
          if (state.collides(newX, newY)) {
            pickNewPatrolTarget(state);
            state.stop();
            return;
          }
        }
        state.position(newX, newY);
        state.direction(ndx, ndy);
      }
    } else {
      pickNewPatrolTarget(state);
    }
  }

  public static void pickNewPatrolTarget(State state) {
    state.clearPath();
    float patrolRadius = state.radius();
    for (int attempt = 0; attempt < 12; attempt++) {
      float angle = state.nextFloat() * 2f * (float) Math.PI;
      float radius = state.nextFloat() * patrolRadius;
      float targetX = state.homeX() + (float) Math.cos(angle) * radius;
      float targetY = state.homeY() + (float) Math.sin(angle) * radius;
      if (!state.collisionInitialized() || !state.collides(targetX, targetY)) {
        state.target(new Point(targetX, targetY));
        return;
      }
    }
    state.target(null);
  }
}
