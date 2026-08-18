package com.perso.T4C.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.perso.T4C.editor.ui.EditorDialog;
import com.perso.T4C.editor.ui.EditorPanelChrome;
import com.perso.T4C.editor.ui.EditorTheme;

final class TeleportGotoDialog extends EditorDialog {
  interface SubmitHandler {
    void submit(int tileX, int tileY, int z);
  }

  private final StringBuilder xValue = new StringBuilder();
  private final StringBuilder yValue = new StringBuilder();
  private final StringBuilder zValue = new StringBuilder();
  private final SubmitHandler submitHandler;
  private final Runnable cancelHandler;
  private final Rectangle xBounds = new Rectangle();
  private final Rectangle yBounds = new Rectangle();
  private final Rectangle zBounds = new Rectangle();
  private final Rectangle okBounds = new Rectangle();
  private final Rectangle cancelBounds = new Rectangle();
  private int activeField;

  TeleportGotoDialog(int initialZ, SubmitHandler submitHandler, Runnable cancelHandler) {
    super("Teleport");
    this.submitHandler = submitHandler;
    this.cancelHandler = cancelHandler;
    zValue.append(initialZ);
  }

  void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
    int screenWidth = Gdx.graphics.getWidth();
    int screenHeight = Gdx.graphics.getHeight();
    layout(screenWidth, screenHeight);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    EditorPanelChrome.overlay(shapeRenderer, screenWidth, screenHeight);
    EditorPanelChrome.panel(shapeRenderer, bounds, EditorTheme.ORANGE, 36f);
    EditorPanelChrome.textField(shapeRenderer, xBounds, activeField == 0);
    EditorPanelChrome.textField(shapeRenderer, yBounds, activeField == 1);
    EditorPanelChrome.textField(shapeRenderer, zBounds, activeField == 2);
    EditorPanelChrome.button(shapeRenderer, okBounds, true, false);
    EditorPanelChrome.button(shapeRenderer, cancelBounds, true, false);
    shapeRenderer.end();
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    EditorPanelChrome.border(shapeRenderer, bounds);
    EditorPanelChrome.border(shapeRenderer, xBounds);
    EditorPanelChrome.border(shapeRenderer, yBounds);
    EditorPanelChrome.border(shapeRenderer, zBounds);
    EditorPanelChrome.border(shapeRenderer, okBounds);
    EditorPanelChrome.border(shapeRenderer, cancelBounds);
    shapeRenderer.end();
    batch.begin();
    font.setColor(EditorTheme.TEXT_LIGHT);
    font.draw(batch, title(), bounds.x + 12f, bounds.y + bounds.height - 12f);
    font.draw(batch, "X", bounds.x + 24f, bounds.y + 162f);
    font.draw(batch, "Y", bounds.x + 24f, bounds.y + 122f);
    font.draw(batch, "Z", bounds.x + 24f, bounds.y + 82f);
    font.setColor(EditorTheme.TEXT);
    font.draw(batch, xValue.toString(), xBounds.x + 8f, xBounds.y + 20f);
    font.draw(batch, yValue.toString(), yBounds.x + 8f, yBounds.y + 20f);
    font.draw(batch, zValue.toString(), zBounds.x + 8f, zBounds.y + 20f);
    EditorPanelChrome.buttonText(batch, font, okBounds, "Teleport", EditorTheme.TEXT_LIGHT);
    EditorPanelChrome.buttonText(batch, font, cancelBounds, "Cancel", EditorTheme.TEXT_LIGHT);
    batch.end();
  }

  @Override
  public boolean handleClick(int screenX, int screenY, int button) {
    if (button != Input.Buttons.LEFT) {
      return true;
    }
    int y = Gdx.graphics.getHeight() - screenY;
    if (xBounds.contains(screenX, y)) {
      activeField = 0;
    } else if (yBounds.contains(screenX, y)) {
      activeField = 1;
    } else if (zBounds.contains(screenX, y)) {
      activeField = 2;
    } else if (okBounds.contains(screenX, y)) {
      submit();
    } else if (cancelBounds.contains(screenX, y)) {
      cancel();
    }
    return true;
  }

  @Override
  public boolean handleKeyTyped(char character) {
    StringBuilder target = activeValue();
    if (character == '\b') {
      if (target.length() > 0) {
        target.setLength(target.length() - 1);
      }
      return true;
    }
    if (character == '\r' || character == '\n') {
      submit();
      return true;
    }
    if ((character >= '0' && character <= '9') || (character == '-' && target.length() == 0)) {
      target.append(character);
    }
    return true;
  }

  @Override
  public boolean handleKeyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) {
      cancel();
      return true;
    }
    if (keycode == Input.Keys.ENTER) {
      submit();
      return true;
    }
    if (keycode == Input.Keys.TAB || keycode == Input.Keys.DOWN) {
      activeField = (activeField + 1) % 3;
      return true;
    }
    if (keycode == Input.Keys.UP) {
      activeField = (activeField + 2) % 3;
      return true;
    }
    return true;
  }

  private void layout(int screenWidth, int screenHeight) {
    float w = 360f;
    float h = 230f;
    float x = (screenWidth - w) * 0.5f;
    float y = (screenHeight - h) * 0.5f;
    bounds.set(x, y, w, h);
    xBounds.set(x + 90f, y + 142f, 240f, 30f);
    yBounds.set(x + 90f, y + 102f, 240f, 30f);
    zBounds.set(x + 90f, y + 62f, 240f, 30f);
    okBounds.set(x + 90f, y + 16f, 110f, 30f);
    cancelBounds.set(x + 220f, y + 16f, 110f, 30f);
  }

  private StringBuilder activeValue() {
    return activeField == 0 ? xValue : activeField == 1 ? yValue : zValue;
  }

  private void submit() {
    try {
      submitHandler.submit(
          Integer.parseInt(xValue.toString().trim()),
          Integer.parseInt(yValue.toString().trim()),
          Integer.parseInt(zValue.toString().trim()));
    } catch (NumberFormatException e) {
      submitHandler.submit(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
  }

  private void cancel() {
    close();
    if (cancelHandler != null) {
      cancelHandler.run();
    }
  }
}
