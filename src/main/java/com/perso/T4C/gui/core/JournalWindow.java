package com.perso.T4C.gui.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;
import java.util.function.Supplier;

/** Shared, independently owned journal chrome for services and small dialogs. */
public abstract class JournalWindow extends GuiScreenBase {
  protected final JournalTheme theme = new JournalTheme();
  protected int windowWidth;
  protected int contentHeight;
  protected static final int HEADER = 64;

  /** x/y remain the content origin; hit testing includes the title band above it. */
  protected final void initJournal(int width, int height, String titleKey) {
    windowWidth = width;
    contentHeight = height;
    background = theme.windowFrame(width, height + HEADER + 24);
    x = Math.round((Gdx.graphics.getWidth() - width) / 2f);
    y = Math.round((Gdx.graphics.getHeight() - background.getRegionHeight()) / 2f) + HEADER;
    if (titleKey != null)
      journalValue(() -> I18n.key(titleKey), 26, -46, width - 100, 28, 22, JournalTheme.GOLD);
    buttons.add(theme.button(x + width - 52, y - 46, 28, 28, () -> "X", this::closeWindow));
  }

  protected void closeWindow() {
    GuiManager.close();
  }

  protected GuiButton journalButton(
      float dx, float dy, int width, int height, String key, Runnable action) {
    GuiButton button = theme.button(x + dx, y + dy, width, height, () -> I18n.key(key), action);
    buttons.add(button);
    return button;
  }

  protected void journalValue(
      Supplier<String> value,
      float dx,
      float dy,
      float width,
      float height,
      int size,
      Color color) {
    labels.add(
        new GuiBoxedText(
                size >= 20
                    ? FontManager.getInstance().getT4CBeaulieuFont(size, color)
                    : FontManager.getInstance().getTahomaFont(size, color, false),
                x + dx,
                y + dy,
                width,
                height,
                value,
                () -> color)
            .align(GuiBoxedText.Align.LEFT)
            .wrap()
            .shrinkToFit());
  }

  protected void renderBody(SpriteBatch batch) {}

  @Override
  public void render(SpriteBatch batch) {
    GuiDraw.drawOverlayRegionFlipped(batch, background, x, y - HEADER);
    renderBody(batch);
    for (GuiElement element : orderedElements()) element.render(batch);
  }

  @Override
  protected boolean isBackgroundHit(float sx, float sy) {
    return sx >= x && sx <= x + windowWidth && sy >= y - HEADER && sy <= y + contentHeight + 24;
  }

  @Override
  public void dispose() {
    theme.dispose();
  }
}
