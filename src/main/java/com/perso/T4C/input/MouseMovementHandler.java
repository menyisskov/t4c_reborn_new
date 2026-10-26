package com.perso.T4C.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.player.Player;
import java.util.function.BooleanSupplier;

/** Runs after world interactions: only an unclaimed ground press may begin mouse steering. */
public final class MouseMovementHandler extends InputAdapter {
  private final OrthographicCamera camera;
  private final Player player;
  private final BooleanSupplier blocked;
  private final Runnable onBegin;
  private final Vector2 movement = new Vector2();
  private final Vector3 target = new Vector3();
  private int pointer = -1, startX, startY;
  private boolean dragging;

  public MouseMovementHandler(
      OrthographicCamera camera, Player player, BooleanSupplier blocked, Runnable onBegin) {
    this.camera = camera;
    this.player = player;
    this.blocked = blocked;
    this.onBegin = onBegin;
  }

  @Override
  public boolean touchDown(int x, int y, int pointer, int button) {
    if (button != Input.Buttons.LEFT || pointer != 0 || blocked.getAsBoolean()) return false;
    this.pointer = pointer;
    startX = x;
    startY = y;
    dragging = false;
    return true;
  }

  @Override
  public boolean touchDragged(int x, int y, int pointer) {
    if (pointer != this.pointer || this.pointer < 0) return false;
    if (blocked.getAsBoolean()) {
      cancel();
      return false;
    }
    if (!dragging
        && (long) (x - startX) * (x - startX) + (long) (y - startY) * (y - startY) >= 36) {
      dragging = true;
      onBegin.run();
    }
    return true;
  }

  @Override
  public boolean touchUp(int x, int y, int pointer, int button) {
    if (button != Input.Buttons.LEFT || pointer != this.pointer || this.pointer < 0) return false;
    cancel();
    return true;
  }

  public void cancel() {
    pointer = -1;
    dragging = false;
    movement.setZero();
  }

  boolean isDragging() {
    return dragging;
  }

  public Vector2 direction() {
    movement.setZero();
    if (pointer < 0) return movement;
    if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT) || blocked.getAsBoolean()) {
      cancel();
      return movement;
    }
    if (!dragging) return movement;
    target.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    camera.unproject(target);
    return projectDirection(
        target.x - player.getPositionVector().x, target.y - player.getPositionVector().y, movement);
  }

  static Vector2 projectDirection(float dx, float dy, Vector2 result) {
    float gridX = dx / GameConstants.GRID_W, gridY = dy / GameConstants.GRID_H;
    if (gridX * gridX + gridY * gridY < .35f * .35f) return result.setZero();
    float angle =
        Math.round(MathUtils.atan2(gridY, gridX) / (MathUtils.PI / 4)) * (MathUtils.PI / 4);
    return GameInputHandler.projectGridDirection(
        Math.round(MathUtils.cos(angle)), Math.round(MathUtils.sin(angle)), result);
  }
}
