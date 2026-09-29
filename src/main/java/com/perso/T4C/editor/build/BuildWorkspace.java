package com.perso.T4C.editor.build;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.perso.T4C.helper.*;
import com.perso.T4C.i18n.I18n;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

/** Docked build workspace. UI gestures produce atomic, file-format-independent edits. */
public final class BuildWorkspace {
  public interface Host {
    MapReader map();

    byte[] collisions();

    Vector3 world(int x, int y);

    void commit(String label, BuildEdits.Edit edit);

    void beginSelection();

    BuildGeometry.Point decorAt(int x, int y);

    void undo();

    void redo();

    void save();

    void status(String message);

    void pan(float dx, float dy);

    void preview(
        SpriteBatch batch, String name, float x, float y, float sx, float sy, float ox, float oy);
  }

  public enum Tool {
    SELECT,
    STAMP,
    PAINT,
    LINE,
    WALL,
    ROOM,
    CAPTURE,
    TEMPLATE,
    MULTI_SELECT
  }

  private record Button(Rectangle box, String label, Runnable action, boolean active) {}

  private record Card(Rectangle box, AssetCatalog.Asset asset, MapStamp stamp) {}

  private final Host host;
  private AssetCatalog catalog;
  private final SpriteLoader sprites;
  private final BitmapFont font;
  private final BuildPreferences preferences;
  private final List<Button> buttons = new ArrayList<>();
  private final List<Card> cards = new ArrayList<>();
  private final List<MapStamp> templates = new ArrayList<>();
  private final Map<MapStamp, FrameBuffer> thumbnails = new HashMap<>();
  private List<AssetCatalog.Asset> results = List.of();
  private AssetCatalog.Asset selected;
  private MapStamp template;
  private WallStyle wallStyle;
  private AssetCatalog.Category category = AssetCatalog.Category.ALL;
  private AssetCatalog.Material material = AssetCatalog.Material.ALL;
  private Tool tool = Tool.SELECT;
  private boolean visible = true,
      templateTab = false,
      grouped = true,
      favoritesOnly = false,
      recentOnly = false;
  private boolean searchFocus = false,
      nameFocus = false,
      dragging = false,
      paletteDrag = false,
      resizing = false,
      panning = false,
      replace = false,
      blocking = false;
  private String query = "",
      templateName = "",
      orientation = "",
      dropdown = "",
      templateCategory = "all";
  private int scroll = 0, startX, startY, endX, endY, lastX, lastY, spacing = 1;
  private float width, left, top, gridTop, gridBottom;
  private final LinkedHashSet<BuildGeometry.Point> painted = new LinkedHashSet<>();
  private List<BuildEdits.Placement> preview = List.of();
  private final LinkedHashSet<BuildGeometry.Point> selection = new LinkedHashSet<>();
  private boolean movingSelection, additiveSelection;
  private String invalid = "";
  private boolean validated = false;
  private static final Path PREFS = Path.of("editor_build_preferences.json");
  private static final Path TEMPLATE_DIR = Path.of("assets/editor/templates");

  public BuildWorkspace(Host host, SpriteLoader sprites, BitmapFont font) {
    this.host = host;
    this.sprites = sprites;
    this.font = font;
    catalog = new AssetCatalog(sprites.getSprites());
    preferences = BuildPreferences.load(PREFS);
    width = preferences.panelWidth;
    try {
      wallStyle = WallStyle.load(Path.of("assets/editor/brick-wall.json"));
    } catch (Exception e) {
      host.status(t("style_missing"));
    }
    reloadTemplates();
    filter();
  }

  private static String t(String key) {
    return I18n.key("editor.build." + key);
  }

  private static String label(Enum<?> e) {
    return t(e.name().toLowerCase(Locale.ROOT));
  }

  public boolean visible() {
    return visible;
  }

  public void clearSelection() {
    cancel();
  }

  public boolean multiSelecting() {
    return tool == Tool.MULTI_SELECT;
  }

  public boolean typing() {
    return visible && (searchFocus || nameFocus);
  }

  public void toggle() {
    visible = !visible;
    cancel();
    savePreferences();
  }

  public void selectTool(Tool value) {
    tool = value;
    searchFocus = false;
    nameFocus = false;
    cancel();
    if (value == Tool.MULTI_SELECT) host.beginSelection();
  }

  public void reloadSprites() {
    cancel();
    selected = null;
    catalog = new AssetCatalog(sprites.getSprites());
    reloadTemplates();
    filter();
  }

  public void reset() {
    cancel();
    template = null;
    tool = Tool.SELECT;
  }

  private void cancel() {
    dragging = false;
    paletteDrag = false;
    resizing = false;
    panning = false;
    preview = List.of();
    painted.clear();
    selection.clear();
    movingSelection = false;
    invalid = "";
    validated = false;
  }

  private boolean overPanel(int x, int y) {
    return visible && x >= left && y >= 48;
  }

  private void savePreferences() {
    try {
      preferences.panelWidth = width;
      preferences.save(PREFS);
    } catch (Exception e) {
      host.status(t("preferences_failed"));
    }
  }

  private void reloadTemplates() {
    for (var buffer : thumbnails.values()) buffer.dispose();
    thumbnails.clear();
    templates.clear();
    if (!Files.isDirectory(TEMPLATE_DIR)) return;
    try (var paths = Files.walk(TEMPLATE_DIR)) {
      for (Path path : paths.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
        try {
          var stamp = MapStamp.load(path);
          if (stamp != null) templates.add(stamp);
        } catch (Exception e) {
          host.status(t("template_failed") + ": " + path.getFileName());
        }
      }
    } catch (Exception e) {
      host.status(t("template_failed"));
    }
  }

  private void filter() {
    results =
        catalog
            .search(query, category, material, false, preferences.favorites, favoritesOnly)
            .stream()
            .filter(a -> orientation.isEmpty() || a.orientation().equalsIgnoreCase(orientation))
            .filter(a -> !recentOnly || preferences.recent.contains(a.name()))
            .toList();
    if (grouped) {
      var families = new HashSet<String>();
      results =
          results.stream().filter(a -> families.add(a.family().toLowerCase(Locale.ROOT))).toList();
    }
    scroll = 0;
  }

  private void choose(AssetCatalog.Asset asset) {
    cancel();
    selected = asset;
    template = null;
    tool = Tool.PAINT;
    blocking = asset.category() == AssetCatalog.Category.WALLS;
    preferences.remember(asset.name());
    savePreferences();
    searchFocus = false;
    nameFocus = false;
  }

  private void cycleVariant(int direction) {
    if (selected == null) return;
    var variants = catalog.variants(selected);
    int index = variants.indexOf(selected);
    validated = false;
    selected = variants.get(Math.floorMod(index + direction, variants.size()));
    preferences.remember(selected.name());
    savePreferences();
  }

  private void favorite() {
    if (selected == null) return;
    if (!preferences.favorites.remove(selected.name())) preferences.favorites.add(selected.name());
    savePreferences();
    filter();
  }

  private int columns() {
    return Math.max(2, (int) ((width - 24) / 116));
  }

  private int rows() {
    return Math.max(1, (int) ((gridTop - gridBottom) / 116));
  }

  private List<MapStamp> filteredTemplates() {
    String q = query.toLowerCase(Locale.ROOT);
    return templates.stream()
        .filter(s -> templateCategory.equals("all") || s.category().equals(templateCategory))
        .filter(
            s ->
                (I18n.resolve(s.name()) + " " + s.category() + " " + s.source())
                    .toLowerCase(Locale.ROOT)
                    .contains(q))
        .toList();
  }

  public void render(SpriteBatch batch, ShapeRenderer shapes) {
    if (!visible) return;
    int sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
    width = Math.max(290, Math.min(Math.min(640, sw - 240), width));
    left = sw - width;
    top = sh - 52;
    gridTop = top - 250;
    gridBottom = 178;
    var matrix = new Matrix4().setToOrtho2D(0, 0, sw, sh);
    batch.setProjectionMatrix(matrix);
    shapes.setProjectionMatrix(matrix);
    buttons.clear();
    cards.clear();
    button(
        left + 12,
        top - 35,
        width - 62,
        30,
        t("multi_select"),
        () -> selectTool(Tool.MULTI_SELECT),
        tool == Tool.MULTI_SELECT);
    button(sw - 42, top - 35, 30, 30, "X", this::toggle, false);
    float y = top - 72;
    button(
        left + 12,
        y,
        (width - 28) / 2,
        28,
        t("assets"),
        () -> {
          templateTab = false;
          filter();
        },
        !templateTab);
    button(
        left + 16 + (width - 28) / 2,
        y,
        (width - 28) / 2,
        28,
        t("templates"),
        () -> {
          templateTab = true;
          scroll = 0;
          reloadTemplates();
        },
        templateTab);
    y -= 34;
    button(
        left + 12,
        y,
        width - 24,
        28,
        (query.isEmpty() ? t("search") : query) + (searchFocus ? " |" : ""),
        () -> {
          searchFocus = true;
          nameFocus = false;
        },
        searchFocus);
    y -= 34;
    button(
        left + 12,
        y,
        (width - 32) / 2,
        26,
        t("category") + ": " + (templateTab ? t(templateCategory) : label(category)),
        () -> {
          if (templateTab) {
            var values = List.of("all", "buildings", "caves", "underground", "custom");
            templateCategory = values.get((values.indexOf(templateCategory) + 1) % values.size());
            scroll = 0;
          } else dropdown = dropdown.equals("category") ? "" : "category";
        },
        false);
    button(
        left + 20 + (width - 32) / 2,
        y,
        (width - 32) / 2,
        26,
        templateTab ? t("scenery_only") : t("material") + ": " + label(material),
        () -> {
          if (!templateTab) dropdown = dropdown.equals("material") ? "" : "material";
        },
        false);
    y -= 30;
    float quarter = (width - 36) / 4;
    if (!templateTab) {
      button(
          left + 12,
          y,
          quarter,
          25,
          grouped ? t("families") : t("variants"),
          () -> {
            grouped = !grouped;
            filter();
          },
          grouped);
      button(
          left + 16 + quarter,
          y,
          quarter,
          25,
          t("favorites"),
          () -> {
            favoritesOnly = !favoritesOnly;
            filter();
          },
          favoritesOnly);
      button(
          left + 20 + 2 * quarter,
          y,
          quarter,
          25,
          t("recent"),
          () -> {
            recentOnly = !recentOnly;
            filter();
          },
          recentOnly);
      button(
          left + 24 + 3 * quarter,
          y,
          quarter,
          25,
          orientation.isEmpty() ? t("facing") : orientation,
          () -> dropdown = dropdown.equals("facing") ? "" : "facing",
          false);
    } else button(left + 12, y, width - 24, 25, t("template_hint"), () -> {}, false);
    y -= 31;
    int index = 0;
    for (Tool value : Tool.values()) {
      if (value == Tool.MULTI_SELECT) continue;
      int col = index % 4, row = index / 4;
      button(
          left + 12 + col * (quarter + 4),
          y - row * 29,
          quarter,
          25,
          label(value),
          () -> {
            selectTool(value);
            if (value == Tool.TEMPLATE) templateTab = true;
          },
          tool == value);
      index++;
    }
    var stamps = filteredTemplates();
    int count = templateTab ? stamps.size() : results.size();
    int cols = columns(), rows = rows(), maxScroll = Math.max(0, (count + cols - 1) / cols - rows);
    scroll = Math.min(scroll, maxScroll);
    float cw = (width - 24) / cols;
    for (int i = scroll * cols; i < Math.min(count, (scroll + rows) * cols); i++) {
      int local = i - scroll * cols;
      var box =
          new Rectangle(
              left + 12 + (local % cols) * cw, gridTop - (local / cols + 1) * 116, cw - 5, 110);
      cards.add(
          new Card(box, templateTab ? null : results.get(i), templateTab ? stamps.get(i) : null));
    }
    button(left + 12, 143, 40, 26, "<", () -> cycleVariant(-1), false);
    button(left + 56, 143, 40, 26, ">", () -> cycleVariant(1), false);
    button(
        left + 100,
        143,
        80,
        26,
        t("favorite"),
        this::favorite,
        selected != null && preferences.favorites.contains(selected.name()));
    button(
        left + 184,
        143,
        width - 196,
        26,
        t("replace"),
        () -> {
          replace = !replace;
          updatePreview();
        },
        replace);
    button(
        left + 12,
        111,
        (width - 32) / 2,
        26,
        t("blocking"),
        () -> {
          blocking = !blocking;
          updatePreview();
        },
        blocking);
    button(
        left + 20 + (width - 32) / 2,
        111,
        (width - 32) / 2,
        26,
        t("spacing") + ": " + spacing,
        () -> {
          spacing = spacing % 8 + 1;
          updatePreview();
        },
        false);
    button(
        left + 12,
        79,
        (width - 32) / 2,
        26,
        t("undo"),
        () -> {
          cancel();
          host.undo();
        },
        false);
    button(
        left + 20 + (width - 32) / 2,
        79,
        (width - 32) / 2,
        26,
        t("redo"),
        () -> {
          cancel();
          host.redo();
        },
        false);
    button(
        left + 12,
        45,
        width - 24,
        28,
        templateName.isEmpty() ? t("template_name") : templateName + (nameFocus ? " |" : ""),
        () -> {
          nameFocus = true;
          searchFocus = false;
        },
        nameFocus);
    int regularButtons = buttons.size();
    if (!dropdown.isEmpty()) {
      float dy = top - 175;
      List<String> options =
          dropdown.equals("category")
              ? Arrays.stream(AssetCatalog.Category.values()).map(Enum::name).toList()
              : dropdown.equals("material")
                  ? Arrays.stream(AssetCatalog.Material.values()).map(Enum::name).toList()
                  : List.of(
                      "ALL", "SE", "SW", "N", "S", "E", "W", "NE", "NW", "000", "045", "090", "135",
                      "180", "225", "270", "315");
      for (String option : options) {
        String kind = dropdown;
        button(
            left + 12,
            dy,
            width - 24,
            23,
            kind.equals("facing")
                ? option
                : label(
                    kind.equals("category")
                        ? AssetCatalog.Category.valueOf(option)
                        : AssetCatalog.Material.valueOf(option)),
            () -> {
              if (kind.equals("category")) category = AssetCatalog.Category.valueOf(option);
              else if (kind.equals("material")) material = AssetCatalog.Material.valueOf(option);
              else orientation = option.equals("ALL") ? "" : option;
              dropdown = "";
              filter();
            },
            false);
        dy -= 24;
      }
    }
    if (templateTab && dropdown.isEmpty()) for (var card : cards) thumbnail(card.stamp, batch);
    batch.setProjectionMatrix(matrix);
    shapes.setProjectionMatrix(matrix);
    shapes.begin(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(.065f, .08f, .105f, 1);
    shapes.rect(left, 0, width, sh - 46);
    shapes.setColor(.18f, .27f, .33f, 1);
    shapes.rect(left, 0, 3, sh - 46);
    for (var c : cards) {
      shapes.setColor(
          (c.asset != null && c.asset == selected) || (c.stamp != null && c.stamp == template)
              ? new Color(.14f, .35f, .40f, 1)
              : new Color(.11f, .14f, .19f, 1));
      shapes.rect(c.box.x, c.box.y, c.box.width, c.box.height);
    }
    for (var b : buttons) {
      shapes.setColor(b.active ? new Color(.16f, .36f, .40f, 1) : new Color(.14f, .18f, .24f, 1));
      shapes.rect(b.box.x, b.box.y, b.box.width, b.box.height);
    }
    shapes.end();
    batch.begin();
    float oldX = font.getData().scaleX, oldY = font.getData().scaleY;
    font.getData().setScale(.82f);
    if (dropdown.isEmpty())
      for (var c : cards) {
        String name = c.asset != null ? c.asset.label() : I18n.resolve(c.stamp.name());
        cardName(batch, name, c.box);
        String sprite =
            c.asset != null
                ? c.asset.name()
                : c.stamp.cells().stream()
                    .map(MapStamp.Cell::tile)
                    .map(MapStamp.Tile::decor)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        if (c.stamp != null && thumbnails.containsKey(c.stamp)) {
          var region = new TextureRegion(thumbnails.get(c.stamp).getColorBufferTexture());
          region.flip(false, true);
          batch.setColor(Color.WHITE);
          batch.draw(region, c.box.x + 4, c.box.y + 4, c.box.width - 8, 76);
        } else if (sprite != null)
          try {
            var region = sprites.getRegionFromSpriteName(sprite);
            if (region != null) {
              float scale =
                  Math.min(
                      (c.box.width - 14) / region.getRegionWidth(), 72f / region.getRegionHeight());
              float w = region.getRegionWidth() * scale, h = region.getRegionHeight() * scale;
              batch.setColor(Color.WHITE);
              batch.draw(region, c.box.x + (c.box.width - w) / 2, c.box.y + 9, w, h);
            }
          } catch (Exception ignored) {
          }
      }
    for (var b : buttons.subList(dropdown.isEmpty() ? 0 : regularButtons, buttons.size()))
      text(batch, b.label, b.box.x + 6, b.box.y + b.box.height - 6, b.box.width - 12, Color.WHITE);
    if (dropdown.isEmpty()) {
      String name =
          tool == Tool.MULTI_SELECT
              ? t("selected_count") + ": " + selection.size()
              : template != null
                  ? I18n.resolve(template.name())
                      + "  "
                      + template.width()
                      + " x "
                      + template.height()
                  : selected != null
                      ? selected.name() + "  " + selected.width() + " x " + selected.height()
                      : t("choose");
      text(batch, name, left + 12, gridBottom + 10, width - 24, new Color(.65f, .85f, .9f, 1));
    }
    if (dropdown.isEmpty()) {
      int mx = Gdx.input.getX(), my = Gdx.graphics.getHeight() - Gdx.input.getY();
      for (var card : cards)
        if (card.box.contains(mx, my)) {
          String full = card.asset != null ? card.asset.name() : I18n.resolve(card.stamp.name());
          text(batch, full, left + 12, gridBottom + 29, width - 24, Color.WHITE);
        }
    }
    if (dropdown.isEmpty())
      text(
          batch,
          t(tool == Tool.MULTI_SELECT ? "selection_hint" : "hint"),
          left + 12,
          31,
          width - 24,
          new Color(.64f, .7f, .77f, 1));
    font.getData().setScale(oldX, oldY);
    font.setColor(Color.WHITE);
    batch.end();
  }

  private void thumbnail(MapStamp stamp, SpriteBatch batch) {
    if (stamp == null || thumbnails.containsKey(stamp)) return;
    var buffer = new FrameBuffer(Pixmap.Format.RGBA8888, 224, 144, false);
    var projection = new OrthographicCamera();
    float worldWidth = stamp.width() * 32 + 256, worldHeight = stamp.height() * 16 + 320;
    float ratio = 224f / 144;
    if (worldWidth / worldHeight < ratio) worldWidth = worldHeight * ratio;
    else worldHeight = worldWidth / ratio;
    projection.setToOrtho(true, worldWidth, worldHeight);
    projection.position.set(stamp.width() * 16, stamp.height() * 8 - 80, 0);
    projection.update();
    buffer.begin();
    Gdx.gl.glClearColor(.07f, .09f, .12f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    batch.setProjectionMatrix(projection.combined);
    batch.begin();
    batch.setColor(Color.WHITE);
    for (var c : stamp.cells()) {
      var tile = c.tile();
      if (tile.ground() != null)
        host.preview(batch, tile.ground(), c.x() * 32, c.y() * 16, 1, 1, 0, 0);
    }
    for (var c : stamp.cells()) {
      var tile = c.tile();
      if (tile.decor() != null)
        host.preview(
            batch,
            tile.decor(),
            c.x() * 32,
            c.y() * 16,
            tile.scaleX(),
            tile.scaleY(),
            tile.offsetX(),
            tile.offsetY());
    }
    batch.end();
    buffer.end();
    thumbnails.put(stamp, buffer);
  }

  public void dispose() {
    savePreferences();
    for (var buffer : thumbnails.values()) buffer.dispose();
    thumbnails.clear();
  }

  private void button(
      float x, float y, float w, float h, String label, Runnable action, boolean active) {
    buttons.add(new Button(new Rectangle(x, y, w, h), label, action, active));
  }

  private void cardName(SpriteBatch batch, String value, Rectangle box) {
    int end = value.length();
    while (end > 1 && new GlyphLayout(font, value.substring(0, end)).width > box.width - 10) end--;
    if (end < value.length()) {
      int word = value.lastIndexOf(' ', end);
      if (word > 0) end = word;
    }
    font.setColor(Color.WHITE);
    font.draw(batch, value.substring(0, end), box.x + 5, box.y + box.height - 5);
    if (end < value.length())
      text(
          batch,
          value.substring(end).stripLeading(),
          box.x + 5,
          box.y + box.height - 18,
          box.width - 10,
          Color.WHITE);
  }

  private void text(SpriteBatch batch, String value, float x, float y, float max, Color color) {
    String s = value;
    GlyphLayout layout = new GlyphLayout(font, s);
    while (layout.width > max && s.length() > 3) {
      s = s.substring(0, s.length() - 4) + "...";
      layout.setText(font, s);
    }
    font.setColor(color);
    font.draw(batch, s, x, y);
  }

  public boolean touchDown(int x, int y, int button) {
    if (!visible) return false;
    if (button != Input.Buttons.LEFT && overPanel(x, y)) return true;
    lastX = x;
    lastY = y;
    if (Math.abs(x - left) < 6 && y > 48) {
      resizing = true;
      return true;
    }
    if (overPanel(x, y)) {
      float uy = Gdx.graphics.getHeight() - y;
      for (int i = buttons.size() - 1; i >= 0; i--)
        if (buttons.get(i).box.contains(x, uy)) {
          buttons.get(i).action.run();
          return true;
        }
      if (dropdown.isEmpty())
        for (var c : cards)
          if (c.box.contains(x, uy)) {
            if (c.asset != null) {
              choose(c.asset);
              paletteDrag = true;
            } else {
              validated = false;
              selected = null;
              template = c.stamp;
              tool = Tool.TEMPLATE;
              paletteDrag = true;
              searchFocus = false;
              nameFocus = false;
            }
            return true;
          }
      return true;
    }
    searchFocus = false;
    nameFocus = false;
    dropdown = "";
    if (y < 48) return false;
    if (button == Input.Buttons.RIGHT || button == Input.Buttons.MIDDLE) {
      panning = true;
      return true;
    }
    if (button != Input.Buttons.LEFT || tool == Tool.SELECT) return false;
    if (tool == Tool.MULTI_SELECT) {
      var p = tile(x, y);
      startX = endX = p.x();
      startY = endY = p.y();
      additiveSelection =
          Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
              || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
      var hit = selection.contains(p) ? p : selectionHit(x, y);
      movingSelection = !additiveSelection && hit != null && selection.contains(hit);
      if (!movingSelection && !additiveSelection) selection.clear();
      dragging = true;
      invalid = "";
      return true;
    }
    beginGesture(x, y);
    return true;
  }

  private void beginGesture(int x, int y) {
    var tile = tile(x, y);
    startX = endX = tile.x();
    startY = endY = tile.y();
    dragging = true;
    painted.clear();
    painted.add(tile);
    updatePreview();
  }

  public boolean touchDragged(int x, int y) {
    if (!visible) return false;
    if (resizing) {
      width = Gdx.graphics.getWidth() - x;
      return true;
    }
    if (panning) {
      host.pan(x - lastX, y - lastY);
      lastX = x;
      lastY = y;
      return true;
    }
    if (paletteDrag) {
      // Templates remain single structures; sprites become a paint stroke on entering the canvas.
      if (selected != null && !overPanel(x, y) && y >= 48) {
        paletteDrag = false;
        beginGesture(x, y);
        return true;
      }
      return true;
    }
    if (!dragging) return false;
    if (tool == Tool.PAINT && (overPanel(x, y) || y < 48)) return true;
    var p = tile(x, y);
    if (tool == Tool.PAINT)
      painted.addAll(BuildGeometry.line(endX, endY, p.x(), p.y(), spacing, false));
    endX = p.x();
    endY = p.y();
    updatePreview();
    return true;
  }

  public boolean touchUp(int x, int y, int button) {
    if (!visible) return false;
    if (resizing) {
      resizing = false;
      savePreferences();
      return true;
    }
    if (panning) {
      panning = false;
      return true;
    }
    if (paletteDrag) {
      paletteDrag = false;
      if (!overPanel(x, y) && y >= 48) {
        var p = tile(x, y);
        startX = endX = p.x();
        startY = endY = p.y();
        if (tool == Tool.PAINT) {
          painted.clear();
          painted.add(p);
        }
        updatePreview();
        place();
      }
      return true;
    }
    if (!dragging) return false;
    dragging = false;
    if (overPanel(x, y) || y < 48) {
      preview = List.of();
      return true;
    }
    var p = tile(x, y);
    if (tool == Tool.PAINT)
      painted.addAll(BuildGeometry.line(endX, endY, p.x(), p.y(), spacing, false));
    endX = p.x();
    endY = p.y();
    if (tool == Tool.MULTI_SELECT) {
      finishSelection(x, y);
      return true;
    }
    if (tool == Tool.CAPTURE) {
      updatePreview();
      if (invalid.isEmpty()) capture();
      else host.status(invalid);
      return true;
    }
    updatePreview();
    place();
    return true;
  }

  private BuildGeometry.Point selectionHit(int x, int y) {
    var p = tile(x, y);
    if (p.x() >= 0
        && p.y() >= 0
        && p.x() < host.map().getWidth()
        && p.y() < host.map().getHeight()) {
      String decor = host.map().getDecorSpriteName(p.x(), p.y());
      if (decor != null && !decor.isBlank()) return p;
    }
    return host.decorAt(x, y);
  }

  private void finishSelection(int x, int y) {
    updatePreview();
    if (!invalid.isEmpty()) {
      host.status(invalid);
      movingSelection = false;
      return;
    }
    if (movingSelection) {
      int dx = endX - startX, dy = endY - startY;
      var edit = BuildEdits.moveDecor(host.map(), host.collisions(), selection, dx, dy);
      if (!edit.changes().isEmpty()) {
        host.commit(t("move_selection"), edit);
        var moved =
            selection.stream().map(p -> new BuildGeometry.Point(p.x() + dx, p.y() + dy)).toList();
        selection.clear();
        selection.addAll(moved);
      }
    } else {
      var found = new LinkedHashSet<BuildGeometry.Point>();
      if (startX == endX && startY == endY) {
        var hit = selectionHit(x, y);
        if (hit != null) found.add(hit);
      } else {
        for (int ty = Math.max(0, Math.min(startY, endY));
            ty <= Math.min(host.map().getHeight() - 1, Math.max(startY, endY));
            ty++)
          for (int tx = Math.max(0, Math.min(startX, endX));
              tx <= Math.min(host.map().getWidth() - 1, Math.max(startX, endX));
              tx++) {
            String decor = host.map().getDecorSpriteName(tx, ty);
            if (decor != null && !decor.isBlank()) found.add(new BuildGeometry.Point(tx, ty));
          }
      }
      if ((long) selection.size() + found.stream().filter(p -> !selection.contains(p)).count()
          > MapStamp.MAX_CELLS) {
        host.status(t("too_large"));
        return;
      }
      // Shift-click toggles an object; Shift-box adds the enclosed anchors.
      if (additiveSelection && startX == endX && startY == endY)
        for (var p : found) {
          if (!selection.remove(p)) selection.add(p);
        }
      else selection.addAll(found);
    }
    movingSelection = false;
    host.status(t("selected_count") + ": " + selection.size());
  }

  private void renderSelection(SpriteBatch batch, ShapeRenderer shapes, Matrix4 projection) {
    int dx = dragging && movingSelection ? endX - startX : 0;
    int dy = dragging && movingSelection ? endY - startY : 0;
    Color color =
        invalid.isEmpty() ? new Color(.2f, .85f, .75f, .65f) : new Color(1, .25f, .25f, .7f);
    shapes.setProjectionMatrix(projection);
    shapes.begin(ShapeRenderer.ShapeType.Line);
    shapes.setColor(color);
    for (var p : selection) shapes.rect((p.x() + dx) * 32, (p.y() + dy) * 16, 32, 16);
    if (dragging && !movingSelection)
      shapes.rect(
          Math.min(startX, endX) * 32,
          Math.min(startY, endY) * 16,
          (Math.abs(endX - startX) + 1) * 32,
          (Math.abs(endY - startY) + 1) * 16);
    shapes.end();
    batch.setProjectionMatrix(projection);
    batch.begin();
    batch.setColor(color);
    for (var p : selection) {
      var t = MapStamp.Tile.read(host.map(), host.collisions(), p.x(), p.y());
      if (t.decor() != null)
        host.preview(
            batch,
            t.decor(),
            (p.x() + dx) * 32,
            (p.y() + dy) * 16,
            t.scaleX(),
            t.scaleY(),
            t.offsetX(),
            t.offsetY());
    }
    batch.setColor(Color.WHITE);
    batch.end();
  }

  public boolean scrolled(float amount) {
    if (!visible || !overPanel(Gdx.input.getX(), Gdx.input.getY())) return false;
    scroll = Math.max(0, scroll + Math.round(amount));
    return true;
  }

  public boolean keyTyped(char c) {
    if (!typing()) return false;
    if (c >= 32 && c != 127) {
      if (nameFocus && templateName.length() < 100) templateName += c;
      else if (searchFocus && query.length() < 100) {
        query += c;
        filter();
      }
    }
    return true;
  }

  public boolean keyDown(int key) {
    if (!visible) return false;
    if (key == Input.Keys.ESCAPE) {
      if (tool == Tool.SELECT && !typing() && dropdown.isEmpty()) return false;
      searchFocus = false;
      nameFocus = false;
      dropdown = "";
      cancel();
      tool = Tool.SELECT;
      return true;
    }
    if (typing()) {
      if (key == Input.Keys.BACKSPACE) {
        if (nameFocus && !templateName.isEmpty())
          templateName = templateName.substring(0, templateName.length() - 1);
        else if (searchFocus && !query.isEmpty()) {
          query = query.substring(0, query.length() - 1);
          filter();
        }
      }
      if (key == Input.Keys.ENTER) {
        searchFocus = false;
        nameFocus = false;
      }
      return true;
    }
    boolean ctrl =
        Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    boolean shift =
        Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    if (ctrl && key == Input.Keys.Z) {
      cancel();
      if (shift) host.redo();
      else host.undo();
      return true;
    }
    if (ctrl && key == Input.Keys.Y) {
      cancel();
      host.redo();
      return true;
    }
    if (ctrl && key == Input.Keys.S) {
      host.save();
      return true;
    }
    if (tool == Tool.MULTI_SELECT) {
      // Keep legacy single-object delete/copy/nudge shortcuts out of group selection mode.
      if (key >= Input.Keys.F1 && key <= Input.Keys.F12) clearSelection();
      return key == Input.Keys.DEL
          || key == Input.Keys.FORWARD_DEL
          || key == Input.Keys.LEFT
          || key == Input.Keys.RIGHT
          || key == Input.Keys.UP
          || key == Input.Keys.DOWN
          || key == Input.Keys.R
          || (ctrl && (key == Input.Keys.C || key == Input.Keys.V));
    }
    if (!ctrl && key == Input.Keys.R) {
      cycleVariant(1);
      return true;
    }
    return dragging || paletteDrag;
  }

  private BuildGeometry.Point tile(int x, int y) {
    Vector3 p = host.world(x, y);
    return new BuildGeometry.Point((int) Math.floor(p.x / 32), (int) Math.floor(p.y / 16));
  }

  private float assetScale() {
    return selected != null && selected.width() == 1024 && selected.height() == 1024 ? .25f : 1f;
  }

  private void updatePreview() {
    invalid = "";
    validated = true;
    try {
      if (tool == Tool.MULTI_SELECT) {
        preview = List.of();
        if (movingSelection)
          BuildEdits.moveDecor(
              host.map(), host.collisions(), selection, endX - startX, endY - startY);
        else if ((long) (Math.abs(endX - startX) + 1) * (Math.abs(endY - startY) + 1)
            > MapStamp.MAX_CELLS) throw new IllegalArgumentException("editor.build.too_large");
        return;
      }
      if (tool == Tool.CAPTURE) {
        if (Math.min(startX, endX) < 0
            || Math.min(startY, endY) < 0
            || Math.max(startX, endX) >= host.map().getWidth()
            || Math.max(startY, endY) >= host.map().getHeight())
          throw new IllegalArgumentException("editor.build.outside");
        if ((long) (Math.abs(endX - startX) + 1) * (Math.abs(endY - startY) + 1)
            > MapStamp.MAX_CELLS) throw new IllegalArgumentException("editor.build.too_large");
      }
      if (tool == Tool.WALL || tool == Tool.ROOM) {
        if (wallStyle == null) {
          invalid = t("style_missing");
          preview = List.of();
          return;
        }
        preview =
            tool == Tool.WALL
                ? wallStyle.line(startX, startY, endX, endY)
                : wallStyle.room(startX, startY, endX, endY);
      } else if (selected != null && tool != Tool.TEMPLATE && tool != Tool.CAPTURE) {
        var points =
            tool == Tool.LINE
                ? BuildGeometry.line(startX, startY, endX, endY, spacing, false)
                : tool == Tool.PAINT
                    ? new ArrayList<>(painted)
                    : List.of(new BuildGeometry.Point(endX, endY));
        preview =
            points.stream()
                .map(
                    p ->
                        new BuildEdits.Placement(
                            p.x(),
                            p.y(),
                            selected.name(),
                            selected.ground(),
                            assetScale(),
                            assetScale(),
                            0,
                            0,
                            0,
                            blocking))
                .toList();
      } else preview = List.of();
      if (tool == Tool.TEMPLATE && template != null)
        BuildEdits.stamp(host.map(), host.collisions(), template, endX, endY, replace);
      else if (!preview.isEmpty())
        BuildEdits.paint(host.map(), host.collisions(), preview, replace);
    } catch (IllegalArgumentException e) {
      invalid = I18n.key(e.getMessage());
    }
  }

  private void place() {
    try {
      if (!invalid.isEmpty()) {
        host.status(invalid);
        return;
      }
      BuildEdits.Edit edit =
          tool == Tool.TEMPLATE && template != null
              ? BuildEdits.stamp(host.map(), host.collisions(), template, endX, endY, replace)
              : BuildEdits.paint(host.map(), host.collisions(), preview, replace);
      if (!edit.changes().isEmpty()) host.commit(label(tool), edit);
    } catch (Exception e) {
      host.status(t("placement_failed"));
      Gdx.app.error("BuildWorkspace", "Placement failed", e);
    }
    preview = List.of();
    painted.clear();
    validated = false;
  }

  private void capture() {
    try {
      int x = Math.min(startX, endX),
          y = Math.min(startY, endY),
          w = Math.abs(endX - startX) + 1,
          h = Math.abs(endY - startY) + 1;
      String name =
          templateName.isBlank() ? t("custom_template") + " " + x + ", " + y : templateName;
      template =
          MapStamp.capture(
              host.map(), host.collisions(), name, "custom", t("current_map"), x, y, w, h, null);
      template.saveNew(TEMPLATE_DIR.resolve("custom"));
      reloadTemplates();
      templateTab = true;
      tool = Tool.TEMPLATE;
      validated = false;
      scroll = 0;
      host.status(t("template_saved") + ": " + name);
    } catch (Exception e) {
      host.status(t("template_failed"));
      Gdx.app.error("BuildWorkspace", "Template capture failed", e);
    }
  }

  public void renderPreview(SpriteBatch batch, ShapeRenderer shapes, Matrix4 projection) {
    if (!visible) return;
    if (tool == Tool.MULTI_SELECT) {
      renderSelection(batch, shapes, projection);
      return;
    }
    int mx = Gdx.input.getX(), my = Gdx.input.getY();
    if (!dragging && tool != Tool.SELECT && !overPanel(mx, my) && my >= 48) {
      var p = tile(mx, my);
      if (p.x() != endX || p.y() != endY || !validated) {
        startX = endX = p.x();
        startY = endY = p.y();
        if (tool == Tool.PAINT) {
          painted.clear();
          painted.add(p);
        }
        updatePreview();
      }
    }
    if (tool == Tool.SELECT || (overPanel(mx, my) || my < 48) && !dragging && !paletteDrag) return;
    shapes.setProjectionMatrix(projection);
    batch.setProjectionMatrix(projection);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    Color color =
        invalid.isEmpty() ? new Color(.2f, .85f, .75f, .65f) : new Color(1f, .25f, .25f, .7f);
    shapes.begin(ShapeRenderer.ShapeType.Line);
    shapes.setColor(color);
    if (tool == Tool.CAPTURE)
      shapes.rect(
          Math.min(startX, endX) * 32,
          Math.min(startY, endY) * 16,
          (Math.abs(endX - startX) + 1) * 32,
          (Math.abs(endY - startY) + 1) * 16);
    else if (tool == Tool.TEMPLATE && template != null)
      shapes.rect(endX * 32, endY * 16, template.width() * 32, template.height() * 16);
    else for (var p : preview) shapes.rect(p.x() * 32, p.y() * 16, 32, 16);
    shapes.end();
    batch.begin();
    batch.setColor(color);
    if (tool == Tool.TEMPLATE && template != null) {
      int shown = 0;
      for (var c : template.cells())
        if (shown++ < 8192) {
          var t = c.tile();
          if (t.ground() != null)
            host.preview(batch, t.ground(), (endX + c.x()) * 32, (endY + c.y()) * 16, 1, 1, 0, 0);
          if (t.decor() != null)
            host.preview(
                batch,
                t.decor(),
                (endX + c.x()) * 32,
                (endY + c.y()) * 16,
                t.scaleX(),
                t.scaleY(),
                t.offsetX(),
                t.offsetY());
        }
    } else
      for (var p : preview)
        host.preview(
            batch,
            p.sprite(),
            p.x() * 32,
            p.y() * 16,
            p.scaleX(),
            p.scaleY(),
            p.offsetX(),
            p.offsetY());
    batch.setColor(Color.WHITE);
    batch.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }
}
