package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiClickZone;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
import java.util.ArrayList;
import java.util.List;

public class ElementalStats extends GuiScreenBase {
  private static final String[] ELEMENTS = {"fire", "water", "air", "earth", "light", "dark"};
  private static final float ROW_HEIGHT = 36f;
  private static final float ROW_START_Y = 60f;
  private static final float ELEMENT_X = 10f;
  private static final float POWER_X = 220f;
  private static final float RESISTANCE_X = 350f;
  private static final Color GOLD = Color.valueOf("F2B705");
  private final Player player;
  private final List<GuiClickZone> tabZones = new ArrayList<>();

  public ElementalStats(Player player) {
    this.player = player;
    try {
      background = SpriteLoader.getInstance().getRegionFromSpriteName("GUI_BackSpell");
    } catch (GameException ignored) {
      background = null;
    }
    centerOnScreen();
    addCloseButton();
    addHeader();
    addTabs();
    addRows();
  }

  private void addCloseButton() {
    addCloseButton(550f, 1f);
  }

  private void addHeader() {
    if (background == null) return;
    labels.add(GuiText.translatedHeader("ui.elemental_stats", "ELEMENTAL", x + 220f, y + 5f));
  }

  private void addTabs() {
    if (background == null || player == null) return;
    BitmapFont tabFont = FontManager.getInstance().getJetBrainsMonoFont(12, Color.WHITE);
    BitmapFont activeTabFont = FontManager.getInstance().getJetBrainsMonoFont(12, GOLD);

    labels.add(new GuiText(tabFont, x + 10f, y + 30f, () -> "[ " + I18n.key("ui.character_sheet") + " ]"));
    tabZones.add(
        new GuiClickZone(
            x + 10f, y + 20f, 130f, 16f, () -> GuiManager.open(new Statistics(player))));

    labels.add(new GuiText(activeTabFont, x + 150f, y + 30f, () -> "[ " + I18n.key("ui.elemental_stats") + " ]"));
  }

  private void addRows() {
    if (background == null || player == null) return;
    BitmapFont headerFont = FontManager.getInstance().getJetBrainsMonoFont(12, GOLD);
    BitmapFont font = FontManager.getInstance().getJetBrainsMonoFont(12, Color.WHITE);

    labels.add(new GuiText(headerFont, x + ELEMENT_X, y + ROW_START_Y, () -> I18n.key("ui.element")));
    labels.add(new GuiText(headerFont, x + POWER_X, y + ROW_START_Y, () -> I18n.key("ui.power")));
    labels.add(
        new GuiText(headerFont, x + RESISTANCE_X, y + ROW_START_Y, () -> I18n.key("ui.resistance")));

    for (int i = 0; i < ELEMENTS.length; i++) {
      String element = ELEMENTS[i];
      float ry = y + ROW_START_Y + (i + 1) * ROW_HEIGHT;
      labels.add(new GuiText(font, x + ELEMENT_X, ry, () -> I18n.key("element." + element)));
      labels.add(new GuiText(font, x + POWER_X, ry, () -> String.valueOf(player.getElementPower(element))));
      labels.add(
          new GuiText(
              font, x + RESISTANCE_X, ry, () -> String.valueOf(player.getElementResistance(element))));
    }
  }

  @Override
  public void onTouchUp(float screenX, float screenY) {
    for (GuiClickZone zone : new ArrayList<>(tabZones)) {
      if (zone.contains(screenX, screenY)) {
        zone.run();
        return;
      }
    }
    super.onTouchUp(screenX, screenY);
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    return super.onKeyDown(keycode);
  }
}
