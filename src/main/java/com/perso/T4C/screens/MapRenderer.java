package com.perso.T4C.screens;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.perso.T4C.config.Paths;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.helper.ObjectPositionBinaryIO;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.render.DecorRenderer;
import com.perso.T4C.render.DecorFlags;
import com.perso.T4C.render.GroundRenderer;
import com.perso.T4C.render.ObjectMapping;
import com.perso.T4C.render.ObjectMappings;
import com.perso.T4C.render.ObjectRenderer;
import com.perso.T4C.objects.ObjectPos;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Coordinates rendering of the world map: ground, decor and objects.
 * Responsible for loading mapping files and recreating renderers on reload.
 */
@SuppressWarnings("unused")
public class MapRenderer {
    @Getter
    private final MapReader reader;
    private final SpriteLoader spriteLoader;
    private final SpriteBatch batchSol;
    private final SpriteBatch batchDecor;
    @Getter
    private List<ObjectPos> objectPositions;
    private final ShaderProgram outlineShader;

    @Getter
    private final Map<String, SpriteLoader.Sprite> metaByName = new HashMap<>();
    @Getter
    private final Map<String, ObjectMapping> objectMappings = new HashMap<>();

    @Getter
    private GroundRenderer groundRenderer;
    private DecorRenderer decorRenderer;
    @Getter
    private ObjectRenderer objectRenderer;
    private boolean decorUseTileOffsets = true;
    private Map<String, int[]> decorOffsetOverrides = java.util.Collections.emptyMap();

    @Setter
    @Getter
    private boolean decorVisible = true;
    @Getter
    private boolean objectsVisible = true;

    // Precomputed set of decoration logical names (lowercase) that must be drawn before entities.
    private final Set<String> flaggedDecorNames = new HashSet<>();

    /**
     * Create a map renderer with required helpers and resources.
     */
    public MapRenderer(MapReader reader, SpriteLoader spriteLoader, SpriteBatch batchSol, SpriteBatch batchDecor, ShaderProgram outlineShader, ModifSprites modifSprites) throws GameException {
        this.reader = reader;
        this.spriteLoader = spriteLoader;
        this.batchSol = batchSol;
        this.batchDecor = batchDecor;
        this.outlineShader = outlineShader;

        for (SpriteLoader.Sprite s : spriteLoader.getSprites()) {
            metaByName.put(s.getName().toLowerCase(Locale.ROOT), s);
        }

        objectMappings.putAll(loadMappings());
        this.objectPositions = loadObjectPositions(objectMappings);

        // Precompute names of flagged decors from the loaded mappings
        flaggedDecorNames.clear();
        for (Map.Entry<String, ObjectMapping> e : objectMappings.entrySet()) {
            if (e.getValue() != null && e.getValue().alwaysBehindEntities) {
                flaggedDecorNames.add(e.getKey().toLowerCase(Locale.ROOT));
            }
        }

        // Merge decor flags from built-in defaults and the editable binary rules.
        for (String name : DecorFlags.loadPlayerAlwaysAboveRules()) {
            if (name != null && !name.isEmpty()) {
                flaggedDecorNames.add(name.toLowerCase(Locale.ROOT));
            }
        }

        this.groundRenderer = new GroundRenderer(reader, spriteLoader, batchSol, metaByName);
        // Pass flaggedDecorNames to DecorRenderer (new constructor overload)
        this.decorRenderer = new DecorRenderer(reader, spriteLoader, batchDecor, metaByName, flaggedDecorNames);
        this.decorRenderer.setUseTileOffsets(decorUseTileOffsets);
        this.decorRenderer.setOffsetOverrides(decorOffsetOverrides);
        this.objectRenderer = new ObjectRenderer(spriteLoader, metaByName, modifSprites, outlineShader);
        this.objectRenderer.setOffsetOverrides(decorOffsetOverrides);
    }

    private static Map<String, ObjectMapping> loadMappings() throws GameException {
        return ObjectMappings.load();
    }

    private static List<ObjectPos> loadObjectPositions(Map<String, ObjectMapping> objectMappings) throws GameException {
        File file = new File(Paths.OBJECT_POSITIONS_BIN);
        if (!file.exists()) {
            return Collections.emptyList();
        }

        try {
            List<ObjectPos> positions = ObjectPositionBinaryIO.read(file);
            if (positions == null || positions.isEmpty()) {
                return Collections.emptyList();
            }
            List<ObjectPos> mappedPositions = new ArrayList<>();
            for (ObjectPos position : positions) {
                if (position != null && position.name() != null
                        && objectMappings.containsKey(position.name().toUpperCase(Locale.ROOT))) {
                    mappedPositions.add(position);
                }
            }
            return mappedPositions;
        } catch (IOException e) {
            throw new GameException("Failed to load object positions from: " + Paths.OBJECT_POSITIONS_BIN, e);
        }
    }

    /**
     * Render only the ground layers (used to draw player after ground but
     * before decor). All ground rendering is now done in a single batch.
     */
    public void renderGroundOnly(OrthographicCamera camera, int startX, int endX, int startY, int endY) {
        batchSol.setProjectionMatrix(camera.combined);

        // Single batch for all ground rendering - major performance improvement
        batchSol.begin();
        groundRenderer.renderGroundInternal(startX, endX, startY, endY);
        groundRenderer.renderGroundBelowDecorInternal(startX, endX, startY, endY, !decorVisible);
        groundRenderer.renderSmoothingInternal(startX, endX, startY, endY, !decorVisible);
        batchSol.end();
    }

    /**
     * Render decors that are behind the player (drawY < playerY).
     * These should be rendered BEFORE the player so the player appears in front.
     */
    public void renderDecorsBehindPlayer(OrthographicCamera camera, float playerY, float mouseX, float mouseY, int startX, int endX, int startY, int endY) {
        batchDecor.setProjectionMatrix(camera.combined);
        batchDecor.begin();

        // First render flagged decors that must always be behind entities (e.g. TapisRouge)
        if (decorVisible) {
            decorRenderer.renderFlaggedDecorsInternal(startX, endX, startY, endY);
        }

        // Render objects tagged alwaysBehind first (they should stay behind regular decors/entities).
        if (objectsVisible && !objectPositions.isEmpty()) {
            objectRenderer.renderObjectsBehindPlayerByBehindFlag(batchDecor, objectPositions, objectMappings, mouseX, mouseY, playerY, true);
        }

        // Render remaining decors first
        if (decorVisible) {
            decorRenderer.renderDecorConditionalInternal(startX, endX, startY, endY, playerY, false);
        }

        // Then render only regular objects so they stay in front of regular decors.
        if (objectsVisible && !objectPositions.isEmpty()) {
            objectRenderer.renderObjectsBehindPlayerByBehindFlag(batchDecor, objectPositions, objectMappings, mouseX, mouseY, playerY, false);
        }

        batchDecor.end();
    }

    /**
     * Render decors that are in front of the player (drawY >= playerY).
     * These should be rendered AFTER the player so they appear in front.
     * Objects and decors are sorted together by Y position for proper depth ordering.
     */
    public void renderDecorsInFrontOfPlayer(OrthographicCamera camera, float playerY, float mouseX, float mouseY, int startX, int endX, int startY, int endY) {
        batchDecor.setProjectionMatrix(camera.combined);
        batchDecor.begin();

        // Render decors first then objects so objects remain on top of regular decors.
        if (objectsVisible && !objectPositions.isEmpty()) {
            objectRenderer.renderObjectsInternalByBehindFlag(batchDecor, objectPositions, objectMappings, mouseX, mouseY, playerY, true);
        }
        if (decorVisible) {
            decorRenderer.renderDecorConditionalInternal(startX, endX, startY, endY, playerY, true);
        }
        if (objectsVisible && !objectPositions.isEmpty()) {
            objectRenderer.renderObjectsInternalByBehindFlag(batchDecor, objectPositions, objectMappings, mouseX, mouseY, playerY, false);
        }

        batchDecor.end();
    }

    /**
     * Render all entities with decors/objects using unified Y-sorting.
     * Flagged decors are still rendered first to stay behind entities.
     */
    public void renderEntitiesWithDecors(OrthographicCamera camera, float mouseX, float mouseY, int startX, int endX, int startY, int endY, java.util.List<ObjectRenderer.RenderItem> entityItems) {
        batchDecor.setProjectionMatrix(camera.combined);
        batchDecor.begin();

        if (decorVisible) {
            decorRenderer.renderFlaggedDecorsInternal(startX, endX, startY, endY);
        }

        // Always render entities; include objects only if available
        boolean includeObjects = objectsVisible && !objectPositions.isEmpty();
        objectRenderer.renderObjectsDecorsAndEntitiesInternal(
                batchDecor,
                objectPositions,
                objectMappings,
                mouseX,
                mouseY,
                decorRenderer,
                startX,
                endX,
                startY,
                endY,
                includeObjects,
                decorVisible,
                entityItems);

        batchDecor.end();
    }

    public boolean isDoorBlockedAt(float worldX, float worldY) {
        if (objectRenderer == null || objectPositions.isEmpty()) {
            return false;
        }
        return objectRenderer.isDoorBlockedAt(objectPositions, objectMappings, worldX, worldY);
    }

    public void warmCaches(int startX, int endX, int startY, int endY, int chunkBudget, int tmplBudget,
            int decorBudget) {
        if (groundRenderer != null) {
            groundRenderer.preloadChunks(startX, endX, startY, endY, chunkBudget);
            groundRenderer.preloadTmplTiles(startX, endX, startY, endY, tmplBudget);
        }
        if (decorVisible && decorRenderer != null) {
            decorRenderer.preloadDecorCaches(startX, endX, startY, endY, decorBudget);
        }
    }

    public void reloadObjectPositions() throws GameException {
        objectPositions = loadObjectPositions(objectMappings);
        if (objectRenderer != null) {
            objectRenderer.reset();
        }
    }

    public void setObjectPositionsForEditor(List<ObjectPos> positions) {
        objectPositions = positions == null ? Collections.emptyList() : new ArrayList<>(positions);
        if (objectRenderer != null) {
            objectRenderer.reset();
        }
    }

    public void reloadObjectMappings() throws GameException {
        objectMappings.clear();
        objectMappings.putAll(loadMappings());
        objectPositions = loadObjectPositions(objectMappings);
        recomputeFlaggedDecorNames();
        if (objectRenderer != null) {
            objectRenderer.reset();
        }
    }

    /**
     * Reload mapping and sprite metadata, recreate renderers to apply the new data.
     */
    public void reload(ModifSprites modifSprites) throws GameException {
        // Clear global tile caches on resource reload
        com.perso.T4C.render.TileCache.clearAll();

        // Ensure loader runtime caches are reset so disposed textures aren't reused
        try {
            spriteLoader.clearTextureCaches();
        } catch (Throwable ignored) {
        }

        // Reload sprite metadata
        metaByName.clear();
        for (SpriteLoader.Sprite s : spriteLoader.getSprites()) {
            metaByName.put(s.getName().toLowerCase(Locale.ROOT), s);
        }

        objectMappings.clear();
        objectMappings.putAll(loadMappings());
        objectPositions = loadObjectPositions(objectMappings);

        recomputeFlaggedDecorNames();

        // Recreate renderers with fresh data
        this.groundRenderer = new GroundRenderer(reader, spriteLoader, batchSol, metaByName);
        this.decorRenderer = new DecorRenderer(reader, spriteLoader, batchDecor, metaByName, flaggedDecorNames);
        this.decorRenderer.setUseTileOffsets(decorUseTileOffsets);
        this.decorRenderer.setOffsetOverrides(decorOffsetOverrides);
        this.objectRenderer = new ObjectRenderer(spriteLoader, metaByName, modifSprites, outlineShader);
        this.objectRenderer.setOffsetOverrides(decorOffsetOverrides);
        this.objectRenderer.reset();
    }

    private void recomputeFlaggedDecorNames() {
        flaggedDecorNames.clear();
        for (Map.Entry<String, ObjectMapping> e : objectMappings.entrySet()) {
            if (e.getValue() != null && e.getValue().alwaysBehindEntities) {
                flaggedDecorNames.add(e.getKey().toLowerCase(Locale.ROOT));
            }
        }

        // Merge decor flags from built-in defaults and the editable binary rules.
        for (String name : DecorFlags.loadPlayerAlwaysAboveRules()) {
            if (name != null && !name.isEmpty()) {
                flaggedDecorNames.add(name.toLowerCase(Locale.ROOT));
            }
        }
    }

    public void setDecorUseTileOffsets(boolean useTileOffsets) {
        this.decorUseTileOffsets = useTileOffsets;
        if (decorRenderer != null) {
            decorRenderer.setUseTileOffsets(useTileOffsets);
        }
    }

    public void setDecorOffsetOverrides(Map<String, int[]> overrides) {
        this.decorOffsetOverrides = overrides == null ? java.util.Collections.emptyMap() : overrides;
        if (decorRenderer != null) {
            decorRenderer.setOffsetOverrides(this.decorOffsetOverrides);
        }
        if (objectRenderer != null) {
            objectRenderer.setOffsetOverrides(this.decorOffsetOverrides);
        }
    }

    public void invalidateDecorTileCache(int x, int y) {
        if (decorRenderer != null) {
            decorRenderer.invalidateTileCache(x, y);
        }
    }

    public void clearDecorTileCache() {
        if (decorRenderer != null) {
            decorRenderer.clearTileCache();
        }
    }

    public void setObjectsVisible(boolean visible) {
        this.objectsVisible = visible;
        if (!visible && objectRenderer != null) {
            objectRenderer.reset();
        }
    }

    public boolean isGroundOutlineEnabled() {
        return groundRenderer != null && groundRenderer.isOutlineEnabled();
    }

    public void setGroundOutlineEnabled(boolean enabled) {
        if (groundRenderer != null) {
            groundRenderer.setOutlineEnabled(enabled);
        }
    }

    private static int clamp(int high, int val) {
        return Math.max(0, Math.min(high, val));
    }

    /**
     * Dispose of resources used by the map renderer.
     */
    public void dispose() {
        if (groundRenderer != null) groundRenderer.disposeOutlineResources();
    }
}
