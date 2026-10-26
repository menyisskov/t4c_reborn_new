package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.core.JournalWindow;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.GmRank;
import com.perso.T4C.player.Player;
import com.perso.T4C.teleport.LocationDiscovery;
import com.perso.T4C.teleport.TravelCatalogue;
import com.perso.T4C.teleport.TravelCatalogue.Destination;
import com.perso.T4C.ui.FontManager;
import java.util.List;
import java.util.Locale;

/** Categorized, exploration-based fast travel (Ctrl+L). Locked destinations remain visible. */
public final class LocationsScreen extends JournalWindow {
  static String slug(com.perso.T4C.teleport.NamedLocation location) {
    return com.perso.T4C.teleport.NamedLocations.catalogueKey(location);
  }

  private static final int ROWS = 6;
  private final Player player;
  private final GlyphLayout layout = new GlyphLayout();
  private final Rectangle searchBounds = new Rectangle();
  private final Rectangle travelBounds = new Rectangle();
  private String category = "cities";
  private String query = "";
  private boolean searchFocused;
  private List<Destination> results;
  private int selected;
  private int first;
  private int hovered = -1;
  private boolean travelHover;
  private long lastClick;
  private String lastClickId;
  private boolean noLanding;

  public LocationsScreen(Player player) {
    this.player = player;
    LocationDiscovery.discoverNearby(player);
    initJournal(850, 430, "locations.title");
    searchBounds.set(x + 202, y + 38, 252, 32);
    travelBounds.set(x + 580, y + 382, 150, 28);
    refresh();
  }

  private void refresh() {
    String needle = query.toLowerCase(Locale.ROOT);
    results =
        TravelCatalogue.forCategory(category).stream()
            .filter(
                d -> (d.name() + " " + d.description()).toLowerCase(Locale.ROOT).contains(needle))
            .toList();
    selected = results.isEmpty() ? -1 : 0;
    first = 0;
    hovered = -1;
    lastClickId = null;
    noLanding = false;
  }

  private boolean available(Destination d) {
    return LocationDiscovery.available(player, d);
  }

  private void text(
      SpriteBatch batch, String value, float dx, float dy, float width, int size, Color color) {
    BitmapFont font = FontManager.getInstance().getTahomaFont(size, color, false);
    layout.setText(font, value, 0, value.length(), color, width, Align.left, false, "...");
    font.draw(batch, layout, x + dx, y + dy);
  }

  private void paragraph(
      SpriteBatch batch, String value, float dx, float dy, float width, float height, Color color) {
    BitmapFont font = FontManager.getInstance().getTahomaFont(12, color, false);
    String visible = value;
    layout.setText(font, visible, color, width, Align.left, true);
    while (layout.height > height && visible.length() > 8) {
      visible = visible.substring(0, visible.length() - 8);
      layout.setText(font, visible + "...", color, width, Align.left, true);
    }
    font.draw(batch, layout, x + dx, y + dy);
  }

  @Override
  public void render(SpriteBatch batch) {
    super.render(batch);
    text(batch, I18n.key("travel.categories"), 22, 14, 166, 12, JournalTheme.DIM);
    text(
        batch,
        I18n.message("travel.chapter", I18n.key("travel.category." + category), results.size()),
        206,
        14,
        250,
        12,
        JournalTheme.GOLD);
    for (int i = 0; i < TravelCatalogue.CATEGORIES.size(); i++) {
      String id = TravelCatalogue.CATEGORIES.get(i);
      if (category.equals(id)) theme.accentWell(batch, x + 20, y + 38 + i * 38, 166, 32);
      else theme.well(batch, x + 20, y + 38 + i * 38, 166, 32);
      text(
          batch,
          I18n.key("travel.category." + id),
          30,
          48 + i * 38,
          148,
          12,
          category.equals(id) ? JournalTheme.GOLD : JournalTheme.TEXT);
    }
    if (searchFocused)
      theme.accentWell(
          batch, searchBounds.x, searchBounds.y, searchBounds.width, searchBounds.height);
    else theme.well(batch, searchBounds.x, searchBounds.y, searchBounds.width, searchBounds.height);
    text(
        batch,
        query.isEmpty() ? I18n.key("travel.search") : query + (searchFocused ? " |" : ""),
        212,
        48,
        232,
        12,
        query.isEmpty() ? JournalTheme.DIM : JournalTheme.TEXT);
    for (int row = 0; row < ROWS; row++) {
      int index = first + row;
      if (index >= results.size()) break;
      Destination d = results.get(index);
      if (index == selected) theme.accentWell(batch, x + 202, y + 86 + row * 48, 252, 42);
      else theme.well(batch, x + 202, y + 86 + row * 48, 252, 42);
      boolean enabled = available(d);
      text(
          batch,
          d.name(),
          212,
          93 + row * 48,
          232,
          12,
          !enabled
              ? JournalTheme.DIM
              : index == selected || index == hovered ? JournalTheme.GOLD : JournalTheme.TEXT);
      String status = enabled ? I18n.key("travel.discovered") : I18n.key("travel.undiscovered");
      if (!d.mapped()) status = I18n.key("travel.pending_coordinates");
      text(batch, status, 212, 110 + row * 48, 232, 10, JournalTheme.DIM);
    }
    if (results.isEmpty())
      text(batch, I18n.key("travel.empty"), 212, 95, 232, 12, JournalTheme.DIM);
    theme.well(batch, x + 474, y + 38, 354, 377);
    if (selected >= 0 && selected < results.size()) {
      Destination d = results.get(selected);
      paragraph(batch, d.name(), 492, 53, 318, 48, JournalTheme.GOLD);
      text(
          batch,
          d.mapped()
              ? I18n.message("locations.coordinates", d.x(), d.y(), d.z())
              : I18n.key("travel.pending_coordinates"),
          492,
          113,
          318,
          12,
          JournalTheme.ACCENT);
      paragraph(batch, d.description(), 492, 145, 318, 145, JournalTheme.TEXT);
      boolean enabled = available(d);
      String status =
          !d.mapped()
              ? I18n.key("travel.unmapped")
              : player.getGmRank().atLeast(GmRank.GM)
                  ? I18n.key("travel.gm_access")
                  : I18n.key(enabled ? "travel.discovered" : "travel.undiscovered");
      if (noLanding) status = I18n.key("travel.no_landing");
      text(batch, status, 492, 308, 318, 12, enabled ? JournalTheme.ACCENT : JournalTheme.DIM);
      if (!enabled && d.mapped())
        paragraph(batch, I18n.key("travel.discovery_hint"), 492, 332, 318, 42, JournalTheme.DIM);
      if (enabled && travelHover)
        theme.accentWell(
            batch, travelBounds.x, travelBounds.y, travelBounds.width, travelBounds.height);
      else
        theme.well(batch, travelBounds.x, travelBounds.y, travelBounds.width, travelBounds.height);
      text(
          batch,
          I18n.key("locations.travel"),
          627,
          390,
          96,
          12,
          enabled ? JournalTheme.GOLD : JournalTheme.DIM);
    }
    text(batch, I18n.key("travel.hint"), 22, 414, 800, 11, JournalTheme.DIM);
  }

  private int rowAt(float sx, float sy) {
    if (sx < x + 202 || sx > x + 454 || sy < y + 86) return -1;
    int row = (int) ((sy - y - 86) / 48);
    int index = first + row;
    return row < ROWS && sy - y - 86 - row * 48 <= 42 && index < results.size() ? index : -1;
  }

  @Override
  public void onMouseMove(float sx, float sy) {
    hovered = rowAt(sx, sy);
    travelHover = travelBounds.contains(sx, sy);
    super.onMouseMove(sx, sy);
  }

  @Override
  public void onTouchDown(float sx, float sy) {
    if (searchBounds.contains(sx, sy)) {
      searchFocused = true;
      return;
    }
    searchFocused = false;
    if (sx >= x + 20 && sx <= x + 186 && sy >= y + 38) {
      int index = (int) ((sy - y - 38) / 38);
      if (index >= 0 && index < TravelCatalogue.CATEGORIES.size()) {
        category = TravelCatalogue.CATEGORIES.get(index);
        query = "";
        refresh();
        return;
      }
    }
    int index = rowAt(sx, sy);
    if (index >= 0) {
      selected = index;
      noLanding = false;
      Destination d = results.get(index);
      long now = System.currentTimeMillis();
      boolean twice = d.id().equals(lastClickId) && now - lastClick < 350;
      lastClickId = d.id();
      lastClick = now;
      if (twice) travel();
      return;
    }
    if (travelBounds.contains(sx, sy)) {
      travel();
      return;
    }
    super.onTouchDown(sx, sy);
  }

  private void travel() {
    if (selected < 0 || selected >= results.size()) return;
    Destination destination = results.get(selected);
    if (!available(destination)) return;
    var arrival = com.perso.T4C.teleport.TravelArrival.find(destination);
    if (arrival == null) {
      noLanding = true;
      return;
    }
    player.getMovement().stop();
    player.setWorldPosition(
        arrival.x() * GameConstants.GRID_W, arrival.y() * GameConstants.GRID_H, destination.z());
    GuiManager.close();
  }

  @Override
  public void onScroll(float amount, float sx, float sy) {
    first =
        Math.max(0, Math.min(Math.max(0, results.size() - ROWS), first + (amount > 0 ? 1 : -1)));
    hovered = -1;
  }

  @Override
  public boolean onKeyDown(int key) {
    if (key == Input.Keys.ESCAPE) GuiManager.close();
    else if (key == Input.Keys.ENTER || key == Input.Keys.NUMPAD_ENTER) {
      searchFocused = false;
      travel();
    } else if (key == Input.Keys.BACKSPACE && searchFocused && !query.isEmpty()) {
      query = query.substring(0, query.offsetByCodePoints(query.length(), -1));
      refresh();
    } else if (key == Input.Keys.TAB) searchFocused = !searchFocused;
    else if ((key == Input.Keys.UP || key == Input.Keys.DOWN) && !results.isEmpty()) {
      selected =
          Math.max(0, Math.min(results.size() - 1, selected + (key == Input.Keys.UP ? -1 : 1)));
      first = Math.max(0, Math.min(first, selected));
      if (selected >= first + ROWS) first = selected - ROWS + 1;
    }
    return true;
  }

  @Override
  public boolean onKeyTyped(char c) {
    if (searchFocused
        && c >= 32
        && c != 127
        && query.length() < 96
        && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
        && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
      query += c;
      refresh();
    }
    return true;
  }
}
