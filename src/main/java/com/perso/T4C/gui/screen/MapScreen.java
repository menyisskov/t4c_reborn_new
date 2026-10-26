package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.JournalWindow;
import com.perso.T4C.gui.widget.GuiWorldMap;
import com.perso.T4C.player.Player;
import java.util.List;

public final class MapScreen extends JournalWindow {
  private final GuiWorldMap worldMap;

  public MapScreen(Player player) {
    initJournal(688, 448, "ui.world_map");
    worldMap = new GuiWorldMap(player, x + 24, y, 640f, 448f);
  }

  @Override
  protected List<GuiElement> extraElements() {
    return List.of(worldMap);
  }

  @Override
  public void render(SpriteBatch batch) {
    super.render(batch);
  }

  @Override
  protected boolean isBackgroundHit(float screenX, float screenY) {
    return super.isBackgroundHit(screenX, screenY);
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
    super.dispose();
  }

  private boolean isControlDown() {
    return com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
        || com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
  }
}
