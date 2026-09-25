package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;
import java.util.ArrayList;
import java.util.List;

/**
 * In-game reference of every keyboard shortcut and mouse action, grouped by topic. Opened with
 * Ctrl+H or from the Options window. Every line is an i18n pair {@code controls.<topic>.<n>.keys}
 * / {@code .action}; a topic lists lines until the next number is missing.
 */
public class ControlsScreen extends GuiScreenBase {
  static final String[] TOPICS = {"movement", "windows", "quickbar", "storage", "chat", "developer"};
  private static final Color GOLD = Color.valueOf("F2B705");
  private static final Color DIM = Color.valueOf("B8AE94");
  private static final float[] SLOT_Y = {50f, 98f, 146f, 194f, 242f, 290f};
  private static final float SLOT_X = 25f;
  private static final float SLOT_W = 166f;
  private static final float SLOT_H = 34f;
  private static final float LIST_X = 244f;
  private static final float LIST_Y = 46f;
  private static final float ACTION_X = 350f;
  private static final float LINE_H = 13f;
  private static final float TIP_X = 244f;
  private static final float TIP_Y = 202f;
  private static final float TIP_W = 306f;
  private final GlyphLayout layout = new GlyphLayout();
  private final List<Rectangle> topicBounds = new ArrayList<>();
  private int selected;

  public ControlsScreen() {
    background = GuiSprites.load("GUI_BackQuest");
    centerOnScreen();
    if (background == null) return;
    addCloseButton(548f, 5f);
    for (float slotY : SLOT_Y) {
      topicBounds.add(new Rectangle(x + SLOT_X, y + slotY, SLOT_W, SLOT_H));
    }
  }

  /** The (keys, action) lines of one topic, read from the i18n catalogue. */
  static List<String[]> lines(String topic) {
    List<String[]> lines = new ArrayList<>();
    for (int i = 1; I18n.has("controls." + topic + "." + i + ".keys"); i++) {
      String base = "controls." + topic + "." + i;
      lines.add(new String[] {I18n.key(base + ".keys"), I18n.key(base + ".action")});
    }
    return lines;
  }

  @Override
  public void render(SpriteBatch batch) {
    if (background == null) {
      super.render(batch);
      return;
    }
    TextureRegion art = background;
    background = null;
    GuiDraw.drawOverlayWithPatch(batch, art, x, y, 239, 287, 318, 37, 240, 120, 150, 37);
    super.render(batch);
    background = art;
    BitmapFont title = FontManager.getInstance().getHaettenschweilerFont(18, GOLD);
    layout.setText(title, I18n.key("controls.title"));
    title.draw(batch, layout, x + 288f - layout.width / 2f, y + 3f);
    BitmapFont topicFont = FontManager.getInstance().getTahomaFont(13, Color.LIGHT_GRAY, true);
    BitmapFont topicActive = FontManager.getInstance().getTahomaFont(13, GOLD, true);
    for (int i = 0; i < TOPICS.length; i++) {
      Rectangle r = topicBounds.get(i);
      BitmapFont font = i == selected ? topicActive : topicFont;
      String name = I18n.key("controls." + TOPICS[i] + ".title");
      layout.setText(font, name);
      font.draw(batch, name, r.x + (r.width - layout.width) / 2f, r.y + 11f);
    }
    BitmapFont keys = FontManager.getInstance().getJetBrainsMonoFont(11, GOLD);
    BitmapFont action = FontManager.getInstance().getJetBrainsMonoFont(11, Color.WHITE);
    float lineY = y + LIST_Y;
    for (String[] line : lines(TOPICS[selected])) {
      keys.draw(batch, line[0], x + LIST_X, lineY);
      action.draw(batch, line[1], x + ACTION_X, lineY);
      lineY += LINE_H;
    }
    String tipKey = "controls." + TOPICS[selected] + ".tip";
    if (I18n.has(tipKey)) {
      BitmapFont tip = FontManager.getInstance().getJetBrainsMonoFont(11, DIM);
      tip.draw(batch, I18n.key(tipKey), x + TIP_X, y + TIP_Y, TIP_W, Align.left, true);
    }
  }

  @Override
  public void onTouchDown(float screenX, float screenY) {
    for (int i = 0; i < topicBounds.size(); i++) {
      if (topicBounds.get(i).contains(screenX, screenY)) {
        selected = i;
        return;
      }
    }
    super.onTouchDown(screenX, screenY);
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    if (keycode == Input.Keys.DOWN || keycode == Input.Keys.UP) {
      selected = Math.floorMod(selected + (keycode == Input.Keys.DOWN ? 1 : -1), TOPICS.length);
      return true;
    }
    return false;
  }
}
