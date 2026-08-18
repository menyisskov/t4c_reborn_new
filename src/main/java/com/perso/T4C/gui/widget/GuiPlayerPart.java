package com.perso.T4C.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiResizable;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemDurabilityService;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
import lombok.Getter;

public class GuiPlayerPart extends AbstractGuiElement implements GuiResizable {
  @Getter private final Player player;
  @Getter private final BodyPart part;
  private float zoneWidth;
  private float zoneHeight;
  private TextureRegion cachedRegion;
  private final BitmapFont durabilityFont;

  public GuiPlayerPart(Player player, BodyPart part, float x, float y) {
    this(player, part, x, y, 0f, 0f);
  }

  public GuiPlayerPart(
      Player player, BodyPart part, float x, float y, float zoneWidth, float zoneHeight) {
    super(x, y);
    this.player = player;
    this.part = part;
    this.zoneWidth = zoneWidth;
    this.zoneHeight = zoneHeight;
    this.durabilityFont = FontManager.getInstance().getJetBrainsMonoFont(10, Color.WHITE);
  }

  @Override
  public GuiPlayerPart setSize(float width, float height) {
    this.zoneWidth = Math.max(1f, width);
    this.zoneHeight = Math.max(1f, height);
    return this;
  }

  public GuiPlayerPart boxed(float width, float height) {
    return setSize(width, height);
  }

  public String equippedItem() {
    if (player == null || player.getEquippedItems() == null) {
      return null;
    }
    String itemName = player.getEquippedItems().get(part);
    if ((itemName == null || itemName.isEmpty()) && part == BodyPart.SHIELD) {
      itemName = player.getEquippedItems().get(BodyPart.WEAPON2);
    }
    return itemName;
  }

  public BodyPart occupiedPart() {
    if (part == BodyPart.SHIELD
        && player != null
        && player.getEquippedItems() != null
        && player.getEquippedItems().get(BodyPart.SHIELD) == null
        && player.getEquippedItems().get(BodyPart.WEAPON2) != null) {
      return BodyPart.WEAPON2;
    }
    return part;
  }

  public void render(SpriteBatch batch) {
    if (player == null) {
      return;
    }
    GuiBoxedItem.drawDebugBorder(batch, x, y, zoneWidth, zoneHeight);
    String itemName = equippedItem();
    if (itemName == null || itemName.isEmpty()) {
      cachedRegion = null;
      return;
    }
    String invSprite = resolveInventorySprite(itemName);
    TextureRegion region = GuiSprites.load(invSprite);
    String equippedSprite = resolveEquippedSprite(itemName);
    if (region == null && equippedSprite != null && !equippedSprite.equals(invSprite)) {
      region = GuiSprites.load(equippedSprite);
    }
    if (region == null) {
      cachedRegion = null;
      return;
    }
    cachedRegion = region;
    float drawX = zoneWidth > 0f ? x + (zoneWidth - region.getRegionWidth()) / 2f : x;
    float drawY = zoneHeight > 0f ? y + (zoneHeight - region.getRegionHeight()) / 2f : y;
    GuiDraw.drawRegionFlipped(batch, region, drawX, drawY);
    ItemDefinition definition = ItemDefinition.get(itemName);
    if (ItemDurabilityService.isRepairable(definition)) {
      double durability = ItemDurabilityService.equipped(player, occupiedPart());
      durabilityFont.setColor(
          durability >= 50 ? Color.GREEN : durability >= 25 ? Color.ORANGE : Color.RED);
      durabilityFont.draw(
          batch,
          ItemDurabilityService.format(durability) + "%",
          drawX + 1f,
          drawY + region.getRegionHeight() - 1f);
    }
  }

  private String resolveInventorySprite(String itemName) {
    try {
      ItemDefinition def = ItemDefinition.get(itemName);
      if (def == null) return itemName;
      String invSprite = def.getAppearanceInventory();
      return invSprite == null || invSprite.isEmpty() ? itemName : invSprite;
    } catch (Throwable ignored) {
      return itemName;
    }
  }

  private String resolveEquippedSprite(String itemName) {
    ItemDefinition def = ItemDefinition.get(itemName);
    if (def == null) return itemName;
    String sprite = def.getAppearanceEquippedFor(part);
    return sprite == null || sprite.isEmpty() ? itemName : sprite;
  }

  @Override
  public float getWidth() {
    if (zoneWidth > 0f) {
      return zoneWidth;
    }
    return cachedRegion == null ? 0f : cachedRegion.getRegionWidth();
  }

  @Override
  public float getHeight() {
    if (zoneHeight > 0f) {
      return zoneHeight;
    }
    return cachedRegion == null ? 0f : cachedRegion.getRegionHeight();
  }
}
