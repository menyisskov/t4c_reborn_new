package com.perso.T4C.gui.widget;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.player.PlayerMovement;
import java.util.Map;

public class GuiPlayerPreview extends AbstractGuiElement {
  private static final float ROTATION_STEP_TIME = 0.5f;
  private static final int DEFAULT_WIDTH = 64;
  private static final int DEFAULT_HEIGHT = 96;
  private static final float[][] ROTATION_DIRECTIONS = {
    {0f, 1f},
    {-1f, 1f},
    {-1f, 0f},
    {-1f, -1f},
    {0f, -1f},
    {1f, -1f},
    {1f, 0f},
    {1f, 1f}
  };
  private final Player player;
  private final PlayerMovement movement = new PlayerMovement();
  private final Vector2 renderPos = new Vector2();
  private float rotationTimer;
  private int rotationIndex;
  private int boundsWidth = DEFAULT_WIDTH;
  private int boundsHeight = DEFAULT_HEIGHT;

  public GuiPlayerPreview(Player player, float x, float y) {
    super(x, y);
    this.player = player;
    resolveBounds();
    applyRotationDirection();
  }

  public void render(SpriteBatch batch) {
    if (player == null) {
      return;
    }
    rotationTimer += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
    if (rotationTimer >= ROTATION_STEP_TIME) {
      rotationTimer = 0f;
      rotationIndex = (rotationIndex + 1) % ROTATION_DIRECTIONS.length;
      applyRotationDirection();
    }
    renderPos.set(x, y);
    player.getAnimations().render(batch, renderPos, movement);
  }

  @Override
  public float getWidth() {
    return boundsWidth;
  }

  @Override
  public float getHeight() {
    return boundsHeight;
  }

  private void applyRotationDirection() {
    float[] dir = ROTATION_DIRECTIONS[rotationIndex];
    movement.faceToward(0f, 0f, dir[0], dir[1]);
  }

  private void resolveBounds() {
    if (player == null || player.getAnimations() == null) {
      return;
    }
    Map<BodyPart, String> parts = player.getAnimations().getPartMap();
    if (parts == null || !parts.containsKey(BodyPart.BODY)) {
      return;
    }
    String base = parts.get(BodyPart.BODY);
    if (base == null || base.isEmpty()) {
      return;
    }
    TextureRegion region = GuiSprites.load(base + "000-a");
    if (region == null) {
      region = GuiSprites.load(base + "000");
    }
    if (region != null) {
      boundsWidth = region.getRegionWidth();
      boundsHeight = region.getRegionHeight();
    }
  }
}
