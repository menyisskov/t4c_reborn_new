package com.perso.T4C.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.perso.T4C.config.GamePreferencesStore;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.player.Player;
import java.util.Locale;

/** Health fills the red orb; mana encases it in a horseshoe with a value in the lower opening. */
final class CombatVitals {
  static final int SIZE = 112;
  private static final int HEIGHT = 120;
  private static final int ORB = 84;
  private static final float MANA_RADIUS = SIZE * JournalTheme.MANA_RADIUS_RATIO;
  private static final float START_ANGLE = JournalTheme.MANA_START_ANGLE;
  private static final float SWEEP_ANGLE = JournalTheme.MANA_SWEEP_ANGLE;
  private static final float ARC_STEP = 3;
  private static final int QUAD_FLOATS = 20;
  private final JournalTheme theme;
  private final TextureRegion rim;
  private final TextureRegion liquid;
  private final TextureRegion manaTrack;
  private final TextureRegion manaArc;
  private final TextureRegion manaCap;
  private final TextureRegion croppedLiquid = new TextureRegion();
  private final GlyphLayout layout = new GlyphLayout();
  private final float[] arcVertices = new float[(int) (SWEEP_ANGLE / ARC_STEP) * QUAD_FLOATS];

  CombatVitals(JournalTheme theme) {
    this.theme = theme;
    rim = theme.healthOrb(ORB, false);
    liquid = theme.healthOrb(ORB, true);
    manaTrack = theme.manaHorseshoe(SIZE, false);
    manaArc = theme.manaHorseshoe(SIZE, true);
    manaCap = theme.manaHorseshoeCap();
  }

  static float top() {
    return Gdx.graphics.getHeight() - HEIGHT - 12;
  }

  static float left() {
    return Gdx.graphics.getWidth() * .5f - SIZE * .5f;
  }

  void render(SpriteBatch batch, Player player) {
    float left = left();
    float top = top();
    GuiDraw.drawRegionFlipped(batch, manaTrack, left, top);
    float manaRatio = ratio(player.getMana(), player.getMaxMana());
    int count = writeManaArc(arcVertices, left, top, manaRatio, batch.getPackedColor());
    if (count > 0) {
      batch.draw(manaArc.getTexture(), arcVertices, 0, count);
      renderManaCap(batch, left, top, START_ANGLE);
      renderManaCap(batch, left, top, START_ANGLE + SWEEP_ANGLE * manaRatio);
    }
    float orbX = left + (SIZE - ORB) * .5f;
    float orbY = top + (SIZE - ORB) * .5f;
    GuiDraw.drawRegionFlipped(batch, rim, orbX, orbY);
    int fillHeight = Math.round(ORB * ratio(player.getCurrentHp(), player.getMaxHp()));
    if (fillHeight > 0) {
      croppedLiquid.setRegion(liquid, 0, ORB - fillHeight, ORB, fillHeight);
      GuiDraw.drawRegionFlipped(batch, croppedLiquid, orbX, orbY + ORB - fillHeight);
    }
    boolean showValues = GamePreferencesStore.get().isShowHudValues();
    String healthValue =
        showValues
            ? String.format(Locale.ROOT, "%,d", player.getCurrentHp())
            : Math.round(ratio(player.getCurrentHp(), player.getMaxHp()) * 100) + "%";
    String manaValue =
        showValues
            ? String.format(Locale.ROOT, "%,d", player.getMana())
            : Math.round(manaRatio * 100) + "%";
    centered(
        batch,
        FontManager.getInstance().getTahomaFont(18, Color.WHITE, true),
        healthValue,
        left + 19,
        top + 45,
        74,
        22);
    centered(
        batch,
        FontManager.getInstance().getTahomaFont(11, Color.valueOf("B7E9FF"), false),
        manaValue,
        left + 24,
        top + 92,
        64,
        19);
    theme.rect(batch, left + 24, top + SIZE + 5, SIZE - 48, 2, JournalTheme.LINE);
    theme.rect(
        batch,
        left + 24,
        top + SIZE + 5,
        (SIZE - 48) * ratio(player.getCurrentXp(), player.getXpToNextLevel()),
        2,
        JournalTheme.GOLD);
  }

  /**
   * Clip the prepainted arc with a radial strip; no textures or buffers are recreated per frame.
   */
  static int writeManaArc(float[] vertices, float x, float y, float fraction, float color) {
    float sweep = SWEEP_ANGLE * Math.max(0, Math.min(1, fraction));
    if (sweep <= 0) return 0;
    int segments = (int) Math.ceil(sweep / ARC_STEP);
    int index = 0;
    for (int i = 0; i < segments; i++) {
      float start = START_ANGLE + sweep * i / segments;
      float end = START_ANGLE + sweep * (i + 1) / segments;
      index = vertex(vertices, index, x, y, start, SIZE * .5f, color);
      index = vertex(vertices, index, x, y, start, SIZE * .35f, color);
      index = vertex(vertices, index, x, y, end, SIZE * .35f, color);
      index = vertex(vertices, index, x, y, end, SIZE * .5f, color);
    }
    return index;
  }

  private static int vertex(
      float[] vertices, int index, float x, float y, float angle, float radius, float color) {
    float lx = SIZE * .5f + MathUtils.cosDeg(angle) * radius;
    float ly = SIZE * .5f + MathUtils.sinDeg(angle) * radius;
    vertices[index++] = x + lx;
    vertices[index++] = y + ly;
    vertices[index++] = color;
    vertices[index++] = lx / SIZE;
    vertices[index++] = ly / SIZE;
    return index;
  }

  private void renderManaCap(SpriteBatch batch, float x, float y, float angle) {
    float lx = SIZE * .5f + MathUtils.cosDeg(angle) * MANA_RADIUS;
    float ly = SIZE * .5f + MathUtils.sinDeg(angle) * MANA_RADIUS;
    float t = ly / SIZE;
    float previous = batch.getPackedColor();
    float alpha = batch.getColor().a;
    batch.setColor(
        MathUtils.lerp(160, 19, t) / 255f,
        MathUtils.lerp(232, 97, t) / 255f,
        MathUtils.lerp(255, 162, t) / 255f,
        alpha);
    GuiDraw.drawRegionFlipped(batch, manaCap, x + lx - 8, y + ly - 8);
    batch.setPackedColor(previous);
  }

  static float ratio(long value, long maximum) {
    return maximum <= 0 ? 0 : Math.max(0, Math.min(1, (float) value / maximum));
  }

  private void centered(
      SpriteBatch batch,
      BitmapFont font,
      String text,
      float x,
      float y,
      float width,
      float height) {
    layout.setText(font, text);
    float sx = font.getData().scaleX, sy = font.getData().scaleY;
    if (layout.width > width) {
      font.getData().setScale(sx * width / layout.width, sy * width / layout.width);
      layout.setText(font, text);
    }
    font.draw(batch, layout, x + (width - layout.width) / 2, y + (height - layout.height) / 2);
    font.getData().setScale(sx, sy);
  }

  boolean contains(float x, float y) {
    float left = left();
    return x >= left && x <= left + SIZE && y >= top() && y <= top() + HEIGHT;
  }
}
