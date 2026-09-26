package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.ui.FontManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * T4C-0062: "how many of each monster have I killed" - a flat, sorted, scrollable tally backed by
 * {@link QuestService#killLog(Player)}. Opened with Ctrl+K, or the "Kills" link on the Quest
 * Journal (see QuestScreen.addKillLogButton()). Purely a read-only view: nothing here writes any
 * player state.
 */
public final class MonsterKillLogScreen extends GuiScreenBase {
  private static final int VISIBLE_ROWS = 11;
  private static final float LIST_X = 29f;
  private static final float LIST_Y = 50f;
  private static final float NAME_W = 340f;
  private static final float COUNT_X = 400f;
  private static final float COUNT_W = 120f;
  private static final float ROW_H = 25f;
  private static final Color GOLD = Color.valueOf("F2B705");
  private static final Color WHITE = Color.valueOf("E6D8BC");

  private final List<Map.Entry<String, Integer>> entries = new ArrayList<>();
  private final BitmapFont listFont;
  private GuiBoxedText emptyLabel;
  private GuiBoxedText totalLabel;
  private int firstVisible;

  public MonsterKillLogScreen(Player player) {
    background = GuiSprites.load("GUI_BackQuest");
    listFont = FontManager.getInstance().getTahomaFont(13, GOLD, false);
    centerOnScreen();
    addCloseButton(548f, 1f);
    entries.addAll(QuestService.killLog(player).entrySet());
    addStaticLabels();
    addRows();
  }

  private void addStaticLabels() {
    if (background == null) return;
    var titleFont = FontManager.getInstance().getHaettenschweilerFont(17, GOLD);
    labels.add(
        new GuiBoxedText(
                titleFont, x + 200f, y + 2f, 176f, 19f, () -> I18n.key("killlog.title"), () -> GOLD)
            .shrinkToFit());
    totalLabel =
        new GuiBoxedText(
                FontManager.getInstance().getTahomaFont(12, WHITE, false),
                x + 29f,
                y + 26f,
                300f,
                16f,
                this::totalSummary,
                () -> WHITE)
            .align(GuiBoxedText.Align.LEFT)
            .shrinkToFit();
    labels.add(totalLabel);
    emptyLabel =
        new GuiBoxedText(
                FontManager.getInstance().getTahomaFont(13, WHITE, false),
                x + 29f,
                y + 150f,
                500f,
                30f,
                () -> entries.isEmpty() ? I18n.key("killlog.none") : "",
                () -> WHITE)
            .shrinkToFit();
    labels.add(emptyLabel);
  }

  private void addRows() {
    for (int row = 0; row < VISIBLE_ROWS; row++) {
      final int rowIndex = row;
      GuiBoxedText nameBox =
          new GuiBoxedText(
                  listFont,
                  x + LIST_X,
                  y + LIST_Y + row * ROW_H,
                  NAME_W,
                  ROW_H - 2f,
                  () -> rowName(rowIndex),
                  () -> WHITE)
              .align(GuiBoxedText.Align.LEFT)
              .shrinkToFit();
      GuiBoxedText countBox =
          new GuiBoxedText(
                  listFont,
                  x + COUNT_X,
                  y + LIST_Y + row * ROW_H,
                  COUNT_W,
                  ROW_H - 2f,
                  () -> rowCount(rowIndex),
                  () -> GOLD)
              .align(GuiBoxedText.Align.RIGHT)
              .shrinkToFit();
      labels.add(nameBox);
      labels.add(countBox);
    }
  }

  private Map.Entry<String, Integer> rowEntry(int row) {
    int index = firstVisible + row;
    return index >= 0 && index < entries.size() ? entries.get(index) : null;
  }

  private String rowName(int row) {
    Map.Entry<String, Integer> entry = rowEntry(row);
    if (entry == null) return "";
    MonsterDef monster = MonsterRegistry.findByName(entry.getKey());
    return monster != null ? I18n.resolve(monster.getDisplayName()) : entry.getKey();
  }

  private String rowCount(int row) {
    Map.Entry<String, Integer> entry = rowEntry(row);
    return entry == null ? "" : I18n.message("killlog.row_count", entry.getValue());
  }

  private String totalSummary() {
    if (entries.isEmpty()) return "";
    int total = 0;
    for (Map.Entry<String, Integer> entry : entries) total += entry.getValue();
    return I18n.message("killlog.total", total, entries.size());
  }

  @Override
  public void render(SpriteBatch batch) {
    if (background != null) GuiDraw.drawOverlayRegionFlipped(batch, background, x, y);
    for (GuiElement element : orderedElements()) element.render(batch);
  }

  @Override
  public void onScroll(float amountY, float screenX, float screenY) {
    int maxFirst = Math.max(0, entries.size() - VISIBLE_ROWS);
    firstVisible = Math.max(0, Math.min(maxFirst, firstVisible + (amountY > 0 ? 1 : -1)));
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    return false;
  }
}
