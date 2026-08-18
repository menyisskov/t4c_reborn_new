package com.perso.T4C.gui.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiClickZone;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemDurabilityService;
import com.perso.T4C.item.ItemIconRegistry;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.SystemMessage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RepairScreen extends GuiListScreen {
  private final Player player;
  private final List<RepairEntry> entries = new ArrayList<>();
  private RepairEntry selected;

  public RepairScreen(Player player) {
    this.player = player;
    try {
      background = SpriteLoader.getInstance().getRegionFromSpriteName("GUIBackBuy");
    } catch (GameException ignored) {
      background = null;
    }
    centerOnScreen();
    addCloseButton(CLOSE_X, CLOSE_Y);
    addStaticLabels();
    loadEntries();
    rebuildList();
  }

  private void addStaticLabels() {
    BitmapFont title = FontManager.getInstance().getHaettenschweilerFont(18, GOLD);
    BitmapFont small = FontManager.getInstance().getJetBrainsMonoFont(11, GOLD);
    labels.add(boxed(title, TITLE_BOX, 0, () -> I18n.key("ui.repair"), GOLD).shrinkToFit());
    labels.add(boxed(title, GOLD_HDR_BOX, 0, () -> I18n.key("ui.gold"), GOLD).shrinkToFit());
    labels.add(boxed(small, HDR_NAME_BOX, 0, () -> I18n.key("ui.item_name"), GOLD));
    labels.add(boxed(small, HDR_PRICE_BOX, 0, () -> I18n.key("ui.price"), GOLD));
    labels.add(boxed(small, HDR_THIRD_BOX, 0, () -> "%", GOLD));
    labels.add(boxed(small, ONHAND_LBL_BOX, 0, () -> I18n.key("ui.on_hand"), GOLD));
    labels.add(boxed(small, COST_LBL_BOX, 0, () -> I18n.key("ui.cost"), GOLD));
    labels.add(boxed(small, TOTAL_LBL_BOX, 0, () -> I18n.key("ui.total"), GOLD));
  }

  private void loadEntries() {
    entries.clear();
    ItemDurabilityService.synchronize(player);
    for (int i = 0; i < player.getInventory().size(); i++) {
      ItemDefinition item = ItemRegistry.findByKey(player.getInventory().get(i));
      double durability = ItemDurabilityService.inventory(player, i);
      if (ItemDurabilityService.repairCost(item, durability) > 0)
        entries.add(RepairEntry.inventory(item, i, durability));
    }
    Set<BodyPart> seen = new HashSet<>();
    for (BodyPart slot : player.getEquippedItems().keySet()) {
      BodyPart primary = ItemDurabilityService.primarySlot(player, slot);
      if (!seen.add(primary)) continue;
      ItemDefinition item = ItemRegistry.findByKey(player.getEquippedItems().get(primary));
      double durability = ItemDurabilityService.equipped(player, primary);
      if (ItemDurabilityService.repairCost(item, durability) > 0)
        entries.add(RepairEntry.equipped(item, primary, durability));
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
    int pages = Math.max(1, (entries.size() + ROWS_VISIBLE - 1) / ROWS_VISIBLE);
    page = Math.min(page, pages - 1);
    BitmapFont white = FontManager.getInstance().getJetBrainsMonoFont(12, WHITE);
    BitmapFont gold = FontManager.getInstance().getJetBrainsMonoFont(12, GOLD);
    int start = page * ROWS_VISIBLE, end = Math.min(entries.size(), start + ROWS_VISIBLE);
    for (int i = start; i < end; i++) {
      RepairEntry entry = entries.get(i);
      float rowY = ROW_0_Y + (i - start) * ROW_H_PITCH;
      var icon = GuiSprites.load(ItemIconRegistry.iconFor(entry.item));
      if (icon == null) icon = GuiSprites.load(entry.item.getAppearanceInventory());
      if (icon != null)
        animatedSprites.add(
            new GuiAnimatedSprite(List.of(icon), x + ICON_BOX[0], y + rowY + ICON_BOX[1], 1f)
                .boxed(ICON_BOX[2], ICON_BOX[3]));
      boolean chosen = entry == selected;
      addDyn(chosen ? gold : white, CELL_NAME, rowY, entry::nameText, chosen ? GOLD : WHITE);
      addDyn(white, CELL_PRICE, rowY, entry::priceText, WHITE);
      addDyn(
          entry.durability < 25
              ? FontManager.getInstance().getJetBrainsMonoFont(12, Color.RED)
              : white,
          CELL_THIRD,
          rowY,
          entry::thirdColumnText,
          entry.durability < 25 ? Color.RED : WHITE);
      zones.add(
          new GuiClickZone(
              x + 10f,
              y + rowY - 4f,
              360f,
              ROW_H_PITCH - 4f,
              () -> {
                selected = entry;
                rebuildList();
              }));
    }
    addScrollThumb(pages);
    long cost = selected == null ? ItemDurabilityService.repairAllCost(player) : selected.cost();
    addDyn(gold, ONHAND_VAL_BOX, 0, () -> String.valueOf(player.getGold()), GOLD);
    addDyn(gold, COST_VAL_BOX, 0, () -> String.valueOf(cost), GOLD);
    addDyn(
        gold, TOTAL_VAL_BOX, 0, () -> String.valueOf(Math.max(0, player.getGold() - cost)), GOLD);
    addActionButton(cost > 0 && player.getGold() >= cost);
  }

  private void addActionButton(boolean enabled) {
    var normal = GuiSprites.load(enabled ? "GUI_ButtonUp" : "GUI_ButtonDisabled");
    if (normal == null) return;
    GuiButton button =
        new GuiButton(
                normal,
                normal,
                normal,
                x + ACTION_BTN_X,
                y + ACTION_BTN_Y,
                enabled ? this::repair : null)
            .withLabel(
                FontManager.getInstance().getHaettenschweilerFont(13, enabled ? GOLD : DIM),
                () -> selected == null ? I18n.key("ui.repair_all") : I18n.key("ui.repair"));
    buttons.add(button);
    dynButtons.add(button);
  }

  private void repair() {
    boolean success =
        selected == null
            ? ItemDurabilityService.repairAll(player)
            : selected.inventoryIndex >= 0
                ? ItemDurabilityService.repairInventory(player, selected.inventoryIndex)
                : ItemDurabilityService.repairEquipped(player, selected.slot);
    if (!success) {
      SystemMessage.showShared(I18n.message("message.not_enough_gold"));
      return;
    }
    PlayerStateStore.save(player);
    selected = null;
    loadEntries();
    rebuildList();
  }

  @Override
  protected List<? extends ListRow> rows() {
    return entries;
  }

  @Override
  protected void basketAdd(ListRow row) {
    selected = (RepairEntry) row;
    rebuildList();
  }

  @Override
  protected void basketRemove(ListRow row) {
    selected = null;
    rebuildList();
  }

  private static final class RepairEntry implements ListRow {
    final ItemDefinition item;
    final int inventoryIndex;
    final BodyPart slot;
    final double durability;

    private RepairEntry(ItemDefinition item, int index, BodyPart slot, double durability) {
      this.item = item;
      this.inventoryIndex = index;
      this.slot = slot;
      this.durability = durability;
    }

    static RepairEntry inventory(ItemDefinition item, int index, double durability) {
      return new RepairEntry(item, index, null, durability);
    }

    static RepairEntry equipped(ItemDefinition item, BodyPart slot, double durability) {
      return new RepairEntry(item, -1, slot, durability);
    }

    long cost() {
      return ItemDurabilityService.repairCost(item, durability);
    }

    @Override
    public String nameText() {
      return I18n.resolve(item.getName());
    }

    @Override
    public String priceText() {
      return String.valueOf(cost());
    }

    @Override
    public String thirdColumnText() {
      return ItemDurabilityService.format(durability) + "%";
    }

    @Override
    public String iconSprite() {
      return ItemIconRegistry.iconFor(item);
    }

    @Override
    public int getCount() {
      return 0;
    }
  }
}
