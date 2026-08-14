package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;

/** Confirmation shown before leaving the active local character session. */
public final class SwitchCharacterConfirmScreen extends GuiScreenBase {
    private final Runnable switchCharacterAction;

    public SwitchCharacterConfirmScreen(Runnable switchCharacterAction) {
        this.switchCharacterAction = switchCharacterAction;
        background = GuiSprites.load("GUI_PopupBack");
        centerOnScreen();
        if (background == null) return;

        var gold = Color.valueOf("DF9D00");
        var textFont = FontManager.getInstance().getJetBrainsMonoFont(12, gold);
        labels.add(new GuiBoxedText(textFont, x + 22f, y + 23f, 196f, 55f,
                () -> I18n.key("options.switch_character.confirm"), () -> gold));

        var normal = GuiSprites.load("GUI_ButtonUp");
        var hover = GuiSprites.load("GUI_ButtonHUp");
        var pressed = GuiSprites.load("GUI_ButtonDown");
        if (normal == null || hover == null || pressed == null) return;
        var buttonFont = FontManager.getInstance().getJetBrainsMonoFont(11, Color.BLACK);
        buttons.add(new GuiButton(normal, hover, pressed, x + 52f, y + 94f,
                this::confirm).setSize(60f, 32f)
                .withLabel(buttonFont, () -> I18n.key("character.yes")));
        buttons.add(new GuiButton(normal, hover, pressed, x + 128f, y + 94f,
                this::cancel).setSize(60f, 32f)
                .withLabel(buttonFont, () -> I18n.key("character.no")));
    }

    private void confirm() {
        if (switchCharacterAction != null) switchCharacterAction.run();
    }

    private void cancel() {
        GuiManager.open(new OptionsScreen(switchCharacterAction));
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
