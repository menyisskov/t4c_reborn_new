package com.perso.T4C.gui.screen;

import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiText;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.config.Paths;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.ResolvedSprite;
import com.perso.T4C.helper.SpriteNameParser;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.player.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
/**
 * Class representing WorldMap.
 */

public class WorldMap extends GuiScreenBase {
    private static final float MAP_AREA_X = 37.0f;
    private static final float MAP_AREA_Y = 54.0f;
    private static final float MAP_AREA_W = 524.0f;
    private static final float MAP_AREA_H = 299.0f;
    private static final float MIN_ZOOM = 1f;
    private static final float MAX_ZOOM = 8f;
    private static final float ZOOM_STEP = 1.15f;
    private static final float PLAYER_INDICATOR_SIZE = 32f;
    private static final int GENERATED_MAP_HEIGHT = 2048;
    private static final byte TERRAIN_UNKNOWN = 0;
    private static final byte TERRAIN_WATER = 1;
    private static final byte TERRAIN_GRASS = 2;
    private static final byte TERRAIN_FOREST = 3;
    private static final byte TERRAIN_ROCK = 4;
    private static final byte TERRAIN_SAND = 5;
    private static final byte TERRAIN_DIRT = 6;
    private static final byte TERRAIN_ROAD = 7;
    private static final byte TERRAIN_BUILDING = 8;
    private static Texture generatedWorldMapTexture;

    private final Player player;
    private TextureRegion fullMap;
    private TextureRegion playerIndicator;
    private float zoom = 1f;
    private float viewCenterX = 0.5f;
    private float viewCenterY = 0.5f;
    private boolean draggingMap = false;
    private float lastDragX = 0f;
    private float lastDragY = 0f;

    public WorldMap(Player player) {
        this.player = player;
        SpriteLoader loader = SpriteLoader.getInstance();
        try {
            background = loader.getRegionFromSpriteName("GUI_Map");
            playerIndicator = loader.getRegionFromSpriteName("GUI_Player_Indicator");
            if (playerIndicator != null) {
                playerIndicator.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
            }
        } catch (GameException ignored) {
            background = null;
            playerIndicator = null;
        }
        fullMap = getGeneratedWorldMap(loader);
        centerOnScreen();
        addCloseButton();
        addLabels();
    }

    public static void preloadGeneratedMap(SpriteLoader spriteLoader) {
        getGeneratedWorldMap(spriteLoader);
    }

    private void addCloseButton() {
        try {
            var loader = SpriteLoader.getInstance();
            var normal = loader.getRegionFromSpriteName("GUI_X_ButtonDown");
            var hover = loader.getRegionFromSpriteName("GUI_X_ButtonHUp");
            var pressed = loader.getRegionFromSpriteName("GUI_X_ButtonUp");
            if (background == null || normal == null || hover == null || pressed == null) {
                return;
            }
            float bx = x + background.getRegionWidth() - normal.getRegionWidth() - 1f;
            float by = y + 1f;
            buttons.add(new GuiButton(normal, hover, pressed, bx, by, GuiManager::close));
        } catch (GameException ignored) {
        }
    }

    private void addLabels() {
        if (player == null || background == null) {
            return;
        }
        labels.add(GuiText.translatedHeader("ui.world_map", "WORLD MAP", x + 260f, y + 6f));
    }

    @Override
    public void render(SpriteBatch batch) {
        renderBackground(batch);
        renderFullMap(batch);
        renderPlayerMarker(batch);
        for (GuiText label : labels) {
            label.render(batch);
        }
        for (GuiButton button : buttons) {
            button.render(batch);
        }
    }

    @Override
    public void onScroll(float amountY, float screenX, float screenY) {
        if (fullMap == null || !containsMap(screenX, screenY)) {
            super.onScroll(amountY, screenX, screenY);
            return;
        }

        MapDrawBounds bounds = getMapDrawBounds();
        MapView view = getMapView();
        float localX = clamp((screenX - bounds.x) / bounds.width);
        float localY = clamp((screenY - bounds.y) / bounds.height);
        float anchorX = view.x + localX * view.width;
        float anchorY = view.y + localY * view.height;

        float previousZoom = zoom;
        zoom = clamp(zoom * (float) Math.pow(ZOOM_STEP, -amountY), MIN_ZOOM, MAX_ZOOM);
        if (zoom == previousZoom) {
            return;
        }

        float mapW = fullMap.getRegionWidth();
        float mapH = fullMap.getRegionHeight();
        float newViewW = mapW / zoom;
        float newViewH = mapH / zoom;
        float newViewX = anchorX - localX * newViewW;
        float newViewY = anchorY - localY * newViewH;
        viewCenterX = (newViewX + newViewW * 0.5f) / mapW;
        viewCenterY = (newViewY + newViewH * 0.5f) / mapH;
        clampViewCenter();
    }

    @Override
    public void onTouchDown(float screenX, float screenY) {
        super.onTouchDown(screenX, screenY);
        if (fullMap != null && containsMap(screenX, screenY) && GdxButtonState.isLeftPressed()) {
            draggingMap = true;
            lastDragX = screenX;
            lastDragY = screenY;
        }
    }

    @Override
    public void onTouchUp(float screenX, float screenY) {
        draggingMap = false;
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

    @Override
    public void onMouseMove(float screenX, float screenY) {
        if (draggingMap && fullMap != null) {
            panMap(screenX - lastDragX, screenY - lastDragY);
            lastDragX = screenX;
            lastDragY = screenY;
            return;
        }
        super.onMouseMove(screenX, screenY);
    }

    private void renderPlayerMarker(SpriteBatch batch) {
        if (player == null || background == null || fullMap == null) {
            return;
        }
        MapDrawBounds bounds = getMapDrawBounds();
        MapView view = getMapView();
        float mapTileWidth = Math.max(1f, player.getMapPixelWidth() / (float) GameConstants.GRID_W);
        float mapTileHeight = Math.max(1f, fullMap.getRegionHeight());
        float ratioX = clamp((player.getCoordinates().getX() / GameConstants.GRID_W) / mapTileWidth);
        float ratioY = clamp((player.getCoordinates().getY() / GameConstants.GRID_H) / mapTileHeight);
        float mapX = ratioX * fullMap.getRegionWidth();
        float mapY = ratioY * fullMap.getRegionHeight();
        if (mapX < view.x || mapX > view.x + view.width || mapY < view.y || mapY > view.y + view.height) {
            return;
        }
        float markerX = bounds.x + ((mapX - view.x) / view.width) * bounds.width;
        float markerY = bounds.y + ((mapY - view.y) / view.height) * bounds.height;

        if (playerIndicator != null) {
            drawRegion(
                    batch,
                    playerIndicator,
                    markerX - PLAYER_INDICATOR_SIZE * 0.5f,
                    markerY - PLAYER_INDICATOR_SIZE * 0.5f,
                    PLAYER_INDICATOR_SIZE,
                    PLAYER_INDICATOR_SIZE
            );
        }
    }

    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void renderBackground(SpriteBatch batch) {
        if (background == null) {
            return;
        }
        drawRegion(batch, background, x, y, background.getRegionWidth(), background.getRegionHeight());
    }

    private void renderFullMap(SpriteBatch batch) {
        if (fullMap == null) {
            return;
        }
        MapDrawBounds bounds = getMapDrawBounds();
        MapView view = getMapView();
        batch.draw(
                fullMap.getTexture(),
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height,
                fullMap.getRegionX() + Math.round(view.x),
                fullMap.getRegionY() + Math.round(view.y),
                Math.round(view.width),
                Math.round(view.height),
                false,
                true
        );
    }

    private MapDrawBounds getMapDrawBounds() {
        if (fullMap == null) {
            return new MapDrawBounds(x, y, background.getRegionWidth(), background.getRegionHeight());
        }
        float areaX = x + MAP_AREA_X;
        float areaY = y + MAP_AREA_Y;
        float areaW = MAP_AREA_W;
        float areaH = MAP_AREA_H;
        float mapRatio = fullMap.getRegionWidth() / (float) Math.max(1, fullMap.getRegionHeight());
        float areaRatio = areaW / areaH;
        float drawW;
        float drawH;
        if (mapRatio > areaRatio) {
            drawW = areaW;
            drawH = drawW / mapRatio;
        } else {
            drawH = areaH;
            drawW = drawH * mapRatio;
        }
        return new MapDrawBounds(
                areaX + (areaW - drawW) * 0.5f,
                areaY + (areaH - drawH) * 0.5f,
                drawW,
                drawH
        );
    }

    private void drawRegion(SpriteBatch batch, TextureRegion region, float drawX, float drawY, float drawW, float drawH) {
        batch.draw(
                region.getTexture(),
                drawX,
                drawY,
                drawW,
                drawH,
                region.getRegionX(),
                region.getRegionY(),
                region.getRegionWidth(),
                region.getRegionHeight(),
                false,
                true
        );
    }

    private MapView getMapView() {
        if (fullMap == null) {
            return new MapView(0f, 0f, 1f, 1f);
        }
        clampViewCenter();
        float mapW = fullMap.getRegionWidth();
        float mapH = fullMap.getRegionHeight();
        float viewW = mapW / zoom;
        float viewH = mapH / zoom;
        float viewX = clamp(viewCenterX * mapW - viewW * 0.5f, 0f, mapW - viewW);
        float viewY = clamp(viewCenterY * mapH - viewH * 0.5f, 0f, mapH - viewH);
        return new MapView(viewX, viewY, viewW, viewH);
    }

    private void clampViewCenter() {
        if (fullMap == null) {
            viewCenterX = 0.5f;
            viewCenterY = 0.5f;
            return;
        }
        float halfViewRatio = 0.5f / zoom;
        viewCenterX = clamp(viewCenterX, halfViewRatio, 1f - halfViewRatio);
        viewCenterY = clamp(viewCenterY, halfViewRatio, 1f - halfViewRatio);
    }

    private boolean containsMap(float screenX, float screenY) {
        MapDrawBounds bounds = getMapDrawBounds();
        return screenX >= bounds.x
                && screenX <= bounds.x + bounds.width
                && screenY >= bounds.y
                && screenY <= bounds.y + bounds.height;
    }

    private void panMap(float deltaX, float deltaY) {
        MapDrawBounds bounds = getMapDrawBounds();
        MapView view = getMapView();
        float mapW = fullMap.getRegionWidth();
        float mapH = fullMap.getRegionHeight();
        viewCenterX -= (deltaX / Math.max(1f, bounds.width)) * (view.width / mapW);
        viewCenterY -= (deltaY / Math.max(1f, bounds.height)) * (view.height / mapH);
        clampViewCenter();
    }

    private static TextureRegion getGeneratedWorldMap(SpriteLoader spriteLoader) {
        if (generatedWorldMapTexture != null) {
            return new TextureRegion(generatedWorldMapTexture);
        }
        try (MapReader reader = MapReader.spriteNamesOnly(new File(Paths.WORLDMAP_MAPBIN))) {
            int width = reader.getWidth();
            int height = Math.min(GENERATED_MAP_HEIGHT, reader.getHeight());
            Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            int[] colors = new int[width * height];
            byte[] terrains = new byte[width * height];
            Map<String, Byte> terrainBySprite = new HashMap<>();
            Map<String, SpriteLoader.Sprite> metaByName = new HashMap<>();
            for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
                metaByName.put(sprite.getName().toLowerCase(Locale.ROOT), sprite);
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    String spriteName = reader.getSpriteName(x, y);
                    int index = y * width + x;
                    terrains[index] = terrainForTile(spriteName, metaByName, terrainBySprite);
                    colors[index] = paletteForTerrain(terrains[index]);
                }
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    pixmap.drawPixel(x, y, stylizeMapPixel(colors, terrains, width, height, x, y, index));
                }
            }

            generatedWorldMapTexture = new Texture(pixmap);
            pixmap.dispose();
            return new TextureRegion(generatedWorldMapTexture);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static byte terrainForTile(String rawName, Map<String, SpriteLoader.Sprite> metaByName,
                                       Map<String, Byte> terrainBySprite) {
        if (rawName == null || rawName.isBlank()) {
            return TERRAIN_UNKNOWN;
        }
        ResolvedSprite resolved = SpriteNameParser.parse(rawName, metaByName);
        String name = resolved != null ? resolved.name : rawName;
        if (name == null || name.isBlank()) {
            return TERRAIN_UNKNOWN;
        }
        String key = name.toLowerCase(Locale.ROOT);
        Byte cached = terrainBySprite.get(key);
        if (cached != null) return cached;
        byte terrain = classifyTerrain(key);
        terrainBySprite.put(key, terrain);
        return terrain;
    }

    private static byte classifyTerrain(String spriteName) {
        if (spriteName.contains("water") || spriteName.contains("sea") || spriteName.contains("ocean")
                || spriteName.contains("river") || spriteName.contains("lake")) {
            return TERRAIN_WATER;
        }
        if (spriteName.contains("road") || spriteName.contains("path") || spriteName.contains("pav")
                || spriteName.contains("street")) {
            return TERRAIN_ROAD;
        }
        if (spriteName.contains("wall") || spriteName.contains("building") || spriteName.contains("house")
                || spriteName.contains("roof") || spriteName.contains("floor") || spriteName.contains("bridge")
                || spriteName.contains("wooden") || spriteName.contains("stucco") || spriteName.contains("brick")) {
            return TERRAIN_BUILDING;
        }
        if (spriteName.contains("tree") || spriteName.contains("bush") || spriteName.contains("forest")
                || spriteName.contains("leaf")) {
            return TERRAIN_FOREST;
        }
        if (spriteName.contains("stone") || spriteName.contains("rock") || spriteName.contains("mount")
                || spriteName.contains("cliff") || spriteName.contains("cave") || spriteName.contains("roche")) {
            return TERRAIN_ROCK;
        }
        if (spriteName.contains("sand") || spriteName.contains("beach") || spriteName.contains("desert")) {
            return TERRAIN_SAND;
        }
        if (spriteName.contains("dirt") || spriteName.contains("ground") || spriteName.contains("mud")
                || spriteName.contains("earth") || spriteName.contains("tmpl")) {
            return TERRAIN_DIRT;
        }
        if (spriteName.contains("grass") || spriteName.contains("field") || spriteName.contains("moss")) {
            return TERRAIN_GRASS;
        }
        return TERRAIN_UNKNOWN;
    }

    private static int stylizeMapPixel(int[] colors, byte[] terrains, int width, int height, int x, int y, int index) {
        byte terrain = terrains[index];
        int color = colors[index];
        int noise = noise2d(x, y);
        color = adjustBrightness(color, ((noise & 0xFF) - 128) * 0.00045f);

        int shade = 0;
        if (x > 0 && y > 0 && x + 1 < width && y + 1 < height) {
            int nw = luminance(colors[(y - 1) * width + x - 1]);
            int se = luminance(colors[(y + 1) * width + x + 1]);
            shade = nw - se;
        }
        color = adjustBrightness(color, clampStatic(shade / 255f, -0.12f, 0.12f));

        boolean water = terrain == TERRAIN_WATER;
        boolean nearWater = hasNeighborTerrain(terrains, width, height, x, y, TERRAIN_WATER);
        boolean nearLand = hasNeighborLand(terrains, width, height, x, y);
        if (water && nearLand) {
            color = blendRgb(color, 0x7FB9B6FF, 0.42f);
        } else if (!water && nearWater) {
            color = blendRgb(color, 0xD8C98AFF, 0.34f);
        }

        if (terrain == TERRAIN_ROCK || terrain == TERRAIN_BUILDING) {
            color = adjustBrightness(color, 0.05f);
        } else if (terrain == TERRAIN_FOREST) {
            color = adjustBrightness(color, -0.04f);
        }
        return color;
    }

    private static int paletteForTerrain(byte terrain) {
        return switch (terrain) {
            case TERRAIN_WATER -> 0x2F6F8FFF;
            case TERRAIN_GRASS -> 0x6B8F42FF;
            case TERRAIN_FOREST -> 0x355F35FF;
            case TERRAIN_ROCK -> 0x7B7769FF;
            case TERRAIN_SAND -> 0xC9AE72FF;
            case TERRAIN_DIRT -> 0x8B744BFF;
            case TERRAIN_ROAD -> 0xD1BE8BFF;
            case TERRAIN_BUILDING -> 0xA08C6AFF;
            default -> 0x7D8B59FF;
        };
    }

    private static boolean hasNeighborTerrain(byte[] terrains, int width, int height, int x, int y, byte target) {
        for (int dy = -1; dy <= 1; dy++) {
            int ny = y + dy;
            if (ny < 0 || ny >= height) {
                continue;
            }
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = x + dx;
                if (nx >= 0 && nx < width && terrains[ny * width + nx] == target) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasNeighborLand(byte[] terrains, int width, int height, int x, int y) {
        for (int dy = -1; dy <= 1; dy++) {
            int ny = y + dy;
            if (ny < 0 || ny >= height) {
                continue;
            }
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = x + dx;
                if (nx < 0 || nx >= width) {
                    continue;
                }
                byte terrain = terrains[ny * width + nx];
                if (terrain != TERRAIN_WATER && terrain != TERRAIN_UNKNOWN) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int blendRgb(int a, int b, float t) {
        t = clampStatic(t, 0f, 1f);
        int ar = (a >>> 24) & 0xFF;
        int ag = (a >>> 16) & 0xFF;
        int ab = (a >>> 8) & 0xFF;
        int br = (b >>> 24) & 0xFF;
        int bg = (b >>> 16) & 0xFF;
        int bb = (b >>> 8) & 0xFF;
        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int blue = Math.round(ab + (bb - ab) * t);
        return (r << 24) | (g << 16) | (blue << 8) | 0xFF;
    }

    private static int adjustBrightness(int color, float amount) {
        int r = (color >>> 24) & 0xFF;
        int g = (color >>> 16) & 0xFF;
        int b = (color >>> 8) & 0xFF;
        if (amount >= 0f) {
            r += Math.round((255 - r) * amount);
            g += Math.round((255 - g) * amount);
            b += Math.round((255 - b) * amount);
        } else {
            float factor = 1f + amount;
            r = Math.round(r * factor);
            g = Math.round(g * factor);
            b = Math.round(b * factor);
        }
        return (clamp255(r) << 24) | (clamp255(g) << 16) | (clamp255(b) << 8) | 0xFF;
    }

    private static int luminance(int color) {
        int r = (color >>> 24) & 0xFF;
        int g = (color >>> 16) & 0xFF;
        int b = (color >>> 8) & 0xFF;
        return (r * 30 + g * 59 + b * 11) / 100;
    }

    private static int noise2d(int x, int y) {
        int n = x * 374761393 + y * 668265263;
        n = (n ^ (n >>> 13)) * 1274126177;
        return (n ^ (n >>> 16)) & 0xFF;
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static float clampStatic(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
/**
 * Class representing GdxButtonState.
 */

    private static final class GdxButtonState {
        private GdxButtonState() {
        }

        private static boolean isLeftPressed() {
            return com.badlogic.gdx.Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        }
    }
/**
 * Record for MapDrawBounds.
 */

    private record MapDrawBounds(float x, float y, float width, float height) {
    }
/**
 * Record for MapView.
 */

    private record MapView(float x, float y, float width, float height) {
    }
}
