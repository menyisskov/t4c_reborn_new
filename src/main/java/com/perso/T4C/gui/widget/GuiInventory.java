package com.perso.T4C.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemDurabilityService;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
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
  private final BitmapFont durabilityFont;

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

  public GuiInventory(Supplier<List<String>> itemsSupplier, float x, float y, float width, float height) {
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
    this.durabilityFont = FontManager.getInstance().getJetBrainsMonoFont(10, Color.WHITE);
  }

  @Override
  public void render(SpriteBatch batch) {
    clipAndDrawItems(batch);
  }

  public ItemHit hitTest(float screenX, float screenY) {
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
        ItemDefinition definition = ItemDefinition.get(itemName);
        if (player != null && ItemDurabilityService.isRepairable(definition)) {
          double durability = ItemDurabilityService.inventory(player, stack.firstIndex);
          durabilityFont.setColor(
              durability >= 50 ? Color.GREEN : durability >= 25 ? Color.ORANGE : Color.RED);
          durabilityFont.draw(
              batch,
              ItemDurabilityService.format(durability) + "%",
              cursorX + 1f,
              cursorY + h - 1f);
        }
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
    if (player != null) ItemDurabilityService.synchronize(player);
    if (items != null)
      for (int i = 0; i < items.size(); i++) {
        String key = items.get(i);
        if (key == null) continue;
        ItemDefinition definition = ItemDefinition.get(key);
        String groupKey =
            player != null && ItemDurabilityService.isRepairable(definition)
                ? key + "\u0000" + ItemDurabilityService.inventory(player, i)
                : key;
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
