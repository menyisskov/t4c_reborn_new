package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.ItemTooltipText;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.AccountStorageBackend;
import com.perso.T4C.item.CharacterStorageBackend;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemDurabilityService;
import com.perso.T4C.item.StorageBackend;
import com.perso.T4C.item.StorageService;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.HudTooltip;
import com.perso.T4C.ui.SystemMessage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Personal storage chest: the stash on the left, the backpack on the right, both as item grids in
 * the style of the original trade window. Items move by drag and drop, right-click (one),
 * shift-click (the whole stack) or ctrl-click (choose an amount).
 */
public class StorageScreen extends GuiScreenBase {
  private static final Color GOLD = Color.valueOf("F2B705");
  private static final Color DIM = Color.valueOf("A09880");
  private static final Color DAMAGED = Color.valueOf("FF9A3C");

  static final float WIDTH = 600f;
  static final float HEIGHT = 456f;
  static final float CELL = 29f;
  static final int STASH_COLS = 10;
  static final int PACK_COLS = 6;
  static final int ROWS = 7;

  // Pieces cut from the original trade window art (GUI_BackTrade, 552x260).
  private static final int[] SRC_TOP_BAR = {30, 0, 180, 24};
  private static final int[] SRC_TITLE_PILL = {222, 0, 106, 24};
  private static final int[] SRC_BORDER_LEFT = {0, 100, 6, 60};
  private static final int[] SRC_BORDER_RIGHT = {546, 100, 6, 60};
  private static final int[] SRC_BORDER_BOTTOM = {30, 1, 180, 5};
  private static final int[] SRC_STONE = {478, 96, 64, 150};
  private static final int[] SRC_PILL = {25, 45, 173, 23};
  private static final float GRID_LEFT = 13f;
  private static final float GRID_TOP = 11f;
  private static final float GRID_RIGHT = 37f;
  private static final float GRID_BOTTOM = 12f;
  private static final int[] SRC_GRID_TL = {11, 68, 13, 11};
  private static final int[] SRC_GRID_TOP = {24, 68, 29, 11};
  private static final int[] SRC_GRID_TR = {197, 68, 37, 11};
  private static final int[] SRC_GRID_LEFT = {11, 79, 13, 29};
  private static final int[] SRC_GRID_CELL = {24, 79, 29, 29};
  private static final int[] SRC_GRID_SCROLL = {197, 79, 37, 115};
  private static final int[] SRC_GRID_BL = {11, 194, 13, 12};
  private static final int[] SRC_GRID_BOTTOM = {24, 194, 29, 12};
  private static final int[] SRC_GRID_BR = {197, 194, 37, 12};

  private static final float MARGIN = 12f;
  private static final float TITLE_H = 24f;
  private static final float SOURCE_Y = 30f;
  private static final float PILL_Y = 56f;
  private static final float TABS_Y = 84f;
  private static final float GRID_Y = 110f;
  private static final float CONTROLS_Y = 348f;
  private static final float GOLD_Y = 386f;
  private static final float HINT_Y = 430f;
  private static final float BUTTON_H = 26f;
  private static final long DOUBLE_CLICK_MS = 300L;

  private enum Pane {
    STASH,
    PACK
  }

  /** One grid cell: an item key, how many copies it holds, and where the first one lives. */
  static final class Stack {
    final String itemKey;
    final int firstIndex;
    final int count;
    final double durability;

    Stack(String itemKey, int firstIndex, int count, double durability) {
      this.itemKey = itemKey;
      this.firstIndex = firstIndex;
      this.count = count;
      this.durability = durability;
    }
  }

  private static final class Button {
    final Rectangle bounds = new Rectangle();
    final java.util.function.Supplier<String> label;
    final Runnable action;
    final java.util.function.BooleanSupplier enabled;

    Button(
        float bx,
        float by,
        float bw,
        float bh,
        java.util.function.Supplier<String> label,
        java.util.function.BooleanSupplier enabled,
        Runnable action) {
      bounds.set(bx, by, bw, bh);
      this.label = label;
      this.enabled = enabled;
      this.action = action;
    }
  }

  private static final class DragState {
    Pane source;
    Stack stack;
    TextureRegion region;
    float startX;
    float startY;
    float screenX;
    float screenY;
    boolean moved;
  }

  private static final class QuantityPrompt {
    String itemKey;
    // The exact stack clicked (item key + durability), so a damaged stack never pulls copies
    // from a pristine stack of the same item.
    Stack stack;
    boolean gold;
    boolean withdraw;
    int max;
    String value;
    final Rectangle bounds = new Rectangle();
    final List<Button> buttons = new ArrayList<>();
  }

  private final Player player;
  private StorageBackend backend;
  private final Rectangle sourceToggleBounds = new Rectangle();
  private final TextureRegion trade;
  private final TextureRegion popup;
  private final TextureRegion cellHighlight;
  private final TextureRegion scrollTick;
  private final TextureRegion[] button = new TextureRegion[3];
  private final TextureRegion[] tabButton = new TextureRegion[3];
  private final TextureRegion scratch = new TextureRegion();
  private final HudTooltip tooltip = new HudTooltip();
  private final GlyphLayout layout = new GlyphLayout();
  private final List<Button> controls = new ArrayList<>();
  private final List<StorageService.Category> tabOrder = new ArrayList<>();
  private final List<Rectangle> tabBounds = new ArrayList<>();
  private final Rectangle stashCells = new Rectangle();
  private final Rectangle packCells = new Rectangle();
  private final Rectangle stashScroll = new Rectangle();
  private final Rectangle packScroll = new Rectangle();
  private final Rectangle searchBounds = new Rectangle();
  private List<Stack> stashStacks = new ArrayList<>();
  private List<Stack> packStacks = new ArrayList<>();
  private StorageService.Category selectedCategory;
  private String searchQuery = "";
  private boolean searchFocused;
  private int stashScrollRow;
  private int packScrollRow;
  private float mouseX = -1f;
  private float mouseY = -1f;
  private Pane selectedPane;
  private String selectedKey;
  private long lastClickMs;
  private String lastClickKey;
  private DragState drag;
  private QuantityPrompt prompt;

  public StorageScreen(Player player) {
    this.player = player;
    this.backend = new CharacterStorageBackend(player);
    this.trade = load("GUI_BackTrade");
    this.popup = GuiSprites.load("GUI_PopupBack");
    this.cellHighlight = load("GUI_BackInvGridSelect");
    this.scrollTick = load("GUI_ScrollTick");
    button[0] = GuiSprites.load("GUI_ButtonUp");
    button[1] = GuiSprites.load("GUI_ButtonHUp");
    button[2] = GuiSprites.load("GUI_ButtonDown");
    tabButton[0] = GuiSprites.load("GUI_ButtonTUp");
    tabButton[1] = GuiSprites.load("GUI_ButtonTHUp");
    tabButton[2] = GuiSprites.load("GUI_ButtonTDown");
    x = Math.round((Gdx.graphics.getWidth() - WIDTH) / 2f);
    y = Math.round((Gdx.graphics.getHeight() - HEIGHT) / 2f);
    layoutPanes();
    addCloseButton();
    addTabs();
    addControls();
    rebuild();
  }

  private static TextureRegion load(String sprite) {
    try {
      return SpriteLoader.getInstance().getRegionFromSpriteName(sprite);
    } catch (GameException e) {
      return null;
    }
  }

  // ---------------------------------------------------------------- layout

  private void layoutPanes() {
    float stashX = x + MARGIN;
    float packX = stashX + gridBoxWidth(STASH_COLS) + 10f;
    stashCells.set(
        stashX + GRID_LEFT, y + GRID_Y + GRID_TOP, STASH_COLS * CELL, ROWS * CELL);
    packCells.set(packX + GRID_LEFT, y + GRID_Y + GRID_TOP, PACK_COLS * CELL, ROWS * CELL);
    stashScroll.set(stashCells.x + stashCells.width, stashCells.y, GRID_RIGHT, stashCells.height);
    packScroll.set(packCells.x + packCells.width, packCells.y, GRID_RIGHT, packCells.height);
    sourceToggleBounds.set(x + MARGIN, y + SOURCE_Y, gridBoxWidth(STASH_COLS), 22f);
  }

  /** Swaps the stash pane between the character's own storage and the shared account vault. */
  private void toggleSource() {
    backend = backend instanceof AccountStorageBackend
        ? new CharacterStorageBackend(player)
        : new AccountStorageBackend(player);
    selectedCategory = null;
    selectedKey = null;
    stashScrollRow = 0;
    rebuild();
  }

  private static float gridBoxWidth(int cols) {
    return GRID_LEFT + cols * CELL + GRID_RIGHT;
  }

  private float packBoxX() {
    return packCells.x - GRID_LEFT;
  }

  private void addCloseButton() {
    TextureRegion normal = GuiSprites.load("GUI_X_ButtonDown");
    TextureRegion hover = GuiSprites.load("GUI_X_ButtonHUp");
    TextureRegion pressed = GuiSprites.load("GUI_X_ButtonUp");
    if (normal == null || hover == null || pressed == null) return;
    buttons.add(new GuiButton(normal, hover, pressed, x + WIDTH - 28f, y, GuiManager::close));
  }

  private void addTabs() {
    tabOrder.add(null);
    for (StorageService.Category category : StorageService.Category.values()) tabOrder.add(category);
    // Each tab gets the width of its label plus equal padding, so long names never clip.
    BitmapFont font = FontManager.getInstance().getTahomaFont(11, Color.BLACK, true);
    float[] widths = new float[tabOrder.size()];
    float textTotal = 0f;
    for (int i = 0; i < tabOrder.size(); i++) {
      layout.setText(font, tabLabel(tabOrder.get(i)));
      widths[i] = layout.width;
      textTotal += layout.width;
    }
    float padding = (gridBoxWidth(STASH_COLS) - textTotal) / tabOrder.size();
    float tabX = x + MARGIN;
    for (int i = 0; i < tabOrder.size(); i++) {
      float w = widths[i] + padding;
      tabBounds.add(new Rectangle(tabX, y + TABS_Y, w - 1f, 22f));
      tabX += w;
    }
  }

  private static String tabLabel(StorageService.Category category) {
    return category == null
        ? I18n.key("ui.storage_all", "All")
        : I18n.key("ui.storage_tab." + category.name().toLowerCase(Locale.ROOT), category.name());
  }

  private void addControls() {
    float stashX = x + MARGIN;
    float stashW = gridBoxWidth(STASH_COLS);
    float packX = packBoxX();
    float packW = gridBoxWidth(PACK_COLS);
    searchBounds.set(stashX, y + CONTROLS_Y, stashW - 118f, BUTTON_H);
    controls.add(
        new Button(
            stashX + stashW - 112f,
            y + CONTROLS_Y,
            112f,
            BUTTON_H,
            () -> I18n.key("ui.storage_take_all", "Take All"),
            () -> !stashStacks.isEmpty(),
            this::takeAllVisible));
    controls.add(
        new Button(
            packX,
            y + CONTROLS_Y,
            packW,
            BUTTON_H,
            () -> I18n.key("ui.storage_stash_gear", "Stash All Gear"),
            this::hasGearInPack,
            this::stashAllGear));
    controls.add(
        new Button(
            stashX + stashW - 112f,
            y + GOLD_Y,
            112f,
            BUTTON_H,
            () -> I18n.key("ui.withdraw", "Withdraw"),
            () -> player != null && backend.gold() > 0,
            () -> openPrompt(null, backend.gold(), true, true)));
    controls.add(
        new Button(
            packX + packW - 100f,
            y + GOLD_Y,
            100f,
            BUTTON_H,
            () -> I18n.key("ui.deposit", "Deposit"),
            () -> player != null && player.getGold() > 0,
            () -> openPrompt(null, player.getGold(), true, false)));
  }

  // ---------------------------------------------------------------- data

  private void rebuild() {
    if (player == null) return;
    backend.synchronize();
    stashStacks = buildStashStacks();
    packStacks = buildPackStacks();
    stashScrollRow = clampScroll(stashScrollRow, stashStacks.size(), STASH_COLS);
    packScrollRow = clampScroll(packScrollRow, packStacks.size(), PACK_COLS);
    if (selectedKey != null && findStack(selectedPane, selectedKey) == null) selectedKey = null;
  }

  private List<Stack> buildStashStacks() {
    List<String> storage = backend.items();
    Map<String, int[]> grouped = new LinkedHashMap<>();
    Map<String, Double> durabilityOf = new LinkedHashMap<>();
    String query = searchQuery.trim().toLowerCase(Locale.ROOT);
    for (int i = 0; i < storage.size(); i++) {
      String key = storage.get(i);
      if (key == null) continue;
      if (selectedCategory != null && StorageService.categoryOf(key) != selectedCategory) continue;
      if (!query.isEmpty() && !displayName(key).toLowerCase(Locale.ROOT).contains(query)) continue;
      double durability = backend.durability(i);
      String group = groupKey(key, durability);
      grouped.computeIfAbsent(group, g -> new int[] {-1, 0});
      int[] v = grouped.get(group);
      if (v[0] < 0) v[0] = i;
      v[1]++;
      durabilityOf.putIfAbsent(group, durability);
    }
    List<Stack> result = new ArrayList<>();
    grouped.forEach(
        (group, v) -> result.add(new Stack(storage.get(v[0]), v[0], v[1], durabilityOf.get(group))));
    result.sort(STACK_ORDER);
    return result;
  }

  private List<Stack> buildPackStacks() {
    List<String> inventory = player.getInventory();
    Map<String, int[]> grouped = new LinkedHashMap<>();
    Map<String, Double> durabilityOf = new LinkedHashMap<>();
    for (int i = 0; i < inventory.size(); i++) {
      String key = inventory.get(i);
      if (key == null) continue;
      double durability = ItemDurabilityService.inventory(player, i);
      String group = groupKey(key, durability);
      grouped.computeIfAbsent(group, g -> new int[] {-1, 0});
      int[] v = grouped.get(group);
      if (v[0] < 0) v[0] = i;
      v[1]++;
      durabilityOf.putIfAbsent(group, durability);
    }
    List<Stack> result = new ArrayList<>();
    grouped.forEach(
        (group, v) ->
            result.add(new Stack(inventory.get(v[0]), v[0], v[1], durabilityOf.get(group))));
    return result;
  }

  private static final Comparator<Stack> STACK_ORDER =
      Comparator.<Stack, Integer>comparing(s -> StorageService.categoryOf(s.itemKey).ordinal())
          .thenComparing(s -> displayName(s.itemKey), String.CASE_INSENSITIVE_ORDER)
          .thenComparing(s -> -s.durability);

  private static String groupKey(String key, double durability) {
    return ItemDurabilityService.isRepairable(ItemDefinition.get(key))
        ? key + "\u0000" + durability
        : key;
  }

  static String displayName(String itemKey) {
    ItemDefinition def = ItemDefinition.get(itemKey);
    return I18n.resolve(def != null && def.getName() != null ? def.getName() : itemKey);
  }

  private static int clampScroll(int row, int stackCount, int cols) {
    int totalRows = (stackCount + cols - 1) / cols;
    return Math.max(0, Math.min(row, Math.max(0, totalRows - ROWS)));
  }

  private Stack findStack(Pane pane, String key) {
    if (pane == null || key == null) return null;
    for (Stack s : pane == Pane.STASH ? stashStacks : packStacks) if (s.itemKey.equals(key)) return s;
    return null;
  }

  private boolean hasGearInPack() {
    if (player == null) return false;
    for (String key : player.getInventory())
      if (key != null && StorageService.categoryOf(key) != StorageService.Category.MISC) return true;
    return false;
  }

  // ---------------------------------------------------------------- actions

  private void deposit(Stack stack, int amount) {
    if (player == null || stack == null || amount <= 0) return;
    int moved = 0;
    for (int i = 0; i < amount; i++) {
      int index = indexOf(player.getInventory(), stack.itemKey, stack.durability, false);
      if (index < 0) break;
      if (!backend.deposit(index, stack.itemKey).success()) break;
      moved++;
    }
    if (moved > 0) persist();
  }

  private void withdraw(Stack stack, int amount) {
    if (player == null || stack == null || amount <= 0) return;
    int moved = 0;
    InventoryService.Result last = null;
    for (int i = 0; i < amount; i++) {
      int index = indexOf(backend.items(), stack.itemKey, stack.durability, true);
      if (index < 0) break;
      last = backend.withdraw(index, stack.itemKey);
      if (!last.success()) break;
      moved++;
    }
    if (last != null && !last.success()) explainFailure(last, stack.itemKey);
    if (moved > 0) persist();
  }

  /** Finds a copy of {@code key} with the given durability (same stack), else any copy. */
  private int indexOf(List<String> items, String key, double durability, boolean stash) {
    boolean repairable = ItemDurabilityService.isRepairable(ItemDefinition.get(key));
    int fallback = -1;
    for (int i = 0; i < items.size(); i++) {
      if (!key.equals(items.get(i))) continue;
      if (fallback < 0) fallback = i;
      if (!repairable) return i;
      double d =
          stash ? backend.durability(i) : ItemDurabilityService.inventory(player, i);
      if (d == durability) return i;
    }
    return fallback;
  }

  private void takeAllVisible() {
    if (player == null) return;
    InventoryService.Result failure = null;
    String failedKey = null;
    int moved = 0;
    for (Stack stack : new ArrayList<>(stashStacks)) {
      for (int i = 0; i < stack.count; i++) {
        int index = indexOf(backend.items(), stack.itemKey, stack.durability, true);
        if (index < 0) break;
        InventoryService.Result result = backend.withdraw(index, stack.itemKey);
        if (!result.success()) {
          if (failure == null) {
            failure = result;
            failedKey = stack.itemKey;
          }
          break;
        }
        moved++;
      }
    }
    if (failure != null) explainFailure(failure, failedKey);
    if (moved > 0) persist();
  }

  private void stashAllGear() {
    if (player == null) return;
    int moved = 0;
    for (StorageService.Category category : StorageService.Category.values()) {
      if (category != StorageService.Category.MISC) moved += backend.depositAll(category);
    }
    if (moved > 0) persist();
  }

  private void explainFailure(InventoryService.Result result, String itemKey) {
    String name = displayName(itemKey);
    String message =
        switch (result.failure()) {
          case TOO_HEAVY -> I18n.message("message.storage_too_heavy", name);
          case UNIQUE_ITEM ->
              I18n.message("message.storage_unique_owned", name);
          default -> null;
        };
    if (message != null) SystemMessage.showShared(message);
  }

  private void persist() {
    PlayerStateStore.save(player);
    rebuild();
  }

  private void moveStack(Pane from, Stack stack, int amount) {
    if (from == Pane.STASH) withdraw(stack, amount);
    else deposit(stack, amount);
  }

  // ---------------------------------------------------------------- quantity prompt

  private void openPrompt(String itemKey, int max, boolean gold, boolean withdraw) {
    if (max <= 0) return;
    QuantityPrompt p = new QuantityPrompt();
    p.itemKey = itemKey;
    p.gold = gold;
    p.withdraw = withdraw;
    p.max = max;
    p.value = String.valueOf(max);
    float pw = 240f;
    float ph = 132f;
    p.bounds.set(x + (WIDTH - pw) / 2f, y + (HEIGHT - ph) / 2f, pw, ph);
    float by = p.bounds.y + ph - 40f;
    p.buttons.add(
        new Button(p.bounds.x + 18f, by, 60f, BUTTON_H, () -> I18n.key("ui.accept", "Accept"), () -> true, this::applyPrompt));
    p.buttons.add(
        new Button(p.bounds.x + 90f, by, 60f, BUTTON_H, () -> I18n.key("ui.storage_max", "Max"), () -> true, () -> prompt.value = String.valueOf(prompt.max)));
    p.buttons.add(
        new Button(p.bounds.x + 162f, by, 60f, BUTTON_H, () -> I18n.key("ui.cancel", "Cancel"), () -> true, () -> prompt = null));
    p.buttons.add(
        new Button(p.bounds.x + 30f, p.bounds.y + 56f, 24f, 22f, () -> "-", () -> true, () -> setPromptValue(promptValue() - 1)));
    p.buttons.add(
        new Button(p.bounds.x + pw - 54f, p.bounds.y + 56f, 24f, 22f, () -> "+", () -> true, () -> setPromptValue(promptValue() + 1)));
    prompt = p;
  }

  private int promptValue() {
    try {
      return Integer.parseInt(prompt.value);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private void setPromptValue(int value) {
    prompt.value = String.valueOf(Math.max(1, Math.min(prompt.max, value)));
  }

  private void applyPrompt() {
    QuantityPrompt p = prompt;
    prompt = null;
    if (p == null || player == null) return;
    int amount = Math.max(0, Math.min(p.max, parse(p.value)));
    if (amount <= 0) return;
    if (p.gold) {
      if (p.withdraw) backend.withdrawGold(amount);
      else backend.depositGold(amount);
      persist();
      return;
    }
    if (p.stack != null) moveStack(p.withdraw ? Pane.STASH : Pane.PACK, p.stack, amount);
  }

  private static int parse(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  // ---------------------------------------------------------------- rendering

  @Override
  public void render(SpriteBatch batch) {
    if (trade == null) {
      super.render(batch);
      return;
    }
    GuiDraw.withOverlayAlpha(batch, () -> drawFrame(batch));
    drawHeaders(batch);
    drawTabs(batch);
    drawGridBox(batch, stashCells, STASH_COLS, stashScrollRow, stashStacks);
    drawGridBox(batch, packCells, PACK_COLS, packScrollRow, packStacks);
    drawItems(batch, Pane.STASH, stashCells, STASH_COLS, stashScrollRow, stashStacks);
    drawItems(batch, Pane.PACK, packCells, PACK_COLS, packScrollRow, packStacks);
    drawControls(batch);
    super.render(batch);
    drawDrag(batch);
    if (prompt != null) drawPrompt(batch);
    else tooltip.render(batch);
  }

  private void drawFrame(SpriteBatch batch) {
    tile(batch, SRC_STONE, x, y + TITLE_H, WIDTH, HEIGHT - TITLE_H);
    tile(batch, SRC_TOP_BAR, x, y, WIDTH, TITLE_H);
    tile(batch, SRC_BORDER_LEFT, x, y + TITLE_H, 6f, HEIGHT - TITLE_H);
    tile(batch, SRC_BORDER_RIGHT, x + WIDTH - 6f, y + TITLE_H, 6f, HEIGHT - TITLE_H);
    tile(batch, SRC_BORDER_BOTTOM, x, y + HEIGHT - 5f, WIDTH, 5f);
    float pillW = 150f;
    drawThreeSlice(batch, SRC_TITLE_PILL, 12, x + (WIDTH - pillW) / 2f, y, pillW, TITLE_H);
  }

  private void drawHeaders(SpriteBatch batch) {
    BitmapFont title = FontManager.getInstance().getHaettenschweilerFont(18, GOLD);
    centered(batch, title, backend.label(), x, y + 4f, WIDTH);
    boolean account = backend instanceof AccountStorageBackend;
    GuiDraw.withOverlayAlpha(
        batch,
        () ->
            drawThreeSlice(
                batch,
                SRC_PILL,
                12,
                sourceToggleBounds.x,
                sourceToggleBounds.y,
                sourceToggleBounds.width,
                sourceToggleBounds.height));
    BitmapFont toggleFont = FontManager.getInstance().getJetBrainsMonoFont(10, account ? DIM : GOLD);
    centered(
        batch,
        toggleFont,
        account
            ? I18n.key("ui.storage_switch_to_character", "◂ Switch to Character Storage")
            : I18n.key("ui.storage_switch_to_account", "Switch to Account Vault ▸"),
        sourceToggleBounds.x,
        sourceToggleBounds.y + 6f,
        sourceToggleBounds.width);
    BitmapFont font = FontManager.getInstance().getJetBrainsMonoFont(11, GOLD);
    float stashW = gridBoxWidth(STASH_COLS);
    float packW = gridBoxWidth(PACK_COLS);
    GuiDraw.withOverlayAlpha(
        batch,
        () -> {
          drawThreeSlice(batch, SRC_PILL, 12, x + MARGIN, y + PILL_Y, stashW, 23f);
          drawThreeSlice(batch, SRC_PILL, 12, packBoxX(), y + PILL_Y, packW, 23f);
        });
    int stored = player == null ? 0 : backend.items().size();
    centered(
        batch,
        font,
        I18n.message("ui.storage_header", String.valueOf(stored)),
        x + MARGIN,
        y + PILL_Y + 5f,
        stashW);
    long weight = InventoryService.currentWeight(player);
    long maxWeight = InventoryService.maximumWeight(player);
    centered(
        batch,
        font,
        I18n.message("ui.storage_backpack_header",
            String.valueOf(weight),
            String.valueOf(maxWeight)),
        packBoxX(),
        y + PILL_Y + 5f,
        packW);
  }

  private void drawTabs(SpriteBatch batch) {
    BitmapFont font = FontManager.getInstance().getTahomaFont(11, Color.BLACK, true);
    BitmapFont active = FontManager.getInstance().getTahomaFont(11, Color.WHITE, true);
    for (int i = 0; i < tabOrder.size(); i++) {
      StorageService.Category category = tabOrder.get(i);
      Rectangle r = tabBounds.get(i);
      boolean selected = category == selectedCategory;
      boolean hover = r.contains(mouseX, mouseY);
      TextureRegion bg = selected ? tabButton[2] : hover ? tabButton[1] : tabButton[0];
      if (bg != null) GuiDraw.drawRegionFlipped(batch, bg, r.x, r.y, r.width, r.height);
      String label = tabLabel(category);
      centered(batch, selected ? active : font, label, r.x, r.y + 5f, r.width);
    }
  }

  private void drawGridBox(
      SpriteBatch batch, Rectangle cells, int cols, int scrollRow, List<Stack> stacks) {
    float bx = cells.x - GRID_LEFT;
    float by = cells.y - GRID_TOP;
    float right = cells.x + cells.width;
    float bottom = cells.y + cells.height;
    GuiDraw.withOverlayAlpha(
        batch,
        () -> {
          draw(batch, SRC_GRID_TL, bx, by, GRID_LEFT, GRID_TOP);
          tile(batch, SRC_GRID_TOP, cells.x, by, cells.width, GRID_TOP);
          draw(batch, SRC_GRID_TR, right, by, GRID_RIGHT, GRID_TOP);
          tile(batch, SRC_GRID_LEFT, bx, cells.y, GRID_LEFT, cells.height);
          tile(batch, SRC_GRID_CELL, cells.x, cells.y, cells.width, cells.height);
          draw(batch, SRC_GRID_SCROLL, right, cells.y, GRID_RIGHT, cells.height);
          draw(batch, SRC_GRID_BL, bx, bottom, GRID_LEFT, GRID_BOTTOM);
          tile(batch, SRC_GRID_BOTTOM, cells.x, bottom, cells.width, GRID_BOTTOM);
          draw(batch, SRC_GRID_BR, right, bottom, GRID_RIGHT, GRID_BOTTOM);
        });
    int totalRows = (stacks.size() + cols - 1) / cols;
    if (scrollTick != null && totalRows > ROWS) {
      float travel = cells.height - 44f - scrollTick.getRegionHeight();
      float t = scrollRow / (float) (totalRows - ROWS);
      GuiDraw.drawRegionFlipped(
          batch,
          scrollTick,
          right + (GRID_RIGHT - 7f - scrollTick.getRegionWidth()) / 2f,
          cells.y + 22f + travel * t);
    }
  }

  private void drawItems(
      SpriteBatch batch, Pane pane, Rectangle cells, int cols, int scrollRow, List<Stack> stacks) {
    BitmapFont countFont = FontManager.getInstance().getJetBrainsMonoFont(10, Color.WHITE);
    BitmapFont shadow = FontManager.getInstance().getJetBrainsMonoFont(10, Color.BLACK);
    BitmapFont durFont = FontManager.getInstance().getJetBrainsMonoFont(9, DAMAGED);
    int first = scrollRow * cols;
    for (int i = first; i < Math.min(stacks.size(), first + cols * ROWS); i++) {
      Stack stack = stacks.get(i);
      int slot = i - first;
      float cx = cells.x + (slot % cols) * CELL;
      float cy = cells.y + (slot / cols) * CELL;
      boolean hovered = drag == null && prompt == null && new Rectangle(cx, cy, CELL, CELL).contains(mouseX, mouseY);
      boolean selected = pane == selectedPane && stack.itemKey.equals(selectedKey);
      if ((hovered || selected) && cellHighlight != null) {
        GuiDraw.drawRegionFlipped(batch, cellHighlight, cx, cy, CELL - 1f, CELL - 1f);
      }
      if (drag != null && drag.moved && drag.stack == stack) continue;
      TextureRegion region = icon(stack.itemKey);
      if (region != null) drawFitted(batch, region, cx + 1f, cy + 1f, CELL - 3f, CELL - 3f);
      if (stack.count > 1) {
        String count = stack.count > 999 ? "999+" : String.valueOf(stack.count);
        layout.setText(countFont, count);
        float tx = cx + CELL - 3f - layout.width;
        float ty = cy + CELL - 13f;
        shadow.draw(batch, count, tx + 1f, ty + 1f);
        countFont.draw(batch, count, tx, ty);
      }
      if (ItemDurabilityService.isRepairable(ItemDefinition.get(stack.itemKey))
          && stack.durability < ItemDurabilityService.MAX) {
        durFont.draw(batch, ItemDurabilityService.format(stack.durability) + "%", cx + 2f, cy + 1f);
      }
    }
  }

  private void drawControls(SpriteBatch batch) {
    BitmapFont white = FontManager.getInstance().getJetBrainsMonoFont(11, Color.WHITE);
    BitmapFont gold = FontManager.getInstance().getJetBrainsMonoFont(11, GOLD);
    BitmapFont dim = FontManager.getInstance().getJetBrainsMonoFont(10, DIM);
    // Search box.
    GuiDraw.withOverlayAlpha(
        batch,
        () ->
            drawThreeSlice(
                batch, SRC_PILL, 12, searchBounds.x, searchBounds.y + 1f, searchBounds.width, 23f));
    String shown =
        searchQuery.isEmpty() && !searchFocused
            ? I18n.key("ui.storage_search_hint", "Search... (click to type)")
            : searchQuery + (searchFocused && (System.currentTimeMillis() / 500L) % 2 == 0 ? "_" : "");
    (searchQuery.isEmpty() && !searchFocused ? dim : white)
        .draw(batch, shown, searchBounds.x + 12f, searchBounds.y + 6f);
    // Gold readouts.
    float stashX = x + MARGIN;
    gold.draw(
        batch,
        I18n.key("ui.storage_banked_gold", "Banked gold") + ": " + formatGold(player == null ? 0 : backend.gold()),
        stashX + 4f,
        y + GOLD_Y + 7f);
    gold.draw(
        batch,
        I18n.key("ui.storage_carried_gold", "Gold") + ": " + formatGold(player == null ? 0 : player.getGold()),
        packBoxX() + 4f,
        y + GOLD_Y + 7f);
    for (Button b : controls) drawButton(batch, b);
    dim.draw(
        batch,
        I18n.key(
            "ui.storage_hint",
            "Right-click: move one  |  Shift+click: move stack  |  Ctrl+click: choose amount  |  Drag & drop"),
        x + MARGIN + 4f,
        y + HINT_Y);
  }

  private static String formatGold(int gold) {
    return String.format(Locale.ROOT, "%,d", gold);
  }

  private void drawButton(SpriteBatch batch, Button b) {
    boolean enabled = b.enabled.getAsBoolean();
    boolean hover = enabled && b.bounds.contains(mouseX, mouseY);
    boolean down = hover && Gdx.input.isButtonPressed(Input.Buttons.LEFT);
    TextureRegion bg = down ? button[2] : hover ? button[1] : button[0];
    if (bg != null) {
      Color previous = new Color(batch.getColor());
      if (!enabled) batch.setColor(0.55f, 0.55f, 0.55f, previous.a);
      GuiDraw.drawRegionFlipped(batch, bg, b.bounds.x, b.bounds.y, b.bounds.width, b.bounds.height);
      batch.setColor(previous);
    }
    BitmapFont font =
        FontManager.getInstance()
            .getTahomaFont(12, enabled ? Color.BLACK : Color.valueOf("5A5040"), true);
    centered(batch, font, b.label.get(), b.bounds.x, b.bounds.y + (b.bounds.height - 12f) / 2f, b.bounds.width);
  }

  private void drawDrag(SpriteBatch batch) {
    if (drag == null || !drag.moved || drag.region == null) return;
    drawFitted(batch, drag.region, drag.screenX - 14f, drag.screenY - 14f, 28f, 28f);
  }

  private void drawPrompt(SpriteBatch batch) {
    QuantityPrompt p = prompt;
    Rectangle r = p.bounds;
    Color previous = new Color(batch.getColor());
    batch.setColor(0f, 0f, 0f, 0.45f);
    batch.draw(tooltip.getBackground(), x, y + TITLE_H, WIDTH, HEIGHT - TITLE_H);
    batch.setColor(previous);
    if (popup != null) {
      // Top of the popup art without its chain band, then its bottom edge.
      draw(batch, popup, 0, 0, 240, 120, r.x, r.y, r.width, r.height - 6f);
      draw(batch, popup, 0, 158, 240, 6, r.x, r.y + r.height - 6f, r.width, 6f);
    }
    BitmapFont header = FontManager.getInstance().getHaettenschweilerFont(18, GOLD);
    BitmapFont white = FontManager.getInstance().getJetBrainsMonoFont(11, Color.WHITE);
    centered(batch, header, I18n.key("ui.how_many", "How many?"), r.x, r.y + 8f, r.width);
    String what = p.gold ? I18n.key("ui.gold", "Gold") : displayName(p.itemKey);
    centered(batch, white, what + " (" + I18n.key("ui.max", "max") + " " + formatGold(p.max) + ")", r.x, r.y + 34f, r.width);
    GuiDraw.withOverlayAlpha(
        batch, () -> drawThreeSlice(batch, SRC_PILL, 12, r.x + 60f, r.y + 55f, r.width - 120f, 23f));
    centered(batch, white, p.value, r.x + 60f, r.y + 60f, r.width - 120f);
    for (Button b : p.buttons) drawButton(batch, b);
  }

  private void centered(SpriteBatch batch, BitmapFont font, String text, float bx, float by, float bw) {
    layout.setText(font, text);
    font.draw(batch, text, bx + Math.round((bw - layout.width) / 2f), by);
  }

  private static TextureRegion icon(String itemKey) {
    ItemDefinition def = ItemDefinition.get(itemKey);
    String sprite = def == null ? itemKey : def.getAppearanceInventory();
    return sprite == null || sprite.isEmpty() ? null : load(sprite);
  }

  private static void drawFitted(
      SpriteBatch batch, TextureRegion region, float bx, float by, float bw, float bh) {
    float w = region.getRegionWidth();
    float h = region.getRegionHeight();
    float scale = Math.min(1f, Math.min(bw / w, bh / h));
    float dw = w * scale;
    float dh = h * scale;
    GuiDraw.drawRegionFlipped(batch, region, bx + (bw - dw) / 2f, by + (bh - dh) / 2f, dw, dh);
  }

  private void draw(SpriteBatch batch, int[] src, float dx, float dy, float dw, float dh) {
    draw(batch, trade, src[0], src[1], src[2], src[3], dx, dy, dw, dh);
  }

  private void draw(
      SpriteBatch batch,
      TextureRegion source,
      int sx,
      int sy,
      int sw,
      int sh,
      float dx,
      float dy,
      float dw,
      float dh) {
    if (source == null || sw <= 0 || sh <= 0) return;
    scratch.setRegion(source, sx, sy, sw, sh);
    GuiDraw.drawRegionFlipped(batch, scratch, dx, dy, dw, dh);
  }

  /** Repeats a source piece at native size to fill the area, cropping the last row/column. */
  private void tile(SpriteBatch batch, int[] src, float dx, float dy, float dw, float dh) {
    for (float ty = 0; ty < dh; ty += src[3]) {
      int h = (int) Math.min(src[3], Math.ceil(dh - ty));
      for (float tx = 0; tx < dw; tx += src[2]) {
        int w = (int) Math.min(src[2], Math.ceil(dw - tx));
        draw(batch, trade, src[0], src[1], w, h, dx + tx, dy + ty, w, h);
      }
    }
  }

  /** Draws a horizontally stretchable piece: fixed end caps, repeated middle. */
  private void drawThreeSlice(
      SpriteBatch batch, int[] src, int cap, float dx, float dy, float dw, float dh) {
    draw(batch, trade, src[0], src[1], cap, src[3], dx, dy, cap, dh);
    int midW = src[2] - 2 * cap;
    float mid = dw - 2 * cap;
    for (float tx = 0; tx < mid; tx += midW) {
      int w = (int) Math.min(midW, Math.ceil(mid - tx));
      draw(batch, trade, src[0] + cap, src[1], w, src[3], dx + cap + tx, dy, w, dh);
    }
    draw(batch, trade, src[0] + src[2] - cap, src[1], cap, src[3], dx + dw - cap, dy, cap, dh);
  }

  // ---------------------------------------------------------------- input

  @Override
  protected boolean isBackgroundHit(float screenX, float screenY) {
    return screenX >= x && screenX <= x + WIDTH && screenY >= y && screenY <= y + HEIGHT;
  }

  private Stack stackAt(Pane pane, float sx, float sy) {
    Rectangle cells = pane == Pane.STASH ? stashCells : packCells;
    if (!cells.contains(sx, sy)) return null;
    int cols = pane == Pane.STASH ? STASH_COLS : PACK_COLS;
    int col = (int) ((sx - cells.x) / CELL);
    int row = (int) ((sy - cells.y) / CELL);
    if (col < 0 || col >= cols || row < 0 || row >= ROWS) return null;
    int scroll = pane == Pane.STASH ? stashScrollRow : packScrollRow;
    int index = (row + scroll) * cols + col;
    List<Stack> stacks = pane == Pane.STASH ? stashStacks : packStacks;
    return index < stacks.size() ? stacks.get(index) : null;
  }

  private Pane paneAt(float sx, float sy) {
    if (new Rectangle(stashCells.x - GRID_LEFT, stashCells.y - GRID_TOP, gridBoxWidth(STASH_COLS), stashCells.height + GRID_TOP + GRID_BOTTOM).contains(sx, sy)) return Pane.STASH;
    if (new Rectangle(packCells.x - GRID_LEFT, packCells.y - GRID_TOP, gridBoxWidth(PACK_COLS), packCells.height + GRID_TOP + GRID_BOTTOM).contains(sx, sy)) return Pane.PACK;
    return null;
  }

  @Override
  public void onTouchDown(float screenX, float screenY) {
    onTouchDown(screenX, screenY, Input.Buttons.LEFT);
  }

  @Override
  public void onTouchDown(float screenX, float screenY, int mouseButton) {
    mouseX = screenX;
    mouseY = screenY;
    tooltip.clear();
    if (prompt != null) {
      for (Button b : prompt.buttons) {
        if (b.bounds.contains(screenX, screenY)) {
          b.action.run();
          return;
        }
      }
      return;
    }
    for (GuiButton b : buttons) {
      if (b.contains(screenX, screenY)) {
        b.onTouchDown(screenX, screenY);
        return;
      }
    }
    if (sourceToggleBounds.contains(screenX, screenY)) {
      toggleSource();
      return;
    }
    searchFocused = searchBounds.contains(screenX, screenY);
    if (searchFocused) return;
    for (Button b : controls) {
      if (b.bounds.contains(screenX, screenY)) {
        if (b.enabled.getAsBoolean()) b.action.run();
        return;
      }
    }
    for (int i = 0; i < tabBounds.size(); i++) {
      if (tabBounds.get(i).contains(screenX, screenY)) {
        selectedCategory = tabOrder.get(i);
        stashScrollRow = 0;
        rebuild();
        return;
      }
    }
    if (stashScroll.contains(screenX, screenY)) {
      scroll(Pane.STASH, screenY < stashScroll.y + stashScroll.height / 2f ? -1 : 1);
      return;
    }
    if (packScroll.contains(screenX, screenY)) {
      scroll(Pane.PACK, screenY < packScroll.y + packScroll.height / 2f ? -1 : 1);
      return;
    }
    Pane pane = paneAt(screenX, screenY);
    Stack stack = pane == null ? null : stackAt(pane, screenX, screenY);
    if (stack == null) {
      selectedKey = null;
      return;
    }
    boolean shift = isDown(Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT);
    boolean ctrl = isDown(Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT);
    if (ctrl) {
      openPrompt(stack.itemKey, stack.count, false, pane == Pane.STASH);
      prompt.stack = stack;
      return;
    }
    if (shift) {
      moveStack(pane, stack, stack.count);
      return;
    }
    if (mouseButton == Input.Buttons.RIGHT) {
      moveStack(pane, stack, 1);
      return;
    }
    long now = System.currentTimeMillis();
    if (now - lastClickMs < DOUBLE_CLICK_MS && stack.itemKey.equals(lastClickKey)) {
      lastClickMs = 0L;
      moveStack(pane, stack, 1);
      return;
    }
    lastClickMs = now;
    lastClickKey = stack.itemKey;
    selectedPane = pane;
    selectedKey = stack.itemKey;
    DragState d = new DragState();
    d.source = pane;
    d.stack = stack;
    d.region = icon(stack.itemKey);
    d.startX = d.screenX = screenX;
    d.startY = d.screenY = screenY;
    drag = d;
  }

  private static boolean isDown(int a, int b) {
    return Gdx.input != null && (Gdx.input.isKeyPressed(a) || Gdx.input.isKeyPressed(b));
  }

  @Override
  public void onMouseMove(float screenX, float screenY) {
    mouseX = screenX;
    mouseY = screenY;
    for (GuiButton b : buttons) b.onMouseMove(screenX, screenY);
    if (drag != null) {
      drag.screenX = screenX;
      drag.screenY = screenY;
      if (Math.abs(screenX - drag.startX) + Math.abs(screenY - drag.startY) > 4f) drag.moved = true;
      tooltip.clear();
      return;
    }
    if (prompt != null) return;
    Pane pane = paneAt(screenX, screenY);
    Stack stack = pane == null ? null : stackAt(pane, screenX, screenY);
    if (stack == null) {
      tooltip.clear();
      return;
    }
    String text = ItemTooltipText.build(stack.itemKey, stack.durability);
    if (stack.count > 1) {
      text += "\n" + I18n.message("ui.storage_stack_count", String.valueOf(stack.count));
    }
    tooltip.show(text, screenX, screenY);
  }

  @Override
  public void onTouchUp(float screenX, float screenY) {
    for (GuiButton b : new ArrayList<>(buttons)) b.onTouchUp(screenX, screenY);
    DragState d = drag;
    drag = null;
    if (d == null || !d.moved) return;
    Pane target = paneAt(screenX, screenY);
    if (target != null && target != d.source) {
      boolean shift = isDown(Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT);
      moveStack(d.source, d.stack, shift ? d.stack.count : 1);
    }
  }

  @Override
  public void onScroll(float amountY, float screenX, float screenY) {
    Pane pane = paneAt(screenX, screenY);
    if (pane != null) scroll(pane, amountY > 0 ? 1 : -1);
  }

  private void scroll(Pane pane, int rows) {
    if (pane == Pane.STASH) {
      stashScrollRow = clampScroll(stashScrollRow + rows, stashStacks.size(), STASH_COLS);
    } else {
      packScrollRow = clampScroll(packScrollRow + rows, packStacks.size(), PACK_COLS);
    }
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (prompt != null) {
      if (keycode == Input.Keys.ESCAPE) prompt = null;
      else if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) applyPrompt();
      else if (keycode == Input.Keys.BACKSPACE)
        prompt.value = prompt.value.length() > 1 ? prompt.value.substring(0, prompt.value.length() - 1) : "0";
      else if (keycode == Input.Keys.UP) setPromptValue(promptValue() + 1);
      else if (keycode == Input.Keys.DOWN) setPromptValue(promptValue() - 1);
      return true;
    }
    if (keycode == Input.Keys.ESCAPE) {
      if (searchFocused || !searchQuery.isEmpty()) {
        searchFocused = false;
        searchQuery = "";
        rebuild();
      } else {
        GuiManager.close();
      }
      return true;
    }
    if (searchFocused) {
      if (keycode == Input.Keys.BACKSPACE && !searchQuery.isEmpty()) {
        searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
        stashScrollRow = 0;
        rebuild();
      } else if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
        searchFocused = false;
      }
      // Swallow everything else so typing a name never triggers game hotkeys.
      return true;
    }
    return false;
  }

  @Override
  public boolean onKeyTyped(char character) {
    if (prompt != null) {
      if (Character.isDigit(character)) {
        String candidate = (prompt.value.equals("0") ? "" : prompt.value) + character;
        if (candidate.length() <= 10) setPromptValue((int) Math.min(Integer.MAX_VALUE, Long.parseLong(candidate)));
      }
      return true;
    }
    if (!searchFocused) return false;
    if ((Character.isLetterOrDigit(character) || character == ' ' || character == '\'' || character == '+')
        && searchQuery.length() < 24) {
      searchQuery += character;
      stashScrollRow = 0;
      rebuild();
    }
    return true;
  }

  @Override
  public boolean capturesKeyboard() {
    return searchFocused || prompt != null;
  }

  @Override
  public void dispose() {
    tooltip.dispose();
  }

  // Visible for tests.
  List<Stack> stashStacks() {
    return stashStacks;
  }

  List<Stack> packStacks() {
    return packStacks;
  }

  void setSearchQuery(String query) {
    searchQuery = query == null ? "" : query;
    rebuild();
  }

  void selectCategory(StorageService.Category category) {
    selectedCategory = category;
    rebuild();
  }
}
