package com.perso.T4C.harvest;

import static com.perso.T4C.config.GameConstants.GRID_H;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.entity.NameRenderer;
import com.perso.T4C.entity.Nameable;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemDefinition;
import lombok.Getter;
import lombok.Setter;

public final class HerbNode {
  private static final float NAME_RENDERER_VERTICAL_OFFSET = 90f;
  private static final float NAME_LABEL_ABOVE_HERB = 18f;

  public enum State {
    AVAILABLE,
    HARVESTING,
    HARVESTED
  }

  @Getter private final HerbDefinition definition;
  @Getter private final Vector2 position = new Vector2();
  @Getter private final int tileX;
  @Getter private final int tileY;
  @Getter @Setter private boolean hovered;
  @Getter @Setter private State state = State.AVAILABLE;
  private TextureRegion region;
  private boolean regionResolved;
  private long nameDisplayUntil;

  public HerbNode(HerbDefinition definition, int tileX, int tileY, float worldX, float worldY) {
    this.definition = definition;
    this.tileX = tileX;
    this.tileY = tileY;
    position.set(worldX, worldY);
  }

  public float getDepthY() {
    return position.y / GRID_H;
  }

  public void showName() {
    nameDisplayUntil = System.currentTimeMillis() + Nameable.NAME_DISPLAY_MS;
  }

  public boolean isNameVisible() {
    return System.currentTimeMillis() < nameDisplayUntil;
  }

  public boolean isMouseOver(float worldX, float worldY) {
    if (state == State.HARVESTED) return false;
    TextureRegion r = resolveRegion();
    float halfW = Math.max(12f, r == null ? 16f : r.getRegionWidth() * .5f);
    float halfH = Math.max(12f, r == null ? 16f : r.getRegionHeight() * .5f);
    return worldX >= position.x - halfW
        && worldX <= position.x + halfW
        && worldY >= position.y - halfH
        && worldY <= position.y + halfH;
  }

  public void render(SpriteBatch batch, ShaderProgram outlineShader) {
    TextureRegion r = resolveRegion();
    if (r == null || state == State.HARVESTED) return;
    float w = r.getRegionWidth();
    float h = r.getRegionHeight();
    boolean outline = hovered && outlineShader != null && outlineShader.isCompiled();
    if (outline) {
      batch.setShader(outlineShader);
      outlineShader.setUniformf("u_texelSize", 1f / w, 1f / h);
      outlineShader.setUniformf("u_outlineColor", 1f, 1f, 0f, 1f);
    }
    batch.draw(
        r.getTexture(),
        position.x - w * .5f,
        position.y - h,
        w,
        h,
        r.getRegionX(),
        r.getRegionY(),
        r.getRegionWidth(),
        r.getRegionHeight(),
        false,
        true);
    if (outline) batch.setShader(null);
    if (isNameVisible()) {
      ItemDefinition item = ItemDefinition.get(definition.getItemKey());
      String label = I18n.resolve(item == null ? definition.getItemKey() : item.getName());
      NameRenderer.renderName(
          batch,
          label,
          position.x - 15f,
          position.y - h + NAME_RENDERER_VERTICAL_OFFSET - NAME_LABEL_ABOVE_HERB,
          w,
          h);
    }
  }

  private TextureRegion resolveRegion() {
    if (regionResolved) return region;
    regionResolved = true;
    try {
      region = SpriteLoader.getInstance().getRegionFromSpriteName(definition.getWorldSprite());
    } catch (GameException ignored) {
      region = null;
    }
    return region;
  }
}
