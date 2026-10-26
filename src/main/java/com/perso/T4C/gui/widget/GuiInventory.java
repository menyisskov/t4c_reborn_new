package com.perso.T4C.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.Player;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class GuiInventory extends AbstractGuiElement {
  private static final float PADDING = 4f;
  private static final float CELL_PADDING = 4f;
  private static final float RESIZE_HANDLE_SIZE = 10f;
  private final Player player;
  private final Supplier<List<String>> itemsSupplier;
  private float width;
  private float height;
  private float scrollOffset;
  private int gridCellSize;
  private com.perso.T4C.gui.core.JournalTheme gridTheme;

  /** Fixed cells are opt-in; shops and storage retain their natural-size item layout. */
  public GuiInventory withGrid(int cellSize, com.perso.T4C.gui.core.JournalTheme theme) {
    gridCellSize = Math.max(24, cellSize);
    gridTheme = theme;
    return this;
  }

  @Getter
  @AllArgsConstructor
  public static final class ItemHit {
    private final int index;
    private final String itemName;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final TextureRegion region;
  }

  public GuiInventory(Player player, float x, float y, float width, float height) {
    this(player, player == null ? List::of : player::getInventory, x, y, width, height);
  }

  public GuiInventory(
      Supplier<List<String>> itemsSupplier, float x, float y, float width, float height) {
    this(null, itemsSupplier, x, y, width, height);
  }

  private GuiInventory(
      Player player,
      Supplier<List<String>> itemsSupplier,
      float x,
      float y,
      float width,
      float height) {
    super(x, y);
    this.player = player;
    this.itemsSupplier = itemsSupplier;
    this.width = width;
    this.height = height;
  }

  @Override
  public void render(SpriteBatch batch) {
    clipAndDrawItems(batch);
  }

  public ItemHit hitTest(float screenX, float screenY) {
    if (gridCellSize > 0) {
      if (!contains(screenX, screenY)) return null;
      List<String> items = itemsSupplier.get();
      if (items == null) return null;
      List<StackEntry> entries = stacks(items);
      int columns = gridColumns();
      int col = (int) Math.floor((screenX - x - PADDING) / gridCellSize);
      int row = (int) Math.floor((screenY - y - PADDING + scrollOffset) / gridCellSize);
      if (col < 0 || col >= columns || row < 0) return null;
      int index = row * columns + col;
      if (index >= entries.size()) return null;
      StackEntry entry = entries.get(index);
      TextureRegion region = resolveInventoryRegion(entry.itemName);
      return region == null
          ? null
          : new ItemHit(
              entry.firstIndex,
              entry.itemName,
              x + PADDING + col * gridCellSize,
              y + PADDING + row * gridCellSize - scrollOffset,
              gridCellSize,
              gridCellSize,
              region);
    }
    List<String> items = itemsSupplier.get();
    if (items == null || items.isEmpty()) {
      return null;
    }
    float innerX = x + PADDING;
    float innerY = y + PADDING;
    float innerW = width - 2f * PADDING;
    float innerH = height - 2f * PADDING;
    if (innerW <= 0f || innerH <= 0f) {
      return null;
    }
    if (screenX < innerX
        || screenX > innerX + innerW
        || screenY < innerY
        || screenY > innerY + innerH) {
      return null;
    }
    float cursorX = innerX;
    float cursorY = innerY - scrollOffset;
    float rowHeight = 0f;
    for (StackEntry stack : stacks(items)) {
      int i = stack.firstIndex;
      String itemName = stack.itemName;
      TextureRegion region = resolveInventoryRegion(itemName);
      if (region == null) {
        continue;
      }
      float w = region.getRegionWidth();
      float h = region.getRegionHeight();
      if (cursorX + w > innerX + innerW) {
        cursorX = innerX;
        cursorY += rowHeight + CELL_PADDING;
        rowHeight = 0f;
      }
      if (screenX >= cursorX
          && screenX <= cursorX + w
          && screenY >= cursorY
          && screenY <= cursorY + h) {
        return new ItemHit(i, itemName, cursorX, cursorY, w, h, region);
      }
      cursorX += w + CELL_PADDING;
      rowHeight = Math.max(rowHeight, h);
    }
    return null;
  }

  public void onScroll(float amountY) {
    float maxScroll = Math.max(0f, getContentHeight() - (height - 2f * PADDING));
    scrollOffset = Math.min(Math.max(0f, scrollOffset + amountY * 24f), maxScroll);
  }

  @Override
  public float getWidth() {
    return width;
  }

  @Override
  public float getHeight() {
    return height;
  }

  public boolean isOnResizeHandle(float screenX, float screenY) {
    return screenX >= x + width - RESIZE_HANDLE_SIZE
        && screenX <= x + width
        && screenY >= y + height - RESIZE_HANDLE_SIZE
        && screenY <= y + height;
  }

  public void setSize(float width, float height) {
    this.width = width;
    this.height = height;
    float maxScroll = Math.max(0f, getContentHeight() - (height - 2f * PADDING));
    scrollOffset = Math.min(scrollOffset, maxScroll);
  }

  private void clipAndDrawItems(SpriteBatch batch) {
    if (width <= 0f || height <= 0f) {
      return;
    }
    batch.flush();
    Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
    Gdx.gl.glScissor(
        Math.round(x),
        Math.round(Gdx.graphics.getHeight() - (y + height)),
        Math.round(width),
        Math.round(height));
    drawItems(batch);
    batch.flush();
    Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
  }

  private void drawItems(SpriteBatch batch) {
    if (gridCellSize > 0) {
      drawGrid(batch);
      return;
    }
    List<String> items = itemsSupplier.get();
    if (items == null || items.isEmpty()) {
      return;
    }
    float innerX = x + PADDING;
    float innerY = y + PADDING;
    float innerW = width - 2f * PADDING;
    float innerH = height - 2f * PADDING;
    if (innerW <= 0f || innerH <= 0f) {
      return;
    }
    float cursorX = innerX;
    float cursorY = innerY - scrollOffset;
    float rowHeight = 0f;
    for (StackEntry stack : stacks(items)) {
      String itemName = stack.itemName;
      TextureRegion region = resolveInventoryRegion(itemName);
      if (region == null) {
        continue;
      }
      float w = region.getRegionWidth();
      float h = region.getRegionHeight();
      if (cursorX + w > innerX + innerW) {
        cursorX = innerX;
        cursorY += rowHeight + CELL_PADDING;
        rowHeight = 0f;
      }
      if (cursorY + h < innerY || cursorY > innerY + innerH) {
      } else {
        GuiDraw.drawRegionFlipped(batch, region, cursorX, cursorY, w, h);
      }
      cursorX += w + CELL_PADDING;
      rowHeight = Math.max(rowHeight, h);
    }
  }

  private TextureRegion resolveInventoryRegion(String itemName) {
    if (itemName == null || itemName.isEmpty()) {
      return null;
    }
    ItemDefinition def = ItemDefinition.get(itemName);
    String sprite = def == null ? itemName : def.getAppearanceInventory();
    if (sprite == null || sprite.isEmpty()) {
      return null;
    }
    try {
      return SpriteLoader.getInstance().getRegionFromSpriteName(sprite);
    } catch (GameException ignored) {
      return null;
    }
  }

  private float getContentHeight() {
    List<String> items = itemsSupplier.get();
    if (items == null) {
      return 0f;
    }
    if (gridCellSize > 0) {
      return (float) Math.ceil(stacks(items).size() / (double) gridColumns()) * gridCellSize;
    }
    float innerW = width - 2f * PADDING;
    if (innerW <= 0f) {
      return 0f;
    }
    float cursorX = 0f;
    float totalHeight = 0f;
    float rowHeight = 0f;
    for (StackEntry stack : stacks(items)) {
      String itemName = stack.itemName;
      TextureRegion region = resolveInventoryRegion(itemName);
      if (region == null) {
        continue;
      }
      float w = region.getRegionWidth();
      float h = region.getRegionHeight();
      if (cursorX + w > innerW && cursorX > 0f) {
        totalHeight += rowHeight + CELL_PADDING;
        cursorX = 0f;
        rowHeight = 0f;
      }
      cursorX += w + CELL_PADDING;
      rowHeight = Math.max(rowHeight, h);
    }
    totalHeight += rowHeight;
    return totalHeight;
  }

  private int gridColumns() {
    return Math.max(1, (int) ((width - PADDING * 2) / gridCellSize));
  }

  private void drawGrid(SpriteBatch batch) {
    List<String> items = itemsSupplier.get();
    List<StackEntry> entries = items == null ? List.of() : stacks(items);
    int columns = gridColumns();
    // Re-clamp after items are removed, so the remaining bag cannot be scrolled out of view.
    float contentHeight = (float) Math.ceil(entries.size() / (double) columns) * gridCellSize;
    scrollOffset = Math.min(scrollOffset, Math.max(0, contentHeight - height + PADDING * 2));
    int rows =
        Math.max(
            (int) Math.ceil((height - PADDING * 2 + scrollOffset) / gridCellSize),
            (entries.size() + columns - 1) / columns);
    for (int row = 0; row < rows; row++) {
      float cy = y + PADDING + row * gridCellSize - scrollOffset;
      if (cy + gridCellSize < y || cy > y + height) continue;
      for (int col = 0; col < columns; col++) {
        float cx = x + PADDING + col * gridCellSize;
        gridTheme.well(batch, cx, cy, gridCellSize - 2, gridCellSize - 2);
        int index = row * columns + col;
        if (index >= entries.size()) continue;
        StackEntry entry = entries.get(index);
        TextureRegion icon = resolveInventoryRegion(entry.itemName);
        if (icon == null) continue;
        float scale =
            Math.min(
                1f,
                Math.min(
                    (gridCellSize - 8f) / icon.getRegionWidth(),
                    (gridCellSize - 8f) / icon.getRegionHeight()));
        float w = icon.getRegionWidth() * scale;
        float h = icon.getRegionHeight() * scale;
        GuiDraw.drawRegionFlipped(
            batch, icon, cx + (gridCellSize - w) / 2, cy + (gridCellSize - h) / 2, w, h);
        if (entry.count > 1) {
          var font =
              com.perso
                  .T4C
                  .ui
                  .FontManager
                  .getInstance()
                  .getTahomaFont(10, com.badlogic.gdx.graphics.Color.valueOf("E6DECA"), true);
          font.draw(batch, String.valueOf(entry.count), cx + 3, cy + gridCellSize - 13);
        }
      }
    }
  }

  private static final class StackEntry {
    final String itemName;
    final int firstIndex;
    final int count;

    StackEntry(String itemName, int firstIndex, int count) {
      this.itemName = itemName;
      this.firstIndex = firstIndex;
      this.count = count;
    }
  }

  private List<StackEntry> stacks(List<String> items) {
    Map<String, int[]> grouped = new LinkedHashMap<>();
    if (items != null)
      for (int i = 0; i < items.size(); i++) {
        String key = items.get(i);
        if (key == null) continue;
        String groupKey = key;
        int[] v = grouped.get(groupKey);
        if (v == null) {
          v = new int[] {i, 0};
          grouped.put(groupKey, v);
        }
        v[1]++;
      }
    List<StackEntry> result = new java.util.ArrayList<>();
    grouped.forEach((groupKey, v) -> result.add(new StackEntry(items.get(v[0]), v[0], v[1])));
    return result;
  }
}
