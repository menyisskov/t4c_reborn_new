package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.ui.PlayerHUD;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class MainGameScreenHudButtonTest {
  @Test
  void onlineHudButtonRoutingUsesRawScreenCoordinatesForLeftPressAndRelease() throws Exception {
    FakeHud hud = newFakeHud();

    assertTrue(
        MainGameScreen.routeRemoteHudButtonTouchDown(hud, 721, 64, Input.Buttons.LEFT, true));
    assertEquals(721, hud.downX);
    assertEquals(64, hud.downY);
    assertTrue(hud.controlDown);

    assertTrue(MainGameScreen.routeRemoteHudButtonTouchUp(hud, 721, 64, Input.Buttons.LEFT));
    assertEquals(721, hud.upX);
    assertEquals(64, hud.upY);

    assertFalse(
        MainGameScreen.routeRemoteHudButtonTouchDown(hud, 721, 64, Input.Buttons.RIGHT, false));
    assertFalse(MainGameScreen.routeRemoteHudButtonTouchUp(hud, 721, 64, Input.Buttons.RIGHT));
    assertFalse(MainGameScreen.routeRemoteHudButtonTouchDown(null, 721, 64, Input.Buttons.LEFT, false));
  }

  private static FakeHud newFakeHud() throws Exception {
    Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    field.setAccessible(true);
    return (FakeHud) ((sun.misc.Unsafe) field.get(null)).allocateInstance(FakeHud.class);
  }

  private static final class FakeHud extends PlayerHUD {
    int downX;
    int downY;
    int upX;
    int upY;
    boolean controlDown;

    private FakeHud() throws GameException {
      super(null, null);
    }

    @Override
    public boolean chatBarButtonsTouchDown(float screenX, float screenY, boolean controlDown) {
      downX = (int) screenX;
      downY = (int) screenY;
      this.controlDown = controlDown;
      return true;
    }

    @Override
    public boolean chatBarButtonsTouchUp(float screenX, float screenY) {
      upX = (int) screenX;
      upY = (int) screenY;
      return true;
    }
  }
}
