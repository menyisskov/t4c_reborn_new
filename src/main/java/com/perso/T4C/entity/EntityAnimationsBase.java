package com.perso.T4C.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.helper.SpriteFrameMetadata;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.render.SpriteOffsetUtil;
import java.util.HashMap;
import java.util.Map;

public abstract class EntityAnimationsBase {
  protected static final String[] ANGLES = {"000", "045", "090", "135", "180", "225", "270", "315"};
  protected static final char FRAME_START = 'a';
  protected static final Vector2 ZERO_OFFSET = new Vector2(0, 0);
  protected final Map<String, Vector2> offset1 = new HashMap<>();
  protected final Map<String, Vector2> offset2 = new HashMap<>();
  private final Map<String, SpriteFrameMetadata> shadowRegions = new HashMap<>();
  private int lastTextureGen = -1;

  protected void cacheOffsets(String name, SpriteLoader loader) {
    if (offset1.containsKey(name) && offset2.containsKey(name)) return;
    Vector2 o1 = new Vector2();
    Vector2 o2 = new Vector2();
    SpriteLoader.Sprite s = loader.getSpriteMeta(name);
    if (s != null) {
      o1.set(s.getDrawOffset1X(), s.getDrawOffset1Y());
      o2.set(s.getDrawOffset2X(), s.getDrawOffset2Y());
    }
    offset1.put(name, o1);
    offset2.put(name, o2);
  }

  protected void cacheShadow(String frameName, SpriteLoader loader) {
    String shadowName = frameName + "Shd";
    try {
      SpriteFrameMetadata shadow = loader.getFrameMetadata(shadowName);
      if (shadow != null) {
        shadowRegions.put(frameName, shadow);
        cacheOffsets(shadowName, loader);
      }
    } catch (Exception ignored) {
    }
  }

  protected void renderShadow(SpriteBatch batch, String frameName, Vector2 pos, boolean flipX) {
    SpriteFrameMetadata metadata = shadowRegions.get(frameName);
    if (metadata == null) return;
    TextureRegion shadow = renderRegion(metadata);
    String shadowName = frameName + "Shd";
    Vector2 off =
        SpriteOffsetUtil.selectOffset(
            offset1.get(shadowName), offset2.get(shadowName), flipX, ZERO_OFFSET);
    float x = pos.x + off.x;
    float y = pos.y + off.y;
    int width = shadow.getRegionWidth();
    int height = shadow.getRegionHeight();
    if (flipX) {
      batch.draw(shadow, x + width, y + height, -width, -height);
    } else {
      batch.draw(shadow, x, y + height, width, -height);
    }
  }

  protected void clearShadowCache() {
    shadowRegions.clear();
  }

  protected TextureRegion renderRegion(SpriteFrameMetadata metadata) {
    TextureRegion region = SpriteLoader.getInstance().getRegionFromSpriteName(metadata.name());
    if (region == null) throw new GameException("Missing sprite frame: " + metadata.name());
    return region;
  }

  protected boolean isTextureGenCurrent() {
    return SpriteLoader.getInstance().getTextureGeneration() == lastTextureGen;
  }

  protected void markRefreshed() {
    lastTextureGen = SpriteLoader.getInstance().getTextureGeneration();
  }
}
