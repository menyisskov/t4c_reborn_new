package com.perso.T4C.editor.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import lombok.Getter;
import lombok.Setter;

public abstract class EditorComponent {
  protected final Rectangle bounds = new Rectangle();
  @Getter @Setter protected boolean visible = true;

  public Rectangle bounds() {
    return bounds;
  }

  public void setBounds(float x, float y, float width, float height) {
    bounds.set(x, y, width, height);
  }

  public boolean contains(float x, float y) {
    return visible && bounds.contains(x, y);
  }

  public void close() {
    visible = false;
  }

  public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {}

  public boolean handleClick(int screenX, int screenY, int button) {
    return false;
  }

  public boolean handleScroll(float amount) {
    return false;
  }

  public boolean handleKeyDown(int keycode) {
    return false;
  }

  public boolean handleKeyTyped(char character) {
    return false;
  }
}
