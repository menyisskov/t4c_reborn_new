package com.perso.T4C.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.SpriteLoader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import com.perso.T4C.render.SpriteOffsetUtil;
import com.perso.T4C.entity.EntityAnimationsBase;

import java.nio.IntBuffer;
import java.util.*;
/**
 * Class representing PlayerAnimations.
 */

@Getter
@Slf4j
public class PlayerAnimations extends EntityAnimationsBase {

    private static final float FRAME_DURATION = 0.05f;

    private final Map<BodyPart, String> partMap = new EnumMap<>(BodyPart.class);
    private final Map<String, Map<String, List<TextureRegion>>> animations = new HashMap<>();
    private final Map<String, Map<String, List<TextureRegion>>> attackAnimations = new HashMap<>();
    private final Map<String, Map<String, List<TextureRegion>>> bowAttackAnimations = new HashMap<>();
    private float animTimer = 0f;
    private boolean attacking = false;
    private boolean holdingMeleeAttackPose = false;
    private boolean bowAttack = false;
    private boolean bowEquipped = false;
    private float attackTimer = 0f;
    private int attackFrame = 0;
    private String attackAngle = "000";
    private final List<DrawPart> drawParts = new ArrayList<>();
    private final Rectangle renderBounds = new Rectangle();
    private final Matrix4 offscreenProjection = new Matrix4();
    private SpriteBatch offscreenBatch;
    private FrameBuffer outlineBuffer;
    private TextureRegion outlineRegion;
    private final IntBuffer viewportBuffer = BufferUtils.newIntBuffer(16);

    /**
     * Construct PlayerAnimations and load all base and attack animations for the
     * provided parts.
     *
     * @param parts alternating BodyPart, String baseName
     * @throws GameException when underlying loaders fail to access resources
     */
    public PlayerAnimations(Object... parts) throws GameException {
        for (int i = 0; i < parts.length; i += 2) {
            partMap.put((BodyPart) parts[i], (String) parts[i + 1]);
        }
        loadAllAnimations();
        loadAttackAnimations();
        loadBowAttackAnimations();
    }

    /**
     * Start a melee attack animation at the given angle.
     */
    public void startAttack(String angle) {
        startAttack(angle, false);
    }

    /**
     * Start an attack animation at the given angle.
     *
     * @param angle The facing angle.
     * @param bow   Whether this is a bow attack (uses the "B" frame variant).
     */
    public void startAttack(String angle, boolean bow) {
        this.attacking = true;
        this.holdingMeleeAttackPose = false;
        this.bowAttack = bow;
        this.attackTimer = 0f;
        this.attackFrame = 0;
        this.attackAngle = angle;
    }

    /** Returns immediately to the idle/walk animation when combat is cancelled. */
    public void clearAttackPose() {
        attacking = false;
        holdingMeleeAttackPose = false;
        attackTimer = 0f;
        attackFrame = 0;
    }

    /** Sets whether a bow is currently equipped (affects weapon draw order). */
    public void setBowEquipped(boolean bowEquipped) {
        this.bowEquipped = bowEquipped;
    }

    /** Prefix used when building attack frame names: "B" for bow, "A" for melee. */
    private String attackPrefix() {
        return bowAttack ? "B" : "A";
    }

    /** Returns the active attack frames for a base/angle, accounting for bow vs melee. */
    private List<TextureRegion> getActiveAttackFrames(String base, String angle) {
        return bowAttack ? getBowAttackFrames(base, angle) : getAttackFrames(base, angle);
    }

    /**
     * Update timers for regular and attack animations.
     *
     * @param delta  time elapsed since last update
     * @param moving whether the player is currently moving
     */
    public void update(float delta, boolean moving) {
        if (attacking) {
            attackTimer += delta;
            if (attackTimer > FRAME_DURATION) {
                attackTimer = 0f;
                attackFrame++;
                // Replaced stream with simple loop to avoid allocations
                int max = 1;
                Map<String, Map<String, List<TextureRegion>>> activeAttack = bowAttack ? bowAttackAnimations : attackAnimations;
                for (Map<String, List<TextureRegion>> angleMap : activeAttack.values()) {
                    for (List<TextureRegion> frames : angleMap.values()) {
                        if (frames.size() > max) {
                            max = frames.size();
                        }
                    }
                }
                if (attackFrame >= max) {
                    attacking = false;
                    if (bowAttack) {
                        holdingMeleeAttackPose = false;
                        attackFrame = 0;
                    } else {
                        holdingMeleeAttackPose = true;
                        attackFrame = Math.max(0, max - 1);
                    }
                }
            }
            return;
        }

        if (moving) {
            holdingMeleeAttackPose = false;
        }
        animTimer = moving ? animTimer + delta : 0f;
    }

    private boolean isAttackPoseVisible() {
        return attacking || holdingMeleeAttackPose;
    }

    /**
     * Render currently active frames for all body parts in the correct drawing
     * order.
     */
    public void render(SpriteBatch batch, Vector2 pos, PlayerMovement movement) {
        render(batch, pos, movement.getCurrentAngle(), movement.isFlipX(), movement.isMoving());
    }

    public void render(SpriteBatch batch, Vector2 pos, PlayerMovement movement, ShaderProgram outlineShader, boolean hovered, float healthPercent) {
        render(batch, pos, movement.getCurrentAngle(), movement.isFlipX(), movement.isMoving(), outlineShader, hovered, healthPercent);
    }

    /**
     * Renders all body parts as one composite. The caller can tint this single
     * draw through the batch color without applying alpha repeatedly where
     * equipment sprites overlap.
     */
    public void renderComposite(SpriteBatch batch, Vector2 pos, PlayerMovement movement) {
        renderComposite(batch, pos, movement.getCurrentAngle(), movement.isFlipX(), movement.isMoving());
    }

    public boolean isMouseOver(float worldX, float worldY, Vector2 pos, PlayerMovement movement) {
        return isMouseOver(worldX, worldY, pos, movement.getCurrentAngle(), movement.isFlipX(), movement.isMoving());
    }

    /**
     * Copies the visual bounds of the current composed player frame.
     * These bounds include every equipped body part and are used by the world
     * renderer to detect decor that actually covers the player on screen.
     */
    public Rectangle getRenderBounds(Vector2 pos, PlayerMovement movement, Rectangle out) {
        return getRenderBounds(pos, movement.getCurrentAngle(), movement.isFlipX(), movement.isMoving(), out);
    }

    public float getDepthY(Vector2 pos, PlayerMovement movement) {
        if (movement == null) {
            return getDepthY(pos, "000", false, false);
        }
        return getDepthY(pos, movement.getCurrentAngle(), movement.isFlipX(), movement.isMoving());
    }

    /**
     * Generic entry points (angle/flipX/moving instead of PlayerMovement) so any
     * composite-sprite entity — not just the player — can reuse this animation
     * engine (body/attack frame resolution, offset alignment, attack sequencing).
     * BaseNPC's composite NPCs (guards, townsfolk) use these directly.
     */
    public void render(SpriteBatch batch, Vector2 pos, String angle, boolean flipX, boolean moving) {
        collectDrawParts(pos, angle, flipX, moving, drawParts, renderBounds);
        for (DrawPart part : drawParts) {
            drawPart(batch, part, 0f, 0f);
        }
    }

    public void render(SpriteBatch batch, Vector2 pos, String angle, boolean flipX, boolean moving, ShaderProgram outlineShader, boolean hovered, float healthPercent) {
        collectDrawParts(pos, angle, flipX, moving, drawParts, renderBounds);
        if (!hovered || outlineShader == null || !outlineShader.isCompiled() || drawParts.isEmpty()) {
            for (DrawPart part : drawParts) {
                drawPart(batch, part, 0f, 0f);
            }
            return;
        }

        float hp = Math.max(0f, Math.min(1f, healthPercent));
        renderOutlinedComposite(batch, outlineShader, 1f - hp, hp, 0f);
    }

    /**
     * Same as the healthPercent-based overload, but with an explicit flat outline
     * color (e.g. yellow hover highlight for NPCs, which have no health bar).
     */
    public void render(SpriteBatch batch, Vector2 pos, String angle, boolean flipX, boolean moving, ShaderProgram outlineShader, boolean hovered, float outlineR, float outlineG, float outlineB) {
        collectDrawParts(pos, angle, flipX, moving, drawParts, renderBounds);
        if (!hovered || outlineShader == null || !outlineShader.isCompiled() || drawParts.isEmpty()) {
            for (DrawPart part : drawParts) {
                drawPart(batch, part, 0f, 0f);
            }
            return;
        }
        renderOutlinedComposite(batch, outlineShader, outlineR, outlineG, outlineB);
    }

    public void renderComposite(SpriteBatch batch, Vector2 pos, String angle, boolean flipX, boolean moving) {
        collectDrawParts(pos, angle, flipX, moving, drawParts, renderBounds);
        if (drawParts.isEmpty()) {
            return;
        }

        int margin = 2;
        int width = Math.max(1, (int) Math.ceil(renderBounds.width) + margin * 2);
        int height = Math.max(1, (int) Math.ceil(renderBounds.height) + margin * 2);
        renderPartsToCompositeBuffer(batch, margin, width, height);
        batch.draw(outlineRegion, renderBounds.x - margin, renderBounds.y - margin + height, width, -height);
    }

    public boolean isMouseOver(float worldX, float worldY, Vector2 pos, String angle, boolean flipX, boolean moving) {
        collectDrawParts(pos, angle, flipX, moving, drawParts, renderBounds);
        return !drawParts.isEmpty() && renderBounds.contains(worldX, worldY);
    }

    public Rectangle getRenderBounds(Vector2 pos, String angle, boolean flipX, boolean moving, Rectangle out) {
        if (out == null) {
            throw new IllegalArgumentException("out must not be null");
        }
        collectDrawParts(pos, angle, flipX, moving, drawParts, renderBounds);
        return out.set(renderBounds);
    }

    public float getDepthY(Vector2 pos, String angle, boolean flipX, boolean moving) {
        refreshIfNeeded();
        if (pos == null) {
            return 0f;
        }
        String bodyBase = partMap.get(BodyPart.BODY);
        if (bodyBase == null) {
            return pos.y;
        }
        boolean attackPoseVisible = isAttackPoseVisible();
        String effAngle = attackPoseVisible ? attackAngle : angle;
        ResolvedFrames bodyResolved = resolveFrames(bodyBase, effAngle);
        List<TextureRegion> bodyFrames = bodyResolved.frames;
        if (bodyFrames.isEmpty()) {
            return pos.y;
        }
        int frameIndex = attackPoseVisible
                ? (bodyResolved.attackSequence ? Math.min(attackFrame, bodyFrames.size() - 1) : 0)
                : (moving ? (int) (animTimer / FRAME_DURATION) % bodyFrames.size() : 0);
        BodyPart[] order = PuppetBodyOrder.getBodyPartsOrder(
                effAngle, flipX, bowEquipped, attackPoseVisible, attackPoseVisible ? attackFrame : frameIndex);
        float maxBottom = pos.y;
        for (BodyPart part : order) {
            String base = partMap.get(part);
            if (base == null || base.isEmpty()) continue;
            ResolvedFrames resolved = resolveFrames(base, effAngle);
            List<TextureRegion> frames = resolved.frames;
            if (frames.isEmpty()) continue;
            int fi = attackPoseVisible
                    ? (resolved.attackSequence ? Math.min(attackFrame, frames.size() - 1) : 0)
                    : Math.min(frameIndex, frames.size() - 1);
            TextureRegion reg = frames.get(fi);
            String name = resolveFrameName(resolved, effAngle, fi);
            Vector2 off = SpriteOffsetUtil.selectOffset(offset1.get(name), offset2.get(name), flipX, ZERO_OFFSET);
            float topLeftY = pos.y + off.y;
            float bottomY = topLeftY + reg.getRegionHeight();
            if (bottomY > maxBottom) {
                maxBottom = bottomY;
            }
        }
        return maxBottom;
    }

    private void collectDrawParts(Vector2 pos, String angleIn, boolean flipX, boolean moving, List<DrawPart> out, Rectangle bounds) {
        refreshIfNeeded();
        out.clear();
        bounds.set(0f, 0f, 0f, 0f);
        String bodyBase = partMap.get(BodyPart.BODY);
        if (bodyBase == null) return;

        boolean attackPoseVisible = isAttackPoseVisible();
        String angle = attackPoseVisible ? attackAngle : angleIn;
        ResolvedFrames bodyResolved = resolveFrames(bodyBase, angle);
        List<TextureRegion> bodyFrames = bodyResolved.frames;
        if (bodyFrames.isEmpty()) return;
        int frameIndex = attackPoseVisible
                ? (bodyResolved.attackSequence ? Math.min(attackFrame, bodyFrames.size() - 1) : 0)
                : (moving ? (int) (animTimer / FRAME_DURATION) % bodyFrames.size() : 0);
        BodyPart[] order = PuppetBodyOrder.getBodyPartsOrder(
                angle, flipX, bowEquipped, attackPoseVisible, attackPoseVisible ? attackFrame : frameIndex);
        boolean boundsInitialized = false;

        boolean logSprites = attackPoseVisible || moving;
        StringBuilder spriteLog = logSprites ? new StringBuilder() : null;

        for (BodyPart part : order) {
            String base = partMap.get(part);
            if (base == null || base.isEmpty()) continue;
            ResolvedFrames resolved = resolveFrames(base, angle);
            List<TextureRegion> frames = resolved.frames;
            if (frames.isEmpty()) continue;
            // Equipment without a dedicated attack sequence must remain on its
            // idle frame. Advancing its normal frames with attackFrame visibly
            // combines walking and attacking (most noticeable with bows).
            int fi = attackPoseVisible
                    ? (resolved.attackSequence ? Math.min(attackFrame, frames.size() - 1) : 0)
                    : Math.min(frameIndex, frames.size() - 1);
            TextureRegion reg = frames.get(fi);
            String name = resolveFrameName(resolved, angle, fi);
            if (spriteLog != null) {
                if (spriteLog.length() > 0) spriteLog.append(", ");
                spriteLog.append(part).append('=').append(name);
            }

            Vector2 off = SpriteOffsetUtil.selectOffset(offset1.get(name), offset2.get(name), flipX, ZERO_OFFSET);

            float topLeftX = pos.x + off.x;
            float topLeftY = pos.y + off.y;

            int w = reg.getRegionWidth();
            int h = reg.getRegionHeight();

            out.add(new DrawPart(reg, topLeftX, topLeftY, w, h, flipX));
            if (!boundsInitialized) {
                bounds.set(topLeftX, topLeftY, w, h);
                boundsInitialized = true;
            } else {
                bounds.merge(topLeftX, topLeftY);
                bounds.merge(topLeftX + w, topLeftY + h);
            }
        }

        if (spriteLog != null) {
            String prefix = attackPoseVisible ? (bowAttack ? "[BOW ATTACK] " : "[ATTACK] ") : "[WALK] ";
            String line = prefix + spriteLog;
            if (!line.equals(lastSpriteLog)) {
                lastSpriteLog = line;
                log.debug(line);
            }
        }
    }

    private String lastSpriteLog = "";

    private void renderOutlinedComposite(SpriteBatch batch, ShaderProgram outlineShader, float outlineR, float outlineG, float outlineB) {
        int margin = 2;
        int width = Math.max(1, (int) Math.ceil(renderBounds.width) + margin * 2);
        int height = Math.max(1, (int) Math.ceil(renderBounds.height) + margin * 2);
        renderPartsToCompositeBuffer(batch, margin, width, height);
        batch.setShader(outlineShader);
        outlineShader.setUniformf("u_texelSize", 1f / width, 1f / height);
        outlineShader.setUniformf("u_outlineColor", outlineR, outlineG, outlineB, 1f);
        batch.draw(outlineRegion, renderBounds.x - margin, renderBounds.y - margin + height, width, -height);
        batch.setShader(null);
    }

    private void renderPartsToCompositeBuffer(SpriteBatch batch, int margin, int width, int height) {
        ensureOutlineBuffer(width, height);

        batch.end();
        viewportBuffer.clear();
        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, viewportBuffer);
        outlineBuffer.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        offscreenProjection.setToOrtho2D(0f, 0f, width, height);
        offscreenBatch.setProjectionMatrix(offscreenProjection);
        offscreenBatch.begin();
        for (DrawPart part : drawParts) {
            drawPart(offscreenBatch, part, -renderBounds.x + margin, -renderBounds.y + margin);
        }
        offscreenBatch.end();
        outlineBuffer.end();
        Gdx.gl.glViewport(viewportBuffer.get(0), viewportBuffer.get(1), viewportBuffer.get(2), viewportBuffer.get(3));
        batch.begin();
    }

    private void ensureOutlineBuffer(int width, int height) {
        if (offscreenBatch == null) {
            offscreenBatch = new SpriteBatch();
        }
        if (outlineBuffer != null && outlineBuffer.getWidth() == width && outlineBuffer.getHeight() == height) {
            return;
        }
        if (outlineBuffer != null) {
            outlineBuffer.dispose();
        }
        outlineBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        Texture texture = outlineBuffer.getColorBufferTexture();
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        outlineRegion = new TextureRegion(texture);
    }

    private static void drawPart(SpriteBatch batch, DrawPart part, float offsetX, float offsetY) {
        float x = part.topLeftX + offsetX;
        float y = part.topLeftY + offsetY;
        if (part.flipX) {
            batch.draw(part.region, x + part.width, y + part.height, -part.width, -part.height);
        } else {
            batch.draw(part.region, x, y + part.height, part.width, -part.height);
        }
    }
/**
 * Class representing DrawPart.
 */

    private static final class DrawPart {
        private final TextureRegion region;
        private final float topLeftX;
        private final float topLeftY;
        private final int width;
        private final int height;
        private final boolean flipX;

        private DrawPart(TextureRegion region, float topLeftX, float topLeftY, int width, int height, boolean flipX) {
            this.region = region;
            this.topLeftX = topLeftX;
            this.topLeftY = topLeftY;
            this.width = width;
            this.height = height;
            this.flipX = flipX;
        }
    }

    /**
     * Reload cached animations if the underlying SpriteLoader cleared its texture
     * caches.
     */
    private void refreshIfNeeded() {
        if (isTextureGenCurrent()) return;
        try {
            animations.clear();
            attackAnimations.clear();
            bowAttackAnimations.clear();
            offset1.clear();
            offset2.clear();
            loadAllAnimations();
            loadAttackAnimations();
            loadBowAttackAnimations();
            markRefreshed();
        } catch (GameException e) {
            log.error("Failed to refresh player animations after texture reload", e);
        }
    }

    /**
     * Force-refresh animations immediately (e.g. when assets were reloaded).
     * Public for callers that receive explicit reload callbacks.
     */
    public void refresh() {
        try {
            animations.clear();
            attackAnimations.clear();
            bowAttackAnimations.clear();
            offset1.clear();
            offset2.clear();
            loadAllAnimations();
            loadAttackAnimations();
            loadBowAttackAnimations();
            markRefreshed();
        } catch (GameException e) {
            log.error("Failed to force-refresh player animations", e);
        }
    }

    public void dispose() {
        if (outlineBuffer != null) {
            outlineBuffer.dispose();
            outlineBuffer = null;
            outlineRegion = null;
        }
        if (offscreenBatch != null) {
            offscreenBatch.dispose();
            offscreenBatch = null;
        }
    }

    /**
     * Load all non-attack animations for each configured body part.
     */
    private void loadAllAnimations() throws GameException {
        SpriteLoader loader = SpriteLoader.getInstance();
        for (String base : partMap.values()) {
            if (base == null || base.isEmpty()) continue;
            Map<String, List<TextureRegion>> byAngle = new HashMap<>();
            loadAnimationsForBase(base, "", byAngle, true, loader);
            animations.put(base, byAngle);
        }
    }

    /**
     * Load all attack animations (prefix 'A') for each configured body part.
     */
    private void loadAttackAnimations() throws GameException {
        SpriteLoader loader = SpriteLoader.getInstance();
        for (String base : partMap.values()) {
            if (base == null || base.isEmpty()) continue;
            Map<String, List<TextureRegion>> byAngle = new HashMap<>();
            loadAnimationsForBase(base, "A", byAngle, false, loader);
            attackAnimations.put(base, byAngle);
        }
    }

    /**
     * Load all bow attack animations (prefix 'B') for each configured body part.
     */
    private void loadBowAttackAnimations() throws GameException {
        SpriteLoader loader = SpriteLoader.getInstance();
        for (String base : partMap.values()) {
            if (base == null || base.isEmpty()) continue;
            Map<String, List<TextureRegion>> byAngle = new HashMap<>();
            loadAnimationsForBase(base, "B", byAngle, false, loader);
            bowAttackAnimations.put(base, byAngle);
        }
    }

    /**
     * Shared helper to load frames for a given base name and optional prefix.
     * Iterates angles and letters 'a'..'z' to collect frames; if allowSingleFrame
     * is true
     * will fallback to a single sprite without a letter suffix when no lettered
     * frames are found.
     */
    private void loadAnimationsForBase(String base, String prefix, Map<String, List<TextureRegion>> target, boolean allowSingleFrame, SpriteLoader loader) throws GameException {
        for (String ang : ANGLES) {
            List<TextureRegion> frames = new ArrayList<>();
            String baseWithAngle = base + prefix + ang;
            for (char c = FRAME_START; c <= 'z'; c++) {
                String name = baseWithAngle + "-" + c;
                TextureRegion r = loader.getRegionFromSpriteName(name);
                if (r == null) continue;
                frames.add(r);
                cacheOffsets(name, loader);
            }
            if (frames.isEmpty() && allowSingleFrame) {
                TextureRegion r = loader.getRegionFromSpriteName(baseWithAngle);
                if (r != null) {
                    frames.add(r);
                    cacheOffsets(baseWithAngle, loader);
                }
            }
            target.put(ang, frames);
        }
    }

    private ResolvedFrames resolveFrames(String base, String angle) {
        if (!isAttackPoseVisible()) {
            return new ResolvedFrames(base, "", getFrames(base, angle), false);
        }
        if (!bowAttack) {
            return new ResolvedFrames(base, "A", getAttackFrames(base, angle), true);
        }

        List<TextureRegion> frames = getBowAttackFrames(base, angle);
        if (!frames.isEmpty()) {
            return new ResolvedFrames(base, "B", frames, true);
        }

        // Bow attacks only use the real B sequence belonging to this exact
        // puppet part. Missing assets must remain visible as missing data rather
        // than silently substituting another puppet or a walking/idle frame.
        return new ResolvedFrames(base, "B", Collections.emptyList(), true);
    }

    private String resolveFrameName(ResolvedFrames resolved, String angle, int frameIndex) {
        int seen = 0;
        for (char c = FRAME_START; c <= 'z'; c++) {
            String name = resolved.base + resolved.prefix + angle + "-" + c;
            if (offset1.containsKey(name) || offset2.containsKey(name)) {
                if (seen == frameIndex) {
                    return name;
                }
                seen++;
            }
        }
        return resolved.base + resolved.prefix + angle + "-" + (char) (FRAME_START + frameIndex);
    }

    private static final class ResolvedFrames {
        private final String base;
        private final String prefix;
        private final List<TextureRegion> frames;
        private final boolean attackSequence;

        private ResolvedFrames(String base, String prefix, List<TextureRegion> frames, boolean attackSequence) {
            this.base = base;
            this.prefix = prefix;
            this.frames = frames == null ? Collections.emptyList() : frames;
            this.attackSequence = attackSequence;
        }
    }

    /**
     * Cache draw offsets for a sprite name (both offset1 and offset2). If sprite
     * meta is not found,
     * default zero vectors are stored.
     */
    /**
     * Return frames for a base at a specific angle, or an empty list if none found.
     */
    private List<TextureRegion> getFrames(String base, String angle) {
        Map<String, List<TextureRegion>> byAngle = animations.get(base);
        if (byAngle == null) return Collections.emptyList();
        return byAngle.getOrDefault(angle, Collections.emptyList());
    }

    /**
     * Return attack frames for a base at a specific angle, or an empty list if none
     * found.
     */
    private List<TextureRegion> getAttackFrames(String base, String angle) {
        Map<String, List<TextureRegion>> byAngle = attackAnimations.get(base);
        if (byAngle == null) return Collections.emptyList();
        return byAngle.getOrDefault(angle, Collections.emptyList());
    }

    /**
     * Return bow attack frames for a base at a specific angle, or an empty list if
     * none found.
     */
    private List<TextureRegion> getBowAttackFrames(String base, String angle) {
        Map<String, List<TextureRegion>> byAngle = bowAttackAnimations.get(base);
        if (byAngle == null) return Collections.emptyList();
        return byAngle.getOrDefault(angle, Collections.emptyList());
    }
}
