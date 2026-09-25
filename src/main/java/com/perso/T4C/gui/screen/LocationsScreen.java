package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.teleport.NamedLocation;
import com.perso.T4C.teleport.NamedLocations;
import com.perso.T4C.ui.FontManager;
import java.util.List;

/**
 * Fast travel (Ctrl+L), on the quest-journal art: the destinations fill the six slots on the left
 * (scroll for more), the selected one is described on the right, and Travel (or a double-click,
 * or Enter) goes there.
 */
public final class LocationsScreen extends GuiScreenBase {
  private static final int VISIBLE_ROWS = 6;
  // The six slot boxes of GUI_BackQuest (window-relative).
  private static final float[] SLOT_Y = {50f, 98f, 146f, 194f, 242f, 290f};
  private static final float SLOT_X = 25f;
  private static final float SLOT_W = 166f;
  private static final float SLOT_H = 34f;
  private static final Rectangle SCROLL = new Rectangle(197f, 38f, 30f, 290f);
  private static final float DETAIL_X = 244f;
  private static final float DETAIL_W = 306f;
  private static final long DOUBLE_CLICK_MS = 350L;
  private static final Color GOLD = Color.valueOf("F2B705");
  private static final Color TEXT = Color.valueOf("E6D8BC");
  private static final Color DIM = Color.valueOf("A89E86");

  private final Player player;
  private final List<NamedLocation> locations;
  private final GlyphLayout layout = new GlyphLayout();
  private final TextureRegion[] button = new TextureRegion[3];
  private final TextureRegion scrollTick;
  private final Rectangle travelBounds = new Rectangle();
  private int firstVisible;
  private int selected;
  private int hovered = -1;
  private boolean travelHover;
  private long lastClickMs;
  private int lastClickIndex = -1;

  public LocationsScreen(Player player) {
    this.player = player;
    this.locations = NamedLocations.forPlayer(player);
    background = GuiSprites.load("GUI_BackQuest");
    scrollTick = GuiSprites.load("GUI_ScrollTick");
    button[0] = GuiSprites.load("GUI_ButtonUp");
    button[1] = GuiSprites.load("GUI_ButtonHUp");
    button[2] = GuiSprites.load("GUI_ButtonDown");
    centerOnScreen();
    addCloseButton(548f, 5f);
    travelBounds.set(x + 337f, y + 292f, 120f, 28f);
  }

  static String slug(NamedLocation location) {
    return location.unlockZoneId() != null
        ? location.unlockZoneId()
        : location.displayName().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
  }

  private NamedLocation selectedLocation() {
    int index = hovered >= 0 ? hovered : selected;
    return index >= 0 && index < locations.size() ? locations.get(index) : null;
  }

  // ---------------------------------------------------------------- rendering

  @Override
  public void render(SpriteBatch batch) {
    if (background == null) {
      super.render(batch);
      return;
    }
    TextureRegion art = background;
    background = null;
    GuiDraw.drawOverlayWithPatch(batch, art, x, y, 239, 287, 318, 37, 240, 120, 150, 37);
    super.render(batch);
    background = art;

    BitmapFont title = FontManager.getInstance().getHaettenschweilerFont(18, GOLD);
    layout.setText(title, I18n.key("locations.title"));
    title.draw(batch, layout, x + 288f - layout.width / 2f, y + 3f);

    drawList(batch);
    drawScrollTick(batch);
    drawDetails(batch);
    drawTravelButton(batch);
  }

  private void drawList(SpriteBatch batch) {
    BitmapFont normal = FontManager.getInstance().getTahomaFont(13, TEXT, true);
    BitmapFont hover = FontManager.getInstance().getTahomaFont(13, Color.WHITE, true);
    BitmapFont active = FontManager.getInstance().getTahomaFont(13, GOLD, true);
    for (int row = 0; row < VISIBLE_ROWS; row++) {
      int index = firstVisible + row;
      if (index >= locations.size()) break;
      BitmapFont font = index == selected ? active : index == hovered ? hover : normal;
      String name = locations.get(index).displayName();
      layout.setText(font, name, font.getColor(), SLOT_W - 16f, Align.center, false);
      font.draw(batch, layout, x + SLOT_X + 8f, y + SLOT_Y[row] + 11f);
    }
    if (locations.isEmpty()) {
      BitmapFont dim = FontManager.getInstance().getTahomaFont(12, DIM, false);
      dim.draw(batch, I18n.key("locations.none"), x + SLOT_X + 8f, y + SLOT_Y[0] + 11f);
    }
  }

  private void drawScrollTick(SpriteBatch batch) {
    int maxFirst = Math.max(0, locations.size() - VISIBLE_ROWS);
    if (scrollTick == null || maxFirst == 0) return;
    float travel = SCROLL.height - 60f - scrollTick.getRegionHeight();
    float tickY = y + SCROLL.y + 30f + travel * firstVisible / maxFirst;
    GuiDraw.drawRegionFlipped(
        batch, scrollTick, x + SCROLL.x + (SCROLL.width - scrollTick.getRegionWidth()) / 2f, tickY);
  }

  private void drawDetails(SpriteBatch batch) {
    NamedLocation location = selectedLocation();
    if (location == null) return;
    String slug = slug(location);
    BitmapFont name = FontManager.getInstance().getHaettenschweilerFont(20, GOLD);
    BitmapFont body = FontManager.getInstance().getJetBrainsMonoFont(11, TEXT);
    BitmapFont dim = FontManager.getInstance().getJetBrainsMonoFont(11, DIM);
    name.draw(batch, location.displayName(), x + DETAIL_X, y + 46f);
    float lineY = y + 74f;
    String levels = I18n.has("location." + slug + ".levels")
        ? I18n.message("locations.levels", I18n.key("location." + slug + ".levels"))
        : null;
    if (levels != null) {
      body.draw(batch, levels, x + DETAIL_X, lineY);
      lineY += 16f;
    }
    String kind =
        location.unlockZoneId() == null
            ? I18n.key("locations.kind_landmark")
            : I18n.key("locations.kind_unlocked");
    dim.draw(batch, kind, x + DETAIL_X, lineY);
    lineY += 20f;
    if (I18n.has("location." + slug + ".desc")) {
      body.draw(
          batch, I18n.key("location." + slug + ".desc"), x + DETAIL_X, lineY, DETAIL_W, Align.left, true);
    }
    dim.draw(
        batch,
        I18n.key("locations.hint"),
        x + DETAIL_X,
        y + 204f,
        DETAIL_W,
        Align.left,
        true);
  }

  private void drawTravelButton(SpriteBatch batch) {
    boolean enabled = selected >= 0 && selected < locations.size();
    TextureRegion bg = !enabled ? button[0] : travelHover ? button[1] : button[0];
    if (bg != null) {
      Color previous = new Color(batch.getColor());
      if (!enabled) batch.setColor(0.55f, 0.55f, 0.55f, previous.a);
      GuiDraw.drawRegionFlipped(
          batch, bg, travelBounds.x, travelBounds.y, travelBounds.width, travelBounds.height);
      batch.setColor(previous);
    }
    BitmapFont font = FontManager.getInstance().getTahomaFont(13, Color.BLACK, true);
    layout.setText(font, I18n.key("locations.travel"));
    font.draw(
        batch,
        layout,
        travelBounds.x + (travelBounds.width - layout.width) / 2f,
        travelBounds.y + 7f);
  }

  // ---------------------------------------------------------------- input

  private int rowAt(float screenX, float screenY) {
    for (int row = 0; row < VISIBLE_ROWS; row++) {
      if (screenX >= x + SLOT_X
          && screenX <= x + SLOT_X + SLOT_W
          && screenY >= y + SLOT_Y[row]
          && screenY <= y + SLOT_Y[row] + SLOT_H) {
        int index = firstVisible + row;
        return index < locations.size() ? index : -1;
      }
    }
    return -1;
  }

  @Override
  public void onMouseMove(float screenX, float screenY) {
    hovered = rowAt(screenX, screenY);
    travelHover = travelBounds.contains(screenX, screenY);
    super.onMouseMove(screenX, screenY);
  }

  @Override
  public void onTouchDown(float screenX, float screenY) {
    int index = rowAt(screenX, screenY);
    if (index >= 0) {
      long now = System.currentTimeMillis();
      boolean doubleClick = index == lastClickIndex && now - lastClickMs < DOUBLE_CLICK_MS;
      selected = index;
      lastClickIndex = index;
      lastClickMs = now;
      if (doubleClick) travel();
      return;
    }
    if (travelBounds.contains(screenX, screenY)) {
      travel();
      return;
    }
    if (new Rectangle(x + SCROLL.x, y + SCROLL.y, SCROLL.width, SCROLL.height)
        .contains(screenX, screenY)) {
      scroll(screenY < y + SCROLL.y + SCROLL.height / 2f ? -1 : 1);
      return;
    }
    super.onTouchDown(screenX, screenY);
  }

  private void travel() {
    if (player == null || selected < 0 || selected >= locations.size()) return;
    NamedLocation location = locations.get(selected);
    player.setWorldPosition(
        location.tileX() * GameConstants.GRID_W,
        location.tileY() * GameConstants.GRID_H,
        location.worldZ());
    GuiManager.close();
  }

  private void scroll(int rows) {
    int maxFirst = Math.max(0, locations.size() - VISIBLE_ROWS);
    firstVisible = Math.max(0, Math.min(maxFirst, firstVisible + rows));
  }

  @Override
  public void onScroll(float amountY, float screenX, float screenY) {
    scroll(amountY > 0 ? 1 : -1);
  }

  @Override
  public boolean onKeyDown(int keycode) {
    switch (keycode) {
      case Input.Keys.ESCAPE -> GuiManager.close();
      case Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> travel();
      default -> {
        return false;
      }
    }
    return true;
  }
}
