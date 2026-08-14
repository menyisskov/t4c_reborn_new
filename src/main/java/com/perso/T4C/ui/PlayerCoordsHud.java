package com.perso.T4C.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.MyGame;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.player.Player;

/**
 * Small HUD shown at the top-left displaying the player's world coordinates.
 */
public class PlayerCoordsHud {
    private final Player player;
    private final BitmapFont font;

    public PlayerCoordsHud(Player player) {
        this.player = player;
        this.font = ((MyGame) Gdx.app.getApplicationListener()).customFont;
        this.font.getData().setScale(FontManager.logicalScale(0.8f));
        this.font.setColor(Color.WHITE);
    }

    /**
     * Render the coordinates near the top-left corner using the HUD camera.
     * @param batch SpriteBatch already begun with HUD projection
     * @param offsetX horizontal padding from left edge
     * @param offsetY vertical padding from top edge
     */
    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        com.perso.T4C.model.Coordinates coords = player.getCoordinates();
        float px = coords.getX();
        float py = coords.getY();
        int z = coords.getZ();
        int tileX = (int) Math.floor(px / GameConstants.GRID_W);
        int tileY = (int) Math.floor(py / GameConstants.GRID_H);

        String line1 = String.format("X: %.0f  Y: %.0f  Z: %d", px, py, z);
        String line2 = String.format("Tile: %d, %d, %d", tileX, tileY, z);
        String line3 = String.format("FPS: %d", Gdx.graphics.getFramesPerSecond());

        GlyphLayout g1 = new GlyphLayout(font, line1);
        GlyphLayout g2 = new GlyphLayout(font, line2);
        GlyphLayout g3 = new GlyphLayout(font, line3);

        // with setToOrtho(true) y increases downward, so small offset is near top

        font.draw(batch, line1, offsetX, offsetY + g1.height);
        font.draw(batch, line2, offsetX, offsetY + g1.height + g2.height + 4);
        font.draw(batch, line3, offsetX, offsetY + g1.height + g2.height + g3.height + 8);
    }
}
