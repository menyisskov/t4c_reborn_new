package com.perso.T4C.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.SpriteNameParser;

import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

/**
 * Renders sprites explicitly stored in the decor layer above ground tiles.
 * Layer placement is authoritative even when a 32x16 sprite is intrinsically
 * classified as ground by its metadata.
 */
public class DecorRenderer {
    private static final float Z_ORDER_STEP = 10000f;
    private static final java.util.Comparator<DecorRenderItem> DECOR_SORT =
            java.util.Comparator.comparingDouble(item -> item.sortKey);
    private final MapReader reader;
    private final SpriteLoader spriteLoader;
    private final SpriteBatch batchDecor;
    private final Map<String, SpriteLoader.Sprite> metaByName;
    private final Map<String, TextureRegion> regionByName = new HashMap<>();
    private final Set<String> flaggedDecorNames;
    private final Map<String, com.perso.T4C.helper.ResolvedSprite> resolvedSpriteByName = new HashMap<>();
    private final Map<String, String> lowerNameCache = new HashMap<>();
    private final Map<String, DecorCache> decorCacheByName = new HashMap<>();
    private final DecorTileCache[] decorCacheByTile;
    private final java.util.ArrayList<DecorRenderItem> renderItems = new java.util.ArrayList<>(2048);
    private final java.util.ArrayDeque<DecorRenderItem> renderItemPool = new java.util.ArrayDeque<>(2048);
    private boolean useTileOffsets = true;
    private Map<String, int[]> offsetOverrides = java.util.Collections.emptyMap();

    public DecorRenderer(MapReader reader, SpriteLoader spriteLoader, SpriteBatch batchDecor, Map<String, SpriteLoader.Sprite> metaByName, Set<String> flaggedDecorNames) {
        this.reader = reader;
        this.spriteLoader = spriteLoader;
        this.batchDecor = batchDecor;
        this.metaByName = metaByName;
        this.flaggedDecorNames = (flaggedDecorNames == null ? java.util.Collections.emptySet() : flaggedDecorNames);
        this.decorCacheByTile = new DecorTileCache[reader.getWidth() * reader.getHeight()];
    }

    public void setUseTileOffsets(boolean useTileOffsets) {
        this.useTileOffsets = useTileOffsets;
    }

    public void setOffsetOverrides(Map<String, int[]> overrides) {
        this.offsetOverrides = overrides == null ? java.util.Collections.emptyMap() : overrides;
    }

    public void invalidateTileCache(int x, int y) {
        if (x < 0 || y < 0 || x >= reader.getWidth() || y >= reader.getHeight()) {
            return;
        }
        decorCacheByTile[y * reader.getWidth() + x] = null;
    }

    public void clearTileCache() {
        java.util.Arrays.fill(decorCacheByTile, null);
    }

    public int preloadDecorCaches(int startX, int endX, int startY, int endY, int budget) {
        if (budget <= 0) {
            return 0;
        }
        int warmed = 0;
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                int tileIndex = y * reader.getWidth() + x;
                if (decorCacheByTile[tileIndex] != null) {
                    continue;
                }

                // The budget measures actual new tile-cache work. Previously,
                // already-warm decorated tiles consumed it every frame, which
                // prevented the preload scan from ever reaching the moving edge.
                getDecorTileCache(x, y);
                warmed++;
                if (warmed >= budget) {
                    return warmed;
                }
            }
        }
        return warmed;
    }

    /**
     * Internal version without batch management for performance.
     */
    public void renderDecorConditionalInternal(int startX, int endX, int startY, int endY, float playerY, boolean drawIfAbovePlayer) {
        float playerTileY = playerY / GRID_H;
        renderItems.clear();

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                DecorTileCache tile = getDecorTileCache(x, y);
                if (tile == null || tile.resolved == null || tile.resolved.name == null) continue;

                var res = tile.resolved;
                DecorCache cache = tile.decor;
                if (cache == null || cache.meta == null) continue;
                if (cache.flagged) continue;
                var meta = cache.meta;

                TextureRegion region = cache.region;
                if (region == null) continue;

                int[] override = offsetOverrides.get(cache.lowerName);
                float offX = res.mirror ? meta.getDrawOffset2X() : meta.getDrawOffset1X();
                float offY = res.mirror ? meta.getDrawOffset2Y() : meta.getDrawOffset1Y();
                if (override != null && override.length >= 4) {
                    offX = res.mirror ? override[2] : override[0];
                    offY = res.mirror ? override[3] : override[1];
                }
                if (useTileOffsets) {
                    offX += reader.getOffsetXFast(x, y);
                    offY += reader.getOffsetYFast(x, y);
                }

                float scaleX = reader.getScaleXFast(x, y);
                float scaleY = reader.getScaleYFast(x, y);
                float w = region.getRegionWidth() * scaleX;
                float h = region.getRegionHeight() * scaleY;

                float topLeftX = x * GRID_W + offX;
                float topLeftY = y * GRID_H + offY;

                boolean decorInFrontOfPlayer = y > playerTileY;
                if (decorInFrontOfPlayer != drawIfAbovePlayer) continue;

                DecorRenderItem item = obtainDecorRenderItem();
                item.region = region;
                item.x = topLeftX;
                item.y = topLeftY;
                item.w = w;
                item.h = h;
                item.mirror = res.mirror;
                item.sortKey = decorSortKey(cache.lowerName, y, reader.getZOrderFast(x, y));
                renderItems.add(item);
            }
        }

        renderItems.sort(DECOR_SORT);
        for (DecorRenderItem item : renderItems) {
            if (item.mirror) {
                batchDecor.draw(item.region, item.x + item.w, item.y + item.h, -item.w, -item.h);
            } else {
                batchDecor.draw(item.region, item.x, item.y + item.h, item.w, -item.h);
            }
        }
        recycleRenderItems();
    }

    /**
     * Render only the flagged decors in the given region. These are drawn before entities
     * so they will always appear behind players/NPC/monsters.
     */
    public void renderFlaggedDecorsInternal(int startX, int endX, int startY, int endY) {
        if (flaggedDecorNames.isEmpty()) return;
        renderItems.clear();

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                DecorTileCache tile = getDecorTileCache(x, y);
                if (tile == null || tile.resolved == null || tile.resolved.name == null) continue;

                var res = tile.resolved;
                DecorCache cache = tile.decor;
                if (cache == null || cache.meta == null) continue;
                if (!cache.flagged) continue;
                var meta = cache.meta;

                TextureRegion region = cache.region;
                if (region == null) continue;

                int[] override = offsetOverrides.get(cache.lowerName);
                float offX = res.mirror ? meta.getDrawOffset2X() : meta.getDrawOffset1X();
                float offY = res.mirror ? meta.getDrawOffset2Y() : meta.getDrawOffset1Y();
                if (override != null && override.length >= 4) {
                    offX = res.mirror ? override[2] : override[0];
                    offY = res.mirror ? override[3] : override[1];
                }
                if (useTileOffsets) {
                    offX += reader.getOffsetXFast(x, y);
                    offY += reader.getOffsetYFast(x, y);
                }

                float scaleX = reader.getScaleXFast(x, y);
                float scaleY = reader.getScaleYFast(x, y);
                float w = region.getRegionWidth() * scaleX;
                float h = region.getRegionHeight() * scaleY;

                float topLeftX = x * GRID_W + offX;
                float topLeftY = y * GRID_H + offY;

                DecorRenderItem item = obtainDecorRenderItem();
                item.region = region;
                item.x = topLeftX;
                item.y = topLeftY;
                item.w = w;
                item.h = h;
                item.mirror = res.mirror;
                item.sortKey = decorSortKey(cache.lowerName, y, reader.getZOrderFast(x, y));
                renderItems.add(item);
            }
        }

        renderItems.sort(DECOR_SORT);
        for (DecorRenderItem item : renderItems) {
            if (item.mirror) {
                batchDecor.draw(item.region, item.x + item.w, item.y + item.h, -item.w, -item.h);
            } else {
                batchDecor.draw(item.region, item.x, item.y + item.h, item.w, -item.h);
            }
        }
        recycleRenderItems();
    }

    /**
     * Collect decor items that are in front of the player for unified Y-sorting with objects.
     * This allows proper depth ordering between decors and objects.
     */
    public void collectDecorsInFrontOfPlayer(java.util.List<ObjectRenderer.RenderItem> items, Deque<ObjectRenderer.RenderItem> pool, int startX, int endX, int startY, int endY, float playerY) {
        float playerTileY = playerY / GRID_H;

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                DecorTileCache tile = getDecorTileCache(x, y);
                if (tile == null || tile.resolved == null || tile.resolved.name == null) continue;

                var res = tile.resolved;
                DecorCache cache = tile.decor;
                if (cache == null || cache.meta == null) continue;
                if (cache.flagged) continue;
                var meta = cache.meta;

                TextureRegion region = cache.region;
                if (region == null) continue;

                boolean decorInFrontOfPlayer = y > playerTileY;
                if (!decorInFrontOfPlayer) continue;

                int[] override = offsetOverrides.get(cache.lowerName);
                float offX = res.mirror ? meta.getDrawOffset2X() : meta.getDrawOffset1X();
                float offY = res.mirror ? meta.getDrawOffset2Y() : meta.getDrawOffset1Y();
                if (override != null && override.length >= 4) {
                    offX = res.mirror ? override[2] : override[0];
                    offY = res.mirror ? override[3] : override[1];
                }
                if (useTileOffsets) {
                    offX += reader.getOffsetXFast(x, y);
                    offY += reader.getOffsetYFast(x, y);
                }

                float scaleX = reader.getScaleXFast(x, y);
                float scaleY = reader.getScaleYFast(x, y);
                float w = region.getRegionWidth() * scaleX;
                float h = region.getRegionHeight() * scaleY;

                float topLeftX = x * GRID_W + offX;
                float topLeftY = y * GRID_H + offY;

                // Create render item
                ObjectRenderer.RenderItem item = obtainRenderItem(pool);
                item.isObject = false;
                item.y = decorSortKey(cache.lowerName, y, reader.getZOrderFast(x, y));
                item.decorRegion = region;
                item.decorX = topLeftX;
                item.decorY = topLeftY;
                item.decorW = w;
                item.decorH = h;
                item.decorMirror = res.mirror;
                items.add(item);
            }
        }
    }

    /**
     * Collect all non-flagged decors in the given region for unified Y-sorting.
     */
    public void collectDecorsInArea(java.util.List<ObjectRenderer.RenderItem> items, Deque<ObjectRenderer.RenderItem> pool, int startX, int endX, int startY, int endY) {
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                DecorTileCache tile = getDecorTileCache(x, y);
                if (tile == null || tile.resolved == null || tile.resolved.name == null) continue;

                var res = tile.resolved;
                DecorCache cache = tile.decor;
                if (cache == null || cache.meta == null) continue;
                if (cache.flagged) continue;
                var meta = cache.meta;

                TextureRegion region = cache.region;
                if (region == null) continue;

                int[] override = offsetOverrides.get(cache.lowerName);
                float offX = res.mirror ? meta.getDrawOffset2X() : meta.getDrawOffset1X();
                float offY = res.mirror ? meta.getDrawOffset2Y() : meta.getDrawOffset1Y();
                if (override != null && override.length >= 4) {
                    offX = res.mirror ? override[2] : override[0];
                    offY = res.mirror ? override[3] : override[1];
                }
                if (useTileOffsets) {
                    offX += reader.getOffsetXFast(x, y);
                    offY += reader.getOffsetYFast(x, y);
                }

                float scaleX = reader.getScaleXFast(x, y);
                float scaleY = reader.getScaleYFast(x, y);
                float w = region.getRegionWidth() * scaleX;
                float h = region.getRegionHeight() * scaleY;

                float topLeftX = x * GRID_W + offX;
                float topLeftY = y * GRID_H + offY;

                ObjectRenderer.RenderItem item = obtainRenderItem(pool);
                item.isObject = false;
                item.y = decorSortKey(cache.lowerName, y, reader.getZOrderFast(x, y));
                item.decorRegion = region;
                item.decorX = topLeftX;
                item.decorY = topLeftY;
                item.decorW = w;
                item.decorH = h;
                item.decorMirror = res.mirror;
                // Decors reaching this collector are foreground ones: those walked upon are
                // flagged and drawn earlier, so they never fade the entity standing on them.
                item.occludesEntities = true;
                items.add(item);
            }
        }
    }

    private ObjectRenderer.RenderItem obtainRenderItem(Deque<ObjectRenderer.RenderItem> pool) {
        ObjectRenderer.RenderItem item = pool != null ? pool.pollFirst() : null;
        if (item == null) {
            item = new ObjectRenderer.RenderItem();
        }
        item.pooled = pool != null;
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
        item.revealThroughDecor = false;
        item.occlusionRevealAction = null;
        item.revealX = 0f;
        item.revealY = 0f;
        item.revealW = 0f;
        item.revealH = 0f;
        item.revealAfterDecor = null;
        return item;
    }

    private float decorSortKey(String lowerName, int tileY, int zOrder) {
        return tileY + (zOrder * Z_ORDER_STEP);
    }

    private TextureRegion getRegion(String name) {
        String key = lowerNameOf(name);
        if (key == null) {
            return null;
        }
        return regionByName.computeIfAbsent(key, spriteLoader::getRegionFromSpriteName);
    }

    private com.perso.T4C.helper.ResolvedSprite resolveSpriteAt(int x, int y) {
        if (!reader.usesSpriteNames()) {
            return null;
        }
        if (x < 0 || y < 0 || x >= reader.getWidth() || y >= reader.getHeight()) {
            return null;
        }
        String name = reader.getDecorSpriteName(x, y);
        if (name == null || name.isEmpty()) return null;
        com.perso.T4C.helper.ResolvedSprite cached = resolvedSpriteByName.get(name);
        if (cached != null) {
            return cached;
        }
        com.perso.T4C.helper.ResolvedSprite parsed = SpriteNameParser.parse(name, metaByName);
        if (parsed != null) {
            resolvedSpriteByName.put(name, parsed);
        }
        return parsed;
    }

    private String lowerNameOf(String name) {
        if (name == null) {
            return null;
        }
        String cached = lowerNameCache.get(name);
        if (cached != null) return cached;
        String lower = name.toLowerCase(Locale.ROOT);
        lowerNameCache.put(name, lower);
        return lower;
    }

    private DecorCache getDecorCache(String spriteName) {
        if (spriteName == null) {
            return null;
        }
        return decorCacheByName.computeIfAbsent(spriteName, name -> {
            DecorCache cache = new DecorCache();
            cache.lowerName = lowerNameOf(name);
            if (cache.lowerName == null) {
                return cache;
            }
            cache.flagged = flaggedDecorNames.contains(cache.lowerName);
            cache.meta = metaByName.get(cache.lowerName);
            if (cache.meta != null) {
                cache.region = getRegion(cache.meta.getName());
            }
            return cache;
        });
    }

    private DecorTileCache getDecorTileCache(int x, int y) {
        if (!reader.usesSpriteNames() || x < 0 || y < 0 || x >= reader.getWidth() || y >= reader.getHeight()) {
            return null;
        }
        int tileIndex = y * reader.getWidth() + x;
        DecorTileCache cached = decorCacheByTile[tileIndex];
        if (cached != null) {
            return cached;
        }
        com.perso.T4C.helper.ResolvedSprite resolved = resolveSpriteAt(x, y);
        DecorCache decor = resolved != null && resolved.name != null ? getDecorCache(resolved.name) : null;
        DecorTileCache tile = new DecorTileCache(resolved, decor);
        decorCacheByTile[tileIndex] = tile;
        return tile;
    }
/**
 * Class representing DecorRenderItem.
 */

    private DecorRenderItem obtainDecorRenderItem() {
        DecorRenderItem item = renderItemPool.pollFirst();
        return item != null ? item : new DecorRenderItem();
    }

    private void recycleRenderItems() {
        for (DecorRenderItem item : renderItems) {
            item.region = null;
            renderItemPool.addLast(item);
        }
        renderItems.clear();
    }

    private static final class DecorRenderItem {
        private TextureRegion region;
        private float x;
        private float y;
        private float w;
        private float h;
        private boolean mirror;
        private float sortKey;
    }
/**
 * Class representing DecorCache.
 */

    private static final class DecorCache {
        private String lowerName;
        private SpriteLoader.Sprite meta;
        private TextureRegion region;
        private boolean flagged; // cached result of flaggedDecorNames.contains(lowerName)
    }

    private static final class DecorTileCache {
        private final com.perso.T4C.helper.ResolvedSprite resolved;
        private final DecorCache decor;

        private DecorTileCache(com.perso.T4C.helper.ResolvedSprite resolved, DecorCache decor) {
            this.resolved = resolved;
            this.decor = decor;
        }
    }
}
