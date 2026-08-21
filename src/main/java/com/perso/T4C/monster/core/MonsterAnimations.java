package com.perso.T4C.monster.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.config.NativeTimingProfile;
import com.perso.T4C.entity.EntityAnimationsBase;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.render.SpriteOffsetUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class MonsterAnimations extends EntityAnimationsBase {
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
  private float attackDuration = MIN_ATTACK_DURATION;

  public MonsterAnimations(
      String walkPattern,
      String attackPattern,
      String deathPattern,
      String soundAttack,
      String soundDeath)
      throws GameException {
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

  public void startAttack() {
    if (isDying || isAttacking) return;
    isAttacking = true;
    holdingAttackPose = false;
    attackTimer = 0f;
    if (soundAttack != null && !soundAttack.isEmpty()) {
      SoundManager.animateSound(soundAttack);
    }
  }

  public void clearAttackPose() {
    isAttacking = false;
    holdingAttackPose = false;
    attackTimer = 0f;
  }

  public boolean hasAttackAnimation() {
    return attackPattern != null && !attackAnimations.isEmpty();
  }

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

  public void reset() {
    isDying = false;
    isDeadComplete = false;
    deathTimer = 0f;
    isAttacking = false;
    holdingAttackPose = false;
    attackTimer = 0f;
    animTimer = 0f;
  }

  public void render(
      SpriteBatch batch,
      Vector2 pos,
      String angle,
      boolean flipX,
      boolean moving,
      boolean isHovered,
      ShaderProgram outlineShader,
      float healthPercent) {
    render(batch, pos, angle, flipX, moving, isHovered, outlineShader, healthPercent, null, false);
  }

  public void render(
      SpriteBatch batch,
      Vector2 pos,
      String angle,
      boolean flipX,
      boolean moving,
      boolean isHovered,
      ShaderProgram outlineShader,
      float healthPercent,
      String entityName,
      boolean showName) {
    refreshIfNeeded();
    List<TextureRegion> frames;
    int frameIndex;
    String patternBase;
    boolean appendAngle;
    PatternParts activeParts;
    if (isDying && deathPattern != null) {
      frames = getDeathFrames("000");
      frameIndex = (int) (deathTimer / NativeTimingProfile.current().frameDurationSeconds());
      if (frameIndex >= frames.size()) {
        frameIndex = frames.size() - 1;
      }
      patternBase = deathParts.baseName;
      appendAngle = false;
      activeParts = deathParts;
    } else if ((isAttacking || holdingAttackPose) && attackPattern != null) {
      frames = getAttackFrames(angle);
      frameIndex =
          holdingAttackPose
              ? Math.max(0, frames.size() - 1)
              : Math.min((int) (attackTimer / NativeTimingProfile.current().frameDurationSeconds()), Math.max(0, frames.size() - 1));
      patternBase = attackParts.baseName;
      appendAngle = !attackParts.angleless;
      activeParts = attackParts;
    } else {
      frames = getWalkFrames(angle);
      frameIndex = moving ? (int) (animTimer / NativeTimingProfile.current().frameDurationSeconds()) % Math.max(1, frames.size()) : 0;
      patternBase = walkParts.baseName;
      appendAngle = !walkParts.angleless;
      activeParts = walkParts;
    }
    if (frames.isEmpty()) return;
    frameIndex = Math.min(frameIndex, frames.size() - 1);
    TextureRegion reg = frames.get(frameIndex);
    String name =
        activeParts.numericFrameCount > 0
            ? activeParts.numericFrameName(frameIndex)
            : patternBase + (appendAngle ? angle : "") + "-" + (char) ('a' + frameIndex);
    Vector2 off =
        SpriteOffsetUtil.selectOffset(offset1.get(name), offset2.get(name), flipX, ZERO_OFFSET);
    float topLeftX = pos.x + off.x;
    float topLeftY = pos.y + off.y;
    int w = reg.getRegionWidth();
    int h = reg.getRegionHeight();
    batch.setShader(null);
    if (isHovered && outlineShader != null && outlineShader.isCompiled()) {
      batch.setShader(outlineShader);
      outlineShader.setUniformf("u_texelSize", 1f / w, 1f / h);
      float hp = Math.max(0f, Math.min(1f, healthPercent));
      float red = 1f - hp;
      float green = hp;
      outlineShader.setUniformf("u_outlineColor", red, green, 0f, 1f);
    }
    if (flipX) {
      batch.draw(reg, topLeftX + w, topLeftY + h, -w, -h);
    } else {
      batch.draw(reg, topLeftX, topLeftY + h, w, -h);
    }
    if (isHovered && outlineShader != null && outlineShader.isCompiled()) {
      batch.setShader(null);
    }
    if (showName && entityName != null && !entityName.isEmpty()) {
      com.perso.T4C.entity.NameRenderer.renderName(batch, entityName, pos.x, pos.y, w, h);
    }
  }

  private void loadAllAnimations() throws GameException {
    SpriteLoader loader = SpriteLoader.getInstance();
    if (walkParts != null) {
      loadAnimationPattern(loader, walkParts, walkAnimations, true);
    }
    if (attackParts != null) {
      loadAnimationPattern(loader, attackParts, attackAnimations, true);
    }
    if (deathParts != null) {
      loadAnimationPattern(loader, deathParts, deathAnimations, false);
    }
    attackDuration = computeAttackDuration();
  }

  private float computeAttackDuration() {
    int maxFrames = 0;
    for (List<TextureRegion> frames : attackAnimations.values()) {
      if (frames != null) {
        maxFrames = Math.max(maxFrames, frames.size());
      }
    }
    return maxFrames > 0
        ? Math.max(MIN_ATTACK_DURATION, maxFrames * NativeTimingProfile.current().frameDurationSeconds())
        : MIN_ATTACK_DURATION;
  }

  private void loadAnimationPattern(
      SpriteLoader loader,
      PatternParts pattern,
      Map<String, List<TextureRegion>> animMap,
      boolean useAngle) {
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
    return (int) (animTimer / NativeTimingProfile.current().frameDurationSeconds()) % frameCount;
  }

  private List<TextureRegion> getAttackFrames(String angle) {
    List<TextureRegion> frames = attackAnimations.get(angle);
    if (frames != null) {
      return frames;
    }
    return attackAnimations.getOrDefault("000", Collections.emptyList());
  }

  private List<TextureRegion> getDeathFrames(String angle) {
    return deathAnimations.getOrDefault(angle, Collections.emptyList());
  }

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

  private void refreshIfNeeded() {
    try {
      if (!isTextureGenCurrent()) {
        refresh();
      }
    } catch (GameException e) {
      log.error("Failed to refresh monster animations", e);
    }
  }

  private record PatternParts(
      String baseName,
      char stopLetter,
      boolean angleless,
      int numericFrameCount,
      int numericDigits) {
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
          return new PatternParts(
              pattern.substring(0, numericSeparator), FRAME_START, true, count, digits);
        } catch (NumberFormatException ignored) {
        }
      }
      int anglelessSeparator = pattern.lastIndexOf('!');
      if (anglelessSeparator > 0 && anglelessSeparator + 1 < pattern.length()) {
        return new PatternParts(
            pattern.substring(0, anglelessSeparator),
            pattern.charAt(anglelessSeparator + 1),
            true,
            0,
            0);
      }
      int separator = pattern.indexOf('#');
      if (separator < 0 || separator + 1 >= pattern.length()) {
        return new PatternParts(pattern, FRAME_START, pattern.endsWith("000"), 0, 0);
      }
      String baseName = pattern.substring(0, separator);
      return new PatternParts(
          baseName, pattern.charAt(separator + 1), baseName.endsWith("000"), 0, 0);
    }

    private String numericFrameName(int zeroBasedIndex) {
      return baseName + String.format("%0" + numericDigits + "d", zeroBasedIndex + 1);
    }
  }
}
