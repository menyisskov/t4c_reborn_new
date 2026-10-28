package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.perso.T4C.combat.ArmorClassRules;
import com.perso.T4C.gui.core.CharacterPanel;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiInventory;
import com.perso.T4C.gui.widget.GuiPlayerPart;
import com.perso.T4C.gui.widget.GuiPlayerPreview;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.gui.widget.ItemTooltipText;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemUseService;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.HudTooltip;
import com.perso.T4C.ui.PlayerHUD;
import com.perso.T4C.ui.SystemMessage;
import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Inventory extends CharacterPanel {
  private static final boolean DEBUG_HOVER_TEXT = false;
  private static final String DEBUG_HOVER_PLACEHOLDER = "ITEM HOVER TEXT x100";
  private static final Rectangle CHARACTER_PREVIEW = new Rectangle(123, 176, 120, 168);

  private enum DragSource {
    INVENTORY,
    EQUIPPED
  }

  private static final class DragState {
    private DragSource source;
    private GuiInventory sourceInventory;
    private int inventoryIndex = -1;
    private BodyPart sourcePart;
    private String itemName;
    private TextureRegion region;
    private float offsetX;
    private float offsetY;
    private float screenX;
    private float screenY;
  }

  private final Player player;
  private final PlayerHUD hud;
  private GuiPlayerPreview playerPreview;
  private DragState dragState;
  private long lastClickTimeMs = 0;
  private int lastClickInventoryIndex = -1;
  private final HudTooltip tooltip = new HudTooltip();
  private String hoverItemText = DEBUG_HOVER_TEXT ? DEBUG_HOVER_PLACEHOLDER : "";
  private int selectedInventoryIndex = -1;
  private String selectedItemName;
  private GuiButton macroButton;

  public Inventory(Player player) {
    this(player, null);
  }

  public Inventory(Player player, PlayerHUD hud) {
    super(player, hud, "ui.journal.inventory", "inventory");
    this.player = player;
    this.hud = hud;
    addPlayerPreview();
    addPlayerParts();
    addStatLabels();
    addInventoryActionButtons();
  }

  @Override
  public void dispose() {
    tooltip.dispose();
    super.dispose();
  }

  @Override
  public void render(SpriteBatch batch) {
    refreshSelection();
    macroButton.setEnabled(canMacroItem(player, selectedItemName));
    renderPanelBackground(batch);
    theme.accentWell(batch, x + 24, y + 481, 272, 31);
    well(batch, 24, 104, 318, 340);
    well(batch, 356, 130, 340, 288);
    for (GuiPlayerPart part : playerParts) {
      theme.well(batch, part.getX(), part.getY(), part.getWidth(), part.getHeight());
    }
    // A quiet inset behind the rotating character keeps the silhouette readable.
    well(
        batch,
        CHARACTER_PREVIEW.x,
        CHARACTER_PREVIEW.y,
        CHARACTER_PREVIEW.width,
        CHARACTER_PREVIEW.height);
    for (GuiAnimatedSprite sprite : animatedSprites) {
      sprite.render(batch);
    }
    for (GuiPlayerPreview preview : previews) {
      preview.render(batch);
    }
    for (GuiPlayerPart part : playerParts) {
      part.render(batch);
    }
    for (GuiInventory inventory : inventories) {
      inventory.render(batch);
    }
    for (GuiText label : labels) {
      label.render(batch);
    }
    for (GuiButton button : buttons) {
      button.render(batch);
    }
    renderDragItem(batch);
    tooltip.render(batch);
  }

  @Override
  public void onTouchDown(float screenX, float screenY) {
    DragState started = tryStartDrag(screenX, screenY);
    if (started != null) {
      long now = System.currentTimeMillis();
      if (started.source == DragSource.INVENTORY) {
        if (now - lastClickTimeMs < 300 && started.inventoryIndex == lastClickInventoryIndex) {
          dragState = null;
          autoEquipItem(started);
          lastClickTimeMs = 0;
          lastClickInventoryIndex = -1;
          return;
        }
        lastClickTimeMs = now;
        lastClickInventoryIndex = started.inventoryIndex;
      } else {
        lastClickTimeMs = 0;
        lastClickInventoryIndex = -1;
      }
      dragState = started;
      return;
    }
    super.onTouchDown(screenX, screenY);
  }

  @Override
  public void onTouchDown(float screenX, float screenY, int button) {
    if (button == Input.Buttons.RIGHT
        && (showArmorClassTooltipAt(screenX, screenY) || showItemTooltipAt(screenX, screenY))) {
      return;
    }
    if (button == Input.Buttons.LEFT) {
      tooltip.clear();
    }
    onTouchDown(screenX, screenY);
  }

  private void autoEquipItem(DragState drag) {
    if (player == null || drag == null || drag.itemName == null) {
      return;
    }
    ItemDefinition def = ItemDefinition.get(drag.itemName);
    if (def == null) {
      return;
    }
    BodyPart target = def.getBodyPart();
    if (target == null) {
      ItemUseService.Result use =
          ItemUseService.useOnSelf(
              player, drag.itemName, java.util.concurrent.ThreadLocalRandom.current());
      if (use.success()) PlayerHUD.persistenceOf(hud).save(player);
      else SystemMessage.showShared(ItemUseService.failureMessage(use, drag.itemName));
      return;
    }
    InventoryService.Result result = equipAutomatically(player, drag.itemName);
    if (!result.success()) {
      showEquipFailure(result, drag.itemName);
      return;
    }
    setPartAppearance(target, drag.itemName);
    if (player.getAnimations() != null) {
      player.getAnimations().refresh();
    }
    PlayerHUD.persistenceOf(hud).save(player);
  }

  /** Equipment dragged onto the preview uses its declared slot and the normal equip rules. */
  static InventoryService.Result equipAutomatically(Player player, String itemKey) {
    ItemDefinition definition = ItemDefinition.get(itemKey);
    BodyPart target = definition == null ? null : definition.getBodyPart();
    if (player != null
        && target == BodyPart.RING1
        && player.getEquippedItems().get(BodyPart.RING1) != null
        && player.getEquippedItems().get(BodyPart.RING2) == null) target = BodyPart.RING2;
    return InventoryService.equip(player, target, itemKey);
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

  @Override
  public void onMouseMove(float screenX, float screenY) {
    if (dragState != null) {
      dragState.screenX = screenX;
      dragState.screenY = screenY;
      return;
    }
    showItemHoverText(screenX, screenY);
    super.onMouseMove(screenX, screenY);
  }

  private void showItemHoverText(float screenX, float screenY) {
    GuiInventory inv = findInventoryAt(screenX, screenY);
    if (inv == null) {
      clearHoverItemText();
      return;
    }
    GuiInventory.ItemHit hit = inv.hitTest(screenX, screenY);
    if (hit == null) {
      clearHoverItemText();
      tooltip.clear();
      return;
    }
    int count = 0;
    for (String item : player.getInventory()) if (hit.getItemName().equals(item)) count++;
    ItemDefinition def = ItemDefinition.get(hit.getItemName());
    String name = def != null && def.getName() != null ? def.getName() : hit.getItemName();
    name = I18n.resolve(name);
    hoverItemText = name + (count > 1 ? " x" + count : "");
  }

  private void clearHoverItemText() {
    hoverItemText = DEBUG_HOVER_TEXT ? DEBUG_HOVER_PLACEHOLDER : "";
  }

  private void refreshSelection() {
    if (selectedItemName == null || player == null) {
      return;
    }
    var inventory = player.getInventory();
    if (selectedInventoryIndex >= 0
        && selectedInventoryIndex < inventory.size()
        && selectedItemName.equals(inventory.get(selectedInventoryIndex))) {
      return;
    }
    int index = inventory.indexOf(selectedItemName);
    if (index < 0) {
      selectedInventoryIndex = -1;
      selectedItemName = null;
    } else {
      selectedInventoryIndex = index;
    }
  }

  private String selectedItemLabel() {
    if (selectedItemName == null) {
      return "";
    }
    ItemDefinition def = ItemDefinition.get(selectedItemName);
    String name = def != null && def.getName() != null ? def.getName() : selectedItemName;
    return I18n.resolve(name);
  }

  private void addInventoryActionButtons() {
    macroButton =
        theme.button(
            x + 358,
            y + 428,
            164,
            26,
            () ->
                I18n.key(
                    hasItemMacro(player, selectedItemName)
                        ? "ui.inventory.macro_remove"
                        : "ui.inventory.macro_add"),
            () -> {
              if (toggleItemMacro(player, selectedItemName))
                PlayerHUD.persistenceOf(hud).save(player);
            });
    buttons.add(macroButton);
    buttons.add(
        theme.button(
            x + 114,
            y + 300,
            132,
            26,
            () -> I18n.key("wardrobe.open"),
            () -> GuiManager.open(new RobeWardrobeScreen(player, hud))));
    buttons.add(
        theme.button(x + 532, y + 428, 76, 26, () -> I18n.key("ui.drop"), this::dropSelectedItem));
    buttons.add(
        theme.button(x + 616, y + 428, 76, 26, () -> I18n.key("ui.junk"), this::junkSelectedItem));
    value(this::selectedItemLabel, 358, 455, 336, 18, 11, JournalTheme.GOLD);
  }

  private static boolean canMacroItem(Player player, String itemKey) {
    var item = com.perso.T4C.item.ItemRegistry.findByKey(itemKey);
    return player != null
        && item != null
        && !item.getSpells().isEmpty()
        && InventoryService.count(player, itemKey) > 0;
  }

  private static boolean hasItemMacro(Player player, String itemKey) {
    if (player == null || itemKey == null) return false;
    String key = ItemDefinition.normalizeKey(itemKey);
    return player.getMacros().stream()
        .anyMatch(
            macro -> macro.isItem() && key.equals(ItemDefinition.normalizeKey(macro.getItemKey())));
  }

  static boolean toggleItemMacro(Player player, String itemKey) {
    if (!canMacroItem(player, itemKey)) return false;
    String key = ItemDefinition.normalizeKey(itemKey);
    if (!player
        .getMacros()
        .removeIf(
            macro ->
                macro.isItem() && key.equals(ItemDefinition.normalizeKey(macro.getItemKey())))) {
      player.getMacros().add(com.perso.T4C.config.MacroBinding.forItem(key));
    }
    return true;
  }

  private int countOwned(String itemName) {
    int count = 0;
    for (String item : player.getInventory()) if (itemName.equals(item)) count++;
    return count;
  }

  private void dropSelectedItem() {
    if (player == null || selectedItemName == null) {
      SystemMessage.showShared(I18n.message("message.inventory_select_item_first"));
      return;
    }
    int owned = countOwned(selectedItemName);
    if (owned <= 1) {
      if (player.dropInventoryItem(selectedInventoryIndex, selectedItemName)) {
        selectedInventoryIndex = -1;
        selectedItemName = null;
      }
      return;
    }
    GuiManager.open(
        new ItemQuantityConfirmScreen(
            "message.drop_confirm", selectedItemLabel(), owned, this::performDrop, this::reopen));
  }

  private void performDrop(int quantity) {
    if (player != null && selectedItemName != null) {
      String itemName = selectedItemName;
      String label = selectedItemLabel();
      int dropped = 0;
      for (int i = 0; i < quantity; i++) {
        if (!player.dropInventoryItem(selectedInventoryIndex, itemName)) break;
        dropped++;
      }
      if (dropped > 0) {
        SystemMessage.showShared(I18n.message("message.items_dropped", dropped, label));
      }
    }
    selectedInventoryIndex = -1;
    selectedItemName = null;
    reopen();
  }

  private void junkSelectedItem() {
    if (player == null || selectedItemName == null) {
      SystemMessage.showShared(I18n.message("message.inventory_select_item_first"));
      return;
    }
    ItemDefinition def = ItemDefinition.get(selectedItemName);
    if (def != null && def.isUndroppable()) {
      SystemMessage.showShared(I18n.key("message.gem_of_destiny_undroppable"));
      return;
    }
    GuiManager.open(
        new ItemQuantityConfirmScreen(
            "message.junk_confirm",
            selectedItemLabel(),
            countOwned(selectedItemName),
            this::performJunk,
            this::reopen));
  }

  private void performJunk(int quantity) {
    if (player != null && selectedItemName != null) {
      String itemName = selectedItemName;
      String label = selectedItemLabel();
      int junked = 0;
      for (int i = 0; i < quantity; i++) {
        InventoryService.Result removed =
            InventoryService.remove(player, selectedInventoryIndex, itemName);
        if (!removed.success()) break;
        junked++;
      }
      if (junked > 0) {
        SystemMessage.showShared(
            junked == 1
                ? I18n.message("message.item_junked", label)
                : I18n.message("message.items_junked", junked, label));
        PlayerHUD.persistenceOf(hud).save(player);
      }
    }
    selectedInventoryIndex = -1;
    selectedItemName = null;
    reopen();
  }

  private void reopen() {
    GuiManager.open(new Inventory(player, hud));
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    return super.onKeyDown(keycode);
  }

  private void addStatLabels() {
    text("ui.journal.equipment", 36, 110, 170, 20, 13, JournalTheme.GOLD);
    value(player::getName, 128, 338, 110, 23, 14, JournalTheme.TEXT);
    text("ui.journal.bag", 358, 105, 130, 20, 13, JournalTheme.GOLD);
    value(
        () ->
            InventoryService.currentWeight(player) + " / " + InventoryService.maximumWeight(player),
        520,
        105,
        174,
        20,
        12,
        JournalTheme.DIM);
    value(
        () ->
            I18n.key("ui.armor_class")
                + "  "
                + (int) Math.floor(ArmorClassRules.effectiveArmorClass(player)),
        356,
        430,
        170,
        20,
        12,
        JournalTheme.TEXT);
    text("ui.gold", 34, 487, 60, 20, 11, JournalTheme.GOLD);
    value(
        () -> String.format(java.util.Locale.ROOT, "%,d", player.getGold()),
        98,
        483,
        192,
        27,
        20,
        JournalTheme.GOLD);
    value(
        () -> hoverItemText.isEmpty() ? I18n.key("ui.journal.inventory_hint") : hoverItemText,
        306,
        487,
        386,
        20,
        10,
        JournalTheme.DIM);
    java.util.function.IntSupplier[] stats = {
      player::getEffectiveStrength,
      player::getEffectiveEndurance,
      player::getEffectiveDexterity,
      player::getEffectiveWisdom,
      player::getEffectiveIntelligence
    };
    String[] keys = {"str", "end", "dex", "wis", "int"};
    for (int i = 0; i < keys.length; i++) {
      int index = i;
      text("ui.journal.short." + keys[i], 32 + i * 62, 447, 60, 13, 10, JournalTheme.DIM);
      value(
          () -> String.valueOf(stats[index].getAsInt()),
          32 + i * 62,
          461,
          60,
          16,
          12,
          JournalTheme.TEXT);
    }
  }

  private void addPlayerPreview() {
    playerPreview = new GuiPlayerPreview(player, x + 179, y + 257);
    previews.add(playerPreview);
  }

  private void addPlayerParts() {
    inventories.add(new GuiInventory(player, x + 361, y + 135, 330, 278).withGrid(40, theme));
    addPart(BodyPart.HEAD, 155, 134, 56, 40);
    addPart(BodyPart.NECK, 95, 134, 42, 40);
    addPart(BodyPart.BRACER, 229, 134, 42, 40);
    addPart(BodyPart.RING1, 41, 134, 42, 40);
    addPart(BodyPart.RING2, 285, 134, 42, 40);
    addPart(BodyPart.BODY, 41, 186, 66, 90);
    addPart(BodyPart.BACK, 257, 186, 66, 90);
    addPart(BodyPart.WEAPON, 41, 287, 66, 90);
    addPart(BodyPart.SHIELD, 257, 287, 66, 90);
    addPart(BodyPart.FEET, 41, 389, 58, 45);
    addPart(BodyPart.LEFT_HAND, 108, 389, 42, 45);
    addPart(BodyPart.LEGS, 157, 369, 54, 65);
    addPart(BodyPart.BELT, 220, 389, 42, 45);
  }

  private void addPart(BodyPart part, float dx, float dy, float w, float h) {
    playerParts.add(new GuiPlayerPart(player, part, x + dx, y + dy, w, h));
  }

  private DragState tryStartDrag(float screenX, float screenY) {
    DragState fromPart = tryStartDragFromPart(screenX, screenY);
    if (fromPart != null) {
      return fromPart;
    }
    return tryStartDragFromInventory(screenX, screenY);
  }

  private DragState tryStartDragFromPart(float screenX, float screenY) {
    if (player == null || playerParts.isEmpty()) {
      return null;
    }
    for (int i = playerParts.size() - 1; i >= 0; i--) {
      GuiPlayerPart part = playerParts.get(i);
      if (!part.contains(screenX, screenY)) {
        continue;
      }
      BodyPart bodyPart = part.occupiedPart();
      String itemName = part.equippedItem();
      if (itemName == null || itemName.isEmpty()) {
        continue;
      }
      TextureRegion region = resolveInventoryRegion(itemName);
      if (region == null) {
        String fallback =
            player.getAnimations() == null
                ? null
                : player.getAnimations().getPartMap().get(bodyPart);
        region = GuiSprites.load(fallback);
      }
      if (region == null) {
        continue;
      }
      DragState state = new DragState();
      state.source = DragSource.EQUIPPED;
      state.sourcePart = bodyPart;
      state.itemName = itemName;
      state.region = region;
      state.offsetX = screenX - part.getX();
      state.offsetY = screenY - part.getY();
      state.screenX = screenX;
      state.screenY = screenY;
      return state;
    }
    return null;
  }

  private DragState tryStartDragFromInventory(float screenX, float screenY) {
    GuiInventory inventory = findInventoryAt(screenX, screenY);
    if (inventory == null) {
      return null;
    }
    GuiInventory.ItemHit hit = inventory.hitTest(screenX, screenY);
    if (hit == null) {
      return null;
    }
    DragState state = new DragState();
    state.source = DragSource.INVENTORY;
    state.sourceInventory = inventory;
    state.inventoryIndex = hit.getIndex();
    state.itemName = hit.getItemName();
    state.region = hit.getRegion();
    state.offsetX = screenX - hit.getX();
    state.offsetY = screenY - hit.getY();
    state.screenX = screenX;
    state.screenY = screenY;
    return state;
  }

  private void handleDrop(float screenX, float screenY, DragState drag) {
    if (player == null || drag == null) {
      return;
    }
    float fallbackW = drag.region == null ? 32f : drag.region.getRegionWidth();
    float fallbackH = drag.region == null ? 32f : drag.region.getRegionHeight();
    int quickSlot = hud == null ? 0 : hud.getQuickSlotAt((int) screenX, (int) screenY);
    if (drag.source == DragSource.INVENTORY && quickSlot > 0 && isPotion(drag.itemName)) {
      hud.assignItemToQuickSlot(drag.itemName, quickSlot);
      PlayerHUD.persistenceOf(hud).save(player);
      return;
    }
    if (CHARACTER_PREVIEW.contains(screenX - x, screenY - y)) {
      ItemDefinition definition = ItemDefinition.get(drag.itemName);
      if (drag.source == DragSource.INVENTORY
          && definition != null
          && definition.getBodyPart() != null) autoEquipItem(drag);
      return;
    }
    GuiPlayerPart targetPart = findPlayerPartDropTarget(screenX, screenY, fallbackW, fallbackH);
    boolean appearanceChanged = false;
    if (targetPart != null) {
      BodyPart target = targetPart.getPart();
      if (drag.source == DragSource.EQUIPPED && target == drag.sourcePart) {
        return;
      }
      boolean movingRing =
          drag.source == DragSource.EQUIPPED
              && (drag.sourcePart == BodyPart.RING1 || drag.sourcePart == BodyPart.RING2)
              && (target == BodyPart.RING1 || target == BodyPart.RING2);
      if (!movingRing && !canEquip(target, drag.itemName)) {
        return;
      }
      if (drag.source == DragSource.INVENTORY) {
        InventoryService.Result result = InventoryService.equip(player, target, drag.itemName);
        if (!result.success()) {
          showEquipFailure(result, drag.itemName);
          return;
        }
      } else if (drag.source == DragSource.EQUIPPED) {
        player.getEquippedItems().remove(drag.sourcePart);
        appearanceChanged |= setPartAppearance(drag.sourcePart, null);
        String displaced = player.getEquippedItems().put(target, drag.itemName);
        if (displaced != null && !displaced.equals(drag.itemName))
          player.getInventory().add(displaced);
      }
      appearanceChanged |= setPartAppearance(target, drag.itemName);
      if (appearanceChanged && player.getAnimations() != null) {
        player.getAnimations().refresh();
      }
      return;
    }
    GuiInventory inventoryTarget = findInventoryAt(screenX, screenY);
    if (inventoryTarget != null) {
      if (drag.source == DragSource.EQUIPPED) {
        if (InventoryService.unequip(player, drag.sourcePart).success()) {
          appearanceChanged |= setPartAppearance(drag.sourcePart, null);
          if (appearanceChanged && player.getAnimations() != null) {
            player.getAnimations().refresh();
          }
        }
      } else if (drag.source == DragSource.INVENTORY) {
        selectedInventoryIndex = drag.inventoryIndex;
        selectedItemName = drag.itemName;
      }
      return;
    }
    if (isInsideInventoryWindow(screenX, screenY)) {
      return;
    }
    if (drag.source == DragSource.INVENTORY) {
      selectedInventoryIndex = drag.inventoryIndex;
      selectedItemName = drag.itemName;
      dropSelectedItem();
    } else if (drag.source == DragSource.EQUIPPED
        && InventoryService.unequip(player, drag.sourcePart).success()) {
      int index = player.getInventory().lastIndexOf(drag.itemName);
      player.dropInventoryItem(index, drag.itemName);
      appearanceChanged |= setPartAppearance(drag.sourcePart, null);
      if (appearanceChanged && player.getAnimations() != null) player.getAnimations().refresh();
    }
  }

  private GuiPlayerPart findPlayerPartDropTarget(
      float screenX, float screenY, float fallbackW, float fallbackH) {
    for (int i = playerParts.size() - 1; i >= 0; i--) {
      GuiPlayerPart part = playerParts.get(i);
      float w = part.getWidth() > 0f ? part.getWidth() : fallbackW;
      float h = part.getHeight() > 0f ? part.getHeight() : fallbackH;
      if (screenX >= part.getX()
          && screenX <= part.getX() + w
          && screenY >= part.getY()
          && screenY <= part.getY() + h) {
        return part;
      }
    }
    return null;
  }

  private GuiInventory findInventoryAt(float screenX, float screenY) {
    for (int i = inventories.size() - 1; i >= 0; i--) {
      GuiInventory inventory = inventories.get(i);
      if (inventory.contains(screenX, screenY)) {
        return inventory;
      }
    }
    return null;
  }

  private boolean isPotion(String itemName) {
    return itemName != null && itemName.toLowerCase(java.util.Locale.ROOT).contains("potion");
  }

  private boolean isInsideInventoryWindow(float screenX, float screenY) {
    return isBackgroundHit(screenX, screenY);
  }

  private boolean showItemTooltipAt(float screenX, float screenY) {
    String equippedItem = findEquippedItemAt(screenX, screenY);
    if (equippedItem != null) {
      tooltip.show(buildItemTooltipText(equippedItem), screenX, screenY);
      return true;
    }
    GuiInventory inventory = findInventoryAt(screenX, screenY);
    if (inventory == null) {
      return false;
    }
    GuiInventory.ItemHit hit = inventory.hitTest(screenX, screenY);
    if (hit == null || hit.getItemName() == null || hit.getItemName().isEmpty()) {
      return false;
    }
    tooltip.show(buildItemTooltipText(hit.getItemName()), screenX, screenY);
    return true;
  }

  private String findEquippedItemAt(float screenX, float screenY) {
    if (player == null || player.getEquippedItems() == null) {
      return null;
    }
    for (int i = playerParts.size() - 1; i >= 0; i--) {
      GuiPlayerPart part = playerParts.get(i);
      if (!part.contains(screenX, screenY)) {
        continue;
      }
      String itemName = part.equippedItem();
      if (itemName != null && !itemName.isEmpty()) {
        return itemName;
      }
    }
    return null;
  }

  private String buildItemTooltipText(String itemName) {
    return ItemTooltipText.build(itemName);
  }

  private static void appendLine(StringBuilder text, String slug, String value) {
    if (value == null || value.isEmpty()) {
      return;
    }
    text.append('\n').append(I18n.key("tooltip." + slug)).append(": ").append(value);
  }

  private static String formatDouble(double value) {
    if (value == Math.rint(value)) {
      return String.valueOf((long) value);
    }
    return String.format(java.util.Locale.ROOT, "%.2f", value);
  }

  private static String formatSignedDouble(double value) {
    return (value > 0d ? "+" : "") + formatDouble(value);
  }

  private boolean setPartAppearance(BodyPart part, String itemName) {
    if (player == null || player.getAnimations() == null || part == null) {
      return false;
    }
    Map<BodyPart, String> partMap = player.getAnimations().getPartMap();
    if (partMap == null) {
      return false;
    }
    Map<BodyPart, String> before = new EnumMap<>(partMap);
    PlayerAppearanceDefaults.applyDefaults(player);
    return !before.equals(partMap);
  }

  private boolean canEquip(BodyPart part, String itemName) {
    InventoryService.Result result = InventoryService.validateEquip(player, part, itemName);
    if (!result.success()) {
      showEquipFailure(result, itemName);
      return false;
    }
    return true;
  }

  private boolean showArmorClassTooltipAt(float screenX, float screenY) {
    if (player == null) return false;
    if (screenX < x + 356f || screenX > x + 526f || screenY < y + 428f || screenY > y + 454f) {
      return false;
    }
    tooltip.show(buildArmorClassTooltipText(), screenX, screenY);
    return true;
  }

  private String buildArmorClassTooltipText() {
    double equipment = ArmorClassRules.trueArmorClass(player);
    double bonus = player.getArmorClassBoost();
    double total = ArmorClassRules.effectiveArmorClass(player);
    double exampleDamage = 20d;
    double exampleResult = Math.max(0d, exampleDamage - total);
    StringBuilder text = new StringBuilder(I18n.key("ac.title"));
    appendLine(text, "equipment_ac", formatDouble(equipment));
    appendLine(text, "ac_bonus", formatSignedDouble(bonus));
    appendLine(text, "total_ac", formatDouble(total));
    text.append("\n\n").append(I18n.key("ac.reduction"));
    text.append("\n").append(I18n.key("ac.formula"));
    text.append("\n")
        .append(
            String.format(
                java.util.Locale.ROOT,
                I18n.key("ac.example"),
                formatDouble(exampleDamage),
                formatDouble(total),
                formatDouble(exampleResult)));
    text.append(I18n.key("ac.taken"));
    text.append("\n").append(I18n.key("ac.percent"));
    text.append("\n").append(I18n.key("ac.penetration"));
    return text.toString();
  }

  private void showEquipFailure(InventoryService.Result result, String itemName) {
    if (result == null || result.success()) return;
    ItemDefinition def = ItemDefinition.get(itemName);
    String message;
    switch (result.failure()) {
      case REQUIREMENTS_NOT_MET -> message = requirementFailureMessage(def);
      case INCOMPATIBLE_EQUIPMENT -> message = I18n.message("message.equip_quiver_requires_bow");
      case WRONG_SLOT -> message = I18n.message("message.equip_wrong_slot");
      case ITEM_NOT_OWNED -> message = I18n.message("message.item_not_owned");
      case UNKNOWN_ITEM -> message = I18n.message("message.item_not_equippable");
      default -> message = I18n.message("message.item_cannot_equip");
    }
    SystemMessage.showShared(message);
  }

  private String requirementFailureMessage(ItemDefinition def) {
    if (def == null) return I18n.message("message.item_cannot_equip");
    String name =
        I18n.resolve(
            def.getName() == null || def.getName().isBlank() ? def.getKey() : def.getName());
    if (player.getEndurance() < def.getMinEnd()) {
      return I18n.message("message.equip_need_endurance", def.getMinEnd(), name);
    }
    if (player.getStrength() < def.getReqStr()) {
      return I18n.message("message.equip_need_strength", def.getReqStr(), name);
    }
    if (player.getDexterity() < def.getReqAgi()) {
      return I18n.message("message.equip_need_agility", def.getReqAgi(), name);
    }
    if (player.getIntelligence() < def.getMinInt()) {
      return I18n.message("message.equip_need_intelligence", def.getMinInt(), name);
    }
    if (player.getWisdom() < def.getMinWis()) {
      return I18n.message("message.equip_need_wisdom", def.getMinWis(), name);
    }
    int attack = player.getSkillLevel("attack");
    if (attack <= 0) attack = player.getLevel() + player.getDexterity();
    if (attack < def.getReqAttack()) {
      return I18n.message("message.equip_need_attack", def.getReqAttack(), name);
    }
    return I18n.message("message.equip_requirements", name);
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
    return GuiSprites.load(sprite);
  }

  private void renderDragItem(SpriteBatch batch) {
    if (dragState == null || dragState.region == null) {
      return;
    }
    float drawX = dragState.screenX - dragState.offsetX;
    float drawY = dragState.screenY - dragState.offsetY;
    GuiDraw.drawRegionFlipped(batch, dragState.region, drawX, drawY);
  }
}
