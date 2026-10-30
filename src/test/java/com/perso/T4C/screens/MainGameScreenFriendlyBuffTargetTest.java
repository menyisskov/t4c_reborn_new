package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.objects.GroundItemManager;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.player.Player;
import com.perso.T4C.session.EntityTick;
import com.perso.T4C.session.RemoteLink;
import com.perso.T4C.session.RemotePlayerCommands;
import com.perso.T4C.session.RemoteWorldDriver;
import com.perso.T4C.session.WorldEntityInputs;
import com.perso.T4C.spell.SpellData;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class MainGameScreenFriendlyBuffTargetTest {
  @Test
  void shiftTargetedTypeThreeBuffRoutesClickedRemotePlayerActorId() {
    long ownId = 7L;
    long otherId = 9L;
    var map = new OnlineConnection.MapInfo(100, 200, 80, 60, true, 6, 0, 0, "avalon", 4L);
    var players =
        List.of(
            new OnlineConnection.Actor(ownId, 3.5, 5.5, 1L, 0L, 100, 100),
            new OnlineConnection.Actor(otherId, 10.5, 12.5, 1L, 0L, 40, 80));
    var view =
        new OnlineConnection.View(
            ownId,
            map,
            new OnlineConnection.Snapshot(1, players, 1L, List.of(), map.region(), List.of(), List.of()),
            null,
            List.of());
    AtomicLong routedActor = new AtomicLong(-1);
    RemoteLink link =
        (RemoteLink)
            Proxy.newProxyInstance(
                RemoteLink.class.getClassLoader(),
                new Class<?>[] {RemoteLink.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("view")) return view;
                  if (method.getName().equals("castToPlayer")) {
                    routedActor.set((long) args[1]);
                    return CompletableFuture.completedFuture(new OnlineConnection.CommandResult(true, null));
                  }
                  return CompletableFuture.completedFuture(new OnlineConnection.CommandResult(true, null));
                });

    var world = new RemoteWorldDriver(() -> view, new GroundItemManager());
    var entities = world.loadEntities(new WorldEntityInputs(null, null, null, () -> null, new Player()));
    world.tickEntities(
        new EntityTick(entities.npcs(), entities.monsters(), new Player(), new Vector2(), 0.016f, 0, 1000, 0, 1000, 5));
    Player other = world.otherPlayers().get(0);
    var commands =
        new RemotePlayerCommands(
            link,
            ignored -> -1L,
            new com.perso.T4C.world.MovementRules.Terrain() {
              public boolean hasCollision(float x, float y) { return false; }
              public boolean hasPlayerCollision(float x, float y) { return false; }
              public boolean canMoveForPlayer(float fromX, float fromY, float toX, float toY) { return true; }
              public boolean isPlayerPassableTile(int gridX, int gridY) { return true; }
            },
            () -> false);
    SpellData friendlyBuff =
        new SpellData(
            "spell.barrier", "", "0", 0, 0, 0, 0, false, false, null, null, null,
            0, 0, null, null, 0, null, null, 0, null, 1, 0, 3, 0,
            null, null, null, null, 0, 0, false, List.of());

    assertTrue(
        MainGameScreen.tryCastFriendlySpellOnRemotePlayer(
            friendlyBuff, world, candidate -> candidate == other, commands));
    assertEquals(otherId, routedActor.get());
  }
}
