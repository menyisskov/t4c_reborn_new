package com.perso.T4C.monster;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.render.SpriteOffsetUtil;
import com.perso.T4C.entity.EntityAnimationsBase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles animations for monsters (walk, attack, death).
 */
@Slf4j
@Getter
public class MonsterAnimations extends EntityAnimationsBase {

    private static final float FRAME_DURATION = 0.08f;
    private static final float MIN_ATTACK_DURATION = 0.5f;
    private static final float DEATH_DURATION = 1.0f;

    private final String walkPattern;
    private final String attackPattern;
    private final String deathPattern;
    private final String soundAttack;
    private final String soundDeath;
    private final PatternParts walkParts;
    private final PatternParts attackParts;
    private final PatternParts deathParts;

    private final Map<String, List<TextureRegion>> walkAnimations = new HashMap<>();
    private final Map<String, List<TextureRegion>> attackAnimations = new HashMap<>();
    private final Map<String, List<TextureRegion>> deathAnimations = new HashMap<>();
    private float animTimer = 0f;
    private float attackTimer = 0f;
    private float deathTimer = 0f;
    private boolean isAttacking = false;
    private boolean holdingAttackPose = false;
    private boolean isDying = false;
    private boolean isDeadComplete = false;
    /** Full length of the loaded attack pattern (max frame count across angles) x FRAME_DURATION. */
    private float attackDuration = MIN_ATTACK_DURATION;

    /**
     * Construct MonsterAnimations with explicit animation patterns.
     */
    public MonsterAnimations(String walkPattern, String attackPattern, String deathPattern, String soundAttack, String soundDeath) throws GameException {
        this.walkPattern = walkPattern;
        this.attackPattern = attackPattern;
        this.deathPattern = deathPattern;
        this.soundAttack = soundAttack;
        this.soundDeath = soundDeath;
        this.walkParts = PatternParts.from(walkPattern);
        this.attackParts = PatternParts.from(attackPattern);
        this.deathParts = PatternParts.from(deathPattern);
        loadAllAnimations();
    }

    /**
     * Update animation timer.
     */
    public boolean update(float delta, boolean moving) {
        if (isDying) {
            deathTimer += delta;
            if (deathTimer >= DEATH_DURATION) {
                isDeadComplete = true;
            }
            return false;
        }

        if (isAttacking) {
            attackTimer += delta;
            if (attackTimer >= attackDuration) {
                isAttacking = false;
                holdingAttackPose = true;
                attackTimer = attackDuration;
            }
            return false;
        }

        if (moving) {
            holdingAttackPose = false;
        }
        if (!moving) {
            return false;
        }
        int previousFrame = getCurrentWalkFrameIndex();
        animTimer += delta;
        return previousFrame > getCurrentWalkFrameIndex();
    }

    /**
     * Start attack animation.
     */
    public void startAttack() {
        if (isDying || isAttacking) return;
        isAttacking = true;
        holdingAttackPose = false;
        attackTimer = 0f;
        if (soundAttack != null && !soundAttack.isEmpty()) {
            SoundManager.animateSound(soundAttack);
        }
    }

    /** Clears both an active swing and its held final pose. */
    public void clearAttackPose() {
        isAttacking = false;
        holdingAttackPose = false;
        attackTimer = 0f;
    }

    public boolean hasAttackAnimation() {
        return attackPattern != null && !attackAnimations.isEmpty();
    }

    /**
     * Start death animation.
     */
    public void startDeath() {
        if (isDying) return;
        isDying = true;
        deathTimer = 0f;
        isAttacking = false;
        holdingAttackPose = false;
        if (soundDeath != null && !soundDeath.isEmpty()) {
            SoundManager.animateSound(soundDeath);
        }
    }

    /**
     * Reset animation state (for respawn).
     */
    public void reset() {
        isDying = false;
        isDeadComplete = false;
        deathTimer = 0f;
        isAttacking = false;
        holdingAttackPose = false;
        attackTimer = 0f;
        animTimer = 0f;
    }

    /**
     * Render monster with current animation state.
     * @param healthPercent Health percentage (0.0 to 1.0) to determine outline color
     */
    public void render(SpriteBatch batch, Vector2 pos, String angle, boolean flipX, boolean moving, boolean isHovered, ShaderProgram outlineShader, float healthPercent) {
        // Delegate to the new signature with no name
        render(batch, pos, angle, flipX, moving, isHovered, outlineShader, healthPercent, null, false);
    }

    /**
     * Extended render that can draw an entity name above the sprite when requested.
     */
    public void render(SpriteBatch batch, Vector2 pos, String angle, boolean flipX, boolean moving, boolean isHovered, ShaderProgram outlineShader, float healthPercent, String entityName, boolean showName) {
        refreshIfNeeded();

        List<TextureRegion> frames;
        int frameIndex;
        String patternBase;
        boolean appendAngle;
        PatternParts activeParts;

        if (isDying && deathPattern != null) {
            frames = getDeathFrames("000");
            frameIndex = (int) (deathTimer / FRAME_DURATION);
            if (frameIndex >= frames.size()) {
                frameIndex = frames.size() - 1; // Stay on last death frame
            }
            patternBase = deathParts.baseName;
            appendAngle = false;
            activeParts = deathParts;
        } else if ((isAttacking || holdingAttackPose) && attackPattern != null) {
            frames = getAttackFrames(angle);
            frameIndex = holdingAttackPose
                    ? Math.max(0, frames.size() - 1)
                    : Math.min((int) (attackTimer / FRAME_DURATION), Math.max(0, frames.size() - 1));
            patternBase = attackParts.baseName;
            appendAngle = !attackParts.angleless;
            activeParts = attackParts;
        } else {
            frames = getWalkFrames(angle);
            frameIndex = moving ? (int) (animTimer / FRAME_DURATION) % Math.max(1, frames.size()) : 0;
            patternBase = walkParts.baseName;
            appendAngle = !walkParts.angleless;
            activeParts = walkParts;
        }

        if (frames.isEmpty()) return;

        frameIndex = Math.min(frameIndex, frames.size() - 1);
        TextureRegion reg = frames.get(frameIndex);
        String name = activeParts.numericFrameCount > 0
                ? activeParts.numericFrameName(frameIndex)
                : patternBase + (appendAngle ? angle : "") + "-" + (char) ('a' + frameIndex);

        Vector2 off = SpriteOffsetUtil.selectOffset(offset1.get(name), offset2.get(name), flipX, ZERO_OFFSET);

        float topLeftX = pos.x + off.x;
        float topLeftY = pos.y + off.y;

        int w = reg.getRegionWidth();
        int h = reg.getRegionHeight();

        // Draw an optional standalone Shd sprite before the outlined entity.
        batch.setShader(null);
        if (com.perso.T4C.config.GamePreferencesStore.get().isNewShadows()) {
            renderShadow(batch, name, pos, flipX);
        }

        // Apply outline shader if hovered
        if (isHovered && outlineShader != null && outlineShader.isCompiled()) {
            batch.setShader(outlineShader);
            outlineShader.setUniformf("u_texelSize", 1f / w, 1f / h);

            // Calculate color based on health: 100% = green (0,1,0), 0% = red (1,0,0)
            // Clamp healthPercent between 0 and 1
            float hp = Math.max(0f, Math.min(1f, healthPercent));
            float red = 1f - hp;    // High health = 0 red, Low health = 1 red
            float green = hp;        // High health = 1 green, Low health = 0 green
            outlineShader.setUniformf("u_outlineColor", red, green, 0f, 1f);
        }

        if (flipX) {
            batch.draw(reg, topLeftX + w, topLeftY + h, -w, -h);
        } else {
            batch.draw(reg, topLeftX, topLeftY + h, w, -h);
        }

        // Reset shader after drawing
        if (isHovered && outlineShader != null && outlineShader.isCompiled()) {
            batch.setShader(null);
        }

        // Draw monster name above head if requested
        // Use fixed base position (pos) without any width/height calculations to keep name completely stable
        if (showName && entityName != null && !entityName.isEmpty()) {
            com.perso.T4C.entity.NameRenderer.renderName(batch, entityName, pos.x, pos.y, w, h);
        }
    }

    /**
     * Load all animations for the monster.
     */
    private void loadAllAnimations() throws GameException {
        SpriteLoader loader = SpriteLoader.getInstance();

        // Load walk animations
        if (walkParts != null) {
            loadAnimationPattern(loader, walkParts, walkAnimations, true);
        }

        // Load attack animations
        if (attackParts != null) {
            loadAnimationPattern(loader, attackParts, attackAnimations, true);
        }

        // Load death animations
        if (deathParts != null) {
            loadAnimationPattern(loader, deathParts, deathAnimations, false);
        }

        attackDuration = computeAttackDuration();
    }

    /**
     * The full attack animation must play out (not cut off mid-swing), so its
     * duration is derived from however many frames were actually loaded for
     * this monster's attack pattern rather than a fixed constant.
     */
    private float computeAttackDuration() {
        int maxFrames = 0;
        for (List<TextureRegion> frames : attackAnimations.values()) {
            if (frames != null) {
                maxFrames = Math.max(maxFrames, frames.size());
            }
        }
        return maxFrames > 0 ? Math.max(MIN_ATTACK_DURATION, maxFrames * FRAME_DURATION) : MIN_ATTACK_DURATION;
    }

    /**
     * Load animation pattern (e.g., "Goblin#l" where # is replaced with angle).
     */
    private void loadAnimationPattern(SpriteLoader loader, PatternParts pattern, Map<String, List<TextureRegion>> animMap, boolean useAngle) {
        String baseName = pattern.baseName;
        char stopLetter = pattern.stopLetter;

        if (pattern.numericFrameCount > 0) {
            List<TextureRegion> frames = new ArrayList<>();
            for (int i = 0; i < pattern.numericFrameCount; i++) {
                String key = pattern.numericFrameName(i);
                TextureRegion region = loader.getRegionFromSpriteName(key);
                if (region == null) {
                    break;
                }
                frames.add(region);
                cacheOffsets(key, loader);
                cacheShadow(key, loader);
            }
            if (!frames.isEmpty()) {
                animMap.put("000", frames);
            }
            return;
        }

        if (useAngle && !pattern.angleless) {
            for (String angle : ANGLES) {
                List<TextureRegion> frames = new ArrayList<>();

                for (char c = FRAME_START; c <= stopLetter; c++) {
                    String key = baseName + angle + "-" + c;
                    TextureRegion r = loader.getRegionFromSpriteName(key);
                    if (r == null) {
                        break;
                    }
                    frames.add(r);
                    cacheOffsets(key, loader);
                    cacheShadow(key, loader);
                }

                if (!frames.isEmpty()) {
                    animMap.put(angle, frames);
                }
            }
        } else {
            List<TextureRegion> frames = new ArrayList<>();
            for (char c = FRAME_START; c <= stopLetter; c++) {
                String key = baseName + "-" + c;
                TextureRegion r = loader.getRegionFromSpriteName(key);
                if (r == null) {
                    break;
                }
                frames.add(r);
                cacheOffsets(key, loader);
                cacheShadow(key, loader);
            }

            if (!frames.isEmpty()) {
                animMap.put("000", frames);
            }
        }

    }
    /**
     * Cache draw offsets for a sprite name.
     */
    /**
     * Get walk animation frames for angle.
     */
    private List<TextureRegion> getWalkFrames(String angle) {
        List<TextureRegion> frames = walkAnimations.get(angle);
        if (frames != null) {
            return frames;
        }
        return walkAnimations.getOrDefault("000", Collections.emptyList());
    }

    private int getCurrentWalkFrameIndex() {
        int frameCount = 0;
        for (List<TextureRegion> frames : walkAnimations.values()) {
            if (frames != null && frames.size() > frameCount) {
                frameCount = frames.size();
            }
        }
        if (frameCount <= 1) {
            return 0;
        }
        return (int) (animTimer / FRAME_DURATION) % frameCount;
    }

    /**
     * Get attack animation frames for angle.
     */
    private List<TextureRegion> getAttackFrames(String angle) {
        List<TextureRegion> frames = attackAnimations.get(angle);
        if (frames != null) {
            return frames;
        }
        return attackAnimations.getOrDefault("000", Collections.emptyList());
    }

    /**
     * Get death animation frames for angle.
     */
    private List<TextureRegion> getDeathFrames(String angle) {
        return deathAnimations.getOrDefault(angle, Collections.emptyList());
    }

    /**
     * Refresh animations if textures were reloaded.
     */
    public void refresh() throws GameException {
        if (!isTextureGenCurrent()) {
            walkAnimations.clear();
            attackAnimations.clear();
            deathAnimations.clear();
            offset1.clear();
            offset2.clear();
            clearShadowCache();
            loadAllAnimations();
            markRefreshed();
        }
    }

    /**
     * Refresh if needed (check texture generation).
     */
    private void refreshIfNeeded() {
        try {
            if (!isTextureGenCurrent()) {
                refresh();
            }
        } catch (GameException e) {
            log.error("Failed to refresh monster animations", e);
        }
    }

    private record PatternParts(String baseName, char stopLetter, boolean angleless,
                                int numericFrameCount, int numericDigits) {
        private static PatternParts from(String pattern) {
            if (pattern == null) {
                return null;
            }
            int numericSeparator = pattern.lastIndexOf('@');
            if (numericSeparator > 0 && numericSeparator + 1 < pattern.length()) {
                try {
                    String countText = pattern.substring(numericSeparator + 1);
                    int count = Integer.parseInt(countText);
                    int digits = countText.length();
                    return new PatternParts(pattern.substring(0, numericSeparator), FRAME_START,
                            true, count, digits);
                } catch (NumberFormatException ignored) {
                    // Fall through to the legacy directional syntax.
                }
            }
            int anglelessSeparator = pattern.lastIndexOf('!');
            if (anglelessSeparator > 0 && anglelessSeparator + 1 < pattern.length()) {
                return new PatternParts(pattern.substring(0, anglelessSeparator),
                        pattern.charAt(anglelessSeparator + 1), true, 0, 0);
            }
            int separator = pattern.indexOf('#');
            if (separator < 0 || separator + 1 >= pattern.length()) {
                return new PatternParts(pattern, FRAME_START, pattern.endsWith("000"), 0, 0);
            }
            String baseName = pattern.substring(0, separator);
            return new PatternParts(baseName, pattern.charAt(separator + 1), baseName.endsWith("000"), 0, 0);
        }

        private String numericFrameName(int zeroBasedIndex) {
            return baseName + String.format("%0" + numericDigits + "d", zeroBasedIndex + 1);
        }
    }

}
