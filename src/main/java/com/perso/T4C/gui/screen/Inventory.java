package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.combat.ArmorClassRules;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiInventory;
import com.perso.T4C.gui.widget.GuiPlayerPart;
import com.perso.T4C.gui.widget.GuiPlayerPreview;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.gui.widget.ItemTooltipText;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemDurabilityService;
import com.perso.T4C.item.ItemUseService;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.HudTooltip;
import com.perso.T4C.ui.PlayerHUD;
import com.perso.T4C.ui.SystemMessage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Inventory extends GuiScreenBase {
  private static final boolean DEBUG_HOVER_TEXT = false;
  private static final String DEBUG_HOVER_PLACEHOLDER = "ITEM HOVER TEXT x100";

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
  private TextureRegion backgroundTop;
  private TextureRegion backgroundBottom;
  private float totalWidth;
  private float totalHeight;
  private GuiPlayerPreview playerPreview;
  private DragState dragState;
  private long lastClickTimeMs = 0;
  private int lastClickInventoryIndex = -1;
  private final HudTooltip tooltip = new HudTooltip();
  private String hoverItemText = DEBUG_HOVER_TEXT ? DEBUG_HOVER_PLACEHOLDER : "";

  public Inventory(Player player) {
    this(player, null);
  }

  public Inventory(Player player, PlayerHUD hud) {
    this.player = player;
    this.hud = hud;
    loadBackgrounds();
    centerOnScreen();
    addCloseButton();
    addHeader();
    addPlayerPreview();
    addPlayerParts();
    addStatLabels();
  }

  private void loadBackgrounds() {
    backgroundTop =
        loadFirstExisting("GUI_InvBackT", "GUI_InvBackTop", "GUI_BackInvT", "GUI_BackInventoryTop");
    backgroundBottom =
        loadFirstExisting(
            "GUI_InvBackB", "GUI_InvBackBottom", "GUI_BackInvB", "GUI_BackInventoryBottom");
    if (backgroundTop == null && backgroundBottom == null) {
      backgroundTop = loadFirstExisting("GUI_BackInv", "GUI_BackInventory", "GUI_InventoryBack");
    }
    if (backgroundTop == null && backgroundBottom == null) {
      log.warn(
          "Inventory background not found. Tried known sprite names: {}",
          Arrays.asList(
              "GUI_InvBackT",
              "GUI_InvBackTop",
              "GUI_BackInvT",
              "GUI_BackInventoryTop",
              "GUI_InvBackB",
              "GUI_InvBackBottom",
              "GUI_BackInvB",
              "GUI_BackInventoryBottom",
              "GUI_BackInv",
              "GUI_BackInventory",
              "GUI_InventoryBack"));
    }
    background = backgroundTop != null ? backgroundTop : backgroundBottom;
    float widthTop = backgroundTop == null ? 0f : backgroundTop.getRegionWidth();
    float widthBottom = backgroundBottom == null ? 0f : backgroundBottom.getRegionWidth();
    float heightTop = backgroundTop == null ? 0f : backgroundTop.getRegionHeight();
    float heightBottom = backgroundBottom == null ? 0f : backgroundBottom.getRegionHeight();
    totalWidth = Math.max(widthTop, widthBottom);
    totalHeight = heightTop + heightBottom;
  }

  private TextureRegion loadFirstExisting(String... names) {
    try {
      var loader = SpriteLoader.getInstance();
      for (String name : names) {
        TextureRegion region = loader.getRegionFromSpriteName(name);
        if (region != null) {
          return region;
        }
      }
      return null;
    } catch (GameException e) {
      log.warn("Failed to load inventory background sprite", e);
      return null;
    }
  }

  @Override
  protected void centerOnScreen() {
    if (totalWidth <= 0f || totalHeight <= 0f) {
      return;
    }
    x = (Gdx.graphics.getWidth() - totalWidth) / 2f;
    y = (Gdx.graphics.getHeight() - totalHeight) / 2f;
  }

  @Override
  protected boolean isBackgroundHit(float screenX, float screenY) {
    return totalWidth > 0f
        && totalHeight > 0f
        && screenX >= x
        && screenX <= x + totalWidth
        && screenY >= y
        && screenY <= y + totalHeight;
  }

  @Override
  public void render(SpriteBatch batch) {
    if (backgroundTop != null) {
      GuiDraw.drawOverlayRegionFlipped(batch, backgroundTop, x, y);
    }
    if (backgroundBottom != null) {
      float topHeight = backgroundTop == null ? 0f : backgroundTop.getRegionHeight();
      GuiDraw.drawOverlayRegionFlipped(batch, backgroundBottom, x, y + topHeight);
    }
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
      if (use.success()) PlayerStateStore.save(player);
      else if (use.failure() == ItemUseService.Failure.NO_HEALING_NEEDED) {
        SystemMessage.showShared(I18n.message("message.no_healing_needed"));
      } else if (use.failure() == ItemUseService.Failure.NO_MANA_NEEDED) {
        SystemMessage.showShared(I18n.message("message.no_mana_needed"));
      }
      return;
    }
    if (target == BodyPart.RING1 && player.getEquippedItems().get(BodyPart.RING1) != null) {
      if (player.getEquippedItems().get(BodyPart.RING2) == null) {
        target = BodyPart.RING2;
      }
    }
    if (!canEquip(target, drag.itemName)) {
      return;
    }
    InventoryService.Result result = InventoryService.equip(player, target, drag.itemName);
    if (!result.success()) {
      showEquipFailure(result, drag.itemName);
      return;
    }
    setPartAppearance(target, drag.itemName);
    if (player.getAnimations() != null) {
      player.getAnimations().refresh();
    }
    PlayerStateStore.save(player);
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

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    return super.onKeyDown(keycode);
  }

  private void addCloseButton() {
    addCloseButton(525f, 0f);
  }

  private void addHeader() {
    if (backgroundTop == null && backgroundBottom == null) {
      return;
    }
    var gold = Color.valueOf("F2B705");
    labels.add(
        new GuiBoxedText(
                FontManager.getInstance().getHaettenschweilerFont(18, gold),
                x + 224f,
                y + 2f,
                102f,
                19f,
                () -> I18n.key("ui.inventory"),
                () -> gold)
            .shrinkToFit());
  }

  private void addStatLabels() {
    if (player == null) {
      return;
    }
    BitmapFont font = FontManager.getInstance().getJetBrainsMonoFont(14, Color.WHITE);
    var gold = Color.valueOf("F2B705");
    labels.add(
        new GuiBoxedText(
            font,
            x + 352f,
            y + 96f,
            48f,
            17f,
            () -> String.valueOf(player.getStrength()),
            () -> Color.WHITE));
    labels.add(
        new GuiBoxedText(
            font,
            x + 352f,
            y + 130f,
            48f,
            17f,
            () -> String.valueOf(player.getEndurance()),
            () -> Color.WHITE));
    labels.add(
        new GuiBoxedText(
            font,
            x + 352f,
            y + 164f,
            48f,
            17f,
            () -> String.valueOf(player.getDexterity()),
            () -> Color.WHITE));
    labels.add(
        new GuiBoxedText(
            font,
            x + 352f,
            y + 198f,
            48f,
            17f,
            () -> String.valueOf(player.getWisdom()),
            () -> Color.WHITE));
    labels.add(
        new GuiBoxedText(
            font,
            x + 352f,
            y + 232f,
            48f,
            17f,
            () -> String.valueOf(player.getIntelligence()),
            () -> Color.WHITE));
    labels.add(
        new GuiBoxedText(
            font,
            x + 352f,
            y + 51f,
            48f,
            17f,
            () -> String.valueOf((int) Math.floor(ArmorClassRules.effectiveArmorClass(player))),
            () ->
                ArmorClassRules.effectiveArmorClass(player) > ArmorClassRules.trueArmorClass(player)
                    ? Color.GREEN
                    : Color.WHITE));
    labels.add(
        new GuiBoxedText(font, x + 424f, y + 51f, 101f, 16f, player::getName, () -> Color.WHITE)
            .shrinkToFit());
    labels.add(
        new GuiBoxedText(
                FontManager.getInstance().getHaettenschweilerFont(18, gold),
                x + 28f,
                y + 397f,
                55f,
                18f,
                () -> I18n.key("ui.gold"),
                () -> gold)
            .shrinkToFit());
    BitmapFont infoFont = FontManager.getInstance().getJetBrainsMonoFont(11, gold);
    labels.add(
        new GuiBoxedText(
            infoFont,
            x + 87f,
            y + 397f,
            89f,
            18f,
            () -> String.valueOf(player.getGold()),
            () -> gold));
    labels.add(
        new GuiBoxedText(infoFont, x + 206f, y + 397f, 248f, 18f, () -> hoverItemText, () -> gold));
  }

  private void addPlayerPreview() {
    if (player == null) {
      return;
    }
    playerPreview = new GuiPlayerPreview(player, x + 456f, y + 197f);
    previews.add(playerPreview);
  }

  private void addPlayerParts() {
    if (player == null) {
      return;
    }
    inventories.add(new GuiInventory(player, x + 25f, y + 272f, 402f, 113f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.BACK, x + 227f, y + 97f, 60f, 88f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.HEAD, x + 85f, y + 40f, 60f, 50f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.RING1, x + 204f, y + 46f, 36f, 36f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.RING2, x + 254f, y + 46f, 36f, 36f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.BELT, x + 156f, y + 205f, 36f, 36f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.NECK, x + 40f, y + 46f, 36f, 36f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.BRACER, x + 156f, y + 46f, 36f, 36f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.LEGS, x + 85f, y + 193f, 60f, 58f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.BODY, x + 85f, y + 97f, 60f, 88f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.FEET, x + 14f, y + 193f, 60f, 58f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.SHIELD, x + 156f, y + 97f, 60f, 88f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.LEFT_HAND, x + 200f, y + 200f, 40f, 40f));
    playerParts.add(new GuiPlayerPart(player, BodyPart.WEAPON, x + 14f, y + 97f, 60f, 88f));
    System.out.println("Inventory: GuiPlayerPart count=" + playerParts.size());
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
      PlayerStateStore.save(player);
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
      }
      return;
    }
    if (isInsideInventoryWindow(screenX, screenY)) {
      return;
    }
    if (drag.source == DragSource.INVENTORY) {
      player.dropInventoryItem(drag.inventoryIndex, drag.itemName);
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
    return totalWidth > 0f
        && totalHeight > 0f
        && screenX >= x
        && screenX <= x + totalWidth
        && screenY >= y
        && screenY <= y + totalHeight;
  }

  private boolean showItemTooltipAt(float screenX, float screenY) {
    String equippedItem = findEquippedItemAt(screenX, screenY);
    if (equippedItem != null) {
      BodyPart equippedSlot = findEquippedSlotAt(screenX, screenY);
      tooltip.show(
          buildItemTooltipText(
              equippedItem,
              equippedSlot == null ? 100d : ItemDurabilityService.equipped(player, equippedSlot)),
          screenX,
          screenY);
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
    tooltip.show(
        buildItemTooltipText(
            hit.getItemName(), ItemDurabilityService.inventory(player, hit.getIndex())),
        screenX,
        screenY);
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

  private BodyPart findEquippedSlotAt(float screenX, float screenY) {
    for (int i = playerParts.size() - 1; i >= 0; i--) {
      GuiPlayerPart part = playerParts.get(i);
      if (part.contains(screenX, screenY) && part.equippedItem() != null)
        return part.occupiedPart();
    }
    return null;
  }

  private String buildItemTooltipText(String itemName, double durability) {
    return ItemTooltipText.build(itemName, durability);
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
    if (screenX < x + 340f || screenX > x + 415f || screenY < y + 35f || screenY > y + 78f) {
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
