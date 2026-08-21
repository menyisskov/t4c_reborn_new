package com.perso.T4C.input;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.SystemMessage;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonsterInputHandler extends InputAdapter {
  private final OrthographicCamera camera;
  private final MonsterManager monsterManager;
  private final Player player;
  private final Function<BaseMonster, Boolean> spellCastHandler;
  private final Function<BaseMonster, Boolean> bowAttackHandler;
  private final SystemMessage systemMessage;
  private final Vector3 worldCoordsTemp = new Vector3();
  private Consumer<BaseMonster> onAttackTargetSelected;
  private Runnable onClickedElsewhere;

  public void setOnAttackTargetSelected(Consumer<BaseMonster> onAttackTargetSelected) {
    this.onAttackTargetSelected = onAttackTargetSelected;
  }

  public void setOnClickedElsewhere(Runnable onClickedElsewhere) {
    this.onClickedElsewhere = onClickedElsewhere;
  }

  public MonsterInputHandler(
      OrthographicCamera camera,
      MonsterManager monsterManager,
      Player player,
      Function<BaseMonster, Boolean> spellCastHandler,
      Function<BaseMonster, Boolean> bowAttackHandler,
      SystemMessage systemMessage) {
    this.camera = camera;
    this.monsterManager = monsterManager;
    this.player = player;
    this.spellCastHandler = spellCastHandler;
    this.bowAttackHandler = bowAttackHandler;
    this.systemMessage = systemMessage;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    Vector2 worldPos = screenToWorld(screenX, screenY);
    monsterManager.onMouseMove(worldPos.x, worldPos.y);
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (button == 0) {
      Vector2 worldPos = screenToWorld(screenX, screenY);
      for (BaseMonster monster : monsterManager.getMonsters()) {
        if (monster.isMouseOver(worldPos.x, worldPos.y) && !monster.isDead()) {
          if (!monster.canBeAttackedByPlayer()) {
            return true;
          }
          if (spellCastHandler != null && spellCastHandler.apply(monster)) {
            return true;
          }
          if (onAttackTargetSelected != null) {
            onAttackTargetSelected.accept(monster);
            return true;
          }
          if (bowAttackHandler != null && bowAttackHandler.apply(monster)) {
            return true;
          }
          boolean success = monsterManager.attackMonster(monster, player);
          if (!success) {
            log.info("Player is too far or not facing {} to attack!", monster.getName());
          }
          return true;
        }
      }
      if (onClickedElsewhere != null) {
        onClickedElsewhere.run();
      }
      return false;
    } else if (button == 1) {
      Vector2 worldPos = screenToWorld(screenX, screenY);
      return monsterManager.onRightClick(worldPos.x, worldPos.y);
    }
    return false;
  }

  private Vector2 screenToWorld(int screenX, int screenY) {
    worldCoordsTemp.set(screenX, screenY, 0);
    camera.unproject(worldCoordsTemp);
    return new Vector2(worldCoordsTemp.x, worldCoordsTemp.y);
  }
}
