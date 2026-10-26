package com.perso.T4C.gui.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.GuiClickZone;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemIconRegistry;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.ItemSalePricing;
import com.perso.T4C.item.ItemSaleService;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.SystemMessage;
import java.util.ArrayList;
import java.util.List;

public class ShopScreen extends GuiListScreen {
  private static final float[] LIST_BOX = {
    18f, ROW_0_Y - 12f, 556f, ROWS_VISIBLE * ROW_H_PITCH + 12f
  };
  private static final float THUMB_X = 407f;
  private static final float THUMB_TOP_Y = 60f;
  private static final float THUMB_BOTTOM_Y = 261f;
  private static final Color BLOCKED = Color.valueOf("E4A29B");
  private final Player player;
  private final boolean selling;
  private final List<ShopEntry> entries = new ArrayList<>();
  private ShopEntry selected = null;

  public ShopScreen(Player player, List<String> itemKeys) {
    this(player, itemKeys, false);
  }

  private ShopScreen(Player player, List<String> itemKeys, boolean selling) {
    this.player = player;
    this.selling = selling;
    initList();
    addStaticLabels();
    loadEntries(itemKeys);
    rebuildList();
  }

  public static ShopScreen forSelling(Player player, List<String> itemKeys) {
    return new ShopScreen(player, itemKeys, true);
  }

  private void addStaticLabels() {
    if (background == null) return;
    BitmapFont chewy = FontManager.getInstance().getT4CBeaulieuFont(22, GOLD);
    labels.add(
        boxed(chewy, TITLE_BOX, 0f, () -> selling ? I18n.key("ui.sell") : I18n.key("ui.buy"), GOLD)
            .shrinkToFit());
    labels.add(
        boxed(
                FontManager.getInstance().getTahomaFont(12, GOLD, false),
                GOLD_HDR_BOX,
                0f,
                () -> I18n.key("ui.gold"),
                GOLD)
            .shrinkToFit());
    BitmapFont sm = FontManager.getInstance().getTahomaFont(12, GOLD, false);
    labels.add(boxed(sm, HDR_NAME_BOX, 0f, () -> I18n.key("ui.item_name"), GOLD));
    labels.add(boxed(sm, HDR_PRICE_BOX, 0f, () -> I18n.key("ui.price"), GOLD));
    labels.add(boxed(sm, HDR_THIRD_BOX, 0f, () -> I18n.key("ui.quantity_short"), GOLD));
    labels.add(boxed(sm, ONHAND_LBL_BOX, 0f, () -> I18n.key("ui.on_hand"), GOLD));
    labels.add(
        boxed(
            sm, COST_LBL_BOX, 0f, () -> I18n.key(selling ? "ui.sale_proceeds" : "ui.cost"), GOLD));
    labels.add(boxed(sm, TOTAL_LBL_BOX, 0f, () -> I18n.key("ui.balance_after"), GOLD));
  }

  private void loadEntries(List<String> itemKeys) {
    entries.clear();
    if (itemKeys == null) return;
    for (String itemKey : itemKeys) {
      if (itemKey == null || itemKey.isEmpty()) continue;
      ItemDefinition def = ItemRegistry.findByKey(itemKey);
      if (def != null && (!selling || ItemSalePricing.sellPrice(def) > 0)) {
        entries.add(new ShopEntry(def, selling ? ItemSalePricing.sellPrice(def) : def.getPrice()));
      }
    }
  }

  @Override
  protected void rebuildList() {
    labels.removeAll(dynLabels);
    dynLabels.clear();
    buttons.removeAll(dynButtons);
    dynButtons.clear();
    zones.clear();
    animatedSprites.clear();
    int total = entries.size();
    int pages = Math.max(1, (total + ROWS_VISIBLE - 1) / ROWS_VISIBLE);
    page = Math.min(page, pages - 1);
    int start = page * ROWS_VISIBLE;
    int end = Math.min(start + ROWS_VISIBLE, total);
    BitmapFont fontWh = FontManager.getInstance().getTahomaFont(12, WHITE, false);
    BitmapFont fontBl = FontManager.getInstance().getTahomaFont(12, BLOCKED, false);
    BitmapFont fontDm = FontManager.getInstance().getTahomaFont(12, DIM, false);
    BitmapFont fontGo = FontManager.getInstance().getTahomaFont(12, GOLD, false);
    for (int i = start; i < end; i++) {
      ShopEntry entry = entries.get(i);
      float rowY = ROW_0_Y + (i - start) * ROW_H_PITCH;
      boolean isSelected = (entry == selected);
      boolean canAfford =
          selling
              ? InventoryService.count(player, entry.def.getKey()) > entry.count
              : player.getGold() >= basketCost() + entry.effectivePrice;
      TextureRegion icon = loadIcon(entry.def);
      if (icon != null) {
        animatedSprites.add(
            new GuiAnimatedSprite(List.of(icon), x + ICON_BOX[0], y + rowY + ICON_BOX[1], 1f)
                .boxed(ICON_BOX[2], ICON_BOX[3]));
      }
      final String name = I18n.resolve(entry.def.getName());
      final String price = String.format(java.util.Locale.ROOT, "%,d", entry.effectivePrice);
      final String qty = String.valueOf(entry.count);
      addDyn(isSelected ? fontGo : fontWh, CELL_NAME, rowY, () -> name, isSelected ? GOLD : WHITE);
      addDyn(
          canAfford ? fontWh : fontBl, CELL_PRICE, rowY, () -> price, canAfford ? WHITE : BLOCKED);
      addDyn(
          entry.count > 0 ? fontGo : fontWh,
          CELL_THIRD,
          rowY,
          () -> qty,
          entry.count > 0 ? GOLD : WHITE);
      final ShopEntry e = entry;
      zones.add(
          new GuiClickZone(
              x + 18f,
              y + rowY - 4f,
              450f,
              ROW_H_PITCH - 4f,
              () -> {
                selected = e;
                rebuildList();
              }));
      addRowSpinButtons(rowY, e);
    }
    if (page > 0) {
      zones.add(
          new GuiClickZone(
              x + SCROLL_X, y + SCROLL_UP_Y, SCROLL_W, SCROLL_BTN_H, () -> scrollPages(-1)));
    }
    if (page < pages - 1) {
      zones.add(
          new GuiClickZone(
              x + SCROLL_X, y + SCROLL_DN_Y, SCROLL_W, SCROLL_BTN_H, () -> scrollPages(1)));
    }
    addScrollThumb(pages);
    final long onHand = player.getGold();
    final long cost = basketCost();
    final long left = selling ? Math.min(Integer.MAX_VALUE, onHand + cost) : onHand - cost;
    addDyn(
        fontGo,
        ONHAND_VAL_BOX,
        0f,
        () -> String.format(java.util.Locale.ROOT, "%,d", onHand),
        GOLD);
    addDyn(
        cost > 0 ? fontGo : fontDm,
        COST_VAL_BOX,
        0f,
        () -> String.format(java.util.Locale.ROOT, "%,d", cost),
        cost > 0 ? GOLD : DIM);
    addDyn(
        left >= 0 ? fontGo : fontBl,
        TOTAL_VAL_BOX,
        0f,
        () -> String.format(java.util.Locale.ROOT, "%,d", left),
        left >= 0 ? GOLD : BLOCKED);
    addBuyButton(basketCount() > 0 && (selling || onHand >= cost));
  }

  private void addBuyButton(boolean canBuy) {
    GuiButton buy =
        theme.button(
            x + ACTION_BTN_X,
            y + ACTION_BTN_Y,
            180,
            34,
            () -> selling ? I18n.key("ui.sell_action") : I18n.key("ui.buy_action"),
            this::buyBasket,
            true);
    buy.setEnabled(canBuy);
    buttons.add(buy);
    dynButtons.add(buy);
  }

  private int basketCount() {
    return entries.stream().mapToInt(e -> e.count).sum();
  }

  private long basketCost() {
    return entries.stream().mapToLong(e -> (long) e.count * e.effectivePrice).sum();
  }

  private void basketAdd(ShopEntry entry) {
    if (selling) {
      if (entry.count < InventoryService.count(player, entry.def.getKey())) entry.count++;
      selected = entry;
      rebuildList();
      return;
    }
    if (basketCost() + entry.effectivePrice > player.getGold()) {
      return;
    }
    entry.count++;
    selected = entry;
    rebuildList();
  }

  private void basketRemove(ShopEntry entry) {
    if (entry.count > 0) {
      entry.count--;
      selected = entry;
      rebuildList();
    }
  }

  private TextureRegion loadIcon(ItemDefinition def) {
    if (def == null) return null;
    TextureRegion icon = GuiSprites.load(ItemIconRegistry.iconFor(def));
    return icon != null ? icon : GuiSprites.load(def.getAppearanceInventory());
  }

  private void buyBasket() {
    long cost = basketCost();
    if (basketCount() == 0) {
      return;
    }
    if (!selling && player.getGold() < cost) {
      SystemMessage.showShared(I18n.message("message.not_enough_gold"));
      return;
    }
    if (selling) {
      for (ShopEntry entry : entries) ItemSaleService.sell(player, entry.def.getKey(), entry.count);
      entries.forEach(entry -> entry.count = 0);
      PlayerStateStore.save(player);
      selected = null;
      rebuildList();
      return;
    }
    player.setGold((int) (player.getGold() - cost));
    List<String> inv = player.getInventory();
    if (inv == null) {
      inv = new ArrayList<>();
      player.setInventory(inv);
    }
    int units = basketCount();
    String onlyName = null;
    for (ShopEntry entry : entries) {
      for (int n = 0; n < entry.count; n++) {
        inv.add(entry.def.getKey());
      }
      if (entry.count > 0) {
        onlyName = onlyName == null ? I18n.resolve(entry.def.getName()) : "";
        entry.count = 0;
      }
    }
    PlayerStateStore.save(player);
    boolean single = units == 1 && onlyName != null && !onlyName.isEmpty();
    SystemMessage.showShared(
        single
            ? I18n.message("message.item_bought", onlyName)
            : I18n.message("message.items_bought", units, cost));
    selected = null;
    rebuildList();
  }

  @Override
  public void onScroll(float amountY, float screenX, float screenY) {
    if (screenX >= x + LIST_BOX[0]
        && screenX <= x + LIST_BOX[0] + LIST_BOX[2]
        && screenY >= y + LIST_BOX[1]
        && screenY <= y + LIST_BOX[1] + LIST_BOX[3]) {
      scrollPages(amountY > 0 ? 1 : -1);
      return;
    }
    super.onScroll(amountY, screenX, screenY);
  }

  @Override
  protected ListRow selectedRow() {
    return selected;
  }

  @Override
  protected List<? extends ListRow> rows() {
    return entries;
  }

  @Override
  protected void basketAdd(ListRow row) {
    basketAdd((ShopEntry) row);
  }

  @Override
  protected void basketRemove(ListRow row) {
    basketRemove((ShopEntry) row);
  }

  @Override
  protected String blockedReason(ListRow row) {
    ShopEntry entry = (ShopEntry) row;
    if (selling)
      return InventoryService.count(player, entry.def.getKey()) <= entry.count
          ? I18n.key("message.insufficient_quantity")
          : null;
    return player.getGold() < basketCost() + entry.effectivePrice
        ? I18n.key("message.not_enough_gold")
        : null;
  }

  @Override
  protected String rowInfoText(ListRow row) {
    ShopEntry entry = (ShopEntry) row;
    String text = com.perso.T4C.gui.widget.ItemTooltipText.build(entry.def.getKey());
    int firstLine = text.indexOf('\n');
    return firstLine < 0 ? null : text.substring(firstLine + 1);
  }

  private static final class ShopEntry implements ListRow {
    final ItemDefinition def;
    final long effectivePrice;
    int count;

    ShopEntry(ItemDefinition d, long p) {
      def = d;
      effectivePrice = p;
    }

    @Override
    public String nameText() {
      return I18n.resolve(def.getName());
    }

    @Override
    public String priceText() {
      return String.valueOf(effectivePrice);
    }

    @Override
    public String thirdColumnText() {
      return String.valueOf(count);
    }

    @Override
    public String iconSprite() {
      return ItemIconRegistry.iconFor(def);
    }

    @Override
    public int getCount() {
      return count;
    }
  }
}
