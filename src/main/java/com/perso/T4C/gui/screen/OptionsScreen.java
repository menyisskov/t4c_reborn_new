package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.config.GamePreferences;
import com.perso.T4C.config.GamePreferencesStore;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiCheckbox;
import com.perso.T4C.gui.widget.GuiSlider;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** In-game audio, display and HUD preferences. */
public class OptionsScreen extends GuiScreenBase {
    private final GamePreferences preferences = GamePreferencesStore.get();
    private final List<GuiElement> controls = new ArrayList<>();

    public OptionsScreen() {
        background = GuiSprites.load("GUI_BackOption");
        centerOnScreen();
        if (background == null) return;

        addCloseButton(488f, 0f);
        var selectedRow = GuiSprites.load("GUI_OptionGraphSelect");
        if (selectedRow != null) {
            animatedSprites.add(new GuiAnimatedSprite(List.of(selectedRow), x + 263f, y + 62f, 1f));
        }
        addLabels();
        addSliders();
        addCheckboxes();
    }

    private void addLabels() {
        var gold = Color.valueOf("F2B705");
        var header = FontManager.getInstance().getHaettenschweilerFont(16, gold);
        var text = FontManager.getInstance().getJetBrainsMonoFont(11, gold);
        labels.add(new GuiBoxedText(header, x + 206f, y + 2f, 100f, 19f,
                () -> I18n.key("options.title"), () -> gold).shrinkToFit());
        labels.add(new GuiBoxedText(header, x + 82f, y + 39f, 100f, 18f,
                () -> I18n.key("options.audio"), () -> gold));
        labels.add(new GuiBoxedText(header, x + 294f, y + 39f, 118f, 18f,
                () -> I18n.key("options.video"), () -> gold));

        addLabel(text, "options.music", 24f, 66f, 71f);
        addLabel(text, "options.effects", 24f, 97f, 71f);

        addLabel(text, "options.fullscreen", 266f, 62f, 169f);
        addLabel(text, "options.vsync", 265f, 82f, 170f);
        addLabel(text, "options.hud_values", 266f, 104f, 169f);
        labels.add(new GuiBoxedText(text, x + 267f, y + 265f, 96f, 23f,
                () -> I18n.key("options.brightness"), () -> gold)
                .align(GuiBoxedText.Align.LEFT).shrinkToFit());
        labels.add(new GuiBoxedText(text, x + 267f, y + 235f, 116f, 23f,
                () -> I18n.key("options.interface_opacity"), () -> gold)
                .align(GuiBoxedText.Align.LEFT).shrinkToFit());
    }

    private void addLabel(com.badlogic.gdx.graphics.g2d.BitmapFont font, String key,
                          float dx, float dy, float width) {
        var gold = Color.valueOf("F2B705");
        labels.add(new GuiBoxedText(font, x + dx, y + dy, width, 16f,
                () -> I18n.key(key), () -> gold).align(GuiBoxedText.Align.LEFT).shrinkToFit());
    }

    private void addSliders() {
        var track = GuiSprites.load("OptionSlider");
        var thumb = GuiSprites.load("GeneralSlider");
        controls.add(new GuiSlider(track, thumb, x + 126f, y + 64f, 97f,
                preferences::getMusicVolume, value -> update(p -> p.setMusicVolume((float) value))));
        controls.add(new GuiSlider(track, thumb, x + 126f, y + 94f, 97f,
                preferences::getEffectsVolume, value -> update(p -> p.setEffectsVolume((float) value))));
        controls.add(new GuiSlider(track, thumb, x + 389f, y + 266f, 82f,
                () -> (preferences.getBrightness() - 0.5f) / 0.75f,
                value -> update(p -> p.setBrightness(0.5f + (float) value * 0.75f))));
        controls.add(new GuiSlider(track, thumb, x + 389f, y + 236f, 82f,
                () -> preferences.getOverlayPercentage() / 100f,
                value -> update(p -> p.setOverlayPercentage((float) value * 100f))));
    }

    private void addCheckboxes() {
        var on = GuiSprites.load("CheckONLightUP");
        var onHover = GuiSprites.load("CheckONLightHUP");
        var off = GuiSprites.load("CheckOFFLightUP");
        var offHover = GuiSprites.load("CheckOFFLightHUP");
        controls.add(new GuiCheckbox(on, onHover, off, offHover, x + 441f, y + 62f,
                preferences::isFullscreen, value -> updateDisplay(p -> p.setFullscreen(value))));
        controls.add(new GuiCheckbox(on, onHover, off, offHover, x + 441f, y + 83f,
                preferences::isVSync, value -> updateDisplay(p -> p.setVSync(value))));
        controls.add(new GuiCheckbox(on, onHover, off, offHover, x + 441f, y + 104f,
                preferences::isShowHudValues, value -> update(p -> p.setShowHudValues(value))));
    }

    private void update(Consumer<GamePreferences> change) {
        change.accept(preferences);
        GamePreferencesStore.save();
        SoundManager.applyVolumes();
    }

    private void updateDisplay(Consumer<GamePreferences> change) {
        update(change);
        GamePreferencesStore.applyDisplaySettings();
    }

    @Override
    protected List<GuiElement> extraElements() {
        return controls;
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
