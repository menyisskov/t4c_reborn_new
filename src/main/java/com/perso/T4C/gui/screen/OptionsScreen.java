package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.config.GamePreferences;
import com.perso.T4C.config.GamePreferencesStore;
import com.perso.T4C.gui.core.*;
import com.perso.T4C.gui.widget.*;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;
import java.util.*;
import java.util.function.*;

/** Faithful local implementation of the original 1.68 {@code OptionsUI}. */
public class OptionsScreen extends GuiScreenBase {
    private static final Color GOLD = Color.valueOf("DF9D00");
    private final GamePreferences preferences = GamePreferencesStore.get();
    private final List<GuiElement> controls = new ArrayList<>();
    private final Runnable switchCharacterAction;
    private GuiOptionList graphicsList;

    public OptionsScreen() { this(null); }

    public OptionsScreen(Runnable switchCharacterAction) {
        this.switchCharacterAction = switchCharacterAction;
        background = GuiSprites.load("GUI_BackOption");
        centerOnScreen();
        if (background == null) return;
        // OptionsUI.cpp reserves the original 150-pixel bottom interface strip.
        y = Math.max(0f, (com.badlogic.gdx.Gdx.graphics.getHeight() - 150f
                - background.getRegionHeight()) / 2f);
        addCloseButton(484f, 0f);
        addLabels();
        addSliders();
        addOptionLists();
        addOriginalButtons();
        addSwitchCharacterButton();
    }

    private void addLabels() {
        var header = FontManager.getInstance().getT4CBeaulieuFont(17, GOLD);
        var text = FontManager.getInstance().getT4CBeaulieuFont(15, GOLD);
        addCentered(header, "options.title", 206f, 0f, 101f);
        addCentered(text, "options.audio", 83f, 39f, 99f);
        addCentered(text, "options.video", 295f, 39f, 116f);
        addCentered(text, "options.logs", 50f, 165f, 116f);
        addCentered(text, "options.music", 22f, 65f, 74f);
        addCentered(text, "options.effects", 22f, 96f, 74f);
        addCentered(text, "options.music_source", 104f, 130f, 122f);
        addCentered(text, "options.log_filename", 21f, 270f, 116f);
        addCentered(text, "options.brightness", 266f, 267f, 98f);
        labels.add(new GuiBoxedText(text, x + 144f, y + 270f, 100f, 18f,
                preferences::getChatLogFilename, () -> Color.LIGHT_GRAY).shrinkToFit());
    }

    private void addCentered(com.badlogic.gdx.graphics.g2d.BitmapFont font, String key,
                             float dx, float dy, float width) {
        labels.add(new GuiBoxedText(font, x + dx, y + dy, width, 18f,
                () -> I18n.key(key), () -> GOLD).shrinkToFit());
    }

    private void addSliders() {
        var track = GuiSprites.load("OptionSlider");
        var thumb = GuiSprites.load("GeneralSlider");
        controls.add(new GuiSlider(track, thumb, x + 127f, y + 65f, 84f,
                preferences::getMusicVolume, v -> update(p -> p.setMusicVolume((float) v))).boxed(84f, 20f));
        controls.add(new GuiSlider(track, thumb, x + 127f, y + 96f, 84f,
                preferences::getEffectsVolume, v -> update(p -> p.setEffectsVolume((float) v))).boxed(84f, 20f));
        controls.add(new GuiSlider(track, thumb, x + 395f, y + 267f, 62f,
                () -> (preferences.getBrightness() - .5f) / .75f,
                v -> update(p -> p.setBrightness(.5f + (float) v * .75f))).boxed(62f, 20f));
    }

    private void addOptionLists() {
        var font = FontManager.getInstance().getT4CBeaulieuFont(14, Color.LIGHT_GRAY);
        var selection = GuiSprites.load("GUI_OptionGraphSelect");
        var on = GuiSprites.load("CheckONLightUP");
        var onHover = GuiSprites.load("CheckONLightHUP");
        var off = GuiSprites.load("CheckOFFLightUP");
        var offHover = GuiSprites.load("CheckOFFLightHUP");
        graphicsList = new GuiOptionList(x + 267f, y + 62f, 227f, 183f, font,
                selection, on, onHover, off, offHover, graphicsEntries());
        controls.add(graphicsList);
        controls.add(new GuiOptionList(x + 22f, y + 188f, 189f, 78f, font,
                selection, on, onHover, off, offHover, List.of(
                entry("options.log_npc", preferences::isLogNpcMessages, preferences::setLogNpcMessages),
                entry("options.log_players", preferences::isLogPlayerMessages, preferences::setLogPlayerMessages),
                entry("options.log_enabled", preferences::isChatLogging, preferences::setChatLogging))));
    }

    private List<GuiOptionList.Entry> graphicsEntries() {
        return List.of(
                entry("options.light_graphics", preferences::isLightGraphics, preferences::setLightGraphics),
                entry("options.high_quality_effects", preferences::isHighQualityEffects, preferences::setHighQualityEffects),
                entry("options.animated_water", preferences::isAnimatedWater, preferences::setAnimatedWater),
                entry("options.animated_decor_lights", preferences::isAnimatedDecorLights, preferences::setAnimatedDecorLights),
                entry("options.weather_effects", preferences::isWeatherEffects, preferences::setWeatherEffects),
                entry("options.dithering", preferences::isDithering, preferences::setDithering),
                entry("options.transparent_gui", preferences::isTransparentGui, preferences::setTransparentGui),
                entry("options.seraph_animation", preferences::isSeraphAnimation, preferences::setSeraphAnimation),
                entry("options.status_effects", preferences::isStatusEffects, preferences::setStatusEffects),
                entry("options.xp_bar_text", preferences::isXpBarText, preferences::setXpBarText),
                entry("options.display_gold", preferences::isDisplayGold, preferences::setDisplayGold),
                entry("options.fullscreen_macros", preferences::isFullscreenMacros, preferences::setFullscreenMacros),
                entry("options.lock_target", preferences::isLockTarget, preferences::setLockTarget),
                liveEntry("options.high_quality_font", preferences::isHighQualityFont, preferences::setHighQualityFont,
                        value -> FontManager.getInstance().applyHighQuality(value)),
                entry("options.zoom_enabled", preferences::isZoomEnabled, preferences::setZoomEnabled),
                entry("options.item_details", preferences::isItemDetails, preferences::setItemDetails),
                entry("options.new_health_bar", preferences::isNewHealthBar, preferences::setNewHealthBar),
                entry("options.old_status_bar", preferences::isOldStatusBar, preferences::setOldStatusBar),
                entry("options.new_shadows", preferences::isNewShadows, preferences::setNewShadows),
                displayEntry("options.fullscreen", preferences::isFullscreen, preferences::setFullscreen),
                displayEntry("options.vsync", preferences::isVSync, preferences::setVSync),
                entry("options.hud_values", preferences::isShowHudValues, preferences::setShowHudValues));
    }

    private GuiOptionList.Entry displayEntry(String key, BooleanSupplier getter, Consumer<Boolean> setter) {
        return new GuiOptionList.Entry(() -> I18n.key(key), getter, value -> {
            setter.accept(value); GamePreferencesStore.save(); GamePreferencesStore.applyDisplaySettings();
        });
    }

    private GuiOptionList.Entry entry(String key, BooleanSupplier getter, Consumer<Boolean> setter) {
        return new GuiOptionList.Entry(() -> I18n.key(key), getter, value -> {
            setter.accept(value); GamePreferencesStore.save();
        });
    }

    private GuiOptionList.Entry liveEntry(String key, BooleanSupplier getter, Consumer<Boolean> setter,
                                          Consumer<Boolean> apply) {
        return new GuiOptionList.Entry(() -> I18n.key(key), getter, value -> {
            setter.accept(value); apply.accept(value); GamePreferencesStore.save();
        });
    }

    private void addOriginalButtons() {
        addButton(177f, "options.quit", () -> GuiManager.open(new ExitGameConfirmScreen(this)));
        addButton(253f, "options.cancel", GuiManager::close);
        addButton(361f, "options.low_cpu", () -> applyGraphicsPreset(false));
        addButton(437f, "options.normal_cpu", () -> applyGraphicsPreset(true));
    }

    private void addButton(float dx, String key, Runnable action) {
        var normal = GuiSprites.load("GUI_ButtonUp");
        var hover = GuiSprites.load("GUI_ButtonHUp");
        var pressed = GuiSprites.load("GUI_ButtonDown");
        if (normal == null || hover == null || pressed == null) return;
        var font = FontManager.getInstance().getT4CBeaulieuFont(17, Color.BLACK);
        buttons.add(new GuiButton(normal, hover, pressed, x + dx, y + 300f, action)
                .setSize(60f, 32f).withLabel(font, () -> I18n.key(key)));
    }

    private void applyGraphicsPreset(boolean normal) {
        preferences.setLightGraphics(!normal); preferences.setHighQualityEffects(normal);
        preferences.setAnimatedWater(normal); preferences.setAnimatedDecorLights(normal);
        preferences.setWeatherEffects(normal); preferences.setDithering(true);
        preferences.setTransparentGui(normal); preferences.setSeraphAnimation(normal);
        preferences.setStatusEffects(true); preferences.setXpBarText(normal);
        preferences.setDisplayGold(true);
        preferences.setFullscreenMacros(normal); preferences.setZoomEnabled(normal);
        preferences.setItemDetails(normal); preferences.setNewHealthBar(normal);
        preferences.setOldStatusBar(!normal); preferences.setNewShadows(false);
        preferences.setLockTarget(false); preferences.setHighQualityFont(false);
        GamePreferencesStore.save();
        FontManager.getInstance().applyHighQuality(preferences.isHighQualityFont());
    }

    private void addSwitchCharacterButton() {
        if (switchCharacterAction == null) return;
        var normal = GuiSprites.load("PS_BtnN");
        var hover = GuiSprites.load("PS_BtnH");
        if (normal == null || hover == null) return;
        var font = FontManager.getInstance().getT4CBeaulieuFont(17, Color.BLACK);
        buttons.add(new GuiButton(normal, hover, hover, x + 198f, y + 350f,
                () -> GuiManager.open(new SwitchCharacterConfirmScreen(switchCharacterAction)))
                .withLabel(font, () -> I18n.key("options.switch_character")));
    }

    private void update(Consumer<GamePreferences> change) {
        change.accept(preferences); GamePreferencesStore.save(); SoundManager.applyVolumes();
    }

    @Override protected List<GuiElement> extraElements() { return controls; }
    @Override public void onScroll(float amountY, float screenX, float screenY) {
        if (graphicsList != null && graphicsList.contains(screenX, screenY)) graphicsList.scroll(amountY);
    }
    @Override public boolean onKeyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) { GuiManager.close(); return true; }
        return false;
    }
}
