package com.perso.T4C.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.BufferUtils;
import com.perso.T4C.exception.GameException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loader and cache for the sprite binary pack.
 * Responsible for reading sprite metadata + PNG payloads and exposing TextureRegion lookups.
 */
@Getter
@Slf4j
public class SpriteLoader {

    private static final SpriteLoader INSTANCE = new SpriteLoader();
    private final Map<String, Integer> nameToId = new HashMap<>();
    private final Map<String, Sprite> metaByLowerName = new HashMap<>();
    private TextureRegion[] regionCacheById = new TextureRegion[0];
    private Texture[] textureCacheById = new Texture[0];
    private final Map<String, TextureRegion> maskedRegionCache = new HashMap<>();
    private final Map<String, Texture> maskedTextureCache = new HashMap<>();
    private final Map<String, TextureRegion> energyBallPaletteRegionCache = new HashMap<>();
    private final Map<String, Texture> energyBallPaletteTextureCache = new HashMap<>();
    private volatile int textureGeneration = 0;
    private static float MAX_SUPPORTED_ANISO = -1f;
    private final List<Sprite> sprites = new ArrayList<>();
    private final List<Runnable> reloadListeners = new ArrayList<>();

    private SpriteLoader() {
    }

    /**
     * Singleton accessor.
     */
    public static SpriteLoader getInstance() {
        return INSTANCE;
    }

    /**
     * Load the sprite binary pack from the provided path.
     *
     * <p>The pack is normally split into shards ({@code sprites_0.bin}, {@code sprites_1.bin}, …)
     * kept under the 100 MB Git/LFS limit. When {@code binPath} names the historical monolithic
     * file and shards sit next to it, the shards win and are concatenated in numeric order — so
     * callers keep passing {@link com.perso.T4C.config.Paths#SPRITE_BIN} unchanged. Sprite ids stay
     * sequential over that concatenation.
     */
    public void loadSpriteBin(String binPath) throws GameException {
        clearAll();

        List<FileHandle> shards = resolveShardHandles(binPath);
        if (shards.isEmpty()) {
            throw new GameException("Sprite binary not found: " + binPath);
        }

        for (FileHandle shard : shards) {
            try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(shard.read()))) {
                SpriteBinIO.readPayload(in, shard.path(), packed -> sprites.add(new Sprite(
                        packed.name(), packed.width(), packed.height(), packed.type(),
                        packed.off1X(), packed.off1Y(), packed.off2X(), packed.off2Y(),
                        packed.png())));
            } catch (IOException e) {
                throw new GameException("Failed to load sprite binary: " + shard.path(), e);
            }
        }

        textureCacheById = new Texture[sprites.size()];
        regionCacheById = new TextureRegion[sprites.size()];

        nameToId.clear();
        metaByLowerName.clear();
        for (int i = 0; i < sprites.size(); i++) {
            String key = sprites.get(i).name.toLowerCase(Locale.ROOT);
            nameToId.putIfAbsent(key, i);
            metaByLowerName.put(key, sprites.get(i));
        }
    }

    /**
     * Resolve the ordered list of files backing {@code binPath}.
     *
     * <p>Shards are probed by contiguous index rather than by listing the directory: {@link
     * com.badlogic.gdx.files.FileHandle#list()} does not work for internal files inside a packaged
     * JAR, whereas {@code exists()} does. Indices are contiguous by construction (see {@link
     * SpriteBinIO#writeSharded}), so stopping at the first gap is complete. Falls back to the
     * monolithic file when no shard is present.
     */
    private static List<FileHandle> resolveShardHandles(String binPath) {
        List<FileHandle> shards = new ArrayList<>();
        int slash = Math.max(binPath.lastIndexOf('/'), binPath.lastIndexOf('\\'));
        String dir = slash >= 0 ? binPath.substring(0, slash + 1) : "";
        String fileName = binPath.substring(slash + 1);
        String baseName = fileName.endsWith(".bin")
                ? fileName.substring(0, fileName.length() - ".bin".length())
                : fileName;

        for (int i = 0; ; i++) {
            FileHandle shard = Gdx.files.internal(dir + SpriteBinIO.shardName(baseName, i));
            if (!shard.exists()) {
                break;
            }
            shards.add(shard);
        }
        if (!shards.isEmpty()) {
            return shards;
        }

        FileHandle legacy = Gdx.files.internal(binPath);
        return legacy.exists() ? List.of(legacy) : List.of();
    }

    /**
     * Return a TextureRegion for a sprite by name, or null if not found.
     */
    public TextureRegion getRegionFromSpriteName(String name) throws GameException {
        if (name == null) return null;
        Integer id = nameToId.get(name.toLowerCase(Locale.ROOT));
        if (id == null) return null;
        return getRegionFromSpriteId(id);
    }

    /** Recreates GoN's missing energy-ball palettes from the packed base frames. */
    public TextureRegion getEnergyBallPaletteRegion(String name, String palette) throws GameException {
        if (name == null || palette == null) return null;
        String normalizedPalette = palette.toLowerCase(Locale.ROOT);
        String key = normalizedPalette + '\0' + name.toLowerCase(Locale.ROOT);
        TextureRegion cached = energyBallPaletteRegionCache.get(key);
        if (cached != null) return cached;
        Pixmap pixmap = createPixmapForSprite(name);
        if (pixmap == null) return null;
        try {
            pixmap.setBlending(Pixmap.Blending.None);
            for (int y = 0; y < pixmap.getHeight(); y++) {
                for (int x = 0; x < pixmap.getWidth(); x++) {
                    int pixel = pixmap.getPixel(x, y);
                    int alpha = pixel & 0xFF;
                    if (alpha == 0) continue;
                    int intensity = Math.max((pixel >>> 24) & 0xFF,
                            Math.max((pixel >>> 16) & 0xFF, (pixel >>> 8) & 0xFF));
                    int core = Math.min(255, Math.max(0, intensity - 190) * 4);
                    int red;
                    int green;
                    int blue;
                    switch (normalizedPalette) {
                        case "blue" -> {
                            red = Math.min(255, intensity / 5 + core);
                            green = Math.min(255, intensity * 3 / 4 + core);
                            blue = intensity;
                        }
                        case "yellow" -> {
                            red = intensity;
                            green = Math.min(255, intensity * 4 / 5 + core);
                            blue = Math.min(255, intensity / 6 + core);
                        }
                        case "black" -> {
                            int shadow = intensity / 5;
                            red = Math.min(255, shadow + core);
                            green = Math.min(255, shadow + core);
                            blue = Math.min(255, shadow + core);
                        }
                        case "purple" -> {
                            red = Math.min(255, intensity * 4 / 5 + core);
                            green = Math.min(255, intensity / 6 + core);
                            blue = Math.min(255, intensity + core);
                        }
                        default -> throw new GameException("Unknown energy-ball palette: " + palette);
                    }
                    pixmap.drawPixel(x, y, (red << 24) | (green << 16) | (blue << 8) | alpha);
                }
            }
            Texture texture = new Texture(pixmap);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            applyAnisotropy(texture);
            TextureRegion region = new TextureRegion(texture);
            energyBallPaletteTextureCache.put(key, texture);
            energyBallPaletteRegionCache.put(key, region);
            return region;
        } finally {
            pixmap.dispose();
        }
    }

    /**
     * Returns a sprite whose alpha channel is modulated by a companion mask.
     *
     * <p>This mirrors the original client's {@code TransAlphaImprovedMask}:
     * mask value 0 keeps the sprite opaque and 255 makes it fully transparent.
     */
    public TextureRegion getMaskedRegionFromSpriteNames(String spriteName, String maskName)
            throws GameException {
        if (spriteName == null || maskName == null) return null;
        String key = spriteName.toLowerCase(Locale.ROOT) + '\0'
                + maskName.toLowerCase(Locale.ROOT);
        TextureRegion cached = maskedRegionCache.get(key);
        if (cached != null) return cached;

        Pixmap sprite = null;
        Pixmap mask = null;
        try {
            sprite = createPixmapForSprite(spriteName);
            mask = createPixmapForSprite(maskName);
            if (sprite == null || mask == null) return null;
            if (mask.getWidth() < sprite.getWidth() || mask.getHeight() < sprite.getHeight()) {
                throw new GameException("Mask is smaller than sprite: "
                        + spriteName + " / " + maskName);
            }
            // Replacing alpha in-place must bypass Pixmap's default SourceOver
            // compositing. Otherwise drawing a translucent pixel over the
            // original opaque pixel produces an opaque result again.
            sprite.setBlending(Pixmap.Blending.None);
            for (int y = 0; y < sprite.getHeight(); y++) {
                for (int x = 0; x < sprite.getWidth(); x++) {
                    int pixel = sprite.getPixel(x, y);
                    int sourceAlpha = pixel & 0xFF;
                    int maskWeight = (mask.getPixel(x, y) >>> 24) & 0xFF;
                    int alpha = sourceAlpha * (255 - maskWeight) / 255;
                    sprite.drawPixel(x, y, (pixel & 0xFFFFFF00) | alpha);
                }
            }
            Texture texture = new Texture(sprite);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            applyAnisotropy(texture);
            TextureRegion region = new TextureRegion(texture);
            maskedTextureCache.put(key, texture);
            maskedRegionCache.put(key, region);
            return region;
        } finally {
            if (sprite != null) sprite.dispose();
            if (mask != null) mask.dispose();
        }
    }

    /**
     * Creates a new Pixmap for the given sprite name.
     * The caller is responsible for disposing the returned Pixmap.
     */
    public Pixmap createPixmapForSprite(String name) throws GameException {
        if (name == null) return null;
        Integer id = nameToId.get(name.toLowerCase(Locale.ROOT));
        if (id == null) return null;
        return createPixmapForSpriteId(id);
    }

    /**
     * Returns the sprite dimensions for a given name.
     * @param name sprite name
     * @return int array [width, height] or null if not found
     */
    public int[] getSpriteDimensions(String name) {
        if (name == null) return null;
        Integer id = nameToId.get(name.toLowerCase(Locale.ROOT));
        if (id == null) return null;
        Sprite s = sprites.get(id);
        return new int[] { s.width, s.height };
    }

    public Sprite getSpriteMeta(String name) {
        if (name == null) return null;
        return metaByLowerName.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Create a Pixmap from a TextureRegion.
     * This is useful for creating smoothed tiles from template masks.
     * The caller is responsible for disposing the returned Pixmap.
     */
    public Pixmap createPixmapForRegion(TextureRegion region) {
        if (region == null) return null;

        Texture texture = region.getTexture();
        if (texture == null) return null;

        if (!texture.getTextureData().isPrepared()) {
            texture.getTextureData().prepare();
        }

        Pixmap fullPixmap = texture.getTextureData().consumePixmap();

        if (region.getRegionX() == 0 && region.getRegionY() == 0 &&
            region.getRegionWidth() == fullPixmap.getWidth() &&
            region.getRegionHeight() == fullPixmap.getHeight()) {
            return fullPixmap;
        }

        Pixmap result = new Pixmap(region.getRegionWidth(), region.getRegionHeight(),
                                   fullPixmap.getFormat());
        for (int y = 0; y < region.getRegionHeight(); y++) {
            for (int x = 0; x < region.getRegionWidth(); x++) {
                int pixel = fullPixmap.getPixel(
                    region.getRegionX() + x,
                    region.getRegionY() + y
                );
                result.drawPixel(x, y, pixel);
            }
        }

        fullPixmap.dispose();
        return result;
    }

    private TextureRegion getRegionFromSpriteId(int id) throws GameException {
        if (id < 0 || id >= sprites.size()) return null;
        TextureRegion r = regionCacheById[id];
        if (r != null) return r;

        Texture t = textureCacheById[id];
        if (t == null) {
            t = buildTextureForSpriteId(id);
            if (t == null) return null;
            textureCacheById[id] = t;
        }
        r = new TextureRegion(t);
        regionCacheById[id] = r;
        return r;
    }

    private Texture buildTextureForSpriteId(int id) throws GameException {
        Pixmap pm = createPixmapForSpriteId(id);
        if (pm == null) return null;
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        applyAnisotropy(t);
        pm.dispose();
        return t;
    }

    private Pixmap createPixmapForSpriteId(int id) throws GameException {
        Sprite s = sprites.get(id);
        if (s.pngData == null || s.pngData.length == 0) return null;
        try {
            return new Pixmap(s.pngData, 0, s.pngData.length);
        } catch (Exception e) {
            throw new GameException("Failed to decode sprite PNG: " + s.name, e);
        }
    }

    private void clearAll() {
        for (Texture t : textureCacheById) if (t != null) t.dispose();
        clearMaskedTextureCaches();
        textureCacheById = new Texture[0];
        regionCacheById = new TextureRegion[0];
        sprites.clear();
        nameToId.clear();
        metaByLowerName.clear();
        textureGeneration++;
        notifyReloadListeners();
    }

    /**
     * Clear only the runtime texture/region caches so textures will be
     * rebuilt on next access. Call this after global texture disposal to
     * ensure SpriteLoader doesn't reuse disposed Texture instances.
     */
    public void clearTextureCaches() {
        try {
            for (Texture t : textureCacheById)
                if (t != null) {
                    try {
                        t.dispose();
                    } catch (Throwable ignored) {
                    }
                }
        } catch (Throwable ignored) {
        }
        clearMaskedTextureCaches();
        textureCacheById = new Texture[sprites.size()];
        regionCacheById = new TextureRegion[sprites.size()];
        textureGeneration++;
        notifyReloadListeners();
    }

    private void clearMaskedTextureCaches() {
        for (Texture texture : maskedTextureCache.values()) {
            if (texture == null) continue;
            try {
                texture.dispose();
            } catch (Throwable ignored) {
            }
        }
        maskedTextureCache.clear();
        maskedRegionCache.clear();
        for (Texture texture : energyBallPaletteTextureCache.values()) {
            if (texture != null) texture.dispose();
        }
        energyBallPaletteTextureCache.clear();
        energyBallPaletteRegionCache.clear();
    }

    /**
     * Register a listener to be executed when runtime texture caches are cleared.
     */
    public void registerReloadListener(Runnable listener) {
        if (listener == null) return;
        synchronized (reloadListeners) {
            reloadListeners.add(listener);
        }
    }

    private void notifyReloadListeners() {
        List<Runnable> copy;
        synchronized (reloadListeners) {
            if (reloadListeners.isEmpty()) return;
            copy = new ArrayList<>(reloadListeners);
        }
        for (Runnable r : copy) {
            try {
                r.run();
            } catch (Throwable ignored) {
            }
        }
    }

    private void applyAnisotropy(Texture t) {
        if (MAX_SUPPORTED_ANISO < 0f) {
            try {
                FloatBuffer buf = BufferUtils.newFloatBuffer(1);
                Gdx.gl.glGetFloatv(0x84FF, buf);
                buf.rewind();
                float value = buf.get(0);
                MAX_SUPPORTED_ANISO = Math.max(1f, value);
            } catch (Throwable ignored) {
                MAX_SUPPORTED_ANISO = 1f;
            }
        }
        float target = Math.min(8f, MAX_SUPPORTED_ANISO);
        if (target > 1f) {
            t.bind();
            Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, 0x84FE, target);
        }
    }
/**
 * Class representing Sprite.
 */

    @Getter
    public static class Sprite {
        public final String name;
        public final int width, height, type;
        public final int drawOffset1X, drawOffset1Y, drawOffset2X, drawOffset2Y;
        public final byte[] pngData;

        public Sprite(String name, int width, int height, int type, int off1X, int off1Y, int off2X, int off2Y, byte[] pngData) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.type = type;
            this.drawOffset1X = off1X;
            this.drawOffset1Y = off1Y;
            this.drawOffset2X = off2X;
            this.drawOffset2Y = off2Y;
            this.pngData = pngData;
        }

        public boolean isGround() {
            return type == 0 || (width == 32 && height == 16);
        }
    }
}
