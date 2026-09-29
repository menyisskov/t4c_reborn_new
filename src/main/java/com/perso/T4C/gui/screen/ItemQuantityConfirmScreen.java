package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;
import java.util.function.IntConsumer;

/**
 * Confirms a drop/junk action, with a quantity stepper when the player owns more than one of the
 * item so they aren't forced to repeat the action one at a time.
 */
public final class ItemQuantityConfirmScreen extends GuiScreenBase {
  private final IntConsumer confirmAction;
  private final Runnable cancelAction;
  private final int maxQuantity;
  private int quantity = 1;

  public ItemQuantityConfirmScreen(
      String messageKey,
      String itemDisplayName,
      int maxQuantity,
      IntConsumer confirmAction,
      Runnable cancelAction) {
    this.maxQuantity = Math.max(1, maxQuantity);
    this.confirmAction = confirmAction;
    this.cancelAction = cancelAction;
    background = GuiSprites.load("GUI_PopupBack");
    centerOnScreen();
    if (background == null) return;
    var gold = Color.valueOf("DF9D00");
    var textFont = FontManager.getInstance().getJetBrainsMonoFont(12, gold);
    labels.add(
        new GuiBoxedText(
                textFont,
                x + 22f,
                y + 23f,
                196f,
                55f,
                () -> I18n.message(messageKey, itemDisplayName),
                () -> gold)
            .wrap()
            .shrinkToFit());
    if (this.maxQuantity > 1) addQuantityStepper(gold);
    var normal = GuiSprites.load("GUI_ButtonUp");
    var hover = GuiSprites.load("GUI_ButtonHUp");
    var pressed = GuiSprites.load("GUI_ButtonDown");
    if (normal == null || hover == null || pressed == null) return;
    var buttonFont = FontManager.getInstance().getJetBrainsMonoFont(11, Color.BLACK);
    buttons.add(
        new GuiButton(normal, hover, pressed, x + 52f, y + 94f, this::confirm)
            .setSize(60f, 32f)
            .withLabel(buttonFont, () -> I18n.key("character.yes")));
    buttons.add(
        new GuiButton(normal, hover, pressed, x + 128f, y + 94f, this::cancel)
            .setSize(60f, 32f)
            .withLabel(buttonFont, () -> I18n.key("character.no")));
  }

  private void addQuantityStepper(Color gold) {
    var normal = GuiSprites.load("GUI_ButtonUp");
    var hover = GuiSprites.load("GUI_ButtonHUp");
    var pressed = GuiSprites.load("GUI_ButtonDown");
    if (normal == null || hover == null || pressed == null) return;
    var stepFont = FontManager.getInstance().getJetBrainsMonoFont(12, Color.BLACK);
    buttons.add(
        new GuiButton(normal, hover, pressed, x + 84f, y + 132f, () -> adjustQuantity(-1))
            .setSize(20f, 20f)
            .repeatable(true)
            .withLabel(stepFont, () -> "-"));
    buttons.add(
        new GuiButton(normal, hover, pressed, x + 136f, y + 132f, () -> adjustQuantity(1))
            .setSize(20f, 20f)
            .repeatable(true)
            .withLabel(stepFont, () -> "+"));
    var qtyFont = FontManager.getInstance().getJetBrainsMonoFont(13, gold);
    labels.add(
        new GuiBoxedText(
                qtyFont,
                x + 108f,
                y + 132f,
                24f,
                20f,
                () -> "x" + quantity,
                () -> gold)
            .shrinkToFit());
  }

  private void adjustQuantity(int delta) {
    quantity = Math.max(1, Math.min(maxQuantity, quantity + delta));
  }

  private void confirm() {
    if (confirmAction != null) confirmAction.accept(quantity);
  }

  private void cancel() {
    if (cancelAction != null) cancelAction.run();
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) {
      cancel();
      return true;
    }
    return false;
  }
}
