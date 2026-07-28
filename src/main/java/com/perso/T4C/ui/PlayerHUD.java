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
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiBar;
import com.perso.T4C.gui.core.GuiBoxedInteraction;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.model.QuickSlotEntry;
import com.perso.T4C.player.Player;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

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
    private TextureRegion emptyBar;
    private TextureRegion frame;
    private TextureRegion quickSlotFrame;
    private final List<ChatBarButtonItem> chatBarButtons = new ArrayList<>();
    private TextureRegion buffBackground;
    private TextureRegion combatModeIcon;
    private final Player player;
    private final BitmapFont font;
    private final BitmapFont statLabelFont;
    private final HudTooltip tooltip;
    private final SpriteLoader spriteLoader;
    private int lastTextureGen;
    private static final int QUICK_SLOT_COUNT = 6;
    private static final float CHAT_BAR_WIDTH = 1024f;
    private static final float CHAT_BAR_HEIGHT = 150f;
    private static final float QUICK_SLOT_X = 742f;
    private static final float QUICK_SLOT_Y = 47f;
    private static final float QUICK_SLOT_SIZE = 42f;
    private static final float QUICK_SLOT_PITCH = 46f;
    private static final float BACKPACK_BUTTON_X = 790f;
    private static final float BACKPACK_BUTTON_Y = 102f;
    private final Map<String, TextureRegion> quickSlotIcons = new HashMap<>();
    private final Map<String, TextureRegion> buffIcons = new HashMap<>();
    private int selectedQuickSlot = 0;
    private final GlyphLayout cooldownLayout = new GlyphLayout();
    private int draggedQuickSlot = 0;
    private float draggedQuickSlotX = 0f;
    private float draggedQuickSlotY = 0f;
    private final float[] quickSlotGuiOffsetX = new float[QUICK_SLOT_COUNT];
    private final float[] quickSlotGuiOffsetY = new float[QUICK_SLOT_COUNT];
    private int draggedQuickSlotGuiItem;
    private float quickSlotGuiGrabOffsetX;
    private float quickSlotGuiGrabOffsetY;
    private Runnable backpackAction = () -> { };
    private Runnable characterAction = () -> { };
    private Runnable spellBookAction = () -> { };
    private Runnable questAction = () -> { };
    private Runnable mapAction = () -> { };
    private Runnable optionsAction = () -> { };
    private ChatBarButtonItem capturedChatBarButton;
    private ChatBarButtonItem draggedChatBarButton;
    private Player.ActiveBuff tooltipBuff;
    private GuiBoxedText hpLabel;
    private GuiBoxedText mpLabel;
    private GuiBoxedText xpLabel;
    private GuiBar hpGuiBar;
    private GuiBar mpGuiBar;
    private GuiBar xpGuiBar;
    private final GuiBoxedInteraction boxedInteraction = new GuiBoxedInteraction();
    private final List<GuiElement> hudBoxedElements = new ArrayList<>();
    private boolean hudBarsPositioned;

    public PlayerHUD(Player player, SpriteLoader spriteLoader) throws GameException {
        this.player = player;

        this.spriteLoader = spriteLoader;
        this.hpBar = spriteLoader.getRegionFromSpriteName("GUI_BackChStat_HP");
        this.mpBar = spriteLoader.getRegionFromSpriteName("GUI_BackChStat_MP");
        this.xpBar = spriteLoader.getRegionFromSpriteName("GUI_BackChStat_XP");
        this.emptyBar = spriteLoader.getRegionFromSpriteName("GUI_BackChStat_Empty");
        this.frame = spriteLoader.getRegionFromSpriteName("64kMainEmptyBar");
        this.quickSlotFrame = spriteLoader.getRegionFromSpriteName("64kMinimizedMacro");
        loadChatBarButtons();
        this.buffBackground = spriteLoader.getRegionFromSpriteName("64kStatusBackGround");
        this.combatModeIcon = spriteLoader.getRegionFromSpriteName(COMBAT_ICON_SPRITE);
        this.lastTextureGen = spriteLoader.getTextureGeneration();

        this.font = ((MyGame) Gdx.app.getApplicationListener()).customFont;
        this.font.getData().setScale(0.7f);
        this.font.setColor(Color.WHITE);
        this.statLabelFont = FontManager.getInstance().getTahomaFont(12, Color.WHITE, true);
        this.hpLabel = new GuiBoxedText(statLabelFont, 0f, 0f, 24f, 16f, () -> "PV", () -> Color.WHITE);
        this.mpLabel = new GuiBoxedText(statLabelFont, 0f, 0f, 24f, 16f, () -> "PM", () -> Color.WHITE);
        this.xpLabel = new GuiBoxedText(statLabelFont, 0f, 0f, 24f, 16f, () -> "XP", () -> Color.WHITE);
        this.hpGuiBar = new GuiBar(emptyBar, hpBar, 0f, 0f, hpBar.getRegionWidth(), hpBar.getRegionHeight(),
                () -> (float) player.getCurrentHp() / player.getMaxHp());
        this.mpGuiBar = new GuiBar(emptyBar, mpBar, 0f, 0f, mpBar.getRegionWidth(), mpBar.getRegionHeight(),
                () -> (float) player.getMana() / player.getMaxMana());
        this.xpGuiBar = new GuiBar(emptyBar, xpBar, 0f, 0f, xpBar.getRegionWidth(), xpBar.getRegionHeight(),
                () -> (float) player.getCurrentXp() / player.getXpToNextLevel());
        hudBoxedElements.add(hpGuiBar);
        hudBoxedElements.add(mpGuiBar);
        hudBoxedElements.add(xpGuiBar);
        hudBoxedElements.add(hpLabel);
        hudBoxedElements.add(mpLabel);
        hudBoxedElements.add(xpLabel);
        this.tooltip = new HudTooltip();
    }

    /**
     * Render the HUD at an offset from the top-right corner.
     */
    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        if (spriteLoader.getTextureGeneration() != lastTextureGen) refreshRegions();
        if (hpBar == null || mpBar == null || xpBar == null) return;

        float hpPercent = (float) player.getCurrentHp() / player.getMaxHp();
        float mpPercent = (float) player.getMana() / player.getMaxMana();
        float xpPercent = (float) player.getCurrentXp() / player.getXpToNextLevel();

        int barWidth = hpBar.getRegionWidth();
        int barHeight = hpBar.getRegionHeight();
        int spacing = barWidth + 40;

        int totalWidth = spacing * 3;
        float startX = Gdx.graphics.getWidth() - totalWidth - offsetX;
        if (!hudBarsPositioned) {
            float baseY = offsetY - barHeight + 1;
            hpGuiBar.setPosition(startX + 35f, baseY);
            mpGuiBar.setPosition(startX + spacing + 35f, baseY);
            xpGuiBar.setPosition(startX + spacing * 2f + 35f, baseY);
            hpLabel.setPosition(startX + 9f, offsetY - 13f);
            mpLabel.setPosition(startX + 152f, offsetY - 13f);
            xpLabel.setPosition(startX + 296f, offsetY - 13f);
            hudBarsPositioned = true;
        }

        float hpBarX = hpGuiBar.getX();
        float hpBarY = hpGuiBar.getY();
        hpLabel.render(batch);
        hpGuiBar.render(batch);

        float mpBarX = mpGuiBar.getX();
        float mpBarY = mpGuiBar.getY();
        mpLabel.render(batch);
        mpGuiBar.render(batch);

        float xpBarX = xpGuiBar.getX();
        float xpBarY = xpGuiBar.getY();
        xpLabel.render(batch);
        xpGuiBar.render(batch);

        if (com.perso.T4C.config.GamePreferencesStore.get().isShowHudValues()) {
            drawBarValue(batch, player.getCurrentHp() + "/" + player.getMaxHp(), hpBarX,
                    offsetY - barHeight + 1, hpBar.getRegionWidth(), barHeight);
            drawBarValue(batch, player.getMana() + "/" + player.getMaxMana(), mpBarX,
                    offsetY - barHeight + 1, mpBar.getRegionWidth(), barHeight);
            drawBarValue(batch, player.getCurrentXp() + "/" + player.getXpToNextLevel(), xpBarX,
                    offsetY - barHeight + 1, xpBar.getRegionWidth(), barHeight);
        }

        renderActiveBuffs(batch);
        renderQuickBar(batch);
        renderChatBarButtons(batch);
        renderBuffTooltip(batch);
        renderCombatModeIndicator(batch);
    }

    public boolean boxedTouchDown(float sx, float sy, boolean controlDown) {
        return boxedInteraction.touchDown(hudBoxedElements, sx, sy);
    }

    public boolean boxedMouseMoved(float sx, float sy) {
        return boxedInteraction.dragged(sx, sy);
    }

    public boolean boxedTouchUp() {
        return boxedInteraction.touchUp();
    }

    private void drawBarValue(SpriteBatch batch, String value, float barX, float barY, float width, float height) {
        GlyphLayout layout = new GlyphLayout(statLabelFont, value);
        statLabelFont.draw(batch, value, barX + (width - layout.width) / 2f,
                barY + (height - layout.height) / 2f);
    }

    private void drawEmptyBar(SpriteBatch batch, float x, float y, float width, float height) {
        if (emptyBar != null) {
            batch.draw(emptyBar, x, y, width, height);
        }
        GuiBoxedItem.drawDebugBorder(batch, x, y, width, height);
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
        QuickBarLayout layout = quickBarLayout();
        for (int i = 0; i < QUICK_SLOT_COUNT; i++) {
            float x = quickSlotX(layout, i);
            float y = quickSlotY(layout, i);
            GuiBoxedItem.drawDebugBorder(batch, x, y, layout.size, layout.size);
            if (selectedQuickSlot == i + 1) {
                Color previous = new Color(batch.getColor());
                float t = TimeUtils.millis() / 1000f;
                float pulse = 0.5f + 0.5f * MathUtils.sin(t * 14f);
                float alpha = 0.3f + 0.7f * pulse;
                float glowSize = layout.size + 6f * layout.scale;
                float glowX = x - 3f;
                float glowY = y - 3f;
                batch.setColor(1f, 0.9f, 0.15f, alpha);
                batch.draw(slot, glowX, glowY, glowSize, glowSize);
                float outerSize = layout.size + 14f * layout.scale;
                float outerX = x - 7f;
                float outerY = y - 7f;
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
                float iconScale = Math.min(1f, Math.min(
                        (layout.size - 8f * layout.scale) / icon.getRegionWidth(),
                        (layout.size - 8f * layout.scale) / icon.getRegionHeight()));
                float iconW = icon.getRegionWidth() * iconScale;
                float iconH = icon.getRegionHeight() * iconScale;
                float iconX = x + (layout.size - iconW) * 0.5f;
                float iconY = y + (layout.size - iconH) * 0.5f;
                boolean onCooldown = spell != null && player.isSpellOnCooldown(spell.getName());
                if (onCooldown) {
                    Color previous = new Color(batch.getColor());
                    batch.setColor(0.55f, 0.55f, 0.55f, 1f);
                    batch.draw(icon.getTexture(), iconX, iconY, iconW, iconH,
                            icon.getRegionX(), icon.getRegionY(), icon.getRegionWidth(), icon.getRegionHeight(), false, true);
                    batch.setColor(0f, 0f, 0f, 0.45f);
                    batch.draw(slot, x, y, layout.size, layout.size);
                    batch.setColor(previous);
                    renderCooldownText(batch, x, y, layout.size, spell);
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
        QuickBarLayout layout = quickBarLayout();
        for (int i = QUICK_SLOT_COUNT - 1; i >= 0; i--) {
            float x = quickSlotX(layout, i);
            float y = quickSlotY(layout, i);
            if (screenX >= x && screenX <= x + layout.size
                    && screenY >= y && screenY <= y + layout.size) {
                return i + 1;
            }
        }
        return 0;
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
        return getQuickSlotAt(screenX, screenY) > 0;
    }

    private QuickBarLayout quickBarLayout() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float scale = Math.min(1f, screenWidth / CHAT_BAR_WIDTH);
        float barX = (screenWidth - CHAT_BAR_WIDTH * scale) * 0.5f;
        float barY = screenHeight - CHAT_BAR_HEIGHT * scale;
        return new QuickBarLayout(
                barX + QUICK_SLOT_X * scale,
                barY + QUICK_SLOT_Y * scale,
                QUICK_SLOT_SIZE * scale,
                QUICK_SLOT_PITCH * scale,
                scale);
    }

    private float quickSlotX(QuickBarLayout layout, int index) {
        return layout.x + (index * layout.pitch + quickSlotGuiOffsetX[index] * layout.scale);
    }

    private float quickSlotY(QuickBarLayout layout, int index) {
        return layout.y + quickSlotGuiOffsetY[index] * layout.scale;
    }

    private record QuickBarLayout(float x, float y, float size, float pitch, float scale) {}

    private void renderChatBarButtons(SpriteBatch batch) {
        QuickBarLayout layout = quickBarLayout();
        for (ChatBarButtonItem item : chatBarButtons) {
            item.button.setPosition(chatBarButtonX(layout, item), chatBarButtonY(layout, item));
            item.button.setSize(40f * layout.scale, 40f * layout.scale);
            item.button.render(batch);
        }
    }

    private float chatBarButtonX(QuickBarLayout layout, ChatBarButtonItem item) {
        return layout.x
                + (item.baseX - QUICK_SLOT_X + item.offsetX) * layout.scale;
    }

    private float chatBarButtonY(QuickBarLayout layout, ChatBarButtonItem item) {
        return layout.y
                + (item.baseY - QUICK_SLOT_Y + item.offsetY) * layout.scale;
    }

    public void setBackpackAction(Runnable backpackAction) {
        this.backpackAction = backpackAction == null ? () -> { } : backpackAction;
    }

    public void setCharacterAction(Runnable characterAction) {
        this.characterAction = characterAction == null ? () -> { } : characterAction;
    }

    public void setSpellBookAction(Runnable spellBookAction) {
        this.spellBookAction = spellBookAction == null ? () -> { } : spellBookAction;
    }

    public void setQuestAction(Runnable questAction) {
        this.questAction = questAction == null ? () -> { } : questAction;
    }

    public void setMapAction(Runnable mapAction) {
        this.mapAction = mapAction == null ? () -> { } : mapAction;
    }

    public void setOptionsAction(Runnable optionsAction) {
        this.optionsAction = optionsAction == null ? () -> { } : optionsAction;
    }

    public boolean chatBarButtonsTouchDown(float screenX, float screenY, boolean controlDown) {
        ChatBarButtonItem item = findChatBarButton(screenX, screenY);
        if (item == null) return false;
        if (controlDown) {
            QuickBarLayout layout = quickBarLayout();
            draggedChatBarButton = item;
            item.grabOffsetX = screenX - chatBarButtonX(layout, item);
            item.grabOffsetY = screenY - chatBarButtonY(layout, item);
        } else {
            capturedChatBarButton = item;
            item.button.onTouchDown(screenX, screenY);
        }
        return true;
    }

    public boolean chatBarButtonsTouchDragged(float screenX, float screenY) {
        if (draggedChatBarButton == null) return false;
        QuickBarLayout layout = quickBarLayout();
        ChatBarButtonItem item = draggedChatBarButton;
        item.offsetX = (screenX - item.grabOffsetX - layout.x) / layout.scale
                + QUICK_SLOT_X - item.baseX;
        item.offsetY = (screenY - item.grabOffsetY - layout.y) / layout.scale
                + QUICK_SLOT_Y - item.baseY;
        return true;
    }

    public boolean chatBarButtonsTouchUp(float screenX, float screenY) {
        if (draggedChatBarButton != null) {
            ChatBarButtonItem item = draggedChatBarButton;
            draggedChatBarButton = null;
            Gdx.app.log("GuiScreen", String.format(Locale.ROOT,
                    "%s button moved to x + %.1ff, y + %.1ff",
                    item.logName, item.baseX + item.offsetX, item.baseY + item.offsetY));
            item.button.onMouseMove(screenX, screenY);
            return true;
        }
        if (capturedChatBarButton == null) return false;
        ChatBarButtonItem item = capturedChatBarButton;
        capturedChatBarButton = null;
        item.button.onTouchUp(screenX, screenY);
        return true;
    }

    public boolean chatBarButtonsMouseMoved(float screenX, float screenY) {
        if (draggedChatBarButton != null) {
            return chatBarButtonsTouchDragged(screenX, screenY);
        }
        boolean hovered = false;
        for (ChatBarButtonItem item : chatBarButtons) {
            item.button.onMouseMove(screenX, screenY);
            hovered |= item.button.contains(screenX, screenY);
        }
        return hovered;
    }

    private ChatBarButtonItem findChatBarButton(float screenX, float screenY) {
        for (int i = chatBarButtons.size() - 1; i >= 0; i--) {
            ChatBarButtonItem item = chatBarButtons.get(i);
            if (item.button.contains(screenX, screenY)) return item;
        }
        return null;
    }

    private void loadChatBarButtons() throws GameException {
        chatBarButtons.clear();
        addChatBarButton("Character", 744f, "GUI_ChatBtnCharCheet",
                () -> characterAction.run());
        addChatBarButton("Backpack", BACKPACK_BUTTON_X, "GUI_ChatBtnBackPack",
                () -> backpackAction.run());
        addChatBarButton("Spell book", 836f, "GUI_ChatBtnSpell",
                () -> spellBookAction.run());
        addChatBarButton("Quest journal", 882f, "GUI_ChatBtnQuest",
                () -> questAction.run());
        addChatBarButton("World map", 928f, "GUI_ChatBtnMap",
                () -> mapAction.run());
        addChatBarButton("Options", 974f, 101f, "GUI_ChatBtnOption",
                () -> optionsAction.run());
    }

    private void addChatBarButton(String logName, float x, String spriteBase, Runnable action)
            throws GameException {
        addChatBarButton(logName, x, BACKPACK_BUTTON_Y, spriteBase, action);
    }

    private void addChatBarButton(String logName, float x, float y, String spriteBase, Runnable action)
            throws GameException {
        TextureRegion normal = spriteLoader.getRegionFromSpriteName(spriteBase + "Up");
        TextureRegion hover = spriteLoader.getRegionFromSpriteName(spriteBase + "HUp");
        TextureRegion pressed = spriteLoader.getRegionFromSpriteName(spriteBase + "Down");
        if (normal != null && hover != null && pressed != null) {
            chatBarButtons.add(new ChatBarButtonItem(logName, x, y,
                    new GuiButton(normal, hover, pressed, 0f, 0f, action)));
        }
    }

    private static final class ChatBarButtonItem {
        final String logName;
        final float baseX;
        final float baseY;
        final GuiButton button;
        float offsetX;
        float offsetY;
        float grabOffsetX;
        float grabOffsetY;

        ChatBarButtonItem(String logName, float baseX, float baseY, GuiButton button) {
            this.logName = logName;
            this.baseX = baseX;
            this.baseY = baseY;
            this.button = button;
        }
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
        tooltip.show(I18n.resolve(spell.getName()), screenX, screenY);
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

    public boolean startQuickSlotGuiItemDrag(int slotNumber, float screenX, float screenY) {
        if (slotNumber <= 0 || slotNumber > QUICK_SLOT_COUNT
                || (getSpellForSlot(slotNumber) == null && getItemForSlot(slotNumber) == null)) {
            return false;
        }
        QuickBarLayout layout = quickBarLayout();
        int index = slotNumber - 1;
        draggedQuickSlotGuiItem = slotNumber;
        quickSlotGuiGrabOffsetX = screenX - quickSlotX(layout, index);
        quickSlotGuiGrabOffsetY = screenY - quickSlotY(layout, index);
        return true;
    }

    public void updateQuickSlotGuiItemDrag(float screenX, float screenY) {
        if (draggedQuickSlotGuiItem <= 0) return;
        QuickBarLayout layout = quickBarLayout();
        int index = draggedQuickSlotGuiItem - 1;
        float targetX = screenX - quickSlotGuiGrabOffsetX;
        float targetY = screenY - quickSlotGuiGrabOffsetY;
        quickSlotGuiOffsetX[index] =
                (targetX - layout.x) / layout.scale - index * QUICK_SLOT_PITCH;
        quickSlotGuiOffsetY[index] = (targetY - layout.y) / layout.scale;
    }

    public void stopQuickSlotGuiItemDragAndLog() {
        if (draggedQuickSlotGuiItem <= 0) return;
        int index = draggedQuickSlotGuiItem - 1;
        float relativeX = QUICK_SLOT_X + index * QUICK_SLOT_PITCH + quickSlotGuiOffsetX[index];
        float relativeY = QUICK_SLOT_Y + quickSlotGuiOffsetY[index];
        Gdx.app.log("GuiScreen", String.format(Locale.ROOT,
                "Quick slot %d moved to x + %.1ff, y + %.1ff",
                draggedQuickSlotGuiItem, relativeX, relativeY));
        draggedQuickSlotGuiItem = 0;
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
        String description = I18n.resolve(buff.getDescription());
        if (description == null || description.isEmpty()) {
            description = I18n.resolve(buff.getSpellName());
        } else if (!description.contains(":")) {
            description = I18n.resolve(buff.getSpellName()) + ": " + description;
        }
        while (description.endsWith(".")) {
            description = description.substring(0, description.length() - 1);
        }
        return description + ". " + I18n.key("tooltip.time_remaining") + ": " + formatBuffRemaining(buff);
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
            this.hpBar = spriteLoader.getRegionFromSpriteName("GUI_BackChStat_HP");
            this.mpBar = spriteLoader.getRegionFromSpriteName("GUI_BackChStat_MP");
            this.xpBar = spriteLoader.getRegionFromSpriteName("GUI_BackChStat_XP");
            this.frame = spriteLoader.getRegionFromSpriteName("64kMainEmptyBar");
            this.quickSlotFrame = spriteLoader.getRegionFromSpriteName("64kMinimizedMacro");
            loadChatBarButtons();
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
