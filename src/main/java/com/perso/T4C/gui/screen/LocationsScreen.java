package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.player.Player;
import com.perso.T4C.teleport.NamedLocation;
import com.perso.T4C.teleport.NamedLocations;
import com.perso.T4C.ui.FontManager;
import java.util.ArrayList;
import java.util.List;

/** Scrollable panel of named fast-travel locations; clicking one teleports the player there. */
public final class LocationsScreen extends GuiScreenBase {
  private static final int VISIBLE_ROWS = 6;
  private static final float LIST_X = 29f;
  private static final float LIST_Y = 55f;
  private static final float LIST_W = 205f;
  private static final float ROW_H = 32f;
  private static final Color GOLD = Color.valueOf("F2B705");
  private static final Color WHITE = Color.valueOf("E6D8BC");

  private final Player player;
  private final List<NamedLocation> locations;
  private final List<GuiBoxedText> rows = new ArrayList<>();
  private final List<float[]> rowBounds = new ArrayList<>();
  private int firstVisible;

  public LocationsScreen(Player player) {
    this.player = player;
    this.locations = NamedLocations.forPlayer(player);
    background = GuiSprites.load("GUI_BackQuest");
    centerOnScreen();
    addCloseButton(548f, 1f);
    addTitle();
    addRows();
    addDetailPanel();
  }

  private void addTitle() {
    if (background == null) {
      return;
    }
    BitmapFont titleFont = FontManager.getInstance().getHaettenschweilerFont(17, GOLD);
    labels.add(
        new GuiBoxedText(titleFont, x + 238f, y + 2f, 100f, 19f, () -> "Locations", () -> GOLD)
            .shrinkToFit());
  }

  private void addRows() {
    BitmapFont listFont = FontManager.getInstance().getTahomaFont(12, GOLD, false);
    for (int row = 0; row < VISIBLE_ROWS; row++) {
      final int rowIndex = row;
      GuiBoxedText box =
          new GuiBoxedText(
                  listFont,
                  x + LIST_X,
                  y + LIST_Y + row * ROW_H,
                  LIST_W,
                  ROW_H - 4f,
                  () -> rowName(rowIndex),
                  () -> GOLD)
              .align(GuiBoxedText.Align.LEFT)
              .shrinkToFit();
      rows.add(box);
      labels.add(box);
      rowBounds.add(new float[] {x + LIST_X, y + LIST_Y + row * ROW_H, LIST_W, ROW_H - 4f});
    }
  }

  private void addDetailPanel() {
    BitmapFont titleFont = FontManager.getInstance().getTahomaFont(14, GOLD, false);
    BitmapFont bodyFont = FontManager.getInstance().getTahomaFont(12, WHITE, false);
    labels.add(
        new GuiBoxedText(titleFont, x + 242f, y + 43f, 308f, 29f, this::detailTitle, () -> GOLD)
            .align(GuiBoxedText.Align.LEFT)
            .shrinkToFit());
    labels.add(
        new GuiBoxedText(bodyFont, x + 242f, y + 78f, 308f, 160f, this::detailBody, () -> WHITE)
            .align(GuiBoxedText.Align.LEFT)
            .wrap());
  }

  private NamedLocation hoveredLocation() {
    float mouseX = Gdx.input.getX();
    float mouseY = Gdx.input.getY();
    for (int row = 0; row < rowBounds.size(); row++) {
      float[] b = rowBounds.get(row);
      if (mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3]) {
        return rowLocation(row);
      }
    }
    return null;
  }

  private String detailTitle() {
    NamedLocation hovered = hoveredLocation();
    return hovered != null ? hovered.displayName() : "Fast Travel";
  }

  private String detailBody() {
    NamedLocation hovered = hoveredLocation();
    if (hovered != null) {
      return "Tile (" + hovered.tileX() + ", " + hovered.tileY() + ")  Z" + hovered.worldZ()
          + "\n\nClick to travel here instantly.";
    }
    return "Select a destination on the left to travel there instantly.\n\n"
        + "More locations will be added here over time.";
  }

  private NamedLocation rowLocation(int row) {
    int index = firstVisible + row;
    return index >= 0 && index < locations.size() ? locations.get(index) : null;
  }

  private String rowName(int row) {
    NamedLocation location = rowLocation(row);
    return location == null ? "" : location.displayName();
  }

  @Override
  public void onTouchUp(float screenX, float screenY) {
    for (int row = 0; row < rows.size(); row++) {
      if (rows.get(row).contains(screenX, screenY)) {
        NamedLocation location = rowLocation(row);
        if (location != null) {
          teleportTo(location);
        }
        return;
      }
    }
    super.onTouchUp(screenX, screenY);
  }

  private void teleportTo(NamedLocation location) {
    if (player == null) {
      return;
    }
    player.setWorldPosition(
        location.tileX() * GameConstants.GRID_W,
        location.tileY() * GameConstants.GRID_H,
        location.worldZ());
    GuiManager.close();
  }

  @Override
  public void onScroll(float amountY, float screenX, float screenY) {
    if (screenX >= x + LIST_X && screenX <= x + LIST_X + LIST_W) {
      int maxFirst = Math.max(0, locations.size() - VISIBLE_ROWS);
      firstVisible = Math.max(0, Math.min(maxFirst, firstVisible + (amountY > 0 ? 1 : -1)));
      return;
    }
    super.onScroll(amountY, screenX, screenY);
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
