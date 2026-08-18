package com.perso.T4C.screens;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.perso.T4C.config.Paths;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.helper.ObjectPositionBinaryIO;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.objects.ObjectPos;
import com.perso.T4C.render.DecorFlags;
import com.perso.T4C.render.DecorRenderer;
import com.perso.T4C.render.GroundRenderer;
import com.perso.T4C.render.ObjectMapping;
import com.perso.T4C.render.ObjectMappings;
import com.perso.T4C.render.ObjectRenderer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("unused")
public class MapRenderer {
  @Getter private final MapReader reader;
  private final SpriteLoader spriteLoader;
  private final SpriteBatch batchSol;
  private final SpriteBatch batchDecor;
  private final Integer worldZ;
  @Getter private List<ObjectPos> objectPositions;
  private final ShaderProgram outlineShader;
  @Getter private final Map<String, SpriteLoader.Sprite> metaByName = new HashMap<>();
  @Getter private final Map<String, ObjectMapping> objectMappings = new HashMap<>();
  @Getter private GroundRenderer groundRenderer;
  private DecorRenderer decorRenderer;
  @Getter private ObjectRenderer objectRenderer;
  private boolean decorUseTileOffsets = true;
  private Map<String, int[]> decorOffsetOverrides = java.util.Collections.emptyMap();
  @Setter @Getter private boolean decorVisible = true;
  @Getter private boolean objectsVisible = true;
  private final Set<String> flaggedDecorNames = new HashSet<>();

  public MapRenderer(
      MapReader reader,
      SpriteLoader spriteLoader,
      SpriteBatch batchSol,
      SpriteBatch batchDecor,
      ShaderProgram outlineShader,
      ModifSprites modifSprites)
      throws GameException {
    this(reader, spriteLoader, batchSol, batchDecor, outlineShader, modifSprites, null);
  }

  public MapRenderer(
      MapReader reader,
      SpriteLoader spriteLoader,
      SpriteBatch batchSol,
      SpriteBatch batchDecor,
      ShaderProgram outlineShader,
      ModifSprites modifSprites,
      Integer worldZ)
      throws GameException {
    this.reader = reader;
    this.spriteLoader = spriteLoader;
    this.batchSol = batchSol;
    this.batchDecor = batchDecor;
    this.outlineShader = outlineShader;
    this.worldZ = worldZ;
    for (SpriteLoader.Sprite s : spriteLoader.getSprites()) {
      metaByName.put(s.getName().toLowerCase(Locale.ROOT), s);
    }
    objectMappings.putAll(loadMappings());
    this.objectPositions = loadObjectPositions(objectMappings, worldZ);
    flaggedDecorNames.clear();
    for (Map.Entry<String, ObjectMapping> e : objectMappings.entrySet()) {
      if (e.getValue() != null && e.getValue().alwaysBehindEntities) {
        flaggedDecorNames.add(e.getKey().toLowerCase(Locale.ROOT));
      }
    }
    for (String name : DecorFlags.loadPlayerAlwaysAboveRules()) {
      if (name != null && !name.isEmpty()) {
        flaggedDecorNames.add(name.toLowerCase(Locale.ROOT));
      }
    }
    this.groundRenderer = new GroundRenderer(reader, spriteLoader, batchSol, metaByName);
    this.decorRenderer =
        new DecorRenderer(reader, spriteLoader, batchDecor, metaByName, flaggedDecorNames);
    this.decorRenderer.setUseTileOffsets(decorUseTileOffsets);
    this.decorRenderer.setOffsetOverrides(decorOffsetOverrides);
    this.objectRenderer = new ObjectRenderer(spriteLoader, metaByName, modifSprites, outlineShader);
    this.objectRenderer.setOffsetOverrides(decorOffsetOverrides);
  }

  private static Map<String, ObjectMapping> loadMappings() throws GameException {
    return ObjectMappings.load();
  }

  private static List<ObjectPos> loadObjectPositions(
      Map<String, ObjectMapping> objectMappings, Integer worldZ) throws GameException {
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
        if (position != null
            && position.name() != null
            && (worldZ == null || (int) position.z() == worldZ)
            && objectMappings.containsKey(position.name().toUpperCase(Locale.ROOT))) {
          mappedPositions.add(position);
        }
      }
      return mappedPositions;
    } catch (IOException e) {
      throw new GameException(
          "Failed to load object positions from: " + Paths.OBJECT_POSITIONS_BIN, e);
    }
  }

  public void renderGroundOnly(
      OrthographicCamera camera, int startX, int endX, int startY, int endY) {
    batchSol.setProjectionMatrix(camera.combined);
    batchSol.begin();
    groundRenderer.renderGroundInternal(startX, endX, startY, endY);
    groundRenderer.renderGroundBelowDecorInternal(startX, endX, startY, endY, !decorVisible);
    groundRenderer.renderSmoothingInternal(startX, endX, startY, endY, !decorVisible);
    batchSol.end();
  }

  public void renderDecorsBehindPlayer(
      OrthographicCamera camera,
      float playerY,
      float mouseX,
      float mouseY,
      int startX,
      int endX,
      int startY,
      int endY) {
    batchDecor.setProjectionMatrix(camera.combined);
    batchDecor.begin();
    if (decorVisible) {
      decorRenderer.renderFlaggedDecorsInternal(startX, endX, startY, endY);
    }
    if (objectsVisible && !objectPositions.isEmpty()) {
      objectRenderer.renderObjectsBehindPlayerByBehindFlag(
          batchDecor, objectPositions, objectMappings, mouseX, mouseY, playerY, true);
    }
    if (decorVisible) {
      decorRenderer.renderDecorConditionalInternal(startX, endX, startY, endY, playerY, false);
    }
    if (objectsVisible && !objectPositions.isEmpty()) {
      objectRenderer.renderObjectsBehindPlayerByBehindFlag(
          batchDecor, objectPositions, objectMappings, mouseX, mouseY, playerY, false);
    }
    batchDecor.end();
  }

  public void renderDecorsInFrontOfPlayer(
      OrthographicCamera camera,
      float playerY,
      float mouseX,
      float mouseY,
      int startX,
      int endX,
      int startY,
      int endY) {
    batchDecor.setProjectionMatrix(camera.combined);
    batchDecor.begin();
    if (objectsVisible && !objectPositions.isEmpty()) {
      objectRenderer.renderObjectsInternalByBehindFlag(
          batchDecor, objectPositions, objectMappings, mouseX, mouseY, playerY, true);
    }
    if (decorVisible) {
      decorRenderer.renderDecorConditionalInternal(startX, endX, startY, endY, playerY, true);
    }
    if (objectsVisible && !objectPositions.isEmpty()) {
      objectRenderer.renderObjectsInternalByBehindFlag(
          batchDecor, objectPositions, objectMappings, mouseX, mouseY, playerY, false);
    }
    batchDecor.end();
  }

  public void renderEntitiesWithDecors(
      OrthographicCamera camera,
      float mouseX,
      float mouseY,
      int startX,
      int endX,
      int startY,
      int endY,
      java.util.List<ObjectRenderer.RenderItem> entityItems) {
    batchDecor.setProjectionMatrix(camera.combined);
    batchDecor.begin();
    if (decorVisible) {
      decorRenderer.renderFlaggedDecorsInternal(startX, endX, startY, endY);
    }
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

  public void warmCaches(
      int startX,
      int endX,
      int startY,
      int endY,
      int chunkBudget,
      int tmplBudget,
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
    objectPositions = loadObjectPositions(objectMappings, worldZ);
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
    objectPositions = loadObjectPositions(objectMappings, worldZ);
    recomputeFlaggedDecorNames();
    if (objectRenderer != null) {
      objectRenderer.reset();
    }
  }

  public void reload(ModifSprites modifSprites) throws GameException {
    com.perso.T4C.render.TileCache.clearAll();
    try {
      spriteLoader.clearTextureCaches();
    } catch (Throwable ignored) {
    }
    metaByName.clear();
    for (SpriteLoader.Sprite s : spriteLoader.getSprites()) {
      metaByName.put(s.getName().toLowerCase(Locale.ROOT), s);
    }
    objectMappings.clear();
    objectMappings.putAll(loadMappings());
    objectPositions = loadObjectPositions(objectMappings, worldZ);
    recomputeFlaggedDecorNames();
    this.groundRenderer = new GroundRenderer(reader, spriteLoader, batchSol, metaByName);
    this.decorRenderer =
        new DecorRenderer(reader, spriteLoader, batchDecor, metaByName, flaggedDecorNames);
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

  public void dispose() {
    if (groundRenderer != null) groundRenderer.disposeOutlineResources();
  }
}
