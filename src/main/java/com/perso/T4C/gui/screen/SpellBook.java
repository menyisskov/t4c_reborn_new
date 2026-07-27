package com.perso.T4C.gui.screen;

import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiText;

import com.perso.T4C.i18n.I18n;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.model.QuickSlotEntry;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.PlayerHUD;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
/**
 * Class representing SpellBook.
 */

public class SpellBook extends GuiScreenBase {
    private static final float[][] SLOT_POSITIONS = {
            {70f, 60f},  // slot 0 (haut-gauche)
            {320f, 60f}, // slot 1 (haut-droite)
            {70f, 200f}, // slot 2 (bas-gauche)
            {320f, 200f} // slot 3 (bas-droite)
    };
    // Icon zone top-left per slot; the icon is centered inside ICON_ZONE_W×ICON_ZONE_H.
    private static final float[][] ICON_OFFSETS = {
            {72f, 15f},  // slot 0
            {69f, 18f},  // slot 1
            {75f, -1f},  // slot 2
            {67f, -2f}   // slot 3
    };
    private static final float ICON_ZONE_W = 48f;
    private static final float ICON_ZONE_H = 48f;
    // Same relative layout for every slot, taken from slot 0 (Fire dart);
    // bottom slots (2/3) start their text block higher.
    private static final float LABEL_X = -3f;
    private static final float LEVEL_X = 130f;
    private static final float TEXT_TOP_Y_UPPER = 64f;
    private static final float TEXT_TOP_Y_LOWER = 45f;
    private static final float NAME_DY = 0f;
    private static final float TYPE_DY = 16f;
    private static final float DURATION_DY = 31f;
    private static final float MANA_DY = 46f;
    private static final float LABEL_VALUE_GAP = 74f;
    private static final float LEVEL_LABEL_VALUE_GAP = 51f;
    // Boxed line zones: label and value cells of each spell-slot line.
    private static final float LINE_H = 16f;
    private static final float LABEL_W = 70f;
    private static final float VALUE_W = 120f;
    private static final float LEVEL_LABEL_W = 48f;
    private static final float LEVEL_VALUE_W = 40f;
    private static final Color LABEL_COLOR = Color.valueOf("09090E");
    private static final Color VALUE_COLOR = Color.valueOf("09090E");
    private static final float FRAME_TIME = 0.1f;

    private final Player player;
    private final PlayerHUD hud;
    private final List<GuiText> pageLabels = new ArrayList<>();
    private final List<GuiAnimatedSprite> pageSprites = new ArrayList<>();
    private final List<SpellBookEntry> spells = new ArrayList<>();
    private int currentPage = 0;
    private SpellBookEntry draggedSpell;
    private TextureRegion draggedIcon;
    private float draggedX;
    private float draggedY;
    private static final Map<String, SpellData> spellByName = new HashMap<>();
    private static boolean registryReady = false;

    public SpellBook(Player player) {
        this(player, null);
    }

    public SpellBook(Player player, PlayerHUD hud) {
        this.player = player;
        this.hud = hud;
        try {
            background = SpriteLoader.getInstance().getRegionFromSpriteName("GUI_BackSpell");
        } catch (GameException ignored) {
            background = null;
        }
        centerOnScreen();
        addCloseButton();
        addCornerAnimations();
        addHeader();
        loadSpells();
        rebuildPage();
    }

    private void addCloseButton() {
        addCloseButton(550f, 1f);
    }

    private void addCornerAnimations() {
        if (background == null) {
            return;
        }
        var leftFrames = loadCornerFrames("64kSpellBookCornerL");
        var rightFrames = loadCornerFrames("64kSpellBookCornerR");
        if (leftFrames.isEmpty() || rightFrames.isEmpty()) {
            return;
        }
        float leftX = x + 448f;
        float leftY = y + 52f;
        float rightX = x + 17f;
        float rightY = y + 50f;
        Runnable prevPage = () -> turnPage(-1);
        Runnable nextPage = () -> turnPage(1);
        animatedSprites.add(new GuiAnimatedSprite(leftFrames, leftX, leftY, FRAME_TIME, nextPage));
        animatedSprites.add(new GuiAnimatedSprite(rightFrames, rightX, rightY, FRAME_TIME, prevPage));
    }

    private void addHeader() {
        if (background == null) {
            return;
        }
        var gold = Color.valueOf("F2B705");
        labels.add(new GuiBoxedText(FontManager.getInstance().getHaettenschweilerFont(18, gold),
                x + 237f, y + 2f, 101f, 19f,
                () -> I18n.key("ui.spellbook"), () -> gold).shrinkToFit());
    }

    private void loadSpells() {
        spells.clear();
        if (player == null || player.getSpells() == null) {
            return;
        }
        for (String spellName : player.getSpells()) {
            SpellData data = resolveSpellData(spellName);
            if (data != null) {
                spells.add(new SpellBookEntry(spellName, data));
            }
        }
    }

    private SpellData resolveSpellData(String spellName) {
        if (spellName == null || spellName.isEmpty()) {
            return null;
        }
        ensureRegistry();
        SpellData data = spellByName.get(spellName);
        return data != null ? data : SpellRegistry.findByName(spellName);
    }

    private void rebuildPage() {
        if (!pageLabels.isEmpty()) {
            labels.removeAll(pageLabels);
            pageLabels.clear();
        }
        if (!pageSprites.isEmpty()) {
            animatedSprites.removeAll(pageSprites);
            pageSprites.clear();
        }
        int start = currentPage * 4;
        for (int i = 0; i < 4; i++) {
            int index = start + i;
            if (index >= spells.size()) {
                break;
            }
            float slotX = x + SLOT_POSITIONS[i][0];
            float slotY = y + SLOT_POSITIONS[i][1];
            addSpellEntry(spells.get(index), i, slotX, slotY);
        }
    }

    private void addSpellEntry(SpellBookEntry entry, int slotIndex, float slotX, float slotY) {
        SpellData spell = entry.spell;
        TextureRegion icon = null;
        if (spell.getIconId() != null && !spell.getIconId().isEmpty()) {
            try {
                icon = SpriteLoader.getInstance().getRegionFromSpriteName(spell.getIconId());
            } catch (GameException ignored) {
            }
        }
        if (icon != null) {
            List<TextureRegion> frames = new ArrayList<>();
            frames.add(icon);
            float iconX = slotX + ICON_OFFSETS[slotIndex][0];
            float iconY = slotY + ICON_OFFSETS[slotIndex][1];
            GuiAnimatedSprite iconSprite = new GuiAnimatedSprite(frames, iconX, iconY, FRAME_TIME)
                    .boxed(ICON_ZONE_W, ICON_ZONE_H);
            animatedSprites.add(iconSprite);
            pageSprites.add(iconSprite);
        }

        var labelFont = FontManager.getInstance().getTahomaFont(12, LABEL_COLOR, true);
        var valueFont = FontManager.getInstance().getTahomaFont(12, VALUE_COLOR, false);
        String name = I18n.resolve(spell.getName());
        String manaCost = spell.getManaCost() == null ? "" : spell.getManaCost();
        String type = spell.getAttackType() == SpellData.ATTACK_MENTAL
                ? I18n.key("ui.spell_type.mental")
                : I18n.key("ui.spell_type.physical");
        String durationValue = spell.getDuration();
        String duration = durationValue == null || durationValue.isEmpty() ? "instant" : durationValue;
        String level = String.valueOf(spell.getMinLevel());

        // Each line is a pair of boxed zones: label (width LABEL_W) + value, all left-aligned.
        float topY = slotIndex < 2 ? TEXT_TOP_Y_UPPER : TEXT_TOP_Y_LOWER;
        addPageBox(labelFont, slotX + LABEL_X, slotY + topY + NAME_DY, LABEL_W, LINE_H, () -> I18n.key("ui.spell_stat.name"), GuiBoxedText.Align.LEFT);
        addPageBox(valueFont, slotX + LABEL_X + LABEL_VALUE_GAP, slotY + topY + NAME_DY, VALUE_W, LINE_H, () -> name, GuiBoxedText.Align.LEFT);
        addPageBox(labelFont, slotX + LABEL_X, slotY + topY + TYPE_DY, LABEL_W, LINE_H, () -> I18n.key("ui.spell_stat.type"), GuiBoxedText.Align.LEFT);
        addPageBox(valueFont, slotX + LABEL_X + LABEL_VALUE_GAP, slotY + topY + TYPE_DY, VALUE_W, LINE_H, () -> type, GuiBoxedText.Align.LEFT);
        addPageBox(labelFont, slotX + LABEL_X, slotY + topY + DURATION_DY, LABEL_W, LINE_H, () -> I18n.key("ui.spell_stat.duration"), GuiBoxedText.Align.LEFT);
        addPageBox(valueFont, slotX + LABEL_X + LABEL_VALUE_GAP, slotY + topY + DURATION_DY, VALUE_W, LINE_H, () -> duration, GuiBoxedText.Align.LEFT);
        addPageBox(labelFont, slotX + LABEL_X, slotY + topY + MANA_DY, LABEL_W, LINE_H, () -> I18n.key("ui.spell_stat.mana"), GuiBoxedText.Align.LEFT);
        addPageBox(valueFont, slotX + LABEL_X + LABEL_VALUE_GAP, slotY + topY + MANA_DY, VALUE_W, LINE_H, () -> manaCost, GuiBoxedText.Align.LEFT);
        addPageBox(labelFont, slotX + LEVEL_X, slotY + topY + MANA_DY, LEVEL_LABEL_W, LINE_H, () -> I18n.key("ui.spell_stat.level"), GuiBoxedText.Align.LEFT);
        addPageBox(valueFont, slotX + LEVEL_X + LEVEL_LABEL_VALUE_GAP, slotY + topY + MANA_DY, LEVEL_VALUE_W, LINE_H, () -> level, GuiBoxedText.Align.LEFT);
    }

    private void addPageBox(com.badlogic.gdx.graphics.g2d.BitmapFont font,
                            float bx, float by, float bw, float bh,
                            java.util.function.Supplier<String> text,
                            GuiBoxedText.Align align) {
        GuiBoxedText label = new GuiBoxedText(font, bx, by, bw, bh, text, null).align(align);
        labels.add(label);
        pageLabels.add(label);
    }

    private void turnPage(int delta) {
        int maxPage = spells.isEmpty() ? 0 : (spells.size() - 1) / 4;
        int next = Math.max(0, Math.min(maxPage, currentPage + delta));
        SoundManager.animateSound("Page turning sound.wav");
        if (next == currentPage) {
            return;
        }
        currentPage = next;
        rebuildPage();
    }

    @Override
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        super.render(batch);
        if (draggedSpell != null && draggedIcon != null) {
            float w = draggedIcon.getRegionWidth();
            float h = draggedIcon.getRegionHeight();
            batch.draw(
                    draggedIcon.getTexture(),
                    draggedX - w * 0.5f,
                    draggedY - h * 0.5f,
                    w,
                    h,
                    draggedIcon.getRegionX(),
                    draggedIcon.getRegionY(),
                    draggedIcon.getRegionWidth(),
                    draggedIcon.getRegionHeight(),
                    false,
                    true
            );
        }
    }

    @Override
    public void onTouchDown(float screenX, float screenY) {
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            SpellBookEntry hit = findSpellAt(screenX, screenY);
            if (hit != null) {
                draggedSpell = hit;
                draggedIcon = loadSpellIcon(hit.spell);
                draggedX = screenX;
                draggedY = screenY;
                return;
            }
        }
        super.onTouchDown(screenX, screenY);
    }

    @Override
    public void onMouseMove(float screenX, float screenY) {
        if (draggedSpell != null) {
            draggedX = screenX;
            draggedY = screenY;
            return;
        }
        super.onMouseMove(screenX, screenY);
    }

    @Override
    public void onTouchUp(float screenX, float screenY) {
        if (draggedSpell != null) {
            if (hud != null) {
                int slot = hud.getQuickSlotAt((int) screenX, (int) screenY);
                if (slot > 0) {
                    assignSpellToQuickSlot(draggedSpell.quickSlotSpellName, slot);
                }
            }
            draggedSpell = null;
            draggedIcon = null;
            return;
        }
        super.onTouchUp(screenX, screenY);
    }

    @Override
    public boolean onKeyDown(int keycode) {
        if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
            GuiManager.close();
            return true;
        }
        return super.onKeyDown(keycode);
    }

    private SpellBookEntry findSpellAt(float screenX, float screenY) {
        int start = currentPage * 4;
        for (int i = 0; i < 4; i++) {
            int index = start + i;
            if (index >= spells.size()) {
                break;
            }
            SpellBookEntry entry = spells.get(index);
            TextureRegion icon = loadSpellIcon(entry.spell);
            if (icon == null) {
                continue;
            }
            float slotX = x + SLOT_POSITIONS[i][0];
            float slotY = y + SLOT_POSITIONS[i][1];
            float iconX = slotX + ICON_OFFSETS[i][0];
            float iconY = slotY + ICON_OFFSETS[i][1];
            if (screenX >= iconX && screenX <= iconX + ICON_ZONE_W
                    && screenY >= iconY && screenY <= iconY + ICON_ZONE_H) {
                return entry;
            }
        }
        return null;
    }

    private TextureRegion loadSpellIcon(SpellData spell) {
        if (spell == null || spell.getIconId() == null || spell.getIconId().isEmpty()) {
            return null;
        }
        try {
            return SpriteLoader.getInstance().getRegionFromSpriteName(spell.getIconId());
        } catch (GameException ignored) {
            return null;
        }
    }

    private void assignSpellToQuickSlot(String spellName, int slot) {
        if (player == null || spellName == null || spellName.isEmpty() || slot <= 0) {
            return;
        }
        List<QuickSlotEntry> quickSlots = player.getQuickSlots();
        if (quickSlots == null) {
            quickSlots = new ArrayList<>();
            player.setQuickSlots(quickSlots);
        }
        QuickSlotEntry target = null;
        for (Iterator<QuickSlotEntry> it = quickSlots.iterator(); it.hasNext();) {
            QuickSlotEntry entry = it.next();
            if (entry == null) {
                it.remove();
                continue;
            }
            if (entry.getSlot() == slot) {
                target = entry;
            } else if (spellName.equals(entry.getSpell())) {
                it.remove();
            }
        }
        if (target == null) {
            quickSlots.add(new QuickSlotEntry(slot, spellName));
        } else {
            target.setSpell(spellName);
        }
        PlayerStateStore.save(player);
    }

    private List<TextureRegion> loadCornerFrames(String baseName) {
        List<TextureRegion> frames = new ArrayList<>();
        try {
            var loader = SpriteLoader.getInstance();
            for (char c = 'a'; c <= 'h'; c++) {
                var region = loader.getRegionFromSpriteName(baseName + "-" + c);
                if (region != null) {
                    frames.add(region);
                }
            }
        } catch (GameException ignored) {
        }
        return frames;
    }

    private static void ensureRegistry() {
        if (registryReady) {
            return;
        }
        registryReady = true;
        for (SpellData data : SpellRegistry.load()) {
            if (data != null && data.getName() != null && !data.getName().isEmpty()) {
                spellByName.put(data.getName(), data);
                if (data.getKey() != null) {
                    spellByName.put(data.getKey(), data);
                }
            }
        }
    }
/**
 * Class representing SpellBookEntry.
 */

    private static final class SpellBookEntry {
        private final String quickSlotSpellName;
        private final SpellData spell;

        private SpellBookEntry(String quickSlotSpellName, SpellData spell) {
            this.quickSlotSpellName = quickSlotSpellName;
            this.spell = spell;
        }
    }
}
