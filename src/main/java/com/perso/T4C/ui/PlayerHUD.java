package com.perso.T4C.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.perso.T4C.MyGame;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiResizable;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiBar;
import com.perso.T4C.gui.core.GuiBoxedInteraction;
import com.perso.T4C.gui.core.GuiDraw;
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

/**
 * Heads-up display for the player showing HP/MP/XP bars.
 */
public class PlayerHUD {
    private static final float BUFF_BAR_X = 0f;
    private static final float BUFF_BAR_Y = 80f;
    private static final String COMBAT_ICON_SPRITE = "64kCursorAttack";
    private static final float COMBAT_ICON_MARGIN = 16f;
    private static final float TOP_BAR_HEIGHT = 46f;
    private static final String GENERATED_TOP_BAR_PATH = "assets/ui/topbar-frame-v2.png";
    private static final float TOP_BAR_BORDER_HEIGHT = 6f;
    private static final int TOP_BAR_BACKGROUND_X = 244;
    private static final int TOP_BAR_BACKGROUND_Y = 41;
    private static final int TOP_BAR_BACKGROUND_WIDTH = 272;
    private static final int TOP_BAR_BACKGROUND_HEIGHT = 46;
    private static final int TOP_BAR_BORDER_X = 64;
    private static final int TOP_BAR_BORDER_Y = 0;
    private static final int TOP_BAR_BORDER_WIDTH = 458;
    private static final int TOP_BAR_BORDER_SOURCE_HEIGHT = 8;
    private static final float STAT_PANEL_WIDTH = 458f;
    private static final float STAT_PANEL_HEIGHT = 46f;
    private static final float STAT_PANEL_Y = 0f;
    private static final float STAT_BAR_WIDTH = 134f;
    private static final float STAT_BAR_HEIGHT = 18f;
    private static final float STAT_LABEL_WIDTH = 28f;
    private static final float STAT_LABEL_HEIGHT = 12f;
    private static final float STAT_VALUE_Y = 27f;
    private static final float[] STAT_BAR_X = {83f, 260f};
    private static final float[] STAT_BAR_Y = {17f, 17f};
    private static final float[] STAT_LABEL_X = {58f, 235f};
    private static final float[] STAT_LABEL_Y = {20f, 19f};

    private TextureRegion topBarBackground;
    private TextureRegion topBarBorder;
    private Texture generatedTopBar;
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
    private static final int QUICK_SLOT_COUNT = 5;
    private static final float CHAT_BAR_WIDTH = 934f;
    private static final float CHAT_BAR_HEIGHT = 166f;
    private static final float QUICK_SLOT_X = 668f;
    private static final float QUICK_SLOT_Y = 31f;
    private static final float QUICK_SLOT_SIZE = 42f;
    private static final float[] QUICK_SLOT_OFFSET_X = {0f, 52f, 103f, 156f, 206f};
    private static final float BACKPACK_BUTTON_X = 707f;
    private static final float BACKPACK_BUTTON_Y = 81f;
    private static final float XP_BAR_X = 15f;
    private static final float XP_BAR_Y = 142f;
    private static final float XP_BAR_WIDTH = 894f;
    private static final float XP_BAR_HEIGHT = 14f;
    private final Map<String, TextureRegion> quickSlotIcons = new HashMap<>();
    private final Map<String, TextureRegion> buffIcons = new HashMap<>();
    private int selectedQuickSlot = 0;
    private final GlyphLayout cooldownLayout = new GlyphLayout();
    private int draggedQuickSlot = 0;
    private float draggedQuickSlotX = 0f;
    private float draggedQuickSlotY = 0f;
    private final float[] quickSlotGuiOffsetX = new float[QUICK_SLOT_COUNT];
    private final float[] quickSlotGuiOffsetY = new float[QUICK_SLOT_COUNT];
    private final QuickSlotBox[] quickSlotBoxes = new QuickSlotBox[QUICK_SLOT_COUNT];
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
    private GuiBar hpGuiBar;
    private GuiBar mpGuiBar;
    private GuiBar xpGuiBar;
    private float xpGuiOffsetX;
    private float xpGuiOffsetY;
    private float lastXpGuiX;
    private float lastXpGuiY;
    private float lastXpGuiWidth;
    private float lastXpGuiHeight;
    private float xpGuiWidthScale = 1f;
    private float xpGuiHeightScale = 1f;
    private boolean xpGuiLayoutInitialized;
    private final GuiBoxedInteraction boxedInteraction = new GuiBoxedInteraction();
    private final List<GuiElement> hudBoxedElements = new ArrayList<>();
    private final HudStatsPanel statsPanel;
    private boolean statsPanelLayoutInitialized;

    public PlayerHUD(Player player, SpriteLoader spriteLoader) throws GameException {
        this.player = player;

        this.spriteLoader = spriteLoader;
        loadTopBarRegions();
        loadGeneratedTopBar();
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
        this.font.getData().setScale(FontManager.logicalScale(0.7f));
        this.font.setColor(Color.WHITE);
        this.statLabelFont = FontManager.getInstance().getTahomaFont(12, Color.WHITE, true);
        this.hpLabel = new GuiBoxedText(statLabelFont, 0f, 0f, STAT_LABEL_WIDTH, STAT_LABEL_HEIGHT,
                () -> "PV", () -> Color.WHITE).shrinkToFit();
        this.mpLabel = new GuiBoxedText(statLabelFont, 0f, 0f, STAT_LABEL_WIDTH, STAT_LABEL_HEIGHT,
                () -> "PM", () -> Color.WHITE).shrinkToFit();
        createStatBars();
        createExperienceBar();
        for (int i = 0; i < QUICK_SLOT_COUNT; i++) quickSlotBoxes[i] = new QuickSlotBox();
        this.statsPanel = new HudStatsPanel();
        statsPanel.initializeChildren();
        registerHudBoxedElements();
        this.tooltip = new HudTooltip();
    }

    /**
     * Render the HUD at an offset from the top-right corner.
     */
    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        if (spriteLoader.getTextureGeneration() != lastTextureGen) refreshRegions();
        if (topBarBackground == null || hpBar == null || mpBar == null) return;

        renderTopBar(batch);
        if (!statsPanelLayoutInitialized) {
            statsPanel.setSize(STAT_PANEL_WIDTH, STAT_PANEL_HEIGHT);
            statsPanel.setPosition(Math.max(0f, (Gdx.graphics.getWidth() - STAT_PANEL_WIDTH) * 0.5f), STAT_PANEL_Y);
            statsPanelLayoutInitialized = true;
        }
        statsPanel.render(batch);

        renderActiveBuffs(batch);
        renderExperienceBar(batch);
        renderGold(batch);
        if (isQuickBarVisible()) renderQuickBar(batch);
        renderChatBarButtons(batch);
        renderBuffTooltip(batch);
        renderCombatModeIndicator(batch);
    }

    private void renderGold(SpriteBatch batch) {
        String value = "Or : " + player.getGold();
        GlyphLayout layout = new GlyphLayout(statLabelFont, value);
        statLabelFont.draw(batch, value, Gdx.graphics.getWidth() - layout.width - 12f, 12f);
    }

    private void loadTopBarRegions() throws GameException {
        TextureRegion chatBackground = spriteLoader.getRegionFromSpriteName("GUI_backChat");
        if (chatBackground == null) {
            topBarBackground = null;
            topBarBorder = null;
            return;
        }
        topBarBackground = subRegion(chatBackground, TOP_BAR_BACKGROUND_X, TOP_BAR_BACKGROUND_Y,
                TOP_BAR_BACKGROUND_WIDTH, TOP_BAR_BACKGROUND_HEIGHT);
        topBarBorder = subRegion(chatBackground, TOP_BAR_BORDER_X, TOP_BAR_BORDER_Y,
                TOP_BAR_BORDER_WIDTH, TOP_BAR_BORDER_SOURCE_HEIGHT);
    }

    private static TextureRegion subRegion(TextureRegion parent, int x, int y, int width, int height) {
        return new TextureRegion(parent.getTexture(), parent.getRegionX() + x, parent.getRegionY() + y,
                width, height);
    }

    private void loadGeneratedTopBar() {
        if (!Gdx.files.internal(GENERATED_TOP_BAR_PATH).exists()) return;
        generatedTopBar = new Texture(Gdx.files.internal(GENERATED_TOP_BAR_PATH));
        generatedTopBar.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private void renderTopBar(SpriteBatch batch) {
        float screenWidth = Gdx.graphics.getWidth();
        GuiDraw.withOverlayAlpha(batch, () -> {
            if (generatedTopBar != null) {
                batch.draw(generatedTopBar, (screenWidth - STAT_PANEL_WIDTH) * 0.5f, 0f,
                        STAT_PANEL_WIDTH, STAT_PANEL_HEIGHT, 0, 0,
                        generatedTopBar.getWidth(), generatedTopBar.getHeight(), false, true);
            } else {
                drawTiled(batch, topBarBackground, 0f, 0f, screenWidth, TOP_BAR_HEIGHT);
                drawTiled(batch, topBarBorder, 0f, TOP_BAR_HEIGHT - TOP_BAR_BORDER_HEIGHT,
                        screenWidth, TOP_BAR_BORDER_HEIGHT);
            }
        });
    }

    private static void drawTiled(SpriteBatch batch, TextureRegion region, float x, float y,
                                  float width, float height) {
        if (region == null || width <= 0f || height <= 0f) return;
        float tileWidth = region.getRegionWidth() * height / region.getRegionHeight();
        for (float drawn = 0f; drawn < width; drawn += tileWidth) {
            float partWidth = Math.min(tileWidth, width - drawn);
            int sourceWidth = Math.max(1, Math.round(region.getRegionWidth() * partWidth / tileWidth));
            batch.draw(region.getTexture(), x + drawn, y, partWidth, height,
                    region.getRegionX(), region.getRegionY(), sourceWidth, region.getRegionHeight(), false, true);
        }
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

    private void createStatBars() {
        this.hpGuiBar = new GuiBar(emptyBar, hpBar, 0f, 0f, STAT_BAR_WIDTH, STAT_BAR_HEIGHT,
                () -> safeRatio(player.getCurrentHp(), player.getMaxHp()));
        this.mpGuiBar = new GuiBar(emptyBar, mpBar, 0f, 0f, STAT_BAR_WIDTH, STAT_BAR_HEIGHT,
                () -> safeRatio(player.getMana(), player.getMaxMana()));
    }

    private void createExperienceBar() {
        this.xpGuiBar = new GuiBar(null, xpBar, 0f, 0f, XP_BAR_WIDTH, XP_BAR_HEIGHT,
                () -> safeRatio(player.getCurrentXp(), player.getXpToNextLevel()));
        this.xpGuiLayoutInitialized = false;
    }

    private void refreshStatBars() {
        GuiBar oldHp = hpGuiBar;
        GuiBar oldMp = mpGuiBar;
        createStatBars();
        createExperienceBar();
        copyBox(oldHp, hpGuiBar);
        copyBox(oldMp, mpGuiBar);
        registerHudBoxedElements();
    }

    private static void copyBox(GuiBar source, GuiBar target) {
        if (source == null) {
            return;
        }
        target.setPosition(source.getX(), source.getY());
        target.setSize(source.getWidth(), source.getHeight());
    }

    private static float safeRatio(long value, long maximum) {
        return maximum <= 0L ? 0f : (float) value / maximum;
    }

    private void registerHudBoxedElements() {
        hudBoxedElements.clear();
        // The panel is deliberately first: reverse hit-testing gives its children priority.
        hudBoxedElements.add(statsPanel);
        hudBoxedElements.add(hpGuiBar);
        hudBoxedElements.add(mpGuiBar);
        hudBoxedElements.add(hpLabel);
        hudBoxedElements.add(mpLabel);
        hudBoxedElements.add(xpGuiBar);
        for (ChatBarButtonItem item : chatBarButtons) {
            hudBoxedElements.add(item.button);
        }
        for (QuickSlotBox box : quickSlotBoxes) {
            if (box != null) hudBoxedElements.add(box);
        }
    }

    /**
     * Boxed unit for the complete character-stat HUD. Its children use coordinates relative to
     * the compact background, so Ctrl+drag and Shift+resize always keep the background, labels,
     * bars and values together.
     */
    private final class HudStatsPanel extends AbstractGuiElement implements GuiResizable {
        private float width = STAT_PANEL_WIDTH;
        private float height = STAT_PANEL_HEIGHT;

        private HudStatsPanel() {
            super(0f, 0f);
        }

        @Override
        public void render(SpriteBatch batch) {
            hpGuiBar.render(batch);
            mpGuiBar.render(batch);
            hpLabel.render(batch);
            mpLabel.render(batch);

            if (com.perso.T4C.config.GamePreferencesStore.get().isShowHudValues()) {
                drawBarValue(batch, player.getCurrentHp() + " / " + player.getMaxHp(),
                        hpGuiBar.getX(), y + STAT_VALUE_Y, hpGuiBar.getWidth(), STAT_LABEL_HEIGHT);
                drawBarValue(batch, player.getMana() + " / " + player.getMaxMana(),
                        mpGuiBar.getX(), y + STAT_VALUE_Y, mpGuiBar.getWidth(), STAT_LABEL_HEIGHT);
            }
            GuiBoxedItem.drawDebugBorder(batch, x, y, width, height);
        }

        private void initializeChildren() {
            layoutInitial(hpGuiBar, hpLabel, 0);
            layoutInitial(mpGuiBar, mpLabel, 1);
        }

        private void layoutInitial(GuiBar bar, GuiBoxedText label, int row) {
            bar.setPosition(x + STAT_BAR_X[row], y + STAT_BAR_Y[row]);
            bar.setSize(STAT_BAR_WIDTH, STAT_BAR_HEIGHT);
            label.setPosition(x + STAT_LABEL_X[row], y + STAT_LABEL_Y[row]);
            label.setSize(STAT_LABEL_WIDTH, STAT_LABEL_HEIGHT);
        }

        @Override
        public void setPosition(float x, float y) {
            float dx = x - this.x;
            float dy = y - this.y;
            super.setPosition(x, y);
            translate(hpGuiBar, dx, dy);
            translate(mpGuiBar, dx, dy);
            translate(hpLabel, dx, dy);
            translate(mpLabel, dx, dy);
        }

        @Override
        public HudStatsPanel setSize(float width, float height) {
            float newWidth = Math.max(1f, width);
            float newHeight = Math.max(1f, height);
            float scaleX = newWidth / this.width;
            float scaleY = newHeight / this.height;
            scaleChild(hpGuiBar, scaleX, scaleY);
            scaleChild(mpGuiBar, scaleX, scaleY);
            scaleChild(hpLabel, scaleX, scaleY);
            scaleChild(mpLabel, scaleX, scaleY);
            this.width = newWidth;
            this.height = newHeight;
            return this;
        }

        private void translate(GuiElement element, float dx, float dy) {
            element.setPosition(element.getX() + dx, element.getY() + dy);
        }

        private void scaleChild(GuiResizable child, float scaleX, float scaleY) {
            child.setPosition(
                    x + (child.getX() - x) * scaleX,
                    y + (child.getY() - y) * scaleY);
            child.setSize(child.getWidth() * scaleX, child.getHeight() * scaleY);
        }

        @Override
        public float getWidth() {
            return width;
        }

        @Override
        public float getHeight() {
            return height;
        }
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
            float backgroundY = y;
            GuiDraw.withOverlayAlpha(batch, () -> batch.draw(buffBackground, x, backgroundY));
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
            QuickSlotBox box = syncQuickSlotBox(layout, i);
            float x = box.getX();
            float y = box.getY();
            float slotWidth = box.getWidth();
            float slotHeight = box.getHeight();
            float slotSize = Math.min(slotWidth, slotHeight);
            box.render(batch);
            if (selectedQuickSlot == i + 1) {
                Color previous = new Color(batch.getColor());
                float t = TimeUtils.millis() / 1000f;
                float pulse = 0.5f + 0.5f * MathUtils.sin(t * 14f);
                float alpha = 0.3f + 0.7f * pulse;
                float glowSize = slotSize + 6f * layout.scale;
                float glowX = x - 3f;
                float glowY = y - 3f;
                batch.setColor(1f, 0.9f, 0.15f, alpha);
                batch.draw(slot, glowX, glowY, glowSize, glowSize);
                float outerSize = slotSize + 14f * layout.scale;
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
                        (slotWidth - 8f * layout.scale) / icon.getRegionWidth(),
                        (slotHeight - 8f * layout.scale) / icon.getRegionHeight()));
                float iconW = icon.getRegionWidth() * iconScale;
                float iconH = icon.getRegionHeight() * iconScale;
                float iconX = x + (slotWidth - iconW) * 0.5f;
                float iconY = y + (slotHeight - iconH) * 0.5f;
                boolean onCooldown = spell != null && player.isSpellOnCooldown(spell.getName());
                if (onCooldown) {
                    Color previous = new Color(batch.getColor());
                    batch.setColor(0.55f, 0.55f, 0.55f, 1f);
                    batch.draw(icon.getTexture(), iconX, iconY, iconW, iconH,
                            icon.getRegionX(), icon.getRegionY(), icon.getRegionWidth(), icon.getRegionHeight(), false, true);
                    batch.setColor(0f, 0f, 0f, 0.45f);
                    batch.draw(slot, x, y, slotWidth, slotHeight);
                    batch.setColor(previous);
                    renderCooldownText(batch, x, y, slotSize, spell);
                } else {
                    batch.draw(icon.getTexture(), iconX, iconY, iconW, iconH,
                            icon.getRegionX(), icon.getRegionY(), icon.getRegionWidth(), icon.getRegionHeight(), false, true);
                }
            }
        }
        renderDraggedQuickSlotIcon(batch);
    }

    private void renderExperienceBar(SpriteBatch batch) {
        if (xpGuiBar == null) return;
        QuickBarLayout layout = quickBarLayout();
        float ratio = Math.max(0f, Math.min(1f,
                safeRatio(player.getCurrentXp(), player.getXpToNextLevel())));
        float x = layout.x + (XP_BAR_X - QUICK_SLOT_X) * layout.scale;
        float y = layout.y + (XP_BAR_Y - QUICK_SLOT_Y) * layout.scale;
        float width = XP_BAR_WIDTH * layout.scale;
        float height = XP_BAR_HEIGHT * layout.scale;
        if (xpGuiLayoutInitialized) {
            // GuiBoxedInteraction moves the element directly. Convert that movement into a
            // persistent offset/scale before applying the HUD's responsive base layout again.
            xpGuiOffsetX += xpGuiBar.getX() - lastXpGuiX;
            xpGuiOffsetY += xpGuiBar.getY() - lastXpGuiY;
            if (lastXpGuiWidth > 0f) {
                xpGuiWidthScale *= xpGuiBar.getWidth() / lastXpGuiWidth;
            }
            if (lastXpGuiHeight > 0f) {
                xpGuiHeightScale *= xpGuiBar.getHeight() / lastXpGuiHeight;
            }
        } else {
            xpGuiLayoutInitialized = true;
        }
        lastXpGuiX = x + xpGuiOffsetX;
        lastXpGuiY = y + xpGuiOffsetY;
        lastXpGuiWidth = width * xpGuiWidthScale;
        lastXpGuiHeight = height * xpGuiHeightScale;
        xpGuiBar.setPosition(lastXpGuiX, lastXpGuiY);
        xpGuiBar.setSize(lastXpGuiWidth, lastXpGuiHeight);
        xpGuiBar.render(batch);
        if (com.perso.T4C.config.GamePreferencesStore.get().isXpBarText()) {
            drawBarValue(batch, "XP  " + Math.round(ratio * 100f) + "%",
                    lastXpGuiX, lastXpGuiY - 3f * layout.scale, lastXpGuiWidth,
                    Math.max(13f * layout.scale, lastXpGuiHeight));
        }
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
        if (!isQuickBarVisible()) return 0;
        QuickBarLayout layout = quickBarLayout();
        for (int i = QUICK_SLOT_COUNT - 1; i >= 0; i--) {
            QuickSlotBox box = syncQuickSlotBox(layout, i);
            if (box.contains(screenX, screenY)) {
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

    private boolean isQuickBarVisible() {
        return true;
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
                scale);
    }

    private float quickSlotX(QuickBarLayout layout, int index) {
        return layout.x + (QUICK_SLOT_OFFSET_X[index] + quickSlotGuiOffsetX[index]) * layout.scale;
    }

    private float quickSlotY(QuickBarLayout layout, int index) {
        return layout.y + quickSlotGuiOffsetY[index] * layout.scale;
    }

    private QuickSlotBox syncQuickSlotBox(QuickBarLayout layout, int index) {
        QuickSlotBox box = quickSlotBoxes[index];
        if (box.layoutInitialized) {
            quickSlotGuiOffsetX[index] += (box.getX() - box.lastX) / layout.scale;
            quickSlotGuiOffsetY[index] += (box.getY() - box.lastY) / layout.scale;
            if (box.lastWidth > 0f) box.widthScale *= box.getWidth() / box.lastWidth;
            if (box.lastHeight > 0f) box.heightScale *= box.getHeight() / box.lastHeight;
        } else {
            box.layoutInitialized = true;
        }
        box.lastX = quickSlotX(layout, index);
        box.lastY = quickSlotY(layout, index);
        box.lastWidth = layout.size * box.widthScale;
        box.lastHeight = layout.size * box.heightScale;
        box.setPosition(box.lastX, box.lastY);
        box.setSize(box.lastWidth, box.lastHeight);
        return box;
    }

    private record QuickBarLayout(float x, float y, float size, float scale) {}

    private static final class QuickSlotBox extends AbstractGuiElement implements GuiResizable {
        private float width = QUICK_SLOT_SIZE;
        private float height = QUICK_SLOT_SIZE;
        private float lastX;
        private float lastY;
        private float lastWidth;
        private float lastHeight;
        private float widthScale = 1f;
        private float heightScale = 1f;
        private boolean layoutInitialized;

        private QuickSlotBox() { super(0f, 0f); }

        @Override public void render(SpriteBatch batch) {
            GuiBoxedItem.drawDebugBorder(batch, x, y, width, height);
        }

        @Override public QuickSlotBox setSize(float width, float height) {
            this.width = Math.max(8f, width);
            this.height = Math.max(8f, height);
            return this;
        }

        @Override public float getWidth() { return width; }
        @Override public float getHeight() { return height; }
    }

    private void renderChatBarButtons(SpriteBatch batch) {
        QuickBarLayout layout = quickBarLayout();
        for (ChatBarButtonItem item : chatBarButtons) {
            float baseWidth = 40f * layout.scale;
            float baseHeight = 40f * layout.scale;
            if (item.layoutInitialized) {
                item.offsetX += (item.button.getX() - item.lastX) / layout.scale;
                item.offsetY += (item.button.getY() - item.lastY) / layout.scale;
                if (item.lastWidth > 0f) item.widthScale *= item.button.getWidth() / item.lastWidth;
                if (item.lastHeight > 0f) item.heightScale *= item.button.getHeight() / item.lastHeight;
            } else {
                item.layoutInitialized = true;
            }
            item.lastX = chatBarButtonX(layout, item);
            item.lastY = chatBarButtonY(layout, item);
            item.lastWidth = baseWidth * item.widthScale;
            item.lastHeight = baseHeight * item.heightScale;
            item.button.setPosition(item.lastX, item.lastY);
            item.button.setSize(item.lastWidth, item.lastHeight);
            GuiDraw.withOverlayAlpha(batch, () -> item.button.render(batch));
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
        boolean shiftDown = Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.SHIFT_RIGHT);
        // Shift+drag belongs to the common boxed resize controller.
        if (shiftDown) return false;
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
        addChatBarButton("Character", 664f, BACKPACK_BUTTON_Y, "GUI_ChatBtnCharCheet",
                () -> characterAction.run());
        addChatBarButton("Backpack", BACKPACK_BUTTON_X, BACKPACK_BUTTON_Y, "GUI_ChatBtnBackPack",
                () -> backpackAction.run());
        addChatBarButton("Spell book", 750f, "GUI_ChatBtnSpell",
                () -> spellBookAction.run());
        addChatBarButton("Quest journal", 793f, "GUI_ChatBtnQuest",
                () -> questAction.run());
        addChatBarButton("World map", 836f, "GUI_ChatBtnMap",
                () -> mapAction.run());
        addChatBarButton("Options", 879f, BACKPACK_BUTTON_Y, "GUI_ChatBtnOption",
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
        float lastX;
        float lastY;
        float lastWidth;
        float lastHeight;
        float widthScale = 1f;
        float heightScale = 1f;
        boolean layoutInitialized;

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
                (targetX - layout.x) / layout.scale - QUICK_SLOT_OFFSET_X[index];
        quickSlotGuiOffsetY[index] = (targetY - layout.y) / layout.scale;
    }

    public void stopQuickSlotGuiItemDragAndLog() {
        if (draggedQuickSlotGuiItem <= 0) return;
        int index = draggedQuickSlotGuiItem - 1;
        float relativeX = QUICK_SLOT_X + QUICK_SLOT_OFFSET_X[index] + quickSlotGuiOffsetX[index];
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
        font.getData().setScale(FontManager.logicalScale(0.6f));
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
        String spellDisplayName = I18n.has(buff.getSpellName())
                ? I18n.key(buff.getSpellName())
                : I18n.resolve(buff.getSpellName());
        if (description == null || description.isEmpty()) {
            description = spellDisplayName;
        } else if (!description.contains(":")) {
            description = spellDisplayName + ": " + description;
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
            loadTopBarRegions();
            this.hpBar = spriteLoader.getRegionFromSpriteName("GUI_BackChStat_HP");
            this.mpBar = spriteLoader.getRegionFromSpriteName("GUI_BackChStat_MP");
            this.xpBar = spriteLoader.getRegionFromSpriteName("GUI_BackChStat_XP");
            this.emptyBar = spriteLoader.getRegionFromSpriteName("GUI_BackChStat_Empty");
            refreshStatBars();
            this.frame = spriteLoader.getRegionFromSpriteName("64kMainEmptyBar");
            this.quickSlotFrame = spriteLoader.getRegionFromSpriteName("64kMinimizedMacro");
            loadChatBarButtons();
            registerHudBoxedElements();
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
        if (generatedTopBar != null) generatedTopBar.dispose();
    }
}
