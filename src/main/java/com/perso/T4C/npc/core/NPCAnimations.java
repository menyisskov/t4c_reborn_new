package com.perso.T4C.npc.core;

import java.util.Map;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BufferUtils;
import com.perso.T4C.entity.EntityAnimationsBase;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.AppearanceDefaultsCatalog;
import com.perso.T4C.helper.ConcealmentResolver;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.player.BodyPart;
import java.nio.IntBuffer;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class NPCAnimations extends EntityAnimationsBase {

  private static final float FRAME_DURATION = 0.10f;

  private static Map<BodyPart, String> nakedParts(boolean female) {

    return AppearanceDefaultsCatalog.nakedParts(
        female ? AppearanceDefaultsCatalog.FEMALE : AppearanceDefaultsCatalog.MALE);
  }

  private final String spriteBase;

  private String soundAttack;

  private final Map<BodyPart, String> partMap = new EnumMap<>(BodyPart.class);

  private final com.perso.T4C.player.PlayerAnimations playerAnimations;

  private final Map<String, Map<String, List<TextureRegion>>> animations = new HashMap<>();

  private final List<DrawPart> drawParts = new ArrayList<>();

  private final Rectangle renderBounds = new Rectangle();

  private final Matrix4 offscreenProjection = new Matrix4();

  private final IntBuffer viewportBuffer = BufferUtils.newIntBuffer(16);

  private SpriteBatch offscreenBatch;

  private FrameBuffer compositeBuffer;

  private TextureRegion compositeRegion;

  private float animTimer = 0f;

  private boolean standingIdle = false;

  public void setStandingIdle(boolean standingIdle) {

    this.standingIdle = standingIdle;
  }

  public void startAttack(String angle) {

    if (playerAnimations != null) {

      playerAnimations.startAttack(angle);
    }

    if (soundAttack != null && !soundAttack.isBlank()) {
      com.perso.T4C.audio.SoundManager.animateSound(soundAttack);
    }
  }

  public boolean isAttacking() {

    return playerAnimations != null && playerAnimations.isAttacking();
  }

  public boolean isHoldingAttackPose() {

    return playerAnimations != null && playerAnimations.isHoldingMeleeAttackPose();
  }

  public void clearAttackPose() {

    if (playerAnimations != null) {

      playerAnimations.clearAttackPose();
    }
  }

  public NPCAnimations(Object... parts) throws GameException {

    this(null, parts);
  }

  public NPCAnimations(String spriteBase, Sounds sounds, Object... parts) throws GameException {
    this(spriteBase, parts);
    this.soundAttack = sounds == null ? null : sounds.attack();
  }

  public record Sounds(String attack, String death, String hit) {
    public static Sounds forClass(Class<?> type) {
      return new Sounds(value(type, "SOUND_ATTACK"), value(type, "SOUND_DEATH"), value(type, "SOUND_HIT"));
    }

    private static String value(Class<?> type, String name) {
      try {
        Object value = type.getField(name).get(null);
        return value == null || value.toString().isBlank() ? null : value.toString();
      } catch (ReflectiveOperationException ignored) {
        return null;
      }
    }
  }

  public boolean hasSingleSpriteBase() {

    return spriteBase != null;
  }

  public boolean hasObjectAppearance() {

    return isStaticBase(spriteBase);
  }

  public NPCAnimations(String spriteBase, Object... parts) throws GameException {

    this.spriteBase = spriteBase == null || spriteBase.isBlank() ? null : spriteBase.trim();
    this.soundAttack = null;

    for (int i = 0; i < parts.length; i += 2) {

      partMap.put((BodyPart) parts[i], (String) parts[i + 1]);
    }

    if (this.spriteBase == null) {

      Map<BodyPart, String> resolved = effectiveCompositeParts(partMap);

      Object[] resolvedParts = new Object[resolved.size() * 2];

      int i = 0;

      for (Map.Entry<BodyPart, String> e : resolved.entrySet()) {

        resolvedParts[i++] = e.getKey();

        resolvedParts[i++] = e.getValue();
      }

      this.playerAnimations = new com.perso.T4C.player.PlayerAnimations(resolvedParts);

    } else {

      this.playerAnimations = null;

      loadAllAnimations();
    }
  }

  public static String defaultNakedBase(Map<BodyPart, String> parts, BodyPart part) {

    if (!effectiveCompositeParts(parts).containsKey(part)) {

      return null;
    }

    return nakedParts(isFemaleParts(parts)).get(part);
  }

  public static String resolveCompositeBase(Map<BodyPart, String> parts, BodyPart part) {

    return effectiveCompositeParts(parts).get(part);
  }

  public static EnumMap<BodyPart, String> effectiveCompositeParts(Map<BodyPart, String> parts) {

    EnumMap<BodyPart, String> effective = new EnumMap<>(BodyPart.class);

    EnumSet<BodyPart> explicitParts = EnumSet.noneOf(BodyPart.class);

    if (parts != null) {

      for (Map.Entry<BodyPart, String> entry : parts.entrySet()) {

        BodyPart part = entry.getKey();

        String base = entry.getValue();

        if (part != null && base != null && !base.isBlank()) {

          effective.put(part, base.trim());

          explicitParts.add(part);
        }
      }
    }

    String feet = effective.get(BodyPart.FEET);

    if (isEquippedBoot(feet)) {

      effective.remove(BodyPart.FEET);

      explicitParts.remove(BodyPart.FEET);

      effective.putIfAbsent(BodyPart.BOOT, feet);

      explicitParts.add(BodyPart.BOOT);
    }

    Map<BodyPart, String> defaults = nakedParts(isFemaleParts(effective));

    for (Map.Entry<BodyPart, String> entry : defaults.entrySet()) {

      effective.putIfAbsent(entry.getKey(), entry.getValue());
    }

    ConcealmentResolver.applyConcealment(effective, explicitParts);

    return effective;
  }

  private static boolean isFemaleParts(Map<BodyPart, String> parts) {

    if (parts == null) {

      return false;
    }

    return parts.values().stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .anyMatch(base -> base.regionMatches(true, 0, "Wo", 0, 2));
  }

  private static boolean isEquippedBoot(String spriteBase) {

    if (spriteBase == null || spriteBase.isBlank()) return false;

    String base = spriteBase.toLowerCase(Locale.ROOT);

    return !base.contains("nakedfoot") && !base.contains("nakedfeet");
  }

  public void update(float delta, boolean moving) {

    if (playerAnimations != null) {

      playerAnimations.update(delta, moving);

      return;
    }

    animTimer = moving || isStaticBase(spriteBase) ? animTimer + delta : 0f;
  }

  public void renderComposite(
      SpriteBatch batch, Vector2 pos, String angle, boolean flipX, boolean moving) {

    if (playerAnimations != null) {

      playerAnimations.renderComposite(batch, pos, angle, flipX, moving);

      return;
    }

    if (spriteBase != null) {
      renderSingleSprite(batch, pos, angle, flipX, moving, false, null, null, false);
    }
  }

  public Rectangle getRenderBounds(
      Vector2 pos, String angle, boolean flipX, boolean moving, Rectangle out) {

    if (out == null) {

      throw new IllegalArgumentException("out must not be null");
    }

    refreshIfNeeded();

    if (playerAnimations != null) {

      return playerAnimations.getRenderBounds(pos, angle, flipX, moving, out);
    }

    Bounds bounds = calculateBounds(pos, angle, flipX, moving);

    if (bounds == null) {

      return out.set(0f, 0f, 0f, 0f);
    }

    return out.set(bounds.minX(), bounds.minY(), bounds.width(), bounds.height());
  }

  private static final String STATIC_PREFIX = "@static:";

  private static boolean isStaticBase(String base) {

    return base != null && base.startsWith(STATIC_PREFIX);
  }

  private String frameSpriteName(String base, String angle, int frameIndex) {

    if (isStaticBase(base)) {

      return staticFrameName(base, frameIndex);
    }

    return base + angle + "-" + (char) (FRAME_START + frameIndex);
  }

  private int animatedFrameIndex(int frameCount, boolean moving) {

    if (isStaticBase(spriteBase)) {

      return frameCount <= 1 ? 0 : (int) (animTimer / FRAME_DURATION) % frameCount;
    }

    return moving ? spriteFrameIndex(frameCount) : 0;
  }

  private int spriteFrameIndex(int frameCount) {

    int tick = (int) (animTimer / FRAME_DURATION);

    if (!standingIdle) {

      return tick % frameCount;
    }

    int cycle = frameCount * 3;

    int phase = tick % cycle;

    return phase < frameCount ? phase : 0;
  }

  public void render(
      SpriteBatch batch,
      Vector2 pos,
      String angle,
      boolean flipX,
      boolean moving,
      boolean isHovered,
      ShaderProgram outlineShader) {

    render(batch, pos, angle, flipX, moving, isHovered, outlineShader, null, false);
  }

  public void render(
      SpriteBatch batch,
      Vector2 pos,
      String angle,
      boolean flipX,
      boolean moving,
      boolean isHovered,
      ShaderProgram outlineShader,
      String entityName,
      boolean showName) {

    refreshIfNeeded();

    if (spriteBase != null) {

      renderSingleSprite(
          batch, pos, angle, flipX, moving, isHovered, outlineShader, entityName, showName);

      return;
    }

    playerAnimations.render(batch, pos, angle, flipX, moving, outlineShader, isHovered, 1f, 1f, 0f);

    if (showName && entityName != null && !entityName.isEmpty()) {

      Rectangle bounds = playerAnimations.getRenderBounds(pos, angle, flipX, moving, renderBounds);

      com.perso.T4C.entity.NameRenderer.renderName(
          batch, entityName, pos.x, pos.y, bounds.width, bounds.height);
    }
  }

  public void renderDialogText(
      SpriteBatch batch, Vector2 pos, String angle, boolean flipX, String text, String keyword) {

    renderDialogText(
        batch, pos, angle, flipX, text, keyword == null ? List.of() : List.of(keyword));
  }

  public void renderDialogText(
      SpriteBatch batch,
      Vector2 pos,
      String angle,
      boolean flipX,
      String text,
      List<String> keywords) {

    if (text == null || text.isEmpty()) {

      return;
    }

    refreshIfNeeded();

    float boundsY;

    float boundsWidth;

    float boundsHeight;

    if (playerAnimations != null) {

      Rectangle bounds = playerAnimations.getRenderBounds(pos, angle, flipX, false, renderBounds);

      if (bounds.width <= 0f && bounds.height <= 0f) return;

      boundsY = bounds.y;

      boundsWidth = bounds.width;

      boundsHeight = bounds.height;

    } else {

      Bounds bounds = calculateStableBounds(pos, angle, flipX);

      if (bounds == null) return;

      boundsY = bounds.minY();

      boundsWidth = bounds.width();

      boundsHeight = bounds.height();
    }

    com.perso.T4C.entity.NameRenderer.renderNameWithKeywords(
        batch, text, keywords, pos.x, boundsY, boundsWidth, boundsHeight);
  }

  private void renderSingleSprite(
      SpriteBatch batch,
      Vector2 pos,
      String angle,
      boolean flipX,
      boolean moving,
      boolean isHovered,
      ShaderProgram outlineShader,
      String entityName,
      boolean showName) {

    List<TextureRegion> frames = getFrames(spriteBase, angle);

    if (frames.isEmpty()) return;

    int frameIndex = animatedFrameIndex(frames.size(), moving);

    TextureRegion reg = frames.get(frameIndex);

    String name = frameSpriteName(spriteBase, angle, frameIndex);

    Vector2 off =
        flipX ? offset2.getOrDefault(name, ZERO_OFFSET) : offset1.getOrDefault(name, ZERO_OFFSET);

    float topLeftX = pos.x + off.x;

    float topLeftY = pos.y + off.y;

    int w = reg.getRegionWidth();

    int h = reg.getRegionHeight();

    renderShadow(batch, name, pos, flipX);

    if (isHovered && outlineShader != null && outlineShader.isCompiled()) {

      batch.setShader(outlineShader);

      outlineShader.setUniformf("u_texelSize", 1f / w, 1f / h);

      outlineShader.setUniformf("u_outlineColor", 1f, 1f, 0f, 1f);
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

  private Bounds calculateStableBounds(Vector2 pos, String angle, boolean flipX) {

    List<TextureRegion> frames = getFrames(spriteBase, angle);

    if (frames.isEmpty()) return null;

    float minX = Float.POSITIVE_INFINITY;

    float maxX = Float.NEGATIVE_INFINITY;

    float minY = Float.POSITIVE_INFINITY;

    float maxY = Float.NEGATIVE_INFINITY;

    for (int index = 0; index < frames.size(); index++) {

      TextureRegion region = frames.get(index);

      String name = frameSpriteName(spriteBase, angle, index);

      Vector2 off =
          flipX ? offset2.getOrDefault(name, ZERO_OFFSET) : offset1.getOrDefault(name, ZERO_OFFSET);

      minX = Math.min(minX, pos.x + off.x);

      maxX = Math.max(maxX, pos.x + off.x + region.getRegionWidth());

      minY = Math.min(minY, pos.y + off.y);

      maxY = Math.max(maxY, pos.y + off.y + region.getRegionHeight());
    }

    return new Bounds(minX, maxX, minY, maxY);
  }

  private Bounds calculateBounds(Vector2 pos, String angle, boolean flipX, boolean moving) {

    List<TextureRegion> frames = getFrames(spriteBase, angle);

    if (frames.isEmpty()) return null;

    int frameIndex = animatedFrameIndex(frames.size(), moving);

    TextureRegion reg = frames.get(frameIndex);

    String name = frameSpriteName(spriteBase, angle, frameIndex);

    Vector2 off =
        flipX ? offset2.getOrDefault(name, ZERO_OFFSET) : offset1.getOrDefault(name, ZERO_OFFSET);

    return new Bounds(
        pos.x + off.x,
        pos.x + off.x + reg.getRegionWidth(),
        pos.y + off.y,
        pos.y + off.y + reg.getRegionHeight());
  }

  public List<com.badlogic.gdx.math.Rectangle> getDialogWordBounds(
      Vector2 pos, String angle, boolean flipX, String text, String word) {

    if (text == null || text.isEmpty() || word == null || word.isEmpty()) {

      return List.of();
    }

    refreshIfNeeded();

    Rectangle bounds;

    if (playerAnimations != null) {

      bounds = playerAnimations.getRenderBounds(pos, angle, flipX, false, renderBounds);

      if (bounds.width <= 0f && bounds.height <= 0f) return List.of();

    } else {

      Bounds b = calculateStableBounds(pos, angle, flipX);

      if (b == null) return List.of();

      bounds = new Rectangle(b.minX(), b.minY(), b.width(), b.height());
    }

    return com.perso.T4C.entity.NameRenderer.getWordBounds(
        text, word, pos.x, bounds.y, bounds.width, bounds.height);
  }

  private void loadAllAnimations() throws GameException {

    if (spriteBase != null) {

      loadBaseAnimations(SpriteLoader.getInstance(), spriteBase);
    }
  }

  private List<TextureRegion> loadStaticFrames(SpriteLoader loader, String baseName) {

    String spriteName = baseName.substring(STATIC_PREFIX.length());

    List<TextureRegion> frames = new ArrayList<>();

    if (!hasFrameSuffix(spriteName)) {

      TextureRegion single = loader.getRegionFromSpriteName(spriteName);

      if (single != null) {

        cacheOffsets(spriteName, loader);

        frames.add(single);
      }

      return frames;
    }

    String family = spriteName.substring(0, spriteName.length() - 1);

    for (char c = FRAME_START; ; c++) {

      String key = family + c;

      TextureRegion region = loader.getRegionFromSpriteName(key);

      if (region == null) break;

      cacheOffsets(key, loader);

      frames.add(region);
    }

    return frames;
  }

  private static boolean hasFrameSuffix(String spriteName) {

    int length = spriteName.length();

    return length >= 2
        && spriteName.charAt(length - 2) == '-'
        && Character.isLetter(spriteName.charAt(length - 1));
  }

  private static String staticFrameName(String baseName, int frameIndex) {

    String spriteName = baseName.substring(STATIC_PREFIX.length());

    if (!hasFrameSuffix(spriteName)) {

      return spriteName;
    }

    return spriteName.substring(0, spriteName.length() - 1) + (char) (FRAME_START + frameIndex);
  }

  private void loadBaseAnimations(SpriteLoader loader, String baseName) throws GameException {

    Map<String, List<TextureRegion>> angleMap = new HashMap<>();

    if (baseName.equals("@invisible")) {

      animations.put(baseName, angleMap);

      return;
    }

    if (isStaticBase(baseName)) {

      List<TextureRegion> frames = loadStaticFrames(loader, baseName);

      if (!frames.isEmpty()) {

        for (String angle : ANGLES) angleMap.put(angle, frames);
      }

      animations.put(baseName, angleMap);

      return;
    }

    for (String angle : ANGLES) {

      List<TextureRegion> frames = new ArrayList<>();

      for (char c = FRAME_START; ; c++) {

        String key = baseName + angle + "-" + c;

        TextureRegion r = loader.getRegionFromSpriteName(key);

        if (r == null) break;

        frames.add(r);

        cacheOffsets(key, loader);

        if (spriteBase != null && spriteBase.equals(baseName)) {

          cacheShadow(key, loader);
        }
      }

      if (!frames.isEmpty()) {

        angleMap.put(angle, frames);
      }
    }

    animations.put(baseName, angleMap);
  }

  private List<TextureRegion> getFrames(String base, String angle) {

    Map<String, List<TextureRegion>> am = animations.get(base);

    if (am == null) {

      try {

        loadBaseAnimations(SpriteLoader.getInstance(), base);

        am = animations.get(base);

        markRefreshed();

      } catch (GameException e) {

        log.debug("Failed to load NPC animation base '{}'", base, e);

        return Collections.emptyList();
      }
    }

    List<TextureRegion> f = am.get(angle);

    return f != null ? f : Collections.emptyList();
  }

  private void refreshIfNeeded() {

    if (isTextureGenCurrent()) return;

    try {

      animations.clear();

      offset1.clear();

      offset2.clear();

      clearShadowCache();

      loadAllAnimations();

      markRefreshed();

    } catch (GameException e) {

      log.error("Failed to refresh NPC animations", e);
    }
  }

  public void refresh() throws GameException {

    if (playerAnimations != null) {

      playerAnimations.refresh();

      return;
    }

    animations.clear();

    offset1.clear();

    offset2.clear();

    clearShadowCache();

    loadAllAnimations();

    markRefreshed();
  }

  public void dispose() {

    if (playerAnimations != null) {

      playerAnimations.dispose();
    }

    if (compositeBuffer != null) {

      compositeBuffer.dispose();

      compositeBuffer = null;

      compositeRegion = null;
    }

    if (offscreenBatch != null) {

      offscreenBatch.dispose();

      offscreenBatch = null;
    }
  }

  private static final class DrawPart {

    private final TextureRegion region;

    private final float x;

    private final float y;

    private final int width;

    private final int height;

    private final boolean flipX;

    private DrawPart(TextureRegion region, float x, float y, int width, int height, boolean flipX) {

      this.region = region;

      this.x = x;

      this.y = y;

      this.width = width;

      this.height = height;

      this.flipX = flipX;
    }
  }

  private record Bounds(float minX, float maxX, float minY, float maxY) {

    float width() {

      return maxX - minX;
    }

    float height() {

      return maxY - minY;
    }
  }
}
