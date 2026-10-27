package com.perso.T4C.online;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.monster.core.MonsterMovement;

/** Render-only motion from consecutive authoritative online creature positions. */
final class OnlineCreatureMotion {
  private final Vector2 position = new Vector2();
  private final MonsterMovement movement = new MonsterMovement();
  private boolean seen;

  void snapshot(float x, float y) {
    if (!seen) {
      position.set(x, y);
      movement.stop();
      seen = true;
      return;
    }
    float dx = x - position.x;
    float dy = y - position.y;
    movement.setDirection(dx, dy);
    position.set(x, y);
  }

  void stop() {
    movement.stop();
  }

  Vector2 position() {
    return position;
  }

  String angle() {
    return movement.getCurrentAngle();
  }

  boolean flipX() {
    return movement.isFlipX();
  }

  boolean moving() {
    return movement.isMoving();
  }
}
