package com.perso.T4C.gui.widget;

import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiResizable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.List;
import java.util.Objects;
/**
 * Class representing GuiAnimatedSprite.
 */

public class GuiAnimatedSprite extends AbstractGuiElement implements GuiResizable {
    private final List<TextureRegion> frames;
    private final float frameTime;
    private float timer;
    private boolean hovered;
    private boolean isPressed;
    private Runnable onClick;
    private float zoneWidth;
    private float zoneHeight;

    public GuiAnimatedSprite(List<TextureRegion> frames, float x, float y, float frameTime) {
        super(x, y);
        this.frames = Objects.requireNonNull(frames);
        this.frameTime = frameTime;
    }

    public GuiAnimatedSprite(List<TextureRegion> frames, float x, float y, float frameTime, Runnable onClick) {
        this(frames, x, y, frameTime);
        this.onClick = onClick;
    }

    /** Centers each frame inside a {@code width}×{@code height} zone anchored at (x, y). */
    public GuiAnimatedSprite boxed(float width, float height) {
        this.zoneWidth = Math.max(1f, width);
        this.zoneHeight = Math.max(1f, height);
        return this;
    }

    @Override
    public GuiAnimatedSprite setSize(float width, float height) {
        return boxed(width, height);
    }

    public void render(SpriteBatch batch) {
        if (frames.isEmpty()) {
            return;
        }
        GuiBoxedItem.drawDebugBorder(batch, x, y, zoneWidth, zoneHeight);
        TextureRegion region = getCurrentFrame();
        float dx = x;
        float dy = y;
        if (zoneWidth > 0) {
            dx = x + (zoneWidth - region.getRegionWidth()) / 2f;
            dy = y + (zoneHeight - region.getRegionHeight()) / 2f;
        }
        GuiDraw.drawRegionFlipped(batch, region, dx, dy);
    }

    public void onMouseMove(float screenX, float screenY) {
        hovered = contains(screenX, screenY);
        if (!hovered) {
            timer = 0f;
        }
    }

    public void onTouchDown(float screenX, float screenY) {
        isPressed = contains(screenX, screenY);
    }

    public void onTouchUp(float screenX, float screenY) {
        if (isPressed && contains(screenX, screenY) && onClick != null) {
            onClick.run();
        }
        isPressed = false;
    }

    @Override
    public float getWidth() {
        return zoneWidth > 0f ? zoneWidth : (frames.isEmpty() ? 0f : frames.get(0).getRegionWidth());
    }

    @Override
    public float getHeight() {
        return zoneHeight > 0f ? zoneHeight : (frames.isEmpty() ? 0f : frames.get(0).getRegionHeight());
    }

    private TextureRegion getCurrentFrame() {
        if (!hovered || frames.size() == 1) {
            return frames.get(0);
        }
        timer += Gdx.graphics.getDeltaTime();
        int index = (int) (timer / frameTime) % frames.size();
        return frames.get(index);
    }
}
