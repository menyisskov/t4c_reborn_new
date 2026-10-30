package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.objects.GroundItemManager;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CreatureView;
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

class MainGameScreenHostileSpellTargetTest {
  @Test
  void hostileSpellClickSendsLiveRemoteCreatureIdWithoutLocalImpact() {
    long creatureId = 42L;
    var map = new OnlineConnection.MapInfo(100, 200, 80, 60, true, 6, 0, 0, "avalon", 4L);
    var creature = new CreatureView(creatureId, "Skeleton", 10, 12, 30, 40, "walk", null, null);
    var view =
        new OnlineConnection.View(
            7L,
            map,
            new OnlineConnection.Snapshot(
                1, List.of(), 1L, List.of(creature), map.region(), List.of(), List.of()),
            null,
            List.of());
    AtomicLong routedCreature = new AtomicLong(-1);
    RemoteLink link =
        (RemoteLink)
            Proxy.newProxyInstance(
                RemoteLink.class.getClassLoader(),
                new Class<?>[] {RemoteLink.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("view")) return view;
                  if (method.getName().equals("cast")) {
                    routedCreature.set((long) args[1]);
                    return CompletableFuture.completedFuture(
                        new OnlineConnection.CommandResult(true, null));
                  }
                  return CompletableFuture.completedFuture(
                      new OnlineConnection.CommandResult(true, null));
                });

    var world = new RemoteWorldDriver(() -> view, new GroundItemManager());
    var entities = world.loadEntities(new WorldEntityInputs(null, null, null, () -> null, new Player()));
    world.tickEntities(
        new EntityTick(
            entities.npcs(), entities.monsters(), new Player(), new Vector2(), 0.016f, 0, 1000, 0, 1000, 5));
    var monster = entities.monsters().getMonsters().get(0);
    var commands =
        new RemotePlayerCommands(
            link,
            world::creatureId,
            new com.perso.T4C.world.MovementRules.Terrain() {
              public boolean hasCollision(float x, float y) { return false; }
              public boolean hasPlayerCollision(float x, float y) { return false; }
              public boolean canMoveForPlayer(float fromX, float fromY, float toX, float toY) { return true; }
              public boolean isPlayerPassableTile(int gridX, int gridY) { return true; }
            },
            () -> false);
    int initialHealth = monster.getHealth();

    assertTrue(MainGameScreen.tryCastHostileSpellOnRemoteMonster(hostileSpell(), monster, world, commands));
    assertEquals(creatureId, routedCreature.get());
    assertEquals(initialHealth, monster.getHealth(), "remote casts do not locally apply spell impact");
  }

  private static SpellData hostileSpell() {
    return new SpellData(
        "spell.fireball", "", "10", 1, 2, 0, 0, true, false, null, null, null,
        0, 0, null, null, 0, null, null, 0, null, 1, 0, 1, 0,
        null, null, null, null, 0, 0, false, List.of());
  }
}
