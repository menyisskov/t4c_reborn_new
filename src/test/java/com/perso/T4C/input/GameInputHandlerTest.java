package com.perso.T4C.input;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

class GameInputHandlerTest {
  @Test
  void aBoundProtectionMacroCannotAlsoReloadGraphicsOrInvalidateAnOpenJournal() {
    var player = new com.perso.T4C.player.Player();
    int modifiers =
        com.perso.T4C.config.MacroBinding.MOD_CTRL | com.perso.T4C.config.MacroBinding.MOD_SHIFT;
    assertTrue(GameInputHandler.resourceReloadAllowed(player, modifiers, false));
    assertFalse(GameInputHandler.resourceReloadAllowed(player, modifiers, true));
    var macro = com.perso.T4C.config.MacroBinding.forItem("gem_of_unstable_protection");
    macro.setKeycode(com.badlogic.gdx.Input.Keys.R);
    macro.setModifiers(modifiers);
    player.getMacros().add(macro);
    assertFalse(GameInputHandler.resourceReloadAllowed(player, modifiers, false));
    assertFalse(GameInputHandler.resourceReloadAllowed(player, modifiers, true));
    assertSame(macro, GameInputHandler.resolveMacro(player, Input.Keys.R, modifiers, true, false));
    assertNull(
        GameInputHandler.resolveMacro(player, Input.Keys.R, modifiers, true, true),
        "Typing and key-binding capture must still block macros");
  }

  @Test
  void diagonalAdvancesEquallyOnBothGridAxes() {
    Vector2 direction = GameInputHandler.projectGridDirection(1, 1, new Vector2());
    assertEquals(direction.x / GRID_W, direction.y / GRID_H, 0.00001f);
    assertEquals(2f, direction.x / direction.y, 0.00001f);
    assertEquals(1f, direction.x, 0.00001f);
    assertEquals(0.5f, direction.y, 0.00001f);
  }

  @Test
  void oppositeInputsCancelAndCardinalDirectionsUseTileAspect() {
    Vector2 stopped = GameInputHandler.projectGridDirection(0, 0, new Vector2(3f, 4f));
    assertEquals(0f, stopped.len2());
    Vector2 horizontal = GameInputHandler.projectGridDirection(1, 0, new Vector2());
    Vector2 vertical = GameInputHandler.projectGridDirection(0, 1, new Vector2());
    assertEquals(horizontal.x / GRID_W, vertical.y / GRID_H, 0.00001f);
  }

  @Test
  void mouseSteeringUsesTheSameGridDirectionsAndStopsAtTheCursor() {
    Vector2 mouse = MouseMovementHandler.projectDirection(GRID_W, GRID_H, new Vector2());
    assertEquals(GameInputHandler.projectGridDirection(1, 1, new Vector2()), mouse);
    assertEquals(new Vector2(1, 0), MouseMovementHandler.projectDirection(1000, 5, new Vector2()));
    assertEquals(0, MouseMovementHandler.projectDirection(2, 1, new Vector2()).len2());
  }

  @Test
  void groundDragStartsAfterThresholdButUiAndEntityClicksCannotStartSteering() {
    var consumed = new java.util.concurrent.atomic.AtomicBoolean(true);
    var blocked = new java.util.concurrent.atomic.AtomicBoolean(false);
    int[] begun = {0};
    var handler = new MouseMovementHandler(null, null, blocked::get, () -> begun[0]++);
    var inputs =
        new com.badlogic.gdx.InputMultiplexer(
            new com.badlogic.gdx.InputAdapter() {
              @Override
              public boolean touchDown(int x, int y, int pointer, int button) {
                return consumed.get();
              }
            },
            handler);
    inputs.touchDown(100, 100, 0, 0);
    inputs.touchDragged(200, 100, 0);
    assertFalse(handler.isDragging());
    consumed.set(false);
    inputs.touchDown(100, 100, 0, 0);
    inputs.touchDragged(103, 100, 0);
    assertFalse(handler.isDragging());
    inputs.touchDragged(110, 100, 0);
    inputs.touchDragged(120, 100, 0);
    assertTrue(handler.isDragging());
    assertEquals(1, begun[0]);
    inputs.touchUp(120, 100, 0, 0);
    assertFalse(handler.isDragging());
    blocked.set(true);
    assertFalse(handler.touchDown(100, 100, 0, 0));
    assertFalse(handler.touchDragged(200, 100, 0));
    assertEquals(1, begun[0]);
  }
}
