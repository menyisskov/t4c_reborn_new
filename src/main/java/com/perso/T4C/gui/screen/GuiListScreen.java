package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.gui.core.GuiClickZone;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.core.JournalWindow;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.HudTooltip;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class GuiListScreen extends JournalWindow {
  private final HudTooltip blockedTooltip = new HudTooltip();
  // T4C-0054: rebuildList() recreates every row's spin buttons from scratch (to refresh
  // prices/counts), which would otherwise reset a button's press-and-hold state mid-hold. Rows
  // keep the same rowY across a rebuild (same index -> same slot), so this survives replacement
  // by carrying hold state forward keyed on that row position.
  private final Map<Float, GuiButton> spinUpByRowY = new HashMap<>();
  private final Map<Float, GuiButton> spinDownByRowY = new HashMap<>();

  public interface ListRow {
    String nameText();

    String priceText();

    String thirdColumnText();

    String iconSprite();

    int getCount();
  }

  protected static final int ROWS_VISIBLE = 6;
  protected static final float ROW_0_Y = 48f;
  protected static final float ROW_H_PITCH = 52f;
  protected static final float[] TITLE_BOX = {26f, -46f, 650f, 28f};
  protected static final float[] HDR_NAME_BOX = {70f, 12f, 220f, 20f};
  protected static final float[] HDR_PRICE_BOX = {298f, 12f, 82f, 20f};
  protected static final float[] HDR_THIRD_BOX = {392f, 12f, 62f, 20f};
  protected static final float[] CELL_NAME = {70f, -7f, 220f, 34f};
  protected static final float[] CELL_PRICE = {298f, 0f, 82f, 20f};
  protected static final float[] CELL_THIRD = {392f, 0f, 62f, 20f};
  protected static final float[] ICON_BOX = {26f, -6f, 34f, 34f};
  protected static final float SCROLL_X = 548f;
  protected static final float SCROLL_W = 30f;
  protected static final float SCROLL_UP_Y = 40f;
  protected static final float SCROLL_DN_Y = 320f;
  protected static final float SCROLL_BTN_H = 32f;
  protected static final float[] GOLD_HDR_BOX = {596f, 10f, 180f, 20f};
  protected static final float[] ONHAND_LBL_BOX = {596f, 46f, 180f, 18f};
  protected static final float[] ONHAND_VAL_BOX = {596f, 68f, 180f, 24f};
  protected static final float[] COST_LBL_BOX = {596f, 116f, 180f, 18f};
  protected static final float[] COST_VAL_BOX = {596f, 138f, 180f, 24f};
  protected static final float[] TOTAL_LBL_BOX = {596f, 184f, 180f, 18f};
  protected static final float[] TOTAL_VAL_BOX = {596f, 206f, 180f, 24f};
  protected static final float ACTION_BTN_X = 596f;
  protected static final float ACTION_BTN_Y = 336f;
  protected static final float SPIN_X = 486f;
  protected static final float SPIN_UP_DY = 0f;
  protected static final float SPIN_DN_DY = 0f;
  protected static final float CLOSE_X = 768f;
  protected static final float CLOSE_Y = -46f;
  protected static final Color GOLD = JournalTheme.GOLD;
  protected static final Color WHITE = JournalTheme.TEXT;
  protected static final Color DIM = JournalTheme.DIM;
  protected final List<GuiText> dynLabels = new ArrayList<>();
  protected final List<GuiButton> dynButtons = new ArrayList<>();
  protected final List<GuiClickZone> zones = new ArrayList<>();
  protected int page;

  protected abstract List<? extends ListRow> rows();

  protected abstract void basketAdd(ListRow row);

  protected abstract void basketRemove(ListRow row);

  protected abstract void rebuildList();

  protected Color blockedColor() {
    return Color.valueOf("B03030");
  }

  protected float rowZoneWidth() {
    return 450f;
  }

  protected boolean pageTurnSound() {
    return false;
  }

  protected String blockedReason(ListRow row) {
    return null;
  }

  protected String rowTooltipReason(ListRow row) {
    return blockedReason(row);
  }

  /** Extra info shown on hover below the row's name, e.g. a spell's description/requirements. */
  protected String rowInfoText(ListRow row) {
    return null;
  }

  protected String rowDisplayName(ListRow row) {
    return row == null ? "" : row.nameText();
  }

  @Override
  public void render(SpriteBatch batch) {
    super.render(batch);
    updateBlockedTooltip();
    blockedTooltip.render(batch);
  }

  private void updateBlockedTooltip() {
    float mouseX = Gdx.input.getX();
    float mouseY = Gdx.input.getY();
    int start = page * ROWS_VISIBLE;
    int end = Math.min(start + ROWS_VISIBLE, rows().size());
    for (int i = start; i < end; i++) {
      float rowY = y + ROW_0_Y + (i - start) * ROW_H_PITCH;
      if (mouseX < x + 10f
          || mouseX > x + rowZoneWidth()
          || mouseY < rowY - 4f
          || mouseY > rowY + ROW_H_PITCH - 8f) continue;
      ListRow row = rows().get(i);
      StringBuilder text = new StringBuilder(rowDisplayName(row));
      String info = rowInfoText(row);
      if (info != null && !info.isBlank()) {
        text.append('\n').append(info);
      }
      String reason = rowTooltipReason(row);
      if (reason != null && !reason.isBlank()) {
        text.append('\n').append(reason);
      }
      blockedTooltip.show(text.toString(), mouseX, mouseY);
      return;
    }
    blockedTooltip.clear();
  }

  protected void initList() {
    initJournal(820, 410, null);
    journalValue(
        () -> rows().isEmpty() ? I18n.key("ui.service_empty") : "", 30, 108, 500, 40, 13, DIM);
    journalValue(
        () -> {
          ListRow row = selectedRow();
          if (row == null) return I18n.key("ui.service_hint");
          String reason = rowTooltipReason(row);
          return reason == null ? rowDisplayName(row) : rowDisplayName(row) + " — " + reason;
        },
        28,
        366,
        520,
        34,
        11,
        DIM);
  }

  protected ListRow selectedRow() {
    return null;
  }

  protected boolean canIncrement(ListRow row) {
    return blockedReason(row) == null;
  }

  @Override
  protected void renderBody(SpriteBatch batch) {
    theme.well(batch, x + 584, y + 36, 208, 342);
    int start = page * ROWS_VISIBLE;
    int end = Math.min(start + ROWS_VISIBLE, rows().size());
    for (int i = start; i < end; i++) {
      float ry = y + ROW_0_Y + (i - start) * ROW_H_PITCH - 11;
      if (rows().get(i) == selectedRow()) theme.accentWell(batch, x + 18, ry, 522, 46);
      else theme.well(batch, x + 18, ry, 522, 46);
    }
    theme.well(batch, x + 18, y + 360, 540, 43);
  }

  protected void addScrollThumb(int pages) {
    GuiButton up =
        theme.button(x + SCROLL_X, y + SCROLL_UP_Y, 26, 28, () -> "↑", () -> scrollPages(-1));
    GuiButton down =
        theme.button(x + SCROLL_X, y + SCROLL_DN_Y, 26, 28, () -> "↓", () -> scrollPages(1));
    up.setEnabled(page > 0);
    down.setEnabled(page < pages - 1);
    buttons.add(up);
    buttons.add(down);
    dynButtons.add(up);
    dynButtons.add(down);
  }

  protected GuiBoxedText boxed(
      BitmapFont font, float[] box, float dy, Supplier<String> text, Color color) {
    boolean numeric =
        box == CELL_PRICE || box == CELL_THIRD || box == HDR_PRICE_BOX || box == HDR_THIRD_BOX;
    BitmapFont actualFont =
        box == ONHAND_VAL_BOX || box == COST_VAL_BOX || box == TOTAL_VAL_BOX
            ? com.perso.T4C.ui.FontManager.getInstance().getTahomaFont(16, color, false)
            : font;
    return new GuiBoxedText(
            actualFont, x + box[0], y + box[1] + dy, box[2], box[3], text, () -> color)
        .align(numeric ? GuiBoxedText.Align.RIGHT : GuiBoxedText.Align.LEFT)
        .wrap()
        .shrinkToFit();
  }

  protected void addDyn(
      BitmapFont font, float[] box, float dy, Supplier<String> text, Color color) {
    GuiText label = boxed(font, box, dy, text, color);
    labels.add(label);
    dynLabels.add(label);
  }

  protected void addRowSpinButtons(float rowY, ListRow row) {
    GuiButton up =
        theme
            .button(x + SPIN_X + 28, y + rowY - 2, 24, 28, () -> "+", () -> basketAdd(row))
            .repeatable(true);
    GuiButton down =
        theme
            .button(x + SPIN_X, y + rowY - 2, 24, 28, () -> "−", () -> basketRemove(row))
            .repeatable(true);
    up.adoptHoldStateFrom(spinUpByRowY.get(rowY));
    down.adoptHoldStateFrom(spinDownByRowY.get(rowY));
    spinUpByRowY.put(rowY, up);
    spinDownByRowY.put(rowY, down);
    up.setEnabled(canIncrement(row));
    down.setEnabled(row.getCount() > 0);
    buttons.add(up);
    buttons.add(down);
    dynButtons.add(up);
    dynButtons.add(down);
  }

  protected void scrollPages(int delta) {
    int pages = Math.max(1, (rows().size() + ROWS_VISIBLE - 1) / ROWS_VISIBLE);
    int target = Math.min(Math.max(0, page + delta), pages - 1);
    if (target != page) {
      page = target;
      if (pageTurnSound()) {
        com.perso.T4C.audio.SoundManager.animateSound("Page turning sound.wav");
      }
      rebuildList();
    }
  }

  @Override
  public void dispose() {
    blockedTooltip.dispose();
    super.dispose();
  }

  @Override
  public void onTouchUp(float screenX, float screenY) {
    if (buttons.stream().anyMatch(button -> button.contains(screenX, screenY))) {
      super.onTouchUp(screenX, screenY);
      return;
    }
    for (GuiClickZone zone : new ArrayList<>(zones)) {
      if (zone.contains(screenX, screenY)) {
        zone.run();
        return;
      }
    }
    super.onTouchUp(screenX, screenY);
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    return super.onKeyDown(keycode);
  }
}
