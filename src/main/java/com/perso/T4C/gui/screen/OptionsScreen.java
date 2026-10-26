package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.config.GamePreferences;
import com.perso.T4C.config.GamePreferencesStore;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.core.JournalWindow;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiOptionList;
import com.perso.T4C.gui.widget.GuiSlider;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;
import java.util.*;
import java.util.function.*;

public class OptionsScreen extends JournalWindow {
  private static final Color GOLD = JournalTheme.GOLD;
  private final GamePreferences preferences = GamePreferencesStore.get();
  private final List<GuiElement> controls = new ArrayList<>();
  private final Runnable switchCharacterAction;
  private GuiOptionList graphicsList;

  public OptionsScreen() {
    this(null);
  }

  public OptionsScreen(Runnable switchCharacterAction) {
    this.switchCharacterAction = switchCharacterAction;
    initJournal(720, 460, "options.title");
    addLabels();
    addSliders();
    addOptionLists();
    addOriginalButtons();
    addSwitchCharacterButton();
    addControlsButton();
  }

  private void addLabels() {
    var header = FontManager.getInstance().getTahomaFont(14, GOLD, false);
    var text = FontManager.getInstance().getTahomaFont(12, JournalTheme.TEXT, false);
    addCentered(header, "options.audio", 26, 8, 290);
    addCentered(header, "options.video", 384, 8, 290);
    addCentered(header, "options.logs", 26, 188, 290);
    addCentered(text, "options.music", 26, 50, 115);
    addCentered(text, "options.effects", 26, 92, 115);
    addCentered(text, "options.music_source", 130, 139, 190);
    addCentered(text, "options.log_filename", 26, 342, 120);
    addCentered(text, "options.brightness", 384, 342, 120);
    journalValue(preferences::getChatLogFilename, 150, 342, 174, 20, 12, JournalTheme.TEXT);
  }

  private void addCentered(
      com.badlogic.gdx.graphics.g2d.BitmapFont font, String key, float dx, float dy, float width) {
    labels.add(
        new GuiBoxedText(
                font, x + dx, y + dy, width, 18f, () -> I18n.key(key), () -> JournalTheme.TEXT)
            .shrinkToFit());
  }

  private void addSliders() {
    var track = theme.control(140, 6, false);
    var thumb = theme.control(14, 18, true);
    controls.add(
        new GuiSlider(
                track,
                thumb,
                x + 174,
                y + 48,
                146,
                preferences::getMusicVolume,
                v -> update(p -> p.setMusicVolume((float) v)))
            .boxed(146, 24));
    controls.add(
        new GuiSlider(
                track,
                thumb,
                x + 174,
                y + 90,
                146,
                preferences::getEffectsVolume,
                v -> update(p -> p.setEffectsVolume((float) v)))
            .boxed(146, 24));
    controls.add(
        new GuiSlider(
                track,
                thumb,
                x + 528,
                y + 338,
                146,
                () -> (preferences.getBrightness() - .5f) / .75f,
                v -> update(p -> p.setBrightness(.5f + (float) v * .75f)))
            .boxed(146, 24));
  }

  private void addOptionLists() {
    var font = FontManager.getInstance().getTahomaFont(12, JournalTheme.TEXT, false);
    var selection = theme.control(294, 24, true);
    var on = theme.checkbox(true, false);
    var onHover = theme.checkbox(true, true);
    var off = theme.checkbox(false, false);
    var offHover = theme.checkbox(false, true);
    graphicsList =
        new GuiOptionList(
            x + 384f,
            y + 46f,
            294f,
            273f,
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
            x + 26f,
            y + 224f,
            294f,
            100f,
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
        entry("options.hud_values", preferences::isShowHudValues, preferences::setShowHudValues),
        entry("options.radar", preferences::isShowRadar, preferences::setShowRadar));
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
    journalButton(
        180, 410, 88, 32, "options.quit", () -> GuiManager.open(new ExitGameConfirmScreen(this)));
    journalButton(280, 410, 88, 32, "options.cancel", GuiManager::close);
    journalButton(432, 410, 104, 32, "options.low_cpu", () -> applyGraphicsPreset(false));
    journalButton(548, 410, 146, 32, "options.normal_cpu", () -> applyGraphicsPreset(true));
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
    if (switchCharacterAction != null)
      journalButton(
          26,
          410,
          142,
          32,
          "options.switch_character",
          () -> GuiManager.open(new SwitchCharacterConfirmScreen(switchCharacterAction)));
  }

  OptionsScreen reopened() {
    return new OptionsScreen(switchCharacterAction);
  }

  private void addControlsButton() {
    journalButton(26, 132, 94, 30, "options.controls", () -> GuiManager.open(new ControlsScreen()));
  }

  @Override
  protected void renderBody(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
    theme.well(batch, x + 18, y, 316, 382);
    theme.well(batch, x + 368, y, 326, 382);
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
