package com.perso.T4C.gui.widget;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiResizable;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.helper.OriginalRtMap;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.player.Player;

import java.io.IOException;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

/** Original RT map view, centered on the player and deliberately without exploration fog. */
public final class GuiWorldMap extends AbstractGuiElement implements GuiResizable {
    private final Player player;
    private final TextureRegion playerMarker;
    private final boolean[] visibleMapPixels;
    private Texture mapTexture;
    private TextureRegion mapRegion;
    private int renderedTileX = Integer.MIN_VALUE;
    private int renderedTileY = Integer.MIN_VALUE;
    private int renderedWorld = Integer.MIN_VALUE;
    private float width;
    private float height;

    public GuiWorldMap(Player player, float x, float y, float width, float height) {
        super(x, y);
        this.player = player;
        this.playerMarker = GuiSprites.load("GUI_back_TMI_PPos");
        this.visibleMapPixels = loadVisibilityMask();
        this.width = width;
        this.height = height;
        refreshMap();
    }

    @Override
    public void render(SpriteBatch batch) {
        refreshMap();
        if (mapRegion != null) GuiDraw.drawRegionFlipped(batch, mapRegion, x, y, width, height);
        if (playerMarker != null) {
            GuiDraw.drawRegionFlipped(batch, playerMarker,
                    x + width * (268f / OriginalRtMap.VIEW_WIDTH),
                    y + height * (169f / OriginalRtMap.VIEW_HEIGHT),
                    width * (116f / OriginalRtMap.VIEW_WIDTH),
                    height * (113f / OriginalRtMap.VIEW_HEIGHT));
        }
        GuiBoxedItem.drawDebugBorder(batch, x, y, width, height);
    }

    public void dispose() {
        if (mapTexture != null) mapTexture.dispose();
        mapTexture = null;
        mapRegion = null;
    }

    private void refreshMap() {
        if (player == null) return;
        int tileX = (int) (player.getCoordinates().getX() / GRID_W);
        int tileY = (int) (player.getCoordinates().getY() / GRID_H);
        int world = player.getCoordinates().getZ();
        if (tileX == renderedTileX && tileY == renderedTileY && world == renderedWorld) return;
        try {
            Pixmap pixmap = OriginalRtMap.createView(world, tileX, tileY);
            applyVisibilityMask(pixmap);
            Texture replacement = new Texture(pixmap);
            replacement.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            pixmap.dispose();
            if (mapTexture != null) mapTexture.dispose();
            mapTexture = replacement;
            mapRegion = new TextureRegion(mapTexture);
            renderedTileX = tileX;
            renderedTileY = tileY;
            renderedWorld = world;
        } catch (IOException ignored) {
            // Keep the last valid frame if the original map resource cannot be read.
        }
    }

    private boolean[] loadVisibilityMask() {
        boolean[] visible = new boolean[OriginalRtMap.VIEW_WIDTH * OriginalRtMap.VIEW_HEIGHT];
        java.util.Arrays.fill(visible, true);
        Pixmap mask = null;
        try {
            mask = SpriteLoader.getInstance().createPixmapForSprite("GUI_RTMapMask");
            if (mask == null) return visible;
            for (int py = 0; py < OriginalRtMap.VIEW_HEIGHT; py++) {
                int maskY = Math.min(mask.getHeight() - 1,
                        py * mask.getHeight() / OriginalRtMap.VIEW_HEIGHT);
                for (int px = 0; px < OriginalRtMap.VIEW_WIDTH; px++) {
                    int maskX = Math.min(mask.getWidth() - 1,
                            px * mask.getWidth() / OriginalRtMap.VIEW_WIDTH);
                    visible[py * OriginalRtMap.VIEW_WIDTH + px] = (mask.getPixel(maskX, maskY) & 0xff) != 0;
                }
            }
        } catch (Exception ignored) {
            java.util.Arrays.fill(visible, true);
        } finally {
            if (mask != null) mask.dispose();
        }
        return visible;
    }

    private void applyVisibilityMask(Pixmap pixmap) {
        pixmap.setBlending(Pixmap.Blending.None);
        for (int py = 0; py < OriginalRtMap.VIEW_HEIGHT; py++) {
            int row = py * OriginalRtMap.VIEW_WIDTH;
            for (int px = 0; px < OriginalRtMap.VIEW_WIDTH; px++) {
                if (!visibleMapPixels[row + px]) pixmap.drawPixel(px, py, 0);
            }
        }
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public GuiWorldMap setSize(float width, float height) {
        this.width = Math.max(8f, width);
        this.height = Math.max(8f, height);
        return this;
    }
}
