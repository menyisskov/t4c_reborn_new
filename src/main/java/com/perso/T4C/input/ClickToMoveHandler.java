package com.perso.T4C.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.TimeUtils;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.model.QuickSlotEntry;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.PlayerHUD;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.i18n.I18n;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
/**
 * Class representing ClickToMoveHandler.
 */

public class ClickToMoveHandler extends InputAdapter {
    private static final float QUICKBAR_DRAG_THRESHOLD = 6f;

    private final Player player;
    private final PlayerHUD hud;
    private final BiConsumer<SpellData, Integer> quickbarSpellCallback;
    private final BiConsumer<String, Integer> quickbarItemCallback;
    private long lastQuickbarClickMs = 0L;
    private int lastQuickbarSlot = 0;
    private int pressedQuickbarSlot = 0;
    private float pressedQuickbarX = 0f;
    private float pressedQuickbarY = 0f;
    private boolean draggingQuickbarSlot = false;

    public ClickToMoveHandler(OrthographicCamera camera, MapReader reader, Player player, PlayerHUD hud,
            BiConsumer<SpellData, Integer> quickbarSpellCallback, BiConsumer<String, Integer> quickbarItemCallback) {
        this.player = player;
        this.hud = hud;
        this.quickbarSpellCallback = quickbarSpellCallback;
        this.quickbarItemCallback = quickbarItemCallback;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT) {
            return false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            return false;
        }
        if (player == null) {
            return false;
        }
        if (hud != null) {
            int slot = hud.getQuickSlotAt(screenX, screenY);
            if (slot > 0) {
                pressedQuickbarSlot = slot;
                pressedQuickbarX = screenX;
                pressedQuickbarY = screenY;
                draggingQuickbarSlot = false;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pressedQuickbarSlot <= 0) {
            return false;
        }
        float dx = screenX - pressedQuickbarX;
        float dy = screenY - pressedQuickbarY;
        if ((dx * dx) + (dy * dy) >= QUICKBAR_DRAG_THRESHOLD * QUICKBAR_DRAG_THRESHOLD) {
            if (!draggingQuickbarSlot && hud != null) {
                hud.startQuickSlotDrag(pressedQuickbarSlot, screenX, screenY);
            }
            draggingQuickbarSlot = true;
        }
        if (draggingQuickbarSlot && hud != null) {
            hud.updateQuickSlotDrag(screenX, screenY);
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT || pressedQuickbarSlot <= 0) {
            return false;
        }
        int slot = pressedQuickbarSlot;
        boolean wasDragging = draggingQuickbarSlot;
        pressedQuickbarSlot = 0;
        draggingQuickbarSlot = false;
        if (hud != null) {
            hud.stopQuickSlotDrag();
        }

        if (wasDragging) {
            if (hud != null && !hud.isQuickBarHit(screenX, screenY)) {
                removeSpellFromQuickSlot(slot);
            }
            return true;
        }

        handleQuickbarClick(slot);
        return true;
    }

    private void handleQuickbarClick(int slot) {
        long now = TimeUtils.millis();
        Gdx.app.log("ClickToMove", "handleQuickbarClick slot=" + slot + " lastSlot=" + lastQuickbarSlot + " elapsed=" + (now - lastQuickbarClickMs) + "ms");
        if (slot == lastQuickbarSlot && (now - lastQuickbarClickMs) <= 350L) {
            String itemName = hud == null ? null : hud.getItemForSlot(slot);
            if (itemName != null && quickbarItemCallback != null) {
                quickbarItemCallback.accept(itemName, slot);
                lastQuickbarSlot = 0;
                lastQuickbarClickMs = 0L;
                return;
            }
            SpellData spell = hud == null ? null : hud.getSpellDataForSlot(slot);
            Gdx.app.log("ClickToMove", "DOUBLE CLICK detected! spell=" + (spell == null ? "null"
                    : I18n.key(spell.getKey(), I18n.resolve(spell.getName()))
                    + " [" + spell.getKey() + "]"));
            if (spell != null && quickbarSpellCallback != null) {
                quickbarSpellCallback.accept(spell, slot);
            }
            lastQuickbarSlot = 0;
            lastQuickbarClickMs = 0L;
            return;
        }
        lastQuickbarSlot = slot;
        lastQuickbarClickMs = now;
    }

    private void removeSpellFromQuickSlot(int slot) {
        List<QuickSlotEntry> quickSlots = player.getQuickSlots();
        if (quickSlots == null || quickSlots.isEmpty()) {
            return;
        }
        boolean removed = false;
        for (Iterator<QuickSlotEntry> it = quickSlots.iterator(); it.hasNext();) {
            QuickSlotEntry entry = it.next();
            if (entry == null || entry.getSlot() == slot) {
                it.remove();
                removed = true;
            }
        }
        if (removed) {
            if (hud != null) {
                hud.setSelectedQuickSlot(0);
            }
            lastQuickbarSlot = 0;
            lastQuickbarClickMs = 0L;
            PlayerStateStore.save(player);
        }
    }
}
