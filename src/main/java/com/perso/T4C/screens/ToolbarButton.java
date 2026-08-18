package com.perso.T4C.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import java.util.function.BooleanSupplier;

final class ToolbarButton {
  private static final int BUTTON_SIZE = 50;
  private static final Color UI_SURFACE = new Color(0.925f, 0.94f, 0.955f, 0.98f);
  private static final Color UI_SURFACE_HOVER = new Color(0.82f, 0.88f, 0.94f, 0.98f);
  private static final Color UI_SURFACE_PRESSED = new Color(0.72f, 0.80f, 0.88f, 0.98f);
  private static final Color UI_BORDER = new Color(0.35f, 0.40f, 0.47f, 0.90f);
  private static final Color UI_BORDER_ACTIVE = new Color(0.18f, 0.52f, 0.82f, 0.95f);
  private static final Color UI_TEXT = new Color(0.08f, 0.10f, 0.13f, 1f);
  private static final Color UI_TEXT_MUTED = new Color(0.40f, 0.45f, 0.52f, 1f);
  private static final Color UI_BLUE = new Color(0.12f, 0.45f, 0.78f, 1f);
  Rectangle bounds;
  String tooltip;
  String icon;
  Runnable action;
  Color accentColor;
  BooleanSupplier activeSupplier;
  boolean hovered = false;
  boolean pressed = false;
  long pressTime = 0;

  ToolbarButton(
      int x,
      int y,
      String tooltip,
      String icon,
      Runnable action,
      Color accentColor,
      BooleanSupplier activeSupplier) {
    this.bounds = new Rectangle(x, y, BUTTON_SIZE, BUTTON_SIZE);
    this.tooltip = tooltip;
    this.icon = icon;
    this.action = action;
    this.accentColor = accentColor;
    this.activeSupplier = activeSupplier;
  }

  void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
    boolean isPressed = pressed && (System.currentTimeMillis() - pressTime) < 150;
    boolean isActive = activeSupplier != null && activeSupplier.getAsBoolean();
    float left = bounds.x;
    float bottom = bounds.y;
    float width = bounds.width;
    float height = bounds.height;
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    Color base = UI_SURFACE;
    if (isPressed) {
      base = UI_SURFACE_PRESSED;
    } else if (hovered || isActive) {
      base = UI_SURFACE_HOVER;
    }
    shapeRenderer.setColor(base);
    shapeRenderer.rect(left, bottom, width, height);
    float accentAlpha = isActive ? 0.95f : (hovered ? 0.62f : 0.0f);
    shapeRenderer.setColor(accentColor.r, accentColor.g, accentColor.b, accentAlpha);
    shapeRenderer.rect(left, bottom, width, isActive ? 5f : 3f);
    shapeRenderer.end();
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(hovered || isActive ? UI_BORDER_ACTIVE : UI_BORDER);
    shapeRenderer.rect(left, bottom, width, height);
    shapeRenderer.end();
    batch.begin();
    String label = getButtonLabel();
    String shortcut = icon;
    float bump = isPressed ? -1f : 0f;
    font.setColor(hovered || isActive ? UI_BLUE : UI_TEXT);
    GlyphLayout shortcutLayout = new GlyphLayout(font, shortcut);
    font.draw(
        batch,
        shortcut,
        bounds.x + (bounds.width - shortcutLayout.width) / 2f,
        bounds.y + bounds.height - 12f + bump);
    float oldScaleX = font.getData().scaleX;
    float oldScaleY = font.getData().scaleY;
    font.getData().setScale(0.78f);
    font.setColor(UI_TEXT_MUTED);
    GlyphLayout labelLayout = new GlyphLayout(font, label);
    font.draw(
        batch, label, bounds.x + (bounds.width - labelLayout.width) / 2f, bounds.y + 15f + bump);
    font.getData().setScale(oldScaleX, oldScaleY);
    batch.end();
  }

  private String getButtonLabel() {
    if (tooltip == null || tooltip.isBlank()) {
      return icon;
    }
    String label = tooltip;
    int paren = label.indexOf('(');
    if (paren > 0) {
      label = label.substring(0, paren).trim();
    }
    if (label.equalsIgnoreCase("Select Sprite")) {
      return "Pick";
    }
    if (label.equalsIgnoreCase("Autofill")) {
      return "Fill";
    }
    if (label.equalsIgnoreCase("Nouvelle map")) {
      return "New";
    }
    if (label.length() > 8) {
      return label.substring(0, 8);
    }
    return label;
  }

  void onClick() {
    pressed = true;
    pressTime = System.currentTimeMillis();
    if (action != null) {
      action.run();
    }
  }
}
