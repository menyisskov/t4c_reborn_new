package com.perso.T4C.gui.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.util.List;
import java.util.Locale;

public final class GuiBoxedInteraction {
  private static final float MIN_SIZE = 8f;
  private GuiResizable active;
  private boolean resizing;
  private boolean left, right, top, bottom;
  private float grabX, grabY, fixedRight, fixedBottom;

  public boolean touchDown(List<? extends GuiElement> elements, float sx, float sy) {
    boolean shift =
        Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    boolean ctrl =
        Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    if (!shift && !ctrl) return false;
    for (int i = elements.size() - 1; i >= 0; i--) {
      GuiElement element = elements.get(i);
      if (!(element instanceof GuiResizable candidate) || !element.contains(sx, sy)) continue;
      active = candidate;
      resizing = shift;
      if (shift) selectNearestEdge(candidate, sx, sy);
      else {
        grabX = sx - candidate.getX();
        grabY = sy - candidate.getY();
      }
      return true;
    }
    return false;
  }

  public boolean dragged(float sx, float sy) {
    if (active == null) return false;
    if (!resizing) {
      active.setPosition(sx - grabX, sy - grabY);
      return true;
    }
    float x = active.getX(), y = active.getY();
    float w = active.getWidth(), h = active.getHeight();
    if (left) {
      x = Math.min(sx, fixedRight - MIN_SIZE);
      w = fixedRight - x;
    } else if (right) w = Math.max(MIN_SIZE, sx - x);
    if (top) {
      y = Math.min(sy, fixedBottom - MIN_SIZE);
      h = fixedBottom - y;
    } else if (bottom) h = Math.max(MIN_SIZE, sy - y);
    active.setPosition(x, y);
    active.setSize(w, h);
    return true;
  }

  public boolean touchUp() {
    if (active == null) return false;
    try {
      Gdx.app.log(
          "GuiScreen",
          String.format(
              Locale.ROOT,
              "%s %s to x + %.1ff, y + %.1ff, %.1ff x %.1ff",
              active.getClass().getSimpleName(),
              resizing ? "resized" : "moved",
              active.getX(),
              active.getY(),
              active.getWidth(),
              active.getHeight()));
    } catch (Throwable ignored) {
    }
    active = null;
    resizing = left = right = top = bottom = false;
    return true;
  }

  private void selectNearestEdge(GuiResizable e, float sx, float sy) {
    float l = Math.abs(sx - e.getX());
    float r = Math.abs(sx - (e.getX() + e.getWidth()));
    float t = Math.abs(sy - e.getY());
    float b = Math.abs(sy - (e.getY() + e.getHeight()));
    float nearest = Math.min(Math.min(l, r), Math.min(t, b));
    left = nearest == l;
    right = !left && nearest == r;
    top = !left && !right && nearest == t;
    bottom = !left && !right && !top;
    fixedRight = e.getX() + e.getWidth();
    fixedBottom = e.getY() + e.getHeight();
  }
}
