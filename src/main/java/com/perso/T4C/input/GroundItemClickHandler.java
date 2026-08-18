package com.perso.T4C.input;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.OBJECT_MAX_INTERACTION_DISTANCE;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.objects.GroundItemManager;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.SystemMessage;

public class GroundItemClickHandler extends InputAdapter {
  private final OrthographicCamera camera;
  private final GroundItemManager groundItemManager;
  private final Player player;
  private final SystemMessage systemMessage;
  private final Vector3 worldCoordsTemp = new Vector3();

  public GroundItemClickHandler(
      OrthographicCamera camera,
      GroundItemManager groundItemManager,
      Player player,
      SystemMessage systemMessage) {
    this.camera = camera;
    this.groundItemManager = groundItemManager;
    this.player = player;
    this.systemMessage = systemMessage;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    worldCoordsTemp.set(screenX, screenY, 0);
    camera.unproject(worldCoordsTemp);
    groundItemManager.onMouseMove(worldCoordsTemp.x, worldCoordsTemp.y);
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (button != Input.Buttons.LEFT && button != Input.Buttons.RIGHT) {
      return false;
    }
    worldCoordsTemp.set(screenX, screenY, 0);
    camera.unproject(worldCoordsTemp);
    if (button == Input.Buttons.RIGHT) {
      return groundItemManager.showNameAt(worldCoordsTemp.x, worldCoordsTemp.y);
    }
    int clickTileX = (int) (worldCoordsTemp.x / GRID_W);
    int clickTileY = (int) (worldCoordsTemp.y / GRID_H);
    int playerTileX = (int) (player.getCoordinates().getX() / GRID_W);
    int playerTileY = (int) (player.getCoordinates().getY() / GRID_H);
    int tileDistance =
        Math.max(Math.abs(clickTileX - playerTileX), Math.abs(clickTileY - playerTileY));
    GroundItemManager.PickupResult result =
        groundItemManager.pickUpAt(
            worldCoordsTemp.x,
            worldCoordsTemp.y,
            player,
            tileDistance,
            OBJECT_MAX_INTERACTION_DISTANCE);
    switch (result.status) {
      case PICKED_UP:
        if (systemMessage != null) {
          systemMessage.show(I18n.message("message.item_picked_up", I18n.resolve(result.label)));
        }
        return true;
      case TOO_FAR:
        if (systemMessage != null) {
          systemMessage.show(I18n.message("message.pickup_too_far"));
        }
        return true;
      case TOO_HEAVY:
        if (systemMessage != null) systemMessage.show(I18n.message("message.item_too_heavy"));
        return true;
      case UNIQUE_ITEM:
        if (systemMessage != null) systemMessage.show(I18n.message("message.unique_item_owned"));
        return true;
      case NONE:
      default:
        return false;
    }
  }
}
