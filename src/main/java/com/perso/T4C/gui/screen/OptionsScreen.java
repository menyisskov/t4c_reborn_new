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
import com.perso.T4C.gui.widget.GuiOptionList;
import com.perso.T4C.gui.widget.GuiSlider;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;
import java.util.*;
import java.util.function.*;

public class OptionsScreen extends GuiScreenBase {
  private static final Color GOLD = Color.valueOf("DF9D00");
  private final GamePreferences preferences = GamePreferencesStore.get();
  private final List<GuiElement> controls = new ArrayList<>();
  private final Runnable switchCharacterAction;
  private GuiOptionList graphicsList;

  public OptionsScreen() {
    this(null);
  }

  public OptionsScreen(Runnable switchCharacterAction) {
    this.switchCharacterAction = switchCharacterAction;
    background = GuiSprites.load("GUI_BackOption");
    centerOnScreen();
    if (background == null) return;
    y =
        Math.max(
            0f,
            (com.badlogic.gdx.Gdx.graphics.getHeight() - 150f - background.getRegionHeight()) / 2f);
    addCloseButton(484f, 0f);
    addLabels();
    addSliders();
    addOptionLists();
    addOriginalButtons();
    addSwitchCharacterButton();
  }

  private void addLabels() {
    var header = FontManager.getInstance().getHaettenschweilerFont(17, GOLD);
    var text = FontManager.getInstance().getT4CBeaulieuFont(15, GOLD);
    addCentered(header, "options.title", 207f, 2f, 101f);
    addCentered(header, "options.audio", 83f, 39f, 99f);
    addCentered(header, "options.video", 295f, 39f, 116f);
    addCentered(header, "options.logs", 50f, 165f, 116f);
    addCentered(text, "options.music", 22f, 65f, 74f);
    addCentered(text, "options.effects", 22f, 96f, 74f);
    addCentered(text, "options.music_source", 104f, 130f, 122f);
    addCentered(text, "options.log_filename", 21f, 270f, 116f);
    addCentered(text, "options.brightness", 266f, 267f, 98f);
    labels.add(
        new GuiBoxedText(
                text,
                x + 144f,
                y + 270f,
                100f,
                18f,
                preferences::getChatLogFilename,
                () -> Color.LIGHT_GRAY)
            .shrinkToFit());
  }

  private void addCentered(
      com.badlogic.gdx.graphics.g2d.BitmapFont font, String key, float dx, float dy, float width) {
    labels.add(
        new GuiBoxedText(font, x + dx, y + dy, width, 18f, () -> I18n.key(key), () -> GOLD)
            .shrinkToFit());
  }

  private void addSliders() {
    var track = GuiSprites.load("OptionSlider");
    var thumb = GuiSprites.load("GeneralSlider");
    controls.add(
        new GuiSlider(
                track,
                thumb,
                x + 127f,
                y + 65f,
                84f,
                preferences::getMusicVolume,
                v -> update(p -> p.setMusicVolume((float) v)))
            .boxed(84f, 20f));
    controls.add(
        new GuiSlider(
                track,
                thumb,
                x + 127f,
                y + 96f,
                84f,
                preferences::getEffectsVolume,
                v -> update(p -> p.setEffectsVolume((float) v)))
            .boxed(84f, 20f));
    controls.add(
        new GuiSlider(
                track,
                thumb,
                x + 395f,
                y + 267f,
                62f,
                () -> (preferences.getBrightness() - .5f) / .75f,
                v -> update(p -> p.setBrightness(.5f + (float) v * .75f)))
            .boxed(62f, 20f));
  }

  private void addOptionLists() {
    var font = FontManager.getInstance().getT4CBeaulieuFont(14, Color.LIGHT_GRAY);
    var selection = GuiSprites.load("GUI_OptionGraphSelect");
    var on = GuiSprites.load("CheckONLightUP");
    var onHover = GuiSprites.load("CheckONLightHUP");
    var off = GuiSprites.load("CheckOFFLightUP");
    var offHover = GuiSprites.load("CheckOFFLightHUP");
    graphicsList =
        new GuiOptionList(
            x + 267f,
            y + 62f,
            227f,
            183f,
            font,
            selection,
            on,
            onHover,
            off,
            offHover,
            graphicsEntries());
    controls.add(graphicsList);
    controls.add(
        new GuiOptionList(
            x + 22f,
            y + 188f,
            189f,
            78f,
            font,
            selection,
            on,
            onHover,
            off,
            offHover,
            List.of(
                entry(
                    "options.log_npc",
                    preferences::isLogNpcMessages,
                    preferences::setLogNpcMessages),
                entry(
                    "options.log_players",
                    preferences::isLogPlayerMessages,
                    preferences::setLogPlayerMessages),
                entry(
                    "options.log_enabled",
                    preferences::isChatLogging,
                    preferences::setChatLogging))));
  }

  private List<GuiOptionList.Entry> graphicsEntries() {
    return List.of(
        entry(
            "options.transparent_gui",
            preferences::isTransparentGui,
            preferences::setTransparentGui),
        entry(
            "options.seraph_animation",
            preferences::isSeraphAnimation,
            preferences::setSeraphAnimation),
        entry("options.xp_bar_text", preferences::isXpBarText, preferences::setXpBarText),
        liveEntry(
            "options.high_quality_font",
            preferences::isHighQualityFont,
            preferences::setHighQualityFont,
            value -> FontManager.getInstance().applyHighQuality(value)),
        displayEntry("options.fullscreen", preferences::isFullscreen, preferences::setFullscreen),
        displayEntry("options.vsync", preferences::isVSync, preferences::setVSync),
        entry("options.enable_32fps", preferences::isEnable32FPS, preferences::setEnable32FPS),
        entry("options.hud_values", preferences::isShowHudValues, preferences::setShowHudValues));
  }

  private GuiOptionList.Entry displayEntry(
      String key, BooleanSupplier getter, Consumer<Boolean> setter) {
    return new GuiOptionList.Entry(
        () -> I18n.key(key),
        getter,
        value -> {
          setter.accept(value);
          GamePreferencesStore.save();
          GamePreferencesStore.applyDisplaySettings();
        });
  }

  private GuiOptionList.Entry entry(String key, BooleanSupplier getter, Consumer<Boolean> setter) {
    return new GuiOptionList.Entry(
        () -> I18n.key(key),
        getter,
        value -> {
          setter.accept(value);
          GamePreferencesStore.save();
        });
  }

  private GuiOptionList.Entry liveEntry(
      String key, BooleanSupplier getter, Consumer<Boolean> setter, Consumer<Boolean> apply) {
    return new GuiOptionList.Entry(
        () -> I18n.key(key),
        getter,
        value -> {
          setter.accept(value);
          apply.accept(value);
          GamePreferencesStore.save();
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
    buttons.add(
        new GuiButton(normal, hover, pressed, x + dx, y + 300f, action)
            .setSize(60f, 32f)
            .withLabel(font, () -> I18n.key(key)));
  }

  private void applyGraphicsPreset(boolean normal) {
    preferences.setTransparentGui(normal);
    preferences.setSeraphAnimation(normal);
    preferences.setXpBarText(normal);
    preferences.setHighQualityFont(false);
    GamePreferencesStore.save();
    FontManager.getInstance().applyHighQuality(preferences.isHighQualityFont());
  }

  private void addSwitchCharacterButton() {
    if (switchCharacterAction == null) return;
    var normal = GuiSprites.load("GUI_ButtonUp");
    var hover = GuiSprites.load("GUI_ButtonHUp");
    var pressed = GuiSprites.load("GUI_ButtonDown");
    if (normal == null || hover == null || pressed == null) return;
    var font = FontManager.getInstance().getT4CBeaulieuFont(17, Color.BLACK);
    buttons.add(
        new GuiButton(
                normal,
                hover,
                pressed,
                x + 22f,
                y + 300f,
                () -> GuiManager.open(new SwitchCharacterConfirmScreen(switchCharacterAction)))
            .setSize(130f, 32f)
            .withLabelVerticalOffset(-1f)
            .withLabel(font, () -> I18n.key("options.switch_character")));
  }

  private void update(Consumer<GamePreferences> change) {
    change.accept(preferences);
    GamePreferencesStore.save();
    SoundManager.applyVolumes();
  }

  @Override
  protected List<GuiElement> extraElements() {
    return controls;
  }

  @Override
  public void onScroll(float amountY, float screenX, float screenY) {
    if (graphicsList != null && graphicsList.contains(screenX, screenY))
      graphicsList.scroll(amountY);
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
