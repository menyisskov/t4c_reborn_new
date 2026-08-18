package com.perso.T4C.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.perso.T4C.harvest.HerbManager;
import com.perso.T4C.harvest.HerbNode;
import java.util.function.Predicate;

public final class HerbInputHandler extends InputAdapter {
  private final OrthographicCamera camera;
  private final HerbManager herbs;
  private final Predicate<HerbNode> startHarvest;
  private final Vector3 world = new Vector3();

  public HerbInputHandler(
      OrthographicCamera camera, HerbManager herbs, Predicate<HerbNode> startHarvest) {
    this.camera = camera;
    this.herbs = herbs;
    this.startHarvest = startHarvest;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    unproject(screenX, screenY);
    herbs.onMouseMove(world.x, world.y);
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (button != Input.Buttons.LEFT && button != Input.Buttons.RIGHT) return false;
    unproject(screenX, screenY);
    if (button == Input.Buttons.RIGHT) return herbs.showNameAt(world.x, world.y);
    HerbNode node = herbs.findAt(world.x, world.y);
    return node != null && startHarvest.test(node);
  }

  private void unproject(int screenX, int screenY) {
    world.set(screenX, screenY, 0f);
    camera.unproject(world);
  }
}
