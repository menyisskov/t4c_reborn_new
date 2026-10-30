package com.perso.T4C.gui.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.OnlineCharacterView;
import com.perso.T4C.player.Player;
import com.perso.T4C.session.RemoteLink;
import com.perso.T4C.session.RemotePlayerCommands;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class StatisticsRemoteAllocationTest {
  @Test
  void onlineAllocationChangesThePlayerOnlyAfterTheServerAcceptsIt() throws Exception {
    var next = new AtomicReference<CompletableFuture<OnlineConnection.CommandResult>>();
    RemoteLink link = (RemoteLink) Proxy.newProxyInstance(
        RemoteLink.class.getClassLoader(), new Class<?>[] {RemoteLink.class},
        (proxy, method, args) -> {
          if (method.getName().equals("allocateStats")) {
            var result = new CompletableFuture<OnlineConnection.CommandResult>();
            next.set(result);
            return result;
          }
          return null;
        });
    var commands = new RemotePlayerCommands(link, monster -> 0L);
    Player player = new Player();
    player.setStrength(10);
    player.setEndurance(11);
    player.setDexterity(12);
    player.setWisdom(13);
    player.setIntelligence(14);
    player.setStatPoints(3);
    Statistics statistics = newScreenWithoutLibGdx();
    set(statistics, "player", player);
    set(statistics, "remotePlayerCommands", commands);
    set(statistics, "pendingAllocations", new LinkedHashMap<>(Map.of("strength", 2)));
    set(statistics, "statUpButtons", new LinkedHashMap<>());
    set(statistics, "statDownButtons", new LinkedHashMap<>());
    Method apply = Statistics.class.getDeclaredMethod("applyPendingAllocations");
    apply.setAccessible(true);

    apply.invoke(statistics);
    assertEquals(10, player.getStrength());
    assertEquals(3, player.getStatPoints());
    assertTrue(next.get() != null);

    var accepted = new OnlineConnection.CommandResult(true, character(12, 1), "online.stats.applied", List.of(2));
    next.get().complete(accepted);
    assertEquals(10, player.getStrength(), "A network reply is not applied on the network thread");
    commands.pump();
    assertEquals(12, player.getStrength());
    assertEquals(1, player.getStatPoints());
    assertTrue(pending(statistics).isEmpty());

    pending(statistics).put("wisdom", 1);
    apply.invoke(statistics);
    var refused = new OnlineConnection.CommandResult(false, character(12, 1), "online.stats.no_points");
    next.get().complete(refused);
    commands.pump();
    assertEquals(13, player.getWisdom(), "A refusal does not apply a local attribute change");
    assertEquals(1, player.getStatPoints());
    assertEquals(1, pending(statistics).get("wisdom"));
    assertFalse(inFlight(statistics));
  }

  private static OnlineCharacterView character(int strength, int statPoints) {
    var progress = new CharacterProgress(
        2,
        "warrior",
        "male",
        new CharacterProgress.Attributes(strength, 11, 12, 13, 14),
        new CharacterProgress.Vitals(60, 60, 20, 20),
        new CharacterProgress.Advancement(2, statPoints, 0, 0, 0),
        List.of(), Map.of(), Map.of(), Set.of(), Map.of(), List.of(), 0,
        new CharacterProgress.Respawn(0, 10, 10), List.of(), Map.of(), 0, null);
    return new OnlineCharacterView(UUID.randomUUID(), "Remote", 0, 0, 8, progress, 0);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Integer> pending(Statistics statistics) throws Exception {
    return (Map<String, Integer>) get(statistics, "pendingAllocations");
  }

  private static boolean inFlight(Statistics statistics) throws Exception {
    return (boolean) get(statistics, "remoteAllocationInFlight");
  }

  private static Object get(Object target, String fieldName) throws Exception {
    Field field = Statistics.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(target);
  }

  private static void set(Object target, String fieldName, Object value) throws Exception {
    Field field = Statistics.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static Statistics newScreenWithoutLibGdx() throws Exception {
    Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    field.setAccessible(true);
    return (Statistics) ((sun.misc.Unsafe) field.get(null)).allocateInstance(Statistics.class);
  }
}
