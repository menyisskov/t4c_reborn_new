package com.perso.T4C.render;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.helper.SpriteNameParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroundRenderer {
  private static final int CHUNK_SIZE = 32;
  private static final int TMPL_TRANSPARENT_TERRAIN_KEY = 0;
  private final MapReader reader;
  private final SpriteLoader spriteLoader;
  private final SpriteBatch batchSol;
  private final Map<String, SpriteLoader.Sprite> metaByName;
  public static final int COMPOSED_CACHE_MAX = 16384;
  private final LongObjectMap<TileChunk> chunkCache = new LongObjectMap<>(256);
  private final Map<String, ArrayList<Integer>> tileIndicesByCacheKey = new HashMap<>();
  private final Map<String, TextureRegion> composedCache =
      new java.util.LinkedHashMap<>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<String, TextureRegion> eldest) {
          if (size() > COMPOSED_CACHE_MAX) {
            try {
              if (eldest.getValue() != null && eldest.getValue().getTexture() != null) {
                eldest.getValue().getTexture().dispose();
              }
            } catch (Throwable ignored) {
            }
            ArrayList<Integer> tileIndices = tileIndicesByCacheKey.remove(eldest.getKey());
            if (tileIndices != null && tmplRegionByTile != null) {
              for (int tileIdx : tileIndices) {
                if (tileIdx >= 0 && tileIdx < tmplRegionByTile.length) {
                  tmplRegionByTile[tileIdx] = null;
                }
              }
            }
            return true;
          }
          return false;
        }
      };
  private final LongObjectMap<TmplMappingCacheEntry> tmplMappingCache = new LongObjectMap<>(256);
  private final Map<String, int[]> tmplTopColorsCache = new HashMap<>();
  private final Map<String, int[]> tmplSampleColorsCache = new HashMap<>();
  private final Map<String, com.perso.T4C.helper.ResolvedSprite> resolvedSpriteByName =
      new HashMap<>();
  private final Map<String, String> lowerNameCache = new HashMap<>();
  private final Map<String, GridFamily> gridFamiliesByBase = new HashMap<>();
  private final com.perso.T4C.helper.ResolvedSprite[] resolvedSpriteByTile;
  private final TextureRegion[] tmplRegionByTile;
  private final ResolvedTile[] groundTileByTile;
  private final ArrayList<ResolvedTile> groundBatch = new ArrayList<>(4096);
  private final java.util.IdentityHashMap<
          com.badlogic.gdx.graphics.Texture, ArrayList<ResolvedTile>>
      groundByTexture = new java.util.IdentityHashMap<>(256);
  private final ArrayList<ArrayList<ResolvedTile>> groundTextureListPool = new ArrayList<>(256);
  private boolean gridFamiliesBuilt = false;
  private boolean outlineEnabled = false;
  private static final int[][] NEIGHBOR_OFFSETS = {
    {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}
  };
  private static final int[][] TMPL_SAMPLE_COORDS = {
    {16, 1},
    {30, 1},
    {30, 8},
    {30, 14},
    {16, 14},
    {1, 14},
    {1, 8},
    {1, 1}
  };
  private static final int TMPL_SAMPLE_RADIUS = 2;

  public GroundRenderer(
      MapReader reader,
      SpriteLoader spriteLoader,
      SpriteBatch batchSol,
      Map<String, SpriteLoader.Sprite> metaByName) {
    this.reader = reader;
    this.spriteLoader = spriteLoader;
    this.batchSol = batchSol;
    this.metaByName = metaByName;
    this.resolvedSpriteByTile =
        new com.perso.T4C.helper.ResolvedSprite[reader.getWidth() * reader.getHeight()];
    this.tmplRegionByTile = new TextureRegion[reader.getWidth() * reader.getHeight()];
    this.groundTileByTile = new ResolvedTile[reader.getWidth() * reader.getHeight()];
  }

  private long chunkKeyFor(int cx, int cy) {
    return (((long) cx) << 32) | (cy & 0xffffffffL);
  }

  public void invalidateTileCache(int tx, int ty) {
    if (tx < 0 || ty < 0 || tx >= reader.getWidth() || ty >= reader.getHeight()) return;
    int cx = tx / CHUNK_SIZE;
    int cy = ty / CHUNK_SIZE;
    chunkCache.remove(chunkKeyFor(cx, cy));
    int tileIdx = ty * reader.getWidth() + tx;
    resolvedSpriteByTile[tileIdx] = null;
    tmplRegionByTile[tileIdx] = null;
    groundTileByTile[tileIdx] = null;
    invalidateTmplMappingCache(tx, ty);
    log.debug("Invalidated chunk cache for tile ({}, {}) in chunk ({}, {})", tx, ty, cx, cy);
  }

  public int preloadChunks(int startX, int endX, int startY, int endY, int budget) {
    if (budget <= 0) {
      return 0;
    }
    int startChunkX = Math.max(0, startX / CHUNK_SIZE);
    int endChunkX = Math.max(0, endX / CHUNK_SIZE);
    int startChunkY = Math.max(0, startY / CHUNK_SIZE);
    int endChunkY = Math.max(0, endY / CHUNK_SIZE);
    int warmed = 0;
    long deadline = System.nanoTime() + 4_000_000L;
    for (int cy = startChunkY; cy <= endChunkY; cy++) {
      for (int cx = startChunkX; cx <= endChunkX; cx++) {
        long key = chunkKeyFor(cx, cy);
        if (chunkCache.get(key) == null) {
          buildChunk(cx, cy);
          warmed++;
          if (warmed >= budget || System.nanoTime() >= deadline) {
            return warmed;
          }
        }
      }
    }
    return warmed;
  }

  public int preloadTmplTiles(int startX, int endX, int startY, int endY, int budget) {
    if (budget <= 0) {
      return 0;
    }
    int warmed = 0;
    long deadline = System.nanoTime() + 4_000_000L;
    for (int y = startY; y <= endY; y++) {
      for (int x = startX; x <= endX; x++) {
        ResolvedTile tile = getResolvedTileAt(x, y);
        if (tile == null || !tile.tmpl()) {
          continue;
        }
        if (resolveTmplRegion(tile) != null) {
          warmed++;
          if (warmed >= budget || System.nanoTime() >= deadline) {
            return warmed;
          }
        }
      }
    }
    return warmed;
  }

  public void clearChunkCache() {
    chunkCache.clear();
    Arrays.fill(resolvedSpriteByTile, null);
    Arrays.fill(tmplRegionByTile, null);
    Arrays.fill(groundTileByTile, null);
    tmplMappingCache.clear();
    clearComposedCache();
  }

  public boolean isOutlineEnabled() {
    return outlineEnabled;
  }

  public void setOutlineEnabled(boolean enabled) {
    this.outlineEnabled = enabled;
    clearChunkCache();
  }

  public int getComposedCacheSize() {
    return composedCache.size();
  }

  public void disposeOutlineResources() {
    clearComposedCache();
  }

  private void clearComposedCache() {
    for (TextureRegion r : composedCache.values()) {
      try {
        if (r != null && r.getTexture() != null) r.getTexture().dispose();
      } catch (Throwable ignored) {
      }
    }
    composedCache.clear();
    tileIndicesByCacheKey.clear();
    Arrays.fill(tmplRegionByTile, null);
  }

  private void invalidateTmplMappingCache(int tx, int ty) {
    for (int i = 0; i < NEIGHBOR_OFFSETS.length; i++) {
      int nx = tx + NEIGHBOR_OFFSETS[i][0];
      int ny = ty + NEIGHBOR_OFFSETS[i][1];
      tmplMappingCache.remove(chunkKeyFor(nx, ny));
    }
    tmplMappingCache.remove(chunkKeyFor(tx, ty));
  }

  private ResolvedTile getResolvedTileAt(int tx, int ty) {
    if (tx < 0 || ty < 0 || tx >= reader.getWidth() || ty >= reader.getHeight()) return null;
    int cx = tx / CHUNK_SIZE;
    int cy = ty / CHUNK_SIZE;
    TileChunk c = chunkCache.get(chunkKeyFor(cx, cy));
    if (c == null) return null;
    int localX = tx - cx * CHUNK_SIZE;
    int localY = ty - cy * CHUNK_SIZE;
    if (localX < 0 || localX >= CHUNK_SIZE || localY < 0 || localY >= CHUNK_SIZE) return null;
    int idx = localY * CHUNK_SIZE + localX;
    return c.tiles()[idx];
  }

  private TileChunk buildChunk(int cx, int cy) {
    int baseX = cx * CHUNK_SIZE;
    int baseY = cy * CHUNK_SIZE;
    ResolvedTile[] tiles = new ResolvedTile[CHUNK_SIZE * CHUNK_SIZE];
    for (int ly = 0; ly < CHUNK_SIZE; ly++) {
      for (int lx = 0; lx < CHUNK_SIZE; lx++) {
        int tx = baseX + lx;
        int ty = baseY + ly;
        ResolvedTile resolved;
        if (tx < 0 || ty < 0 || tx >= reader.getWidth() || ty >= reader.getHeight()) {
          tiles[ly * CHUNK_SIZE + lx] = null;
          continue;
        }
        String decorName = reader.getDecorSpriteName(tx, ty);
        if (decorName != null && !decorName.isBlank()) {
          tiles[ly * CHUNK_SIZE + lx] = null;
          continue;
        }
        com.perso.T4C.helper.ResolvedSprite resolvedSprite = resolveSpriteAt(tx, ty);
        if (resolvedSprite == null || resolvedSprite.name == null) {
          tiles[ly * CHUNK_SIZE + lx] = null;
          continue;
        }
        String spriteName = resolvedSprite.name;
        boolean mirrorFlag = resolvedSprite.mirror;
        boolean isTmpl = isTmplName(spriteName);
        String key = lowerNameOf(spriteName);
        TextureRegion region = null;
        var meta = metaByName.get(key);
        if (meta == null || !meta.isGround()) {
          tiles[ly * CHUNK_SIZE + lx] = null;
          continue;
        }
        region = TileCache.regionCache.get(key);
        if (region == null) {
          region = spriteLoader.getRegionFromSpriteName(key);
          if (region != null) TileCache.regionCache.put(key, region);
        }
        if (region == null) {
          tiles[ly * CHUNK_SIZE + lx] = null;
          continue;
        }
        resolved = new ResolvedTile(tx, ty, region, mirrorFlag, spriteName, isTmpl);
        tiles[ly * CHUNK_SIZE + lx] = resolved;
      }
    }
    TileChunk chunk = new TileChunk(cx, cy, tiles);
    chunkCache.put(chunkKeyFor(cx, cy), chunk);
    return chunk;
  }

  public void renderGroundInternal(int startX, int endX, int startY, int endY) {
    int startChunkX = Math.max(0, startX / CHUNK_SIZE);
    int endChunkX = Math.max(0, endX / CHUNK_SIZE);
    int startChunkY = Math.max(0, startY / CHUNK_SIZE);
    int endChunkY = Math.max(0, endY / CHUNK_SIZE);
    for (int cy = startChunkY; cy <= endChunkY; cy++) {
      for (int cx = startChunkX; cx <= endChunkX; cx++) {
        TileChunk chunk = chunkCache.get(chunkKeyFor(cx, cy));
        if (chunk == null) chunk = buildChunk(cx, cy);
        for (int tileIndex = 0; tileIndex < chunk.tiles().length; tileIndex++) {
          ResolvedTile tile = chunk.tiles()[tileIndex];
          if (tile == null) {
            int tx = cx * CHUNK_SIZE + (tileIndex % CHUNK_SIZE);
            int ty = cy * CHUNK_SIZE + (tileIndex / CHUNK_SIZE);
            String rawName = reader.getGroundSpriteName(tx, ty);
            if (rawName != null && rawName.matches("[0-9A-Fa-f]{4}")) {
              MissingTileRenderer.draw(batchSol, rawName, tx * GRID_W, ty * GRID_H);
            }
            continue;
          }
          if (tile.region() == null || tile.tmpl()) continue;
          float dx = tile.tileX() * GRID_W;
          float dy = tile.tileY() * GRID_H;
          if (tile.mirrorX()) {
            batchSol.draw(
                tile.region(), dx + GRID_W, dy + GRID_H, 0, 0, GRID_W, GRID_H, -1f, -1f, 0f);
          } else {
            batchSol.draw(tile.region(), dx, dy + GRID_H, 0, 0, GRID_W, GRID_H, 1, -1, 0);
          }
        }
      }
    }
  }

  public void renderGroundBelowDecorInternal(int startX, int endX, int startY, int endY) {
    renderGroundBelowDecorInternal(startX, endX, startY, endY, false);
  }

  public void renderGroundBelowDecorInternal(
      int startX, int endX, int startY, int endY, boolean rawTmpl) {
    for (int y = startY; y <= endY; y++) {
      for (int x = startX; x <= endX; x++) {
        ResolvedTile tile = getResolvedTileAt(x, y);
        if (tile != null) continue;
        var resAt = resolveDecorSpriteAt(x, y);
        if (resAt == null || resAt.name == null) continue;
        ResolvedTile explicitGround = resolveGroundTileAt(x, y, reader.getGroundSpriteName(x, y));
        if (explicitGround != null) {
          if (rawTmpl && explicitGround.tmpl()) {
            drawRawTileRegionAt(explicitGround, x, y);
          } else {
            drawTileRegionAt(explicitGround, x, y);
          }
        }
      }
    }
  }

  private ResolvedTile resolveGroundTileAt(int tx, int ty, String rawName) {
    if (rawName == null || rawName.isBlank()) {
      return null;
    }
    int tileIdx = ty * reader.getWidth() + tx;
    ResolvedTile cached = groundTileByTile[tileIdx];
    if (cached != null) {
      return cached;
    }
    com.perso.T4C.helper.ResolvedSprite resolved = SpriteNameParser.parse(rawName, metaByName);
    if (resolved == null || resolved.name == null) {
      return null;
    }
    String key = lowerNameOf(resolved.name);
    var meta = metaByName.get(key);
    if (meta == null || !meta.isGround()) {
      return null;
    }
    TextureRegion region = TileCache.regionCache.get(key);
    if (region == null) {
      region = spriteLoader.getRegionFromSpriteName(key);
      if (region != null) {
        TileCache.regionCache.put(key, region);
      }
    }
    if (region == null) {
      return null;
    }
    ResolvedTile tile =
        new ResolvedTile(tx, ty, region, resolved.mirror, resolved.name, isTmplName(resolved.name));
    groundTileByTile[tileIdx] = tile;
    return tile;
  }

  private void drawTileRegionAt(ResolvedTile tile, int drawTileX, int drawTileY) {
    TextureRegion region = tile.tmpl() ? resolveTmplRegion(tile) : tile.region();
    if (region == null) {
      region = tile.region();
    }
    if (region == null) {
      return;
    }
    float dx = drawTileX * GRID_W;
    float dy = drawTileY * GRID_H;
    if (tile.mirrorX()) {
      batchSol.draw(region, dx + GRID_W, dy + GRID_H, 0, 0, GRID_W, GRID_H, -1f, -1f, 0f);
    } else {
      batchSol.draw(region, dx, dy + GRID_H, 0, 0, GRID_W, GRID_H, 1, -1, 0);
    }
  }

  private void drawRawTileRegionAt(ResolvedTile tile, int drawTileX, int drawTileY) {
    TextureRegion region = tile.region();
    if (region == null) {
      return;
    }
    float dx = drawTileX * GRID_W;
    float dy = drawTileY * GRID_H;
    if (tile.mirrorX()) {
      batchSol.draw(region, dx + GRID_W, dy + GRID_H, 0, 0, GRID_W, GRID_H, -1f, -1f, 0f);
    } else {
      batchSol.draw(region, dx, dy + GRID_H, 0, 0, GRID_W, GRID_H, 1, -1, 0);
    }
  }

  private int[] extractTopTwoOpaqueColors(String spriteName) {
    if (spriteName == null) return new int[0];
    int[] cached = tmplTopColorsCache.get(spriteName);
    if (cached != null) return cached;
    Pixmap pm = null;
    try {
      pm = spriteLoader.createPixmapForSprite(spriteName);
      if (pm == null) return new int[0];
      Map<Integer, Integer> counts = new java.util.HashMap<>();
      for (int y = 0; y < pm.getHeight(); y++) {
        for (int x = 0; x < pm.getWidth(); x++) {
          int p = pm.getPixel(x, y);
          int alpha = p & 0xFF;
          if (alpha == 0) continue;
          counts.merge(p, 1, Integer::sum);
        }
      }
      if (counts.isEmpty()) return new int[0];
      java.util.List<Map.Entry<Integer, Integer>> list =
          new java.util.ArrayList<>(counts.entrySet());
      list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
      int limit = Math.min(2, list.size());
      int[] res = new int[limit];
      for (int i = 0; i < limit; i++) res[i] = list.get(i).getKey();
      tmplTopColorsCache.put(spriteName, res);
      return res;
    } catch (Throwable t) {
      log.warn("Failed to extract colors from sprite {}: {}", spriteName, t.toString());
      return new int[0];
    } finally {
      if (pm != null) pm.dispose();
    }
  }

  private int[] getTmplSampleColors(String spriteName) {
    if (spriteName == null) return null;
    int[] cached = tmplSampleColorsCache.get(spriteName);
    if (cached != null) return cached;
    Pixmap pm = null;
    try {
      pm = spriteLoader.createPixmapForSprite(spriteName);
      if (pm == null) return null;
      int[] samples = new int[TMPL_SAMPLE_COORDS.length];
      for (int i = 0; i < TMPL_SAMPLE_COORDS.length; i++) {
        int sx = TMPL_SAMPLE_COORDS[i][0];
        int sy = TMPL_SAMPLE_COORDS[i][1];
        sx = Math.max(0, Math.min(sx, pm.getWidth() - 1));
        sy = Math.max(0, Math.min(sy, pm.getHeight() - 1));
        samples[i] = dominantTmplColorAround(pm, sx, sy);
      }
      tmplSampleColorsCache.put(spriteName, samples);
      return samples;
    } catch (Throwable t) {
      log.warn("Failed to read TMPL samples from sprite {}: {}", spriteName, t.toString());
      return null;
    } finally {
      if (pm != null) pm.dispose();
    }
  }

  private int dominantTmplColorAround(Pixmap pixmap, int centerX, int centerY) {
    Map<Integer, Integer> counts = new HashMap<>();
    int minX = Math.max(0, centerX - TMPL_SAMPLE_RADIUS);
    int maxX = Math.min(pixmap.getWidth() - 1, centerX + TMPL_SAMPLE_RADIUS);
    int minY = Math.max(0, centerY - TMPL_SAMPLE_RADIUS);
    int maxY = Math.min(pixmap.getHeight() - 1, centerY + TMPL_SAMPLE_RADIUS);
    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        counts.merge(pixmap.getPixel(x, y), 1, Integer::sum);
      }
    }
    int centerColor = pixmap.getPixel(centerX, centerY);
    int dominantColor = centerColor;
    int dominantCount = counts.getOrDefault(centerColor, 0);
    for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
      if (entry.getValue() > dominantCount) {
        dominantColor = entry.getKey();
        dominantCount = entry.getValue();
      }
    }
    return dominantColor;
  }

  public void renderSmoothingInternal(int startX, int endX, int startY, int endY) {
    renderSmoothingInternal(startX, endX, startY, endY, false);
  }

  public void renderSmoothingInternal(int startX, int endX, int startY, int endY, boolean rawTmpl) {
    int startChunkX = Math.max(0, startX / CHUNK_SIZE);
    int endChunkX = Math.max(0, endX / CHUNK_SIZE);
    int startChunkY = Math.max(0, startY / CHUNK_SIZE);
    int endChunkY = Math.max(0, endY / CHUNK_SIZE);
    for (int cy = startChunkY; cy <= endChunkY; cy++) {
      for (int cx = startChunkX; cx <= endChunkX; cx++) {
        TileChunk chunk = chunkCache.get(chunkKeyFor(cx, cy));
        if (chunk == null) chunk = buildChunk(cx, cy);
        for (ResolvedTile tile : chunk.tiles()) {
          if (tile == null || tile.region() == null) continue;
          if (tile.tmpl()) {
            float dx = tile.tileX() * GRID_W;
            float dy = tile.tileY() * GRID_H;
            TextureRegion composed = rawTmpl ? tile.region() : resolveTmplRegion(tile);
            if (composed != null) {
              if (tile.mirrorX()) {
                batchSol.draw(
                    composed, dx + GRID_W, dy + GRID_H, 0, 0, GRID_W, GRID_H, -1f, -1f, 0f);
              } else {
                batchSol.draw(composed, dx, dy + GRID_H, 0, 0, GRID_W, GRID_H, 1, -1, 0);
              }
            } else {
              if (tile.mirrorX()) {
                batchSol.draw(
                    tile.region(), dx + GRID_W, dy + GRID_H, 0, 0, GRID_W, GRID_H, -1f, -1f, 0f);
              } else {
                batchSol.draw(tile.region(), dx, dy + GRID_H, 0, 0, GRID_W, GRID_H, 1, -1, 0);
              }
            }
          }
        }
      }
    }
  }

  private TextureRegion resolveTmplRegion(ResolvedTile tile) {
    if (tile == null || !tile.tmpl()) {
      return null;
    }
    int tileIdx = tile.tileY() * reader.getWidth() + tile.tileX();
    TextureRegion cached = tmplRegionByTile[tileIdx];
    if (cached != null) return cached;
    String spriteName = tile.spriteName();
    if (spriteName == null) {
      return null;
    }
    TmplMappingCacheEntry mappingEntry =
        getTmplMappingEntry(tile.tileX(), tile.tileY(), spriteName);
    if (mappingEntry == null) {
      return null;
    }
    TextureRegion result = renderTmplTile(spriteName, mappingEntry.cacheKey, mappingEntry.mapping);
    if (result != null) {
      tmplRegionByTile[tileIdx] = result;
      tileIndicesByCacheKey
          .computeIfAbsent(mappingEntry.cacheKey, k -> new ArrayList<>())
          .add(tileIdx);
    }
    return result;
  }

  private TextureRegion renderTmplTile(
      String tmplSpriteName, String cacheKey, Map<Integer, String> colorToTerrain) {
    if (colorToTerrain == null
        || colorToTerrain.isEmpty()
        || cacheKey == null
        || cacheKey.isEmpty()) {
      return null;
    }
    TextureRegion cached = composedCache.get(cacheKey);
    if (cached != null) return cached;
    Pixmap maskPm = null;
    Pixmap outPm = null;
    Map<Integer, Pixmap> terrainPixmaps = new HashMap<>();
    try {
      maskPm = spriteLoader.createPixmapForSprite(tmplSpriteName);
      if (maskPm == null) return null;
      int w = maskPm.getWidth();
      int h = maskPm.getHeight();
      outPm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
      for (Map.Entry<Integer, String> e : colorToTerrain.entrySet()) {
        Pixmap tpm = spriteLoader.createPixmapForSprite(e.getValue());
        if (tpm != null) {
          terrainPixmaps.put(e.getKey(), tpm);
        }
      }
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          int maskPixel = maskPm.getPixel(x, y);
          int alpha = maskPixel & 0xFF;
          if (alpha == 0) {
            Pixmap background = terrainPixmaps.get(TMPL_TRANSPARENT_TERRAIN_KEY);
            outPm.drawPixel(x, y, background != null ? background.getPixel(x, y) : 0x00000000);
            continue;
          }
          Pixmap src = terrainPixmaps.get(maskPixel);
          if (src == null) {
            outPm.drawPixel(x, y, 0x00000000);
            continue;
          }
          int terrainPixel = src.getPixel(x, y);
          outPm.drawPixel(x, y, terrainPixel);
        }
      }
      com.badlogic.gdx.graphics.Texture t = new com.badlogic.gdx.graphics.Texture(outPm);
      t.setFilter(
          com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest,
          com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest);
      t.setWrap(
          com.badlogic.gdx.graphics.Texture.TextureWrap.ClampToEdge,
          com.badlogic.gdx.graphics.Texture.TextureWrap.ClampToEdge);
      TextureRegion region = new TextureRegion(t);
      composedCache.put(cacheKey, region);
      return region;
    } finally {
      if (maskPm != null) maskPm.dispose();
      if (outPm != null) outPm.dispose();
      for (Pixmap p : terrainPixmaps.values()) p.dispose();
    }
  }

  private String buildTmplCacheKey(String tmplSpriteName, Map<Integer, String> colorToTerrain) {
    java.util.List<Map.Entry<Integer, String>> entries =
        new java.util.ArrayList<>(colorToTerrain.entrySet());
    entries.sort((a, b) -> Integer.compare(a.getKey(), b.getKey()));
    StringBuilder sb = new StringBuilder(tmplSpriteName.length() + entries.size() * 16 + 8);
    sb.append(tmplSpriteName).append('|');
    for (Map.Entry<Integer, String> e : entries) {
      sb.append(e.getKey()).append('=').append(e.getValue()).append(';');
    }
    return sb.toString();
  }

  private com.perso.T4C.helper.ResolvedSprite resolveSpriteAt(int x, int y) {
    if (reader.usesSpriteNames()) {
      if (x < 0 || y < 0 || x >= reader.getWidth() || y >= reader.getHeight()) {
        return null;
      }
      int tileIndex = y * reader.getWidth() + x;
      com.perso.T4C.helper.ResolvedSprite tileCached = resolvedSpriteByTile[tileIndex];
      if (tileCached != null) {
        return tileCached;
      }
      String name = reader.getGroundSpriteName(x, y);
      if (name == null || name.isEmpty()) {
        return null;
      }
      com.perso.T4C.helper.ResolvedSprite cached = resolvedSpriteByName.get(name);
      if (cached != null) {
        resolvedSpriteByTile[tileIndex] = cached;
        return cached;
      }
      com.perso.T4C.helper.ResolvedSprite parsed = SpriteNameParser.parse(name, metaByName);
      if (parsed != null) {
        resolvedSpriteByName.put(name, parsed);
        resolvedSpriteByTile[tileIndex] = parsed;
      }
      return parsed;
    }
    return null;
  }

  private com.perso.T4C.helper.ResolvedSprite resolveDecorSpriteAt(int x, int y) {
    if (!reader.usesSpriteNames()) {
      return null;
    }
    if (x < 0 || y < 0 || x >= reader.getWidth() || y >= reader.getHeight()) {
      return null;
    }
    String name = reader.getDecorSpriteName(x, y);
    if (name == null || name.isEmpty()) {
      return null;
    }
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

  private TmplMappingCacheEntry getTmplMappingEntry(int tx, int ty, String spriteName) {
    long key = chunkKeyFor(tx, ty);
    TmplMappingCacheEntry cached = tmplMappingCache.get(key);
    if (cached != null && cached.spriteName.equals(spriteName)) {
      return cached;
    }
    Map<Integer, String> mapping = mapMaskColorsToNeighborTerrains(tx, ty, spriteName);
    String cacheKey = mapping.isEmpty() ? null : buildTmplCacheKey(spriteName, mapping);
    TmplMappingCacheEntry entry = new TmplMappingCacheEntry(spriteName, mapping, cacheKey);
    tmplMappingCache.put(key, entry);
    return entry;
  }

  private Map<Integer, String> mapMaskColorsToNeighborTerrains(int tx, int ty, String spriteName) {
    Map<Integer, Map<String, TerrainScore>> colorToTerrainCounts = new HashMap<>();
    Map<String, TerrainScore> terrainCountsOverall = new HashMap<>();
    int[] maskColors = extractTopTwoOpaqueColors(spriteName);
    if (maskColors.length == 0) return Map.of();
    try {
      int[] sampleColors = getTmplSampleColors(spriteName);
      if (sampleColors == null) return Map.of();
      for (int ni = 0; ni < NEIGHBOR_OFFSETS.length; ni++) {
        TerrainLookup terrain =
            resolveNeighborTerrain(tx, ty, NEIGHBOR_OFFSETS[ni][0], NEIGHBOR_OFFSETS[ni][1]);
        if (terrain == null || terrain.name == null) continue;
        String terrainName = terrain.name;
        String familyName = terrainFamilyName(terrainName);
        addTerrainScore(terrainCountsOverall, familyName, terrainName, terrain.distance2);
        int p = sampleColors[ni];
        if ((p & 0xFF) == 0) {
          p = TMPL_TRANSPARENT_TERRAIN_KEY;
        }
        addTerrainScore(
            colorToTerrainCounts.computeIfAbsent(p, k -> new HashMap<>()),
            familyName,
            terrainName,
            terrain.distance2);
      }
      java.util.List<Integer> colorsList = new java.util.ArrayList<>();
      for (int c : maskColors) colorsList.add(c);
      for (int sampleColor : sampleColors) {
        if ((sampleColor & 0xFF) == 0) {
          colorsList.add(TMPL_TRANSPARENT_TERRAIN_KEY);
          break;
        }
      }
      colorsList.sort(
          (a, b) -> {
            int cmp =
                Integer.compare(
                    colorTerrainBestDistance(colorToTerrainCounts.get(a)),
                    colorTerrainBestDistance(colorToTerrainCounts.get(b)));
            if (cmp != 0) return cmp;
            cmp =
                Integer.compare(
                    colorTerrainConfidence(colorToTerrainCounts.get(b)),
                    colorTerrainConfidence(colorToTerrainCounts.get(a)));
            return cmp != 0 ? cmp : Integer.compare(a, b);
          });
      Map<Integer, String> result = new HashMap<>();
      java.util.Set<String> usedFamilies = new java.util.HashSet<>();
      for (int color : colorsList) {
        Map<String, TerrainScore> terrains = colorToTerrainCounts.get(color);
        if (terrains != null && !terrains.isEmpty()) {
          String pick = pickBestColorTerrain(terrains, usedFamilies);
          if (pick != null) {
            result.put(color, pick);
            usedFamilies.add(terrainFamilyName(pick));
            continue;
          }
        }
        String pick = null;
        if (!terrainCountsOverall.isEmpty()) {
          pick = pickBestTerrain(terrainCountsOverall, usedFamilies);
        } else {
          pick = "<unknown>";
        }
        result.put(color, pick);
        usedFamilies.add(terrainFamilyName(pick));
      }
      if (!result.containsKey(TMPL_TRANSPARENT_TERRAIN_KEY)) {
        String transparentTerrain = resolveTransparentTmplTerrain(tx, ty);
        if (transparentTerrain != null) {
          result.put(TMPL_TRANSPARENT_TERRAIN_KEY, transparentTerrain);
        } else if (!terrainCountsOverall.isEmpty()) {
          String pick = pickBestTerrain(terrainCountsOverall, usedFamilies);
          if (pick != null) {
            result.put(TMPL_TRANSPARENT_TERRAIN_KEY, pick);
          }
        }
      }
      return result;
    } catch (Throwable t) {
      log.warn("Failed mapping mask colors for tmpl {}: {}", spriteName, t.toString());
      return Map.of();
    }
  }

  private String resolveTransparentTmplTerrain(int tx, int ty) {
    return null;
  }

  private void addTerrainScore(
      Map<String, TerrainScore> scores, String familyName, String terrainName, int distance2) {
    scores.computeIfAbsent(familyName, ignored -> new TerrainScore()).add(terrainName, distance2);
  }

  private String pickBestTerrain(Map<String, TerrainScore> scores, Set<String> usedFamilies) {
    if (scores == null || scores.isEmpty()) {
      return null;
    }
    java.util.List<Map.Entry<String, TerrainScore>> list =
        new java.util.ArrayList<>(scores.entrySet());
    list.sort(
        (a, b) -> {
          int cmp = Integer.compare(b.getValue().count, a.getValue().count);
          if (cmp != 0) return cmp;
          cmp = Integer.compare(a.getValue().bestDistance2, b.getValue().bestDistance2);
          if (cmp != 0) return cmp;
          return a.getKey().compareTo(b.getKey());
        });
    for (Map.Entry<String, TerrainScore> entry : list) {
      if (!usedFamilies.contains(entry.getKey())) {
        return entry.getValue().bestTerrainName != null
            ? entry.getValue().bestTerrainName
            : entry.getKey();
      }
    }
    TerrainScore fallback = list.get(0).getValue();
    return fallback.bestTerrainName != null ? fallback.bestTerrainName : list.get(0).getKey();
  }

  private int colorTerrainBestDistance(Map<String, TerrainScore> scores) {
    if (scores == null || scores.isEmpty()) {
      return Integer.MAX_VALUE;
    }
    int bestDistance = Integer.MAX_VALUE;
    for (TerrainScore score : scores.values()) {
      bestDistance = Math.min(bestDistance, score.bestDistance2);
    }
    return bestDistance;
  }

  private String pickBestColorTerrain(Map<String, TerrainScore> scores, Set<String> usedFamilies) {
    if (scores == null || scores.isEmpty()) {
      return null;
    }
    java.util.List<Map.Entry<String, TerrainScore>> list =
        new java.util.ArrayList<>(scores.entrySet());
    list.sort(
        (a, b) -> {
          int cmp = Integer.compare(a.getValue().bestDistance2, b.getValue().bestDistance2);
          if (cmp != 0) return cmp;
          cmp = Integer.compare(b.getValue().count, a.getValue().count);
          if (cmp != 0) return cmp;
          return a.getKey().compareTo(b.getKey());
        });
    for (Map.Entry<String, TerrainScore> entry : list) {
      if (!usedFamilies.contains(entry.getKey())) {
        return entry.getValue().bestTerrainName != null
            ? entry.getValue().bestTerrainName
            : entry.getKey();
      }
    }
    return null;
  }

  private int colorTerrainConfidence(Map<String, TerrainScore> scores) {
    if (scores == null || scores.isEmpty()) {
      return 0;
    }
    int best = 0;
    int second = 0;
    for (TerrainScore score : scores.values()) {
      if (score.count > best) {
        second = best;
        best = score.count;
      } else if (score.count > second) {
        second = score.count;
      }
    }
    return best - second;
  }

  private String terrainFamilyName(String terrainName) {
    if (terrainName == null) {
      return null;
    }
    int cut = terrainName.indexOf(" (");
    if (cut > 0) {
      return terrainName.substring(0, cut);
    }
    int lastSpace = terrainName.lastIndexOf(' ');
    if (lastSpace > 0 && lastSpace < terrainName.length() - 1) {
      boolean numericSuffix = true;
      for (int i = lastSpace + 1; i < terrainName.length(); i++) {
        if (!Character.isDigit(terrainName.charAt(i))) {
          numericSuffix = false;
          break;
        }
      }
      if (numericSuffix) {
        return terrainName.substring(0, lastSpace);
      }
    }
    return terrainName;
  }

  private TerrainLookup resolveNeighborTerrain(int tx, int ty, int dx, int dy) {
    for (int step = 1; step <= 8; step++) {
      int nx = tx + dx * step;
      int ny = ty + dy * step;
      if (nx < 0 || ny < 0 || nx >= reader.getWidth() || ny >= reader.getHeight()) {
        return null;
      }
      com.perso.T4C.helper.ResolvedSprite neighRes = resolveSpriteAt(nx, ny);
      if (neighRes == null || neighRes.name == null) {
        continue;
      }
      if (isTmplName(neighRes.name)) {
        continue;
      }
      var neighMeta = metaByName.get(lowerNameOf(neighRes.name));
      if (neighMeta != null && neighMeta.isGround()) {
        return new TerrainLookup(
            extrapolateGridTerrainName(neighRes.name, tx, ty),
            dx * dx * step * step + dy * dy * step * step);
      }
    }
    return null;
  }

  private String extrapolateGridTerrainName(String terrainName, int targetX, int targetY) {
    GridVariant variant = parseGridVariant(terrainName);
    if (variant == null) {
      return terrainName;
    }
    GridFamily family = gridFamilyFor(variant.base);
    if (family == null) {
      return terrainName;
    }
    GridPhase phase = inferGridPhase(family, targetX, targetY);
    if (phase == null) {
      return terrainName;
    }
    int col = Math.floorMod(targetX + phase.offsetX, family.cols) + 1;
    int row = Math.floorMod(targetY + phase.offsetY, family.rows) + 1;
    String predicted = family.base + " (" + col + ", " + row + ")";
    return metaByName.containsKey(lowerNameOf(predicted)) ? predicted : terrainName;
  }

  private GridPhase inferGridPhase(GridFamily family, int targetX, int targetY) {
    final int radius = 8;
    int[] xScores = new int[family.cols];
    int[] yScores = new int[family.rows];
    for (int y = targetY - radius; y <= targetY + radius; y++) {
      if (y < 0 || y >= reader.getHeight()) {
        continue;
      }
      for (int x = targetX - radius; x <= targetX + radius; x++) {
        if (x < 0 || x >= reader.getWidth()) {
          continue;
        }
        int distance = Math.max(Math.abs(x - targetX), Math.abs(y - targetY));
        int weight = radius + 1 - distance;
        addGridPhaseVote(family, xScores, yScores, x, y, weight);
      }
    }
    int offsetX = bestScoreIndex(xScores);
    int offsetY = bestScoreIndex(yScores);
    if (offsetX < 0 || offsetY < 0) {
      return null;
    }
    return new GridPhase(offsetX, offsetY);
  }

  private void addGridPhaseVote(
      GridFamily family, int[] xScores, int[] yScores, int x, int y, int weight) {
    com.perso.T4C.helper.ResolvedSprite resolved = resolveSpriteAt(x, y);
    if (resolved == null || resolved.name == null) {
      return;
    }
    if (!isTmplName(resolved.name)) {
      addGridPhaseVoteFromName(family, xScores, yScores, x, y, weight, resolved.name);
    }
  }

  private void addGridPhaseVoteFromName(
      GridFamily family,
      int[] xScores,
      int[] yScores,
      int x,
      int y,
      int weight,
      String terrainName) {
    GridVariant variant = parseGridVariant(terrainName);
    if (variant == null || !variant.base.equalsIgnoreCase(family.base)) {
      return;
    }
    xScores[Math.floorMod(variant.col - 1 - x, family.cols)] += weight;
    yScores[Math.floorMod(variant.row - 1 - y, family.rows)] += weight;
  }

  private int bestScoreIndex(int[] scores) {
    int bestIndex = -1;
    int bestScore = 0;
    for (int i = 0; i < scores.length; i++) {
      if (scores[i] > bestScore) {
        bestScore = scores[i];
        bestIndex = i;
      }
    }
    return bestIndex;
  }

  private GridFamily gridFamilyFor(String base) {
    if (base == null || base.isBlank()) {
      return null;
    }
    buildGridFamiliesIfNeeded();
    return gridFamiliesByBase.get(base.toLowerCase(Locale.ROOT));
  }

  private void buildGridFamiliesIfNeeded() {
    if (gridFamiliesBuilt) {
      return;
    }
    Map<String, GridFamilyBuilder> builders = new HashMap<>();
    for (SpriteLoader.Sprite sprite : metaByName.values()) {
      if (sprite == null || sprite.name == null) {
        continue;
      }
      GridVariant variant = parseGridVariant(sprite.name);
      if (variant == null) {
        continue;
      }
      builders
          .computeIfAbsent(
              variant.base.toLowerCase(Locale.ROOT), ignored -> new GridFamilyBuilder(variant.base))
          .add(variant.col, variant.row);
    }
    for (Map.Entry<String, GridFamilyBuilder> entry : builders.entrySet()) {
      GridFamily family = entry.getValue().build();
      if (family != null) {
        gridFamiliesByBase.put(entry.getKey(), family);
      }
    }
    gridFamiliesBuilt = true;
  }

  private GridVariant parseGridVariant(String terrainName) {
    if (terrainName == null || !terrainName.endsWith(")")) {
      return null;
    }
    int open = terrainName.lastIndexOf(" (");
    int comma = terrainName.lastIndexOf(", ");
    if (open <= 0 || comma <= open || comma >= terrainName.length() - 1) {
      return null;
    }
    try {
      String base = terrainName.substring(0, open).trim();
      int col = Integer.parseInt(terrainName.substring(open + 2, comma).trim());
      int row = Integer.parseInt(terrainName.substring(comma + 2, terrainName.length() - 1).trim());
      if (base.isEmpty() || col <= 0 || row <= 0) {
        return null;
      }
      return new GridVariant(base, col, row);
    } catch (NumberFormatException ignored) {
      return null;
    }
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

  private static boolean startsWithIgnoreCase(String value, String prefix) {
    if (value == null || prefix == null) {
      return false;
    }
    if (value.length() < prefix.length()) {
      return false;
    }
    return value.regionMatches(true, 0, prefix, 0, prefix.length());
  }

  private static boolean isTmplName(String value) {
    return startsWithIgnoreCase(value, "tmpl");
  }

  private static final class TmplMappingCacheEntry {
    private final String spriteName;
    private final Map<Integer, String> mapping;
    private final String cacheKey;

    private TmplMappingCacheEntry(
        String spriteName, Map<Integer, String> mapping, String cacheKey) {
      this.spriteName = spriteName;
      this.mapping = mapping;
      this.cacheKey = cacheKey;
    }
  }

  private static final class TerrainScore {
    private int count;
    private int bestDistance2 = Integer.MAX_VALUE;
    private String bestTerrainName;

    private void add(String terrainName, int distance2) {
      count++;
      if (distance2 < bestDistance2
          || (distance2 == bestDistance2
              && bestTerrainName != null
              && terrainName != null
              && terrainName.compareTo(bestTerrainName) < 0)
          || bestTerrainName == null) {
        bestDistance2 = distance2;
        bestTerrainName = terrainName;
      }
    }
  }

  private static final class TerrainLookup {
    private final String name;
    private final int distance2;

    private TerrainLookup(String name, int distance2) {
      this.name = name;
      this.distance2 = distance2;
    }
  }

  private static final class GridFamilyBuilder {
    private final String base;
    private int maxCol;
    private int maxRow;

    private GridFamilyBuilder(String base) {
      this.base = base;
    }

    private void add(int col, int row) {
      maxCol = Math.max(maxCol, col);
      maxRow = Math.max(maxRow, row);
    }

    private GridFamily build() {
      if (maxCol <= 1 || maxRow <= 1) {
        return null;
      }
      return new GridFamily(base, maxCol, maxRow);
    }
  }

  private static final class GridFamily {
    private final String base;
    private final int cols;
    private final int rows;

    private GridFamily(String base, int cols, int rows) {
      this.base = base;
      this.cols = cols;
      this.rows = rows;
    }
  }

  private static final class GridVariant {
    private final String base;
    private final int col;
    private final int row;

    private GridVariant(String base, int col, int row) {
      this.base = base;
      this.col = col;
      this.row = row;
    }
  }

  private static final class GridPhase {
    private final int offsetX;
    private final int offsetY;

    private GridPhase(int offsetX, int offsetY) {
      this.offsetX = offsetX;
      this.offsetY = offsetY;
    }
  }

  private static final class LongObjectMap<V> {
    private static final long EMPTY = Long.MIN_VALUE;
    private long[] keys;
    private Object[] values;
    private int size;
    private int mask;

    LongObjectMap(int capacity) {
      int cap = Integer.highestOneBit(capacity * 2 - 1) << 1;
      keys = new long[cap];
      values = new Object[cap];
      Arrays.fill(keys, EMPTY);
      mask = cap - 1;
    }

    @SuppressWarnings("unchecked")
    V get(long key) {
      int idx = (int) (key ^ (key >>> 32)) & mask;
      while (keys[idx] != EMPTY) {
        if (keys[idx] == key) return (V) values[idx];
        idx = (idx + 1) & mask;
      }
      return null;
    }

    void put(long key, V value) {
      if (size >= keys.length * 3 / 4) resize();
      int idx = (int) (key ^ (key >>> 32)) & mask;
      while (keys[idx] != EMPTY && keys[idx] != key) idx = (idx + 1) & mask;
      if (keys[idx] == EMPTY) size++;
      keys[idx] = key;
      values[idx] = value;
    }

    void remove(long key) {
      int idx = (int) (key ^ (key >>> 32)) & mask;
      while (keys[idx] != EMPTY) {
        if (keys[idx] == key) {
          keys[idx] = EMPTY;
          values[idx] = null;
          size--;
          idx = (idx + 1) & mask;
          while (keys[idx] != EMPTY) {
            long k = keys[idx];
            Object v = values[idx];
            keys[idx] = EMPTY;
            values[idx] = null;
            size--;
            put(k, (V) v);
            idx = (idx + 1) & mask;
          }
          return;
        }
        idx = (idx + 1) & mask;
      }
    }

    void clear() {
      Arrays.fill(keys, EMPTY);
      Arrays.fill(values, null);
      size = 0;
    }

    private void resize() {
      long[] oldKeys = keys;
      Object[] oldVals = values;
      keys = new long[oldKeys.length * 2];
      values = new Object[keys.length];
      Arrays.fill(keys, EMPTY);
      mask = keys.length - 1;
      size = 0;
      for (int i = 0; i < oldKeys.length; i++)
        if (oldKeys[i] != EMPTY) put(oldKeys[i], (V) oldVals[i]);
    }
  }
}
