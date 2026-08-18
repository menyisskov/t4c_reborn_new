package com.perso.T4C.movement;

import com.badlogic.gdx.math.Vector2;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class BaseMovement {
  protected String currentAngle = "000";
  protected boolean isMoving = false;
  protected boolean flipX = false;

  public void setDirection(float dx, float dy) {
    if (dx == 0 && dy == 0) {
      isMoving = false;
      return;
    }
    isMoving = true;
    float worldDy = -dy;
    computeAngleFlip(dx, worldDy);
  }

  public void faceToward(Vector2 from, Vector2 to) {
    float dx = to.x - from.x;
    float dy = to.y - from.y;
    float worldDy = -dy;
    computeAngleFlip(dx, worldDy);
  }

  public void stop() {
    isMoving = false;
  }

  protected void computeAngleFlip(float dx, float worldDy) {
    String oldAngle = currentAngle;
    boolean oldFlip = flipX;
    final float TOLERANCE = 0.3f;
    boolean dxZero = Math.abs(dx) < TOLERANCE;
    boolean dyZero = Math.abs(worldDy) < TOLERANCE;
    if (dxZero && worldDy < 0) {
      currentAngle = "000";
      flipX = false;
    } else if (dxZero && worldDy > 0) {
      currentAngle = "180";
      flipX = false;
    } else if (dx < 0 && dyZero) {
      currentAngle = "090";
      flipX = false;
    } else if (dx > 0 && dyZero) {
      currentAngle = "090";
      flipX = true;
    } else if (dx < 0 && worldDy < 0) {
      currentAngle = "045";
      flipX = false;
    } else if (dx > 0 && worldDy < 0) {
      currentAngle = "045";
      flipX = true;
    } else if (dx < 0 && worldDy > 0) {
      currentAngle = "135";
      flipX = false;
    } else if (dx > 0 && worldDy > 0) {
      currentAngle = "135";
      flipX = true;
    }
    if (!oldAngle.equals(currentAngle) || oldFlip != flipX) {
      logDirectionChange(dx, worldDy);
    }
  }

  protected void logDirectionChange(float dx, float worldDy) {
    log.debug(
        "Direction: dx={}, worldDy={} angle={}, flipX={}",
        String.format("%.2f", dx),
        String.format("%.2f", worldDy),
        currentAngle,
        flipX);
  }
}
