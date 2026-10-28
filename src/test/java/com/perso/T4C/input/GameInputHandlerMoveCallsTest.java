package com.perso.T4C.input;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.input.MoveCallRecording.Call;
import com.perso.T4C.player.Player;
import com.perso.T4C.player.PlayerMovement;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Pins the movement intent {@link GameInputHandler#handleInput} emits each frame: the exact
 * (dx, dy, delta) triple, once per frame, in order, for keyboard, mouse-drag and click-path
 * steering, and for the states that suppress or override it (typing, Ctrl, an open window). Stun,
 * exhaustion and attack gating live inside the movement rule itself; the handler must keep calling
 * it every frame so that a click path or a held key stops cleanly. The recording sits at the
 * boundary the input handler hands movement to, so the same assertions hold when that boundary is
 * the session's commands.
 */
@ResourceLock("Gdx.input")
class GameInputHandlerMoveCallsTest {
  private static final float ASPECT = (float) GRID_H / (float) GRID_W;

  private final Set<Integer> held = new HashSet<>();
  private Input previousInput;

  @BeforeEach
  void fakeKeyboard() {
    previousInput = Gdx.input;
    Gdx.input =
        (Input)
            Proxy.newProxyInstance(
                Input.class.getClassLoader(),
                new Class<?>[] {Input.class},
                (proxy, method, args) ->
                    method.getName().equals("isKeyPressed") && held.contains((int) args[0]));
  }

  @AfterEach
  void restore() {
    Gdx.input = previousInput;
    GuiManager.close();
  }

  private final List<Call> calls = new ArrayList<>();

  private GameInputHandler handler() {
    return MoveCallRecording.handler(calls);
  }

  private void frame(GameInputHandler handler, float delta) {
    MoveCallRecording.frame(handler, delta);
  }

  @Test
  void noInputStillMovesWithAZeroDirectionEveryFrameInOrder() {
    GameInputHandler handler = handler();
    float[] deltas = {0.016f, 0.033f, 0.004f, 0.1f};
    for (float delta : deltas) frame(handler, delta);
    assertEquals(
        List.of(
            new Call(0, 0, 0.016f),
            new Call(0, 0, 0.033f),
            new Call(0, 0, 0.004f),
            new Call(0, 0, 0.1f)),
        calls);
  }

  @Test
  void keyboardDirectionsProjectToTheTileAspectAndArriveEveryFrame() {
    GameInputHandler handler = handler();
    held.add(Input.Keys.D);
    frame(handler, 0.016f);
    held.clear();
    held.add(Input.Keys.S);
    frame(handler, 0.016f);
    held.add(Input.Keys.A); // down + left
    frame(handler, 0.02f);
    held.clear();
    held.add(Input.Keys.UP);
    frame(handler, 0.016f);
    held.clear();
    frame(handler, 0.016f);
    assertEquals(
        List.of(
            new Call(1f, 0f, 0.016f),
            new Call(0f, ASPECT, 0.016f),
            new Call(-1f, ASPECT, 0.02f),
            new Call(0f, -ASPECT, 0.016f),
            new Call(0f, 0f, 0.016f)),
        calls);
  }

  @Test
  void everyAliasKeyMapsToItsDirectionAndOpposingKeysCancel() {
    GameInputHandler handler = handler();
    int[][] keys = {
      {Input.Keys.Q, -1, 0}, {Input.Keys.LEFT, -1, 0}, {Input.Keys.RIGHT, 1, 0},
      {Input.Keys.Z, 0, -1}, {Input.Keys.W, 0, -1}, {Input.Keys.DOWN, 0, 1}
    };
    for (int[] key : keys) {
      held.clear();
      held.add(key[0]);
      frame(handler, 0.016f);
    }
    held.clear();
    held.add(Input.Keys.A);
    held.add(Input.Keys.D);
    held.add(Input.Keys.W);
    held.add(Input.Keys.S);
    frame(handler, 0.016f);
    assertEquals(7, calls.size());
    for (int i = 0; i < keys.length; i++) {
      assertEquals(new Call(keys[i][1], keys[i][2] * ASPECT, 0.016f), calls.get(i), "key " + i);
    }
    assertEquals(new Call(0f, 0f, 0.016f), calls.get(6));
  }

  @Test
  void controlSuppressesKeyboardAndMouseSteeringButMovementIsStillCalled() {
    GameInputHandler handler = handler();
    handler.setMouseMovementSupplier(() -> new Vector2(1f, 0f));
    held.add(Input.Keys.CONTROL_LEFT);
    held.add(Input.Keys.D);
    frame(handler, 0.016f);
    held.clear();
    held.add(Input.Keys.CONTROL_RIGHT);
    frame(handler, 0.016f);
    assertEquals(List.of(new Call(0, 0, 0.016f), new Call(0, 0, 0.016f)), calls);
  }

  @Test
  void typingSuppressesKeyboardAndMouseSteeringButMovementIsStillCalled() {
    GameInputHandler handler = handler();
    handler.setMouseMovementSupplier(() -> new Vector2(1f, 0f));
    handler.setTextInputActiveSupplier(() -> true);
    held.add(Input.Keys.D);
    frame(handler, 0.016f);
    assertEquals(List.of(new Call(0, 0, 0.016f)), calls);
  }

  @Test
  void dragSteeringPassesTheMouseDirectionAndIsFollowedByAStopWhenReleased() {
    GameInputHandler handler = handler();
    Vector2[] mouse = {new Vector2(1f, ASPECT), null, new Vector2(-1f, 0f)};
    int[] index = {0};
    handler.setMouseMovementSupplier(() -> mouse[index[0]]);
    for (int i = 0; i < mouse.length; i++) {
      index[0] = i;
      frame(handler, 0.016f);
    }
    assertEquals(
        List.of(new Call(1f, ASPECT, 0.016f), new Call(0, 0, 0.016f), new Call(-1f, 0f, 0.016f)),
        calls);
  }

  @Test
  void keyboardWinsOverTheMouseAndAnOpenWindowBlocksTheMouse() {
    GameInputHandler handler = handler();
    handler.setMouseMovementSupplier(() -> new Vector2(-1f, 0f));
    held.add(Input.Keys.D);
    frame(handler, 0.016f);
    held.clear();
    GuiManager.open(new Window());
    frame(handler, 0.016f);
    GuiManager.close();
    frame(handler, 0.016f);
    assertEquals(
        List.of(new Call(1f, 0f, 0.016f), new Call(0, 0, 0.016f), new Call(-1f, 0f, 0.016f)),
        calls);
  }

  @Test
  void aClickPathIsAdvancedByZeroDirectionCallsAndAnExhaustedPlayerIsStoppedByThem() {
    GameInputHandler handler = handler();
    for (int i = 0; i < 3; i++) frame(handler, 0.016f);
    assertEquals(3, calls.size());
    assertTrue(calls.stream().allMatch(c -> c.dx() == 0f && c.dy() == 0f));
    Player player = new Player();
    player.setMapBounds(1_000, 1_000);
    player.getMovement().setPath(List.of(new PlayerMovement.PathNode(10, 10)));
    player.applyExhaustion(1_000L, 750L, 750L);
    float x = player.getPositionVector().x;
    float y = player.getPositionVector().y;
    frame(MoveCallRecording.handlerFor(player), 0.016f);
    assertEquals(x, player.getPositionVector().x);
    assertEquals(y, player.getPositionVector().y);
    assertTrue(player.getMovement().getPath().isEmpty(), "the exhausted frame cancels the path");
  }

  private static final class Window extends GuiScreenBase {
    @Override
    protected List<GuiElement> extraElements() {
      return List.of();
    }
  }
}
