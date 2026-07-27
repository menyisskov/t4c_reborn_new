package com.perso.T4C.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.perso.T4C.MyGame;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.model.QuickSlotEntry;
import com.perso.T4C.player.Player;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Heads-up display for the player showing HP/MP/XP bars.
 */
public class PlayerHUD {
    private static final float BUFF_BAR_X = 0f;
    private static final float BUFF_BAR_Y = 80f;
    private static final String COMBAT_ICON_SPRITE = "64kCursorAttack";
    private static final float COMBAT_ICON_MARGIN = 16f;

    private TextureRegion hpBar;
    private TextureRegion mpBar;
    private TextureRegion xpBar;
    private TextureRegion frame;
    private TextureRegion quickSlotFrame;
    private TextureRegion buffBackground;
    private TextureRegion combatModeIcon;
    private final Player player;
    private final BitmapFont font;
    private final HudTooltip tooltip;
    private final SpriteLoader spriteLoader;
    private int lastTextureGen;
    private final int quickSlotCount = 10;
    private final int quickSlotRow = 2;
    private final float quickSlotSize = 44f;
    private final float quickBarMargin = 18f;
    private final float quickSlotSpacing = 6f;
    private final Map<String, TextureRegion> quickSlotIcons = new HashMap<>();
    private final Map<String, TextureRegion> buffIcons = new HashMap<>();
    private int selectedQuickSlot = 0;
    private final GlyphLayout cooldownLayout = new GlyphLayout();
    private int draggedQuickSlot = 0;
    private float draggedQuickSlotX = 0f;
    private float draggedQuickSlotY = 0f;
    private Player.ActiveBuff tooltipBuff;

    public PlayerHUD(Player player, SpriteLoader spriteLoader) throws GameException {
        this.player = player;

        this.spriteLoader = spriteLoader;
        this.hpBar = spriteLoader.getRegionFromSpriteName("64kMainVitalityBar");
        this.mpBar = spriteLoader.getRegionFromSpriteName("64kMainManaBar");
        this.xpBar = spriteLoader.getRegionFromSpriteName("64kMainExperienceBar");
        this.frame = spriteLoader.getRegionFromSpriteName("64kMainEmptyBar");
        this.quickSlotFrame = spriteLoader.getRegionFromSpriteName("64kMinimizedMacro");
        this.buffBackground = spriteLoader.getRegionFromSpriteName("64kStatusBackGround");
        this.combatModeIcon = spriteLoader.getRegionFromSpriteName(COMBAT_ICON_SPRITE);
        this.lastTextureGen = spriteLoader.getTextureGeneration();

        this.font = ((MyGame) Gdx.app.getApplicationListener()).customFont;
        this.font.getData().setScale(0.7f);
        this.font.setColor(Color.WHITE);
        this.tooltip = new HudTooltip();
    }

    /**
     * Render the HUD at an offset from the top-right corner.
     */
    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        if (spriteLoader.getTextureGeneration() != lastTextureGen) refreshRegions();
        if (hpBar == null || mpBar == null || xpBar == null || frame == null) return;

        float hpPercent = (float) player.getCurrentHp() / player.getMaxHp();
        float mpPercent = (float) player.getMana() / player.getMaxMana();
        float xpPercent = (float) player.getCurrentXp() / player.getXpToNextLevel();

        int barWidth = frame.getRegionWidth();
        int barHeight = frame.getRegionHeight();
        int spacing = barWidth + 40;

        int totalWidth = spacing * 3;
        float startX = Gdx.graphics.getWidth() - totalWidth - offsetX;

        font.draw(batch, "PV", startX - 5, offsetY - 10);
        batch.draw(frame, startX + 25, offsetY - barHeight);
        int hpVisible = (int) (hpBar.getRegionWidth() * hpPercent);
        if (hpVisible > 0) {
            batch.draw(hpBar.getTexture(), startX + 35, offsetY - barHeight + 1, hpVisible, barHeight, hpBar.getRegionX(), hpBar.getRegionY(), hpVisible, hpBar.getRegionHeight(), false, true);
        }

        font.draw(batch, "PM", startX + spacing - 10, offsetY - 10);
        batch.draw(frame, startX + spacing + 25, offsetY - barHeight);
        int mpVisible = (int) (mpBar.getRegionWidth() * mpPercent);
        if (mpVisible > 0) {
            batch.draw(mpBar.getTexture(), startX + spacing + 35, offsetY - barHeight + 1, mpVisible, barHeight, mpBar.getRegionX(), mpBar.getRegionY(), mpVisible, mpBar.getRegionHeight(), false, true);
        }

        font.draw(batch, "XP", startX + spacing * 2 - 10, offsetY - 10);
        batch.draw(frame, startX + spacing * 2 + 25, offsetY - barHeight);
        int xpVisible = (int) (xpBar.getRegionWidth() * xpPercent);
        if (xpVisible > 0) {
            batch.draw(xpBar.getTexture(), startX + spacing * 2 + 35, offsetY - barHeight + 1, xpVisible, barHeight, xpBar.getRegionX(), xpBar.getRegionY(), xpVisible, xpBar.getRegionHeight(), false, true);
        }

        renderActiveBuffs(batch);
        renderQuickBar(batch);
        renderBuffTooltip(batch);
        renderCombatModeIndicator(batch);
    }

    private void renderCombatModeIndicator(SpriteBatch batch) {
        if (!player.isCombatMode() || combatModeIcon == null) {
            return;
        }
        float iconW = combatModeIcon.getRegionWidth();
        float iconH = combatModeIcon.getRegionHeight();
        float x = Gdx.graphics.getWidth() - iconW - COMBAT_ICON_MARGIN;
        float y = Gdx.graphics.getHeight() - iconH - COMBAT_ICON_MARGIN;
        batch.draw(combatModeIcon.getTexture(), x, y, iconW, iconH,
                combatModeIcon.getRegionX(), combatModeIcon.getRegionY(),
                combatModeIcon.getRegionWidth(), combatModeIcon.getRegionHeight(), false, true);
    }

    private void renderActiveBuffs(SpriteBatch batch) {
        if (buffBackground == null || player.getActiveBuffs().isEmpty()) {
            return;
        }
        float x = BUFF_BAR_X;
        float y = BUFF_BAR_Y;
        float spacing = buffBackground.getRegionHeight();
        for (Player.ActiveBuff buff : player.getActiveBuffs()) {
            if (buff == null) {
                continue;
            }
            batch.draw(buffBackground, x, y);
            renderBuffDurationBar(batch, buff, x, y);
            TextureRegion icon = resolveBuffIcon(buff);
            if (icon != null) {
                float iconW = icon.getRegionWidth();
                float iconH = icon.getRegionHeight();
                float iconX = x + (buffBackground.getRegionWidth() - iconW) * 0.5f;
                float iconY = y + (buffBackground.getRegionHeight() - iconH) * 0.5f;
                batch.draw(icon.getTexture(), iconX, iconY, iconW, iconH,
                        icon.getRegionX(), icon.getRegionY(), icon.getRegionWidth(), icon.getRegionHeight(), false, true);
            }
            y += spacing;
        }
    }

    private void renderBuffDurationBar(SpriteBatch batch, Player.ActiveBuff buff, float x, float y) {
        if (tooltip == null || buff == null || buff.getDurationMillis() == Long.MAX_VALUE) {
            return;
        }
        long remainingMillis = Math.max(0L, buff.getExpiresAtMillis() - System.currentTimeMillis());
        float percent = buff.getDurationMillis() <= 0L ? 0f : Math.min(1f, remainingMillis / (float) buff.getDurationMillis());
        float barX = x + 3.55f;
        float barY = y + 2f;
        float barW = 2;
        float barH = Math.max(1f, buffBackground.getRegionHeight() - 4f);
        float remainingH = barH * percent;
        Color previous = new Color(batch.getColor());
        batch.setColor(0f, 0f, 0f, 0.95f);
        batch.draw(tooltip.getBackground(), barX, barY, barW, barH);
        if (remainingH > 0.5f) {
            batch.setColor(1f, 0.82f, 0.18f, 1f);
            batch.draw(tooltip.getBackground(), barX, barY + barH - remainingH, barW, remainingH);
        }
        batch.setColor(previous);
    }

    private void renderQuickBar(SpriteBatch batch) {
        TextureRegion slot = quickSlotFrame != null ? quickSlotFrame : frame;
        if (slot == null) {
            return;
        }
        int columns = Math.max(1, (int) Math.ceil(quickSlotCount / (float) quickSlotRow));
        float totalWidth = columns * quickSlotSize + (columns - 1) * quickSlotSpacing;
        float startX = (Gdx.graphics.getWidth() - totalWidth) * 0.5f;
        float baseY = Gdx.graphics.getHeight() - quickBarMargin;
        for (int i = 0; i < quickSlotCount; i++) {
            int col = i % columns;
            int row = i / columns;
            float x = startX + col * (quickSlotSize + quickSlotSpacing);
            float y = baseY - row * (quickSlotSize + quickSlotSpacing);
            batch.draw(slot, x, y - quickSlotSize, quickSlotSize, quickSlotSize);
            if (selectedQuickSlot == i + 1) {
                Color previous = new Color(batch.getColor());
                float t = TimeUtils.millis() / 1000f;
                float pulse = 0.5f + 0.5f * MathUtils.sin(t * 14f);
                float alpha = 0.3f + 0.7f * pulse;
                float glowSize = quickSlotSize + 6f;
                float glowX = x - 3f;
                float glowY = y - quickSlotSize - 3f;
                batch.setColor(1f, 0.9f, 0.15f, alpha);
                batch.draw(slot, glowX, glowY, glowSize, glowSize);
                float outerSize = quickSlotSize + 14f;
                float outerX = x - 7f;
                float outerY = y - quickSlotSize - 7f;
                batch.setColor(1f, 0.8f, 0.1f, alpha * 0.6f);
                batch.draw(slot, outerX, outerY, outerSize, outerSize);
                batch.setColor(previous);
            }
            int slotNumber = i + 1;
            String spellName = getSpellForSlot(slotNumber);
            String itemName = getItemForSlot(slotNumber);
            SpellData spell = getSpellData(spellName);
            TextureRegion icon = itemName == null ? resolveSpellIcon(spellName) : resolveItemIcon(itemName);
            if (icon != null) {
                float iconW = icon.getRegionWidth();
                float iconH = icon.getRegionHeight();
                float iconX = x + (quickSlotSize - iconW) * 0.5f;
                float iconY = y - quickSlotSize + (quickSlotSize - iconH) * 0.5f;
                boolean onCooldown = spell != null && player.isSpellOnCooldown(spell.getName());
                if (onCooldown) {
                    Color previous = new Color(batch.getColor());
                    batch.setColor(0.55f, 0.55f, 0.55f, 1f);
                    batch.draw(icon.getTexture(), iconX, iconY, iconW, iconH,
                            icon.getRegionX(), icon.getRegionY(), icon.getRegionWidth(), icon.getRegionHeight(), false, true);
                    batch.setColor(0f, 0f, 0f, 0.45f);
                    batch.draw(slot, x, y - quickSlotSize, quickSlotSize, quickSlotSize);
                    batch.setColor(previous);
                    renderCooldownText(batch, x, y - quickSlotSize, quickSlotSize, spell);
                } else {
                    batch.draw(icon.getTexture(), iconX, iconY, iconW, iconH,
                            icon.getRegionX(), icon.getRegionY(), icon.getRegionWidth(), icon.getRegionHeight(), false, true);
                }
            }
        }
        renderDraggedQuickSlotIcon(batch);
    }

    private void renderDraggedQuickSlotIcon(SpriteBatch batch) {
        if (draggedQuickSlot <= 0) {
            return;
        }
        String spellName = getSpellForSlot(draggedQuickSlot);
        TextureRegion icon = resolveSpellIcon(spellName);
        if (icon == null) {
            return;
        }
        float iconW = icon.getRegionWidth();
        float iconH = icon.getRegionHeight();
        batch.draw(icon.getTexture(), draggedQuickSlotX - iconW * 0.5f, draggedQuickSlotY - iconH * 0.5f, iconW, iconH,
                icon.getRegionX(), icon.getRegionY(), icon.getRegionWidth(), icon.getRegionHeight(), false, true);
    }

    public int getQuickSlotAt(int screenX, int screenY) {
        int columns = Math.max(1, (int) Math.ceil(quickSlotCount / (float) quickSlotRow));
        float totalWidth = columns * quickSlotSize + (columns - 1) * quickSlotSpacing;
        float startX = (Gdx.graphics.getWidth() - totalWidth) * 0.5f;
        float baseY = Gdx.graphics.getHeight() - quickBarMargin;
        float totalHeight = quickSlotRow * quickSlotSize + (quickSlotRow - 1) * quickSlotSpacing;
        float left = startX;
        float right = startX + totalWidth;
        float top = baseY - totalHeight;
        float bottom = baseY;
        if (screenX < left || screenX > right || screenY < top || screenY > bottom) {
            return 0;
        }
        int col = (int) ((screenX - startX) / (quickSlotSize + quickSlotSpacing));
        int row = (int) ((baseY - screenY) / (quickSlotSize + quickSlotSpacing));
        if (col < 0 || row < 0 || col >= columns || row >= quickSlotRow) {
            return 0;
        }
        int slot = row * columns + col + 1;
        return slot <= quickSlotCount ? slot : 0;
    }

    public SpellData getSpellDataForSlot(int slotNumber) {
        String spellName = getSpellForSlot(slotNumber);
        if (spellName == null) {
            return null;
        }
        return getSpellData(spellName);
    }

    public String getItemForSlot(int slotNumber) {
        List<QuickSlotEntry> slots = player.getQuickSlots();
        if (slots == null) return null;
        for (QuickSlotEntry entry : slots) {
            if (entry != null && entry.getSlot() == slotNumber) return entry.getItem();
        }
        return null;
    }

    public void assignItemToQuickSlot(String itemName, int slotNumber) {
        if (itemName == null || slotNumber <= 0) return;
        List<QuickSlotEntry> slots = player.getQuickSlots();
        if (slots == null) {
            slots = new java.util.ArrayList<>();
            player.setQuickSlots(slots);
        }
        for (QuickSlotEntry entry : slots) {
            if (entry != null && entry.getSlot() == slotNumber) {
                entry.setSpell(null);
                entry.setItem(itemName);
                return;
            }
        }
        QuickSlotEntry entry = new QuickSlotEntry();
        entry.setSlot(slotNumber);
        entry.setItem(itemName);
        slots.add(entry);
    }

    public boolean isQuickBarHit(int screenX, int screenY) {
        int columns = Math.max(1, (int) Math.ceil(quickSlotCount / (float) quickSlotRow));
        float totalWidth = columns * quickSlotSize + (columns - 1) * quickSlotSpacing;
        float startX = (Gdx.graphics.getWidth() - totalWidth) * 0.5f;
        float baseY = Gdx.graphics.getHeight() - quickBarMargin;
        float totalHeight = quickSlotRow * quickSlotSize + (quickSlotRow - 1) * quickSlotSpacing;
        float left = startX;
        float right = startX + totalWidth;
        float top = baseY - totalHeight;
        float bottom = baseY;
        return screenX >= left && screenX <= right && screenY >= top && screenY <= bottom;
    }

    public boolean showBuffTooltipAt(int screenX, int screenY) {
        Player.ActiveBuff buff = getBuffAt(screenX, screenY);
        if (buff == null) {
            return false;
        }
        tooltipBuff = buff;
        tooltip.show(buildBuffTooltipText(buff), screenX, screenY);
        return true;
    }

    public String getBuffSpellNameAt(int screenX, int screenY) {
        Player.ActiveBuff buff = getBuffAt(screenX, screenY);
        return buff == null ? null : buff.getSpellName();
    }

    public boolean showQuickSlotTooltipAt(int screenX, int screenY) {
        int slot = getQuickSlotAt(screenX, screenY);
        if (slot <= 0) {
            return false;
        }
        SpellData spell = getSpellDataForSlot(slot);
        if (spell == null || spell.getName() == null || spell.getName().isEmpty()) {
            return false;
        }
        tooltipBuff = null;
        tooltip.show(I18n.spellName(spell.getName()), screenX, screenY);
        return true;
    }

    private Player.ActiveBuff getBuffAt(int screenX, int screenY) {
        if (buffBackground == null) {
            return null;
        }
        float x = BUFF_BAR_X;
        float y = BUFF_BAR_Y;
        float w = buffBackground.getRegionWidth();
        float h = buffBackground.getRegionHeight();
        for (Player.ActiveBuff buff : player.getActiveBuffs()) {
            if (buff != null && screenX >= x && screenX <= x + w && screenY >= y && screenY <= y + h) {
                return buff;
            }
            y += h;
        }
        return null;
    }

    private String getSpellForSlot(int slotNumber) {
        List<QuickSlotEntry> slots = player.getQuickSlots();
        if (slots == null || slots.isEmpty()) {
            return null;
        }
        for (QuickSlotEntry entry : slots) {
            if (entry != null && entry.getSlot() == slotNumber) {
                return entry.getSpell();
            }
        }
        return null;
    }

    private TextureRegion resolveSpellIcon(String spellName) {
        if (spellName == null || spellName.isEmpty()) {
            return null;
        }
        TextureRegion cached = quickSlotIcons.get(spellName);
        if (cached != null) {
            return cached;
        }
        SpellData spell = getSpellData(spellName);
        if (spell == null || spell.getIconId() == null || spell.getIconId().isEmpty()) {
            return null;
        }
        TextureRegion icon = spriteLoader.getRegionFromSpriteName(spell.getIconId());
        if (icon != null) {
            quickSlotIcons.put(spellName, icon);
        }
        return icon;
    }

    private TextureRegion resolveItemIcon(String itemName) {
        ItemDefinition item = ItemDefinition.get(itemName);
        return item == null ? null : spriteLoader.getRegionFromSpriteName(item.getAppearanceInventory());
    }

    private TextureRegion resolveBuffIcon(Player.ActiveBuff buff) {
        String iconId = buff.getIconId();
        if (iconId == null || iconId.isEmpty()) {
            return null;
        }
        TextureRegion cached = buffIcons.get(iconId);
        if (cached != null) {
            return cached;
        }
        TextureRegion icon = spriteLoader.getRegionFromSpriteName(iconId);
        if (icon != null) {
            buffIcons.put(iconId, icon);
        }
        return icon;
    }

    private SpellData getSpellData(String spellName) {
        if (spellName == null || spellName.isEmpty()) {
            return null;
        }
        return SpellRegistry.findByName(spellName);
    }

    public void setSelectedQuickSlot(int slotNumber) {
        this.selectedQuickSlot = slotNumber;
    }

    public void startQuickSlotDrag(int slotNumber, float screenX, float screenY) {
        this.draggedQuickSlot = slotNumber;
        this.draggedQuickSlotX = screenX;
        this.draggedQuickSlotY = screenY;
    }

    public void updateQuickSlotDrag(float screenX, float screenY) {
        if (draggedQuickSlot <= 0) {
            return;
        }
        this.draggedQuickSlotX = screenX;
        this.draggedQuickSlotY = screenY;
    }

    public void stopQuickSlotDrag() {
        this.draggedQuickSlot = 0;
    }

    private void renderCooldownText(SpriteBatch batch, float slotX, float slotY, float size, SpellData spell) {
        if (spell == null) {
            return;
        }
        float remaining = player.getSpellCooldownRemainingSeconds(spell.getName());
        if (remaining <= 0f) {
            return;
        }
        int seconds = (int) Math.ceil(remaining);
        String text = String.valueOf(seconds);
        float previousScale = font.getData().scaleX;
        font.getData().setScale(0.6f);
        cooldownLayout.setText(font, text);
        float textX = slotX + (size - cooldownLayout.width) * 0.5f;
        float textY = slotY + (size + cooldownLayout.height) * 0.5f;
        font.draw(batch, cooldownLayout, textX, textY);
        font.getData().setScale(previousScale);
    }

    private void renderBuffTooltip(SpriteBatch batch) {
        if (tooltipBuff == null) {
            tooltip.render(batch);
            return;
        }
        if (!tooltip.isVisible() || !player.getActiveBuffs().contains(tooltipBuff)) {
            tooltipBuff = null;
            tooltip.clear();
            return;
        }
        tooltip.render(batch, buildBuffTooltipText(tooltipBuff));
    }

    private String buildBuffTooltipText(Player.ActiveBuff buff) {
        String description = I18n.spellDescription(buff.getSpellName(), buff.getDescription());
        if (description == null || description.isEmpty()) {
            description = I18n.spellName(buff.getSpellName());
        } else if (!description.contains(":")) {
            description = I18n.spellName(buff.getSpellName()) + ": " + description;
        }
        while (description.endsWith(".")) {
            description = description.substring(0, description.length() - 1);
        }
        return description + ". " + I18n.tooltipLabel("Time remaining") + ": " + formatBuffRemaining(buff);
    }

    private String formatBuffRemaining(Player.ActiveBuff buff) {
        long expiresAt = buff.getExpiresAtMillis();
        if (expiresAt == Long.MAX_VALUE) {
            return "Infinite";
        }
        long remainingSeconds = Math.max(0L, (expiresAt - System.currentTimeMillis() + 999L) / 1000L);
        long minutes = remainingSeconds / 60L;
        long seconds = remainingSeconds % 60L;
        if (minutes > 0L) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    private void refreshRegions() {
        try {
            this.hpBar = spriteLoader.getRegionFromSpriteName("64kMainVitalityBar");
            this.mpBar = spriteLoader.getRegionFromSpriteName("64kMainManaBar");
            this.xpBar = spriteLoader.getRegionFromSpriteName("64kMainExperienceBar");
            this.frame = spriteLoader.getRegionFromSpriteName("64kMainEmptyBar");
            this.quickSlotFrame = spriteLoader.getRegionFromSpriteName("64kMinimizedMacro");
            this.buffBackground = spriteLoader.getRegionFromSpriteName("64kStatusBackGround");
            this.combatModeIcon = spriteLoader.getRegionFromSpriteName(COMBAT_ICON_SPRITE);
        } catch (Exception ignored) {
        }
        this.quickSlotIcons.clear();
        this.buffIcons.clear();
        this.lastTextureGen = spriteLoader.getTextureGeneration();
    }

    public void dispose() {
        tooltip.dispose();
    }
}
