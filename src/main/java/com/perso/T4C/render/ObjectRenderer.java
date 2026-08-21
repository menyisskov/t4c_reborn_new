package com.perso.T4C.render;

import java.util.Map;
import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint2;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.entity.NameRenderer;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.objects.ObjectPos;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectRenderer {
  private static final long DOOR_BLOCK_LOG_COOLDOWN_MS = 500L;
  private static final float OCCLUDED_ENTITY_ALPHA = 1f - (125f / 256f);
  private static final float DOOR_OCCLUSION_DEPTH_OFFSET = 1f;
  private static final int OBJECT_CULL_MARGIN_TILES = 8;
  private final SpriteLoader spriteLoader;
  private final Map<String, SpriteLoader.Sprite> metaByName;
  private final ModifSprites modifs;
  private final ShaderProgram outlineShader;
  private final Map<String, List<TextureRegion>> framesByType = new HashMap<>();
  private final Map<String, String[]> spriteNamesByType = new HashMap<>();
  private final Map<String, SpriteLoader.Sprite> metaBySpriteName = new HashMap<>();
  private final Map<String, ModifSprites.Offset> offsetByName = new HashMap<>();
  private final Map<String, ModifSprites.Offset> offsetByNameMirror = new HashMap<>();
  private Map<String, int[]> offsetOverrides = Collections.emptyMap();
  private final Map<ObjectPos, AnimState> animations = new HashMap<>();
  private final Map<ObjectPos, GridPoint2> doorGateCache = new HashMap<>();
  private final Map<Long, List<ObjectPos>> closedDoorsByGateTile = new HashMap<>();
  private final LongHashSet blockedTileSet = new LongHashSet(64);
  private final Map<String, String> upperNameCache = new HashMap<>();
  private final Map<String, String> lowerNameCache = new HashMap<>();
  private final Map<ObjectPos, String> logicalIdByPos = new HashMap<>();
  private boolean doorIndexBuilt = false;
  private boolean hasDoors = false;
  private long lastDoorBlockLogAt = 0L;
  private ObjectPos hoveredKey = null;
  private ObjectPos lastClickedObject = null;
  private static final Logger log = LoggerFactory.getLogger(ObjectRenderer.class);
  private static final Comparator<RenderInfo> RENDER_INFO_SORT =
      Comparator.comparingLong(r -> r.tileY);
  private static final Comparator<RenderItem> RENDER_ITEM_SORT =
      (a, b) -> {
        int depth = Float.compare(a.y, b.y);
        if (depth != 0) {
          return depth;
        }
        return Integer.compare(renderPriority(a), renderPriority(b));
      };
  private final ArrayDeque<RenderItem> renderItemPool = new ArrayDeque<>();
  private final ArrayDeque<RenderInfo> renderInfoPool = new ArrayDeque<>();
  private final ArrayList<RenderItem> combinedItems = new ArrayList<>(2048);
  private final ArrayList<RenderInfo> renderInfos = new ArrayList<>(256);
  private final ArrayList<RenderItem> occlusionRevealItems = new ArrayList<>(64);

  private static class AnimState {
    int index = 0;
    float timer = 0f;
    boolean playing = false;
    boolean reverse = false;
    boolean loop = false;
    float reverseDelay = -1f;
    String animateSound;
    String reverseAnimateSound;
    long nameDisplayUntil;
    String clickedDisplayName;
    long lastAnimationUpdateFrame = -1L;
  }

  static class RenderInfo {
    TextureRegion region;
    float px;
    float py;
    float w;
    float h;
    String spriteName;
    ObjectPos uniqueKey;
    boolean mirror;
    long tileY;
    ObjectMapping mapping;
    AnimState state;
  }

  public ObjectRenderer(
      SpriteLoader spriteLoader,
      Map<String, SpriteLoader.Sprite> metaByName,
      ModifSprites modifs,
      ShaderProgram outlineShader) {
    this.spriteLoader = spriteLoader;
    this.metaByName = metaByName;
    this.modifs = modifs;
    this.outlineShader = outlineShader;
  }

  public void setOffsetOverrides(Map<String, int[]> overrides) {
    this.offsetOverrides = overrides == null ? Collections.emptyMap() : overrides;
  }

  public void renderObjectsInternal(
      SpriteBatch batch,
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings,
      float mouseX,
      float mouseY,
      float playerY) {
    renderObjectsConditional(batch, objectPositions, objectMappings, mouseX, mouseY, playerY, true);
  }

  public void renderObjectsBehindPlayer(
      SpriteBatch batch,
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings,
      float mouseX,
      float mouseY,
      float playerY) {
    renderObjectsConditional(
        batch, objectPositions, objectMappings, mouseX, mouseY, playerY, false);
  }

  public void renderObjectsBehindPlayerByBehindFlag(
      SpriteBatch batch,
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings,
      float mouseX,
      float mouseY,
      float playerY,
      boolean alwaysBehindEntities) {
    renderObjectsConditional(
        batch,
        objectPositions,
        objectMappings,
        mouseX,
        mouseY,
        playerY,
        false,
        alwaysBehindEntities);
  }

  public void renderObjectsInternalByBehindFlag(
      SpriteBatch batch,
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings,
      float mouseX,
      float mouseY,
      float playerY,
      boolean alwaysBehindEntities) {
    renderObjectsConditional(
        batch,
        objectPositions,
        objectMappings,
        mouseX,
        mouseY,
        playerY,
        true,
        alwaysBehindEntities);
  }

  private void renderObjectsConditional(
      SpriteBatch batch,
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings,
      float mouseX,
      float mouseY,
      float playerY,
      boolean renderInFront) {
    renderObjectsConditional(
        batch, objectPositions, objectMappings, mouseX, mouseY, playerY, renderInFront, null);
  }

  private void renderObjectsConditional(
      SpriteBatch batch,
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings,
      float mouseX,
      float mouseY,
      float playerY,
      boolean renderInFront,
      Boolean alwaysBehindFilter) {
    float delta = Gdx.graphics.getDeltaTime();
    float playerTileY = playerY / GRID_H;
    if (!renderInFront) {
      hoveredKey = null;
    }
    renderInfos.clear();
    for (ObjectPos pos : objectPositions) {
      String logicalId = logicalIdFor(pos);
      ObjectMapping mapping = objectMappings.get(logicalId);
      if (mapping == null) continue;
      if (alwaysBehindFilter != null
          && isAlwaysBehindObject(logicalId, mapping) != alwaysBehindFilter)
        continue;
      ObjectPos uniqueKey = pos;
      List<TextureRegion> frames =
          framesByType.computeIfAbsent(logicalId, id -> loadFrames(mapping));
      if (frames.isEmpty()) continue;
      AnimState state = animations.computeIfAbsent(uniqueKey, k -> new AnimState());
      if (isAmbientAnimation(mapping, frames)) {
        state.loop = true;
        state.playing = true;
      }
      updateAnim(state, delta, frames.size());
      RenderInfo info = computeRenderInfo(mapping, pos, state, frames);
      info.uniqueKey = uniqueKey;
      info.mapping = mapping;
      info.state = state;
      boolean objectInFront = info.tileY > playerTileY;
      if (objectInFront == renderInFront) {
        renderInfos.add(info);
      }
    }
    renderInfos.sort(RENDER_INFO_SORT);
    for (RenderInfo info : renderInfos) {
      boolean hovered =
          mouseX >= info.px
              && mouseX <= info.px + info.w
              && mouseY >= info.py
              && mouseY <= info.py + info.h;
      if (hovered) hoveredKey = info.uniqueKey;
      if (info.uniqueKey.equals(hoveredKey)
          && outlineShader != null
          && outlineShader.isCompiled()) {
        batch.setShader(outlineShader);
        outlineShader.setUniformf(
            "u_texelSize", 1f / info.region.getRegionWidth(), 1f / info.region.getRegionHeight());
        outlineShader.setUniformf("u_outlineColor", Color.YELLOW);
        draw(batch, info.region, info.px, info.py, info.w, info.h, info.mirror);
        batch.setShader(null);
      } else {
        draw(batch, info.region, info.px, info.py, info.w, info.h, info.mirror);
      }
      renderObjectNameIfVisible(batch, info, info.mapping, info.state);
    }
    recycleRenderInfos(renderInfos);
    renderInfos.clear();
  }

  public void reset() {
    animations.clear();
    framesByType.clear();
    spriteNamesByType.clear();
    metaBySpriteName.clear();
    offsetByName.clear();
    offsetByNameMirror.clear();
    doorGateCache.clear();
    closedDoorsByGateTile.clear();
    blockedTileSet.clear();
    upperNameCache.clear();
    lowerNameCache.clear();
    logicalIdByPos.clear();
    doorIndexBuilt = false;
    hasDoors = false;
    hoveredKey = null;
    renderItemPool.clear();
    renderInfoPool.clear();
    combinedItems.clear();
    renderInfos.clear();
  }

  public boolean handleClick(
      float worldX,
      float worldY,
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings,
      int tileDistance,
      int maxInteractionDistance) {
    lastClickedObject = null;
    if (objectPositions == null || objectPositions.isEmpty()) {
      return false;
    }
    for (ObjectPos pos : objectPositions) {
      String logicalId = logicalIdFor(pos);
      ObjectMapping mapping = objectMappings.get(logicalId);
      if (mapping == null) continue;
      ItemDefinition definition = ItemRegistry.findByKey(pos.name());
      boolean isContainer = definition != null && definition.getStructure() == 3;
      if (!mapping.clickAnimate && !isContainer) continue;
      ObjectPos uniqueKey = pos;
      List<TextureRegion> frames = framesByType.get(logicalId);
      if (frames == null || frames.isEmpty()) {
        frames = loadFrames(mapping);
        framesByType.put(logicalId, frames);
      }
      if (frames.isEmpty()) continue;
      AnimState state = animations.computeIfAbsent(uniqueKey, k -> new AnimState());
      state.animateSound = mapping.animateSound;
      state.reverseAnimateSound = mapping.reverseAnimateSound;
      TextureRegion region = frames.get(state.index);
      String[] spriteNames = spriteNamesByType.get(mapping.sprite);
      if (spriteNames == null) {
        spriteNames = computeSpriteNames(mapping.sprite);
        spriteNamesByType.put(mapping.sprite, spriteNames);
      }
      String spriteName = spriteNames[Math.min(state.index, spriteNames.length - 1)];
      SpriteLoader.Sprite meta =
          metaBySpriteName.computeIfAbsent(spriteName, n -> metaByName.get(lowerNameOf(n)));
      ModifSprites.Offset off = getOffset(spriteName, mapping.mirror);
      float[] drawOffsets = getDrawOffsets(spriteName, meta, mapping.mirror);
      float offX = drawOffsets[0] + off.x;
      float offY = drawOffsets[1] + off.y;
      float w = region.getRegionWidth();
      float h = region.getRegionHeight();
      float px = pos.x() * GRID_W + offX;
      float py = pos.y() * GRID_H + offY;
      if (worldX >= px && worldX <= px + w && worldY >= py && worldY <= py + h) {
        lastClickedObject = pos;
        if (tileDistance <= maxInteractionDistance && mapping.clickAnimate) {
          if (!state.playing) {
            state.playing = true;
            state.reverse = false;
            state.index = 0;
            state.timer = 0f;
            state.reverseDelay = -1f;
            state.lastAnimationUpdateFrame = Gdx.graphics.getFrameId();
            log.info(
                "Started object animation: object={}, logicalId={}, sprite={}, frames={}",
                uniqueKey,
                logicalId,
                mapping.sprite,
                frames.size());
            if (state.animateSound != null && !state.animateSound.trim().isEmpty()) {
              SoundManager.animateSound(state.animateSound);
            }
          } else {
            log.debug("Animation already playing for object: {}", uniqueKey);
          }
        }
        return true;
      }
    }
    return false;
  }

  public ObjectPos getLastClickedObject() {
    return lastClickedObject;
  }

  public void renderObjectsAndDecorsInternal(
      SpriteBatch batch,
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings,
      float mouseX,
      float mouseY,
      com.perso.T4C.render.DecorRenderer decorRenderer,
      int startX,
      int endX,
      int startY,
      int endY,
      float playerY) {
    float delta = Gdx.graphics.getDeltaTime();
    float playerTileY = playerY / GRID_H;
    hoveredKey = null;
    combinedItems.clear();
    for (ObjectPos pos : objectPositions) {
      String logicalId = logicalIdFor(pos);
      ObjectMapping mapping = objectMappings.get(logicalId);
      if (mapping == null || isAlwaysBehindObject(logicalId, mapping)) continue;
      ObjectPos uniqueKey = pos;
      List<TextureRegion> frames =
          framesByType.computeIfAbsent(logicalId, id -> loadFrames(mapping));
      if (frames.isEmpty()) continue;
      AnimState state = animations.computeIfAbsent(uniqueKey, k -> new AnimState());
      updateAnim(state, delta, frames.size());
      RenderInfo info = computeRenderInfo(mapping, pos, state, frames);
      info.uniqueKey = uniqueKey;
      if (info.tileY > playerTileY) {
        RenderItem item = obtainRenderItem();
        item.isObject = true;
        item.objectInfo = info;
        item.y = info.tileY;
        combinedItems.add(item);
      }
    }
    decorRenderer.collectDecorsInFrontOfPlayer(
        combinedItems, renderItemPool, startX, endX, startY, endY, playerY);
    combinedItems.sort(RENDER_ITEM_SORT);
    for (RenderItem item : combinedItems) {
      if (item.isObject) {
        RenderInfo info = item.objectInfo;
        boolean hovered =
            mouseX >= info.px
                && mouseX <= info.px + info.w
                && mouseY >= info.py
                && mouseY <= info.py + info.h;
        if (hovered) hoveredKey = info.uniqueKey;
        if (info.uniqueKey.equals(hoveredKey)
            && outlineShader != null
            && outlineShader.isCompiled()) {
          batch.setShader(outlineShader);
          outlineShader.setUniformf(
              "u_texelSize", 1f / info.region.getRegionWidth(), 1f / info.region.getRegionHeight());
          outlineShader.setUniformf("u_outlineColor", Color.YELLOW);
          draw(batch, info.region, info.px, info.py, info.w, info.h, info.mirror);
          batch.setShader(null);
        } else {
          draw(batch, info.region, info.px, info.py, info.w, info.h, info.mirror);
        }
        renderObjectNameIfVisible(batch, info, info.mapping, info.state);
      } else if (item.renderAction != null) {
        item.renderAction.run();
      } else if (item.decorRegion != null) {
        draw(
            batch,
            item.decorRegion,
            item.decorX,
            item.decorY,
            item.decorW,
            item.decorH,
            item.decorMirror);
      }
    }
    recyclePooledItems(combinedItems);
    recycleRenderInfosFromItems(combinedItems);
    combinedItems.clear();
  }

  public void renderObjectsDecorsAndEntitiesInternal(
      SpriteBatch batch,
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings,
      float mouseX,
      float mouseY,
      com.perso.T4C.render.DecorRenderer decorRenderer,
      int startX,
      int endX,
      int startY,
      int endY,
      boolean includeObjects,
      boolean includeDecors,
      List<RenderItem> entityItems) {
    float delta = Gdx.graphics.getDeltaTime();
    hoveredKey = null;
    combinedItems.clear();
    if (includeObjects && objectPositions != null && !objectPositions.isEmpty()) {
      int cullStartX = startX - OBJECT_CULL_MARGIN_TILES;
      int cullEndX = endX + OBJECT_CULL_MARGIN_TILES;
      int cullStartY = startY - OBJECT_CULL_MARGIN_TILES;
      int cullEndY = endY + OBJECT_CULL_MARGIN_TILES;
      for (ObjectPos pos : objectPositions) {
        if (pos.x() < cullStartX
            || pos.x() > cullEndX
            || pos.y() < cullStartY
            || pos.y() > cullEndY) {
          continue;
        }
        String logicalId = logicalIdFor(pos);
        ObjectMapping mapping = objectMappings.get(logicalId);
        if (mapping == null) continue;
        ObjectPos uniqueKey = pos;
        List<TextureRegion> frames =
            framesByType.computeIfAbsent(logicalId, id -> loadFrames(mapping));
        if (frames.isEmpty()) continue;
        AnimState state = animations.computeIfAbsent(uniqueKey, k -> new AnimState());
        updateAnim(state, delta, frames.size());
        if (isAlwaysBehindObject(logicalId, mapping)) {
          continue;
        }
        RenderInfo info = computeRenderInfo(mapping, pos, state, frames);
        info.uniqueKey = uniqueKey;
        info.mapping = mapping;
        info.state = state;
        RenderItem item = obtainRenderItem();
        item.isObject = true;
        item.objectInfo = info;
        item.occludesEntities = isDoorMapping(logicalId, mapping);
        item.y = info.tileY + (item.occludesEntities ? DOOR_OCCLUSION_DEPTH_OFFSET : 0f);
        combinedItems.add(item);
      }
    }
    if (includeDecors && decorRenderer != null) {
      decorRenderer.collectDecorsInArea(combinedItems, renderItemPool, startX, endX, startY, endY);
    }
    if (entityItems != null && !entityItems.isEmpty()) {
      combinedItems.addAll(entityItems);
    }
    combinedItems.sort(RENDER_ITEM_SORT);
    occlusionRevealItems.clear();
    for (int entityIndex = 0; entityIndex < combinedItems.size(); entityIndex++) {
      RenderItem entity = combinedItems.get(entityIndex);
      if (!entity.revealThroughDecor) {
        continue;
      }
      for (int decorIndex = entityIndex + 1; decorIndex < combinedItems.size(); decorIndex++) {
        RenderItem decor = combinedItems.get(decorIndex);
        if (isEntityOccluder(decor) && overlapsRevealBounds(entity, decor)) {
          entity.revealAfterDecor = decor;
        }
      }
      if (entity.revealAfterDecor != null) {
        occlusionRevealItems.add(entity);
      }
    }
    for (RenderItem item : combinedItems) {
      if (item.isObject) {
        RenderInfo info = item.objectInfo;
        boolean hovered =
            mouseX >= info.px
                && mouseX <= info.px + info.w
                && mouseY >= info.py
                && mouseY <= info.py + info.h;
        if (hovered) hoveredKey = info.uniqueKey;
        if (info.uniqueKey.equals(hoveredKey)
            && outlineShader != null
            && outlineShader.isCompiled()) {
          batch.setShader(outlineShader);
          outlineShader.setUniformf(
              "u_texelSize", 1f / info.region.getRegionWidth(), 1f / info.region.getRegionHeight());
          outlineShader.setUniformf("u_outlineColor", Color.YELLOW);
          draw(batch, info.region, info.px, info.py, info.w, info.h, info.mirror);
          batch.setShader(null);
        } else {
          draw(batch, info.region, info.px, info.py, info.w, info.h, info.mirror);
        }
        renderObjectNameIfVisible(batch, info, info.mapping, info.state);
        revealEntitiesAfter(batch, item);
      } else if (item.renderAction != null) {
        if (!(item.opaqueOcclusionReveal && item.revealAfterDecor != null)) {
          item.renderAction.run();
        }
      } else if (item.decorRegion != null) {
        draw(
            batch,
            item.decorRegion,
            item.decorX,
            item.decorY,
            item.decorW,
            item.decorH,
            item.decorMirror);
        revealEntitiesAfter(batch, item);
      }
    }
    occlusionRevealItems.clear();
    recyclePooledItems(combinedItems);
    recycleRenderInfosFromItems(combinedItems);
    combinedItems.clear();
  }

  public static class RenderItem {
    public boolean isObject;
    public RenderInfo objectInfo;
    public Runnable renderAction;
    public float y;
    public boolean pooled;
    public TextureRegion decorRegion;
    public float decorX;
    public float decorY;
    public float decorW;
    public float decorH;
    public boolean decorMirror;
    public boolean occludesEntities;
    public boolean revealThroughDecor;
    public Runnable occlusionRevealAction;
    public float revealX;
    public float revealY;
    public float revealW;
    public float revealH;
    public RenderItem revealAfterDecor;
    public boolean opaqueOcclusionReveal;
  }

  private static int renderPriority(RenderItem item) {
    return item.renderAction != null ? 0 : 1;
  }

  private RenderItem obtainRenderItem() {
    RenderItem item = renderItemPool.pollFirst();
    if (item == null) {
      item = new RenderItem();
    }
    item.pooled = true;
    item.isObject = false;
    item.objectInfo = null;
    item.renderAction = null;
    item.decorRegion = null;
    item.decorX = 0f;
    item.decorY = 0f;
    item.decorW = 0f;
    item.decorH = 0f;
    item.decorMirror = false;
    item.occludesEntities = false;
    resetOcclusionReveal(item);
    item.y = 0f;
    return item;
  }

  private void recyclePooledItems(List<RenderItem> items) {
    for (RenderItem item : items) {
      if (!item.pooled) {
        continue;
      }
      item.pooled = false;
      item.isObject = false;
      item.objectInfo = null;
      item.renderAction = null;
      item.decorRegion = null;
      item.decorX = 0f;
      item.decorY = 0f;
      item.decorW = 0f;
      item.decorH = 0f;
      item.decorMirror = false;
      item.occludesEntities = false;
      resetOcclusionReveal(item);
      item.y = 0f;
      renderItemPool.addLast(item);
    }
  }

  private static boolean overlapsRevealBounds(RenderItem entity, RenderItem decor) {
    float occluderX = decor.decorRegion != null ? decor.decorX : decor.objectInfo.px;
    float occluderY = decor.decorRegion != null ? decor.decorY : decor.objectInfo.py;
    float occluderW = decor.decorRegion != null ? decor.decorW : decor.objectInfo.w;
    float occluderH = decor.decorRegion != null ? decor.decorH : decor.objectInfo.h;
    if (entity.revealW <= 0f || entity.revealH <= 0f || occluderW <= 0f || occluderH <= 0f) {
      return false;
    }
    return entity.revealX < occluderX + occluderW
        && entity.revealX + entity.revealW > occluderX
        && entity.revealY < occluderY + occluderH
        && entity.revealY + entity.revealH > occluderY;
  }

  private static boolean isEntityOccluder(RenderItem item) {
    return item.renderAction == null && (item.occludesEntities || item.decorRegion != null || item.isObject);
  }

  private void revealEntitiesAfter(SpriteBatch batch, RenderItem occluder) {
    for (RenderItem entity : occlusionRevealItems) {
      if (entity.revealAfterDecor != occluder) {
        continue;
      }
      if (entity.opaqueOcclusionReveal && entity.renderAction != null) {
        entity.renderAction.run();
      } else {
        renderOcclusionReveal(batch, entity);
      }
    }
  }

  private static void renderOcclusionReveal(SpriteBatch batch, RenderItem entity) {
    if (entity.occlusionRevealAction == null) {
      return;
    }
    Color color = batch.getColor();
    float r = color.r;
    float g = color.g;
    float b = color.b;
    float a = color.a;
    batch.setColor(r, g, b, entity.opaqueOcclusionReveal ? a : a * OCCLUDED_ENTITY_ALPHA);
    try {
      entity.occlusionRevealAction.run();
    } finally {
      batch.setColor(r, g, b, a);
    }
  }

  private static void resetOcclusionReveal(RenderItem item) {
    item.revealThroughDecor = false;
    item.occlusionRevealAction = null;
    item.revealX = 0f;
    item.revealY = 0f;
    item.revealW = 0f;
    item.revealH = 0f;
    item.revealAfterDecor = null;
    item.opaqueOcclusionReveal = false;
  }

  private List<TextureRegion> loadFrames(ObjectMapping mapping) {
    List<TextureRegion> frames = new ArrayList<>();
    String pattern = mapping.sprite;
    int frameCount = 1;
    if ((pattern.contains("%d") || pattern.contains("%s")) && pattern.contains("$")) {
      int dollar = pattern.lastIndexOf('$');
      frameCount = Integer.parseInt(pattern.substring(dollar + 1));
      pattern = pattern.substring(0, dollar);
    }
    String[] spriteNames = new String[frameCount];
    for (int i = 1; i <= frameCount; i++) {
      String name =
          pattern.contains("%s")
              ? pattern.replace("%s", String.valueOf((char) ('a' + i - 1)))
              : pattern.replace("%d", String.valueOf(i));
      spriteNames[i - 1] = name;
      TextureRegion region = spriteLoader.getRegionFromSpriteName(name);
      if (region != null) frames.add(region);
    }
    if (frameCount == 1 && pattern.matches(".*-[a-z]$")) {
      String base = pattern.substring(0, pattern.length() - 1);
      List<TextureRegion> discovered = new ArrayList<>();
      for (char suffix = 'a'; suffix <= 'z'; suffix++) {
        TextureRegion region = spriteLoader.getRegionFromSpriteName(base + "-" + suffix);
        if (region == null) break;
        discovered.add(region);
      }
      if (discovered.size() > 1) {
        frames = discovered;
        spriteNames = new String[discovered.size()];
        for (int i = 0; i < discovered.size(); i++) {
          spriteNames[i] = base + "-" + (char) ('a' + i);
        }
      }
    }
    if (frames.isEmpty()) {
      TextureRegion single = spriteLoader.getRegionFromSpriteName(mapping.sprite);
      if (single != null) frames.add(single);
      spriteNames = new String[] {mapping.sprite};
    }
    spriteNamesByType.putIfAbsent(mapping.sprite, spriteNames);
    return frames;
  }

  private RenderInfo computeRenderInfo(
      ObjectMapping mapping, ObjectPos pos, AnimState state, List<TextureRegion> frames) {
    RenderInfo info = obtainRenderInfo();
    TextureRegion region = frames.get(state.index);
    info.region = region;
    String[] spriteNames = spriteNamesByType.get(mapping.sprite);
    if (spriteNames == null) {
      spriteNames = computeSpriteNames(mapping.sprite);
      spriteNamesByType.put(mapping.sprite, spriteNames);
    }
    String spriteName = spriteNames[Math.min(state.index, spriteNames.length - 1)];
    info.spriteName = spriteName;
    boolean mirror = mapping.mirror ^ pos.mirror();
    SpriteLoader.Sprite meta =
        metaBySpriteName.computeIfAbsent(spriteName, n -> metaByName.get(lowerNameOf(n)));
    ModifSprites.Offset off = getOffset(spriteName, mirror);
    float[] drawOffsets = getDrawOffsets(spriteName, meta, mirror);
    float offX = drawOffsets[0] + off.x;
    float offY = drawOffsets[1] + off.y;
    info.w = region.getRegionWidth();
    info.h = region.getRegionHeight();
    info.px = pos.x() * GRID_W + offX;
    info.py = pos.y() * GRID_H + offY;
    info.mirror = mirror;
    info.tileY = pos.y() + mapping.depthTileOffsetY;
    return info;
  }

  private String[] computeSpriteNames(String pattern) {
    if (pattern == null) {
      return new String[] {""};
    }
    if ((pattern.contains("%d") || pattern.contains("%s")) && pattern.contains("$")) {
      int dollar = pattern.lastIndexOf('$');
      int frameCount = Integer.parseInt(pattern.substring(dollar + 1));
      String base = pattern.substring(0, dollar);
      String[] names = new String[frameCount];
      for (int i = 1; i <= frameCount; i++) {
        names[i - 1] =
            base.contains("%s")
                ? base.replace("%s", String.valueOf((char) ('a' + i - 1)))
                : base.replace("%d", String.valueOf(i));
      }
      return names;
    }
    return new String[] {pattern};
  }

  private RenderInfo obtainRenderInfo() {
    RenderInfo info = renderInfoPool.pollFirst();
    if (info == null) {
      info = new RenderInfo();
    }
    info.region = null;
    info.px = 0f;
    info.py = 0f;
    info.w = 0f;
    info.h = 0f;
    info.spriteName = null;
    info.uniqueKey = null;
    info.mirror = false;
    info.tileY = 0L;
    return info;
  }

  private void recycleRenderInfo(RenderInfo info) {
    if (info != null) {
      renderInfoPool.addLast(info);
    }
  }

  private void recycleRenderInfos(List<RenderInfo> infos) {
    for (RenderInfo info : infos) {
      if (info != null) {
        renderInfoPool.addLast(info);
      }
    }
  }

  private void recycleRenderInfosFromItems(List<RenderItem> items) {
    for (RenderItem item : items) {
      if (item.isObject && item.objectInfo != null) {
        renderInfoPool.addLast(item.objectInfo);
        item.objectInfo = null;
      }
    }
  }

  private ModifSprites.Offset getOffset(String spriteName, boolean mirror) {
    if (spriteName == null) {
      return modifs.getOffset(null);
    }
    if (!mirror) {
      return offsetByName.computeIfAbsent(spriteName, modifs::getOffset);
    }
    return offsetByNameMirror.computeIfAbsent(spriteName, name -> modifs.getOffset(name + "M"));
  }

  public boolean handleRightClick(
      float worldX,
      float worldY,
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings) {
    if (objectPositions == null || objectPositions.isEmpty()) {
      return false;
    }
    for (ObjectPos pos : objectPositions) {
      String logicalId = logicalIdFor(pos);
      ObjectMapping mapping = objectMappings.get(logicalId);
      if (mapping == null) continue;
      AnimState state = animations.computeIfAbsent(pos, k -> new AnimState());
      RenderInfo info = computeRenderInfoForHitTest(mapping, pos, state);
      if (info == null) continue;
      if (worldX >= info.px
          && worldX <= info.px + info.w
          && worldY >= info.py
          && worldY <= info.py + info.h) {
        state.nameDisplayUntil = System.currentTimeMillis() + 4000L;
        state.clickedDisplayName =
            (mapping.displayName != null && !mapping.displayName.isBlank())
                ? I18n.resolve(mapping.displayName)
                : I18n.resolve(humanizeLogicalName(logicalId));
        return true;
      }
    }
    return false;
  }

  private RenderInfo computeRenderInfoForHitTest(
      ObjectMapping mapping, ObjectPos pos, AnimState state) {
    String logicalId = logicalIdFor(pos);
    List<TextureRegion> frames = framesByType.get(logicalId);
    if (frames == null || frames.isEmpty()) {
      frames = loadFrames(mapping);
      framesByType.put(logicalId, frames);
    }
    if (frames.isEmpty()) return null;
    return computeRenderInfo(mapping, pos, state, frames);
  }

  private void renderObjectNameIfVisible(
      SpriteBatch batch, RenderInfo info, ObjectMapping mapping, AnimState state) {
    if (mapping == null || state == null || System.currentTimeMillis() >= state.nameDisplayUntil) {
      return;
    }
    String text = state.clickedDisplayName;
    if (text == null || text.isBlank()) {
      text = I18n.resolve(mapping.displayName);
    }
    if (text == null || text.isBlank()) {
      return;
    }
    NameRenderer.renderName(
        batch, text, info.px + info.w * 0.5f - 15f, info.py + 60f, info.w, info.h);
  }

  public void renderNameOverlay(
      SpriteBatch batch,
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings) {
    if (batch == null || objectPositions == null || objectMappings == null) {
      return;
    }
    long now = System.currentTimeMillis();
    for (ObjectPos pos : objectPositions) {
      AnimState state = animations.get(pos);
      if (state == null || now >= state.nameDisplayUntil) {
        continue;
      }
      String logicalId = logicalIdFor(pos);
      ObjectMapping mapping = objectMappings.get(logicalId);
      if (mapping == null) {
        continue;
      }
      RenderInfo info = computeRenderInfoForHitTest(mapping, pos, state);
      if (info == null) {
        continue;
      }
      renderObjectNameIfVisible(batch, info, mapping, state);
      recycleRenderInfo(info);
    }
  }

  private String humanizeLogicalName(String logicalId) {
    if (logicalId == null || logicalId.isBlank()) {
      return "";
    }
    return Arrays.stream(logicalId.toLowerCase(Locale.ROOT).split("[_\\s]+"))
        .filter(part -> !part.isBlank())
        .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
        .collect(Collectors.joining(" "));
  }

  private float[] getDrawOffsets(String spriteName, SpriteLoader.Sprite meta, boolean mirror) {
    int[] override = spriteName != null ? offsetOverrides.get(lowerNameOf(spriteName)) : null;
    if (override != null && override.length >= 4) {
      return mirror
          ? new float[] {override[2], override[3]}
          : new float[] {override[0], override[1]};
    }
    if (meta == null) {
      return new float[] {0f, 0f};
    }
    return mirror
        ? new float[] {meta.getDrawOffset2X(), meta.getDrawOffset2Y()}
        : new float[] {meta.getDrawOffset1X(), meta.getDrawOffset1Y()};
  }

  private String upperNameOf(String name) {
    if (name == null) {
      return null;
    }
    return upperNameCache.computeIfAbsent(name, n -> n.toUpperCase(Locale.ROOT));
  }

  private String lowerNameOf(String name) {
    if (name == null) {
      return null;
    }
    return lowerNameCache.computeIfAbsent(name, n -> n.toLowerCase(Locale.ROOT));
  }

  private String logicalIdFor(ObjectPos pos) {
    if (pos == null) {
      return null;
    }
    String cached = logicalIdByPos.get(pos);
    if (cached != null) {
      return cached;
    }
    String computed = upperNameOf(pos.name());
    logicalIdByPos.put(pos, computed);
    return computed;
  }

  private static void draw(
      SpriteBatch batch,
      TextureRegion region,
      float px,
      float py,
      float w,
      float h,
      boolean mirror) {
    if (mirror) {
      batch.draw(region, px + w, py + h, -w, -h);
    } else {
      batch.draw(region, px, py + h, w, -h);
    }
  }

  public boolean isDoorBlockedAt(
      List<ObjectPos> objectPositions,
      Map<String, ObjectMapping> objectMappings,
      float worldX,
      float worldY) {
    if (objectPositions == null
        || objectPositions.isEmpty()
        || objectMappings == null
        || objectMappings.isEmpty()) {
      return false;
    }
    ensureDoorIndex(objectPositions, objectMappings);
    if (!hasDoors) return false;
    int gridX = (int) Math.floor(worldX / GRID_W);
    int gridY = (int) Math.floor(worldY / GRID_H);
    long key = tileKey(gridX, gridY);
    if (!blockedTileSet.contains(key)) return false;
    List<ObjectPos> doors = closedDoorsByGateTile.get(key);
    if (doors == null || doors.isEmpty()) {
      return false;
    }
    for (ObjectPos pos : doors) {
      String logicalId = logicalIdFor(pos);
      ObjectMapping mapping = objectMappings.get(logicalId);
      if (mapping == null) {
        continue;
      }
      List<TextureRegion> frames = framesByType.get(logicalId);
      if (frames == null) {
        frames = loadFrames(mapping);
        framesByType.put(logicalId, frames);
      }
      ObjectPos uniqueKey = pos;
      AnimState state = animations.computeIfAbsent(uniqueKey, k -> new AnimState());
      boolean closed = true;
      if (frames != null && !frames.isEmpty()) {
        closed = !isDoorOpenOrOpening(state, frames.size());
      }
      if (!closed) {
        continue;
      }
      int doorX = (int) pos.x();
      int doorY = (int) pos.y();
      GridPoint2 gate = getDoorGateTile(uniqueKey, doorX, doorY);
      if (gate != null && isDoorCollisionAt(gate, worldX, worldY)) {
        maybeLogDoorBlock(logicalId, pos, gate.x, gate.x, gate.y, gate.y + 1, gridX, gridY);
        return true;
      }
    }
    return false;
  }

  public List<GridPoint2> getClosedDoorBlockTiles(
      List<ObjectPos> objectPositions, Map<String, ObjectMapping> objectMappings) {
    if (objectPositions == null
        || objectPositions.isEmpty()
        || objectMappings == null
        || objectMappings.isEmpty()) {
      return java.util.Collections.emptyList();
    }
    List<GridPoint2> blocked = new ArrayList<>();
    Set<Long> seen = new HashSet<>();
    ensureDoorIndex(objectPositions, objectMappings);
    for (List<ObjectPos> doors : closedDoorsByGateTile.values()) {
      for (ObjectPos pos : doors) {
        String logicalId = logicalIdFor(pos);
        ObjectMapping mapping = objectMappings.get(logicalId);
        if (mapping == null) {
          continue;
        }
        List<TextureRegion> frames = framesByType.get(logicalId);
        if (frames == null) {
          frames = loadFrames(mapping);
          framesByType.put(logicalId, frames);
        }
        ObjectPos uniqueKey = pos;
        if (frames != null && !frames.isEmpty()) {
          AnimState state = animations.computeIfAbsent(uniqueKey, k -> new AnimState());
          if (isDoorOpenOrOpening(state, frames.size())) {
            continue;
          }
        }
        int doorX = (int) pos.x();
        int doorY = (int) pos.y();
        GridPoint2 gate = getDoorGateTile(uniqueKey, doorX, doorY);
        if (gate != null) {
          for (int y = gate.y; y <= gate.y + 1; y++) {
            long key = tileKey(gate.x, y);
            if (seen.add(key)) {
              blocked.add(new GridPoint2(gate.x, y));
            }
          }
        }
      }
    }
    return blocked;
  }

  private void ensureDoorIndex(
      List<ObjectPos> objectPositions, Map<String, ObjectMapping> objectMappings) {
    if (doorIndexBuilt) {
      return;
    }
    closedDoorsByGateTile.clear();
    for (ObjectPos pos : objectPositions) {
      String logicalId = logicalIdFor(pos);
      ObjectMapping mapping = objectMappings.get(logicalId);
      if (mapping == null || !isDoorMapping(logicalId, mapping)) {
        continue;
      }
      GridPoint2 gate = getDoorGateTile(pos, (int) pos.x(), (int) pos.y());
      if (gate != null) {
        closedDoorsByGateTile
            .computeIfAbsent(tileKey(gate.x, gate.y), key -> new ArrayList<>())
            .add(pos);
        closedDoorsByGateTile
            .computeIfAbsent(tileKey(gate.x, gate.y + 1), key -> new ArrayList<>())
            .add(pos);
      }
    }
    hasDoors = !closedDoorsByGateTile.isEmpty();
    blockedTileSet.clear();
    for (long key : closedDoorsByGateTile.keySet()) {
      blockedTileSet.add(key);
    }
    doorIndexBuilt = true;
  }

  private static long tileKey(int x, int y) {
    return (((long) x) << 32) ^ (y & 0xffffffffL);
  }

  private boolean isAlwaysBehindObject(String logicalId, ObjectMapping mapping) {
    if (mapping != null && mapping.alwaysBehindEntities) {
      return true;
    }
    return DecorFlags.isWalkableBridge(logicalId)
        || (mapping != null && DecorFlags.isWalkableBridge(mapping.sprite));
  }

  private boolean isDoorMapping(String logicalId, ObjectMapping mapping) {
    String name = logicalId == null ? "" : logicalId;
    String sprite = mapping.sprite == null ? "" : upperNameOf(mapping.sprite);
    return name.contains("DOOR") || sprite.contains("DOOR");
  }

  private static boolean isDoorFullyOpen(AnimState state, int frameCount) {
    if (frameCount <= 1) {
      return false;
    }
    return state.index == frameCount - 1 && !state.playing && state.reverseDelay > 0f;
  }

  private static boolean isDoorOpenOrOpening(AnimState state, int frameCount) {
    if (frameCount <= 1) {
      return false;
    }
    return (state.playing && !state.reverse) || isDoorFullyOpen(state, frameCount);
  }

  private GridPoint2 getDoorGateTile(ObjectPos uniqueKey, int doorX, int doorY) {
    GridPoint2 cached = doorGateCache.get(uniqueKey);
    if (cached != null) {
      return cached;
    }
    GridPoint2 gate = findDoorGateTile(doorX, doorY);
    if (gate != null) {
      doorGateCache.put(uniqueKey, gate);
    }
    return gate;
  }

  private GridPoint2 findDoorGateTile(int doorX, int doorY) {
    CollisionManager cm = CollisionManager.getInstance();
    if (!cm.isInitialized()) {
      return new GridPoint2(doorX, doorY);
    }
    GridPoint2 best = null;
    int bestScore = Integer.MIN_VALUE;
    int[][] candidates =
        new int[][] {
          {doorX, doorY},
          {doorX - 1, doorY},
          {doorX + 1, doorY},
          {doorX, doorY - 1},
          {doorX, doorY + 1}
        };
    for (int[] c : candidates) {
      int x = c[0];
      int y = c[1];
      if (cm.hasStaticCollisionAtGrid(x, y)) {
        continue;
      }
      boolean horizontal =
          cm.hasStaticCollisionAtGrid(x - 1, y) && cm.hasStaticCollisionAtGrid(x + 1, y);
      boolean vertical =
          cm.hasStaticCollisionAtGrid(x, y - 1) && cm.hasStaticCollisionAtGrid(x, y + 1);
      boolean diag1 =
          cm.hasStaticCollisionAtGrid(x - 1, y - 1) && cm.hasStaticCollisionAtGrid(x + 1, y + 1);
      boolean diag2 =
          cm.hasStaticCollisionAtGrid(x - 1, y + 1) && cm.hasStaticCollisionAtGrid(x + 1, y - 1);
      boolean diagonal = diag1 || diag2;
      if (!horizontal && !vertical && !diagonal) {
        continue;
      }
      int dist = Math.abs(x - doorX) + Math.abs(y - doorY);
      int score = (diagonal ? 30 : 0) + ((horizontal || vertical) ? 10 : 0) - dist;
      if (score > bestScore) {
        bestScore = score;
        best = new GridPoint2(x, y);
      }
    }
    return best != null ? best : new GridPoint2(doorX, doorY);
  }

  private boolean isDoorCollisionAt(GridPoint2 gate, float worldX, float worldY) {
    int gridX = (int) Math.floor(worldX / GRID_W);
    if (gridX != gate.x) {
      return false;
    }
    float tileTopY = gate.y * GRID_H;
    return worldY >= tileTopY && worldY <= tileTopY + (2f * GRID_H);
  }

  private void maybeLogDoorBlock(
      String logicalId,
      ObjectPos pos,
      int startX,
      int endX,
      int startY,
      int endY,
      int gridX,
      int gridY) {
    long now = System.currentTimeMillis();
    if (now - lastDoorBlockLogAt < DOOR_BLOCK_LOG_COOLDOWN_MS) {
      return;
    }
    lastDoorBlockLogAt = now;
    log.debug(
        "Door block: {} at ({},{}), box=({},{})->({},{}), hit=({},{}).",
        logicalId,
        pos.x(),
        pos.y(),
        startX,
        startY,
        endX,
        endY,
        gridX,
        gridY);
  }

  private static final class LongHashSet {
    private static final long EMPTY = Long.MIN_VALUE;
    private long[] keys;
    private int size;
    private int mask;

    LongHashSet(int capacity) {
      int cap = Integer.highestOneBit(capacity * 2 - 1) << 1;
      keys = new long[cap];
      java.util.Arrays.fill(keys, EMPTY);
      mask = cap - 1;
    }

    boolean contains(long key) {
      int idx = (int) (key ^ (key >>> 32)) & mask;
      while (keys[idx] != EMPTY) {
        if (keys[idx] == key) return true;
        idx = (idx + 1) & mask;
      }
      return false;
    }

    void add(long key) {
      if (size >= keys.length * 3 / 4) resize();
      int idx = (int) (key ^ (key >>> 32)) & mask;
      while (keys[idx] != EMPTY && keys[idx] != key) idx = (idx + 1) & mask;
      if (keys[idx] == EMPTY) {
        keys[idx] = key;
        size++;
      }
    }

    void clear() {
      java.util.Arrays.fill(keys, EMPTY);
      size = 0;
    }

    private void resize() {
      long[] old = keys;
      keys = new long[old.length * 2];
      java.util.Arrays.fill(keys, EMPTY);
      mask = keys.length - 1;
      size = 0;
      for (long k : old) if (k != EMPTY) add(k);
    }
  }

  private void updateAnim(AnimState s, float delta, int frameCount) {
    long frameId = Gdx.graphics.getFrameId();
    if (s.lastAnimationUpdateFrame == frameId) return;
    s.lastAnimationUpdateFrame = frameId;
    if (s.playing) {
      s.timer += delta;
      if (s.timer > 0.12f) {
        s.timer = 0f;
        if (s.index < frameCount - 1) {
          s.index++;
        } else if (s.loop) {
          s.index = 0;
        } else {
          s.playing = false;
          s.reverseDelay = 5f;
        }
      }
    } else if (s.reverseDelay > 0) {
      s.reverseDelay -= delta;
      if (s.reverseDelay <= 0) {
        s.playing = true;
        s.reverse = true;
        if (s.reverseAnimateSound != null && !s.reverseAnimateSound.trim().isEmpty()) {
          SoundManager.reverseAnimateSound(s.reverseAnimateSound);
        }
      }
    }
  }

  private static boolean isAmbientAnimation(ObjectMapping mapping, List<TextureRegion> frames) {
    return mapping != null
        && frames != null
        && frames.size() > 1
        && mapping.sprite != null
        && mapping.sprite.toLowerCase(Locale.ROOT).contains("shop sign-");
  }
}
