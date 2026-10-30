package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.model.Coordinates;
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
import sun.misc.Unsafe;
import org.junit.jupiter.api.Test;

class MainGameScreenRemoteAreaSpellTest {
  @Test
  void remoteAreaCastRoutesKeyWithoutMutatingLocalPlayerOrCreatures() throws Exception {
    AtomicReference<String> castKey = new AtomicReference<>();
    RemoteLink link =
        (RemoteLink)
            Proxy.newProxyInstance(
                RemoteLink.class.getClassLoader(),
                new Class<?>[] {RemoteLink.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("cast")) {
                    castKey.set((String) args[0]);
                    return CompletableFuture.completedFuture(
                        new com.perso.T4C.online.OnlineConnection.CommandResult(true, null));
                  }
                  if (method.getName().equals("view")) return null;
                  return CompletableFuture.completedFuture(
                      new com.perso.T4C.online.OnlineConnection.CommandResult(true, null));
                });
    MainGameScreen screen = newScreenWithoutLibGdx();
    set(screen, "session", new RemoteGameSession(link, null));
    Player player = new Player();
    player.setMaxHp(100);
    player.setCurrentHp(60);
    player.setMaxMana(100);
    player.setMana(80);
    player.setWorldPosition(320, 640, 2);
    set(screen, "player", player);
    Coordinates coordinates = player.getCoordinates();
    float x = coordinates.getX();
    float y = coordinates.getY();
    int hp = player.getCurrentHp();
    int mana = player.getMana();
    int buffs = player.getActiveBuffs().size();

    Method cast = MainGameScreen.class.getDeclaredMethod("castSelfCenteredAreaSpell", SpellData.class);
    cast.setAccessible(true);
    cast.invoke(screen, areaSpell());

    assertEquals("spell.area_blast", castKey.get());
    assertEquals(hp, player.getCurrentHp());
    assertEquals(mana, player.getMana());
    assertEquals(buffs, player.getActiveBuffs().size());
    assertEquals(x, player.getCoordinates().getX());
    assertEquals(y, player.getCoordinates().getY());
  }

  private static SpellData areaSpell() {
    return new SpellData(
        "spell.area_blast", "Area Blast", "25", 2, 1, 1, 0, true, false, null, null, null,
        20, 30, null, null, 0, null, null, 0, null, 1, 0, 18, 1,
        null, null, null, null, 0, 0, false, List.of());
  }

  private static void set(Object target, String fieldName, Object value) throws Exception {
    Field field = MainGameScreen.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static MainGameScreen newScreenWithoutLibGdx() throws Exception {
    Field field = Unsafe.class.getDeclaredField("theUnsafe");
    field.setAccessible(true);
    return (MainGameScreen) ((Unsafe) field.get(null)).allocateInstance(MainGameScreen.class);
  }
}
