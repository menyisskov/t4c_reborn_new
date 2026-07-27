package com.perso.T4C.gui.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
/**
 * Class representing GuiManager.
 */

public final class GuiManager {
    private static GuiScreenBase current;

    private GuiManager() {
    }

    public static void open(GuiScreenBase screen) {
        current = screen;
    }

    public static void close() {
        current = null;
    }

    public static boolean isOpen() {
        return current != null;
    }

    public static boolean isCurrent(Class<? extends GuiScreenBase> type) {
        return type != null && type.isInstance(current);
    }

    public static void render(SpriteBatch batch) {
        if (current != null) {
            current.render(batch);
        }
    }

    public static void onTouchDown(float screenX, float screenY) {
        if (current != null) {
            current.onTouchDown(screenX, screenY);
        }
    }

    public static void onTouchDown(float screenX, float screenY, int button) {
        if (current != null) {
            current.onTouchDown(screenX, screenY, button);
        }
    }

    public static void onTouchUp(float screenX, float screenY) {
        if (current != null) {
            // Finish a Ctrl+drag layout tweak before any screen-specific click
            // handling, so every screen logs the moved element's offset.
            if (current.releaseDraggedElement()) {
                return;
            }
            current.onTouchUp(screenX, screenY);
        }
    }

    public static void onMouseMove(float screenX, float screenY) {
        if (current != null) {
            current.onMouseMove(screenX, screenY);
        }
    }

    public static void onScroll(float amountY, float screenX, float screenY) {
        if (current != null) {
            current.onScroll(amountY, screenX, screenY);
        }
    }

    public static boolean onKeyDown(int keycode) {
        if (current != null) {
            return current.onKeyDown(keycode);
        }
        return false;
    }
}
