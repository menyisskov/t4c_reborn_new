package com.perso.T4C.gui.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.gui.screen.ElementalStats;
import com.perso.T4C.gui.screen.Inventory;
import com.perso.T4C.gui.screen.MacrosScreen;
import com.perso.T4C.gui.screen.QuestScreen;
import com.perso.T4C.gui.screen.SpellBook;
import com.perso.T4C.gui.screen.Statistics;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.PlayerHUD;
import java.util.function.Supplier;

/** Shared journal frame and navigation for the character's personal windows. */
public abstract class CharacterPanel extends GuiScreenBase {
  protected static final int PANEL_WIDTH = 720;
  protected static final int PANEL_HEIGHT = 520;
  private int activeIndex;
  private final int tabWidth;
  protected final JournalTheme theme = new JournalTheme();

  protected CharacterPanel(Player player, PlayerHUD hud, String titleKey, String activeTab) {
    this(player, hud, titleKey, activeTab, PANEL_WIDTH, PANEL_HEIGHT);
  }

  protected CharacterPanel(
      Player player, PlayerHUD hud, String titleKey, String activeTab, int width, int height) {
    background = theme.frame(width, height);
    centerOnScreen();
    text(titleKey, 30, 21, width - 120, 26, 22, JournalTheme.GOLD);
    buttons.add(theme.button(x + width - 57, y + 20, 28, 28, () -> "X", GuiManager::close));
    String[] tabs = {
      "character", "inventory", "spells", "elements", "macros", "quests", "wiki", "adventure"
    };
    Runnable[] actions = {
      () -> GuiManager.open(new Statistics(player, hud)),
      inventoryTabAction(player, hud),
      () -> GuiManager.open(new SpellBook(player, hud)),
      () -> GuiManager.open(new ElementalStats(player, hud)),
      () -> GuiManager.open(new MacrosScreen(player, hud)),
      () -> GuiManager.open(new QuestScreen(player, hud)),
      () -> GuiManager.open(new com.perso.T4C.gui.screen.WikiScreen(player, hud)),
      () -> GuiManager.open(new com.perso.T4C.gui.screen.AdventureScreen(player, hud))
    };
    tabWidth = (width - 48) / tabs.length;
    for (int i = 0; i < tabs.length; i++) {
      String tab = tabs[i];
      GuiButton button =
          theme.button(
              x + 24 + i * tabWidth,
              y + 61,
              tabWidth - 2,
              28,
              () -> I18n.key("ui.journal.tab." + tab),
              actions[i],
              tab.equals(activeTab));
      if (tab.equals(activeTab)) {
        button.setEnabled(false);
        button.dimWhenDisabled(false);
        activeIndex = i;
      }
      buttons.add(button);
    }
  }

  static Runnable inventoryTabAction(Player player, PlayerHUD hud) {
    Runnable localInventory = () -> GuiManager.open(new Inventory(player, hud));
    Runnable configuredRoute = hud == null ? null : hud.backpackActionOr(localInventory);
    return inventoryTabAction(configuredRoute, localInventory);
  }

  static Runnable inventoryTabAction(Runnable configuredRoute, Runnable localFallback) {
    return configuredRoute == null ? localFallback : configuredRoute;
  }

  protected void renderPanelBackground(SpriteBatch batch) {
    float previous = batch.getPackedColor();
    batch.setColor(
        1, 1, 1, com.perso.T4C.config.GamePreferencesStore.get().isTransparentGui() ? .94f : 1f);
    GuiDraw.drawRegionFlipped(batch, background, x, y);
    batch.setPackedColor(previous);
    theme.rect(batch, x + 24 + activeIndex * tabWidth, y + 89, tabWidth - 2, 1, JournalTheme.GOLD);
  }

  @Override
  public void render(SpriteBatch batch) {
    renderPanelBackground(batch);
    for (GuiElement element : orderedElements()) element.render(batch);
  }

  protected void text(String key, float dx, float dy, float w, float h, int size, Color color) {
    value(() -> I18n.key(key), dx, dy, w, h, size, color);
  }

  protected void value(
      Supplier<String> value, float dx, float dy, float w, float h, int size, Color color) {
    labels.add(
        new GuiBoxedText(
                size >= 20
                    ? FontManager.getInstance().getT4CBeaulieuFont(size, color)
                    : FontManager.getInstance().getTahomaFont(size, color, false),
                x + dx,
                y + dy,
                w,
                h,
                value,
                () -> color)
            .align(GuiBoxedText.Align.LEFT)
            .shrinkToFit());
  }

  protected void well(SpriteBatch batch, float dx, float dy, float w, float h) {
    theme.well(batch, x + dx, y + dy, w, h);
  }

  @Override
  public void dispose() {
    theme.dispose();
  }
}
