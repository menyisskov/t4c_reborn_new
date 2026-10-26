package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.core.JournalWindow;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;
import java.util.ArrayList;
import java.util.List;

/** The same keyboard reference, with wrapped, scrollable journal pages. */
public class ControlsScreen extends JournalWindow {
  static final String[] TOPICS = {
    "movement", "windows", "quickbar", "storage", "chat", "developer"
  };
  private final List<Rectangle> topics = new ArrayList<>();
  private int selected;
  private float scroll;
  private float maxScroll;

  public ControlsScreen() {
    initJournal(820, 430, "controls.title");
    for (int i = 0; i < TOPICS.length; i++)
      topics.add(new Rectangle(x + 22, y + 10 + i * 58, 174, 44));
    journalValue(() -> I18n.key("ui.scroll_details"), 230, 397, 550, 22, 11, JournalTheme.DIM);
  }

  static List<String[]> lines(String topic) {
    List<String[]> result = new ArrayList<>();
    for (int i = 1; I18n.has("controls." + topic + "." + i + ".keys"); i++) {
      String base = "controls." + topic + "." + i;
      result.add(new String[] {I18n.key(base + ".keys"), I18n.key(base + ".action")});
    }
    return result;
  }

  @Override
  protected void renderBody(SpriteBatch batch) {
    var text = FontManager.getInstance().getTahomaFont(13, JournalTheme.TEXT, false);
    var keyFont = FontManager.getInstance().getJetBrainsMonoFont(12, JournalTheme.GOLD);
    for (int i = 0; i < topics.size(); i++) {
      Rectangle r = topics.get(i);
      if (i == selected) theme.accentWell(batch, r.x, r.y, r.width, r.height);
      else theme.well(batch, r.x, r.y, r.width, r.height);
      var label = new GlyphLayout(text, I18n.key("controls." + TOPICS[i] + ".title"));
      text.draw(batch, label, r.x + (r.width - label.width) / 2, r.y + 16);
    }
    theme.well(batch, x + 214, y + 4, 580, 384);
    var rows = lines(TOPICS[selected]);
    List<GlyphLayout> keys = new ArrayList<>(), actions = new ArrayList<>();
    float total = 0;
    for (String[] row : rows) {
      var key = new GlyphLayout(keyFont, row[0], JournalTheme.GOLD, 150, Align.left, true);
      var action = new GlyphLayout(text, row[1], JournalTheme.TEXT, 370, Align.left, true);
      keys.add(key);
      actions.add(action);
      total += Math.max(Math.abs(key.height), Math.abs(action.height)) + 23;
    }
    String tipKey = "controls." + TOPICS[selected] + ".tip";
    GlyphLayout tip =
        I18n.has(tipKey)
            ? new GlyphLayout(text, I18n.key(tipKey), JournalTheme.DIM, 530, Align.left, true)
            : null;
    if (tip != null) total += Math.abs(tip.height) + 30;
    maxScroll = Math.max(0, total - 350);
    scroll = Math.min(scroll, maxScroll);
    batch.flush();
    Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
    Gdx.gl.glScissor(Math.round(x + 226), Math.round(Gdx.graphics.getHeight() - y - 378), 556, 360);
    try {
      float cursor = y + 20 - scroll;
      for (int i = 0; i < rows.size(); i++) {
        keyFont.draw(batch, keys.get(i), x + 230, cursor);
        text.draw(batch, actions.get(i), x + 395, cursor);
        cursor += Math.max(Math.abs(keys.get(i).height), Math.abs(actions.get(i).height)) + 23;
      }
      if (tip != null) text.draw(batch, tip, x + 230, cursor + 8);
      batch.flush();
    } finally {
      Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }
    if (maxScroll > 0)
      theme.rect(batch, x + 784, y + 16 + 320 * scroll / maxScroll, 2, 36, JournalTheme.DIM);
  }

  @Override
  public void onTouchDown(float sx, float sy) {
    for (int i = 0; i < topics.size(); i++) {
      if (topics.get(i).contains(sx, sy)) {
        selected = i;
        scroll = 0;
        return;
      }
    }
    super.onTouchDown(sx, sy);
  }

  @Override
  public void onScroll(float amount, float sx, float sy) {
    if (sx >= x + 214 && sx <= x + 794 && sy >= y && sy <= y + 388)
      scroll = Math.max(0, Math.min(maxScroll, scroll + amount * 36));
  }

  @Override
  public boolean onKeyDown(int key) {
    if (key == Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    if (key == Input.Keys.UP || key == Input.Keys.DOWN) {
      selected = Math.floorMod(selected + (key == Input.Keys.DOWN ? 1 : -1), TOPICS.length);
      scroll = 0;
      return true;
    }
    return false;
  }
}
