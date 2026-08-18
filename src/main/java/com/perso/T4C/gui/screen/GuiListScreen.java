package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.gui.core.GuiClickZone;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.ui.HudTooltip;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class GuiListScreen extends GuiScreenBase {
  private final HudTooltip blockedTooltip = new HudTooltip();

  public interface ListRow {
    String nameText();

    String priceText();

    String thirdColumnText();

    String iconSprite();

    int getCount();
  }

  protected static final int ROWS_VISIBLE = 6;
  protected static final float ROW_0_Y = 60f;
  protected static final float ROW_H_PITCH = 46f;
  protected static final float[] TITLE_BOX = {237f, 2f, 101f, 19f};
  protected static final float[] HDR_NAME_BOX = {114f, 40f, 104f, 16f};
  protected static final float[] HDR_PRICE_BOX = {272f, 40f, 41f, 16f};
  protected static final float[] HDR_THIRD_BOX = {332f, 40f, 41f, 16f};
  protected static final float[] CELL_NAME = {77f, 0f, 177f, 16f};
  protected static final float[] CELL_PRICE = {259f, 0f, 67f, 16f};
  protected static final float[] CELL_THIRD = {331f, 0f, 43f, 16f};
  protected static final float[] ICON_BOX = {21f, -12f, 40f, 40f};
  protected static final float SCROLL_X = 400f;
  protected static final float SCROLL_W = 30f;
  protected static final float SCROLL_UP_Y = 30f;
  protected static final float SCROLL_DN_Y = 280f;
  protected static final float SCROLL_BTN_H = 32f;
  protected static final float[] GOLD_HDR_BOX = {457f, 44f, 94f, 17f};
  protected static final float[] ONHAND_LBL_BOX = {457f, 82f, 94f, 17f};
  protected static final float[] ONHAND_VAL_BOX = {457f, 102f, 94f, 15f};
  protected static final float[] COST_LBL_BOX = {457f, 131f, 94f, 17f};
  protected static final float[] COST_VAL_BOX = {457f, 151f, 94f, 15f};
  protected static final float[] TOTAL_LBL_BOX = {457f, 180f, 94f, 17f};
  protected static final float[] TOTAL_VAL_BOX = {457f, 200f, 94f, 15f};
  protected static final float ACTION_BTN_X = 472f;
  protected static final float ACTION_BTN_Y = 284f;
  protected static final float SPIN_X = 382f;
  protected static final float SPIN_UP_DY = -5f;
  protected static final float SPIN_DN_DY = 8f;
  protected static final float CLOSE_X = 552f;
  protected static final float CLOSE_Y = 0f;
  protected static final Color GOLD = Color.valueOf("F2B705");
  protected static final Color WHITE = Color.WHITE;
  protected static final Color DIM = Color.valueOf("888888");
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
    return 360f;
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
      String reason = rowTooltipReason(row);
      if (reason != null && !reason.isBlank()) {
        blockedTooltip.show(rowDisplayName(row) + "\n" + reason, mouseX, mouseY);
      } else {
        blockedTooltip.clear();
      }
      return;
    }
    blockedTooltip.clear();
  }

  protected void addScrollThumb(int pages) {}

  protected GuiBoxedText boxed(
      BitmapFont font, float[] box, float dy, Supplier<String> text, Color color) {
    return new GuiBoxedText(font, x + box[0], y + box[1] + dy, box[2], box[3], text, () -> color);
  }

  protected void addDyn(
      BitmapFont font, float[] box, float dy, Supplier<String> text, Color color) {
    GuiText label = boxed(font, box, dy, text, color);
    labels.add(label);
    dynLabels.add(label);
  }

  protected void addRowSpinButtons(float rowY, ListRow row) {
    var upN = GuiSprites.load("GUI_SpinUp");
    var upH = GuiSprites.load("GUI_SpinHUp");
    var dnN = GuiSprites.load("GUI_SpinDown");
    var dnH = GuiSprites.load("GUI_SpinHDown");
    if (upN == null || dnN == null) {
      return;
    }
    GuiButton up =
        new GuiButton(
            upN,
            upH != null ? upH : upN,
            upH != null ? upH : upN,
            x + SPIN_X,
            y + rowY + SPIN_UP_DY,
            () -> basketAdd(row));
    GuiButton down =
        new GuiButton(
            dnN,
            dnH != null ? dnH : dnN,
            dnH != null ? dnH : dnN,
            x + SPIN_X,
            y + rowY + SPIN_DN_DY,
            () -> basketRemove(row));
    boolean enabled = blockedReason(row) == null;
    up.setEnabled(enabled);
    down.setEnabled(enabled);
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
  public void onTouchUp(float screenX, float screenY) {
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
