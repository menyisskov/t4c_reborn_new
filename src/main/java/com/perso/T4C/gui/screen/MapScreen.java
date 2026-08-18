package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiWorldMap;
import com.perso.T4C.player.Player;
import java.util.List;

public final class MapScreen extends GuiScreenBase {
  private final GuiWorldMap worldMap;

  public MapScreen(Player player) {
    background = GuiSprites.load("GUI_RTMapBack");
    x = (Gdx.graphics.getWidth() - 640f) / 2f;
    y = Math.max(0f, (Gdx.graphics.getHeight() - 150f - 448f) / 2f);
    worldMap = new GuiWorldMap(player, x, y, 640f, 448f);
  }

  @Override
  protected List<GuiElement> extraElements() {
    return List.of(worldMap);
  }

  @Override
  public void render(SpriteBatch batch) {
    if (background != null) {
      GuiDraw.drawOverlayRegionFlipped(batch, background, x, y, 640f, 448f);
    }
    worldMap.render(batch);
  }

  @Override
  protected boolean isBackgroundHit(float screenX, float screenY) {
    return screenX >= x && screenX <= x + 640f && screenY >= y && screenY <= y + 448f;
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE || (keycode == Input.Keys.W && isControlDown())) {
      GuiManager.close();
      return true;
    }
    return false;
  }

  @Override
  public void dispose() {
    worldMap.dispose();
  }

  private boolean isControlDown() {
    return com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
        || com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
  }
}
