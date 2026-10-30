package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.player.Player;
import com.perso.T4C.session.RemoteGameSession;
import com.perso.T4C.session.RemoteLink;
import com.perso.T4C.spell.SpellData;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MainGameScreenRemotePositionSpellTest {
  @Test
  void positionTargetUsesRegionLocalTileAndDoesNotApplyLocalEffects() throws Exception {
    var target = new AtomicReference<int[]>();
    var map = new OnlineConnection.MapInfo(100, 200, 40, 30, true, 6, 0, 0, "avalon", 4L);
    var view = new OnlineConnection.View(
        7L,
        map,
        new OnlineConnection.Snapshot(1, List.of(), 1L, List.of(), map.region(), List.of(), List.of()),
        null,
        List.of());
    RemoteLink link = (RemoteLink) Proxy.newProxyInstance(
        RemoteLink.class.getClassLoader(),
        new Class<?>[] {RemoteLink.class},
        (proxy, method, args) -> {
          if (method.getName().equals("view")) return view;
          if (method.getName().equals("castAtPosition")) {
            target.set(new int[] {(int) args[1], (int) args[2]});
            return CompletableFuture.completedFuture(new OnlineConnection.CommandResult(true, null));
          }
          if (java.util.concurrent.CompletionStage.class.isAssignableFrom(method.getReturnType()))
            return CompletableFuture.completedFuture(new OnlineConnection.CommandResult(true, null));
          return null;
        });
    MainGameScreen screen = newScreenWithoutLibGdx();
    set(screen, "session", new RemoteGameSession(link, null));
    Player player = new Player();
    player.setMaxHp(100);
    player.setCurrentHp(70);
    player.setMaxMana(100);
    player.setMana(80);
    set(screen, "player", player);
    int hp = player.getCurrentHp();
    int mana = player.getMana();
    Vector2 clickedWorldPosition = new Vector2(
        (map.originX() + 5.75f) * GameConstants.GRID_W,
        (map.originY() + 3.25f) * GameConstants.GRID_H);

    Method route = MainGameScreen.class.getDeclaredMethod(
        "tryCastRemotePositionSpell", SpellData.class, Vector2.class, OnlineConnection.View.class);
    route.setAccessible(true);
    assertTrue((boolean) route.invoke(screen, tileSpell(), clickedWorldPosition, view));

    assertEquals(5, target.get()[0]);
    assertEquals(3, target.get()[1]);
    assertEquals(hp, player.getCurrentHp());
    assertEquals(mana, player.getMana());
  }

  private static SpellData tileSpell() {
    return new SpellData(
        "spell.tile_blast", "Tile Blast", "25", 1, 1, 0, 0, true, false, null, null, null,
        20, 30, null, null, 0, null, null, 0, null, 1, 0, 18, 1,
        null, null, null, null, 0, 0, false, List.of());
  }

  private static void set(Object target, String fieldName, Object value) throws Exception {
    Field field = MainGameScreen.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static MainGameScreen newScreenWithoutLibGdx() throws Exception {
    Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    field.setAccessible(true);
    return (MainGameScreen) ((sun.misc.Unsafe) field.get(null)).allocateInstance(MainGameScreen.class);
  }
}
