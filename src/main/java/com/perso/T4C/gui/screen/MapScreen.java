package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiWorldMap;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;

import java.util.List;

/** Full game map displaying the complete current level. */
public final class MapScreen extends GuiScreenBase {
    private static final Color GOLD = Color.valueOf("F2B705");
    private final GuiWorldMap worldMap;

    public MapScreen() {
        background = GuiSprites.load("GUI_BackMap");
        centerOnScreen();

        worldMap = new GuiWorldMap(x + 20f, y + 40f, 534f, 252f);

        BitmapFont titleFont = FontManager.getInstance().getHaettenschweilerFont(17, GOLD);
        labels.add(new GuiBoxedText(titleFont, x + 238f, y + 2f, 100f, 19f,
                () -> I18n.key("ui.world_map"), () -> GOLD).shrinkToFit());

        addCloseButton(548f, 1f);
    }

    @Override
    protected List<GuiElement> extraElements() {
        return List.of(worldMap);
    }

    @Override
    public boolean onKeyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            GuiManager.close();
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        worldMap.dispose();
    }
}
