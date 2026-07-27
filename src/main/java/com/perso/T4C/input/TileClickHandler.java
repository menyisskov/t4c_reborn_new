package com.perso.T4C.input;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.perso.T4C.helper.MapReader;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

/**
 * Handles tile clicking functionality, specifically for Tmpl tiles.
 * Detects left-clicks on tiles and displays information for Tmpl tiles.
 */
public class TileClickHandler extends InputAdapter {

    private final MapReader reader;
    private final OrthographicCamera camera;

    public TileClickHandler(MapReader reader, OrthographicCamera camera) {
        this.reader = reader;
        this.camera = camera;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == com.badlogic.gdx.Input.Buttons.LEFT) {
            Vector3 worldCoords = screenToWorldCoords(screenX, screenY);
            int tileX = (int) (worldCoords.x / GRID_W);
            int tileY = (int) (worldCoords.y / GRID_H);

            // Check if coordinates are valid
            if (tileX >= 0 && tileY >= 0 && tileX < reader.getWidth() && tileY < reader.getHeight()) {
                String name = reader.getSpriteName(tileX, tileY);
                if (name != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Converts screen coordinates to world coordinates using the camera.
     *
     * @param screenX screen X coordinate
     * @param screenY screen Y coordinate
     * @return world coordinates as a Vector3
     */
    private Vector3 screenToWorldCoords(int screenX, int screenY) {
        return camera.unproject(new Vector3(screenX, screenY, 0));
    }
}
