package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;
import java.util.function.IntConsumer;

/** A keyboard-editable, bounded quantity prompt for drop and destroy actions. */
public final class ItemQuantityConfirmScreen extends GuiScreenBase {
  private final JournalTheme theme = new JournalTheme();
  private final IntConsumer confirmAction;
  private final Runnable cancelAction;
  private final int maxQuantity;
  private String input = "1";
  private boolean replaceOnType = true;

  public ItemQuantityConfirmScreen(
      String messageKey,
      String itemDisplayName,
      int maxQuantity,
      IntConsumer confirmAction,
      Runnable cancelAction) {
    this.maxQuantity = Math.max(1, maxQuantity);
    this.confirmAction = confirmAction;
    this.cancelAction = cancelAction;
    background = theme.plainFrame(420, 276);
    centerOnScreen();
    labels.add(
        new GuiBoxedText(
                FontManager.getInstance().getTahomaFont(16, JournalTheme.TEXT, false),
                x + 30,
                y + 26,
                360,
                60,
                () -> I18n.message(messageKey, itemDisplayName),
                () -> JournalTheme.TEXT)
            .wrap()
            .shrinkToFit());
    labels.add(
        new GuiBoxedText(
            FontManager.getInstance().getTahomaFont(12, JournalTheme.DIM, false),
            x + 30,
            y + 95,
            360,
            22,
            () -> I18n.message("ui.journal.quantity_available", this.maxQuantity),
            () -> JournalTheme.DIM));
    labels.add(
        new GuiBoxedText(
            FontManager.getInstance().getTahomaFont(22, JournalTheme.TEXT, true),
            x + 114,
            y + 129,
            128,
            36,
            () -> input.isEmpty() ? "_" : input,
            () -> JournalTheme.TEXT));
    buttons.add(
        theme.button(x + 64, y + 131, 36, 34, () -> "-", () -> adjust(-1)).repeatable(true));
    buttons.add(
        theme.button(x + 256, y + 131, 36, 34, () -> "+", () -> adjust(1)).repeatable(true));
    buttons.add(
        theme.button(
            x + 305,
            y + 131,
            60,
            34,
            () -> I18n.key("ui.journal.all_items"),
            () -> setQuantity(this.maxQuantity)));
    labels.add(
        new GuiBoxedText(
            FontManager.getInstance().getTahomaFont(11, JournalTheme.DIM, false),
            x + 30,
            y + 178,
            360,
            22,
            () -> I18n.key("ui.journal.quantity_hint"),
            () -> JournalTheme.DIM));
    buttons.add(
        theme.button(
            x + 66, y + 225, 132, 32, () -> I18n.key("ui.journal.confirm"), this::confirm));
    buttons.add(
        theme.button(x + 220, y + 225, 132, 32, () -> I18n.key("ui.journal.cancel"), this::cancel));
  }

  static int clampQuantity(String value, int maximum) {
    int limit = Math.max(1, maximum);
    if (value == null || value.isBlank()) return 1;
    try {
      return (int) Math.max(1, Math.min(limit, Long.parseLong(value)));
    } catch (NumberFormatException ignored) {
      return limit;
    }
  }

  private void setQuantity(int value) {
    input = String.valueOf(Math.max(1, Math.min(maxQuantity, value)));
    replaceOnType = true;
  }

  private void adjust(int delta) {
    long next = (long) clampQuantity(input, maxQuantity) + delta;
    setQuantity((int) Math.max(1, Math.min(maxQuantity, next)));
  }

  private void confirm() {
    if (confirmAction != null) confirmAction.accept(clampQuantity(input, maxQuantity));
  }

  private void cancel() {
    if (cancelAction != null) cancelAction.run();
  }

  @Override
  public void render(SpriteBatch batch) {
    GuiDraw.drawRegionFlipped(batch, background, x, y);
    theme.accentWell(batch, x + 111, y + 126, 134, 42);
    for (var element : orderedElements()) element.render(batch);
  }

  @Override
  public void onTouchDown(float sx, float sy) {
    if (sx >= x + 111 && sx <= x + 245 && sy >= y + 126 && sy <= y + 168) replaceOnType = true;
    super.onTouchDown(sx, sy);
  }

  @Override
  public boolean capturesKeyboard() {
    return true;
  }

  @Override
  public boolean onKeyDown(int keycode) {
    switch (keycode) {
      case Input.Keys.ESCAPE -> cancel();
      case Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> confirm();
      case Input.Keys.UP -> adjust(1);
      case Input.Keys.DOWN -> adjust(-1);
      case Input.Keys.A -> replaceOnType = true;
      case Input.Keys.BACKSPACE, Input.Keys.FORWARD_DEL -> {
        input = replaceOnType || input.isEmpty() ? "" : input.substring(0, input.length() - 1);
        replaceOnType = false;
      }
      default -> {}
    }
    return true;
  }

  @Override
  public boolean onKeyTyped(char character) {
    if (character >= '0' && character <= '9') {
      String next = (replaceOnType ? "" : input) + character;
      input = String.valueOf(clampQuantity(next, maxQuantity));
      replaceOnType = false;
    }
    return true;
  }

  @Override
  public void dispose() {
    theme.dispose();
  }
}
