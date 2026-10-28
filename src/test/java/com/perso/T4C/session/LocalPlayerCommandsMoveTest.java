package com.perso.T4C.session;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.player.Player;
import java.lang.reflect.Proxy;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/** The local move intent is exactly {@code Player.move}. */
@ResourceLock("CollisionManager")
class LocalPlayerCommandsMoveTest {
  @AfterEach
  void clear() {
    CollisionManager.getInstance().clear();
  }

  private static LocalPlayerCommands bound(Player player) {
    LocalPlayerCommands commands = new LocalPlayerCommands();
    commands.bind(
        (LocalAttackHost)
            Proxy.newProxyInstance(
                LocalAttackHost.class.getClassLoader(),
                new Class<?>[] {LocalAttackHost.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("player")) return player;
                  throw new UnsupportedOperationException(method.getName());
                }));
    return commands;
  }

  private static Player player() {
    Player player = new Player();
    player.setMapBounds(500 * GRID_W, 500 * GRID_H);
    player.setWorldPosition(100 * GRID_W, 100 * GRID_H, 0);
    return player;
  }

  @Test
  void movingThroughTheIntentEqualsMovingThePlayerDirectly() {
    Player viaCommands = player();
    Player direct = player();
    LocalPlayerCommands commands = bound(viaCommands);
    Random random = new Random(3);
    for (int i = 0; i < 300; i++) {
      float dx = random.nextInt(3) - 1;
      float dy = (random.nextInt(3) - 1) * 0.5f;
      float delta = 0.005f + random.nextFloat() * 0.05f;
      commands.move(dx, dy, delta);
      direct.move(dx, dy, delta);
      assertEquals(direct.getCoordinates().getX(), viaCommands.getCoordinates().getX());
      assertEquals(direct.getCoordinates().getY(), viaCommands.getCoordinates().getY());
      assertEquals(direct.getMovement().isMoving(), viaCommands.getMovement().isMoving());
    }
  }

  @Test
  void usingTheIntentBeforeTheScreenBoundItFailsLoudly() {
    assertThrows(IllegalStateException.class, () -> new LocalPlayerCommands().move(1, 0, 0.016f));
  }
}
