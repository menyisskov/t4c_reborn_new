package com.perso.T4C.gui.core;

import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiInventory;
import com.perso.T4C.gui.widget.GuiPlayerPart;
import com.perso.T4C.gui.widget.GuiPlayerPreview;
import com.perso.T4C.gui.widget.GuiText;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/**
 * Class representing GuiScreenBase.
 */

public abstract class GuiScreenBase {
    protected TextureRegion background;
    protected float x;
    protected float y;
    protected final List<GuiButton> buttons = new ArrayList<>();
    protected final List<GuiText> labels = new ArrayList<>();
    protected final List<GuiAnimatedSprite> animatedSprites = new ArrayList<>();
    protected final List<GuiPlayerPreview> previews = new ArrayList<>();
    protected final List<GuiPlayerPart> playerParts = new ArrayList<>();
    protected final List<GuiInventory> inventories = new ArrayList<>();
    private GuiElement draggedElement;
    private float dragOffsetX;
    private float dragOffsetY;

    protected void centerOnScreen() {
        if (background == null) {
            return;
        }
        float w = background.getRegionWidth();
        float h = background.getRegionHeight();
        x = (Gdx.graphics.getWidth() - w) / 2f;
        y = (Gdx.graphics.getHeight() - h) / 2f;
    }

    public void render(SpriteBatch batch) {
        if (background != null) {
            GuiDraw.drawRegionFlipped(batch, background, x, y);
        }
        for (GuiElement element : orderedElements()) {
            element.render(batch);
        }
    }

    /** Bouton de fermeture standard GUI_X en (x + dx, y + dy). Null-safe. */
    protected void addCloseButton(float dx, float dy) {
        TextureRegion normal = GuiSprites.load("GUI_X_ButtonDown");
        TextureRegion hover = GuiSprites.load("GUI_X_ButtonHUp");
        TextureRegion pressed = GuiSprites.load("GUI_X_ButtonUp");
        if (background == null || normal == null || hover == null || pressed == null) {
            return;
        }
        buttons.add(new GuiButton(normal, hover, pressed, x + dx, y + dy, GuiManager::close));
    }

    public void onTouchDown(float screenX, float screenY) {
        if (isDragModifierDown()) {
            GuiElement candidate = findTopmostElement(screenX, screenY);
            if (candidate != null) {
                draggedElement = candidate;
                dragOffsetX = screenX - candidate.getX();
                dragOffsetY = screenY - candidate.getY();
                return;
            }
        }
        for (GuiElement element : new ArrayList<>(orderedElements())) {
            element.onTouchDown(screenX, screenY);
        }
    }

    public void onTouchDown(float screenX, float screenY, int button) {
        onTouchDown(screenX, screenY);
    }

    public void onTouchUp(float screenX, float screenY) {
        if (releaseDraggedElement()) {
            return;
        }
        for (GuiElement element : new ArrayList<>(orderedElements())) {
            element.onTouchUp(screenX, screenY);
        }
    }

    public void onMouseMove(float screenX, float screenY) {
        if (draggedElement != null) {
            draggedElement.setPosition(screenX - dragOffsetX, screenY - dragOffsetY);
            return;
        }
        for (GuiElement element : orderedElements()) {
            element.onMouseMove(screenX, screenY);
        }
    }

    protected List<GuiElement> orderedElements() {
        List<GuiElement> elements = new ArrayList<>();
        elements.addAll(animatedSprites);
        elements.addAll(previews);
        elements.addAll(playerParts);
        elements.addAll(inventories);
        elements.addAll(labels);
        elements.addAll(buttons);
        elements.addAll(extraElements());
        return elements;
    }

    protected List<GuiElement> extraElements() {
        return List.of();
    }

    private GuiElement findTopmostElement(float screenX, float screenY) {
        List<GuiElement> elements = orderedElements();
        for (int i = elements.size() - 1; i >= 0; i--) {
            GuiElement element = elements.get(i);
            if (element.contains(screenX, screenY)) {
                return element;
            }
        }
        return null;
    }

    private boolean isDragModifierDown() {
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }

    protected boolean releaseDraggedElement() {
        if (draggedElement == null) {
            return false;
        }
        try {
            float baseX = x;
            float baseY = y;
            float offsetX = draggedElement.getX() - baseX;
            float offsetY = draggedElement.getY() - baseY;
            Gdx.app.log(
                    "GuiScreen",
                    String.format(
                            Locale.ROOT,
                            "Element moved to x + %.1ff, y + %.1ff",
                            offsetX,
                            offsetY
                    )
            );
        } catch (Throwable ignored) {
        }
        draggedElement = null;
        return true;
    }

    public void onScroll(float amountY, float screenX, float screenY) {
        for (GuiInventory inventory : inventories) {
            if (inventory.contains(screenX, screenY)) {
                inventory.onScroll(amountY);
                return;
            }
        }
    }

    public boolean onKeyDown(int keycode) {
        return false;
    }
}
