package com.perso.T4C.gui.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.core.JournalWindow;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.ui.FontManager;
import java.util.List;
import java.util.function.Supplier;

/**
 * The online client's inventory, spell, potion, shop and dialogue lists, drawn in the same
 * centred journal window the single-player screens use. The first line is the title, the last
 * line (when there are at least three) is the key hint, everything between is a body row, top
 * to bottom.
 */
public final class OnlineListWindow extends JournalWindow {
  public static final int WIDTH = 600;
  public static final int HEIGHT = 380;
  /** Body rows that fit: the shop's gold line, nine choices, the page and quantity lines. */
  public static final int MAX_BODY_ROWS = 12;

  static final float ROW_X = 30f;
  static final float ROW_Y = 22f;
  static final float ROW_H = 24f;
  static final float HINT_Y = HEIGHT - 34f;

  private final Supplier<List<String>> lines;
  private final Runnable onClose;

  /** @param onClose what the window's X button does (the caller closes whichever panel is open) */
  public OnlineListWindow(Supplier<List<String>> lines, Runnable onClose) {
    this.lines = lines;
    this.onClose = onClose;
    initJournal(WIDTH, HEIGHT, null);
    journalValue(() -> line(0), 26, -46, WIDTH - 100, 28, 22, JournalTheme.GOLD);
    for (int row = 0; row < MAX_BODY_ROWS; row++) {
      final int index = row + 1;
      labels.add(rowLabel(ROW_X, ROW_Y + row * ROW_H, () -> body(index), JournalTheme.TEXT));
    }
    labels.add(rowLabel(ROW_X, HINT_Y, this::hint, JournalTheme.DIM));
  }

  private GuiBoxedText rowLabel(float dx, float dy, Supplier<String> text, Color color) {
    return new GuiBoxedText(
            FontManager.getInstance().getTahomaFont(14, color, false),
            x + dx,
            y + dy,
            WIDTH - 2 * dx,
            ROW_H - 2f,
            text,
            () -> color)
        .align(GuiBoxedText.Align.LEFT)
        .shrinkToFit();
  }

  private String line(int index) {
    var current = lines.get();
    return index < current.size() ? current.get(index) : "";
  }

  /** A body row, or nothing when the line is the hint. */
  private String body(int index) {
    var current = lines.get();
    return index < current.size() - (hasHint(current) ? 1 : 0) ? current.get(index) : "";
  }

  private String hint() {
    var current = lines.get();
    return hasHint(current) ? current.get(current.size() - 1) : "";
  }

  private static boolean hasHint(List<String> current) {
    return current.size() >= 3;
  }

  /** Y of the top of body row {@code row} (0 = first row under the title), relative to the window. */
  public static float rowTop(int row) {
    return ROW_Y + row * ROW_H;
  }

  /** Screen rectangle {x, y, width, height} of the whole window, title band included, on a screen. */
  public static float[] bounds(float screenWidth, float screenHeight) {
    float totalHeight = HEIGHT + HEADER + 24;
    float left = Math.round((screenWidth - WIDTH) / 2f);
    float top = Math.round((screenHeight - totalHeight) / 2f);
    return new float[] {left, top, WIDTH, totalHeight};
  }

  @Override
  protected void closeWindow() {
    onClose.run();
  }

  @Override
  protected void renderBody(SpriteBatch batch) {
    theme.well(batch, x + 16, y + 12, WIDTH - 32, HEIGHT - 56);
  }
}
