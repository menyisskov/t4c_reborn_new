package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.perso.T4C.gui.core.*;
import com.perso.T4C.gui.widget.*;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;

/** Original OptionsUI quit confirmation popup. */
public final class ExitGameConfirmScreen extends GuiScreenBase {
    private final Runnable cancelAction;
    public ExitGameConfirmScreen(OptionsScreen options) {
        cancelAction = () -> GuiManager.open(options);
        background = GuiSprites.load("GUI_PopupBack"); centerOnScreen();
        if (background == null) return;
        var gold = Color.valueOf("DF9D00");
        var text = FontManager.getInstance().getT4CBeaulieuFont(16, gold);
        labels.add(new GuiBoxedText(text, x + 22f, y + 23f, 195f, 55f,
                () -> I18n.key("options.quit.confirm"), () -> gold));
        addButton(52f, "character.yes", () -> Gdx.app.exit());
        addButton(128f, "character.no", cancelAction);
    }
    private void addButton(float dx, String key, Runnable action) {
        var normal = GuiSprites.load("GUI_ButtonUp"); var hover = GuiSprites.load("GUI_ButtonHUp");
        var pressed = GuiSprites.load("GUI_ButtonDown");
        if (normal == null || hover == null || pressed == null) return;
        var font = FontManager.getInstance().getT4CBeaulieuFont(17, Color.BLACK);
        buttons.add(new GuiButton(normal, hover, pressed, x + dx, y + 94f, action)
                .setSize(60f, 32f).withLabel(font, () -> I18n.key(key)));
    }
    @Override public boolean onKeyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) { cancelAction.run(); return true; }
        return false;
    }
}
