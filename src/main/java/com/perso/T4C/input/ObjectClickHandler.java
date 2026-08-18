package com.perso.T4C.input;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.objects.ChestService;
import com.perso.T4C.objects.GroundItemManager;
import com.perso.T4C.objects.ObjectPos;
import com.perso.T4C.player.Player;
import com.perso.T4C.render.ObjectRenderer;
import com.perso.T4C.screens.MapRenderer;
import com.perso.T4C.ui.SystemMessage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectClickHandler extends InputAdapter {
  private static final Logger log = LoggerFactory.getLogger(ObjectClickHandler.class);
  private final ObjectRenderer objectRenderer;
  private final List<ObjectPos> objectPositions;
  private final MapRenderer mapRenderer;
  private final OrthographicCamera camera;
  private final Player player;
  private final SystemMessage systemMessage;
  private final ChestService chestService = new ChestService();
  private final GroundItemManager groundItemManager;

  public ObjectClickHandler(
      ObjectRenderer objectRenderer,
      List<ObjectPos> objectPositions,
      MapRenderer mapRenderer,
      OrthographicCamera camera,
      Player player,
      SystemMessage systemMessage,
      GroundItemManager groundItemManager) {
    this.objectRenderer = objectRenderer;
    this.objectPositions = objectPositions;
    this.mapRenderer = mapRenderer;
    this.camera = camera;
    this.player = player;
    this.systemMessage = systemMessage;
    this.groundItemManager = groundItemManager;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (objectPositions == null || objectPositions.isEmpty()) {
      return false;
    }
    Vector3 worldCoords = screenToWorldCoords(screenX, screenY);
    if (button == com.badlogic.gdx.Input.Buttons.RIGHT) {
      return objectRenderer.handleRightClick(
          worldCoords.x, worldCoords.y, objectPositions, mapRenderer.getObjectMappings());
    }
    if (button == com.badlogic.gdx.Input.Buttons.LEFT) {
      int clickTileX = (int) (worldCoords.x / GRID_W);
      int clickTileY = (int) (worldCoords.y / GRID_H);
      com.perso.T4C.model.Coordinates coords = player.getCoordinates();
      int playerTileX = (int) (coords.getX() / GRID_W);
      int playerTileY = (int) (coords.getY() / GRID_H);
      int distanceX = Math.abs(clickTileX - playerTileX);
      int distanceY = Math.abs(clickTileY - playerTileY);
      int tileDistance = Math.max(distanceX, distanceY);
      boolean objectClicked =
          objectRenderer.handleClick(
              worldCoords.x,
              worldCoords.y,
              objectPositions,
              mapRenderer.getObjectMappings(),
              tileDistance,
              com.perso.T4C.config.GameConstants.OBJECT_MAX_INTERACTION_DISTANCE);
      if (objectClicked) {
        if (tileDistance > com.perso.T4C.config.GameConstants.OBJECT_MAX_INTERACTION_DISTANCE) {
          systemMessage.show(I18n.message("message.object_too_far"));
          log.info(
              "Object too far - Distance: {} tiles (max: {})",
              tileDistance,
              com.perso.T4C.config.GameConstants.OBJECT_MAX_INTERACTION_DISTANCE);
        } else {
          ChestService.Result chest =
              chestService.open(player, objectRenderer.getLastClickedObject(), groundItemManager);
          if (chest.opened()) {
            PlayerStateStore.save(player);
            systemMessage.show(chestService.message(chest));
          } else if (chest.failure() == ChestService.Failure.EMPTY
              || chest.failure() == ChestService.Failure.COOLDOWN) {
            systemMessage.show(I18n.message("message.chest_empty"));
          }
          log.info(
              "Object interaction handled at world coords ({}, {}, {})",
              worldCoords.x,
              worldCoords.y,
              coords.getZ());
        }
        return true;
      }
    }
    return false;
  }

  private Vector3 screenToWorldCoords(int screenX, int screenY) {
    return camera.unproject(new Vector3(screenX, screenY, 0));
  }
}
