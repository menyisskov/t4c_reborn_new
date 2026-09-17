package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiClickZone;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.widget.GuiInventory;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.StorageService;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StorageScreen extends GuiScreenBase {
  private static final Color GOLD = Color.valueOf("F2B705");
  private static final float ROW_START_Y = 65f;
  private static final float ROW_HEIGHT = 14f;
  private static final float ROW_X = 20f;
  private static final float ROW_NAME_WIDTH = 260f;
  private static final float DETAIL_X = 375f;
  private static final float DETAIL_Y = 15f;
  private static final float DETAIL_WIDTH = 165f;
  private static final float DETAIL_HEIGHT = 40f;
  private static final float TRANSFER_BTN_Y = 75f;
  private static final float WITHDRAW_BTN_X = 378f;
  private static final float DEPOSIT_BTN_X = 430f;
  private static final float TRANSFER_BTN_WIDTH = 45f;
  private static final float TRANSFER_BTN_HEIGHT = 20f;
  private static final float STORAGE_GRID_X = 375f;
  private static final float STORAGE_GRID_Y = 150f;
  private static final float STORAGE_GRID_WIDTH = 165f;
  private static final float STORAGE_GRID_HEIGHT = 170f;
  private static final float DEPOSIT_X = 375f;
  private static final float DEPOSIT_Y = 345f;
  private static final float DEPOSIT_WIDTH = 165f;
  private static final float DEPOSIT_HEIGHT = 30f;
  private static final float GOLD_Y = 385f;
  private static final float INVENTORY_X = 20f;
  private static final float INVENTORY_Y = 420f;
  private static final float INVENTORY_WIDTH = 340f;
  private static final float INVENTORY_HEIGHT = 100f;

  private enum DragSource {
    STORAGE,
    PLAYER_INVENTORY
  }

  private static final class DragState {
    DragSource source;
    int index;
    String itemName;
    TextureRegion region;
    float offsetX;
    float offsetY;
    float screenX;
    float screenY;
  }

  private static final class StorageRow {
    final String itemKey;
    final int firstIndex;
    final int count;
    final Rectangle bounds;

    StorageRow(String itemKey, int firstIndex, int count, Rectangle bounds) {
      this.itemKey = itemKey;
      this.firstIndex = firstIndex;
      this.count = count;
      this.bounds = bounds;
    }
  }

  private static final class QuantityPrompt {
    String itemKey;
    int max;
    String value = "1";
    boolean gold;
    boolean withdraw;
    final Rectangle bounds = new Rectangle();
    final Rectangle acceptBounds = new Rectangle();
    final Rectangle cancelBounds = new Rectangle();
    final Rectangle minusBounds = new Rectangle();
    final Rectangle plusBounds = new Rectangle();
  }

  private final Player player;
  private final List<GuiClickZone> tabZones = new ArrayList<>();
  private final List<StorageRow> storageRows = new ArrayList<>();
  private final List<String> filteredStorageKeys = new ArrayList<>();
  private GuiInventory playerInventory;
  private GuiInventory storageInventory;
  private StorageService.Category selectedCategory;
  private String selectedItem;
  private String selectedInventoryItem;
  private String searchQuery = "";
  private boolean searchFocused = false;
  private DragState dragState;
  private QuantityPrompt prompt;
  private final Rectangle searchBoxBounds = new Rectangle();

  public StorageScreen(Player player) {
    this.player = player;
    try {
      background = SpriteLoader.getInstance().getRegionFromSpriteName("interfaceChest");
    } catch (GameException ignored) {
      background = null;
    }
    centerOnScreen();
    addCloseButton();
    addHeader();
    addCategoryTabs();
    addPlayerInventory();
    addStorageGrid();
    addTransferButtons();
    addGoldButtons();
    rebuildRows();
  }

  private void addCloseButton() {
    addCloseButton(528f, 6f);
  }

  private void addHeader() {
    if (background == null) return;
    labels.add(GuiText.translatedHeader("ui.personal_storage", "PERSONAL STORAGE", x + 60f, y + 8f));
    searchBoxBounds.set(x + 305f, y + 3f, 195f, 14f);
    labels.add(
        new GuiText(
            FontManager.getInstance().getJetBrainsMonoFont(10, Color.WHITE),
            x + 308f,
            y + 15f,
            () -> "[" + I18n.key("ui.search") + ": " + searchQuery + (searchFocused ? "_" : "") + "]"));
  }

  private void addCategoryTabs() {
    if (background == null) return;
    selectedCategory = null;
    BitmapFont font = FontManager.getInstance().getJetBrainsMonoFont(10, Color.WHITE);
    BitmapFont activeFont = FontManager.getInstance().getJetBrainsMonoFont(10, GOLD);
    float tabX = x + 18f;
    float tabY = y + 28f;
    float tabWidth = 65f;
    addTab(tabX, tabY, tabWidth, font, activeFont, null, "ui.storage_all", "ALL");
    StorageService.Category[] categories = StorageService.Category.values();
    for (int i = 0; i < categories.length; i++) {
      int position = i + 1;
      int row = position / 4;
      int col = position % 4;
      addTab(
          tabX + tabWidth * col,
          tabY + row * 16f,
          tabWidth,
          font,
          activeFont,
          categories[i],
          "ui.storage_category." + categories[i].name().toLowerCase(Locale.ROOT),
          categories[i].name());
    }
  }

  private void addTab(
      float tabX,
      float tabY,
      float tabWidth,
      BitmapFont font,
      BitmapFont activeFont,
      StorageService.Category category,
      String labelKey,
      String fallback) {
    labels.add(
        new GuiText(
            font,
            tabX,
            tabY,
            () -> selectedCategory == category ? "" : I18n.key(labelKey, fallback)));
    labels.add(
        new GuiText(
            activeFont,
            tabX,
            tabY,
            () -> selectedCategory == category ? I18n.key(labelKey, fallback) : ""));
    tabZones.add(
        new GuiClickZone(
            tabX,
            tabY - 12f,
            tabWidth,
            16f,
            () -> {
              selectedCategory = category;
              rebuildRows();
            }));
  }

  private void addPlayerInventory() {
    if (background == null || player == null) return;
    labels.add(
        new GuiText(
            FontManager.getInstance().getJetBrainsMonoFont(11, GOLD),
            x + INVENTORY_X,
            y + INVENTORY_Y - 6f,
            () -> I18n.key("ui.inventory")));
    playerInventory =
        new GuiInventory(player, x + INVENTORY_X, y + INVENTORY_Y, INVENTORY_WIDTH, INVENTORY_HEIGHT);
    inventories.add(playerInventory);
  }

  private void addStorageGrid() {
    if (background == null || player == null) return;
    labels.add(
        new GuiText(
            FontManager.getInstance().getJetBrainsMonoFont(11, GOLD),
            x + STORAGE_GRID_X,
            y + STORAGE_GRID_Y - 6f,
            () -> I18n.key("ui.storage")));
    storageInventory =
        new GuiInventory(
            () -> filteredStorageKeys,
            x + STORAGE_GRID_X,
            y + STORAGE_GRID_Y,
            STORAGE_GRID_WIDTH,
            STORAGE_GRID_HEIGHT);
    inventories.add(storageInventory);
  }

  private void addTransferButtons() {
    if (background == null || player == null) return;
    BitmapFont font = FontManager.getInstance().getJetBrainsMonoFont(9, GOLD);
    labels.add(
        new GuiText(
            font, x + WITHDRAW_BTN_X + 2f, y + TRANSFER_BTN_Y + 14f, () -> I18n.key("ui.withdraw")));
    tabZones.add(
        new GuiClickZone(
            x + WITHDRAW_BTN_X,
            y + TRANSFER_BTN_Y,
            TRANSFER_BTN_WIDTH,
            TRANSFER_BTN_HEIGHT,
            () -> {
              StorageRow row = findRow(selectedItem);
              if (row != null) openPrompt(row.itemKey, row.count, false, true);
            }));
    labels.add(
        new GuiText(
            font, x + DEPOSIT_BTN_X + 2f, y + TRANSFER_BTN_Y + 14f, () -> I18n.key("ui.deposit")));
    tabZones.add(
        new GuiClickZone(
            x + DEPOSIT_BTN_X,
            y + TRANSFER_BTN_Y,
            TRANSFER_BTN_WIDTH,
            TRANSFER_BTN_HEIGHT,
            () -> {
              if (selectedInventoryItem != null) {
                int count = 0;
                for (String key : player.getInventory())
                  if (selectedInventoryItem.equals(key)) count++;
                if (count > 0) openPrompt(selectedInventoryItem, count, false, false);
              }
            }));
  }

  private void openPrompt(String itemKey, int max, boolean gold, boolean withdraw) {
    QuantityPrompt p = new QuantityPrompt();
    p.itemKey = itemKey;
    p.max = Math.max(1, max);
    p.gold = gold;
    p.withdraw = withdraw;
    p.bounds.set(x + 160f, y + 140f, 260f, 110f);
    p.minusBounds.set(p.bounds.x + 20f, p.bounds.y + 50f, 20f, 20f);
    p.plusBounds.set(p.bounds.x + 160f, p.bounds.y + 50f, 20f, 20f);
    p.acceptBounds.set(p.bounds.x + 30f, p.bounds.y + 10f, 90f, 20f);
    p.cancelBounds.set(p.bounds.x + 140f, p.bounds.y + 10f, 90f, 20f);
    prompt = p;
  }

  private void addGoldButtons() {
    if (background == null || player == null) return;
    BitmapFont font = FontManager.getInstance().getJetBrainsMonoFont(11, Color.WHITE);
    labels.add(
        new GuiText(
            font,
            x + DEPOSIT_X,
            y + GOLD_Y,
            () -> "Gold: " + player.getGold() + "  Stored: " + player.getStorageGold()));
    tabZones.add(
        new GuiClickZone(
            x + DEPOSIT_X,
            y + GOLD_Y + 6f,
            85f,
            16f,
            () -> {
              if (player.getGold() > 0) openPrompt(null, player.getGold(), true, false);
            }));
    labels.add(
        new GuiText(
            font, x + DEPOSIT_X, y + GOLD_Y + 18f, () -> "[" + I18n.key("ui.deposit_gold") + "]"));
    tabZones.add(
        new GuiClickZone(
            x + DEPOSIT_X + 90f,
            y + GOLD_Y + 6f,
            90f,
            16f,
            () -> {
              if (player.getStorageGold() > 0) openPrompt(null, player.getStorageGold(), true, true);
            }));
    labels.add(
        new GuiText(
            font,
            x + DEPOSIT_X + 90f,
            y + GOLD_Y + 18f,
            () -> "[" + I18n.key("ui.withdraw_gold") + "]"));
  }

  private void rebuildRows() {
    storageRows.clear();
    filteredStorageKeys.clear();
    if (player == null) return;
    Map<String, int[]> grouped = new LinkedHashMap<>();
    List<String> storage = player.getStorage();
    for (int i = 0; i < storage.size(); i++) {
      String key = storage.get(i);
      if (key == null) continue;
      if (selectedCategory != null && StorageService.categoryOf(key) != selectedCategory) continue;
      String displayName = I18n.resolve(resolveName(key));
      if (!searchQuery.isBlank()
          && !displayName.toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT))) {
        continue;
      }
      filteredStorageKeys.add(key);
      int[] v = grouped.get(key);
      if (v == null) {
        v = new int[] {i, 0};
        grouped.put(key, v);
      }
      v[1]++;
    }
    List<Map.Entry<String, int[]>> entries = new ArrayList<>(grouped.entrySet());
    entries.sort(
        (a, b) ->
            I18n.resolve(resolveName(a.getKey()))
                .compareToIgnoreCase(I18n.resolve(resolveName(b.getKey()))));
    float rowY = y + ROW_START_Y;
    for (Map.Entry<String, int[]> entry : entries) {
      storageRows.add(
          new StorageRow(
              entry.getKey(),
              entry.getValue()[0],
              entry.getValue()[1],
              new Rectangle(x + ROW_X, rowY - 12f, ROW_NAME_WIDTH, ROW_HEIGHT)));
      rowY += ROW_HEIGHT;
    }
  }

  private static String resolveName(String itemKey) {
    ItemDefinition def = ItemDefinition.get(itemKey);
    return def != null && def.getName() != null ? def.getName() : itemKey;
  }

  @Override
  public void render(SpriteBatch batch) {
    super.render(batch);
    renderRows(batch);
    renderDetailPanel(batch);
    renderDepositZone(batch);
    renderDragItem(batch);
    renderPrompt(batch);
  }

  private void renderPrompt(SpriteBatch batch) {
    if (prompt == null) return;
    BitmapFont header = FontManager.getInstance().getJetBrainsMonoFont(12, GOLD);
    BitmapFont font = FontManager.getInstance().getJetBrainsMonoFont(11, Color.WHITE);
    String label =
        prompt.gold
            ? I18n.key("ui.gold")
            : I18n.resolve(resolveName(prompt.itemKey));
    header.draw(batch, I18n.key("ui.how_many"), prompt.bounds.x + 20f, prompt.bounds.y + prompt.bounds.height - 10f);
    font.draw(batch, label + " (" + I18n.key("ui.max") + " " + prompt.max + ")", prompt.bounds.x + 20f, prompt.bounds.y + prompt.bounds.height - 30f);
    font.draw(batch, "[-]", prompt.minusBounds.x, prompt.minusBounds.y + 14f);
    font.draw(batch, prompt.value, prompt.minusBounds.x + 30f, prompt.minusBounds.y + 14f);
    font.draw(batch, "[+]", prompt.plusBounds.x, prompt.plusBounds.y + 14f);
    font.draw(batch, "[" + I18n.key("ui.accept") + "]", prompt.acceptBounds.x, prompt.acceptBounds.y + 14f);
    font.draw(batch, "[" + I18n.key("ui.cancel") + "]", prompt.cancelBounds.x, prompt.cancelBounds.y + 14f);
  }

  private void renderRows(SpriteBatch batch) {
    if (background == null) return;
    BitmapFont font = FontManager.getInstance().getJetBrainsMonoFont(11, Color.WHITE);
    BitmapFont selectedFont = FontManager.getInstance().getJetBrainsMonoFont(11, GOLD);
    for (StorageRow row : storageRows) {
      String name = I18n.resolve(resolveName(row.itemKey));
      String text = name + (row.count > 1 ? " x" + row.count : " x1");
      BitmapFont rowFont = row.itemKey.equals(selectedItem) ? selectedFont : font;
      rowFont.draw(batch, text, row.bounds.x, row.bounds.y + row.bounds.height - 2f);
    }
  }

  private void renderDetailPanel(SpriteBatch batch) {
    if (background == null) return;
    BitmapFont font = FontManager.getInstance().getJetBrainsMonoFont(9, Color.WHITE);
    StorageRow selected = findRow(selectedItem);
    if (selected == null) return;
    ItemDefinition def = ItemDefinition.get(selected.itemKey);
    String name = I18n.resolve(resolveName(selected.itemKey));
    font.draw(batch, name + " x" + selected.count, x + DETAIL_X, y + DETAIL_Y + 15f);
    if (def != null) {
      font.draw(
          batch,
          I18n.key("ui.price") + ": " + def.getPrice() + "  " + I18n.key("tooltip.weight") + ": " + def.getWeight(),
          x + DETAIL_X,
          y + DETAIL_Y + 30f);
    }
  }

  private void renderDepositZone(SpriteBatch batch) {
    if (background == null) return;
    BitmapFont font = FontManager.getInstance().getJetBrainsMonoFont(10, GOLD);
    font.draw(
        batch,
        "[ " + I18n.key("ui.deposit_zone") + " ]",
        x + DEPOSIT_X,
        y + DEPOSIT_Y + DEPOSIT_HEIGHT);
  }

  private void renderDragItem(SpriteBatch batch) {
    if (dragState == null || dragState.region == null) return;
    GuiDraw.drawRegionFlipped(
        batch, dragState.region, dragState.screenX - dragState.offsetX, dragState.screenY - dragState.offsetY);
  }

  private StorageRow findRow(String itemKey) {
    if (itemKey == null) return null;
    for (StorageRow row : storageRows) if (row.itemKey.equals(itemKey)) return row;
    return null;
  }

  private static TextureRegion resolveRegion(ItemDefinition def) {
    String sprite = def == null ? null : def.getAppearanceInventory();
    if (sprite == null || sprite.isEmpty()) return null;
    try {
      return SpriteLoader.getInstance().getRegionFromSpriteName(sprite);
    } catch (GameException ignored) {
      return null;
    }
  }

  @Override
  public void onTouchDown(float screenX, float screenY) {
    if (prompt != null) {
      handlePromptTouch(screenX, screenY);
      return;
    }
    if (searchBoxBounds.contains(screenX, screenY)) {
      searchFocused = true;
      return;
    }
    searchFocused = false;
    for (GuiClickZone zone : new ArrayList<>(tabZones)) {
      if (zone.contains(screenX, screenY)) {
        zone.run();
        return;
      }
    }
    for (StorageRow row : storageRows) {
      if (row.bounds.contains(screenX, screenY)) {
        selectedItem = row.itemKey;
        dragState = startStorageDrag(row, screenX, screenY);
        return;
      }
    }
    if (storageInventory != null) {
      GuiInventory.ItemHit hit = storageInventory.hitTest(screenX, screenY);
      if (hit != null) {
        selectedItem = hit.getItemName();
        DragState state = new DragState();
        state.source = DragSource.STORAGE;
        state.index = player.getStorage().indexOf(hit.getItemName());
        state.itemName = hit.getItemName();
        state.region = hit.getRegion();
        state.offsetX = screenX - hit.getX();
        state.offsetY = screenY - hit.getY();
        state.screenX = screenX;
        state.screenY = screenY;
        dragState = state;
        return;
      }
    }
    if (playerInventory != null) {
      GuiInventory.ItemHit hit = playerInventory.hitTest(screenX, screenY);
      if (hit != null) {
        selectedInventoryItem = hit.getItemName();
        DragState state = new DragState();
        state.source = DragSource.PLAYER_INVENTORY;
        state.index = hit.getIndex();
        state.itemName = hit.getItemName();
        state.region = hit.getRegion();
        state.offsetX = screenX - hit.getX();
        state.offsetY = screenY - hit.getY();
        state.screenX = screenX;
        state.screenY = screenY;
        dragState = state;
        return;
      }
    }
    super.onTouchDown(screenX, screenY);
  }

  private void handlePromptTouch(float screenX, float screenY) {
    if (prompt.acceptBounds.contains(screenX, screenY)) {
      applyPrompt();
      prompt = null;
      return;
    }
    if (prompt.cancelBounds.contains(screenX, screenY)) {
      prompt = null;
      return;
    }
    if (prompt.minusBounds.contains(screenX, screenY)) {
      setPromptValue(Math.max(1, parsePromptValue() - 1));
      return;
    }
    if (prompt.plusBounds.contains(screenX, screenY)) {
      setPromptValue(Math.min(prompt.max, parsePromptValue() + 1));
      return;
    }
  }

  private int parsePromptValue() {
    try {
      return Integer.parseInt(prompt.value);
    } catch (NumberFormatException e) {
      return 1;
    }
  }

  private void setPromptValue(int value) {
    prompt.value = String.valueOf(Math.max(1, Math.min(prompt.max, value)));
  }

  private void applyPrompt() {
    if (player == null) return;
    int amount = Math.max(1, Math.min(prompt.max, parsePromptValue()));
    if (prompt.gold) {
      if (prompt.withdraw) StorageService.withdrawGold(player, amount);
      else StorageService.depositGold(player, amount);
    } else if (prompt.withdraw) {
      StorageService.withdrawMany(player, prompt.itemKey, amount);
    } else {
      StorageService.depositMany(player, prompt.itemKey, amount);
    }
    PlayerStateStore.save(player);
    rebuildRows();
  }

  private DragState startStorageDrag(StorageRow row, float screenX, float screenY) {
    DragState state = new DragState();
    state.source = DragSource.STORAGE;
    state.index = row.firstIndex;
    state.itemName = row.itemKey;
    state.region = resolveRegion(ItemDefinition.get(row.itemKey));
    state.offsetX = 8f;
    state.offsetY = 8f;
    state.screenX = screenX;
    state.screenY = screenY;
    return state;
  }

  @Override
  public void onMouseMove(float screenX, float screenY) {
    if (dragState != null) {
      dragState.screenX = screenX;
      dragState.screenY = screenY;
      return;
    }
    super.onMouseMove(screenX, screenY);
  }

  @Override
  public void onTouchUp(float screenX, float screenY) {
    if (dragState != null) {
      handleDrop(screenX, screenY, dragState);
      dragState = null;
      return;
    }
    super.onTouchUp(screenX, screenY);
  }

  private void handleDrop(float screenX, float screenY, DragState drag) {
    if (player == null) return;
    Rectangle depositBounds = new Rectangle(x + DEPOSIT_X, y + DEPOSIT_Y, DEPOSIT_WIDTH, DEPOSIT_HEIGHT);
    Rectangle storageGridBounds =
        new Rectangle(x + STORAGE_GRID_X, y + STORAGE_GRID_Y, STORAGE_GRID_WIDTH, STORAGE_GRID_HEIGHT);
    if (drag.source == DragSource.PLAYER_INVENTORY
        && (depositBounds.contains(screenX, screenY) || storageGridBounds.contains(screenX, screenY))) {
      StorageService.deposit(player, drag.index, drag.itemName);
      PlayerStateStore.save(player);
      rebuildRows();
      return;
    }
    if (drag.source == DragSource.STORAGE && playerInventory != null) {
      Rectangle invBounds =
          new Rectangle(x + INVENTORY_X, y + INVENTORY_Y, INVENTORY_WIDTH, INVENTORY_HEIGHT);
      if (invBounds.contains(screenX, screenY)) {
        StorageService.withdraw(player, drag.index, drag.itemName);
        PlayerStateStore.save(player);
        rebuildRows();
      }
    }
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (prompt != null) {
      if (keycode == Input.Keys.ESCAPE) {
        prompt = null;
        return true;
      }
      if (keycode == Input.Keys.ENTER) {
        applyPrompt();
        prompt = null;
        return true;
      }
      if (keycode == Input.Keys.BACKSPACE) {
        if (prompt.value.length() > 1) prompt.value = prompt.value.substring(0, prompt.value.length() - 1);
        else prompt.value = "0";
        return true;
      }
      return true;
    }
    if (keycode == Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    if (searchFocused && keycode == Input.Keys.BACKSPACE) {
      if (!searchQuery.isEmpty()) {
        searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
        rebuildRows();
      }
      return true;
    }
    return super.onKeyDown(keycode);
  }

  @Override
  public boolean onKeyTyped(char character) {
    if (prompt != null) {
      if (Character.isDigit(character)) {
        String candidate = (prompt.value.equals("0") ? "" : prompt.value) + character;
        if (candidate.length() <= 6) {
          try {
            setPromptValue(Integer.parseInt(candidate));
          } catch (NumberFormatException ignored) {
          }
        }
      }
      return true;
    }
    if (!searchFocused) return false;
    if (Character.isLetterOrDigit(character) || character == ' ') {
      if (searchQuery.length() < 24) {
        searchQuery += character;
        rebuildRows();
      }
      return true;
    }
    return false;
  }
}
