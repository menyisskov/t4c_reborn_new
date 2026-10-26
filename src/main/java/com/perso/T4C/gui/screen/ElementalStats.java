package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiClickZone;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.CharacterPanel;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.ui.PlayerHUD;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
import java.util.ArrayList;
import java.util.List;

public class ElementalStats extends CharacterPanel {
  private static final String[] ELEMENTS = {"fire", "water", "air", "earth", "light", "dark"};
  private static final float ROW_HEIGHT = 48f;
  private static final float ROW_START_Y = 123f;
  private static final float ELEMENT_X = 40f;
  private static final float POWER_X = 320f;
  private static final float RESISTANCE_X = 520f;
  private static final Color GOLD = JournalTheme.GOLD;
  private final Player player;
  private final List<GuiClickZone> tabZones = new ArrayList<>();

  public ElementalStats(Player player) {
    this(player, null);
  }

  public ElementalStats(Player player, PlayerHUD hud) {
    super(player, hud, "ui.journal.elements", "elements");
    this.player = player;
    addRows();
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
  public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
    renderPanelBackground(batch);
    for (int i = 0; i < ELEMENTS.length; i++) well(batch, 28, 158 + i * ROW_HEIGHT, 664, 42);
    for (GuiElement element : orderedElements()) element.render(batch);
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
